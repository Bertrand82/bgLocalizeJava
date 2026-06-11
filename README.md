# bgLocalizeJava
Localisation à partir d'images : POC en java/opencv destiné à être porté en cpp.

# But :
A partir de données issues du traitement colmap, d'une pre-estimation de la pose d'une image (localisation de la camera); essayer d'avoir une localisation précise de la camera.

# Etapes de traitements:

  - Estimation "grossier" de la position de l'image à partir des metasdonnées.
  - Recuperation de n images les plus proches.
  - Extraction des features de l'images
  - Match des features avec les n images
  - Position de la camera (pose) par trigo 

# Prise de vue avec la camera "Action Camera"



# Extraction features



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



