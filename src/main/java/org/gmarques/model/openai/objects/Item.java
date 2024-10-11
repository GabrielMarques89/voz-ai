package org.gmarques.model.openai.objects;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {

      private String type;
      private String role;
      private List<Content> content;
    }