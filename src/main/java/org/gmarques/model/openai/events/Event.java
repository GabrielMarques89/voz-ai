package org.gmarques.model.openai.events;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.gmarques.model.openai.objects.Item;

@Getter
@Setter
@Builder
public class Event {

  private String type;
  private Item item;
}