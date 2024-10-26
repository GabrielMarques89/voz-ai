package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.gmarques.AccountCreationPostmanRunner;
import org.gmarques.model.openai.objects.Tool;

public class GerarConta extends FunctionBase {
    @Override
    public String name() {
        return "gerar_conta";
    }

    @Override
    public void run(JsonNode functionArgs) {
        System.out.println("Função chamada: " + name());
        execute();
    }

    @Override
    public Tool getTool() {
        return Tool.builder()
                .name(name())
                .type("function")
                .description("Gera uma conta corrente do AGI e o copia o CPF para a área de transferência do Windows.")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                ))
                .build();
    }

    @Override
    public void execute(String... parameters) {
        try {
            AccountCreationPostmanRunner.createAccount();
        } catch (Exception e) {
            handleException(e);
        }
    }
}
