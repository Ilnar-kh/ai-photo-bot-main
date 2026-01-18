package com.aiphoto.bot.core.port.persistence;

import com.aiphoto.bot.core.domain.Upload;

import java.util.List;
import java.util.UUID;

public interface UploadRepository {

    Upload save(Upload upload);

    List<Upload> findByOrderId(UUID orderId);

}
