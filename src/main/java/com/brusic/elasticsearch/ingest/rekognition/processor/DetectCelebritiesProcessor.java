package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.aws.rekognition.model.Celebrity;
import com.brusic.aws.rekognition.model.DetectCelebritiesResult;
import com.brusic.elasticsearch.ingest.rekognition.service.DetectCelebritiesService;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DetectCelebritiesProcessor extends AbstractAwsRekognitionProcessor {

    public static final String TYPE = "detect-celebrities";
    private final DetectCelebritiesService detectCelebritiesService;

    DetectCelebritiesProcessor(String tag,
                               String sourceField,
                               String targetField,
                               Float minScore,
                               Integer maxValues,
                               boolean ignoreMissing,
                               boolean remove, DetectCelebritiesService detectCelebritiesService) {
        super(tag, sourceField, targetField, minScore, maxValues, ignoreMissing, remove);
        this.detectCelebritiesService = detectCelebritiesService;
    }

    @Override
    public void execute(IngestDocument ingestDocument) {
        Optional<ByteBuffer> imageBytes = getImageAndValidate(ingestDocument);
        if (!imageBytes.isPresent()) {
            return;
        }

        DetectCelebritiesResult celebritiesResult = detectCelebritiesService.detectCelebrities(imageBytes.get(), minScore);


        Stream<Celebrity> entityStream = celebritiesResult.getCelebrities().stream();

        List<Map<String, Object>> celebrities = entityStream
                .map(entity -> {
                    HashMap<String, Object> entityMap = new HashMap<>();
                    entityMap.put("id", entity.getId());
                    entityMap.put("name", entity.getName());
                    return entityMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("celebrityFaces", celebrities);
        result.put("unknownFaces", celebritiesResult.numUnknownFaces());

        setTargetField(ingestDocument, result);
        removeSourceField(ingestDocument);
    }


    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory extends CommonFactory {
        private final DetectCelebritiesService detectCelebritiesService;

        public Factory(DetectCelebritiesService detectCelebritiesService) {
            this.detectCelebritiesService = detectCelebritiesService;
        }

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, Map<String, Object> config) {
            readCommonProperties(TYPE, tag, config, "_celebrities");
            return new DetectCelebritiesProcessor(tag, source, target,  minScore, maxValues, ignoreMissing,
                    remove, detectCelebritiesService);
        }
    }
}
