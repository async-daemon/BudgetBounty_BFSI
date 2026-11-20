package com.budgetbounty.service;

import com.budgetbounty.dto.locker.DocumentDto;
import com.budgetbounty.entity.Document;
import com.budgetbounty.entity.User;
import com.budgetbounty.exception.BadRequestException;
import com.budgetbounty.exception.NotFoundException;
import com.budgetbounty.repository.DocumentRepository;
import com.budgetbounty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LockerService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${budgetbounty.upload-dir}")
    private String uploadDir;

    public List<DocumentDto> listForUser(Long userId) {
        return documentRepository.findByUserIdOrderByUploadedAtDesc(userId).stream().map(DocumentDto::from).toList();
    }

    @Transactional
    public DocumentDto upload(Long userId, MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided.");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
        String storedName = UUID.randomUUID() + extension;

        try {
            Path userDir = Paths.get(uploadDir, String.valueOf(userId));
            Files.createDirectories(userDir);
            Path target = userDir.resolve(storedName);
            file.transferTo(target);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }

        Document document = Document.builder()
                .user(user)
                .folder(folder == null || folder.isBlank() ? "General" : folder)
                .originalName(originalName)
                .storedName(storedName)
                .fileType(extension.replace(".", "").toUpperCase())
                .sizeBytes(file.getSize())
                .source("manual")
                .build();
        documentRepository.save(document);
        return DocumentDto.from(document);
    }

    public Resource download(Long userId, Long documentId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        Path path = Paths.get(uploadDir, String.valueOf(userId), document.getStoredName());
        return new FileSystemResource(path);
    }

    public Document getDocument(Long userId, Long documentId) {
        return documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
    }

    @Transactional
    public void delete(Long userId, Long documentId) {
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        Path path = Paths.get(uploadDir, String.valueOf(userId), document.getStoredName());
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // file already gone from disk; still remove the DB record
        }
        documentRepository.delete(document);
    }
}
