package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.AccountCreationPostmanRunner;
import org.gmarques.util.ParameterBuilder;

@Log4j2
public class GerarConta extends FunctionBase {

    @Override
    public String name() {
        return "gerar_conta";
    }

    @Override
    public String description() {
        return "Gera uma conta corrente do AGI e copia o CPF para a área de transferência do Windows.";
    }

    @Override
    public Map<String, Object> parameters() {
        return new ParameterBuilder().build();
    }

    @Override
    protected void execute(JsonNode functionArgs) throws Exception {
        log.info("Executing function {}", name());
        AccountCreationPostmanRunner.createAccount();
    }
}
