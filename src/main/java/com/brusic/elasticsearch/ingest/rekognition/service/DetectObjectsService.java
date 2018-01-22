package com.brusic.elasticsearch.ingest.rekognition.service;

import java.nio.ByteBuffer;
import java.util.List;

public interface DetectObjectsService {

    List<String> detectObjects(ByteBuffer imageBytes, int maxResults, float minConfidence);
}
