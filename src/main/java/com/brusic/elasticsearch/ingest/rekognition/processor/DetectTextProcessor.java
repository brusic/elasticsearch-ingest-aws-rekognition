package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.elasticsearch.ingest.rekognition.service.DetectTextService;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DetectTextProcessor extends AbstractAwsRekognitionProcessor {

    public static final String TYPE = "detect-text";
    private final DetectTextService detectTextService;

    DetectTextProcessor(String tag,
                        String sourceField,
                        String targetField,
                        Float minScore,
                        Integer maxValues,
                        boolean ignoreMissing,
                        boolean remove, DetectTextService detectTextService) {
        super(tag, sourceField, targetField, minScore, maxValues, ignoreMissing, remove);
        this.detectTextService = detectTextService;
    }

    @Override
    public void execute(IngestDocument ingestDocument) {
        Optional<ByteBuffer> imageBytes = getImageAndValidate(ingestDocument);
        if (!imageBytes.isPresent()) {
            return;
        }

        List<String> labels = detectTextService.detectText(imageBytes.get(), minScore);
        setTargetField(ingestDocument, labels);
        removeSourceField(ingestDocument);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory extends CommonFactory{
        private final DetectTextService detectTextService;

        public Factory(DetectTextService detectTextService) {
            this.detectTextService = detectTextService;
        }

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, Map<String,
                Object> config) {
            readCommonProperties(TYPE, tag, config, "_text");
            return new DetectTextProcessor(tag, source, target,  minScore, maxValues, ignoreMissing,
                    remove, detectTextService);
        }
    }
}
