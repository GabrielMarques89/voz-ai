package org.gmarques.model.openai.objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class TurnDetection {

  private String type = "server_vad";
  private double threshold = 0.5;
  private int prefixPaddingMs = 300;
  private int silenceDurationMs = 200;
}