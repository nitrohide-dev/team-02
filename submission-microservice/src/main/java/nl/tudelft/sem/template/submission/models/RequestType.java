package nl.tudelft.sem.template.submission.models;

public enum RequestType {
    REVIEW("review"),
    USER("user"),
    SUBMISSION("submission");

    private String value;

    RequestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
