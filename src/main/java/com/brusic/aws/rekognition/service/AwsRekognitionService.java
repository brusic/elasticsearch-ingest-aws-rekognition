package com.brusic.aws.rekognition.service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsResult;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.ModerationLabel;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesRequest;
import com.amazonaws.services.rekognition.model.RecognizeCelebritiesResult;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.brusic.aws.rekognition.model.Celebrity;
import com.brusic.aws.rekognition.model.DetectCelebritiesResult;
import com.brusic.elasticsearch.ingest.rekognition.service.DetectCelebritiesService;
import com.brusic.elasticsearch.ingest.rekognition.service.DetectObjectsService;
import com.brusic.elasticsearch.ingest.rekognition.service.DetectTextService;
import com.brusic.elasticsearch.ingest.rekognition.service.DetectUnsafeContentService;
import org.elasticsearch.SpecialPermission;

import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AwsRekognitionService implements DetectObjectsService, DetectCelebritiesService,
        DetectTextService, DetectUnsafeContentService {

    private final AmazonRekognition rekognitionClient;

    public static AwsRekognitionService create(AWSCredentials credentials, String region) {
        return new AwsRekognitionService(credentials, region);
    }

    private AwsRekognitionService(AWSCredentials credentials, String region) {

        // creating an AmazonRekognition client requires to be run inside
        // a privileged code block
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            // unprivileged code such as scripts do not have SpecialPermission
            sm.checkPermission(new SpecialPermission());
        }

        rekognitionClient = AccessController.doPrivileged(
                (PrivilegedAction<AmazonRekognition>) () ->
                        AmazonRekognitionClientBuilder
                                .standard()
                                .withRegion(region)
                                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                                .withClientConfiguration(new ClientConfiguration().withReaper(false))
                                .build()
        );
    }


    // visible for testing
    // TODO remove AmazonRekognition creation from main constructor and simply use this constructor
    static AwsRekognitionService create(AmazonRekognition rekognitionClient) {
        return new AwsRekognitionService(rekognitionClient);
    }

    private AwsRekognitionService(AmazonRekognition rekognitionClient) {
        this.rekognitionClient = rekognitionClient;
    }

    /**
     * Returns a list of labels found in the image
     * @param imageBytes the image as bytes
     * @param maxResults the maximum number of labels to return - 0 indicates all
     * @param minConfidence - the minimum confidence score for a label - 0 indicates all
     * @return a list of labels
     */
    @Override
    public List<String> detectObjects(ByteBuffer imageBytes, int maxResults, float minConfidence) {

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(imageBytes));

        if (maxResults > 0) {
            request.withMaxLabels(maxResults);
        }

        if (minConfidence > 0) {
            request.withMinConfidence(minConfidence);
        }

        DetectLabelsResult result = rekognitionClient.detectLabels(request);
        List<Label> labels = result.getLabels();

        if (!labels.isEmpty()) {
            return labels.stream().map(Label::getName).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public DetectCelebritiesResult detectCelebrities(ByteBuffer imageBytes, float minConfidence) {
        RecognizeCelebritiesRequest request = new RecognizeCelebritiesRequest()
                .withImage(new Image().withBytes(imageBytes));

        RecognizeCelebritiesResult result = rekognitionClient.recognizeCelebrities(request);
        List<com.amazonaws.services.rekognition.model.Celebrity> celebrityFaces = result.getCelebrityFaces();

        if (!celebrityFaces.isEmpty()) {
            Stream<com.amazonaws.services.rekognition.model.Celebrity> stream = celebrityFaces.stream();

            if (minConfidence > 0) {
                stream = stream.filter(celebrity -> celebrity.getMatchConfidence() > minConfidence);
            }

            List<Celebrity> celebrities = stream
                    .map(celebrity -> new Celebrity(celebrity.getId(), celebrity.getName()))
                    .collect(Collectors.toList());

            return new DetectCelebritiesResult(celebrities, result.getUnrecognizedFaces().size());
        }

        return DetectCelebritiesResult.emptyResult();
    }

    @Override
    public List<String> detectUnsafeContent(ByteBuffer imageBytes, float minConfidence) {
        DetectModerationLabelsRequest request = new DetectModerationLabelsRequest()
                .withImage(new Image().withBytes(imageBytes));

        if (minConfidence > 0) {
            request.withMinConfidence(minConfidence);
        }

        DetectModerationLabelsResult result = rekognitionClient.detectModerationLabels(request);
        List<ModerationLabel> labels = result.getModerationLabels();

        if (!labels.isEmpty()) {
            return labels.stream().map(ModerationLabel::getName).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> detectText(ByteBuffer imageBytes, float minConfidence) {
        DetectTextRequest request =  new DetectTextRequest()
                .withImage(new Image()
                        .withBytes(imageBytes));
        DetectTextResult result = rekognitionClient.detectText(request);
        List<TextDetection> textDetections = result.getTextDetections();

        if (!textDetections.isEmpty()) {
            Stream<TextDetection> stream = textDetections.stream();

            if (minConfidence > 0) {
                stream = stream.filter(textDetection -> textDetection.getConfidence() > minConfidence);
            }

            return stream.map(TextDetection::getDetectedText).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
