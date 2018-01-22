package com.brusic.elasticsearch.ingest.rekognition.service;

import java.nio.ByteBuffer;
import java.util.List;

public interface DetectTextService {

    List<String> detectText(ByteBuffer imageBytes, float minConfidence);
}
