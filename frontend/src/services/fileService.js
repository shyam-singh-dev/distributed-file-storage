import axiosInstance from "../api/axiosInstance";

// ─────────────────────────────────────
// UPLOAD FILE
// ─────────────────────────────────────
export const uploadFile = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await axiosInstance.post(
    "/api/v1/files/upload",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }
  );

  return response.data;
};

// ─────────────────────────────────────
// GET MY FILES (PAGINATED)
// ─────────────────────────────────────
export const getMyFiles = async (page = 0, size = 10) => {
  const response = await axiosInstance.get(
    `/api/v1/files/my-files?page=${page}&size=${size}`
  );
  return response.data;
};

// ─────────────────────────────────────
// DOWNLOAD FILE
// ─────────────────────────────────────
export const downloadFile = async (fileId, fileName) => {
  const response = await axiosInstance.get(
    `/api/v1/files/download/${fileId}`,
    {
      responseType: "blob",
    }
  );

  // Create download link in browser
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", fileName);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

// ─────────────────────────────────────
// DELETE FILE
// ─────────────────────────────────────
export const deleteFile = async (fileId) => {
  const response = await axiosInstance.delete(
    `/api/v1/files/${fileId}`
  );
  return response.data;
};

// ─────────────────────────────────────
// SHARE FILE
// ─────────────────────────────────────
export const shareFile = async (fileId, sharedWithEmail) => {
  const response = await axiosInstance.post(
    "/api/v1/files/share",
    {
      fileId: fileId,
      sharedWithEmail: sharedWithEmail,
    }
  );
  return response.data;
};

// ─────────────────────────────────────
// GET SHARED WITH ME
// ─────────────────────────────────────
export const getSharedFiles = async () => {
  const response = await axiosInstance.get(
    "/api/v1/files/shared-with-me"
  );
  return response.data;
};

// ─────────────────────────────────────
// DOWNLOAD SHARED FILE
// ─────────────────────────────────────
export const downloadSharedFile = async (fileId, fileName) => {
  const response = await axiosInstance.get(
    `/api/v1/files/download/shared/${fileId}`,
    {
      responseType: "blob",
    }
  );

  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", fileName);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};