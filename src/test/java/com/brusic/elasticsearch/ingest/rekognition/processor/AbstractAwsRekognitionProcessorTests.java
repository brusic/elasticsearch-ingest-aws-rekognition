package com.brusic.elasticsearch.ingest.rekognition.processor;

import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;

public class AbstractAwsRekognitionProcessorTests extends LuceneTestCase {

    public void testRemoveSourceField() throws Exception {
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        String sourceField = "source";
        AbstractAwsRekognitionProcessor processor = new AbstractAwsRekognitionProcessor("tag", sourceField,
                "target", 0f, 0, false, true) {
            @Override
            public void execute(IngestDocument ingestDocument) {
                removeSourceField(ingestDocument);
            }

            @Override
            public String getType() {
                return "";
            }
        };

        Object fieldValue = ingestDocument.getFieldValue(sourceField, Object.class, true);
        assertNotNull("source field should be present", fieldValue);

        processor.execute(ingestDocument);
        fieldValue = ingestDocument.getFieldValue(sourceField, Object.class, true);
        assertNull("source field should have been removed", fieldValue);
    }

    public void testDoNotRemoveSourceField() throws Exception {
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        String sourceField = "source";
        AbstractAwsRekognitionProcessor processor = new AbstractAwsRekognitionProcessor("tag", sourceField,
                "target", 0f, 0, false, false) {
            @Override
            public void execute(IngestDocument ingestDocument) {
                removeSourceField(ingestDocument);
            }

            @Override
            public String getType() {
                return "";
            }
        };

        Object fieldValue = ingestDocument.getFieldValue(sourceField, Object.class, true);
        assertNotNull("source field should be present", fieldValue);

        processor.execute(ingestDocument);
        fieldValue = ingestDocument.getFieldValue(sourceField, Object.class, true);
        assertNotNull("source field should be present", fieldValue);
    }
}