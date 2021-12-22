


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Random;

import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.*;

import jig.misc.rd.AirCurrentGenerator;
import jig.misc.rd.Direction;
import jig.misc.rd.RobotDefense;


/**
 *  A simple agent that uses reinforcement learning to direct the vacuum
 *  The agent has the following critical limitations:
 *  
 *  	- it only considers a small set of possible actions
 *  	- it does not consider turning the vacuum off
 *  	- it only reconsiders an action when the 'local' state changes  
 *         in some cases this may take a (really) long time
 *      - it uses a very simplisitic action selection mechanism
 *      - actions are based only on the cells immediately adjacent to a tower
 *      - action values are not dependent (at all) on the resulting state 
 */
public class NathanAgent extends BaseLearningAgent {

	/**
	 * A Map of states to actions
	 * 
	 *  States are encoded in the StateVector objects
	 *  Actions are associated with a utility value and stored in the QMap
	 */
	HashMap<StateVector,QMap> actions = new HashMap<StateVector,QMap>();

	/**
	 * The agent's sensor system tracks /how many/ insects a particular generator
	 * captures, but here I want to know /when/ an air current generator just
	 * captured an insect so I can reward the last action. We use this captureCount
	 * to see when a new capture happens.
	 */
	HashMap<AirCurrentGenerator, Integer> captureCount;

	/**
	 * Keep track of the agent's last action so we can reward it
	 */
	HashMap<AirCurrentGenerator, AgentAction> lastAction;
	
	/**
	 * This stores the possible actions that an agent many take in any
	 * particular state.
	 */
	private static final AgentAction [] potentials;

	static {
		Direction [] dirs = Direction.values();
		//Manually setting actions to avoid actions that are consistently bad choices
		potentials = new AgentAction[24]; //possible actions should be 24 at the moment,
											// due to 8 possible directions and 3 power settings

		int i = 0;
		for(Direction d: dirs) {
			potentials[i] = new AgentAction(2, d);
			i++;
			potentials[i] = new AgentAction(3, d);
			i++;
			potentials[i] = new AgentAction(4, d);
			i++;
		}
	}
	
	List<StateVector> stateList;
	
	private static final AgentAction [] shutDown; // new array that holds the shutdown action, to ensure that
													// it is never randomly selected by mistake
	
	static
	{
		Direction [] dirs = Direction.values();
		shutDown = new AgentAction[1];
		shutDown[0] = new AgentAction(0, dirs[3]);
	}
	
	public NathanAgent() {
		captureCount = new HashMap<AirCurrentGenerator,Integer>();
		lastAction = new HashMap<AirCurrentGenerator,AgentAction>();
		stateList = new LinkedList<StateVector>();
	}
	
	/**
	 * Step the agent by giving it a chance to manipulate the environment.
	 * 
	 * Here, the agent should look at information from its sensors and 
	 * decide what to do.
	 * 
	 * @param deltaMS the number of milliseconds since the last call to step
	 * 
	 */
	public void step(long deltaMS) {
		StateVector state;
		StateVector newState;
		StateVector compareState;
		QMap qmap;
		QMap currentQmap;
		QMap compareQmap;
		QMap utilityChecker;
		

		// This must be called each step so that the performance log is 
		// updated.
		updatePerformanceLog();
		
		for (AirCurrentGenerator acg : sensors.generators.keySet()) {
			if (!stateChanged(acg)) continue;


			// Check the current state, and make sure member variables are
			// initialized for this particular state...
			state = thisState.get(acg);
			
			
			if (actions.get(state) == null) {
				actions.put(state, new QMap(potentials));
				stateList.add(state);
			}
			
			compareState = state;
			
			if (captureCount.get(acg) == null) captureCount.put(acg, 0);
			
			if (!state.checkRadiusForBugs())
			{
				AgentAction myAction = shutDown[0];
				myAction.doAction(acg);
			}
			


			// Check to see if an insect was just captured by comparing our
			// cached value of the insects captured by each ACG with the
			// most up-to-date value from the sensors
			boolean justCaptured;
			justCaptured = (captureCount.get(acg) < sensors.generators.get(acg));

			// if this ACG has been selected by the user, we'll do some verbose printing
			boolean verbose = (RobotDefense.getGame().getSelectedObject() == acg);

			// If we did something on the last 'turn', we need to reward it
			if (lastAction.get(acg) != null ) {
				
				for(int z=0; z < stateList.size(); z++)
				{
					
					compareState = state.checkForSimilarStates(stateList.get(z), actions);
					utilityChecker = actions.get(compareState);
					
					utilityChecker = (actions.get(compareState));
					for(int j = 0; j < utilityChecker.utility.length; j++)
					{
						if (utilityChecker.utility[j] > 0)
						{
							break;
						}
					}
					
				}
				// get the action map associated with the previous state
				qmap = actions.get(lastState.get(acg));
				compareQmap = actions.get(compareState);
				

				if (justCaptured) {
					// capturing insects is good
					newState = thisState.get(acg);
					
					if (actions.get(newState) == null) {
						actions.put(newState, new QMap(potentials));
					}
					
					currentQmap = actions.get(thisState.get(acg));
					qmap.rewardAction(lastAction.get(acg), 10.0, currentQmap);
					captureCount.put(acg,sensors.generators.get(acg));
				}
				
				else if (!justCaptured) {
					newState = thisState.get(acg);
					
					if (actions.get(newState) == null) {
						actions.put(newState, new QMap(potentials));
					}
					
					currentQmap = actions.get(thisState.get(acg));
					qmap.rewardAction(lastAction.get(acg), -1, currentQmap);
					captureCount.put(acg,sensors.generators.get(acg));
				}

				if (verbose) {
					System.out.println("Last State for " + acg.toString() );
					System.out.println(lastState.get(acg).representation());
					System.out.println("Updated Last Action: " + qmap.getQRepresentation());
				}
			} 

			// decide what to do now...
			// first, get the action map associated with the current state
			qmap = actions.get(state);
			compareQmap = actions.get(compareState);

			if (verbose) {
				System.out.println("This State for Tower " + acg.toString() );
				System.out.println(thisState.get(acg).representation());
			}
			// find the 'right' thing to do, and do it.
			AgentAction bestAction;
			if(state.checkRadiusForBugs())
			{
				bestAction = qmap.findBestAction(verbose, lastAction.get(acg), compareQmap);
				bestAction.doAction(acg);
				lastAction.put(acg, bestAction);
			}

		}
	}


