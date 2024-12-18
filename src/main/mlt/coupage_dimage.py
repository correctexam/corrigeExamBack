import cv2
import numpy as np
import sys
import json
import base64

def is_content_line(line_img):
    """
    Check if the line contains actual handwritten content vs printed text or ruled lines
    Returns True if it's likely handwritten content
    """
    # Get binary image
    _, binary = cv2.threshold(line_img, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    
    # Calculate horizontal and vertical projections
    h_proj = np.sum(binary, axis=1)
    v_proj = np.sum(binary, axis=0)
    
    # Normalize projections
    h_proj = h_proj / np.max(h_proj) if np.max(h_proj) > 0 else h_proj
    v_proj = v_proj / np.max(v_proj) if np.max(v_proj) > 0 else v_proj
    
    # Check for characteristics of handwritten content:
    # 1. Variation in stroke width
    h_std = np.std(h_proj[h_proj > 0.1])
    
    # 2. Presence of connected components
    num_labels, _ = cv2.connectedComponents(binary)
    
    # Calculate content density
    height, width = line_img.shape
    filled_ratio = np.sum(binary > 0) / (height * width)
    
    # Line is likely content if:
    # - Has significant stroke variation
    # - Has multiple connected components
    # - Takes up reasonable portion of image
    return (h_std > 0.2 and  # Has variation in stroke width
            num_labels > 5 and  # Has multiple components
            0.01 < filled_ratio < 0.4)  # Reasonable amount of content

def refine_extract_lines(image_path):
    """
    Extract lines from an image that contain handwritten content
    """
    # Load the image
    img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
    
    # Binarize the image
    _, binary_img = cv2.threshold(img, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    
    # Calculate the horizontal projection
    horizontal_projection = np.sum(binary_img, axis=1)
    
    # Find line boundaries
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
    
    # Merge close lines
    merged_boundaries = []
    merge_threshold = 2
    for start, end in line_boundaries:
        if end - start < 2:
            continue
        if merged_boundaries and start - merged_boundaries[-1][1] <= merge_threshold:
            merged_boundaries[-1] = (merged_boundaries[-1][0], end)
        else:
            merged_boundaries.append((start, end))
    
    # Extract only content lines
    refined_lines = []
    for start, end in merged_boundaries:
        pad = 8
        line_img = img[max(0, start-pad):min(img.shape[0], end+pad), :]
        
        # Only keep lines that have handwritten content
        if is_content_line(line_img):
            refined_lines.append(line_img)
    
    return refined_lines

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No image path provided"}))
        sys.exit(1)
    
    image_path = sys.argv[1]
    try:
        refined_lines = refine_extract_lines(image_path)
        base64_lines = []
        for line_img in refined_lines:
            _, buffer = cv2.imencode('.png', line_img)
            base64_line = base64.b64encode(buffer).decode('utf-8')
            base64_lines.append(base64_line)
        
        print(json.dumps({"refinedLines": base64_lines}))
        
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)