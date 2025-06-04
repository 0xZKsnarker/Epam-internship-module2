package com.epam.domain;

public class TrainingType {
    private final String name;

    public TrainingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainingType)) return false;
        return name.equalsIgnoreCase(((TrainingType) o).name);
    }

    @Override
    public int hashCode() { return name.toLowerCase().hashCode();
    }
}
