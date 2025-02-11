FROM pedrombmachado/ntu_lubuntu:comp20081

# Set working directory
WORKDIR /app

# Install Python, pip, and SSH server
RUN apt update && apt install -y python3 python3-pip python3-venv openssh-server supervisor

# Create and activate virtual environment
RUN python3 -m venv /app/venv

# Copy application files
COPY app.py requirements.txt /app/

# Create directory for storing chunks
RUN mkdir -p /chunks

# Install dependencies into the virtual environment
RUN /app/venv/bin/pip install --no-cache-dir -r requirements.txt

# Configure SSH
RUN mkdir -p /var/run/sshd
RUN chmod 0755 /var/run/sshd

# Set root password (Change 'rootpassword' to a secure password)
RUN echo 'root:Rootpassword@23' | chpasswd

# Allow root login and password authentication
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN echo 'PasswordAuthentication yes' >> /etc/ssh/sshd_config
RUN echo 'Port 22' >> /etc/ssh/sshd_config

# Configure supervisord
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Expose Flask and SSH ports
EXPOSE 5000 22

# Start supervisord
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
