import { useState } from "react";
import { deleteFile, downloadFile } from "../services/fileService";
import FileShareModal from "./FileShareModal";
import Pagination from "./Pagination";

const FileList = ({ files, pagination, onPageChange, onRefresh }) => {
  const [deleting, setDeleting] = useState(null);
  const [downloading, setDownloading] = useState(null);
  const [shareModal, setShareModal] = useState(null);

  const handleDownload = async (fileId, fileName) => {
    setDownloading(fileId);
    try {
      await downloadFile(fileId, fileName);
    } catch (err) {
      alert("Download failed");
    } finally {
      setDownloading(null);
    }
  };

  const handleDelete = async (fileId, fileName) => {
    const confirmDelete = window.confirm(
      `Are you sure you want to delete "${fileName}"?`
    );

    if (!confirmDelete) return;

    setDeleting(fileId);
    try {
      await deleteFile(fileId);
      if (onRefresh) onRefresh();
    } catch (err) {
      alert("Delete failed");
    } finally {
      setDeleting(null);
    }
  };

  if (!files || files.length === 0) {
    return (
      <div className="empty-state">
        <h3>No files yet</h3>
        <p>Upload your first file using the form above.</p>
      </div>
    );
  }

  return (
    <div className="file-list-section">
      <h3>My Files</h3>

      <div className="file-table-wrapper">
        <table className="file-table">
          <thead>
            <tr>
              <th>File Name</th>
              <th>Type</th>
              <th>Size</th>
              <th>Uploaded</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {files.map((file) => (
              <tr key={file.id}>
                <td className="file-name-cell">
                  {file.originalName}
                </td>
                <td>
                  <span className="badge">
                    {file.contentType?.split("/")[1]?.toUpperCase() || "FILE"}
                  </span>
                </td>
                <td>{file.fileSizeReadable}</td>
                <td>
                  {new Date(file.createdAt).toLocaleDateString()}
                </td>
                <td className="actions-cell">
                  <button
                    className="btn btn-small btn-success"
                    onClick={() =>
                      handleDownload(file.id, file.originalName)
                    }
                    disabled={downloading === file.id}
                  >
                    {downloading === file.id ? "..." : "Download"}
                  </button>

                  <button
                    className="btn btn-small btn-info"
                    onClick={() =>
                      setShareModal({
                        id: file.id,
                        name: file.originalName,
                      })
                    }
                  >
                    Share
                  </button>

                  <button
                    className="btn btn-small btn-danger"
                    onClick={() =>
                      handleDelete(file.id, file.originalName)
                    }
                    disabled={deleting === file.id}
                  >
                    {deleting === file.id ? "..." : "Delete"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {pagination && (
        <Pagination
          currentPage={pagination.pageNumber}
          totalPages={pagination.totalPages}
          onPageChange={onPageChange}
        />
      )}

      {shareModal && (
        <FileShareModal
          fileId={shareModal.id}
          fileName={shareModal.name}
          onClose={() => setShareModal(null)}
          onShareSuccess={() => setShareModal(null)}
        />
      )}
    </div>
  );
};

export default FileList;