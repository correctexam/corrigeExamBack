import fitz  # PyMuPDF to install
from semantic_text_splitter import TextSplitter # semantic_text_splitter to install
## Move installs to init of pyenv


def read_pdf_file_as_binary(file_path: str) -> bytes:
    with open(file_path, 'rb') as file:
        return file.read()


def extract_text_from_pdf(pdf_binary: bytes) -> str:
    """
    Extracts text from a PDF binary.
    
    Parameters:
    pdf_binary (bytes): The binary content of a PDF file.
    
    Returns:
    str: The extracted text from the PDF.
    """
    text = ""
    with fitz.open("pdf", pdf_binary) as doc:
        for page_num in range(len(doc)):
            page = doc.load_page(page_num)
            text += page.get_text()
    return text

def chunk_text(text: str, chunk_size_min: int = 300, chunk_size_max: int = 500) -> list:
    """
    Splits text semantically into chunks within a specified range.
    
    Parameters:
    text (str): The input text to split.
    chunk_size_min (int): The minimum size of each chunk. Default is 300 characters.
    chunk_size_max (int): The maximum size of each chunk. Default is 500 characters.
    
    Returns:
    list: A list of text chunks.
    """
    splitter = TextSplitter((chunk_size_min,chunk_size_max))
    chunks = splitter.chunks(text)
    return chunks

def chunk_text_from_pdf(pdf_binary: bytes, chunk_size_min: int = 300, chunk_size_max: int = 500) -> list:
    """
    Splits text semantically into chunks within a specified range, from a pdf.
    
    Parameters:
    pdf_binary (bytes): The binary content of a PDF file.
    chunk_size_min (int): The minimum size of each chunk. Default is 300 characters.
    chunk_size_max (int): The maximum size of each chunk. Default is 500 characters.
    
    Returns:
    list: A list of text chunks.
    """
    text = extract_text_from_pdf(pdf_binary=pdf_binary)
    splitter = TextSplitter((chunk_size_min,chunk_size_max))
    chunks = splitter.chunks(text)
    return chunks
