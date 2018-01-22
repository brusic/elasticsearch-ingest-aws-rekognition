package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.elasticsearch.ingest.rekognition.service.DetectTextService;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * Unit tests for {@link DetectTextProcessor}
 */
public class DetectTextProcessorTests extends LuceneTestCase {

    public void testExecute() throws Exception {
        DetectTextService mockService = TestUtils.createMockService();
        Mockito.when(mockService.detectText(any(), eq(0f)))
                .thenReturn(Arrays.asList("FOO", "BAR"));

        DetectTextProcessor processor = new DetectTextProcessor("tag", "source",
                "target", 0f, 0, false, true, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);

        List<?> text = ingestDocument.getFieldValue("target", List.class);

        assertEquals(2, text.size());
        assertEquals("FOO", text.get(0));
        assertEquals("BAR", text.get(1));
    }

    public void testMaxOne() throws Exception {
        DetectTextService mockService = TestUtils.createMockService();
        Mockito.when(mockService.detectText(any(), eq(0f)))
                .thenReturn(Arrays.asList("FOO", "BAR"));

        DetectTextProcessor processor = new DetectTextProcessor("tag", "source",
                "target", 0f, 1, false, true, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);

        List<?> text = ingestDocument.getFieldValue("target", List.class);

        assertEquals(1, text.size());
        assertEquals("FOO", text.get(0));
    }

    public void testGetType() {
        DetectObjectsProcessor processor = new DetectObjectsProcessor("tag", "source",
                "target", 0f, 0, false, true, null);
        assertEquals(DetectObjectsProcessor.TYPE, processor.getType());
    }
}