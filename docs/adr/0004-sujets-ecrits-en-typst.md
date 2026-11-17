# ADR 0004 — Sujets écrits en Typst dans la plateforme, zones générées et copies variables

- **Statut** : accepté (16/11/2026) — prototype V0 livré (`corrigeExamFront`, branche `feat/T25a-typst-exam-package`)
- **Décideurs** : équipe CorrectExam (DiverSE / IRISA / Univ. Rennes)
- **Dépôts concernés** : `corrigeExamFront`, `corrigeExamBack` ; `deskewandcachecreator` à partir de la V2
- **Tâche** : T25 (exigences : document de projet `T22-editeur-enonces`, renommé T25)

## Contexte

L'enseignant écrit aujourd'hui son sujet hors de CorrectExam (Word, LaTeX…), le dépose en PDF comme modèle, puis trace à la main les zones : nom, prénom, numéro étudiant, questions, QCM. La classe LaTeX fournie (`content/latex-template.zip`) évite le tracé : elle écrit la position des zones dans le dictionnaire d'information du PDF (`correctexam-answer-{q}-{s}`, `correctexam-lastname`…), que l'éditeur de modèle lit à l'ouverture (`fabric-canvas.component.ts`, `getCustomPdfProperties`). Elle impose cependant LaTeX à l'enseignant et ne permet pas de faire varier les copies.

Besoins :

- écrire le sujet dans la plateforme, avec des formules, du code mis en forme et des diagrammes UML décrits en texte ;
- obtenir le PDF à imprimer et l'examen prêt à corriger, sans tracer de zone ;
- prévoir dès maintenant des copies différentes d'un étudiant à l'autre (ordre des questions, formulations, ordre des réponses de QCM), avec une correspondance exacte avec le sujet de référence ;
- **conserver le dépôt d'un sujet PDF, inchangé**.

Constats dans le code qui contraignent la solution :

- une zone (`Zone`) est en 1/100000 de la largeur et de la hauteur de la page (`xInit`, `yInit`, `width`, `height`, `pageNumber`) ;
- un examen a un seul modèle (`Exam.template`) et quatre zones d'identité (`idzone`, `namezone`, `firstnamezone`, `notezone`) ; une question (`Question.numero`) peut avoir plusieurs zones (plusieurs `Question` de même numéro) ;
- la détection QCM (`qcm.ts`) cherche dans la zone du modèle les contours à 4 côtés (seuil 245, `approxPolyDP` à 0,02, au moins 10 px, `CARRE` / `RECTANGLE`), les trie de haut en bas puis de gauche à droite et nomme la *k*-ième case cochée `String.fromCharCode(97 + k)` ;
- l'alignement par ronds (`align.pool.worker.ts`) garde, parmi les cercles de Hough de rayon 0,4 % à 4 % de la largeur, le plus proche de chaque coin.

## Décision

### 1. Deux modes de création, le mode PDF reste intact

À la création d'un examen : « Déposer un sujet PDF » (actuel, défaut) ou « Écrire le sujet ». Aucune tâche T25 ne modifie le flux PDF ; toute évolution partagée (lecture du code de page, modèle par version, contrat d'alignement) est optionnelle et sans effet sur un examen en mode PDF. Un sujet écrit produit un examen ordinaire : modèle, zones et questions. La correction, l'export et le portail étudiant fonctionnent sans connaître l'origine du sujet.

### 2. Langage : Typst, avec un paquet `correctexam`

Le sujet est écrit en **Typst**. Plus de Markdown ni de grammaire de macros maison : la plateforme fournit le paquet `correctexam` (`corrigeExamFront/src/main/webapp/content/typst/correctexam/`), versionné et publié en open source pour compiler un sujet hors plateforme :

- `exam` (en-tête, identité, repères, code de page), `identity`, `question` + `answer`, `qcm` (`ok` / `ko` / `fixed`), `numeric` (cases manuscrites ou grille à cocher), `short`, `section` (mélange), `group`, `solution` ;
- chaque zone émet une métadonnée `<ce-zone>` (`format: 1`) : type, question, partie, page dans la copie, rectangle en points, lettres affichée et canonique des cases de QCM. La plateforme la lit après compilation (`query(<ce-zone>)`) ; `app/scanexam/typst/typst-zones.ts` la convertit en zones CorrectExam et refuse un format inconnu ;
- la géométrie est réglée sur la chaîne existante : ronds pleins de 2,5 mm de rayon à 9 mm des bords ; cases de QCM carrées de 4,2 mm, trait de 0,8 pt, lettres hors de la zone ; zone de QCM = boîte englobante des cases + 1,2 mm ;
- les mêmes positions sont aussi exportables dans les propriétés PDF de la classe LaTeX (`toLatexCompatibleProperties`) : un PDF généré peut passer par le dépôt de modèle actuel.

