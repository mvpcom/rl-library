/* Mountain Car Domain
 * Copyright (C) 2007, Brian Tanner brian@tannerpages.com (http://brian.tannerpages.com/)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
package org.rlcommunity.mountaincar;

import java.util.StringTokenizer;
import java.util.Vector;

import org.rlcommunity.mountaincar.messages.MCGoalResponse;
import org.rlcommunity.mountaincar.messages.MCHeightResponse;
import org.rlcommunity.mountaincar.messages.MCStateResponse;
import rlVizLib.Environments.EnvironmentBase;
import rlVizLib.general.ParameterHolder;
import rlVizLib.general.RLVizVersion;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvironmentMessageParser;
import rlVizLib.messaging.environment.EnvironmentMessages;
import rlVizLib.messaging.interfaces.HasAVisualizerInterface;
import rlVizLib.messaging.interfaces.RLVizVersionResponseInterface;
import rlVizLib.messaging.interfaces.getEnvMaxMinsInterface;
import rlVizLib.messaging.interfaces.getEnvObsForStateInterface;
import rlglue.types.Action;
import rlglue.types.Observation;
import rlglue.types.Random_seed_key;
import rlglue.types.Reward_observation;
import rlglue.types.State_key;
import java.util.Random;
import org.rlcommunity.mountaincar.visualizer.MountainCarVisualizer;
import rlVizLib.general.hasVersionDetails;
import rlVizLib.utilities.UtilityShop;

/*
 * July 2007
 * This is the Java Version MountainCar Domain from the RL-Library.  
 * Brian Tanner ported it from the Existing RL-Library to Java.
 * I found it here: http://rlai.cs.ualberta.ca/RLR/environment.html
 * 
 * 
 * This is quite an advanced environment in that it has some fancy visualization
 * capabilities which have polluted the code a little.  What I'm saying is that 
 * this is not the easiest environment to get started with.
 */
