package Project1agents;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Agent implements Runnable {

	private int id; 									// unique id for the agent.
	private int n; 										// the total number of agents that each agent knows of.
	private Mailer mailer; 								// a reference to the mailer.
	private List<Integer> recieved = new ArrayList<Integer>(); // printed upon termination.
	private Message m; 
	static int counter = -1; 							// the unique id of each agent.
	private ArrayList<Integer> constrainted = new ArrayList<Integer>(); // who the agent is constrained with.


	public Agent(Mailer mailer , int total) {
		this.mailer = mailer;
		this.n = total;
		this.id = generateId();										// increments the id upon the constructor, creates a unique id for each agent
	}
	
	@Override
	public void run() {									// sends and recieves the content of the messages (id's) from and to the other agents.	
		this.mailer.getMap().put(this.id, new ArrayList<Message>()); // mapping each agent upon running it.
		this.m = new Message(this.id , this.id,0); 		// the message of the agent.
		List<Integer> sent = new ArrayList<Integer>(); 	// a list contains the agents I already sent a message to.
		Message temp = new Message(0,0,0); 				// temporary message.
		
		while (this.recieved.size() < n-1) {
			for (int i = 0 ; i < n; i++) { 				// sends the message to all the other agents.
				if(i != this.id && sent.contains(i) == false) { // checking to send to the other agents that arent me and that I havent sent to yet.
					if(this.mailer.getMap().containsKey(i) == true) { //checking if the agent being sent has mapped itself.
						this.mailer.send(i, this.m); 	// sending him the message.
						sent.add(i); 					// adding him to the sent agents list.
					}
				}
			}			

			for(int i = 0 ; i < n ; i++) {
				temp = this.mailer.readOne(this.id); 	// reading the messages one by one from the mapped array list of messages.
				if (temp != null) {	
					this.recieved.add(temp.getContent()); // if the message isn't empty => adds it to the list of recieved messages.
				}
			}
		}
		System.out.println(this.id + " " +  this.getRecieved()); // after finishing sending and reading all the messages, prints the messages.
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getId() {
		return id;
	}

	public int getN() {
		return n;
	}

	public Mailer getMailer() {
		return mailer;
	}

	public List<Integer> getRecieved() {
		return recieved;
	}
	
	public ArrayList<Integer> getConstrainted() {
		return constrainted;
	}
	
	public int generateId() {
		counter++;
		return counter;
	}
}
