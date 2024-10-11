package org.gmarques.model.openai.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gmarques.model.openai.objects.Tool;

public interface FunctionInterface {
  String name();
  Tool getTool();
  void run(JsonNode functionArgs);
  void execute(String... parameters);
  ObjectMapper mapper();
}
