# ADR 0005 — Plusieurs versions d'un sujet : copie reconstruite dans la mise en page canonique

- Statut : proposé (18/11/2026)
- Contexte lié : ADR 0004 (sujets écrits en Typst, codes de page), ADR 0001 (alignement navigateur / worker)
- Tâche : T25e

## Contexte

Un enseignant veut imprimer, pour un même examen, K versions d'un sujet Typst pour limiter la copie entre voisins : ordre des questions mélangé dans une section, ordre des choix de QCM mélangé, variantes d'énoncé (formulations ou valeurs différentes). La correction, la notation, les barèmes QCM (DSL T20), les exports, le portail étudiant et les statistiques doivent rester **par question canonique**, comme pour un examen à une version.

Tout le code suppose aujourd'hui « un examen = un modèle = une position par zone » : `Exam.template` unique, `Question` → une `Zone`, découpe des zones dans la page alignée `pagemin + zone.pageNumber` dans une dizaine d'écrans (correction, vue de copie et de réponse, comparaison, prédictions, export PDF anonyme, association des copies, détection QCM par rang) et dans le portail (`StudentSheetViewService`).

Déjà disponibles (ADR 0004) : plan de version appliqué par le paquet Typst (`order`, `choices`, `numbers`, `pad`), métadonnées de chaque case de QCM (lettre affichée et lettre canonique), codes de page `CE1|tirage|copie|page` lus et remis en ordre dans le navigateur (T25d2) et dans le worker (T25d3). Les ronds d'alignement et l'en-tête d'identité sont au même endroit dans toutes les versions.

## Décision

1. **Une mise en page canonique.** La version A est le sujet tel qu'écrit (plan identité). Elle porte, comme aujourd'hui, le modèle (`Template`), les zones et les questions de l'examen. Les versions B, C… n'ont **pas** de zones ni de questions en base.

2. **Copie reconstruite après l'alignement.** Pour une copie d'une version autre que A, chaque page alignée est remplacée par une page **reconstruite** dans la mise en page canonique :
   - fond : la page du modèle canonique ;
   - pour chaque zone canonique (question, partie) : l'image de la même zone est découpée à sa position dans la version de la copie et collée à sa position canonique ;
   - pour un QCM : **chaque case** est recopiée de sa position dans la version vers la position de la case de même lettre canonique ; la détection par rang lit ainsi directement les lettres canoniques, sans traduction ;
   - zones d'identité : recopiées (même position) ;
   - une mention « Version B — page reconstruite » est apposée dans la marge.

   Tout ce qui est en aval de l'alignement est inchangé.

3. **La page originale reste disponible.** Les pages non alignées du cache (pages réellement numérisées) ne sont pas modifiées ; l'enseignant et l'étudiant (portail, `noalign`) peuvent les consulter.

4. **Version d'une copie.** Un tirage a `versions` (K) ; la copie `c` reçoit la version `(c − 1) mod K` (tourniquet : deux copies voisines dans la pile ont des versions différentes). Le code de page reste `CE1|tirage|copie|page` ; la version s'en déduit.

5. **Mise en page de chaque version, stockée.** À la création de l'examen, le front compile chaque version, vérifie qu'elles sont compatibles et enregistre pour chacune : le plan (JSON), la mise en page (positions des zones, des cases de QCM et des zones d'identité, en 1/100000 de la page, JSON) et le PDF (S3 `version/{examId}/{index}.pdf`). Contrôles bloquants : mêmes zones (clé de question, partie) et mêmes lettres de QCM dans toutes les versions, **même taille** pour une même zone, même nombre de pages (pages blanches ajoutées), mêmes zones d'identité. Les versions ne changent plus après le premier tirage à plusieurs versions.

6. **Reconstruction aux deux endroits de l'alignement**, avec le même contrat : dans le navigateur (Web Worker, après l'alignement local) et dans le worker Rust (après son alignement ; le back ajoute les mises en page à `ScanRequestDto`, comme les tirages en T25d3). Évolution simultanée du DTO Java, du DTO TypeScript et de la struct Rust.

7. **Variabilité.** Le front calcule les plans (graine enregistrée, code TypeScript testé) ; le paquet les applique : ordre des éléments d'une `section(shuffle: true)` (`group` reste d'un bloc), ordre des choix d'un `qcm` (les choix `fixed` restent en place), variante d'un `variant(…)` (nouvelle fonction du paquet, un corrigé par variante). K est limité à 6 (versions A à F).

## Conséquences

Positives :

- correction, détection QCM et DSL, ML, comparaison de réponses, exports, portail et statistiques par question : **aucune modification** ;
- le travail est concentré dans un module de reconstruction testable isolément, appelé par les deux alignements ;
- les versions sont figées et reproductibles (plans, mises en page et PDF stockés).

Négatives :

- la page vue en correction n'est pas celle qu'a eue l'étudiant : énoncé canonique autour de ses réponses, écriture hors des zones absente → mention sur la page, accès à l'original, version affichée en correction (avec la variante et son corrigé) ;
- une copie dont aucun code n'est lisible ne peut pas être reconstruite : elle reste telle quelle et est signalée ;
- deux implémentations de la reconstruction (TypeScript et Rust) à garder alignées : un même jeu de cas de test pour les deux ;
- contrainte de mise en page : une zone doit avoir la même taille dans toutes les versions (vrai par construction dans le paquet, contrôlé).

## Alternatives écartées

- **Géométrie invariante** (seul l'ordre des choix de QCM et des variantes de hauteur réservée) : simple, mais pas d'ordre des questions différent, la mesure la plus efficace contre la copie entre voisins.
- **Zones et modèle par version** : la vraie page partout, mais une dizaine d'écrans front, le portail et les exports à adapter, et chaque futur écran devrait gérer les versions ; risque de régression élevé.
- **Lettre affichée traduite au moment de la détection QCM** : inutile avec la recopie case par case.
