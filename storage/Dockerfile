FROM pedrombmachado/ntu_lubuntu:comp20081
RUN apt-get update && apt-get install -y nginx
COPY storage_config.sh /usr/local/bin/storage_config.sh
RUN chmod +x /usr/local/bin/storage_config.sh
CMD ["bash", "/usr/local/bin/storage_config.sh"]
