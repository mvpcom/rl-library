/*
Copyright 2007 Brian Tanner
http://rl-library.googlecode.com/
brian@tannerpages.com
http://brian.tannerpages.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.rlcommunity.environments.hotplate;

import java.util.Random;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 * This class manages all of the problem parameters, current state variables, 
 * and state transition and reward dynamics.
 *
 * @author btanner
 */
public class HotPlateState {
//	Current State Information

    private double position;
    private double velocity;
//Some of these are fixed.  This environment would be easy to parameterize further by changing these.
    final public double minPosition = -1.2;
    final public double maxPosition = 0.6;
    final public double minVelocity = -0.07;
    final public double maxVelocity = 0.07;
    final public double goalPosition = 0.5;
    final public double accelerationFactor = 0.001;
    final public double gravityFactor = -0.0025;
    final public double hillPeakFrequency = 3.0;
    //This is the middle of the valley (no slope)
    final public double defaultInitPosition = -0.5d;
    final public double defaultInitVelocity = 0.0d;
    final public double rewardPerStep = -1.0d;
    final public double rewardAtGoal = 0.0d;
    final private Random randomGenerator;
    //These are configurable
    public boolean randomStarts = false;


    private int lastAction=0;
    /**
     * Constructor 
     * @param randomGenerator
     */
    HotPlateState(Random randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public double getPosition() {
        return position;
    }

    public double getVelocity() {
        return velocity;
    }

    /**
     * Calculate the reward for the 
     * @return
     */
    public double getReward() {
        if (inGoalRegion()) {
            return rewardAtGoal;
        } else {
            return rewardPerStep;
        }
    }

    /**
     * IS the agent past the goal marker?
     * @return
     */
    public boolean inGoalRegion() {
        return position >= goalPosition;
    }

    protected void reset() {
        position = defaultInitPosition;
        velocity = defaultInitVelocity;
        if (randomStarts) {
            //We use goal position instead of max position so that the agent
            //doesn't start past the goal ever
            double maxStartPosition = goalPosition;
            double randStartPosition = (randomGenerator.nextDouble() * (maxStartPosition + Math.abs(minPosition)) - Math.abs(minPosition));
            position = randStartPosition;
            double randStartVelocity = (randomGenerator.nextDouble() * (maxVelocity + Math.abs(minVelocity)) - Math.abs(minVelocity));
            velocity = randStartVelocity;
        }

    }

    /**
     * Update the agent's velocity, threshold it, then
     * update position and threshold it.
     * @param a Should be in {0 (left), 1 (neutral), 2 (right)}
     */
    void update(int a) {
        lastAction=a;
        double variedAccel = accelerationFactor;

        velocity += ((a - 1)) * variedAccel + getSlope(position) * (gravityFactor);
        if (velocity > maxVelocity) {
            velocity = maxVelocity;
        }
        if (velocity < minVelocity) {
            velocity = minVelocity;
        }
        position += velocity;
        if (position > maxPosition) {
            position = maxPosition;
        }
        if (position < minPosition) {
            position = minPosition;
        }
        if (position == minPosition && velocity < 0) {
            velocity = 0;
        }

    }

    public int getLastAction(){
        return lastAction;
    }

    /**
     * Get the height of the hill at this position
     * @param queryPosition
     * @return
     */
    public double getHeightAtPosition(double queryPosition) {
        return -Math.sin(hillPeakFrequency * (queryPosition));
    }

    /**
     * Get the slop of the hill at this position
     * @param queryPosition
     * @return
     */
    public double getSlope(double queryPosition) {
        /*The curve is generated by cos(hillPeakFrequency(x-pi/2)) so the 
         * pseudo-derivative is cos(hillPeakFrequency* x) 
         */
        return Math.cos(hillPeakFrequency * queryPosition);
    }

    Observation makeObservation() {
        Observation currentObs = new Observation(0, 2);

        currentObs.doubleArray[0] = getPosition();
        currentObs.doubleArray[1] = getVelocity();

        return currentObs;

    }
}