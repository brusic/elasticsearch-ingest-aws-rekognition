package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.elasticsearch.ingest.rekognition.service.DetectUnsafeContentService;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * Unit tests for {@link DetectUnsafeContentProcessor}
 */
public class DetectUnsafeContentTests extends LuceneTestCase {

    public void testExecute() throws Exception {
        DetectUnsafeContentService mockService = TestUtils.createMockService();
        Mockito.when(mockService.detectUnsafeContent(any(), eq(0f)))
                .thenReturn(Arrays.asList("FOO", "BAR"));

        DetectUnsafeContentProcessor processor = new DetectUnsafeContentProcessor("tag", "source",
                "target", 0f, 0, false, true, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);

        List<?> unsafeContent = ingestDocument.getFieldValue("target", List.class);

        assertEquals(2, unsafeContent.size());
        assertEquals("FOO", unsafeContent.get(0));
        assertEquals("BAR", unsafeContent.get(1));
    }

    public void testMaxOne() throws Exception {
        DetectUnsafeContentService mockService = TestUtils.createMockService();
        Mockito.when(mockService.detectUnsafeContent(any(), eq(0f)))
                .thenReturn(Arrays.asList("FOO", "BAR"));

        DetectUnsafeContentProcessor processor = new DetectUnsafeContentProcessor("tag", "source",
                "target", 0f, 1, false, true, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);

        List<?> unsafeContent = ingestDocument.getFieldValue("target", List.class);

        assertEquals(1, unsafeContent.size());
        assertEquals("FOO", unsafeContent.get(0));
    }

    public void testGetType() {
        DetectObjectsProcessor processor = new DetectObjectsProcessor("tag", "source",
                "target", 0f, 0, false, true, null);
        assertEquals(DetectObjectsProcessor.TYPE, processor.getType());
    }
}