package utils.api;

import java.util.Map;

public class ApiOrchestrator {

    public void callApi(String apiName, Map<String, String> apiData) {
        System.out.println("[API ORCHESTRATOR] Calling the API: " + apiName);

        switch (apiName.toLowerCase()) {
            case "apiz":
                apiZ(apiData);
                break;
            case "login_api":
                login_api(apiData);
                break;
            case "apiy":
                apiY(apiData);
                break;
            default:
                System.err.println("[API ORCHESTRATOR] No API action matches: " + apiName);
                break;
        }
    }

    private void apiZ(Map<String, String> data) {
        System.out.println("[API ORCHESTRATOR] Executing logic for the login API Z.");
    }

    private void login_api(Map<String, String> data) {
        System.out.println("[API ORCHESTRATOR] Executing logic for the login API.");
        LoginApi loginApi = new LoginApi();
        loginApi.requestLoginApi();
    }

    private void apiY(Map<String, String> data) {
        System.out.println("[API ORCHESTRATOR] Executing logic for the login API Y.");
    }
}