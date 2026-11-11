# ADR 0001 — Alignement des scans : navigateur par défaut, worker Rust en option

- **Statut** : accepté (documente l'existant au 11/11/2026)
- **Décideurs** : équipe CorrectExam (DiverSE / IRISA / Univ. Rennes)
- **Dépôts concernés** : `corrigeExamFront`, `corrigeExamBack`, `deskewandcachecreator` (dossier local `pdf_to_webp`)

## Contexte

À l'origine, CorrectExam fait tous les traitements d'image dans le navigateur : OpenCV compilé en WebAssembly (repères de calage, redressement, alignement, découpage des zones) et TensorFlow.js / ONNX pour la reconnaissance d'écriture, dans des Web Workers. Le backend Quarkus ne fait que stocker et orchestrer. Ce choix réduit le coût d'hébergement et fait fonctionner la version autonome hors ligne.

Ses limites apparaissent sur les gros examens : plusieurs centaines de pages haute résolution, comme les 185 Mo de scan de l'examen 819. Le navigateur doit alors :

- télécharger puis convertir tout le PDF en images (rasteriser) ;
- garder des centaines d'images en mémoire, dans le tas WebAssembly, où les fuites se sont révélées coûteuses (voir T4) ;
- rester ouvert pendant toute la durée du traitement.

Un worker Rust (`pdf_to_webp`, dépôt `correctexam/deskewandcachecreator`) a donc été ajouté. Il aligne côté serveur et produit directement le cache SQLite que le front sait importer.

La règle documentée jusqu'ici dans `Claude.md` (« le backend ne doit pas effectuer de traitements graphiques lourds ») ne décrivait plus cette réalité.

## Décision

1. **Deux modes d'alignement**, choisis par l'utilisateur dans l'écran d'alignement (`alignscan.component`, option « local » / « serveur ») :
   - **Local (par défaut)** : OpenCV Wasm dans les Web Workers du navigateur (`align.pool.worker.ts`, `opencv.worker.ts`). C'est le seul mode disponible quand le cache local est en IndexedDB (`cacheDb === 'indexdb'`) ou quand l'alignement est désactivé.
   - **Serveur (optionnel)** : le worker Rust `pdf_to_webp`, déployé séparément (image Docker `barais/pdf_to_webp`). Algorithmes : `1` = ronds noirs (`marker`), `2` = AKAZE maison (`nomarker`), `0` = sans alignement.
2. **La JVM Quarkus ne fait aucun traitement d'image** : ni conversion en images, ni alignement, ni OCR. Elle peut manipuler des PDF au niveau du document (fusion de scans avec PDFBox, sans rendu) et elle orchestre : authentification, métadonnées, accès au stockage S3, relais des messages.
3. **Les traitements d'image côté serveur passent par un worker séparé**, qui communique par RabbitMQ et S3. Jamais par un appel synchrone depuis une requête HTTP.

## Contrat d'intégration (mode serveur)

```
Front ──POST /api/scansalign (ScanRequestDto)──▶ Quarkus ──publish──▶ RabbitMQ queue « scan_jobs_queue »
                                                                              │
                                                                              ▼
                                                                   worker Rust pdf_to_webp
                                                     lit  S3 template/{templateId}.pdf, scan/{scanId}.pdf
                                                     écrit S3 cache/{examId}.sqlite3
                                                                              │ publish « scan.status.{examId} »
                                                                              ▼
Front ◀──SSE GET /api/scansalign/{examId}/events── Quarkus ◀── exchange topic « scan_status_exchange »
  │                                                (queue scan_status_java_listener, filtre sur exam_id)
  └──▶ importe cache/{examId}.sqlite3 dans le cache local (CacheUploadService)
```

**`ScanRequestDto`** (JSON en snake_case) :

| Champ | Type | Sens |
|---|---|---|
| `pages_to_manage` | string | pages à traiter, par ex. `"1-3"` ou `"all"` |
| `exam_id`, `template_id`, `scan_id` | long | identifiants base / clés S3 |
| `algo` | long | `0` sans alignement, `1` ronds, `2` AKAZE |
| `heightresolution` | long | hauteur de rendu des pages (défaut 2000) |
| `corner_square_size` | long | taille des zones de coin analysées (défaut 300) |
| `min_radius` | float | rayon minimal des ronds (défaut 10) |

**`ScanStatusDto`** : `exam_id`, `page`, `status` (`running` par page, `finished` en fin de job), `progress` (0–100) et `details`. Le front considère le job terminé sur `finished` ou dès que `progress >= 100`.

**Configuration** : `mp.messaging.outgoing.scan-request-out.*` et `mp.messaging.incoming.scan-status-in.*` dans `application.properties`, côté worker les options `--mq-server-host`, `--server-url` (MinIO) et `--bucket-name`.

## Conséquences

**Positives**

- Les gros examens sont alignés sans dépendre de la machine ni du navigateur de l'enseignant, et le navigateur peut être fermé pendant le traitement.
- Le mode local reste l'option par défaut et la seule hors ligne : l'alignement local ne dépend ni de RabbitMQ ni du worker. Reste à vérifier que la version autonome démarre proprement sans broker RabbitMQ joignable.
- La JVM garde une empreinte mémoire faible, compatible avec la compilation native.

**Négatives / à surveiller**

- Deux implémentations de l'alignement (OpenCV Wasm et Rust) peuvent donner des résultats différents. Il faut des jeux de test communs (T11 : `demo1Template.pdf` / `demo1Copies.pdf`).
- Trois composants de plus à déployer et superviser en mode serveur : RabbitMQ, le worker et son `libpdfium.so`.
- Fiabilité du suivi, problèmes repérés en écrivant cet ADR :
  - le worker ne publie aucun statut d'erreur : en cas d'échec, le message est acquitté (`ack`) et seulement loggé, donc le front attend indéfiniment, faute de timeout ;
  - le flux SSE est « chaud », sans rejeu (`BroadcastProcessor`), et le front l'ouvre *après* le `POST` : un job très court peut se terminer avant que le client ne s'abonne ;
  - le message final est publié avec la clé de routage `scan.status.{scan_id}` au lieu de `{exam_id}`. C'est sans effet aujourd'hui (filtre `#`), mais incohérent.
- Sécurité : `ScanAlignResource` n'a ni `@RolesAllowed` ni contrôle `SecurityService.canAccess` sur l'examen, et la politique HTTP `/api/*` est commentée. Ajouté à T10.

## Alternatives écartées

- **Tout dans le navigateur** : ne tient pas sur les gros volumes (mémoire, durée, navigateur à garder ouvert).
- **Alignement dans la JVM** (OpenCV Java ou PDFBox + AWT) : forte consommation mémoire et CPU dans le serveur d'API, ce qui pénalise toutes les requêtes et la compilation native. Le traitement doit être isolé dans un worker qu'on peut multiplier.
- **Appel HTTP synchrone vers un service d'alignement** : traitements de plusieurs minutes, incompatibles avec les timeouts HTTP. La file de messages découple et absorbe les pics.
