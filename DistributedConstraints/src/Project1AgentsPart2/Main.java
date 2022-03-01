package Project1AgentsPart2;

import java.util.ArrayList;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Main {

	public static void main(String[] args) throws InterruptedException {

		int n = Integer.valueOf(args[0]).intValue(); // number of agents is given as command line argument.
		int k = Integer.valueOf(args[1]).intValue(); // the range of values in range [0,k].
		float p1 = Float.valueOf(args[2]).floatValue(); // the probability the agents will be constrained.
		float p2 = Float.valueOf(args[3]).floatValue(); // the probability a pair of values is constrained.

		Generator generator = new Generator(n, k, p1, p2); // creating the generator using the given values.
		Mailer mailer = new Mailer();
		
		ArrayList<Agent> agents = new ArrayList<Agent>(); // an arraylist of the agents.
		for (int i = 0; i < n; i++) {
			agents.add(new Agent(mailer, n));
		}
		
		for(Agent agent : agents) {
			mailer.getMap().put(agent.getId(), new ArrayList<Message>()); // Mapping every agent in the mailer's hashmap.
		}
		generator.setAgents(agents); // setting the array of agents to the generator.

		generator.initGame(); // initiation of the game, using multiple functions inside this function.

		ArrayList<Thread> threads = new ArrayList<Thread>();

		for (int i = 0; i < n; i++) {
			threads.add(i, new Thread(agents.get(i))); // creating threads with agent objects => agents are being mapped
														// upon creation.
		}

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			t.join();
		}

		System.out.println("Completed Successfully");
	}

}