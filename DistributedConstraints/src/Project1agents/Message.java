package Project1agents;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Message {
	
	private int sender;  //id of the sender
	private int content;	
	private int constrainedness;
	
	public Message(int sender, int content,int constrainedness) {
		this.sender = sender;
		this.content = content;
		this.constrainedness = constrainedness;
	}

	public int getSender() {
		return sender;
	}

	public int getContent() {
		return content;
	}
	
	public int getConstrainedness() {
		return constrainedness;
	}
}
