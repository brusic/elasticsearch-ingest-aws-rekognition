package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.elasticsearch.ingest.rekognition.service.DetectObjectsService;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DetectObjectsProcessor extends AbstractAwsRekognitionProcessor {

    public static final String TYPE = "detect-objects";
    private final DetectObjectsService detectObjectsService;

    DetectObjectsProcessor(String tag,
                           String sourceField,
                           String targetField,
                           Float minScore,
                           Integer maxValues,
                           boolean ignoreMissing,
                           boolean remove, DetectObjectsService detectObjectsService) {
        super(tag, sourceField, targetField, minScore, maxValues, ignoreMissing, remove);
        this.detectObjectsService = detectObjectsService;
    }

    @Override
    public void execute(IngestDocument ingestDocument) {
        Optional<ByteBuffer> imageBytes = getImageAndValidate(ingestDocument);
        if (!imageBytes.isPresent()) {
            return;
        }

        List<String> labels = detectObjectsService.detectObjects(imageBytes.get(), maxValues, minScore);
        setTargetField(ingestDocument, labels);
        removeSourceField(ingestDocument);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory extends CommonFactory{
        private final DetectObjectsService detectObjectsService;

        public Factory(DetectObjectsService detectObjectsService) {
            this.detectObjectsService = detectObjectsService;
        }

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, Map<String, Object> config) {
            readCommonProperties(TYPE, tag, config, "_objects");
            return new DetectObjectsProcessor(tag, source, target,  minScore, maxValues, ignoreMissing,
                    remove, detectObjectsService);
        }
    }
}
