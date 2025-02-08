# Use the specified base image
FROM pedrombmachado/ntu_lubuntu:comp20081

# Set working directory
WORKDIR /app

# Install Python and pip
RUN apt update && apt install -y python3 python3-pip python3-venv

# Create and activate virtual environment
RUN python3 -m venv /app/venv

# Copy application files
COPY app.py requirements.txt /app/

# Create directory for storing chunks
RUN mkdir -p /chunks

# Install dependencies into the virtual environment
RUN /app/venv/bin/pip install --no-cache-dir -r requirements.txt

# Expose the Flask port
EXPOSE 5000

# Run the Flask application using the virtual environment's Python
CMD ["/app/venv/bin/python", "app.py"]