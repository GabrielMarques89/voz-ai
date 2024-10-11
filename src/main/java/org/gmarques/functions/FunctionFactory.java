package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.gmarques.model.openai.interfaces.FunctionInterface;
import org.reflections.Reflections;

public class FunctionFactory {
    public static final Map<String, FunctionInterface> functionMap = new HashMap<>();

    static {
        Reflections reflections = new Reflections("org.gmarques.functions");

        Set<Class<? extends FunctionBase>> classes = reflections.getSubTypesOf(FunctionBase.class);

        for (Class<? extends FunctionBase> clazz : classes) {
            try {
                FunctionBase function = clazz.getDeclaredConstructor().newInstance();
                functionMap.put(function.name(), function);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static FunctionInterface getFunction(String functionName) {
        return functionMap.get(functionName);
    }

    public static void run(String functionName, JsonNode args) {
        var function = FunctionFactory.getFunction(functionName);
        if (function != null) {
            function.run(args);
        } else {
            System.out.println("Function called not found: " + functionName + ". Nothing executed.");
        }
    }
}