### 3. Compilation dans le navigateur, sans réseau, versions épinglées

- Compilation par le compilateur Typst en Wasm dans un Web Worker (même principe que l'ADR 0001 : calcul lourd hors de l'API). L'aperçu est le PDF réel : pas d'aperçu HTML qui pourrait diverger de l'impression.
- Les paquets Typst et leurs plugins Wasm sont **embarqués** par la plateforme (liste blanche) et **jamais téléchargés** pendant la compilation ; une révision du sujet enregistre les versions du compilateur et des paquets. Liste initiale : `correctexam`, `tiaoma` (code de page), `mitex`, `codly`, `fletcher`, `cetz`, `chronos`, `diagraph`, `pintorita` et leurs dépendances ; Mermaid (`mmdr` / `merman`) après évaluation.
- Le PDF modèle et les zones de chaque version sont stockés à la génération : une mise à jour du compilateur ne déplace jamais les zones d'un tirage imprimé.

### 4. Copies : une compilation par version, code de page apposé ensuite

- Chaque page porte un **Data Matrix** de 11 mm en bas au centre : `CE1|{tirage}|{copie}|{page}`. Identifiant de copie opaque ; aucune donnée personnelle ; jamais de nom imprimé (pas de copie nominative). L'aperçu n'a pas de code.
- Typst compile **une fois par version** (`ce-plan.stamp = true` : emplacement du code laissé vide) ; les copies sont assemblées par **pdf-lib** (duplication des pages, opération sur le document) et le code de chaque page est dessiné en vectoriel à partir de la matrice calculée par **bwip-js**.
- Chaque copie a le même nombre pair de pages : la plateforme lit le nombre de pages d'une copie (`copy-end`) et passe `ce-plan.pad`. Le calcul de ce remplissage dans Typst, à partir du compteur de pages, ne converge pas.
- La lecture du code au scan (V1 pour l'ordre des pages, V2 pour le choix de la version) se fait dans le navigateur ou dans le worker Rust, jamais dans la JVM ; un code illisible se corrige à la main.

### 5. Variabilité pilotée de l'extérieur

La plateforme calcule l'ordre de chaque version (TypeScript testé, graine du tirage) et le passe en JSON (`--input ce-plan=…`) : ordre des éléments d'une section, ordre des choix d'un QCM, numéro canonique de chaque question. Le paquet applique et ne tire rien au hasard. Mode livré d'abord : **K versions** (A, B, C…).

Correspondance : tout ce qui est noté et exporté est rattaché à la **question canonique** (`Question.numero`, lettres canoniques) ; tout ce qui est image ou zone est rattaché à la **version**. Pour un QCM, la réponse lue (rang des cases cochées dans l'ordre de la détection) est traduite en lettres canoniques (`toCanonicalAnswer`) **avant** le barème DSL de l'ADR 0002, qui reste inchangé, comme les options et les corrections manuelles (T19).

### 6. Contenu riche

| Besoin | Solution |
|---|---|
| Formules | Maths Typst natives ; LaTeX accepté via `mitex` |
| Code | Blocs Typst (coloration intégrée), `codly` (numéros de ligne, mise en évidence), thème lisible en noir et blanc ; grammaires `.sublime-syntax` pour les langages manquants |
| Mise en forme du code | **Action de l'éditeur, jamais de la compilation** (déterminisme ; un code mal indenté peut être voulu). Formateurs exécutés dans le navigateur, chargés à la demande : Prettier (JavaScript, TypeScript, CSS, HTML, JSON, YAML) et `prettier-plugin-java` (Java) ; Ruff en Wasm (Python) ; clang-format en Wasm (C, C++) ; `sql-formatter` (SQL) |
| Diagrammes en texte | 1) paquets Typst natifs (`fletcher`, `cetz`, `chronos` qui lit aussi la syntaxe PlantUML des diagrammes de séquence) ; 2) plugins Wasm (`diagraph` pour Graphviz, `pintorita`) ; 3) **PlantUML et les autres syntaxes par Kroki** |

