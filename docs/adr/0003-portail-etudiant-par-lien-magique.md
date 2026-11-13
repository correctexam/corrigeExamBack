# ADR 0003 — Portail étudiant : accès aux copies par lien magique et publication contrôlée par l'enseignant

- **Statut** : accepté (13/11/2026) ; le choix du captcha reste à valider (voir § 4)
- **Décideurs** : équipe CorrectExam (DiverSE / IRISA / Univ. Rennes)
- **Dépôts concernés** : `corrigeExamBack`, `corrigeExamFront`
- **Tâche** : T21 (prolonge T10c)

## Contexte

Aujourd'hui, une copie corrigée est accessible par un lien personnel `/copie/{uuid}/1` (l'UUID est `ExamSheet.name`), envoyé à chaque étudiant par `POST /api/sendResult/{examId}`. Ce modèle a plusieurs limites :

- envoi de mails en masse, à limiter en débit ;
- un étudiant qui perd le mail perd l'accès ; il n'a aucune vue d'ensemble de ses copies ni de ses données personnelles (RGPD) ;
- le lien n'expire jamais et ne peut pas être révoqué ;
- la vue élève est anonyme et lit ses données par des listes ouvertes filtrées par identifiants séquentiels (audit T10, constats S4 et S5) : l'UUID n'est pas la seule protection.

Une même personne a une fiche `Student` par cours ; la seule donnée commune fiable est l'adresse e-mail (`Student.mail`), parfois vide ou différente de l'adresse académique.

## Décision

### 1. Publication contrôlée par l'enseignant

`Exam.studentAccess` (colonne `student_access`) :

| Valeur | Portail étudiant |
|---|---|
| `HIDDEN` | l'examen n'est pas listé |
| `SHEET` | copie scannée seulement, sans annotations, commentaires ni note (**défaut d'un nouvel examen**) |
| `GRADED` | copie corrigée : annotations, commentaires et note |

- Les examens existants passent en `GRADED` à la migration (leurs résultats ont pu être envoyés).
- L'enseignant change l'état par `PUT /api/exams/{id}/student-access/{access}` (rôle + `canAccess`).
- Envoyer les résultats à **tous** les étudiants (`sendResult` sans `sheetuuid`) publie l'examen en `GRADED`. L'envoi à un seul étudiant ne change pas l'état de l'examen.

### 2. Les liens `/copie/{uuid}` sont conservés

Ils restent valides et montrent la copie corrigée, comme aujourd'hui : ce sont des accès donnés explicitement par l'enseignant (mail individuel ou collectif). `studentAccess` ne gouverne que le portail. Leur lecture passera par un endpoint dédié à une copie (T10c) au lieu des listes ouvertes.

### 3. Portail : lien magique envoyé à l'adresse de l'étudiant

1. `/my` : l'étudiant saisit une adresse e-mail et résout le captcha.
2. `POST /api/public/my/link` répond **toujours** la même chose, en un temps comparable, que l'adresse soit connue ou non (pas d'énumération des inscrits). Si au moins une fiche `Student` porte cette adresse (comparaison sans casse ni espaces), un mail contenant un lien `/my/{jeton}` est envoyé.
3. Jeton : 256 bits aléatoires, stocké **haché** (SHA-256) dans `student_access_token` (adresse, hash, expiration, date d'utilisation), **à usage unique**, valable `correctexam.student-portal.token-ttl` (défaut 30 min).
4. `POST /api/public/my/session` échange le jeton contre un JWT de session de `correctexam.student-portal.session-ttl` (défaut 2 h), sans rafraîchissement, avec le rôle **`ROLE_STUDENT`** et l'adresse en sujet. Les endpoints enseignants (`@RolesAllowed(USER, ADMIN)`) lui restent fermés ; `EndpointSecurityTest` le vérifie.
5. Endpoints étudiants (`@RolesAllowed(STUDENT)`) : liste de ses copies (`/api/student/me/sheets`), une copie filtrée selon `studentAccess`, ses pages, ses données personnelles (`/api/student/me/data`, avec export JSON).
6. Toutes les adresses sont acceptées (pas de restriction de domaine). Protection contre les abus : captcha + limite de demandes par adresse et par IP, comptée en base (fonctionne avec plusieurs instances).
7. Correspondance par e-mail **uniquement** (pas d'INE ni de login CAS). Pour la fiabiliser, l'import et la saisie des étudiants signalent les adresses vides, invalides ou en double.
8. `correctexam.student-portal.enabled` permet de désactiver le portail (version autonome sans serveur de mail).

### 4. Captcha (à valider)

Recommandation : **ALTCHA** (preuve de travail, bibliothèque libre, défi signé et vérifié par le back) : aucun service tiers, pas de cookie ni de suivi (RGPD), fonctionne aussi en version autonome. Alternatives : Cloudflare Turnstile (le front est servi par Cloudflare Pages ; service tiers), hCaptcha, reCAPTCHA (service tiers, suivi).

## Conséquences

**Positives**

- Plus d'envoi en masse ; un étudiant retrouve ses copies à tout moment, et ses données personnelles.
- L'enseignant décide de ce qui est visible, examen par examen.
- Les données des copies ne sont plus lues par des listes anonymes (T10c).

**Négatives**

- Une fiche `Student` sans adresse, ou avec une autre adresse que celle que l'étudiant saisit, ne sera pas trouvée : d'où les avertissements à la saisie.
- Un nouveau rôle et un second type de jeton à maintenir ; des envois de mail à la demande (protégés par captcha et limite de débit).
- Les liens `/copie/{uuid}` restent des capacités permanentes non révocables (choix de compatibilité).

## Alternatives écartées

- **Restreindre aux domaines académiques** : écarté, les adresses des fiches ne sont pas toujours académiques ; captcha et limite de débit suffisent.
- **Identifier aussi par INE ou login CAS** : écarté pour cette version ; l'e-mail est la seule donnée que l'étudiant connaît et que l'on peut vérifier par l'envoi.
- **Faire respecter `studentAccess` par les liens `/copie/{uuid}`** : écarté, un lien envoyé à un seul étudiant ne montrerait plus sa correction tant que l'examen n'est pas publié.
- **Jeton du mail utilisé directement comme session** : écarté ; l'échange contre une session courte permet un jeton à usage unique et une session distincte et révocable par expiration.

## Découpage

1. Back : `studentAccess` + endpoint enseignant + publication à l'envoi des résultats + cet ADR.
2. Back : vue d'une copie par endpoint dédié (`/api/public/copie/{uuid}`, T10c), restriction des lectures ouvertes.
3. Back : lien magique, session `ROLE_STUDENT`, captcha, `/api/student/me/*`.
4. Front : sélecteur de publication côté enseignant, avertissements sur les adresses des étudiants.
5. Front : vue de copie sur les nouveaux endpoints, portail `/my`.
