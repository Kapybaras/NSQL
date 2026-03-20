package uk.ac.bangor.cs.dyp24nbv.nsql;

import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.*;
import java.time.Duration;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

@Component
public class GeminiManager {

	private GoogleAiGeminiChatModel model;
	private String generatorInstructions;
	private String validatorInstructions;

	@Autowired
	public GeminiManager(@Value("${gemini.api.key}") String apiKey, @Value("${gemini.model.name}") String modelName) {
		this.model = GoogleAiGeminiChatModel.builder().apiKey(apiKey).modelName(modelName)
				.timeout(Duration.ofSeconds(120)).build();

		// 3. Load the instructions
		try {
			this.generatorInstructions = loadResourceFile("SYS_INSTRUCTIONS.txt");
			this.validatorInstructions = loadResourceFile("VAL_INSTRUCTIONS.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("CRITICAL ERROR: Could not load AI instruction files. " + e.getMessage());
		}
	}

	private String loadResourceFile(String fileName) throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
			if (is == null)
				throw new IOException("File not found: " + fileName);
			return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines()
					.collect(Collectors.joining("\n"));
		}
	}

	/**
	 * Multi-Pass Audit Logic: Pass 1: Generate SQL from Natural Language Pass 2:
	 * Audit the generated SQL for safety
	 */
	public String askAI(String userQuery, String dynamicSchema) {
		try {
	        // Inject the real database schema into the instruction template
	        String personalizedInstructions = this.generatorInstructions
	                .replace("{{DYNAMIC_SCHEMA}}", dynamicSchema);

	        // PASS 1: Generation
	        String generationPrompt = personalizedInstructions + "\nUser Query: " + userQuery;
	        String generatedSQL = model.generate(generationPrompt).trim();

			// PASS 2: Validation
			String validationPrompt = validatorInstructions + "\nPROPOSED SQL: " + generatedSQL
					+ "\nRespond ONLY with YES or NO.";
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