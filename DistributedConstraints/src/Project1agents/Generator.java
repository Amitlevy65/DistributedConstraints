package Project1agents;

import java.util.ArrayList;
import java.util.Random;

/**
 * 
 * @author Amit Levy 313268773
 * @author Maya Shternlicht 308341411
 */

public class Generator {

	int n;
	int k;
	double p1;
	double p2;
	int[][] agentConstraints;
	int[][] valueConstraints;
	Random rand = new Random();

	public Generator(int n, int k, double p1, double p2) {
		this.n = n;
		this.k = k;
		this.p1 = p1;
		this.p2 = p2;
	}

	public void generateAgentConstraints() {
		for (int row = 0; row < this.n; row++) {
			for (int col = 0; col < this.n; col++) {

				if (agentConstraints[row][col] == -1) {
					if (row == col) {
						this.agentConstraints[row][col] = 0;
					}
					if (row != col) {
						float createProbability = (float) (0.0 + (1.0 - 0.0) * rand.nextFloat());

						if (createProbability > this.p1 && createProbability <= 1) {
							this.agentConstraints[row][col] = 1;
							this.agentConstraints[col][row] = 1;
						} else {
							this.agentConstraints[row][col] = 0;
							this.agentConstraints[col][row] = 0;
						}
					}

				}
			}
		}
	}

	public void generateValueConstraints() {
		for (int row = 0; row <= this.k; row++) {
			for (int col = 0; col <= this.k; col++) {

				if (valueConstraints[row][col] == -1) {
					float createProbability = (float) (0.0 + (1.0 - 0.0) * rand.nextFloat());

					if (createProbability > this.p2 && createProbability <= 1) {
						this.valueConstraints[row][col] = 0;
						this.valueConstraints[col][row] = 0;
					} else {
						this.valueConstraints[row][col] = 1;
						this.valueConstraints[col][row] = 1;
					}
				}
			}
		}
	}

	public void resetMatrices() {
		for (int row = 0; row < this.n; row++) {
			for (int col = 0; col < this.n; col++) {
				agentConstraints[row][col] = -1;
			}
		}
		for (int row = 0; row <= this.k; row++) {
			for (int col = 0; col <= this.k; col++) {
				valueConstraints[row][col] = -1;
			}
		}
	}

	public void constrain(Agent agent) {
		for (int i = 0; i < n; i++) {
			if (this.agentConstraints[agent.getId()][i] == 1) {
				agent.getConstrainted().add(i);
			}
		}
	}
	
	public void createMatrices(int n,int k) {
		this.agentConstraints = new int[n - 1][n - 1];
		this.valueConstraints = new int[k][k];
	}
	
	public int[][] getAgentConstraints() {
		return agentConstraints;
	}

	public int[][] getValueConstraints() {
		return valueConstraints;
	}
	
	public int countConstrainedValues() {
		int counter = 0;
		for(int i = 0; i <= this.k ; i++) {
			for(int j = 0; j <= this.k ; j++) {
				if(this.valueConstraints[i][j] == 1) {
					counter++;
				}
			}
		}
		return counter;
	}
}
