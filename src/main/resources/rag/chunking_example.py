
from chunking import extract_text_from_pdf, chunk_text, chunk_text_from_pdf, read_pdf_file_as_binary

# Specify the path to PDF file
pdf_file_path = 'corrigeExamBackDAN/src/main/resources/rag/English_Course.pdf'

# Read the PDF binary data
pdf_binary = read_pdf_file_as_binary(pdf_file_path)

# Extracting text from the PDF binary
extracted_text = extract_text_from_pdf(pdf_binary)
print("Extracted Text from PDF:")
print(extracted_text)

# Chunk the extracted text
chunks = chunk_text(extracted_text, chunk_size_min=50, chunk_size_max=100)
print("\nChunked Text:")
for i, chunk in enumerate(chunks):
    print(f"Chunk {i + 1}: {chunk}")

# Chunk text directly from the PDF binary, with different chunking sizes
pdf_chunks = chunk_text_from_pdf(pdf_binary, chunk_size_min=4000, chunk_size_max=8194) ## Max Token Size is 8194
print("\nPDF Text Chunks:")
for i, chunk in enumerate(pdf_chunks):
    print(f"Chunk {i + 1}: {chunk}")
