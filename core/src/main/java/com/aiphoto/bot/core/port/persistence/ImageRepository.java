package com.aiphoto.bot.core.port.persistence;

import com.aiphoto.bot.core.domain.Image;

import java.util.List;
import java.util.UUID;

public interface ImageRepository {

    Image save(Image image);

    List<Image> findByJobId(UUID jobId);
}
