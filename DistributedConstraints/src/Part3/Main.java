package Part3;

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
		
		int counter = 1;
		double totalAS = 0; // all assignment of values.
		double totalBT = 0; // all backtrack messages sent.
		double totalCSS = 0; // all constraints checks.
		
		System.out.println("Please enter how many runs you'd like to make: ");
		Scanner sc = new Scanner(System.in);
		int N = sc.nextInt(); // N runs, user's choise.
		
		ArrayList<Agent> agents = new ArrayList<Agent>();
		for(int i = 0 ; i < n ; i++) {
			agents.add(null);
		}
		
		while(counter <= N) { // N runs.
			if (Agent.ended.size() == n) { // if all agents were terminated.
				Agent.terminated = false; // changing terminated to false so we can run it all again.
				Agent.ended.clear(); // clearing the ended list, making it reuseable.
				Agent.counter = -1; // counter is set to -1, id's start from 0 to n;
				Agent.totalAS = 0; // reseting AS,BT,and CSS -> reuse in next run;
				Agent.totalBT = 0;
				Agent.totalCSS = 0;
			}
			if(Agent.terminated == false) { // terminated is false while running and true after termination, set to false again after all resets.
				System.out.println("\nRun No." + counter+"\n"); 
				Generator generator = new Generator(n, k, p1, p2); // creating the generator using the given values.
				Mailer mailer = new Mailer();
				
				for (int i = 0; i < n; i++) {
					agents.set(i,new Agent(mailer, n,k));
					agents.get(i).setM(new Message(agents.get(i).getId(), new HashMap<Integer,Integer>()));
				}
				
				agents.get(0).generateValue(); // setting the first value to the first agent.
				Agent.totalAS ++;
				agents.get(0).getM().getContent().put(agents.get(0).getId(),agents.get(0).getValue() ); // setting the value to the content of the message.
				agents.get(0).getM().setHeader("Normal"); // header is normal to send a normal message first.
				
				for(Agent agent : agents) {
					mailer.getMap().put(agent.getId(), new ArrayList<Message>()); // Mapping every agent in the mailer's hashmap.
				}
				generator.setAgents(agents); // setting the array of agents to the generator.

				generator.initGame(); // initiation of the game, using multiple functions inside this function.
								
				ArrayList<Thread> threads = new ArrayList<Thread>();
				for (int i = 0; i < n; i++) {
					threads.add(i, new Thread(agents.get(i))); // creating threads with agent objects => agents are being mapped upon creation.
				}
				
				for (Thread t : threads) {
					t.start();
				}
					
				for (Thread t : threads) {
					t.join();
				}
				
				for(Agent agent : agents) {
					System.out.println(agent.getId() + " : " + agent.getM().getHeader());
				}
				totalAS += Agent.totalAS; // adding the totalAS of agent to the total in the main
				totalBT += Agent.totalBT; // same for BT.
				totalCSS += Agent.totalCSS; // same for CSS
				counter++;
				
			}
			int[][] copy = new int[n][n];
			System.out.println("\n");
			System.out.println(" ----- Stats for run no. " + (counter-1) + " ----- ");
			for(Agent agent: agents) {
				System.out.println("Agent id: " + agent.getId() );
				System.out.println(" --- List of constraints ---");
				for(int i = 0 ; i<=n ; i++) {
					if(agent.getConstrainedWith().contains(i)) {
						System.out.println("Constraints with agent " + i);
						copy = agent.getConstraints()[agent.getId()][i];
						System.out.println(Arrays.deepToString(copy));
					}
				}
				System.out.println(" --- List of constraints ---\n");
			}
		}
		
//		System.out.println("======================= CHECKING THE LAST SOLUTION FOR CORRECTNESS =======================");
//
//		
//		
//		if(agents.get(0).getM().getHeader() == "Solution ") { // checking the last result, only if it gave a "Solution".
//			for(int i = n-1 ; i >= 0 ; i--) { // every agent from end to start.
//				if (i == 0) {
//					System.out.println("THE SOLUTION IS CORRECT !!! "); // if it's the first one, it was the last check and it's a correct solution.
//				}
//				else{ // else, it's not the first one.
//					if(agents.get(i).getConstrainedWith().contains(i-1)) { // if there's a constraint between this agent and the one before it.
//						if(agents.get(i).getConstraints()[i][i-1][agents.get(i).value][agents.get(i-1).value] == 1) { // checking their values.
//							System.err.println("THE SOLUTION IS WRONG!!!!"); // if they are constrained values, prints WRONG.
//						}
//						else if(agents.get(i).getConstraints()[i-1][i][agents.get(i-1).value][agents.get(i).value] == 1) {
//							System.err.println("THE SOLUTION IS WRONG!!!!");
//						}
//					}
//				}
//			}
//		}else {
//			System.out.println("Unfortunatly the last result was a No-Solution.");
//		}
		
		System.out.println("======================= RESULTS =======================");
		
		System.out.println("Number of agents is: " + n + ".");
		System.out.println("domain size is: " + k + ".");
		System.out.println("p1 is: " + (float)p1 + ".");
		System.out.println("p2 is: " + (float)p2 + ".");
		System.out.println("The number of runs is: " + N + ".");
		
		System.out.println("-------------------------------------------------------");
		
		System.out.println("Total AS's is: " + totalAS + " , while " + (float)totalAS/N + " in average for each run.");
		System.out.println("Total BT's is: " + totalBT + " , while " + (float)totalBT/N + " in average for each run.");
		System.out.println("Total CSS's is: " + totalCSS + " , while " + (float)totalCSS/N + " in average for each run.");
		
		System.out.println("=====================  THE END =====================");
	}

}