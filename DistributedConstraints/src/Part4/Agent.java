package Part4;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Agent implements Runnable {

	private int id; // unique id for the agent.
	private int n; // the total number of agents that each agent knows of.
	static int counter = -1; // the unique id of each agent.
	int value = -1; // the value each agent obtains and sends.
	private int k; // the domain size.
	private int[][][][] constraints;
	static int msgCounter = 0;
	static int ccsCounter = 0;
	private Mailer mailer; // a reference to the mailer.
	private Message m;
	private Message temporary; // temporary message.
	private ArrayList<Integer> constrainedWith = new ArrayList<Integer>(); // who the agent is constrained with.
	static ArrayList<Integer> ended = new ArrayList<Integer>(); // list of all agent's id's that got to the goal (solution or no-solution).
	private int[] domain; // the permanent domain.
	private int[] currentDomain; // the domain to be checked while running.
	Boolean premission = false; // 
	static Boolean terminated = false; // chenges to true if the agents were all terminated.

	public Agent(Mailer mailer, int total, int k) {
		this.mailer = mailer;
		this.n = total;
		this.k = k;
		this.id = generateId(); // creates a unique id for each agent.
	}

	@Override
	public void run() { // sends and recieves from and to the other agents.
		Message temp; // temporary message.
		createDomains(); // creating domains with size k, with values in it in range [0,k].
		while (ended.size() < n) { // checking if the ended list is full.
			if (this.m.getHeader() == "Normal") { // if normal message.
				if (this.id + 1 == n) { // if last -> sends solution back to it's previous.
					this.m.setHeader("Solution");
				} else {
					this.temporary = new Message(this.id, new HashMap<Integer, Integer>()); // creates a temporary message to send.
					this.temporary.setHeader("Normal"); 
					transferContent(this.m,this.temporary); // transfering all existing values in m to temporary.
					System.out.println(this.id + " : Normal" + " to Agent " + (this.id+1));
					this.temporary.setMsgCCs(ccsCounter);
					this.mailer.send(this.id + 1, this.temporary); // sending the updated temporary.
					msgCounter ++;
					this.currentDomain[this.value] = -1; // deleting the value from the domain.
					this.m.setHeader("Idle"); // waiting for next message to be sent.
				}
			}

			if (this.m.getHeader() == "BT") { // if backtrack message.
				if (this.getId() == 0) { // if the first one -> sends no solution to the next one.
					this.m.setHeader("No-Solution");
					this.temporary.setMsgCCs(ccsCounter);
					this.mailer.send(this.id + 1, this.m);
					msgCounter ++;
				} else {
					this.temporary = new Message(this.id, new HashMap<Integer, Integer>());
					this.temporary.setHeader("BT");
					transferContent(this.m,this.temporary);
					System.out.println(this.id + " : BT" + " to Agent " + (this.id-1));
					this.temporary.setMsgCCs(ccsCounter);
					this.mailer.send(this.id - 1, this.temporary); 
					msgCounter ++;
					this.m.setHeader("Idle");
					resetCurrentDomain(); // reseting the current domain, a new value to be checked will come up next.
				}
			}

			if (this.m.getHeader() == "No-Solution") { // if no-solution messeage -> sends it to the next one.
				if (this.id + 1 == n) { // if it's the last agent -> only adds it to the ended list.
					if (!(ended.contains(this.id))) {
						synchronized(ended) {
							ended.add(this.id);							
						}
						this.m.setHeader("No-Solution "); 
					}
				} else {
					this.temporary = new Message(this.id, new HashMap<Integer, Integer>()); // if not, sends forward.
					this.temporary.setHeader("No-Solution");
					if (!(ended.contains(this.id))) {
						synchronized(ended){
							ended.add(this.id);							
						}
					}
					this.temporary.setMsgCCs(ccsCounter);
					this.mailer.send(this.id + 1, this.temporary);
					msgCounter ++;
					this.m.setHeader("No-Solution ");
				}
			}

			if (this.m.getHeader() == "Solution") { // if solution message.
				this.mailer.permission = true;
				if (this.id == 0) { // if first one, only adds it to the ended list.
					if (!(ended.contains(this.id))) {
						ended.add(this.id);
						this.m.setHeader("Solution ");
					}
				} else {
					this.temporary = new Message(this.id, new HashMap<Integer, Integer>()); //if not, sends it backwards.
					this.temporary.setHeader("Solution");
					if (!(ended.contains(this.id))) {
						ended.add(this.id);
					}
					this.temporary.setMsgCCs(ccsCounter);
					this.mailer.send(this.id - 1, this.temporary);
					msgCounter ++;
					this.m.setHeader("Solution ");
				}
			}

			temp = this.mailer.readOne(this.id); // reading the messages one by one from the mapped array list of messages.
			if (temp != null) { 

				if (temp.getHeader() == "Normal") { // if normal message.
					transferContent(temp,this.m); // m gets all the content from temp.
					generateValue(); // getting a new value -> first one is 0 if its the first normal message recieved.
					if (constrainedWith.contains(this.id - 1)) { // if there is a constraint between them.
						prevValue(); // searches for a good value.
						if (value == -1) { // if a good value wasn't found.
							this.m.setHeader("BT"); // will send a backtrack.
						} else {
							this.m.setHeader("Normal"); // else, a good value was found.
							this.m.getContent().put(this.id, this.value); // adding the content to the message to be sent.
							this.currentDomain[this.value] = -1; // removing the value from the current domain.
						}
					} else {
						this.m.setHeader("Normal"); // if there is no constraint -> just adding the next value to the message to be sent.
						this.m.getContent().put(this.id, this.value);
						this.currentDomain[this.value] = -1;
					}
				}

				if (temp.getHeader() == "BT") { // if backtrack message.
					transferContent(temp,this.m); // transfering content from temp to m.
					this.m.getContent().remove(this.id); // removing the content of the specific agent.
					if(this.id == 0) { // if its the first agent.
						generateValue(); // getting a new value, since there are no previous agents to check constraints with.
						currentDomain[this.value] = -1; // removing the value from the current domain.
						if(checkIfEmpty() == true) { // if the current domain is empty.
 							this.m.setHeader("BT"); // will send a backtrack.
						}
						else { // else, it has a value, will send a regular message forward.
							this.m.setHeader("Normal");
							this.m.getContent().put(this.id, this.value);
						}
					}
					else { // if its not the first agent.
						if(this.constrainedWith.contains(this.id - 1)) { // checks if there is a constraint between this agent and the one previous.
 							if(value < k ) { // no looping over the value k.
								generateValue();								
							}
							prevValue(); // searching for a good value.
							if (value == -1) { // if no such value was found.
								this.m.setHeader("BT"); // will send backtrack.
							} else {
								this.m.setHeader("Normal"); // else, a good value was found, will send a regular message.
								this.m.getContent().put(this.id, this.value);
								this.currentDomain[this.value] = -1;
							}							
						}
						else {
							if(value == k) { // if there is no constraint backwards -> making sure it won't loop over the value k.
								value = -1;
								this.m.setHeader("BT");// will send a backtrack.
							}
							else {
								generateValue(); // if it hasn't reached k, getting a new value and will send a regular message forward.
								this.m.setHeader("Normal");
								this.m.getContent().put(this.id, this.value);
								this.currentDomain[this.value] = -1;
							}
						}
					}
				}

				if (temp.getHeader() == "No-Solution") { // if no solution, will send a no solution.
					this.m.setHeader("No-Solution");
				}

				if (temp.getHeader() == "Solution") { // if solution, will send a solution.
					this.m.setHeader("Solution");
				}
			}
		}
		terminated = true; // will change to true if all agents were terminated.
	}
	
	public void createDomains() { // creating domain and currentdomain in range [0,k] values.
		this.domain = new int[k+1];
		this.currentDomain = new int[k+1];
		for (int i = 0; i <= this.k; i++) {
			domain[i] = i;
			currentDomain[i] = i;
		}
	}

	public void resetCurrentDomain() { // reseting the values inside current domain.
		for (int i = 0; i <= this.k; i++) {
			currentDomain[i] = i;
		}
	}

	public Boolean checkIfEmpty() { // if all values are -1 in it, means its empty.
		for (int i = 0; i <= k; i++) {
			if (currentDomain[i] != -1) {
				return false;
			}
		}
		return true;
	}

	public int generateValue() { // getting a new value by incrementing it. if it has reached k+1, will reset it to -1 then ++  = 0;
		if (this.value > this.k) {
			this.value = -1;
		}
		this.value++;
		return this.value;
	}

	public void prevValue() { // this function checks using the constraints and the values in constraint, which good value to send.
		// if there isn't one, value will get -1, which will mean to send a backtrack message.
		if (this.constraints[this.id - 1][this.id][m.getContent().get(this.id - 1)][this.value] == 1) {
			while (premission == false && value != -1 ){
				if(this.constraints[this.id - 1][this.id][m.getContent().get(this.id - 1)][this.value] == 1) { // if the values are constrained.
					ccsCounter ++;
					currentDomain[this.value] = -1; // removing the value from the current domain.
					generateValue(); // getting the next value by order.
					if (checkIfEmpty() == true) { // if the current domain is empty.
						this.value = -1; // value gets -1, exits the while, will send a backtrack.
					}					
				}else {
					premission = true; // if the values aren't constrained, the value will be kept and exiting with premission granted (good value).
				}
			}
		} 
		else if (this.constraints[this.id][this.id - 1][this.value][m.getContent().get(this.id - 1)] == 1) {
			while (value != -1 && premission == false ) {
				if(this.constraints[this.id][this.id - 1][this.value][m.getContent().get(this.id - 1)] == 1) {
					ccsCounter ++;
					currentDomain[this.value] = -1;
					generateValue();
					if (checkIfEmpty() == true) {
						this.value = -1;
					}					
				}else {
					premission = true;
				}
			}
		} 
		else { // avoiding looping over the value k.
			if(value == k) {
				value = -1; // will send a backtrack.
			}
		}
		premission = false; // reseting premission to false for next use.
	}
	
	public void transferContent(Message origin , Message destination) { // transfering the content from one message to the other.
		for (int i : origin.getContent().keySet()) {
			destination.getContent().put(i, origin.getContent().get(i));
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

	public ArrayList<Integer> getConstrainedWith() {
		return constrainedWith;
	}

	public int generateId() {
		counter++;
		return counter;
	}

	public int[][][][] getConstraints() {
		return constraints;
	}

	public void setConstraints(int[][][][] constraints) {
		this.constraints = constraints;
	}

	public Message getM() {
		return m;
	}

	public void setM(Message m) {
		this.m = m;
	}

	public int getValue() {
		return value;
	}

}
