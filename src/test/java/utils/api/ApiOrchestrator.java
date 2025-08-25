package utils.api;

import java.util.Map;

public class ApiOrchestrator {

    public void callApi(String apiName, Map<String, String> apiData) {
        System.out.println("[API ORCHESTRATOR] Chamando a API: " + apiName);

        switch (apiName.toLowerCase()) {
            case "apiz":
                apiZ(apiData);
                break;
            case "apix":
                apiX(apiData);
                break;
            case "apiy":
                apiY(apiData);
                break;
            default:
                System.err.println("[API ORCHESTRATOR] Nenhuma acao de API corresponde a: " + apiName);
                break;
        }
    }

    private void apiZ(Map<String, String> data) {
        System.out.println("[API ORCHESTRATOR] Executando logica para a API Z.");
        // Implementar a logica da chamada da API Rest-Assured aqui.
    }

    private void apiX(Map<String, String> data) {
        System.out.println("[API ORCHESTRATOR] Executando logica para a API X.");
        // Implementar a logica da chamada da API Rest-Assured aqui.
    }

    private void apiY(Map<String, String> data) {
        System.out.println("[API ORCHESTRATOR] Executando logica para a API Y.");
        // Implementar a logica da chamada da API Rest-Assured aqui.
    }
}