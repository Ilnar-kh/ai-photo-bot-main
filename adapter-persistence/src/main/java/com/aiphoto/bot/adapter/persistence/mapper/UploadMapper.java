package com.aiphoto.bot.adapter.persistence.mapper;

import com.aiphoto.bot.adapter.persistence.entity.OrderEntity;
import com.aiphoto.bot.adapter.persistence.entity.UploadEntity;
import com.aiphoto.bot.core.domain.Upload;

public final class UploadMapper {

    private UploadMapper() {
    }

    public static Upload toDomain(UploadEntity entity) {
        return new Upload(
            entity.getId(),
            entity.getOrder().getId(),
            entity.getObjectKey(),
            entity.getContentType(),
            entity.getCreatedAt()
        );
    }

    public static UploadEntity toEntity(Upload upload, OrderEntity order) {
        UploadEntity entity = new UploadEntity();
        entity.setId(upload.id());
        entity.setOrder(order);
        entity.setObjectKey(upload.objectKey());
        entity.setContentType(upload.contentType());
        entity.setCreatedAt(upload.createdAt());
        return entity;
    }
}
