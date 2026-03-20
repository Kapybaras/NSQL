package uk.ac.bangor.cs.dyp24nbv.nsql;

import java.io.IOException;
import java.nio.charset.*;
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
	public GeminiManager(@Value("${gemini.api.key}") String apiKey, @Value("${gemini.model.name}") String modelName) {
		this.model = GoogleAiGeminiChatModel.builder().apiKey(apiKey).modelName(modelName)
				.timeout(Duration.ofSeconds(120)).build();
	}

	/**
	 * Multi-Pass Audit Logic: 
	 * Pass 1: Generate SQL from Natural Language 
	 * Pass 2: Audit the generated SQL for safety
	 */
	public String askAI(String userQuery, String dynamicSchema) {
		try {
			// Get the instructions
            String genInstructions = sysInstructionsResource.getContentAsString(StandardCharsets.UTF_8);
            String valInstructions = valInstructionsResource.getContentAsString(StandardCharsets.UTF_8);
            
            // Pass 1
            String personalizedInstructions = genInstructions.replace("{{DYNAMIC_SCHEMA}}", dynamicSchema);
            String generatedSQL = model.generate(personalizedInstructions + "\nUser Query: " + userQuery).trim();
            
         // Check if AI is asking for clarification
            if (generatedSQL.startsWith("CLARIFICATION:") || generatedSQL.contains("ERROR:")) {
                return generatedSQL; // Bypass Auditor for non-SQL responses
            }

            // Pass 2
            String auditResponse = model.generate(valInstructions + "\nPROPOSED SQL: " + generatedSQL).trim();

            if (auditResponse.equalsIgnoreCase("YES")) {
                return generatedSQL;
            } else {
                log.warn("Security Alert: Blocked query: {}", generatedSQL); // Log security events
                return "Security Alert: The command was deemed unsafe.";
            }
        } catch (IOException e) {
            log.error("Failed to load instructions", e);
            return "AI Error: Configuration issue.";
        }
	}
}