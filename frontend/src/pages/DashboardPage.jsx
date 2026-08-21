import { useState, useEffect, useCallback } from "react";
import { getLoggedInUser } from "../services/authService";
import { getMyFiles, getSharedFiles, downloadSharedFile } from "../services/fileService";
import FileUpload from "../components/FileUpload";
import FileList from "../components/FileList";

const DashboardPage = () => {
  const user = getLoggedInUser();

  const [activeTab, setActiveTab] = useState("my-files");
  const [files, setFiles] = useState([]);
  const [sharedFiles, setSharedFiles] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // ─────────────────────────────────────
  // LOAD MY FILES
  // ─────────────────────────────────────
  const loadMyFiles = useCallback(async (page = 0) => {
    setLoading(true);
    setError("");

    try {
      const response = await getMyFiles(page, 10);

      setFiles(response.data.content || []);
      setPagination({
        pageNumber: response.data.pageNumber,
        totalPages: response.data.totalPages,
        totalElements: response.data.totalElements,
      });
      setCurrentPage(page);
    } catch (err) {
      setError("Failed to load files");
    } finally {
      setLoading(false);
    }
  }, []);

  // ─────────────────────────────────────
  // LOAD SHARED FILES
  // ─────────────────────────────────────
  const loadSharedFiles = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const response = await getSharedFiles();
      setSharedFiles(response.data || []);
    } catch (err) {
      setError("Failed to load shared files");
    } finally {
      setLoading(false);
    }
  }, []);

  // ─────────────────────────────────────
  // LOAD DATA ON TAB CHANGE
  // ─────────────────────────────────────
  useEffect(() => {
    if (activeTab === "my-files") {
      loadMyFiles(currentPage);
    } else if (activeTab === "shared") {
      loadSharedFiles();
    }
  }, [activeTab, currentPage, loadMyFiles, loadSharedFiles]);

  // ─────────────────────────────────────
  // HANDLE PAGE CHANGE
  // ─────────────────────────────────────
  const handlePageChange = (page) => {
    loadMyFiles(page);
  };

  // ─────────────────────────────────────
  // HANDLE DOWNLOAD SHARED FILE
  // ─────────────────────────────────────
  const handleSharedDownload = async (fileId, fileName) => {
    try {
      await downloadSharedFile(fileId, fileName);
    } catch (err) {
      alert("Download failed");
    }
  };

  return (
    <div className="page-container">
      <div className="dashboard-card wide">
        <h2>Dashboard</h2>
        <p className="subtitle">
          Welcome, <strong>{user?.fullName}</strong> ({user?.role})
        </p>

        {/* TABS */}
        <div className="tabs">
          <button
            className={`tab ${activeTab === "my-files" ? "tab-active" : ""}`}
            onClick={() => setActiveTab("my-files")}
          >
            My Files
          </button>
          <button
            className={`tab ${activeTab === "shared" ? "tab-active" : ""}`}
            onClick={() => setActiveTab("shared")}
          >
            Shared With Me
          </button>
        </div>

        {/* MY FILES TAB */}
        {activeTab === "my-files" && (
          <>
            <FileUpload onUploadSuccess={() => loadMyFiles(currentPage)} />

            {loading && <div className="loading">Loading files...</div>}
            {error && <div className="error-message">{error}</div>}

            {!loading && !error && (
              <FileList
                files={files}
                pagination={pagination}
                onPageChange={handlePageChange}
                onRefresh={() => loadMyFiles(currentPage)}
              />
            )}
          </>
        )}

        {/* SHARED FILES TAB */}
        {activeTab === "shared" && (
          <>
            {loading && <div className="loading">Loading shared files...</div>}
            {error && <div className="error-message">{error}</div>}

            {!loading && !error && (
              <div className="file-list-section">
                <h3>Files Shared With Me</h3>

                {sharedFiles.length === 0 ? (
                  <div className="empty-state">
                    <p>No files have been shared with you yet.</p>
                  </div>
                ) : (
                  <table className="file-table">
                    <thead>
                      <tr>
                        <th>File Name</th>
                        <th>Type</th>
                        <th>Size</th>
                        <th>Uploaded By</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sharedFiles.map((file) => (
                        <tr key={file.id}>
                          <td>{file.originalName}</td>
                          <td>
                            <span className="badge">
                              {file.contentType?.split("/")[1]?.toUpperCase() || "FILE"}
                            </span>
                          </td>
                          <td>{file.fileSizeReadable}</td>
                          <td>{file.uploadedBy}</td>
                          <td>
                            <button
                              className="btn btn-small btn-success"
                              onClick={() =>
                                handleSharedDownload(
                                  file.id,
                                  file.originalName
                                )
                              }
                            >
                              Download
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default DashboardPage;