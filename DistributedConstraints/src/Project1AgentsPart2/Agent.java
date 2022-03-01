package Project1AgentsPart2;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Agent implements Runnable {

	private int id; // unique id for the agent.
	private int n; // the total number of agents that each agent knows of.
	private Mailer mailer; // a reference to the mailer.
	private List<Integer> recieved = new ArrayList<Integer>(); // printed upon termination.
	private Message m;
	static int counter = -1; // the unique id of each agent.
	private ArrayList<Integer> constrainedWith = new ArrayList<Integer>(); // who the agent is constrained with.
	private int[][] constraints; // a matrix that holds in the correct constraint between id's -> the amount of values constraints they share.

	public Agent(Mailer mailer, int total) {
		this.mailer = mailer;
		this.n = total;
		this.id = generateId(); // creates a unique id for each agent.
		this.constraints = new int[n][n]; // each agent holds a matrix of n*n to present the constraints with the other agents.
	}

	@Override
	public void run() { // sends and recieves from and to the other agents.
		List<Integer> sent = new ArrayList<Integer>(); // a list contains the agents I already sent a message to.
		Message temp; // temporary message.

		while (this.recieved.size() < constrainedWith.size()) {
			for (int i = 0; i < n; i++) { // sends the message to all the other agents.
				if (this.constrainedWith.contains(i) && sent.contains(i) == false) { // checking to send to the other agents that aren't me and that I havent sent to yet.
					this.m = new Message(this.id, this.id, this.constraints[this.id][i]); // the message of the agent, now also holds the number of values in constaint with the sent agent.
					if (this.mailer.getMap().containsKey(i) == true) { // checking if the agent being sent has mapped itself.
						this.mailer.send(i, this.m); // sending him the message.
						sent.add(i); // adding him to the sent agents list.
					}
				}
			}

			for (int i = 0; i < n; i++) {
				temp = this.mailer.readOne(this.id); // reading the messages one by one from the mapped array list of messages.
				if (temp != null) {
					this.recieved.add(temp.getConstrainedness()); // if the message isn't empty => adds it to the list of recieved messages (adds the number of values constrained to the list).
					if (temp.getConstrainedness() > 0) {
						System.out.println("My id is " + this.id + ". my constrainedness with " + temp.getSender() + " is " + temp.getConstrainedness() + ".");
						// prints out the number between 2 agents.
					}
				}
			}
		}
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

	public ArrayList<Integer> getConstrainedWith() {
		return constrainedWith;
	}

	public int generateId() {
		counter++;
		return counter;
	}

	public int[][] getConstraints() {
		return constraints;
	}

}
