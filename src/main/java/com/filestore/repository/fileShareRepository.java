package com.filestore.repository;

import com.filestore.entity.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface fileShareRepository extends JpaRepository<FileShare,Long> {

    // File shared with me

    List<FileShare> findBySharedWithEmail(String email);

    //  files shared by me

    List<FileShare> findBySharedByEmail(String email);

    // check if file already shared with someone

    boolean existsByFileIdAndSharedWithEmail(Long fileId,String sharedWithEmail);

    // Find specific share record

    Optional<FileShare> findByFileIdAndSharedWithEmail(Long fileId,String sharedWithEmail);

    // Delete a share
    void deleteByFileIdAndSharedWithEmail(
            Long fileId, String sharedWithEmail);
}
