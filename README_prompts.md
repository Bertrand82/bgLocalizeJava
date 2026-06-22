

## PROMPT EXTRACTION FEATURE FROM IMAGE

Je veux que tu développes un outil Java basé sur OpenCV pour extraire des features d’images, avec une API propre et réutilisable.

Objectif :
- extraire des points d’intérêt et des descripteurs compatibles avec OpenCV 
- les features produites doivent pouvoir être utilisées directement avec les classes OpenCV Java standard, notamment KeyPoint / MatOfKeyPoint, Mat descriptors, BFMatcher, etc.
- je veux éviter tout format propriétaire non compatible avec OpenCV

Contraintes techniques :
- langage : Java
- bibliothèque : OpenCV Java
- code propre, structuré, factorisé
- compatible avec une utilisation en bibliothèque et en ligne de commande
- prévoir une séparation claire entre :
  1. chargement image
  2. extraction des features


Fonctionnalités minimales :
1. charger une image depuis le filesystem
2. extraire les features avec au minimum :
   - ORB
   - SIFT
   - AKAZE ou BRISK
3. retourner un objet résultat contenant au minimum :
   - chemin image ou identifiant image
   - dimensions image
   - algorithme utilisé
   - keypoints OpenCV
   - descriptors OpenCV
   - nombre de keypoints
   - type OpenCV du Mat descriptors
4. fournir une API Java simple, par exemple :
   - FeatureExtractionResult extract(String imagePath, FeatureAlgorithm algorithm)
5. fournir aussi un petit exécutable CLI, par exemple :
   - --image <path>
   - --algo ORB|SIFT|AKAZE|BRISK
   - --output <dir ou fichier>

7. ajouter un test unitaire  (Une image de test est dans le repertoire data)

Exigences de conception :
- utiliser les structures natives OpenCV Java autant que possible
- documenter clairement quels algorithmes produisent des descripteurs binaires ou flottants
- gérer proprement les erreurs (image absente, image illisible, OpenCV non chargé, etc.)
- rendre l’API extensible pour ajouter d’autres extracteurs plus tard

Livrables attendus :
1. structure de projet proposée
2. classes Java principales
3. code source complet
4. exemple main() ou CLI et dans test unitaire
5. doc d’utilisation (Dans un fichier README_extraction.md)
6. explication des choix techniques
7. Faire une pull request

Important :
- je veux un code directement exploitable
- je veux que l’outil produise des features réellement compatibles avec les objets et matchers OpenCV
- si une décision d’implémentation peut casser la compatibilité OpenCV, il faut l’éviter
- commence par proposer l’architecture, puis implémente les classes

Ne pas faire : 
- Le matching

## Prompt matching

data A: J'ai  une serie d'images prise par drone, avec les data associées issue de colmap (db, fichiers bin et txt). 
data B :  j'ai une image dant j'extrait les features avec opencv.
Je voudrai faire un match des data A et data B

  - Contexte
Je dispose :

d’une série d’images drone ;
d’une reconstruction COLMAP ;
du fichier database.db ;
des fichiers de reconstruction COLMAP en format .bin et/ou .txt :
 images;
points3D;
cameras.

d’une image requête sur laquelle j’extrais des features avec OpenCV Java.

  - Objectif
Construire une application Java qui :

charge les descripteurs et keypoints des images COLMAP depuis database.db ;
charge les données de reconstruction depuis images.bin/.txt, points3D.bin/.txt, cameras.bin/.txt ;
extrait les features de l’image requête avec OpenCV Java ;
matche les descripteurs de l’image requête avec ceux des images COLMAP (en utilisant openCV) ;
récupère les correspondances entre keypoints 2D et POINT3D_ID de COLMAP ;
reconstruit les correspondances 2D requête ↔ 3D scène ;
estime la pose de l’image requête ;
retourne :
la pose estimée ;
le nombre de matches bruts ;
le nombre de matches filtrés ;
le nombre d’inliers ;

Contraintes techniques
Utiliser Java.
Utiliser OpenCV Java pour l’extraction et le matching de features.
Utiliser SQLite JDBC pour lire database.db.
Lire les fichiers COLMAP .txt en priorité.

Le code doit être :
  - modulaire ;
  - lisible ;
  - documenté ;
  - testable.
  
Fournir une exécution en ligne de commande.
Hypothèses importantes
  - Vérifier la compatibilité entre les descripteurs OpenCV et ceux utilisés par COLMAP.
  - utiliser SIFT côté OpenCV Java.
Respecter l’alignement entre :
les keypoints/descriptors de database.db
les points 2D de images.txt ou images.bin
Ignorer les observations dont POINT3D_ID = -1.
Détails d’implémentation attendus
Je veux une architecture claire avec par exemple les composants suivants :

ColmapDatabaseReader

