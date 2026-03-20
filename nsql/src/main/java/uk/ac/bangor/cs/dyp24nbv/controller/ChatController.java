package uk.ac.bangor.cs.dyp24nbv.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;

import uk.ac.bangor.cs.dyp24nbv.message.Message;
import uk.ac.bangor.cs.dyp24nbv.nsql.*;

@Controller
public class ChatController {
	private final GeminiManager ai;
	private final SchemaService schemaService;
	private final JdbcTemplate jdbcTemplate;

	// Spring automatically finds the GeminiManager, SchemaService and JdbcTemplate
	// beans and passes them into this constructor, pretty cool huh.
	@Autowired
	public ChatController(GeminiManager ai, SchemaService schemaService, JdbcTemplate jdbcTemplate) {
		this.ai = ai;
		this.schemaService = schemaService;
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("/")
	public String showChat(Model m, HttpSession session) {
		List<Message> sessionMessages = (List<Message>) session.getAttribute("messages");
		if(sessionMessages == null) sessionMessages = new ArrayList<>();
		
		m.addAttribute("message", new Message());
		m.addAttribute("messages", sessionMessages);
		m.addAttribute("DatabaseResults", session.getAttribute("results"));
		return "workspace";
	}

	@PostMapping("/")
	public String upDateChat(Message message, HttpSession session, Model m) {
		List<Message> sessionMessages = (List<Message>) session.getAttribute("messages");
		if(sessionMessages == null) sessionMessages = new ArrayList<>();
		sessionMessages.add(message);
		
        String currentSchema = schemaService.generateSchemaPrompt();
        String aiResponse = ai.askAI(message.getMessage(), currentSchema);   
        
        sessionMessages.add(new Message(Message.People.AI, aiResponse));
        session.setAttribute("messages", sessionMessages);
        try {
            // Only run SQL if it's NOT a clarification, error, or security block
            boolean isSql = !aiResponse.startsWith("CLARIFICATION:") 
                         && !aiResponse.startsWith("Security Alert") 
                         && !aiResponse.startsWith("AI Error")
                         && !aiResponse.startsWith("ERROR:");

            if (isSql) {
                session.setAttribute("results", jdbcTemplate.queryForList(aiResponse));
            } else {
                session.setAttribute("results", null); // Clear results for conversational turns
            }
        } catch (Exception e) {
            session.setAttribute("results", null);
            sessionMessages.add(new Message(Message.People.AI, "Database Error: " + e.getMessage()));
        }
		return "redirect:/";
	}
}