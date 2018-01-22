package com.brusic.aws.rekognition.service;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.Celebrity;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsResult;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.ModerationLabel;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesResult;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.util.IOUtils;
import com.brusic.aws.rekognition.model.DetectCelebritiesResult;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static org.mockito.Matchers.any;

public class AwsRekognitionServiceTests extends LuceneTestCase {

    private static ByteBuffer imageBytes;
    private static AwsRekognitionService rekognitionService;

    @BeforeClass
    public static void createImage() throws Exception {
        // image bytes irrelevant in mock
        String photo = "/images/binary/image.jpg";
        imageBytes = getByteBuffer(photo);

        rekognitionService = AwsRekognitionService.create(createMockClient());
    }

    public void testDetectLabels() {
        List<String> labels = rekognitionService.detectObjects(imageBytes, 0, 0);
        assertEquals(2, labels.size());
        assertEquals("labelA", labels.get(0));
        assertEquals("labelB", labels.get(1));
    }

    public void testDetectLabelsMinConfidence() {
        List<String> labels = rekognitionService.detectObjects(imageBytes, 0, 0.6f);
        assertEquals(2, labels.size());
        assertEquals("labelA", labels.get(0));
        assertEquals("labelB", labels.get(1));
    }

    public void testDetectCelebrities() {
        DetectCelebritiesResult celebritiesResult = rekognitionService.detectCelebrities(imageBytes, 0f);

        assertEquals(2, celebritiesResult.getCelebrities().size());
        assertEquals(1, celebritiesResult.numUnknownFaces());
    }

    public void testDetectModerationLabels() throws Exception {
        String photo = "/images/binary/unsafe.jpg";
        ByteBuffer imageBytes = getByteBuffer(photo);

        List<String> labels = rekognitionService.detectUnsafeContent(imageBytes, 0f);
        assertEquals(2, labels.size());
    }

    public void testDetectText() {
        List<String> text = rekognitionService.detectText(imageBytes, 0f);
        assertEquals(2, text.size());
        assertEquals("foo", text.get(0));
        assertEquals("bar", text.get(1));
    }

    private static ByteBuffer getByteBuffer(String photo) throws IOException {
        InputStream resourceStream  =
                AwsRekognitionServiceTests.class.getResourceAsStream(photo);

        return ByteBuffer.wrap(IOUtils.toByteArray(resourceStream));
    }

    private static AmazonRekognition createMockClient() {
        AmazonRekognition mockClient = Mockito.mock(AmazonRekognition.class);

        DetectLabelsResult detectLabelsResult = new DetectLabelsResult()
                .withLabels(new Label().withName("labelA").withConfidence(0.6f),
                        new Label().withName("labelB").withConfidence(0.6f));

        Mockito.when(mockClient.detectLabels(any())).thenReturn(detectLabelsResult);

        RecognizeCelebritiesResult celebritiesResult = new RecognizeCelebritiesResult()
                .withCelebrityFaces(
                        new Celebrity().withId("idA").withName("nameA").withMatchConfidence(0.5f),
                        new Celebrity().withId("idB").withName("nameB").withMatchConfidence(0.9f))
                .withUnrecognizedFaces(new ComparedFace());

        Mockito.when(mockClient.recognizeCelebrities(any())).thenReturn(celebritiesResult);

        DetectTextResult textResult = new DetectTextResult().withTextDetections(
                new TextDetection().withDetectedText("foo").withConfidence(0.8f),
                new TextDetection().withDetectedText("bar").withConfidence(0.99f));
        Mockito.when(mockClient.detectText(any())).thenReturn(textResult);

        DetectModerationLabelsResult unsafeContentResult = new DetectModerationLabelsResult().withModerationLabels(
                new ModerationLabel().withName("nudity").withConfidence(0.7f),
                new ModerationLabel().withName("language").withConfidence(0.7f));
        Mockito.when(mockClient.detectModerationLabels(any())).thenReturn(unsafeContentResult);

        return mockClient;
    }
}