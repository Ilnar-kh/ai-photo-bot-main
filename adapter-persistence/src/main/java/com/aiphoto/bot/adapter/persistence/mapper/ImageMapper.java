package com.aiphoto.bot.adapter.persistence.mapper;

import com.aiphoto.bot.adapter.persistence.entity.ImageEntity;
import com.aiphoto.bot.adapter.persistence.entity.JobEntity;
import com.aiphoto.bot.core.domain.Image;

public final class ImageMapper {

    private ImageMapper() {
    }

    public static Image toDomain(ImageEntity entity) {
        return new Image(
            entity.getId(),
            entity.getJob().getId(),
            entity.getObjectKey(),
            entity.getCreatedAt()
        );
    }

    public static ImageEntity toEntity(Image image, JobEntity job) {
        ImageEntity entity = new ImageEntity();
        entity.setId(image.id());
        entity.setJob(job);
        entity.setObjectKey(image.objectKey());
        entity.setCreatedAt(image.createdAt());
        return entity;
    }
}
