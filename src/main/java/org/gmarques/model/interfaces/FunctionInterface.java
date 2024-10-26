package org.gmarques.model.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import org.gmarques.model.openai.objects.Tool;

public interface FunctionInterface {
  String name();
  Tool getTool();
  void run(JsonNode functionArgs);
  void execute(String... parameters);
}
