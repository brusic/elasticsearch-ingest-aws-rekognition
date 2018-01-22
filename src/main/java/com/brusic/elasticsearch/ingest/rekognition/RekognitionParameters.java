package com.brusic.elasticsearch.ingest.rekognition;

public enum RekognitionParameters {

    FIELD("field"),
    TARGET("target_field"),
    MIN_SCORE("min_score"),
    MAX_VALUES("max_values"),
    IGNORE_MISSING("ignore_missing"),
    REMOVE("remove");

    private final String name;

    RekognitionParameters(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
