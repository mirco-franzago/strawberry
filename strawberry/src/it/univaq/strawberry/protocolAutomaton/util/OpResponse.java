package it.univaq.strawberry.protocolAutomaton.util;

import org.paukov.combinatorics.ICombinatoricsVector;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.iface.Response;

public class OpResponse {

	private OperationAndParameters operationAndParameters;

	private Response response;
	
	public OpResponse(WsdlOperation operation, ICombinatoricsVector<ParameterEntry> parameterEntries,
			Response response) {
		this.operationAndParameters = new OperationAndParameters(operation, parameterEntries);
		this.response = response;
	}
	
	public OperationAndParameters getOperationAndParameters() {
		return operationAndParameters;
	}

	public ICombinatoricsVector<ParameterEntry> getParameterEntry() {
		return operationAndParameters.getParameterEntries();
	}

	public WsdlOperation getOperation() {
		return operationAndParameters.getOperation();
	}

	public Response getResponse() {
		return response;
	}
	
	
}
