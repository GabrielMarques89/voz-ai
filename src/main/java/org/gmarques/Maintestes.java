package org.gmarques;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;

public class Maintestes {


  @SneakyThrows
  public static void main(String[] args) {
    var phone = "+5531993166054";
    String regex = "^\\+\\d{1,3}(\\d{1,4})?\\d{4,10}$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(phone);

    boolean isValidFormat = matcher.matches();
    var asd = "";
  }
}
