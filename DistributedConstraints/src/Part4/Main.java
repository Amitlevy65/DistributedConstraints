package Part4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Main {

	public static void main(String[] args) throws InterruptedException {

		int n = Integer.valueOf(args[0]).intValue(); // number of agents is given as command line argument.
		int k = Integer.valueOf(args[1]).intValue(); // the range of values in range [0,k].
		double p1 = Float.valueOf(args[2]).floatValue(); // the probability the agents will be constrained.
		double p2 = Float.valueOf(args[3]).floatValue(); // the probability a pair of values is constrained.
		
		int counter;
		int[][][][] tempConstraints = new int[n][n][][];
//		String result = null;
		System.out.println("Please enter how many runs you'd like to make: ");
		Scanner sc = new Scanner(System.in);
		int N = sc.nextInt(); // N runs, user's choise.
		System.out.println("Choose the value of which you'd like to increment p2 with: ");
		double increment = sc.nextDouble(); // increment of p2 
		
		ArrayList<Agent> synchAgents = new ArrayList<Agent>();
		ArrayList<ASynchAgent> a_synchAgents = new ArrayList<ASynchAgent>(); 
		for(int i = 0 ; i < n ; i++) {
			synchAgents.add(null);
			a_synchAgents.add(null);
		}
		
		while(p2 < 1.0) { // N runs.

			counter = 1;
			while(counter <= N) {
				if(ASynchAgent.terminated == true) {
					Agent.terminated = false; // changing terminated to false so we can run it all again.
					Agent.ended.clear(); // clearing the ended list, making it reuseable.
					Agent.counter = -1; // counter is set to -1, id's start from 0 to n;
				}
				
				if(Agent.terminated == false) { // terminated is false while running and true after termination, set to false again after all resets.
					System.out.println("\nRun No." + counter+" Synchronized \n"); 
					System.out.println("p2 = " + p2);
					Generator synchGenerator = new Generator(n, k, p1, p2); // creating the generator using the given values.
					Mailer synchMailer = new Mailer();
					
					for (int i = 0; i < n; i++) {
						synchAgents.set(i,new Agent(synchMailer, n,k));
						synchAgents.get(i).setM(new Message(synchAgents.get(i).getId(), new HashMap<Integer,Integer>()));
						synchMailer.getIdlesList().add(i);
					}
					
					synchAgents.get(0).generateValue();
					synchAgents.get(0).getM().getContent().put(synchAgents.get(0).getId(),synchAgents.get(0).getValue() ); // setting the value to the content of the message.
					synchAgents.get(0).getM().setHeader("Normal"); // header is normal to send a normal message first.
					
					for(Agent agent : synchAgents) {
						synchMailer.getMap().put(agent.getId(), new ArrayList<Message>()); // Mapping every agent in the mailer's hashmap.
					}
					synchGenerator.setAgents(synchAgents); // setting the array of agents to the generator.
					tempConstraints = synchGenerator.getConstraints();
					synchGenerator.initGame(); // initiation of the game, using multiple functions inside this function.
					
					ArrayList<Thread> threads = new ArrayList<Thread>();
					for (int i = 0; i < n; i++) {
						threads.add(i, new Thread(synchAgents.get(i))); // creating threads with agent objects => agents are being mapped upon creation.
					}
					
					int[][] copy = new int[n][n];
					System.out.println("\n");
					System.out.println(" ----- Stats for run no. " + (counter) + " ----- ");
					for(Agent agent: synchAgents) {
						System.out.println("Agent id: " + agent.getId() );
						System.out.println(" === List of constraints ===");
						for(int i = 0 ; i<=n ; i++) {
							if(agent.getConstrainedWith().contains(i)) {
								System.out.println("Constraints with agent " + i);
								copy = agent.getConstraints()[agent.getId()][i];
								System.out.println(Arrays.deepToString(copy));
							}
						}
						System.out.println(" === End List of constraints ===\n");
					}
					
					for (Thread t : threads) {
						t.start();
					}
					
					for (Thread t : threads) {
						t.join();
					}
					
					System.out.println(" ------- The result of the run ------- \n");
					System.out.println(synchAgents.get(0).getM().getHeader());
					System.out.println(" ------- End of the run ------- \n");
				}
				
				if(Agent.terminated == true) {
					ASynchAgent.terminated = false;
					ASynchAgent.getEnded().clear();
					ASynchAgent.counter = -1;
				}
				
				if(ASynchAgent.terminated == false) { // terminated is false while running and true after termination, set to false again after all resets.
					System.out.println("\nRun No." + counter+" Asynchronized \n"); 
					System.out.println("p2 = " + p2);
					Generator a_synchGenerator = new Generator(n, k, p1, p2); // creating the generator using the given values.
					Mailer a_synchMailer = new Mailer();
					
					for (int i = 0; i < n; i++) {
						a_synchAgents.set(i,new ASynchAgent(a_synchMailer, n,k));
						a_synchAgents.get(i).setM(new Message(a_synchAgents.get(i).getId(), new HashMap<Integer,Integer>()));
						a_synchAgents.get(i).generateValue();
						a_synchMailer.getIdlesList().add(i);
					}
					
					a_synchAgents.get(0).getM().getContent().put(a_synchAgents.get(0).getId(),a_synchAgents.get(0).getValue() ); // setting the value to the content of the message.
					a_synchAgents.get(0).getM().setHeader("Normal"); // header is normal to send a normal message first.
					
					for(ASynchAgent agent : a_synchAgents) {
						a_synchMailer.getMap().put(agent.getId(), new ArrayList<Message>()); // Mapping every agent in the mailer's hashmap.
					}
					
					for(int i = 0 ; i < synchAgents.size() ; i++) {
						a_synchAgents.get(i).setConstrainedWith(synchAgents.get(i).getConstrainedWith());
						a_synchAgents.get(i).setConstraints(synchAgents.get(i).getConstraints());
					}
					
					a_synchGenerator.setASynchAgents(a_synchAgents); // setting the array of agents to the generator.
					
					a_synchGenerator.setConstraints(tempConstraints);
					ArrayList<Thread> threads = new ArrayList<Thread>();
					for (int i = 0; i < n; i++) {
						threads.add(i, new Thread(a_synchAgents.get(i))); // creating threads with agent objects => agents are being mapped upon creation.
					}
					
					int[][] copy = new int[k][k];
					System.out.println("\n");
					System.out.println(" ----- Stats for run no. " + (counter) + " ----- ");
					for(ASynchAgent agent: a_synchAgents) {
						System.out.println("Agent id: " + agent.getId() );
						System.out.println(" === List of constraints ===");
						for(int i = 0 ; i <= n ; i++) {
							if(agent.getConstrainedWith().contains(i)) {
								System.out.println("Constraints with agent " + i);
								copy = synchAgents.get(i).getConstraints()[agent.getId()][i];
								System.out.println(Arrays.deepToString(copy));
							}
						}
						System.out.println(" === End List of constraints ===\n");
					}
					
					for (Thread t : threads) {
						t.start();
					}
					
					for (Thread t : threads) {
						t.join();
					}
					
					
					System.out.println(" ------- The result of the run ------- \n");
					if(a_synchAgents.get(0).getM().getHeader() == "No-Solution ") {
						System.out.println("No-Solution\n");						
					}else {
						if(a_synchAgents.get(n-1).getM().getHeader() == "Solution ") {
							System.out.println("Solution\n");
						}
					}
					System.out.println(" ------- End of the run ------- \n");

					
					
				}
				counter++;
			}
			System.out.println("======================= RESULTS =======================\n");
			
			System.out.println("Number of agents is: " + n + ".");
			System.out.println("domain size is: " + k + ".");
			System.out.println("p1 is: " + (float)p1 + ".");
			System.out.println("p2 is: " + (float)p2 + ".");
			System.out.println("increment of p2 is: " + (float)increment + ".");
			System.out.println("The number of runs is: " + N + ".");
			
			System.out.println("-------------------------------------------------------\n");
			System.out.println("@@ Synchronized Agent @@ \n");
			System.out.println("Total synchronized agents messages are:" + Agent.msgCounter + " , while " + (float)Agent.msgCounter/N + " in average for each run.");
			System.out.println("Total CSS's is: " + Agent.ccsCounter + " , while " + (float)Agent.ccsCounter/N + " in average for each run.\n");
			
			System.out.println("@@ Asynchronized Agent @@ \n");
			System.out.println("Total synchronized agents messages are:" + ASynchAgent.msgCounter + " , while " + (float)ASynchAgent.msgCounter/N + " in average for each run.");
			System.out.println("Total CSS's is: " + ASynchAgent.ccsCounter + " , while " + (float)ASynchAgent.ccsCounter/N + " in average for each run.");
			System.out.println("=====================  THE END =====================\n");
			
			ASynchAgent.ccsCounter = 0;
			ASynchAgent.msgCounter = 0;
			Agent.ccsCounter = 0;
			Agent.msgCounter = 0;
			p2 += increment;
		}
		

	
	}

}