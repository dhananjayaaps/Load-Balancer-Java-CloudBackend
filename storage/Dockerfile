# Use the base image
FROM pedrombmachado/ntu_lubuntu:comp20081

# Update apt and install nginx, python3, and pip
RUN apt-get update && apt-get install -y nginx python3 python3-pip && rm -rf /var/lib/apt/lists/*

# Install necessary Python packages (e.g., Flask)
# RUN pip3 install Flask

# Copy the storage configuration script and custom Python file upload handler
COPY storage_config.sh /usr/local/bin/storage_config.sh
COPY file_server.py /usr/local/bin/file_server.py

# Make the scripts executable
RUN chmod +x /usr/local/bin/storage_config.sh /usr/local/bin/file_server.py

# Expose ports for Nginx and Python server
EXPOSE 80 8080

# Start Nginx and the Python file upload server
CMD ["bash", "/usr/local/bin/storage_config.sh"]