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



