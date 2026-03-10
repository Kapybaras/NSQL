package com.kernels.nsql;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class GeminiManager {

    private final GoogleAiGeminiChatModel model;
    private String generatorInstructions;
    private String validatorInstructions;

    public GeminiManager() {
        this.model = GoogleAiGeminiChatModel.builder()
                .apiKey("AIzaSyBepeS0nhq34eV646Ig8yi1YhMjbqlhk9U")
                .modelName("gemini-3.1-flash-lite-preview")
                .timeout(java.time.Duration.ofSeconds(60))
                .build();

        try {
            this.generatorInstructions = new String(Files.readAllBytes(
                Paths.get("src/main/resources/SYS_INSTRUCTIONS.txt")));
            
            this.validatorInstructions = new String(Files.readAllBytes(
            	Paths.get("src/main/resources/VAL_INSTRUCTIONS.txt")));
        } catch (IOException e) {
            System.err.println("Could not load instruction files!");
        }
    }

    public String askAI(String userQuery) {
    	// SQL Generator Layer
        String generatedSql = model.generate(generatorInstructions + "\nUser Query: " + userQuery);
        
        // Validation Layer
        String validationCheck = model.generate(validatorInstructions + "\nSQL to Check: " + generatedSql);

        if (validationCheck.trim().equalsIgnoreCase("YES")) {
            return generatedSql;
        } else {
            return "Security Alert: The generated command was deemed unsafe and blocked.";
        }
    }
}