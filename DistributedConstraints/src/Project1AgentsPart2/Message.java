package Project1AgentsPart2;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Message {

	private int sender; // id of the sender.
	private int content; // the content is the id of the sender.
	private int constrainedness; // amount of values constrained between the agents.

	public Message(int sender, int content, int constrainedness) {
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

	public void setConstrainedness(int constrainedness) {
		this.constrainedness = constrainedness;
	}
}
