#!/usr/bin/env bash
set -euo pipefail


SCRIPT_PATH="$(readlink -f -- "${BASH_SOURCE[0]}")"
SCRIPT_DIR="$(dirname -- "$SCRIPT_PATH")"
COLMAP_CMD="/home/bertrand/workspaceCpp/colmap/build/src/colmap/exe/colmap"
COLMAP_EXE_PATH="$HOME/workspaceCpp/colmap/build/src/colmap/exe"

"$COLMAP_CMD" --version
BG_WORK="$SCRIPT_DIR"

SPARSE_DIR="$BG_WORK/sparse"
LOG_DIR="$SPARSE_DIR/logs"
IMAGES_DIR="$BG_WORK/images"
NB_IMAGES=$(find "$IMAGES_DIR" -maxdepth 1 -type f | wc -l)

mkdir -p "$SPARSE_DIR"
mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/colmap_sparse$(date +%Y%m%d_%H%M%S).log"

# redirige stdout+stderr vers le log, tout en gardant l'affichage terminal
exec > >(tee -a "$LOG_FILE") 2>&1
echo "bg=data  COLMAP_CMD=$COLMAP_CMD"
echo "bg=data  BG_WORK=$BG_WORK"
echo "bg=data  LOG_DIR=$LOG_DIR"
echo "bg=data  LOG_FILE=$LOG_FILE"
NB_FICHIERS=$(find "$BG_WORK" -maxdepth 1 -type f | wc -l)
LISTE_FICHIERS=$(find "$BG_WORK" -maxdepth 1 -type f -printf '%f ')
echo "bg=data NB_FICHIERS=$NB_FICHIERS"
echo "bg=data LISTE_FICHIERS=$LISTE_FICHIERS"
echo "bg=data  NB_IMAGES=$NB_IMAGES"
echo "COLMAP EXTRACTOR"
# "$COLMAP_CMD" feature_extractor --help
echo "bg=colmap process=sparse  etape=start   date=$(date -Is)"

ls -la "$BG_WORK"
echo "bg=colmap process=sparse etape=feature_extractor date=$(date -Is)"

# Extraction des features
"$COLMAP_CMD" feature_extractor \
  --database_path "$BG_WORK/database.db" \
  --image_path "$BG_WORK/images" \
  --ImageReader.single_camera 1 \
  --ImageReader.camera_model SIMPLE_RADIAL \
  --FeatureExtraction.use_gpu 1 \
  --FeatureExtraction.num_threads 7 \
  --log_level 2

echo "bg=colmap process=sparse etape=MATCH date=$(date -Is)"
# "$COLMAP_CMD" matches_importer --help

# Import des matches (fichier match.txt)
"$COLMAP_CMD" matches_importer \
  --database_path "$BG_WORK/database.db" \
  --FeatureMatching.use_gpu 0 \
  --match_list_path "$BG_WORK/match.txt"

echo "bg=colmap process=sparse etape=add_pose_gps date=$(date -Is)"
~/bgColmapUtils/bgPosePriorsProvider_4_1_0 --write "$BG_WORK/database.db" "$BG_WORK/metadataCSV.txt" 
echo "bg=colmap process=sparse etape=controle_gps date=$(date -Is)"
~/bgColmapUtils/bgPosePriorsProvider_4_1_0 --check "$BG_WORK/database.db" "$BG_WORK/metadataCSV.txt" 
echo "bg=colmap process=sparse etape=mkd_sparse  date=$(date -Is)"

# Créer le dossier sparse 
echo "xxxxxx mkdir $BG_WORK/sparse"


# Reconstruction sparse
echo "bg=colmap process=sparse etape=mapper  date=$(date -Is)"


"$COLMAP_CMD" mapper \
  --database_path "$BG_WORK/database.db" \
  --image_path "$BG_WORK/images" \
  --output_path "$BG_WORK/sparse" \
  --Mapper.num_threads 6 \
  --Mapper.multiple_models 1 \
  --Mapper.ba_use_gpu 0 \
  --Mapper.ba_global_frames_freq 1200 \
  --Mapper.ba_global_points_freq 800000 \
  --Mapper.ba_global_max_num_iterations 15 \
  --Mapper.ba_global_max_refinements 1 \
  --Mapper.ba_local_max_num_iterations 20 \
  --Mapper.tri_ignore_two_view_tracks 1 \
  --Mapper.filter_max_reproj_error 4 \
  --Mapper.abs_pose_min_num_inliers 30

echo "bg=colmap process=sparse etape=model_converter_PLY  date=$(date -Is)"

# Export PLY
SPARSE_ROOT="$BG_WORK/sparse"
found_any=0
echo "BG SPARSE_ROOT =$SPARSE_ROOT "
for model_dir in "$SPARSE_ROOT"/*; do
  [[ -d "$model_dir" ]] || continue
  model_name="$(basename "$model_dir")"

  # Option: ne garder que les dossiers qui sont des entiers
  if [[ ! "$model_name" =~ ^[0-9]+$ ]]; then
    echo "bg  model_name =$model_name step=skip" 
    continue
  fi
  echo "bg  model_name =$model_name step=loop" 
  # Détecte si un modèle COLMAP existe dans ce dossier
  has_bin=0
  [[ -f "$model_dir/cameras.bin" && -f "$model_dir/images.bin" && -f "$model_dir/points3D.bin" ]] && has_bin=1
  has_txt=0
  [[ -f "$model_dir/cameras.txt" && -f "$model_dir/images.txt" && -f "$model_dir/points3D.txt" ]] && has_txt=1

  if [[ $has_bin -eq 0 && $has_txt -eq 0 ]]; then
    echo "bg=colmap process=sparse model=$model_name etape=skip reason=no_model_files dir=$model_dir date=$(date -Is)"
    continue
  fi

  found_any=1
  echo "bg=colmap process=sparse model=$model_name etape=export_start dir=$model_dir date=$(date -Is)"

  # Export PLY
  "$COLMAP_CMD" model_converter \
    --input_path "$model_dir" \
    --output_path "$model_dir/points3D.ply" \
    --output_type PLY

  # Export TXT (cameras.txt / images.txt / points3D.txt)
  "$COLMAP_CMD" model_converter \
    --input_path "$model_dir" \
    --output_path "$model_dir" \
    --output_type TXT



  echo "bg=colmap process=sparse model=$model_name etape=export_done dir=$model_dir date=$(date -Is)"
done

if [[ $found_any -eq 0 ]]; then
  echo "bg=WARNING process=sparse etape=export_models_none reason=no_sparse_models_found root=$SPARSE_ROOT date=$(date -Is)"
fi

echo "bg=colmap process=sparse etape=export_models_end date=$(date -Is)"
  
echo "bg=colmap process=sparse etape=model_analyzer  date=$(date -Is)"
  
"$COLMAP_CMD" model_analyzer --path "$BG_WORK/sparse/0"


echo "bg=colmap process=sparse etape=fin_sparse  date=$(date -Is)"


### ./processColmapDense.sh
