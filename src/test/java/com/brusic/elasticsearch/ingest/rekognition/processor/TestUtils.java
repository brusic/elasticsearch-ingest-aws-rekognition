package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.brusic.aws.rekognition.service.AwsRekognitionService;
import org.elasticsearch.ingest.IngestDocument;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

public class TestUtils {

    static AwsRekognitionService createMockService() {
        return Mockito.mock(AwsRekognitionService.class);
    }

    static IngestDocument createTestDocument() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("foo", "bar");

        String base64 = getBase64ImageAsString("/images/base64/image.base64txt");
        document.put("source", base64);

        return createTestDocument(document);
    }

    private static IngestDocument createTestDocument(Map<String, Object> document) {
        return new IngestDocument("index", "type", "id", null, null, null,
                null, document);
    }

    private static String getBase64ImageAsString(String fileName) throws IOException {
        InputStream resourceStream = TestUtils.class.getResourceAsStream(fileName);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = resourceStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }
}
