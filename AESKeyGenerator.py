from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend
import os

def generate_aes_key(key_size=64):
    """
    Generates a random AES key of the specified size.
    Default size is 256 bits (32 bytes).
    """
    if key_size not in [16, 24, 32]:
        raise ValueError("Invalid AES key size. It must be 16, 24, or 32 bytes.")
    
    # Generate a random AES key of the specified size
    key = os.urandom(key_size)
    return key

# Example: Generate a 256-bit AES key (32 bytes)
aes_key = generate_aes_key(32)
print(f"Generated AES Key: {aes_key.hex()}")