lecture SQLite de database.db
chargement des descripteurs par image_id
chargement des keypoints par image_id
ColmapTextModelReader

lecture de images.txt
lecture de points3D.txt
lecture de cameras.txt
FeatureExtractor

extraction des features avec OpenCV Java
support prioritaire de SIFT
FeatureMatcher

matching des descripteurs
ratio test de Lowe
éventuellement cross-check ou filtrage géométrique
CorrespondenceBuilder

conversion des matches en correspondances 2D–3D
récupération des POINT3D_ID
filtrage des points invalides
PoseEstimator

estimation de pose avec OpenCV (solvePnPRansac si disponible en Java)
calcul des inliers
retour rotation/translation
Main

parsing des arguments CLI
orchestration complète du pipeline
Fonctionnalités minimales attendues
Le programme doit :

prendre en entrée :
chemin vers database.db
chemin vers le dossier du modèle COLMAP
chemin vers l’image requête
identifiant ou nom d’image COLMAP candidate, ou bien tester plusieurs images
charger les données nécessaires ;
extraire les descripteurs de l’image requête ;
faire le matching avec une ou plusieurs images COLMAP ;
construire les correspondances 2D–3D ;
tenter une estimation de pose ;
afficher un résumé console.
Sortie console attendue
Exemple :

image COLMAP testée
nombre de keypoints requête
nombre de descripteurs COLMAP
nombre de matches bruts
nombre de matches après ratio test
nombre de correspondances 2D–3D valides
succès/échec PnP
nombre d’inliers
vecteur de rotation
vecteur de translation
Gestion d’erreurs
Prévoir des erreurs explicites si :

le fichier image n’existe pas ;
database.db est introuvable ;
les tables SQLite attendues ne sont pas présentes ;
aucun descripteur n’est disponible pour une image ;
les fichiers COLMAP texte sont mal formés ;
aucun POINT3D_ID valide n’est trouvé ;
il n’y a pas assez de correspondances pour lancer PnP ;
l’estimation de pose échoue.
Choix techniques recommandés
Java 17+ si possible
Maven ou Gradle
OpenCV Java
SQLite JDBC
logs clairs
classes de modèle pour :
caméra
image COLMAP
point 3D
observation 2D
résultat de pose
Résultat attendu
Je veux un livrable exploitable avec :

le code source Java complet ;
un projet Maven ou Gradle ;
une commande d’exécution ;
un README expliquant :
les prérequis ;
l’installation d’OpenCV Java ;
le format des entrées ;
le pipeline ;
les limites connues.
Bonus souhaité
Si possible :

support de plusieurs images COLMAP candidates avec ranking ;
sauvegarde d’une visualisation des matches ;
support futur des fichiers .bin COLMAP ;
tests unitaires sur le parsing des fichiers texte ;
séparation nette entre parsing, matching et estimation de pose.


## Version courte
Développe une application Java utilisant OpenCV Java et SQLite JDBC pour faire correspondre les features d’une image requête avec les données d’une reconstruction COLMAP. Le programme doit lire database.db ainsi que images.txt, points3D.txt et cameras.txt, extraire les features de l’image requête, matcher les descripteurs avec ceux des images COLMAP, reconstruire les correspondances 2D–3D via les POINT3D_ID, puis estimer la pose avec solvePnPRansac. Le code doit être modulaire, documenté, exécutable en CLI, et inclure une gestion d’erreurs claire.


## Extraction Data from colmap sqlite db
Je veux pouvoir lire une base sqlite colmap et avoir l'id d'une image à partir de son id : installe les dependances maven , crée une class avec un constructeur avec le  File de la database, et fait une requete permettant d'obtenir image_id (INTEGER) à partir de name (TEXT) dans la table "images" .
Fait un test unitaire avec la base de donnée ./data/BG/database.db
name="IMG_20260618_124549.jpg" image_id=1


## Lecture des fichier colmap
 Fait une api pour Lire les fichiers colmap images.txt et points3D.txt
 fait des tests unitaires avec les fichiers ./data/BG/sparse/0/images.txt et ./data/BG/sparse/0/points3D.txt
 
 
## Créer les features openCV de n colmapImage
Je veux  pouvoir extraire les features opencv de n colmapImage à partir des points2d  decrit dans la classe ColmapImage:
A partir d'une instance de ColmapImage, je veux pouvoir crer une instance de ColmapImageOpenCV qui comprendra comme variable: une ColmapImage, le nom de l'image (recuperer via ColmapDatabaseReader), les fetaures opencv correspondant à chaque ColmapImageObservation;
ColmapImageOpenCVFactory fournira ce service.

## Match entre 1 image et n ColmapImageOpenCV
Je voudrai un service qui utilise openCv pour tester le match entre une image et n ColmapImageOpenCV
Une image , APres avoir recuperé FeatureExtractionResult de cette image  






