package com.brusic.aws.rekognition.model;

import java.util.Objects;

public class Celebrity {

    private final String id;
    private final String name;

    public Celebrity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Celebrity celebrity = (Celebrity) o;
        return Objects.equals(id, celebrity.id) &&
                Objects.equals(name, celebrity.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Celebrity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
