FROM pedrombmachado/ntu_lubuntu:comp20081

# Update apt and install nginx and python3
RUN apt-get update && apt-get install -y nginx python3 python3-pip && rm -rf /var/lib/apt/lists/*

# Install necessary Python packages for file upload handling (Flask or SimpleHTTPServer)
RUN pip3 install Flask

# Copy the storage configuration script and custom Python file upload handler
COPY storage_config.sh /usr/local/bin/storage_config.sh
COPY upload_handler.py /usr/local/bin/upload_handler.py

# Make sure the scripts are executable
RUN chmod +x /usr/local/bin/storage_config.sh
RUN chmod +x /usr/local/bin/upload_handler.py

# Expose ports for Nginx and Python server
EXPOSE 80 5000

# Start both Nginx and the Python file upload server in the background
CMD ["bash", "/usr/local/bin/storage_config.sh"]
