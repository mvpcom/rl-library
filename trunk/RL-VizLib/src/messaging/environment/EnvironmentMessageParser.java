package messaging.environment;

import messaging.GenericMessage;
import messaging.GenericMessageParser;
import messaging.MessageUser;

public class EnvironmentMessageParser extends GenericMessageParser{
	public static EnvironmentMessages parseMessage(String theMessage){
		GenericMessage theGenericMessage=new GenericMessage(theMessage);

		int cmdId=theGenericMessage.getTheMessageType();

		if(cmdId==EnvMessageType.kEnvQueryVarRanges.id()) 				return new EnvRangeRequest(theGenericMessage);
		if(cmdId==EnvMessageType.kEnvQueryObservationsForState.id()) 	return new EnvObsForStateRequest(theGenericMessage);
		if(cmdId==EnvMessageType.kEnvCustom.id())						return new EnvCustomRequest(theGenericMessage);


		System.out.println("EnvironmentMessageParser - unknown query type: "+theMessage);
		Thread.dumpStack();
		System.exit(1);
		return null;
	}
}
