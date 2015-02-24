package it.univaq.strawberry.protocolAutomaton.util;

import java.util.ArrayList;
import java.util.List;

import org.paukov.combinatorics.ICombinatoricsVector;

import com.eviware.soapui.impl.wsdl.WsdlOperation;

//serve per richiamare le operazioni in sequenza dopo una chiamata "di reset" (vogliamo "ricostruire" lo stato interno del servizio)
public class OperationAndParameters {
	private WsdlOperation operation;
	private ICombinatoricsVector<ParameterEntry> parameterEntries;
	
	public OperationAndParameters(
			WsdlOperation operation,
			ICombinatoricsVector<ParameterEntry> parameterEntries) {
		super();
		this.parameterEntries = parameterEntries;
		this.operation = operation;
	}

	public ICombinatoricsVector<ParameterEntry> getParameterEntries() {
		return parameterEntries;
	}

	public WsdlOperation getOperation() {
		return operation;
	}
	
	public boolean equals (Object oAp) {
		
		if (!(this.operation.getName().equals(((OperationAndParameters)oAp).getOperation().getName()))) {
			return false;
		}
		
		if (this.parameterEntries == null &&
				((OperationAndParameters)oAp).getParameterEntries() == null) {
			return true;
		}
		
		if (this.parameterEntries == null ||
				((OperationAndParameters)oAp).getParameterEntries() == null) {
			return false;
		}
		
		List<ParameterEntry> list1 = this.parameterEntries.getVector();
		List<ParameterEntry> list2 = ((OperationAndParameters)oAp).getParameterEntries().getVector();
		
		if (list1.size() != list2.size()) 
			return false;
		
		for (int i=0; i < list1.size(); i++) {
			if (!(list1.get(i).equals(list2.get(i)))) {
				return false;
			}
		}
		
		return true;
	}
	
}
