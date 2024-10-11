package org.gmarques.model.openai.objects;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class Session {

  private List<String> modalities;
  private String instructions;
  private String voice;
  private String input_audio_format = "pcm16";
  private String output_audio_format = "pcm16";
  private InputAudioTranscription input_audio_transcription;
  private TurnDetection turn_detection;
  private List<Tool> tools;
  private String tool_choice = "auto";
  private double temperature = 0.9;
  private Integer max_output_tokens;
}