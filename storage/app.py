from flask import Flask, request, send_file, jsonify
import os

app = Flask(__name__)

# Directory to store chunks inside the container
STORAGE_DIR = "/chunks"
os.makedirs(STORAGE_DIR, exist_ok=True)

@app.route("/", methods=["GET"])
def health_check():
    return jsonify({"status": "running"}), 200

@app.route("/upload/<filename>", methods=["POST"])
def upload_chunk(filename):
    try:
        file_path = os.path.join(STORAGE_DIR, filename)
        with open(file_path, "wb") as f:
            f.write(request.data)
        return jsonify({"message": f"Chunk '{filename}' saved successfully."}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/download/<filename>", methods=["GET"])
def download_chunk(filename):
    file_path = os.path.join(STORAGE_DIR, filename)
    if os.path.exists(file_path):
        return send_file(file_path, as_attachment=True)
    else:
        return jsonify({"error": "Chunk not found."}), 404

@app.route("/delete/<filename>", methods=["DELETE"])
def delete_chunk(filename):
    file_path = os.path.join(STORAGE_DIR, filename)
    if os.path.exists(file_path):
        os.remove(file_path)
        return jsonify({"message": f"Chunk '{filename}' deleted successfully."}), 200
    else:
        return jsonify({"error": "Chunk not found."}), 404

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
