package org.gmarques.personalizations;

import java.util.HashMap;
import java.util.Map;

public class Websites {
  public static final Map<String, String> siteMap = new HashMap<>();
  static {
    siteMap.put("board", "https://agibank.atlassian.net/jira/software/c/projects/CORE20/boards/371");
    siteMap.put("ticket", "https://agibank.atlassian.net/browse/CORE20-$NUMEROTICKET$");
  }
}
