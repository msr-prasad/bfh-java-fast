package com.example.bfh;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    // === EDIT THESE THREE WITH YOUR REAL DETAILS ===
    private static final String NAME  = "Mucherla Siva Rama Prasad";
    private static final String REGNO = "PES2UG22CS324";
    private static final String EMAIL = "pes2ug22cs324@pesu.pes.edu";

    // API endpoints
    private static final String GEN_URL    = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String SUBMIT_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    public static void main(String[] args) throws Exception {
        if (REGNO == null || REGNO.isBlank()) {
            throw new IllegalArgumentException("Please set your EVEN registration number in REGNO.");
        }

        ObjectMapper om = new ObjectMapper();
        HttpClient http = HttpClient.newHttpClient();

        // 1) Generate webhook + token
        String genBody = String.format("{\"name\":\"%s\",\"regNo\":\"%s\",\"email\":\"%s\"}",
                escape(NAME), escape(REGNO), escape(EMAIL));

        HttpRequest genReq = HttpRequest.newBuilder()
                .uri(URI.create(GEN_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(genBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> genResp = http.send(genReq, HttpResponse.BodyHandlers.ofString());
        if (genResp.statusCode() / 100 != 2) {
            System.err.println("Failed to generate webhook. Status: " + genResp.statusCode());
            System.err.println(genResp.body());
            return;
        }

        JsonNode genJson = om.readTree(genResp.body());
        String accessToken = textOrNull(genJson, "accessToken");
        String webhook = textOrNull(genJson, "webhook");

        System.out.println("Got accessToken: " + (accessToken != null));
        System.out.println("Webhook URL: " + webhook);

        if (accessToken == null || accessToken.isBlank()) {
            System.err.println("No accessToken in response. Cannot proceed.");
            return;
        }

        // 2) Final SQL for Question 2 (EVEN reg no)
        String finalQuery =
            "SELECT " +
            "  e1.EMP_ID, " +
            "  e1.FIRST_NAME, " +
            "  e1.LAST_NAME, " +
            "  d.DEPARTMENT_NAME, " +
            "  COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
            "FROM EMPLOYEE e1 " +
            "JOIN DEPARTMENT d ON d.DEPARTMENT_ID = e1.DEPARTMENT " +
            "LEFT JOIN EMPLOYEE e2 " +
            "  ON e2.DEPARTMENT = e1.DEPARTMENT " +
            " AND e2.DOB > e1.DOB " + // born later => younger
            "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
            "ORDER BY e1.EMP_ID DESC;";

        String submitJson = "{\"finalQuery\":\"" + escape(finalQuery) + "\"}";

        // 3) Submit the SQL to testWebhook/JAVA with Authorization header = accessToken
        HttpRequest submitReq = HttpRequest.newBuilder()
                .uri(URI.create(SUBMIT_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken) // EXACTLY as required
                .POST(HttpRequest.BodyPublishers.ofString(submitJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> submitResp = http.send(submitReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("Submission status: " + submitResp.statusCode());
        System.out.println("Submission response: " + submitResp.body());

        // 4) (Optional) Also post to your unique webhook if they expect it
        if (webhook != null && !webhook.isBlank()) {
            HttpRequest hookReq = HttpRequest.newBuilder()
                    .uri(URI.create(webhook))
                    .header("Content-Type", "application/json")
                    .header("Authorization", accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(submitJson, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> hookResp = http.send(hookReq, HttpResponse.BodyHandlers.ofString());
            System.out.println("Webhook post status: " + hookResp.statusCode());
            System.out.println("Webhook post response: " + hookResp.body());
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String textOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }
}
