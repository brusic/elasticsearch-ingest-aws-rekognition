package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.aws.rekognition.model.Celebrity;
import com.brusic.aws.rekognition.model.DetectCelebritiesResult;
import com.brusic.elasticsearch.ingest.rekognition.service.DetectCelebritiesService;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.ingest.IngestDocument;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * Unit tests for {@link DetectCelebritiesProcessor}
 */
public class DetectCelebritiesProcessorTests extends LuceneTestCase {

    public void testExecute() throws Exception {
        DetectCelebritiesService mockService = createDetectCelebritiesService();

        DetectCelebritiesProcessor processor = new DetectCelebritiesProcessor("tag", "source",
                "target", 0f, 0, false, true, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);

        Map<?, ?> celebrityResult = ingestDocument.getFieldValue("target", Map.class);

        Object celebrities = celebrityResult.get("celebrityFaces");
        assertThat(celebrities, instanceOf(List.class));
        assertEquals(2, ((List)celebrities).size());
        assertEquals("myname1", ((Map)((List)celebrities).get(0)).get("name"));

        Object unknownFaces = celebrityResult.get("unknownFaces");
        assertThat(unknownFaces, instanceOf(Integer.class));
        assertEquals(2, unknownFaces);
    }

    public void testMaxOne() throws Exception {
        // maxValues not support by the DetectCelebritiesProcessor

        DetectCelebritiesService mockService = createDetectCelebritiesService();

        DetectCelebritiesProcessor processor = new DetectCelebritiesProcessor("tag", "source",
                "target", 0f, 1, false, true, mockService);
        IngestDocument ingestDocument = TestUtils.createTestDocument();
        processor.execute(ingestDocument);

        Map<?, ?> celebrityResult = ingestDocument.getFieldValue("target", Map.class);

        Object celebrities = celebrityResult.get("celebrityFaces");
        assertThat(celebrities, instanceOf(List.class));
        assertEquals(2, ((List)celebrities).size());
    }

    public void testGetType() {
        DetectCelebritiesProcessor processor = new DetectCelebritiesProcessor("tag", "source",
                "target", 0f, 0, false, true, null);
        assertEquals(DetectCelebritiesProcessor.TYPE, processor.getType());
    }

    private DetectCelebritiesService createDetectCelebritiesService() {
        DetectCelebritiesResult celebritiesResult =
                new DetectCelebritiesResult(
                        Arrays.asList(
                            new Celebrity("myid1", "myname1"),
                            new Celebrity("myid2", "myname2")),
                        2);

        DetectCelebritiesService mockService = TestUtils.createMockService();
        Mockito.when(mockService.detectCelebrities(any(), eq(0f)))
                .thenReturn(celebritiesResult);
        return mockService;
    }
}