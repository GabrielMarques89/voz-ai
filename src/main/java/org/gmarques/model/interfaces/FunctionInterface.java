package org.gmarques.model.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.gmarques.model.openai.objects.Tool;

public interface FunctionInterface {
  Tool getTool();
  String name();
  String description();
  Map<String, Object> parameters();
  void run(JsonNode functionArgs);
}
