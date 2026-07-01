package com.telusko;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
/**
 * Simple demo program that calls a generative language/chat completions HTTP API
 * using Java's built-in HttpClient.
 *
 * <p>Purpose:
 * - Demonstrate building and sending a JSON chat completion request.
 * - Show how to read an API key from an environment variable.
 *
 * Usage:
 * - Set the environment variable GEMINI-API-KEY to a valid API key before running.
 *   Example (Windows PowerShell):
 *     $env:GEMINI_API_KEY = 'your-key-here'
 *
 * Notes:
 * - This example sends a small chat request and simply prints the raw response body.
 * - The code intentionally keeps error handling minimal (throws Exception) for brevity.
 */
public class Main {
    /**
     * Program entry point.
     *
     * @param args command-line arguments (ignored)
     * @throws Exception if an I/O or network error occurs while sending the request
     */
    public static void main(String[] args) throws Exception
    {
//        String apiKey= System.getenv("OPENAI_API_KEY");
        // Read the API key from an environment variable. The example uses GEMINI-API-KEY.
        // You may choose a different variable name (e.g. OPENAI_API_KEY) depending on which
        // service and key you have available.
        String apiKey= System.getenv("GEMINI-API-KEY");
//        String uri= "https://api.openai.com/v1/chat/completions";
        // The endpoint below targets Google's Generative Language OpenAI-compatible endpoint.
        // Replace with the appropriate endpoint for your provider if needed.
        String uri= "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions";

        // Create a simple, default HttpClient. For production code you may want to
        // customize timeouts, proxy, or authentication settings.
        HttpClient client= HttpClient.newHttpClient();

        // Request body as a JSON text block. This example asks the model to act as
        // a movie review expert and recommend a movie for a software engineer.
        String requestBody =
                """
                {
                  "model": "gemini-3.5-flash",
                  "messages": [
                    {"role": "system", "content": "You are a top movie review expert"},
                    {"role": "user", "content": "name a movie that a software engineer must watch"}
                  ]
                }
                """;

        // Build the HTTP POST request with required headers. Content-Type must be
        // application/json and Authorization uses a Bearer token with the API key.
        HttpRequest request= HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        // Send the request and receive the response as a String body. This call blocks
        // until the response is received. In production, consider using the async
        // sendAsync method and better error handling / retries.
        HttpResponse<String> response =client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print the raw response body to stdout. The response will typically be a JSON
        // document containing model output, usage metrics, etc.
        System.out.println(response.body());

    }
}