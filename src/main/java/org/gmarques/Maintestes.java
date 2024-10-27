package org.gmarques;

import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.gmarques.util.ParameterBuilder;

public class Maintestes {


  @SneakyThrows
  public static void main(String[] args) {

    var params1 = Map.of(
        "type", "object",
        "properties", Map.of(
            "nome_projeto", Map.of(
                "type", "string",
                "description", "O nome do projeto onde os arquivos serão criados."
            ),
            "descricoes", Map.of(
                "type", "array",
                "items", Map.of(
                    "type", "string",
                    "description", "A descrição de um arquivo de código a ser criado."
                ),
                "description", "Uma lista de descrições dos arquivos de código a serem criados."
            )
        ),
        "required", List.of("nome_projeto", "descricoes")
    );

    Map<String, Object> itemsSchema = Map.of(
        "type", "string",
        "description", "A descrição de um arquivo de código a ser criado."
    );

    var parames2 = new ParameterBuilder()
          .addParameter(
              "nome_projeto",
              "string",
              "O nome do projeto onde os arquivos serão criados."
          )
          .addParameter(
              "descricoes",
              "array",
              "Uma lista de descrições dos arquivos de código a serem criados.",
              Map.of("items", itemsSchema)
          )
          .addRequired("nome_projeto")
          .addRequired("descricoes")
          .build();

    var something = "";
  }


}
