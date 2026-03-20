package uk.ac.bangor.cs.dyp24nbv.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import uk.ac.bangor.cs.dyp24nbv.dbManager.DataTransferObject;
import uk.ac.bangor.cs.dyp24nbv.message.Message;
import uk.ac.bangor.cs.dyp24nbv.nsql.GeminiManager;

@Controller
public class ChatController {

	private List<Message> messages = new ArrayList<Message>();
	private GeminiManager ai = new GeminiManager();
	private ArrayList<ArrayList<String>> results;
	private DataTransferObject dto;
	
	ChatController()
	{
		try {
			dto = new DataTransferObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping("")
	public String showChat(Model m) {
		m.addAttribute("message", new Message());
		if (!m.containsAttribute("messages")) {
			m.addAttribute("messages",messages);
			}
		m.addAttribute("DatabaseResults",results);
		return "workspace";
	}
	@PostMapping("")
	public String upDateChat(Message message, BindingResult result, Model m) 
	{
		messages.add(message);
		String response = ai.askAI(message.getMessage());
		messages.add(new Message(Message.People.AI, response));
		results = dto.getAllparametersFromStatement(response);

		return showChat(m);
	}
	
}
