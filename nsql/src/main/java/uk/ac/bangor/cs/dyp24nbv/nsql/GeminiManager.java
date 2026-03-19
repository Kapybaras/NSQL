package uk.ac.bangor.cs.dyp24nbv.nsql;

import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.*;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Collectors;

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
            this.generatorInstructions = loadResourceFile("SYS_INSTRUCTIONS.txt");
            this.validatorInstructions = loadResourceFile("VAL_INSTRUCTIONS.txt");

        } catch (Exception e) {
        	System.err.println("Initialization Error: " + e.getMessage());
        }
    }

    private String loadResourceFile(String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) throw new IOException("File not found: " + fileName);
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        }
    }
    /**
     * Multi-Pass Audit Logic:
     * Pass 1: Generate SQL from Natural Language
     * Pass 2: Audit the generated SQL for safety
     */
    public String askAI(String userQuery) {
        try {
            // PASS 1: Generation
            String sqlGenerationPrompt = generatorInstructions + "\nUser Query: " + userQuery;
            String generatedSQL = model.generate(sqlGenerationPrompt).trim();

            // PASS 2: Validation
            String validationPrompt = validatorInstructions + 
                                      "\nPROPOSED SQL: " + generatedSQL + 
                                      "\nRespond ONLY with YES or NO.";
            String auditResponse = model.generate(validationPrompt).trim();

            // Check if the auditor approved the query
            if (auditResponse.equalsIgnoreCase("YES")) {
                return generatedSQL;
            } else {
                return "Security Alert: The generated command was deemed unsafe and blocked by the auditor.";
            }

        } catch (Exception e) {
            return "AI Error: " + e.getMessage();
        }
    }
}