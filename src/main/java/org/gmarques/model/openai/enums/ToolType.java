package org.gmarques.model.openai.enums;

public enum ToolType {
    string("string"),
    array("array"),
    object("object"),
    function("function");

    private final String value;

    ToolType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}