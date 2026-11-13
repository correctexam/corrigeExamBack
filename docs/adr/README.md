# Architecture Decision Records

Décisions d'architecture de CorrectExam (front, back et workers). Un fichier par décision, numéroté, jamais réécrit : une décision remplacée passe au statut « remplacée par ADR NNNN ».

| N° | Décision | Statut |
|---|---|---|
| [0001](0001-alignement-des-scans-client-ou-serveur.md) | Alignement des scans : navigateur par défaut, worker Rust en option ; pas de traitement d'image dans la JVM | accepté |
| [0002](0002-bareme-des-qcm-par-dsl.md) | Barème des QCM par DSL ; le pourcentage est porté par chaque option, la note se calcule sans le DSL côté front et back | accepté |

Modèle : contexte, décision, conséquences (positives et négatives), alternatives écartées.
