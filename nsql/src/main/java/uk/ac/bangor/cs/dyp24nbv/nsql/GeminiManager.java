package uk.ac.bangor.cs.dyp24nbv.nsql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

import org.springframework.stereotype.Component;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

@Component
public class GeminiManager {

    private GoogleAiGeminiChatModel model;
    private String generatorInstructions;
    private String validatorInstructions;

    public GeminiManager() {
        try {
            // 1. Load the API Key from the properties file
            Properties props = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("application-secrets.properties");
            if (is != null) {
                props.load(is);
                String apiKey = props.getProperty("gemini.api.key");

                // 2. Build the model
                this.model = GoogleAiGeminiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName("gemini-3.1-flash-lite-preview")
                        .timeout(Duration.ofSeconds(120))
                        .build();
            }

            // 3. Load the instructions
            this.generatorInstructions = new String(Files.readAllBytes(
                Paths.get("src/main/resources/SYS_INSTRUCTIONS.txt")));
            
            this.validatorInstructions = new String(Files.readAllBytes(
                Paths.get("src/main/resources/VAL_INSTRUCTIONS.txt")));

        } catch (IOException e) {
            System.err.println("Could not load: " + e.getMessage());
        }
    }

    public String askAI(String userQuery) {

        String combinedPrompt = "System Instructions: " + generatorInstructions + 
                                "\nSafety Rule: " + validatorInstructions +
                                "\nIf the SQL is unsafe, start your response with 'BLOCKED'." +
                                "\nUser Query: " + userQuery;

        try {
            String response = model.generate(combinedPrompt).trim();

            if (response.startsWith("BLOCKED")) {
                return "Security Alert: The generated command was deemed unsafe and blocked.";
            }
            
            return response;
        } catch (Exception e) {
            return "AI Error: " + e.getMessage();
        }
    }
}