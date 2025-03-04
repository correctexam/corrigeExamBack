import cv2
import numpy as np
import sys
import json
import base64

def refine_extract_lines(image_path):
    # Load the image
    img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)

    # Binarize the image
    _, binary_img = cv2.threshold(img, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

    # Calculate the horizontal projection
    horizontal_projection = np.sum(binary_img, axis=1)

    # Find line boundaries with filtering
    threshold = np.max(horizontal_projection) * 0.05  # Ignore very low-projection areas
    line_boundaries = []
    in_line = False
    start = 0

    for i, value in enumerate(horizontal_projection):
        if value > threshold and not in_line:  # Start of a new line
            start = i
            in_line = True
        elif value <= threshold and in_line:  # End of a line
            line_boundaries.append((start, i))
            in_line = False

    # Merge and filter lines
    merged_boundaries = []
    merge_threshold = 2  # Merge lines within this pixel distance
    for start, end in line_boundaries:
        # Skip very small lines (likely blank lines or noise)
        if end - start < 2:
            continue
        if merged_boundaries and start - merged_boundaries[-1][1] <= merge_threshold:
            merged_boundaries[-1] = (merged_boundaries[-1][0], end)
        else:
            merged_boundaries.append((start, end))

    # Extract the lines
    refined_lines = []
    for start, end in merged_boundaries:
        # Skip blank lines
        region = binary_img[start:end, :]
        if np.sum(region) < 1000:  # Skip almost blank lines
            continue
        pad = 8 # Padding for cleaner cuts
        line_img = img[max(0, start-pad):min(img.shape[0], end+pad), :]
        refined_lines.append(line_img)

    return refined_lines

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No image path provided"}))
        sys.exit(1)

    image_path = sys.argv[1]

    try:
        # Process the image and extract refined lines
        refined_lines = refine_extract_lines(image_path)

        # Convert the refined lines to base64-encoded strings
        base64_lines = []
        for line_img in refined_lines:
            _, buffer = cv2.imencode('.png', line_img)
            base64_line = base64.b64encode(buffer).decode('utf-8')
            base64_lines.append(base64_line)

        # Output the result as JSON
        print(json.dumps({"refinedLines": base64_lines}))

    except Exception as e:
        # Handle errors and output them as JSON
        print(json.dumps({"error": str(e)}))
        sys.exit(1)