package org.gmarques.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParameterBuilder {
    private final Map<String, Object> root;
    private final Map<String, Object> properties;
    private final List<String> required;

    public ParameterBuilder() {
        this.root = new HashMap<>();
        this.properties = new LinkedHashMap<>(); // Preserve insertion order
        this.required = new ArrayList<>();
        root.put("type", "object");
        root.put("properties", properties);
        root.put("required", required);
    }

    public ParameterBuilder addParameter(String name, String type, String description) {
        return addParameter(name, type, description, null);
    }

    public ParameterBuilder addParameter(String name, String type, String description, Map<String, Object> additionalProperties) {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("type", type);
        parameter.put("description", description);
        if (additionalProperties != null) {
            parameter.putAll(additionalProperties);
        }
        properties.put(name, parameter);
        return this;
    }

    public ParameterBuilder addRequired(String name) {
        required.add(name);
        return this;
    }

    public ParameterBuilder addEnumParameter(String name, String type, String description, List<String> enumValues) {
        properties.put(name, Map.of(
            "type", type,
            "description", description,
            "enum", enumValues
        ));
        return this;
    }

    public Map<String, Object> build() {
        return root;
    }
}
