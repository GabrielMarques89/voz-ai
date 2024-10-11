package org.gmarques.model.openai.objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class InputAudioTranscription {

  private boolean enabled;
  private String model;
}