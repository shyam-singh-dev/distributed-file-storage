import { useState, useRef } from "react";
import { uploadFile } from "../services/fileService";

const FileUpload = ({ onUploadSuccess }) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const fileInputRef = useRef(null);

  const handleFileChange = (event) => {
    const file = event.target.files[0];

    if (file) {
      setSelectedFile(file);
      setMessage("");
      setError("");
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setError("Please select a file first");
      return;
    }

    setUploading(true);
    setMessage("");
    setError("");

    try {
      const response = await uploadFile(selectedFile);

      setMessage(
        `Uploaded: ${response.data.originalName} (${response.data.fileSizeReadable})`
      );

      setSelectedFile(null);

      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }

      if (onUploadSuccess) {
        onUploadSuccess();
      }
    } catch (err) {
      const errorMessage =
        err.response?.data?.message || "File upload failed";
      setError(errorMessage);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="upload-section">
      <h3>Upload File</h3>
      <p className="subtitle">
        Supported: Images, PDF, Office docs, Text, ZIP, Video, Audio.
        Max 100MB.
      </p>

      {message && <div className="success-message">{message}</div>}
      {error && <div className="error-message">{error}</div>}

      <div className="upload-controls">
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileChange}
          className="file-input"
        />

        <button
          onClick={handleUpload}
          className="btn btn-primary"
          disabled={uploading || !selectedFile}
        >
          {uploading ? "Uploading..." : "Upload"}
        </button>
      </div>

      {selectedFile && (
        <div className="file-preview">
          <strong>Selected:</strong> {selectedFile.name} (
          {(selectedFile.size / 1024).toFixed(2)} KB)
        </div>
      )}
    </div>
  );
};

export default FileUpload;