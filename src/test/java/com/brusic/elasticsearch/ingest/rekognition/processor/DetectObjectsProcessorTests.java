package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.elasticsearch.ingest.rekognition.service.DetectObjectsService;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * Unit tests for {@link DetectObjectsProcessor}
 */
public class DetectObjectsProcessorTests extends LuceneTestCase {

    public void testExecute() throws Exception {
        DetectObjectsService mockService = TestUtils.createMockService();
        Mockito.when(mockService.detectObjects(any(), eq(1), eq(0f)))
                .thenReturn(Collections.singletonList("FOO"));
        Mockito.when(mockService.detectObjects(any(), not(eq(1)), eq(0f)))
                .thenReturn(Arrays.asList("FOO", "BAR"));

        DetectObjectsProcessor processor = new DetectObjectsProcessor("tag", "source",
                "target", 0f, 0, false, true, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);

        List<?> labels = ingestDocument.getFieldValue("target", List.class);

        assertEquals(2, labels.size());
        assertEquals("FOO", labels.get(0));
        assertEquals("BAR", labels.get(1));
    }

    public void testMaxOne() throws Exception {
        DetectObjectsService mockService = TestUtils.createMockService();
        Mockito.when(mockService.detectObjects(any(), eq(1), eq(0f)))
                .thenReturn(Collections.singletonList("FOO"));
        Mockito.when(mockService.detectObjects(any(), not(eq(1)), eq(0f)))
                .thenReturn(Arrays.asList("FOO", "BAR"));

        DetectObjectsProcessor processor = new DetectObjectsProcessor("tag", "source",
                "target", 0f, 1, false, true, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);

        List<?> labels = ingestDocument.getFieldValue("target", List.class);

        assertEquals(1, labels.size());
        assertEquals("FOO", labels.get(0));
    }

    public void testGetType() {
        DetectObjectsProcessor processor = new DetectObjectsProcessor("tag", "source",
                "target", 0f, 0, false, true, null);
        assertEquals(DetectObjectsProcessor.TYPE, processor.getType());
    }
}