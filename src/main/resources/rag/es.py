## install elasticsearch

from elasticsearch import Elasticsearch, helpers
import uuid
from embedding import get_embeddings
from chunking import chunk_text_from_pdf, read_pdf_file_as_binary

# Connect to local Elasticsearch
es = Elasticsearch("http://localhost:9200")

def create_index(index_name: str):
    """
    Creates an index in the ElasticSearch Client if it does not exists
    Renders a print if index creation concluded successfully

    Parameters:
    index_name (str): The index to be created.
    """
    # Ensure index exists with proper mapping
    if not es.indices.exists(index=index_name):
        es.indices.create(
            index=index_name,
            body={
                "mappings": {
                    "properties": {
                        "id": {"type": "keyword"},   
                        "text": {"type": "text"},    
                        "embedding": {
                            "type": "dense_vector",
                            "dims": 1024
                        }
                    }
                }
            }
        )
        print(f"Index {index_name} created successfully.")



def add_data(texts: list, exam_name: str = "exam", course_name: str = "course") : 
    """
    Add texts with the corresponding embedding to an elasticsearch.
    
    Parameters:
    texts (list): The list of texts to add.
    exam_name (str): The name of the exam in which we add data 
    course_name (str): The name of the course in which we add data
    """
    # Make index relevnant to exam and course name
    index_name = exam_name + "_" + course_name
    create_index(index_name=index_name)

    embeddings = get_embeddings(texts)

    documents = []
    i = 0

    for text in texts:
        document = {
            "_id": str(uuid.uuid4()),  # Dynamic unique ID
            "text": text,
            "embedding": embeddings[i]
        }
        documents.append(document)

    # Bulk index the documents
    helpers.bulk(es, documents, index=index_name)
    print("Data added successfully!")


def add_data_from_pdf(pdf_binary: bytes, exam_name: str = "exam", course_name: str = "course") : 
    """
    Add texts from a pdf, with the corresponding embedding, to an elasticsearch.
    
    Parameters:
    pdf_binary (bytes): The binary content of a PDF file.
    exam_name (str): The name of the exam in which we add data 
    course_name (str): The name of the course in which we add data
    """
    texts = chunk_text_from_pdf(pdf_binary=pdf_binary)
    add_data(texts, exam_name=exam_name, course_name=course_name)

def add_data_from_pdf_path(pdf_path, exam_name="exam", course_name="course"):
    """
    Extract text from a PDF using it's path, chunk it, generate embeddings, and add to Elasticsearch.
    
    Parameters:
    pdf_path (str): Path to the PDF file.
    exam_name (str): Exam name for indexing.
    course_name (str): Course name for indexing.
    """
    pdf_binary = read_pdf_file_as_binary(pdf_path)
    chunks = chunk_text_from_pdf(pdf_binary, chunk_size_min=450, chunk_size_max=500)
    add_data(texts=chunks, exam_name=exam_name, course_name=course_name)

def get_relevant_chunks(text: str, index_name: str, top_n: int = 5):
    """
    Get the most relevant chunks from Elasticsearch based on the similarity of their embeddings.

    Parameters:
    text (str): The input text to search for relevant chunks.
    index_name (str): The name of the Elasticsearch index to query.
    top_n (int): The number of top relevant chunks to retrieve.

    Returns:
    list: The most relevant chunks from Elasticsearch.
    """
    # Get the embedding of the input text
    input_embedding = get_embeddings([text])[0]

    # Query Elasticsearch for documents with vectors
    response = es.search(index=index_name, body={
        "query": {
            "knn": {
                "embedding": {
                    "vector": input_embedding.tolist(),  # Convert numpy array to list
                    "k": top_n
                }
            }
        }
    })

    # Extract relevant chunks based on cosine similarity score
    relevant_chunks = []
    for doc in response['hits']['hits']:
        relevant_chunks.append(doc["_source"])

    return relevant_chunks