**Kroki** est déployé à côté de l'API, **auto-hébergé** (conteneur `yuzutech/kroki` et compagnons utiles, dont PlantUML), sans accès réseau sortant, en mode sûr (`KROKI_SAFE_MODE=secure` : pas d'inclusion de fichiers ni d'URL), avec des limites de taille et de durée. Le back expose `POST /api/exams/{examId}/subject/diagrams` (`@RolesAllowed` + `canAccess`) : il transmet le source à Kroki en HTTP et stocke le SVG obtenu sous l'empreinte du source. **La JVM ne rend rien** : elle relaie une requête et écrit dans S3, ce qui respecte la règle de l'ADR 0001. Le rendu a lieu à l'édition ; la compilation lit le SVG en cache et reste hors ligne. Si Kroki ne répond pas, le SVG déjà en cache sert ; sans cache, la génération est bloquée avec un message clair.

### 7. Stockage

Le sujet appartient à l'examen ; il se réutilise par copie depuis un autre examen.

| Clé S3 | Contenu |
|---|---|
| `subject/{examId}/files/{chemin}` | sources `.typ`, images, fichiers de code |
| `subject/{examId}/diagrams/{sha256}.svg` | diagrammes rendus par Kroki |
| `print/{examId}/{editionId}.pdf` | PDF d'impression, régénérable, purgeable |
| `template/{templateId}.pdf` | modèle de chaque version (un `Template` par version à partir de la V2) |

Les nouvelles clés sont ajoutées au `StorageOrphanDetector` ; la suppression d'un examen les supprime après le commit (`StorageDeletionService`).

## Conséquences

Positives :

- examen prêt à corriger sans tracé, positions exactes au lieu d'un tracé à la souris ;
- un seul langage pour le texte, les formules, le code et la mise en page ; aperçu = impression ;
- aucune charge de rendu sur l'API ; compilation et assemblage dans le navigateur ;
- le code de page sert aussi aux sujets sans variabilité : pages remises dans l'ordre, pages manquantes signalées ;
- la variabilité ne change ni le barème DSL ni les options de QCM.

Négatives et risques :

- Typst est nouveau pour beaucoup d'enseignants → modèle de départ, extraits, formulaire de propriétés des questions, `mitex` ; le mode PDF reste là ;
- le poids du Wasm (compilateur, polices, plugins : `diagraph` 1,1 Mo, `tiaoma` 0,8 Mo, `mitex` 0,3 Mo) → chargement à la demande, mise en cache par le service worker ;
- un service de plus à exploiter (Kroki) ;
- la détection QCM numérote les cases par rang et compare les `y` exactement : des cases d'une même ligne peuvent changer d'ordre à la numérisation. Le paquet place les cases de QCM en colonne ; à terme, associer chaque case détectée à la case générée la plus proche (positions connues) plutôt qu'à son rang ;
- l'import des propriétés PDF de la classe LaTeX multiplie `y` et `h` par `devicePixelRatio` (`scaleRect`) : l'import par ce chemin dépend de l'écran. Les sujets Typst créent leurs zones directement (V1), sans passer par ce chemin.

## Alternatives écartées

