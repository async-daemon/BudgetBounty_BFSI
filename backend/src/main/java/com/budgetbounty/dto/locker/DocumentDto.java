package com.budgetbounty.dto.locker;

import com.budgetbounty.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DocumentDto {
    private Long id;
    private String folder;
    private String originalName;
    private String fileType;
    private Long sizeBytes;
    private String source;
    private LocalDateTime uploadedAt;

    public static DocumentDto from(Document d) {
        return new DocumentDto(d.getId(), d.getFolder(), d.getOriginalName(), d.getFileType(), d.getSizeBytes(),
                d.getSource(), d.getUploadedAt());
    }
}
