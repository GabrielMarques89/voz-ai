package org.gmarques.model.openai.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import org.gmarques.model.openai.interfaces.FunctionInterface;

public class FunctionFactory {
    public static final Map<String, FunctionInterface> functionMap = new HashMap<>();

    static {
//        var abrirAplicativo = new AbrirAplicativo();
        var criarArquivoCodigo = new CriarArquivoCodigo();
        var encontraArquivoParaAlteracao = new EncontrarEstruturaProjeto();
        var googleSearch = new GoogleSearch();
        var pesquisarLocal = new PesquisarLocal();
        var controlarMedia = new ControlarMedia();
        var digitarTexto = new DigitarTexto();
        var alterarJanela = new AlterarJanela();

        //functionMap.put(abrirAplicativo.name(), abrirAplicativo);
        functionMap.put(googleSearch.name(), new GoogleSearch());
        functionMap.put(criarArquivoCodigo.name(),criarArquivoCodigo);
        functionMap.put(encontraArquivoParaAlteracao.name(),encontraArquivoParaAlteracao);
        functionMap.put(pesquisarLocal.name(), new PesquisarLocal());
        functionMap.put(controlarMedia.name(), new ControlarMedia());
        functionMap.put(alterarJanela.name(), new AlterarJanela());
        functionMap.put(digitarTexto.name(), new DigitarTexto());

//        functionMap.put(AlterarArquivoCodigo.name(), new AlterarArquivoCodigo());
    }

    private static FunctionInterface getFunction(String functionName) {
        return functionMap.get(functionName);
    }

    public static void run(String functionName, JsonNode args){
        var function = FunctionFactory.getFunction(functionName);
        if(function != null) {
            function.run(args);
        }else{
            System.out.println("Function called not found: " + functionName + ". Nothing executed.");
        }
    }
}