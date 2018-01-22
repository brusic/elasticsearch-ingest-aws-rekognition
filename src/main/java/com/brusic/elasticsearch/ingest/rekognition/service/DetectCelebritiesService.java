package com.brusic.elasticsearch.ingest.rekognition.service;

import com.brusic.aws.rekognition.model.DetectCelebritiesResult;

import java.nio.ByteBuffer;

public interface DetectCelebritiesService {

    DetectCelebritiesResult detectCelebrities(ByteBuffer imageBytes, float minConfidence);
}
