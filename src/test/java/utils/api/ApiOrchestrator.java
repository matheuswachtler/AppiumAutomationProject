package utils.api;

import java.util.Map;

public class ApiOrchestrator {

    public void callApi(String apiName, Map<String, String> apiData) {
        System.out.println("2025-08-25 12:44:00 | [API ORCHESTRATOR] Chamando a API: " + apiName);

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
                System.err.println("2025-08-25 12:44:00 | [API ORCHESTRATOR] Nenhuma acao de API corresponde a: " + apiName);
                break;
        }
    }

    private void apiZ(Map<String, String> data) {
        System.out.println("2025-08-25 12:44:00 | [API ORCHESTRATOR] Executando logica para a API Z.");
    }

    private void apiX(Map<String, String> data) {
        System.out.println("2025-08-25 12:44:00 | [API ORCHESTRATOR] Executando logica para a API X.");
    }

    private void apiY(Map<String, String> data) {
        System.out.println("2025-08-25 12:44:00 | [API ORCHESTRATOR] Executando logica para a API Y");
    }
}