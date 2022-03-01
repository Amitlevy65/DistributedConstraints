package Project1agents;

import java.util.ArrayList;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Main {

	public static void main(String[] args) throws InterruptedException {

		int n = Integer.valueOf(args[0]).intValue(); // number of agents is given as command line argument.
		int d = Integer.valueOf(args[1]).intValue(); // number of agents is given as command line argument.
		double p1 = Float.valueOf(args[2]).doubleValue(); // number of agents is given as command line argument.
		double p2 = Float.valueOf(args[3]).doubleValue(); // number of agents is given as command line argument.
		
		Generator generator = new Generator(n,d,p1,p2);
		
		Mailer mailer = new Mailer(); 
	
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for (int i = 0 ; i<n ; i++) {
			threads.add(i,new Thread(new Agent(mailer,n))); // creating threads with agent objects => agents are being mapped upon creation.
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