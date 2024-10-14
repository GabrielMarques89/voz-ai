package org.gmarques.personalizations;

import java.util.HashMap;
import java.util.Map;

public class PostmanCollections {
  public static final Map<String, String> postmanCollections = new HashMap<>();
  static {
    postmanCollections.put("criar conta", "https://agibank.atlassian.net/jira/software/c/projects/CORE20/boards/371");
    postmanCollections.put("ticket", "https://agibank.atlassian.net/browse/CORE20-$NUMEROTICKET$");
  }
}
