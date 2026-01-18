package com.aiphoto.bot.adapter.persistence.repository;

import com.aiphoto.bot.adapter.persistence.mapper.ImageMapper;
import com.aiphoto.bot.adapter.persistence.repository.jpa.ImageJpaRepository;
import com.aiphoto.bot.adapter.persistence.repository.jpa.JobJpaRepository;
import com.aiphoto.bot.core.domain.Image;
import com.aiphoto.bot.core.port.persistence.ImageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class ImageRepositoryAdapter implements ImageRepository {

    private final ImageJpaRepository imageJpaRepository;
    private final JobJpaRepository jobJpaRepository;

    public ImageRepositoryAdapter(ImageJpaRepository imageJpaRepository,
                                  JobJpaRepository jobJpaRepository) {
        this.imageJpaRepository = imageJpaRepository;
        this.jobJpaRepository = jobJpaRepository;
    }

    @Override
    @Transactional
    public Image save(Image image) {
        return ImageMapper.toDomain(imageJpaRepository.save(
            ImageMapper.toEntity(image, jobJpaRepository.getReferenceById(image.jobId()))
        ));
    }

    @Override
    public List<Image> findByJobId(UUID jobId) {
        return imageJpaRepository.findByJobId(jobId).stream().map(ImageMapper::toDomain).toList();
    }
}
