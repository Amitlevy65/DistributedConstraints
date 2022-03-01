package Part3;

import java.util.HashMap;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Message {

	private int sender; // id of the sender.
	private HashMap<Integer, Integer> content = new HashMap<>();
	private String header;

	public Message(int sender, HashMap<Integer, Integer> content) {
		this.sender = sender;
		this.content = content;
	}

	public int getSender() {
		return sender;
	}

	public HashMap<Integer,Integer> getContent() {
		return content;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}
}