	/**
	 * This inner class simply helps to associate actions with utility values
	 */
	static class QMap {
		static Random RN = new Random();
		private double[] utility; 		// current utility estimate
		private int[] attempts;			// number of times action has been tried
		private AgentAction[] actions;  // potential actions to consider

		public QMap(AgentAction[] potential_actions) {
			
			actions = potential_actions.clone();
			int len = actions.length;

			utility = new double[len];
			attempts = new int[len];
			for(int i = 0; i < len; i++) {
				utility[i] = 0.0;
				attempts[i] = 0;
			}
		}

		/**
		 * Finds the 'best' action for the agent to take.
		 * 
		 * @param verbose
		 * @return
		 */
		public AgentAction findBestAction(boolean verbose, AgentAction lastAction, QMap compareQmap) {
			//Find best action in comparable state
			int i2,maxi2,maxcount2;
			int compare1, compare2;
			maxi2=0;
			maxcount2 = 1;
			
			
			if (verbose)
				System.out.print("Picking Best Actions from Comparable State: " + compareQmap.getQRepresentation());

			for (i2 = 1; i2 < compareQmap.utility.length; i2++) {
				if (compareQmap.utility[i2] > compareQmap.utility[maxi2]) {
					maxi2 = i2;
					maxcount2 = 1;
				}
				else if (compareQmap.utility[i2] == compareQmap.utility[maxi2]) {
					maxcount2++;
				}
			}
			if (RN.nextDouble() > .2) {
				int whichMax2 = RN.nextInt(maxcount2);
			}
			
			//Original lines start here
			int i,maxi,maxcount;
			maxi=0;
			maxcount = 1;
			
			if (verbose)
				System.out.print("Picking Best Actions: " + getQRepresentation());

			for (i = 1; i < utility.length; i++) {
				if (utility[i] > utility[maxi]) {
					maxi = i;
					maxcount = 1;
				}
				else if (utility[i] == utility[maxi]) {
					maxcount++;
				}
			}
			if(compareQmap.utility[maxi2] > utility[maxi])
			{
				maxi = maxi2;
				maxcount = 1;
			}
			if (RN.nextDouble() > .2) {
				int whichMax = RN.nextInt(maxcount);
				
				if(maxcount > 1)
				{
					if(RN.nextDouble() > .5)
					{
						for (i = 0; i < actions.length; i++)
						{
							if(lastAction == actions[i])
								return actions[i];
						}
					}
				}

				if (verbose)
					System.out.println( " -- Doing Best! #" + whichMax);

				for (i = 0; i < utility.length; i++) {
					if (utility[i] == utility[maxi]) {
						if (whichMax == 0) return actions[i];
						whichMax--;
					}
				}
				return actions[maxi];
			}
			else {
				int which = RN.nextInt(actions.length);
				if (verbose)
					System.out.println( " -- Doing Random (" + which + ")!!");

				return actions[which];
			}
		}

		/**
		 * Modifies an action value by associating a particular reward with it.
		 * 
		 * @param a the action performed 
		 * @param value the reward received
		 */
		public void rewardAction(AgentAction a, double value, QMap currentUtility) {
			int i, n, j;
			double qMax = 0;
			double learningRate = 0.7;
			double discountFactor = 0.9;
			
			//Find action to be rewarded, is utility[i]
			for (i = 0; i < actions.length; i++) {
				if (a == actions[i]) break;
			}
			if (i >= actions.length) {
				System.err.println("ERROR: Tried to reward an action that doesn't exist in the QMap. (Ignoring reward)");
				return;
			}
			
			//Next, find qMax utility in the current (successor) state
			for (n = 0; n < currentUtility.utility.length; n++)
			{
				if (currentUtility.utility[n] > qMax)
				{
					qMax = currentUtility.utility[n];
				}
			}
			
			utility[i] = (1 - learningRate)*utility[i] + (learningRate*(value + (discountFactor*qMax)));
		}
		/**
		 * Gets a string representation (for debugging).
		 * 
		 * @return a simple string representation of the action values
		 */
		public String getQRepresentation() {
			StringBuffer sb = new StringBuffer(80);

			for (int i = 0; i < utility.length; i++) {
				sb.append(String.format("%.2f  ", utility[i]));
			}
			return sb.toString();

		}

	}
}