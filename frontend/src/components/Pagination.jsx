const Pagination = ({ currentPage, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  const pages = [];
  for (let i = 0; i < totalPages; i++) {
    pages.push(i);
  }

  return (
    <div className="pagination">
      <button
        className="btn btn-small"
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
      >
        Previous
      </button>

      {pages.map((page) => (
        <button
          key={page}
          className={`btn btn-small ${
            page === currentPage ? "btn-active" : ""
          }`}
          onClick={() => onPageChange(page)}
        >
          {page + 1}
        </button>
      ))}

      <button
        className="btn btn-small"
        disabled={currentPage === totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
      >
        Next
      </button>
    </div>
  );
};

export default Pagination;