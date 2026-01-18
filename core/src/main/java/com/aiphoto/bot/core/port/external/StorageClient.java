package com.aiphoto.bot.core.port.external;

public interface StorageClient {

    String generateUploadUrl(String objectKey, String contentType);
}
