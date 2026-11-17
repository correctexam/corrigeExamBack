# Architecture Decision Records

Décisions d'architecture de CorrectExam (front, back et workers). Un fichier par décision, numéroté, jamais réécrit : une décision remplacée passe au statut « remplacée par ADR NNNN ».

| N° | Décision | Statut |
|---|---|---|
| [0001](0001-alignement-des-scans-client-ou-serveur.md) | Alignement des scans : navigateur par défaut, worker Rust en option ; pas de traitement d'image dans la JVM | accepté |
| [0003](0003-portail-etudiant-par-lien-magique.md) | Portail étudiant : lien magique envoyé à l'adresse de l'étudiant, rôle `ROLE_STUDENT`, publication des copies contrôlée par l'enseignant (`Exam.studentAccess`) | accepté |
| [0004](0004-sujets-ecrits-en-typst.md) | Sujets écrits en Typst dans la plateforme : paquet `correctexam`, zones lues dans les métadonnées, compilation dans le navigateur, une compilation par version et code de page apposé par pdf-lib, diagrammes PlantUML par Kroki auto-hébergé ; le dépôt d'un sujet PDF reste inchangé | accepté |

Modèle : contexte, décision, conséquences (positives et négatives), alternatives écartées.
