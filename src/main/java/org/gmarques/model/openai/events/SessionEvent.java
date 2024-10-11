package org.gmarques.model.openai.events;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.gmarques.model.openai.objects.Item;
import org.gmarques.model.openai.objects.Session;

@Setter
@Getter
public class SessionEvent extends Event {
  private String eventId;
  private Session session;

  public SessionEvent(String type, Item item, String eventId, Session session) {
    super(type, item);
    this.eventId = eventId;
    this.session = session;
  }

  SessionEvent(String type, Item item) {
    super(type, item);
  }
}