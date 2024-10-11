package org.gmarques.model.openai.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageData {

  public String revised_prompt;
  public String url;
}