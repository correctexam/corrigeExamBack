
from embedding import get_embeddings
from chunking import chunk_text_from_pdf, read_pdf_file_as_binary

# Specify the path to PDF file
pdf_file_path = 'corrigeExamBackDAN/src/main/resources/rag/English_Course.pdf'

# Read the PDF binary data
pdf_binary = read_pdf_file_as_binary(pdf_file_path)

# Chunk text directly from the PDF binary
pdf_chunks = chunk_text_from_pdf(pdf_binary, chunk_size_min=450, chunk_size_max=500)
embeddings = get_embeddings(pdf_chunks)
print("\nPDF Text Chunks With Embeddings:")
for i, chunk in enumerate(pdf_chunks):
    print(f"Chunk {i + 1}: {chunk}")
    print(f"Embedding {i + 1}: {embeddings[i]}")


## GPU much needed as it took +20 minutes on my computer running on my CPU, see GCloud and AWS