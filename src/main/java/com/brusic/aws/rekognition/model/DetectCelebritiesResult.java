package com.brusic.aws.rekognition.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DetectCelebritiesResult {

    private final List<Celebrity> celebrities;
    private final int unknownFaces;

    private static final DetectCelebritiesResult EMPTY =
            new DetectCelebritiesResult(Collections.emptyList(), 0);

    public static DetectCelebritiesResult emptyResult() {
        return EMPTY;
    }

    public DetectCelebritiesResult(List<Celebrity> celebrities, int unknownFaces) {
        this.celebrities = celebrities;
        this.unknownFaces = unknownFaces;
    }

    public List<Celebrity> getCelebrities() {
        return celebrities;
    }

    public int numUnknownFaces() {
        return unknownFaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetectCelebritiesResult that = (DetectCelebritiesResult) o;
        return unknownFaces == that.unknownFaces &&
                Objects.equals(celebrities, that.celebrities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(celebrities, unknownFaces);
    }

    @Override
    public String toString() {
        return "DetectCelebritiesResult{" +
                "celebrities=" + celebrities +
                ", unknownFaces=" + unknownFaces +
                '}';
    }
}
