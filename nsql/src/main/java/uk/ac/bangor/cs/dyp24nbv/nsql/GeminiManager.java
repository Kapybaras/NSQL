package uk.ac.bangor.cs.dyp24nbv.nsql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

@Component
public class GeminiManager {

    private static final Logger log = LoggerFactory.getLogger(GeminiManager.class);
    private final GoogleAiGeminiChatModel model;

    @Value("classpath:SYS_INSTRUCTIONS.txt")
    private Resource sysInstructionsResource;
    
    @Value("classpath:VAL_INSTRUCTIONS.txt")
    private Resource valInstructionsResource;

    @Autowired
    public GeminiManager(@Value("${gemini.api.key}") String apiKey, 
                         @Value("${gemini.model.name}") String modelName) {
        this.model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * Multi-Pass Audit Logic: 
     * Pass 1: Generate SQL from Natural Language 
     * Pass 2: Audit the generated SQL for safety
     */
    public String askAI(String userQuery, String dynamicSchema) {
        try {
            // Load resource content
            String genInstructions = sysInstructionsResource.getContentAsString(StandardCharsets.UTF_8);
            String valInstructions = valInstructionsResource.getContentAsString(StandardCharsets.UTF_8);
            
            // Pass 1: Generation
            String personalizedInstructions = genInstructions.replace("{{DYNAMIC_SCHEMA}}", dynamicSchema);
            String generatedSQL = safeGenerate(personalizedInstructions + "\nUser Query: " + userQuery);
            
            // If safeGenerate returned an error message instead of SQL
            if (generatedSQL.startsWith("AI Error:")) {
                return generatedSQL;
            }

            // Check if AI is asking for clarification
            if (generatedSQL.startsWith("CLARIFICATION:") || generatedSQL.contains("ERROR:")) {
                return generatedSQL; 
            }

            // Pass 2: Audit
            String auditResponse = safeGenerate(valInstructions + "\nPROPOSED SQL: " + generatedSQL);

            if (auditResponse.equalsIgnoreCase("YES")) {
                return generatedSQL;
            } else if (auditResponse.startsWith("AI Error:")) {
                return auditResponse;
            } else {
                log.warn("Security Alert: Blocked query: {}", generatedSQL);
                return "Security Alert: The command was deemed unsafe.";
            }

        } catch (IOException e) {
            log.error("Failed to load instructions from classpath", e);
            return "AI Error: Instruction files missing.";
        }
    }

    // Catches errors (timeouts, cant connect etc to stop whitepage)
    private String safeGenerate(String prompt) {
        try {
            return model.generate(prompt).trim();
        } catch (RuntimeException e) {
            log.error("Gemini API Error: {}", e.getMessage());
            
            if (e.getMessage().contains("503") || e.getMessage().contains("high demand")) {
                return "AI Error: The model is currently overloaded. Please wait 30 seconds and try again.";
            } else if (e.getMessage().contains("timeout") || e.getMessage().contains("SocketTimeoutException")) {
                return "AI Error: The request timed out. The query might be too complex.";
            }
            
            return "AI Error: An unexpected error occurred while contacting the model.";
        }
    }
}