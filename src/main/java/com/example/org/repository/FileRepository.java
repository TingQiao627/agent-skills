package com.example.org.repository;

import com.example.org.entity.File;
import com.example.org.entity.File.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByHash(String hash);
    Optional<File> findByUploadId(String uploadId);
}