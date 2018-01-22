package com.brusic.elasticsearch.ingest.rekognition.processor;

import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;
import org.elasticsearch.test.rest.yaml.ClientYamlTestCandidate;
import org.elasticsearch.test.rest.yaml.ClientYamlTestExecutionContext;
import org.elasticsearch.test.rest.yaml.ESClientYamlSuiteTestCase;

public class AwsRekognitionClientYamlTestSuiteIT extends ESClientYamlSuiteTestCase {

    public AwsRekognitionClientYamlTestSuiteIT(@Name("yaml") ClientYamlTestCandidate testCandidate) {
        super(testCandidate);
    }

    @ParametersFactory
    public static Iterable<Object[]> parameters() throws Exception {
        return ESClientYamlSuiteTestCase.createParameters();
    }

    @Override
    protected ClientYamlTestExecutionContext getAdminExecutionContext() {
        return super.getAdminExecutionContext();
    }
}
