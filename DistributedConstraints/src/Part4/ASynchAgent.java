package Part4;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class ASynchAgent implements Runnable {

	private int id; // unique id for the agent.
	private int n; // the total number of agents that each agent knows of.
	static int counter = -1; // the unique id of each agent.
	int value = -1; // the value each agent obtains and sends.
	private int k; // the domain size.
	private int[][][][] constraints;
	static int msgCounter = 0;
	static int ccsCounter = 0;
	private int to = -1;
	private Mailer mailer; // a reference to the mailer.
	private Message m;
	private Message temporary; // temporary message.
	private ArrayList<Integer> constrainedWith = new ArrayList<Integer>(); // who the agent is constrained with.
	private ArrayList<Integer> prevConstraints = new ArrayList<Integer>();
	private ArrayList<Integer> nextConstraints = new ArrayList<Integer>();
	private static ArrayList<Integer> ended = new ArrayList<Integer>(); // list of all agent's id's that got to the goal (solution or no-solution).
	private int[] domain; // the permanent domain.
	private int[] currentDomain; // the domain to be checked while running.
	static Boolean terminated = false; // chenges to true if the agents were all terminated.
	Boolean constraintExist = false;

	public ASynchAgent(Mailer mailer, int total, int k) {
		this.mailer = mailer;
		this.n = total;
		this.k = k;
		this.id = generateId(); // creates a unique id for each agent.
	}

	@Override
	public void run() {
		Message temp = null; // temporary message.
		createDomains(); // creating domains with size k, with values in it in range [0,k].
		createPrevConstraints();
		createNextConstraints();
		if(prevConstraints.isEmpty() && nextConstraints.size() > 0) {
			this.m.setHeader("Normal");
		}
		while (ended.size() < n) { // checking if the ended list is full.
			if (this.m.getHeader() == "Normal") { // if normal message.
				if(this.id+1 == n) {
					this.m.setHeader("Idle");
				}
				else {
					this.temporary = new Message(this.id, new HashMap<Integer, Integer>()); // creates a temporary message
					// to send.
					this.temporary.setHeader("Normal");
					transferContent(this.m, this.temporary); // transfering all existing values in m to temporary.
					for (int i : nextConstraints) {
						this.mailer.send(i, this.temporary); // sending the updated temporary.
						System.out.println(this.id + " : Normal message to Agent " + i);
						msgCounter++;
					}
					this.currentDomain[this.value] = -1; // deleting the value from the domain.
					if (this.mailer.getMap().get(this.id).isEmpty()) {
						this.m.setHeader("Idle"); // waiting for next message to be sent.
					} else {
						this.m.setHeader("BT ");
					}
				}
			}

			if (this.m.getHeader() == "BT") { // if backtrack message.
				if (this.getId() == 0) { // if the first one -> sends no solution to the next one.
					this.m.setHeader("No-Solution");
				} else {
					if(checkIfEmpty() == true && prevConstraints.size() == 0) {
						this.m.setHeader("No-Solution");
					}
					else {
						this.temporary = new Message(this.id, new HashMap<Integer, Integer>());
						this.temporary.setHeader("BT");
						transferContent(this.m, this.temporary);
						System.out.println(this.id + " : BT to Agent " + to);
						this.mailer.send(to, this.temporary);
						msgCounter++;
						this.m.setHeader("Idle");
						resetCurrentDomain(); // reseting the current domain, a new value to be checked will come up next.
						value = 0;						
					}
				}
			}

			if (this.m.getHeader() == "No-Solution") { // if no-solution messeage -> sends it to the next one.
				this.temporary = new Message(this.id, new HashMap<Integer, Integer>()); // if not, sends forward.
				this.temporary.setHeader("STOP");
				if(this.mailer.getSolution() == false) {
					this.m.setHeader("No-Solution ");					
				}
				else {
					this.m.setHeader("Solution ");					
				}
				if (!(ended.contains(this.id))) {
					synchronized (ended) {
						ended.add(this.id);
					}
				}
					
				for(int i = 0 ; i < n ; i++) {
					if(i != this.id) {
						this.mailer.send(i, this.temporary);
						msgCounter++;
					}
				}					
				terminated = true;
				break;
			}
			

			if (this.mailer.getIdlesList().size() == n && this.mailer.getMap().get(this.id).size() == 0 && this.m.getHeader() == "Idle") { // if solution message.
				this.temporary = new Message(this.id, new HashMap<Integer, Integer>()); // if not, sends it backwards.
				this.temporary.setHeader("STOP");
				this.m.setHeader("Solution ");
				this.mailer.setSolution(true);
				if (!(ended.contains(this.id))) {
					ended.add(this.id);
				}
				for(int i = 0 ; i < n ; i++) {
					if(i != this.id) {
						this.mailer.send(i, this.temporary);						
					}
				}
				terminated = true;
				break;
			}
			
			if(this.m.getHeader() != "Solution " && this.m.getHeader() != "No-Solution ") {
				temp = this.mailer.readOne(this.id); // reading the messages one by one from the mapped array list of				
			}
			else {
				temp = null;
			}
			if (temp != null ) {
				
				if(temp.getHeader() == "STOP") {
					this.m.setHeader("Solution ");
					this.mailer.setSolution(true);
					terminated = true;
					break;
				}
				
				if (temp.getHeader() == "Normal") { // if normal message.
					transferContent(temp, this.m); // m gets all the content from temp.
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						System.out.println("FAIL");
					}
					if (this.value == k) {
						value = -1;
						if(prevConstraints.isEmpty() == true) {
							to = this.id;
						}else {
							for(int i = 0 ; i < prevConstraints.size() ; i++) {
								if(to < prevConstraints.get(i)) {
									to = prevConstraints.get(i);
								}
							}
						}
					} else {
						createPossibleValues();
					}
					if (to != -1) {
						this.m.setHeader("BT"); // will send a backtrack.
					}
					else { // if a good value wasn't found.
						pickValue();
						this.m.setHeader("Normal"); // else, a good value was found.
						this.m.getContent().put(this.id, this.value); // adding the content to the message to be
																			// sent.
						if (!(this.mailer.getIdlesList().contains(this.id))) {
							synchronized(this.mailer.getIdlesList()) {
								this.mailer.getIdlesList().add(this.id);										
							}
						}
					}
				}

				if (temp.getHeader() == "BT") { // if backtrack message.
					transferContent(temp, this.m); // transfering content from temp to m.
					if (this.id == 0) { // if its the first agent.
						if (this.m.getHeader() != "BT " && this.m.getHeader() != "Solution " && this.m.getHeader() != "No-Solution ") {
							this.m.getContent().remove(this.id); // removing the content of the specific agent.
							if (value < k) { // no looping over the value k.
								generateValue();
							}
							if (checkIfEmpty() == true) { // if the current domain is empty.
								this.m.setHeader("BT"); // will send a backtrack.
							} else { // else, it has a value, will send a regular message forward.
								this.m.setHeader("Normal");
								this.m.getContent().put(this.id, this.value);
								if (!(this.mailer.getIdlesList().contains(this.id))) {
									synchronized(this.mailer.getIdlesList()) {
										this.mailer.getIdlesList().add(this.id);										
									}
								}
							}
							currentDomain[this.value] = -1; // removing the value from the current domain.
						}
						if(this.mailer.getMap().get(this.id).isEmpty() && this.m.getHeader() == "BT ") {
							this.m.setHeader("Idle");
						}
					}

					else { // if its not the first agent.
						checkIfConstraintExist();
						if (constraintExist == true) { // checks if there is a constraint between this agent and the one
														// previous.
							if(this.m.getHeader() != "BT " && this.m.getHeader() != "Solution" && this.m.getHeader() != "No-Solution") {
								this.m.getContent().remove(this.id); // removing the content of the specific agent.
								if (value < k) { // no looping over the value k.
									generateValue();
								}
								if (value == k) {
									if(to == -1) {
										for(int i = 0 ; i < prevConstraints.size() ; i++) {
											if(to < prevConstraints.get(i)) {
												to = prevConstraints.get(i);
											}
										}
									}
									this.m.setHeader("BT"); // will send backtrack.
									constraintExist = false;
								}else {
									this.m.setHeader("Normal"); // else, a good value was found, will send a regular
									// message.
									this.m.getContent().put(this.id, this.value);
									this.currentDomain[this.value] = -1;
									constraintExist = false;
									if (!(this.mailer.getIdlesList().contains(this.id))) {
										this.mailer.getIdlesList().add(this.id);
									}
								}
								if(this.mailer.getMap().get(this.id).isEmpty() && this.m.getHeader() == "BT ") {
									this.m.setHeader("Idle");
								}
							}
						} else {
							if (value == k) { // if there is no constraint backwards -> making sure it won't loop over
												// the value k.
								value = -1;
								this.m.setHeader("BT");// will send a backtrack.
							} else {
								generateValue(); // if it hasn't reached k, getting a new value and will send a regular
													// message forward.
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
					this.m.setHeader("Idle");
				}
			}
		}
		terminated = true; // will change to true if all agents were terminated.
	}

	public void createDomains() { // creating domain and currentdomain in range [0,k] values.
		this.domain = new int[k + 1];
		this.currentDomain = new int[k + 1];
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

	public int generateValue() { // getting a new value by incrementing it. if it has reached k+1, will reset it
									// to -1 then ++ = 0;
		if (this.value > this.k) {
			this.value = -1;
		}
		this.value++;
		return this.value;
	}

	public void createPossibleValues() {
		for (int j = 0; j < prevConstraints.size(); j++) {
			for (int i = 0; i <= k; i++) {
				if (m.getContent().get(prevConstraints.get(j)) != null) {
					if (this.constraints[prevConstraints.get(j)][this.id][m.getContent().get(prevConstraints.get(j))][i] == 1) {
						ccsCounter++;
						currentDomain[i] = -1;
					} 
				}
			}
			if(checkIfEmpty() == true) {
				to = prevConstraints.get(j);
				break;
			}
		}
	}

	public void transferContent(Message origin, Message destination) { // transfering the content from one message to
																		// the other.
		for (int i : origin.getContent().keySet()) {
			destination.getContent().put(i, origin.getContent().get(i));
		}
	}

	public void createPrevConstraints() {
		for (int i = 0; i < constrainedWith.size(); i++) {
			if (constrainedWith.get(i) < this.id) {
				prevConstraints.add(constrainedWith.get(i));
			}
		}
	}

	public void createNextConstraints() {
		for (int i = 0; i < constrainedWith.size(); i++) {
			if (constrainedWith.get(i) > this.id) {
				nextConstraints.add(constrainedWith.get(i));
			}
		}
	}

	public void pickValue() {
		for (int i = 0; i <= k; i++) {
			if (currentDomain[i] != -1) {
				value = currentDomain[i];
				currentDomain[i] = -1;
				break;
			}
		}
	}

	public boolean checkIfConstraintExist() {
		for (int i = 0; i < prevConstraints.size(); i++) {
			if (m.getContent().containsKey(prevConstraints.get(i))) {
				constraintExist = true;
			}
		}
		return constraintExist;
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

	public void setConstrainedWith(ArrayList<Integer> constrainedWith) {
		this.constrainedWith = constrainedWith;
	}
	
	public static ArrayList<Integer> getEnded() {
		return ended;
	}

}
