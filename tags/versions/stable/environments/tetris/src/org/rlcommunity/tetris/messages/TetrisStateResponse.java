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

package org.rlcommunity.tetris.messages;

import java.util.StringTokenizer;

import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;

public class TetrisStateResponse extends AbstractResponse {
	private int tet_global_score= 0;
	private int world_width =0;
	private int world_height =0;
	private int [] world = null;
	private int currentPiece =0;
	
	public TetrisStateResponse(int score,int width, int height, int [] gs, int piece){
		this.tet_global_score =score;
		this.world_width = width;
		this.world_height = height;
		this.world = gs;	
		this.currentPiece = piece;
	}
	
	public TetrisStateResponse(String responseMessage)throws NotAnRLVizMessageException{
		
		GenericMessage theGenericResponse;
			theGenericResponse = new GenericMessage(responseMessage);


		String thePayLoadString=theGenericResponse.getPayLoad();

		StringTokenizer stateTokenizer = new StringTokenizer(thePayLoadString, ":");

		this.world_width=Integer.parseInt(stateTokenizer.nextToken());
		this.world_height=Integer.parseInt(stateTokenizer.nextToken());
		this.tet_global_score=Integer.parseInt(stateTokenizer.nextToken());
		int i =0;
		int worldSize = this.world_width*this.world_height;
		world = new int[worldSize];
		while((stateTokenizer.hasMoreTokens())&&(i<worldSize)){
			this.world[i] = Integer.parseInt(stateTokenizer.nextToken());
			++i;
		}
		this.currentPiece = Integer.parseInt(stateTokenizer.nextToken());
	}
	
	@Override
	public String makeStringResponse() {
		
		StringBuffer theResponseBuffer= new StringBuffer();
		theResponseBuffer.append("TO=");
		theResponseBuffer.append(MessageUser.kBenchmark.id());
		theResponseBuffer.append(" FROM=");
		theResponseBuffer.append(MessageUser.kEnv.id());
		theResponseBuffer.append(" CMD=");
		theResponseBuffer.append(EnvMessageType.kEnvResponse.id());
		theResponseBuffer.append(" VALTYPE=");
		theResponseBuffer.append(MessageValueType.kStringList.id());
		theResponseBuffer.append(" VALS=");

		theResponseBuffer.append(this.world_width);
		theResponseBuffer.append(":");
		theResponseBuffer.append(this.world_height);
		theResponseBuffer.append(":");
		theResponseBuffer.append(this.tet_global_score);
		theResponseBuffer.append(":");
		for(int i = 0; i < this.world.length; i++){
		theResponseBuffer.append(":");
		theResponseBuffer.append(world[i]);
		}
		theResponseBuffer.append(":");
		theResponseBuffer.append(this.currentPiece);

		return theResponseBuffer.toString();
	}
	
	public int getScore(){
		return this.tet_global_score;
	}
	public int getWidth(){
		return this.world_width;
	}
	public int getHeight(){
		return this.world_height;
	}
	public int [] getWorld(){
		return this.world;
	}
	
	public int getCurrentPiece(){
		return currentPiece;
	}


}
