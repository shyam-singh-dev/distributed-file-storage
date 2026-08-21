import { useState } from "react";
import { shareFile } from "../services/fileService";

const FileShareModal = ({ fileId, fileName, onClose, onShareSuccess }) => {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const handleShare = async () => {
    if (!email.trim()) {
      setError("Please enter an email address");
      return;
    }

    setLoading(true);
    setError("");
    setMessage("");

    try {
      const response = await shareFile(fileId, email);
      setMessage(response.message || "File shared successfully");
      setEmail("");

      if (onShareSuccess) {
        onShareSuccess();
      }

      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      const errorMessage =
        err.response?.data?.message || "Share failed";
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h3>Share File</h3>
        <p className="subtitle">
          Sharing: <strong>{fileName}</strong>
        </p>

        {message && <div className="success-message">{message}</div>}
        {error && <div className="error-message">{error}</div>}

        <div className="form-group">
          <label>Share with email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="user@email.com"
          />
        </div>

        <div className="modal-actions">
          <button
            className="btn btn-primary"
            onClick={handleShare}
            disabled={loading}
          >
            {loading ? "Sharing..." : "Share"}
          </button>
          <button className="btn btn-secondary" onClick={onClose}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default FileShareModal;