- **Markdown (style CodiMD) converti en PDF** : grammaire de macros à maintenir, aperçu HTML différent du PDF, deux moteurs.
- **HTML + Paged.js, impression par Chromium** : déterminisme moyen, Chromium à exploiter dans un worker, pas de mode local fiable.
- **LaTeX côté serveur (façon AMC)** : TeX Live dans un worker, plusieurs Go ; pas de compilation dans le navigateur. La classe LaTeX existante reste utilisable en mode PDF.
- **Toutes les copies dans une seule compilation Typst** : 20 copies en 3,4 s, 60 en 10 s, échec à 300 dans le prototype ; l'assemblage par pdf-lib fait 300 copies × 4 pages en 5,3 s.
- **PlantUML dans la JVM de l'API** : rendu dans l'API, contraire à l'ADR 0001.
- **Appel direct de Kroki depuis le navigateur** : service exposé publiquement, sans contrôle d'accès ni cache partagé.
- **Mise en forme automatique du code à la compilation** : non déterministe d'une version de formateur à l'autre et contraire à l'intention de l'enseignant.
- **Copies nominatives** : refusées (aucune donnée d'étudiant imprimée).

## Découpage

1. **T25a** (front, fait) : paquet `correctexam` 0.1.0, lecture des zones, sujet d'exemple, mesures.
2. **T25b** (front) : compilation Typst dans un Web Worker, éditeur « Écrire le sujet » (aperçu, diagnostics, extraits), création de l'examen avec ses zones et questions à partir des métadonnées ; le PDF généré devient le modèle.
3. **T25c** (back) : sujet de l'examen (fichiers S3, révisions, verrou, commentaires), proxy Kroki, clés S3 et détecteur d'orphelins ; front : enregistrement du sujet, rendu des diagrammes.
4. **T25d** (front, puis worker) : tirage, assemblage des copies et code de page, lecture du code au scan, types de question `numeric` et `short`.
5. **T25e** (V2, trois dépôts) : K versions, modèle et zones par version, alignement par version (`ScanRequestDto`, évolution simultanée Java / TS / Rust), traduction des lettres QCM, statistiques par version.

## Précisions (T25c, 17/11/2026)

Choix faits à l'implémentation du stockage du sujet ; ils précisent la section 7 sans changer la décision.

- **Fichiers du sujet dans S3, pas en base.** Table `exam_subject` (un sujet par examen : fichier principal, compteur de révision, verrou) et `exam_subject_file` (chemin, taille, SHA-256, type) ; contenu sous `subject/{examId}/files/{chemin}`. Chemins relatifs validés (`SubjectPaths` : 5 niveaux, segments `[A-Za-z0-9_][A-Za-z0-9._-]*`, extensions en liste blanche, pas de `..`). Limites configurables : 5 Mo par fichier, 50 Mo et 200 fichiers par sujet. Sans stockage (ni S3 ni `saveasfile`), l'API répond 503 et l'éditeur reste en brouillon local.
- **Révision = compteur, pas d'historique (V1).** Chaque écriture incrémente `revision` ; le client envoie `If-Match: <révision>` et reçoit 409 si un autre onglet a écrit entre-temps. L'historique des révisions, la comparaison et les commentaires ancrés sont reportés (tâche à part) : ils demandent des objets versionnés et un modèle `SubjectRevision` / `SubjectComment`.
- **Verrou d'édition** (`SubjectLocks`) : détenu par un enseignant, expire 15 min après le dernier renouvellement (l'éditeur le renouvelle à chaque enregistrement) ; un autre enseignant de l'examen peut le reprendre explicitement (`force`) après avertissement. Écrire sans détenir le verrou → 409.
- **Cache des diagrammes partagé et adressé par contenu** : `diagram-cache/{kind}/{sha256}.svg`, empreinte de `kind + "\n" + source`, au lieu de `subject/{examId}/diagrams/…`. Un même diagramme sert à tous les examens (réutilisation d'un sujet par copie, sujets d'une année sur l'autre) et n'est rendu qu'une fois ; le SVG ne contient que ce que dit son source. Points d'entrée : `POST /api/subject-diagrams` (rendu ou cache) et `GET /api/subject-diagrams/{kind}/{sha256}.svg` (`Cache-Control: immutable`), réservés aux enseignants. **Écart assumé à la règle « identifiant du propriétaire dans la clé »** : ces objets n'ont pas de propriétaire ; le détecteur d'orphelins ne les analyse pas (clés inconnues, jamais supprimées) et une purge par ancienneté pourra s'ajouter. Les clés `subject/{examId}/…` sont, elles, rattachées à l'examen (`EXAM_MISSING`) et supprimées après le commit avec l'examen.
- **Kroki** : URL `correctexam.kroki.url` (vide = désactivé → 503), délai 15 s, source ≤ 64 Kio, SVG ≤ 2 Mio ; types acceptés `plantuml`, `c4plantuml`, `mermaid`, `d2`, `nomnoml`. Erreur de syntaxe → 422 avec le message de Kroki. `src/main/docker/kroki.yml` lance Kroki en mode sûr, lié à `127.0.0.1`.
- **Fichiers servis** avec `X-Content-Type-Options: nosniff` et `Content-Security-Policy: sandbox` (un SVG déposé ne peut pas exécuter de script dans l'origine de l'application).
