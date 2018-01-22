package com.brusic.elasticsearch.ingest.rekognition.service;

import java.nio.ByteBuffer;
import java.util.List;

public interface DetectUnsafeContentService {

    List<String> detectUnsafeContent(ByteBuffer imageBytes, float minConfidence);
}
