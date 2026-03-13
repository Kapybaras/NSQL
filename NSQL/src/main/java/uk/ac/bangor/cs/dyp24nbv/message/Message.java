package uk.ac.bangor.cs.dyp24nbv.message;

import java.time.LocalTime;
import org.springframework.stereotype.Component;


public class Message {
	public static enum People {
			AI,
			User
	};
	private String message;
	private LocalTime time;
	private People owner;
	
	public Message() {}
	
	public Message(People owner,String message) 
	{
		this.owner=owner;
		this.message=message;
		this.time = LocalTime.now();
	}
	
	public Message(String message) 
	{
		this.owner=People.User;
		this.message=message;
		this.time = LocalTime.now();
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public LocalTime getTime() {
		return time;
	}
	public People getOwner() {
		return owner;
	}
	
	
	

}
