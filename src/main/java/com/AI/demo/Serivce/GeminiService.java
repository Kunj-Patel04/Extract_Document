package com.AI.demo.Serivce;


import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.stereotype.Service;

import com.google.genai.Client;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.model}")
    private String model;

    String prompt = """
    Analyze the uploaded document image (Indian Aadhaar card or PAN card). Extract the details and return the result STRICTLY as a JSON object. Do not include markdown formatting, code blocks, or any other conversational text.
    
    If the document is an Aadhaar card, use this exact JSON structure:
    {
      "document_type": "Aadhaar",
      "aadhaar_number": "",
      "full_name_english": "",
      "full_name_regional": "",
      "dob": "",
      "address_english": "",
      "address_regional": ""
    }
    
    If the document is a PAN card, use this exact JSON structure:
    {
      "document_type": "PAN",
      "pan_number": "",
      "full_name": "",
      "fathers_name": "",
      "dob": ""
    }
    
    If any specific field cannot be read from the image, leave its value as an empty string.
    """;


    public String extractInformation(MultipartFile file) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        String base64Data = Base64.getEncoder().encodeToString(file.getBytes());

        // Use a Text Block to format the raw JSON payload directly
        String jsonPayload = """
        {
          "contents": [{
            "parts": [
              { "text": "%s" },
              { "inline_data": { "mime_type": "%s", "data": "%s" } }
            ]
          }]
        }
        """.formatted(prompt.replace("\"", "\\\""), file.getContentType(), base64Data);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(jsonPayload, headers),
                String.class
        );

        // Parse the response down to just the text
        try {
            return new ObjectMapper().readTree(response.getBody())
                    .at("/candidates/0/content/parts/0/text") // Short-hand path traversal
                    .asText();
        } catch (Exception e) {
            return response.getBody(); // Fallback on failure
        }
    }


}
















































//
//
//@Value("${gemini.api.key}")
//private String apiKey;
//@Value("${gemini.api.model}")
//private String model;
//
//public String extractInformation(MultipartFile file) throws Exception {
//    // Use gemini-1.5-flash as it supports multimodal (text + document/image) inputs natively
//    String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
//
//    String prompt = "You are an expert Indian KYC document data extractor. Analyze the uploaded image or document and extract all personal details present. Identify whether the document is an Aadhaar Card, a PAN Card, or both. \n" +
//            "\n" +
//            "Extract every single piece of information exactly as written. Do not leave any field out. If a specific detail is not present on the document, return `null` for that key.\n" +
//            "\n" +
//            "Return the final output STRICTLY as a valid JSON object using the exact structure below. Do not include any markdown formatting (like ```json), explanations, or conversational text.\n" +
//            "\n" +
//            "{\n" +
//            "  \"document_type\": \"Aadhaar Card / PAN Card / Both / Unknown\",\n" +
//            "  \"aadhaar_details\": {\n" +
//            "    \"aadhaar_number\": \"\",\n" +
//            "    \"full_name\": \"\",\n" +
//            "    \"dob_or_yob\": \"\",\n" +
//            "    \"gender\": \"\",\n" +
//            "    \"full_address\": \"\",\n" +
//            "    \"vid\": \"\",\n" +
//            "    \"issue_date\": \"\"\n" +
//            "  },\n" +
//            "  \"pan_details\": {\n" +
//            "    \"pan_number\": \"\",\n" +
//            "    \"full_name\": \"\",\n" +
//            "    \"father_name\": \"\",\n" +
//            "    \"dob\": \"\",\n" +
//            "  }\n" +
//            "}";
//
//    // Convert the uploaded document to Base64
//    String base64Data = Base64.getEncoder().encodeToString(file.getBytes());
//    String mimeType = file.getContentType();
//
//    // 1. Build the document inline_data part
//    Map<String, Object> inlineData = new HashMap<>();
//    inlineData.put("mime_type", mimeType);
//    inlineData.put("data", base64Data);
//
//    Map<String, Object> filePart = new HashMap<>();
//    filePart.put("inline_data", inlineData);
//
//    // 2. Build the text prompt part
//    Map<String, Object> textPart = new HashMap<>();
//    textPart.put("text", prompt + "\n\nStrictly follow the formatting requested in the prompt above.");
//
//    // 3. Combine parts into contents
//    Map<String, Object> partsNode = new HashMap<>();
//    partsNode.put("parts", Arrays.asList(textPart, filePart));
//
//    Map<String, Object> payload = new HashMap<>();
//    payload.put("contents", Arrays.asList(partsNode));
//
//    // 4. Send HTTP POST request to Gemini
//    RestTemplate restTemplate = new RestTemplate();
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.APPLICATION_JSON);
////        "///////////////////////"
//    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
//    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
//
//    // 5. Parse the response to extract ONLY the text
//    ObjectMapper mapper = new ObjectMapper();
//    JsonNode rootNode = mapper.readTree(response.getBody());
//
//    try {
//        // Navigate through the JSON tree to get the specific text response
//        String extractedText = rootNode.path("candidates").get(0)
//                .path("content").path("parts").get(0)
//                .path("text").asText();
//
//        return extractedText;
//    } catch (Exception e) {
//        // Fallback: If it fails to parse (like during a 503 error), return the raw error
//        return response.getBody();
//    }
//
////        return response.getBody();
//}