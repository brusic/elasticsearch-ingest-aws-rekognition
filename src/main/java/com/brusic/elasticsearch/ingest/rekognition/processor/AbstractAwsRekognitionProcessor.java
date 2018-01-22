package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.elasticsearch.ingest.rekognition.RekognitionParameters;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.ConfigurationUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.elasticsearch.ingest.ConfigurationUtils.readIntProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

abstract class AbstractAwsRekognitionProcessor extends AbstractProcessor {

    static final int ALL_VALUES = 0;
    static final float DEFAULT_MIN_SCORE = 0f;

    // common parameters
    private final String sourceField;
    private final String targetField;
    private final boolean ignoreMissing;
    private final boolean remove;

    final Integer maxValues;
    final Float minScore;

    AbstractAwsRekognitionProcessor(String tag, String sourceField, String targetField,
                                    Float minScore, Integer maxValues,
                                    boolean ignoreMissing, boolean remove) {
        super(tag);

        this.sourceField = sourceField;
        this.targetField = targetField;
        this.minScore = minScore;
        this.maxValues = maxValues;
        this.ignoreMissing = ignoreMissing;
        this.remove = remove;
    }

    /**
     * retrieve the value of the source field from the document
     * if the field is not found or null:
     *     return null if ignoreMissing is true
     *     throw Exception is ignoreMissing is false
     *
     * @param ingestDocument the document source contains the field
     * @return the source field text
     */
    Optional<ByteBuffer> getImageAndValidate(IngestDocument ingestDocument) {
        String base64 = ingestDocument.getFieldValue(sourceField, String.class, true);

        if (base64 == null && ignoreMissing) {
            return Optional.empty();
        } else if (base64 == null) {
            throw new IllegalArgumentException("field [" + sourceField + "] is null, cannot parse text.");
        }

        byte[] bytes = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        return Optional.of(byteBuffer);
    }

    void setTargetField(IngestDocument ingestDocument, List<?> list) {
        list = trim(list);
        ingestDocument.setFieldValue(targetField, list);
    }

    void setTargetField(IngestDocument ingestDocument, Map<?, ?> map) {
        ingestDocument.setFieldValue(targetField, map);
    }

    void removeSourceField(IngestDocument ingestDocument) {
        if (remove) {
            ingestDocument.removeField(sourceField);
        }
    }

    private <T> List<T> trim(List<T> list) {
        if (maxValues != ALL_VALUES && list.size() > maxValues) {
            list = list.subList(0, maxValues);
        }
        return list;
    }

    /**
     * Package private for tests
     *
     * Returns and removes the specified property from the specified configuration map.
     *
     * If the property value isn't of type float a {@link ElasticsearchParseException} is thrown.
     * If the property is missing an {@link ElasticsearchParseException} is thrown
     */
    static Float readFloatProperty(String processorType, String processorTag, Map<String, Object>
            configuration, String propertyName, Float defaultValue) {
        Object value = configuration.remove(propertyName);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value.toString());
        } catch (Exception e) {
            throw ConfigurationUtils.newConfigurationException(processorType, processorTag, propertyName,
                    "property cannot be converted to an int [" + value.toString() + "]");
        }
    }

    abstract static class CommonFactory implements Processor.Factory {

        String source;
        String target;
        Float minScore;
        Integer maxValues;
        boolean ignoreMissing;
        boolean remove;

        void readCommonProperties(String type, String tag, Map<String, Object> config, String targetSuffix) {
            source = readStringProperty(type, tag, config, RekognitionParameters.FIELD.getName());
            target = readStringProperty(type, tag, config, RekognitionParameters.TARGET.getName(), source + targetSuffix);
            maxValues = readIntProperty(type, tag, config, RekognitionParameters.MAX_VALUES.getName(), ALL_VALUES);
            minScore = readFloatProperty(type, tag, config, RekognitionParameters.MIN_SCORE.getName(), DEFAULT_MIN_SCORE);
            ignoreMissing = ConfigurationUtils.readBooleanProperty(type, tag, config,
                    RekognitionParameters.IGNORE_MISSING.getName(), false);
            remove = ConfigurationUtils.readBooleanProperty(type, tag, config,
                    RekognitionParameters.REMOVE.getName(), true);

        }
    }

}
