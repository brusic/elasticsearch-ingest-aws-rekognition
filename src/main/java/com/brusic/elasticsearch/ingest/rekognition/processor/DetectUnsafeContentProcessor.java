package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.elasticsearch.ingest.rekognition.service.DetectUnsafeContentService;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DetectUnsafeContentProcessor extends AbstractAwsRekognitionProcessor {

    public static final String TYPE = "detect-unsafe-content";
    private final DetectUnsafeContentService detectUnsafeContentService;

    DetectUnsafeContentProcessor(String tag,
                                 String sourceField,
                                 String targetField,
                                 Float minScore,
                                 Integer maxValues,
                                 boolean ignoreMissing,
                                 boolean remove, DetectUnsafeContentService detectUnsafeContentService) {
        super(tag, sourceField, targetField, minScore, maxValues, ignoreMissing, remove);
        this.detectUnsafeContentService = detectUnsafeContentService;
    }

    @Override
    public void execute(IngestDocument ingestDocument) {
        Optional<ByteBuffer> imageBytes = getImageAndValidate(ingestDocument);
        if (!imageBytes.isPresent()) {
            return;
        }

        List<String> labels = detectUnsafeContentService.detectUnsafeContent(imageBytes.get(), minScore);
        setTargetField(ingestDocument, labels);
        removeSourceField(ingestDocument);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory extends CommonFactory{
        private final DetectUnsafeContentService detectUnsafeContentService;

        public Factory(DetectUnsafeContentService detectUnsafeContentService) {
            this.detectUnsafeContentService = detectUnsafeContentService;
        }

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, Map<String, Object> config) {
            readCommonProperties(TYPE, tag, config, "_unsafe");
            return new DetectUnsafeContentProcessor(tag, source, target,  minScore, maxValues,
                    ignoreMissing, remove, detectUnsafeContentService);
        }
    }
}
