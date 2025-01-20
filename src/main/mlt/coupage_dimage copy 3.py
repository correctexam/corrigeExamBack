import cv2
import numpy as np
import sys
import json
import base64
import os
import subprocess
import tempfile
import logging


def extract_coordinates(line):
    """
    On extrait les coordonnées d'une ligne
    """
    coords_une_ligne = []
    line_splited1 = line.split(":")
    if len(line_splited1) == 2:
        line_splited2 = line_splited1[1].strip().split(";")
    elif len(line_splited1) == 3:
        line_splited2 = line_splited1[2].strip().split(";")
    else:
        raise ValueError("Erreur: La ligne ne contient pas le bon format.")

    for coords_str in line_splited2:
        coords = coords_str.split(",")
        coords = [int(coord) for coord in coords]  # Convertir les coordonnées en entiers
        coords_une_ligne.append(coords)

    return coords_une_ligne


def load_coordinates(file):
    """
    On charge les coordonnées du fichier
    """
    lines = file.readlines()
    coordinates = {"haut": [], "bas": []}
    for line in lines:
        if line.startswith("Haut"):
            coordinates["haut"].append(extract_coordinates(line))
        elif line.startswith("Bas"):
            coordinates["bas"].append(extract_coordinates(line))
    return coordinates


def detect_lines(image_path):
    """
    On détecte les lignes dans l'image avec LineDetector
    Args:
        image_path: le chemin de l'image

    Returns: Un dictionnaire contenant les coordonnées des lignes
    """
    with tempfile.NamedTemporaryFile("r") as named_pipe:
        subprocess.run(
            ["/home/thomas/Documents/Projet4A/corrigeExamBackDAN/src/main/resources/linedetector/LineDetector-x86_64.AppImage", image_path, named_pipe.name],
            capture_output=True,
        )
        return load_coordinates(named_pipe)


def add_white_bands(image, top_height=50, bottom_height=50):
    """
    Ajoute des bandes blanches en haut et en bas de l'image.
    """
    # Création de la bande blanche en haut et en bas
    top_band = np.ones((top_height, image.shape[1], 3), dtype=np.uint8) * 255  # Blanc
    bottom_band = np.ones((bottom_height, image.shape[1], 3), dtype=np.uint8) * 255  # Blanc

    # Ajouter la bande blanche en haut et en bas de l'image
    image_with_bands = cv2.vconcat([top_band, image, bottom_band])
    return image_with_bands


def join_lines_individually(register, page, image_path, coordinates):
    """
    On récupère les rectangles contenant le texte de chaque ligne et on les traite individuellement.
    Ajoute des bandes blanches en haut et en bas de chaque ligne extraite.
    """
    image = cv2.imread(image_path)
    
    base64_lines = []

    # Trouver la hauteur maximale parmi toutes les régions extraites
    max_height = 100

    # Parcourir les coordonnées Haut et Bas
    for idx, (haut, bas) in enumerate(zip(coordinates["haut"], coordinates["bas"])):
        # Augmenter les coordonnées Haut et Bas
        haut_augmente = [(x, max(0, y - 15)) for x, y in haut]
        bas_augmente = [(x, y + 15) for x, y in bas]

        # Trouver les coordonnées du rectangle
        x_haut_gauche = min(haut_augmente, key=lambda x: x[0])[0]
        x_bas_droite = max(bas_augmente, key=lambda x: x[0])[0]
        y_haut = min(haut_augmente, key=lambda x: x[1])[1]
        y_bas = max(bas_augmente, key=lambda x: x[1])[1]

        roi = image[y_haut:y_bas, x_haut_gauche:x_bas_droite]

        if not roi.size == 0:
            # Redimensionner la région pour avoir la même hauteur que la hauteur maximale
            roi_resized = cv2.resize(roi, (roi.shape[1], max_height))

            # Ajouter les bandes blanches en haut et en bas
            roi_with_bands = add_white_bands(roi_resized)

            # Convertir l'image traitée en base64 et ajouter à la liste
            base64_line = convert_image_to_base64(roi_with_bands)
            base64_lines.append(base64_line)

    return base64_lines


def join_cell_lines_individually(register, page, cell_image_path):
    """
    On joint les lignes de la cellule donnée en paramètre une par une.
    Args:
        register:
        page:
        cell_image_path: le chemin de l'image

    Returns: une liste des lignes en base64 avec bandes blanches.
    """
    coordinates = detect_lines(cell_image_path)
    if len(coordinates["haut"]) == 0 or len(coordinates["bas"]) == 0:
        # no lines detected, nothing to join
        logging.warning(f"No lines detected in {cell_image_path}! ")
        return []  # Retourner une liste vide si aucune ligne n'est détectée.
    
    return join_lines_individually(register, page, cell_image_path, coordinates)

def convert_image_to_base64(image):
    """Convertit une image en base64."""
    _, buffer = cv2.imencode('.png', image)
    base64_image = base64.b64encode(buffer).decode('utf-8')
    return base64_image


def save_base64_to_file(base64_list, output_file):
    """Sauvegarde la liste base64 dans un fichier."""
    with open(output_file, 'w') as f:
        json.dump(base64_list, f)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No image path provided"}))
        sys.exit(1)
    
    image_path = sys.argv[1]
    base64_lines = []

    try:
        # Appliquez le traitement des lignes et récupérez les lignes traitées individuellement
        base64_lines = join_cell_lines_individually("", "", image_path)
        # Sauvegarder la base64 dans un fichier
        save_base64_to_file(base64_lines, "output_base64.txt")
        
        # Retourner toutes les lignes en base64
        print(json.dumps({"refinedLines": base64_lines}))
        
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)
