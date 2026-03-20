package uk.ac.bangor.cs.dyp24nbv.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import uk.ac.bangor.cs.dyp24nbv.message.Message;
import uk.ac.bangor.cs.dyp24nbv.nsql.*;

@Controller
public class ChatController {
	private final List<Message> messages = new ArrayList<Message>();
	private final GeminiManager ai;
	private final SchemaService schemaService;
	private final JdbcTemplate jdbcTemplate; // Directly use Spring's JDBC tool
	private List<Map<String, Object>> results; // Change type to handle dynamic columns

	// Spring automatically finds the GeminiManager, SchemaService and JdbcTemplate
	// beans and passes them into this constructor, pretty cool huh.
	@Autowired
	public ChatController(GeminiManager ai, SchemaService schemaService, JdbcTemplate jdbcTemplate) {
		this.ai = ai;
		this.schemaService = schemaService;
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("")
	public String showChat(Model m) {
		m.addAttribute("message", new Message());
		m.addAttribute("messages", messages);
		m.addAttribute("DatabaseResults", results);
		return "workspace";
	}

	@PostMapping("")
	public String upDateChat(Message message, BindingResult result, Model m) {
		messages.add(message);
        String currentSchema = schemaService.generateSchemaPrompt();
        String aiResponse = ai.askAI(message.getMessage(), currentSchema);   
        messages.add(new Message(Message.People.AI, aiResponse));
		// 2. Execute directly via JdbcTemplate (no DTO needed)
		try {
			// Only attempt to run if the response isn't a security block or error
			if (!aiResponse.contains("Security Alert") && !aiResponse.contains("AI Error")) {
				this.results = jdbcTemplate.queryForList(aiResponse);
			}
		} catch (Exception e) {
			this.results = null;
			messages.add(new Message(Message.People.AI, "Database Error: " + e.getMessage()));
		}

		return showChat(m);
	}

}
