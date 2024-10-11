package org.gmarques.model.openai.objects;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class Tool {

  private String type;
  private String name;
  private String description;
  private Map<String, Object> parameters;
}