public class MountainCar extends EnvironmentBase implements
        getEnvMaxMinsInterface,
        getEnvObsForStateInterface,
        RLVizVersionResponseInterface,
        HasAVisualizerInterface {

    static final int numActions = 3;
    protected MountainCarState theState = null;

    //Used for env_get_state and env_save_state
    protected Vector<MountainCarState> savedStates = null;
    //Problem parameters have been moved to MountainCar State
    private Random randomGenerator = new Random();


    public String env_init() {
        savedStates = new Vector<MountainCarState>();
        //This should be like a final static member or something, or maybe it should be configurable... dunno
        int taskSpecVersion = 2;

        return taskSpecVersion + ":e:2_[f,f]_[" + theState.minPosition + "," + theState.maxPosition + "]_[" + theState.minVelocity + "," + theState.maxVelocity + "]:1_[i]_[0,2]:[-1,0]";
    }

    /**
     * Restart the car on the mountain.  Pick a random position and velocity if
     * randomStarts is set.
     * @return
     */
    public Observation env_start() {
        if (theState.randomStarts) {
            double randStartPosition = (randomGenerator.nextDouble() * (theState.maxPosition + Math.abs((theState.minPosition))) - Math.abs(theState.minPosition));
            theState.position = theState.minVelocity;
        } else {
            theState.position = theState.defaultInitPosition;
        }
        theState.velocity = theState.defaultInitVelocity;

        return makeObservation();
    }

    /**
     * Takes a step.  If an invalid action is selected, choose a random action.
     * @param theAction
     * @return
     */public Reward_observation env_step(Action theAction) {

        int a = theAction.intArray[0];

        if (a > 2 || a < 0) {
            System.err.println("Invalid action selected in mountainCar: " + a);
            a = randomGenerator.nextInt(3);
        }

        theState.update(a);

        return makeRewardObservation(theState.getReward(), theState.inGoalRegion());
    }


    /**
     * Return the ParameterHolder object that contains the default parameters for
     * mountain car.  The only parameter is random start states.
     * @return
     */
    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());

        p.addBooleanParam("randomStartStates", true);
        return p;
    }

    /**
     * Create a new mountain car environment using parameter settings in p.
     * @param p
     */
    public MountainCar(ParameterHolder p) {
        super();
        theState = new MountainCarState(randomGenerator);
        if (p != null) {
            if (!p.isNull()) {
                theState.randomStarts = p.getBooleanParam("randomStartStates");
            }
        }
    }

    /**
     * Handles messages that find out the version, what visualizer is available, 
     * etc.
     * @param theMessage
     * @return
     */
    public String env_message(String theMessage) {
        EnvironmentMessages theMessageObject;
        try {
            theMessageObject = EnvironmentMessageParser.parseMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            System.err.println("Someone sent mountain Car a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }

        if (theMessageObject.canHandleAutomatically(this)) {
            String theResponseString = theMessageObject.handleAutomatically(this);
            return theResponseString;
        }

        //If it wasn't handled automatically, maybe its a custom Mountain Car Message
        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();

            if (theCustomType.equals("GETMCSTATE")) {
                //It is a request for the state
                double position = theState.position;
                double velocity = theState.velocity;
                double height = this.getHeight();
                double deltaheight = theState.getHeightAtPosition(position + .05);
                MCStateResponse theResponseObject = new MCStateResponse(position, velocity, height, deltaheight);
                return theResponseObject.makeStringResponse();
            }

            if (theCustomType.startsWith("GETHEIGHTS")) {
                Vector<Double> theHeights = new Vector<Double>();

                StringTokenizer theTokenizer = new StringTokenizer(theCustomType, ":");
                //throw away the first token
                theTokenizer.nextToken();

                int numQueries = Integer.parseInt(theTokenizer.nextToken());
                for (int i = 0; i < numQueries; i++) {
                    double thisPoint = Double.parseDouble(theTokenizer.nextToken());
                    theHeights.add(theState.getHeightAtPosition(thisPoint));
                }

                MCHeightResponse theResponseObject = new MCHeightResponse(theHeights);
                return theResponseObject.makeStringResponse();
            }

            if (theCustomType.startsWith("GETMCGOAL")) {
                MCGoalResponse theResponseObject = new MCGoalResponse(theState.goalPosition);
                return theResponseObject.makeStringResponse();
            }

        }
        System.err.println("We need some code written in Env Message for MountainCar.. unknown request received: " + theMessage);
        Thread.dumpStack();
        return null;
    }

    /**
     * Turns theState object into an observation.
     * @return
     */
    @Override
    protected Observation makeObservation() {
        Observation currentObs = new Observation(0, 2);

        currentObs.doubleArray[0] = theState.position;
        currentObs.doubleArray[1] = theState.velocity;

        return currentObs;
    }

    public MountainCar() {
        this(getDefaultParameters());
    }

    public void env_cleanup() {
        if (savedStates != null) {
            savedStates.clear();
        }
    }

/**
 * Provides a random seed that can be used with env_set_random_seed to sample
 * multiple transitions from a single state.
 * <p>
 * Note that calling this method has a side effect, it creates a new seed and 
 * sets it.
 * @return
 */
    public Random_seed_key env_get_random_seed() {
        Random_seed_key k = new Random_seed_key(2, 0);
        long newSeed = getRandomGenerator().nextLong();
        getRandomGenerator().setSeed(newSeed);
        k.intArray[0] = UtilityShop.LongHighBitsToInt(newSeed);
        k.intArray[1] = UtilityShop.LongLowBitsToInt(newSeed);
        return k;
    }

    public void env_set_random_seed(Random_seed_key k) {
        long storedSeed = UtilityShop.intsToLong(k.intArray[0], k.intArray[1]);
        getRandomGenerator().setSeed(storedSeed);
    }

    public State_key env_get_state() {
        savedStates.add(new MountainCarState(theState));
        State_key k = new State_key(1, 0);
        k.intArray[0] = savedStates.size() - 1;
        return k;
    }

    public void env_set_state(State_key k) {
        int theIndex = k.intArray[0];

        if (savedStates == null || theIndex >= savedStates.size()) {
            System.err.println("Could not set state to index:" + theIndex + ", that's higher than saved size");
            return;
        }
        MountainCarState oldState = savedStates.get(k.intArray[0]);
        this.theState = new MountainCarState(oldState);
    }

    /**
     * The value function will be drawn over the position and velocity.  This 
     * method provides the max values for those variables.
     * @param dimension
     * @return
     */
    public double getMaxValueForQuerableVariable(int dimension) {
        if (dimension == 0) {
            return theState.maxPosition;
        } else {
            return theState.maxVelocity;
        }
    }

    /**
     * The value function will be drawn over the position and velocity.  This 
     * method provides the min values for those variables.
     * @param dimension
     * @return
     */
    public double getMinValueForQuerableVariable(int dimension) {
        if (dimension == 0) {
            return theState.minPosition;
        } else {
            return theState.minVelocity;
        }
    }


    /**
     * Given a state, return an observation.  This is trivial in mountain car
     * because the observation is the same as the internal state 
     * @param theState
     * @return
     */
    public Observation getObservationForState(Observation theState) {
        return theState;
    }

    /**
     * How many state variables are there (used for value function drawing)
     * @return
     */
    public int getNumVars() {
        return 2;
    }
    /**
     * Used by MCHeightRequest Message
     * @return
     */
    private double getHeight() {
        return theState.getHeightAtPosition(theState.position);
    }

    public RLVizVersion getTheVersionISupport() {
        return new RLVizVersion(1, 1);
    }

    public String getVisualizerClassName() {
        return MountainCarVisualizer.class.getName();
    }
    
    private Random getRandomGenerator() {
        return randomGenerator;
    }

}

/**
 * This is a little helper class that fills in the details about this environment
 * for the fancy print outs in the visualizer application.
 * @author btanner
 */
class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "Mountain Car 1.20";
    }

    public String getShortName() {
        return "Mount-Car";
    }

    public String getAuthors() {
        return "Richard Sutton, Adam White, Brian Tanner";
    }

    public String getInfoUrl() {
        return "http://code.google.com/p/rl-library/wiki/MountainCar";
    }

    public String getDescription() {
        return "RL-Library Java Version of the classic Mountain Car RL-Problem.";
    }
}
