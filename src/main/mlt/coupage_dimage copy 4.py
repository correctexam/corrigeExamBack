import cv2
import numpy as np
import json
import base64
import os

def add_white_bands(image, top_height=50, bottom_height=50):
    """
    Ajoute des bandes blanches en haut et en bas de l'image.
    """
    top_band = np.ones((top_height, image.shape[1], 3), dtype=np.uint8) * 255
    bottom_band = np.ones((bottom_height, image.shape[1], 3), dtype=np.uint8) * 255
    image_with_bands = cv2.vconcat([top_band, image, bottom_band])
    return image_with_bands

def is_content_line(line_img):
    """
    Vérifie si une ligne contient du contenu manuscrit.
    """
    gray = cv2.cvtColor(line_img, cv2.COLOR_BGR2GRAY)
    _, binary = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    h_proj = np.sum(binary, axis=1)
    h_std = np.std(h_proj[h_proj > 0.1]) if np.any(h_proj > 0.1) else 0
    num_labels, _ = cv2.connectedComponents(binary)
    height, width = binary.shape
    filled_ratio = np.sum(binary > 0) / (height * width)
    
    # Améliorer la détection des lignes vides
    return (h_std > 0.1 and num_labels > 2 and 0.001 < filled_ratio < 0.4)

def extract_text_lines(image_path):
    """
    Extrait les lignes de texte d'une image, en ajoutant des bandes blanches.
    """
    img = cv2.imread(image_path)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    _, binary_img = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    horizontal_projection = np.sum(binary_img, axis=1)
    threshold = np.max(horizontal_projection) * 0.05
    line_boundaries = []
    in_line = False
    start = 0

    for i, value in enumerate(horizontal_projection):
        if value > threshold and not in_line:
            start = i
            in_line = True
        elif value <= threshold and in_line:
            line_boundaries.append((start, i))
            in_line = False

    merged_boundaries = []
    merge_threshold = 5  # Augmentation du seuil de fusion pour éviter les erreurs
    for start, end in line_boundaries:
        if end - start < 2:
            continue
        if merged_boundaries and start - merged_boundaries[-1][1] <= merge_threshold:
            # Fusionner les lignes seulement si elles sont assez proches
            merged_boundaries[-1] = (merged_boundaries[-1][0], end)
        else:
            merged_boundaries.append((start, end))

    refined_lines = []
    if not os.path.exists("output"):
        os.makedirs("output")

    for idx, (start, end) in enumerate(merged_boundaries):
        pad = 15  # Padding pour avoir plus de pixels autour des lignes
        line_img = img[max(0, start - pad):min(img.shape[0], end + pad), :]
        if is_content_line(line_img):
            line_with_bands = add_white_bands(line_img)
            refined_lines.append(line_with_bands)
            cv2.imwrite(f"output/line_{idx}.png", line_with_bands)

    return refined_lines

def convert_lines_to_base64(lines):
    """
    Convertit une liste d'images en base64.
    """
    base64_lines = []
    for line_img in lines:
        _, buffer = cv2.imencode('.png', line_img)
        base64_line = base64.b64encode(buffer).decode('utf-8')
        base64_lines.append(base64_line)
    return base64_lines

if __name__ == "__main__":
    import sys
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No image path provided"}))
        sys.exit(1)

    image_path = sys.argv[1]
    try:
        text_lines = extract_text_lines(image_path)
        if text_lines:
            refined_lines = convert_lines_to_base64(text_lines)
            print(json.dumps({"refinedLines": refined_lines}))
        else:
            print(json.dumps({"refinedLines": []}))
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)
