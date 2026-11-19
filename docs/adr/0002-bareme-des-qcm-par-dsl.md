# ADR 0002 — Barème des QCM par DSL, pourcentage porté par chaque option

- **Statut** : accepté (13/11/2026)
- **Décideurs** : équipe CorrectExam (DiverSE / IRISA / Univ. Rennes)
- **Dépôts concernés** : `corrigeExamBack`, `corrigeExamFront`
- **Tâche** : T20

## Contexte

Un QCM est décrit par son expression valide (`a&b|a&c` : réponses exactes considérées comme justes), ses points (`point`) et un pas (`step`) qui active une pénalité. La détection (OpenCV Wasm, `qcm.ts`) renvoie pour chaque copie la réponse cochée, normalisée (`a&c`). Chaque réponse observée devient une **option** de la question (`GradedComment`), dont la description commence par `correct` ou `incorrect`.

La note est calculée **deux fois**, à partir de ce préfixe :

- dans le front, `qcmNote()` (`scanexam/grading/question-grading.ts`), qui écrit `StudentResponse.note` ;
- dans le back, `FinalNoteService` (note finale, qui réécrit `quarternote`) et `QuestionService.computeNote` (détail des réponses), plus la conversion de `ExamResultResource`.

Ce modèle ne permet que « tout juste / tout faux » : pas de réponse partiellement juste, pas de barème par case, pas de pénalité différente selon la mauvaise réponse.

## Décision

### 1. Une expression valide peut être un DSL de barème

Une expression qui contient `=` est un DSL ; sinon elle garde son sens actuel (`point` / `step`). Même test des deux côtés (`QcmDslGrading.isDslExpression`, `isQcmDslExpression` dans le front).

```
dsl     = rule { ";" rule } [ ";" ]                 (espaces et retours à la ligne ignorés)
rule    = answers "=" percent                       a&b|a&c=100      réponses exactes
        | "[" letter "]" "=" percent                [c]=-25          barème par case
        | "void" "=" percent                        void=-10         aucune case cochée
        | "*" "=" percent                           *=-33            tout le reste
        | ("min" | "max") "=" percent               min=-100
answers = answer { "|" answer }
answer  = letter { "&" letter } [ "+" ]             a&b+ : au moins a et b
percent = [ "+" | "-" ] digits [ "." digits ]       en % des points de la question
```

Valeurs implicites : `void=0`, `*=0`, `min=0`, `max=100` ; `min` et `max` restent dans [-100, 100].

Évaluation d'une réponse : aucune case → `void` ; sinon première règle de réponse qui correspond (ordre d'écriture) ; sinon somme des `[x]` des cases cochées, si au moins une est définie ; sinon `*` ; puis bornes `min` / `max`.

Erreurs signalées à la saisie : réponse définie deux fois, `void` / `*` / `min` / `max` en double, `min > max`, valeur hors de [-100, 100] pour les bornes.

### 2. Le DSL n'est analysé que dans le front ; chaque option porte son pourcentage

- Quand le front crée une option (autocorrection, réponse ajoutée à la main), il évalue le DSL pour cette réponse et stocke le **pourcentage** dans `GradedComment.grade` (en quarts de pourcent, arrondi).
- La note d'une réponse se calcule **sans le DSL**, à l'identique dans le front et le back :
  `pourcentage = somme des grade de ses options, borné à [-100, 100]` puis `note = pourcentage × point / 100`.
- Elle est stockée comme une note hybride : `StudentResponse.note` = points × 100 (`quarternote = round(points × 400)`), donc sans troncature au quart de point.
- Le back ne contient pas de parseur : `QcmDslGrading` (pur) fait ce calcul pour `FinalNoteService`, `QuestionService` et `ExamResultResource`.
- Modifier le DSL oblige le front à recalculer le pourcentage des options de la question puis les notes ; le back recalcule de toute façon `quarternote` au calcul des notes finales.

### 3. Une option par réponse

En mode DSL une réponse n'a qu'une option (la réponse cochée), comme le garantit déjà l'ajout d'une réponse à la main (T19b). La borne [-100, 100] sur la somme évite tout débordement si plusieurs options étaient cochées.

## Exemples de référence

Communs aux tests du front (DSL → pourcentage) et du back (pourcentage → note). Question sur 2 points.

| DSL | Réponse | % | Note |
|---|---|---|---|
| `a&b\|a&c=100; a\|b=50; c=-25; *=-33` | `a&b` | 100 | 2 |
| idem | `b` | 50 | 1 |
| idem | `c` | -25 → borné à `min=0` | 0 |
| idem | `a&b&c` | -33 → 0 | 0 |
| `a&b=100; c=-25; *=-33; min=-100` | `c` | -25 | -0,5 |
| idem | `a&b&c` | -33 | -0,66 |
| idem | aucune case | 0 (`void` implicite) | 0 |
| `a&b=100; void=-10; min=-100` | aucune case | -10 | -0,2 |
| `a&b=100; a&b+=60` | `a&b&d` | 60 | 1,2 |
| idem | `a&b` | 100 (la règle exacte est écrite avant) | 2 |
| `[a]=50; [b]=50; [c]=-25; [d]=-25` | `a&c` | 25 | 0,5 |
| idem | `c&d` | -50 → 0 | 0 |
| `a&b=100; [a]=40; [b]=40; [c]=-30` | `a&b` | 100 (règle exacte d'abord) | 2 |
| idem | `a&c` | 10 | 0,2 |

## Conséquences

**Positives**

- Barème libre (réponses partielles, pénalités ciblées, barème par case) sans toucher à la détection ni au worker.
- Une seule implémentation de la grammaire (front). Le calcul partagé avec le back est trivial et testé des deux côtés.
- Le pourcentage d'une option peut plus tard être modifié à la main dans l'interface, sans changer ce modèle.
- Les expressions existantes gardent exactement leur sens.

**Négatives**

- Le pourcentage stocké sur une option peut devenir obsolète si le DSL change sans que le front recalcule : le recalcul doit être déclenché à l'enregistrement de la question.
- Le back ne peut pas valider un DSL : un client qui enregistre un DSL invalide n'est pas bloqué côté serveur (le front valide avant d'enregistrer).
- `valid_expression` passe de VARCHAR(255) à VARCHAR(1024) (migration de prod).

## Alternatives écartées

- **Parser le DSL aussi dans le back** : deux implémentations de la grammaire à garder identiques ; plus de risque d'écart que de bénéfice, puisque le back n'a besoin que du pourcentage.
- **Formules libres** (`max(0, 50*bonnes - 25*mauvaises)`) : plus puissantes, mais difficiles à valider et à expliquer ; le barème par case couvre l'essentiel.
- **Colonne dédiée au pourcentage** : plus précise que les quarts de pourcent de `grade`, mais inutile à cette échelle.
- **Clamp par `min` / `max` du DSL au niveau de la note** : impossible côté back sans parseur ; les bornes sont appliquées au pourcentage de chaque option.
