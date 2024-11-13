## install transformers torch einops
## install 'numpy<2'

from transformers import AutoModel
## install flash_attn
def get_embeddings(text_chunks: list) -> list:
    """
    Converts a list of text chunks to their embeddings.
    
    Parameters:
    text_chunks (list): A list of text strings to convert to embeddings.
    
    Returns:
    list: A list of embeddings, one for each text chunk.
    """
    # Initialize the model
    model = AutoModel.from_pretrained("jinaai/jina-embeddings-v3", trust_remote_code=True)
    embeddings = [model.encode(chunk) for chunk in text_chunks]
    return embeddings


### Compute similarities
##  print(embeddings[0] @ embeddings[1].T) # T is for Transposition of a matrix, @ is for dot product