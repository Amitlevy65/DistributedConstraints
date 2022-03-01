package Part3;

import java.util.ArrayList;
import java.util.Random;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Generator {
	
	int n; // number of agents.
	int k; // the range of values in range [0,k].
	double p1; // the probability two agents are constrained.
	double p2; // the probability two values are constrained.
	int[][][][] constraints; // a matrix that has [n][n][k][k] => n*n for each pair of agents, k*k for all range of values between each pair of agents.
	int maxAmount; // the maximum amount of constrained values.
	int constraintsCounter = 0; // the counter that counts the amount of values constrained up to maxAmount.
	ArrayList<Agent> agents = new ArrayList<Agent>(); // a list of all the agents in the game.
	Random rand = new Random(); // a random variable.

	public Generator(int n, int k, double p1, double p2) {
		this.n = n;
		this.k = k;
		this.p1 = p1;
		this.p2 = p2;
	}

	public void generateConstraints() { // This function generates the constraints between agents and between values for each pair of agent.
		this.maxAmount = (n * (n - 1)) / 2; // The maximum amount of values constrained in the game, according to the formula given.
		for (int row = 0; row <= this.n - 1; row++) { // from 0 to n for all pair of agents.
			for (int col = 0; col <= this.n - 1; col++) { 

				if (constraints[row][col] == null) { // if the agents still aren't constrained.
					if (row != col) { // an agent can't constrain with itself.
						float createProbabilityP1 = (float) (0.0 + (1.0 - 0.0) * rand.nextFloat()); // a random float variable in range of [0.0 , 1.0].
 
						if (createProbabilityP1 <= this.p1) { // if the outcome is less then the probability given => it creates a k*k matrix between the two agents.

							this.constraints[row][col] = new int[this.k + 1][this.k + 1]; // example: k=8, so range is [0,8], thats why k+1 is given as the argument.
							this.constraints[col][row] = new int[this.k + 1][this.k + 1];

							this.constraintsCounter++; // increments the counter and checks if it got to maxAmount. breaks the function if it does.
							if (constraintsCounter == maxAmount) {
								break;
							}
							for (int row2 = 0; row2 <= this.k; row2++) { // in range of [0,k] for all pair of values possible.
								for (int col2 = 0; col2 <= this.k; col2++) {

									float createProbabilityP2 = (float) (0.0 + (1.0 - 0.0) * rand.nextFloat()); // anoutner float variable with the same range.
									if (createProbabilityP2 <= this.p2) { // if the outcome is less then the probability given => the values are given the value 1 between them, otherwise, 0.
										this.constraints[row][col][row2][col2] = 1;
										this.constraints[col][row][col2][row2] = 1;
									} else {
										this.constraints[row][col][row2][col2] = 0;
										this.constraints[col][row][col2][row2] = 0;

									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void constrainedWithWho(Agent agent) { // This function checks if the agents are constrained, and if they are, it counts the amount of values constrained (amount of 1's in the k*k matrix).
		for (int index = 0; index <= this.n - 1; index++) { 
			if (this.constraints[agent.getId()][index] != null) {  // if there is a k*k matrix between 2 agents.
				agent.getConstrainedWith().add(index); //adds the id of the constrained agent to the list of constrained agents of the agent given as an argument to the function.
			}
		}
	}

	public void initGame() { 
		this.constraints = new int[n][n][][]; // First, creates a matrix of n*n values with empty matrix to each value.
		this.generateConstraints(); // Then, calls the function to create the constraints between agents and values.
		for (Agent agent : this.agents) { // for every agent in the list of agents, connects the constraints between them and shares the amount of values constrained between them.
			this.constrainedWithWho(agent);
		}
		for(Agent agent : this.agents) {
			agent.setConstraints(constraints);
		}
	}

	public ArrayList<Agent> getAgents() {
		return agents;
	}

	public void setAgents(ArrayList<Agent> agent) {
		this.agents = agent;
	}

}
