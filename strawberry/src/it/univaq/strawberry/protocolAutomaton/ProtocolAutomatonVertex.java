package it.univaq.strawberry.protocolAutomaton;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import it.univaq.strawberry.StrawberryUtils;
import it.univaq.strawberry.protocolAutomaton.util.Combinatorics;
import it.univaq.strawberry.protocolAutomaton.util.OperationAndParameters;
import it.univaq.strawberry.protocolAutomaton.util.ParameterEntry;
import it.univaq.strawberry.protocolAutomaton.ProtocolAutomatonVertex;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.iface.Operation;

public class ProtocolAutomatonVertex {
	
	public WsdlInterface wsdlInterface;
	private ArrayList<ParameterEntry> parameters; //knowledge
	private ArrayList<OperationAndParameters> operationAndParameters; //sequenza di operazioni che hanno condotto fino a questo stato
	private boolean visited;
	private List<OperationAndParametersToTest> OpToTest; //operazioni ancora da testare

	public ProtocolAutomatonVertex(WsdlInterface wsdlInterface) {
		super();
		this.wsdlInterface = wsdlInterface;
		this.parameters = new ArrayList<ParameterEntry>();
		this.operationAndParameters = new ArrayList<OperationAndParameters>();
		this.visited = false;
		this.OpToTest = new ArrayList<OperationAndParametersToTest>();
	}
	
	public ProtocolAutomatonVertex(ProtocolAutomatonVertex old) {
		super();
		this.wsdlInterface = old.wsdlInterface;
		this.parameters = new ArrayList<ParameterEntry>(old.getParameters());
		this.operationAndParameters = new ArrayList<OperationAndParameters>(old.getOperationAndParameters());
		this.visited = false;
		this.OpToTest = new ArrayList<OperationAndParametersToTest>();
	}
	
	public ArrayList<ParameterEntry> getParameters() {
		return parameters;
	}
	
	public void addParameter(SchemaType schemaType, XmlObject value, boolean newOpToTest) {
		ParameterEntry newParameter = new ParameterEntry(schemaType, value);
		if (!this.parameters.contains(newParameter)) {
			this.parameters.add(newParameter);
			
			this.refreshOpToTest(newOpToTest);
		}
	}
	
	//aggiorno lo stato con una lista di tutte le operazioni che è possibile chiamare SINTATTICAMENTE
	//data la knowledge dello stato stesso
	public void refreshOpToTest (boolean newOpToTest) {
		if (newOpToTest) 
			this.OpToTest = new ArrayList<OperationAndParametersToTest>();
		List<Operation> operations = this.wsdlInterface.getOperationList();
		for (Operation operation : operations) {
			WsdlOperation wsdlOperation = wsdlInterface.getOperationByName(operation.getName());
			//è possibile chiamare l'operation da questo stato?
			if (this.canCallOperationFromKnowledge(wsdlOperation)) {
				if (StrawberryUtils.numberOfInputs(wsdlOperation) > 0) {
					Generator<ParameterEntry> matchingParams = Combinatorics.combinParams(this, wsdlOperation);
					for (ICombinatoricsVector<ParameterEntry> vector : matchingParams) {
						if (StrawberryUtils.canCallOperation(vector, wsdlOperation)) {
							OperationAndParametersToTest op = new OperationAndParametersToTest(new OperationAndParameters(wsdlOperation, vector));
							if (!this.OpToTest.contains(op)) {
								if (!newOpToTest) {
									//non vogliamo testare nuovamente operazioni già testate con dati 
									//dello stesso tipo ma con valore diverso (modificato dall'operazione di reset) 
									op.tested = true;
								}
								this.OpToTest.add(op);
							}
						}
					}
				}
				//l'operation non ha parametri in input
				else {
					OperationAndParametersToTest op = new OperationAndParametersToTest(new OperationAndParameters(wsdlOperation, null));
					if (!this.OpToTest.contains(op)) 
						this.OpToTest.add(op);
				}
			}
		}
	}
	
	public ArrayList<OperationAndParameters> getResetOperation () {
		ArrayList<OperationAndParameters> result = new ArrayList<OperationAndParameters>();
		for (OperationAndParametersToTest opToTest : this.OpToTest) {
			OperationAndParameters op = opToTest.operationAndParameters;
			if (op.getOperation().getName().equals("destroySession")) {
				opToTest.tested = true;
				result.add(op);
			}
		}
		return result;
	}
	
	public OperationAndParameters getNonTestedOp () {
		for (OperationAndParametersToTest operationAndParametersToTest : this.OpToTest) {
			if (!operationAndParametersToTest.tested &&
					!(operationAndParametersToTest.operationAndParameters.getOperation().getName().equals("destroySession"))) {
				operationAndParametersToTest.tested = true;
				return operationAndParametersToTest.operationAndParameters;
			}
		}
		return null;
	}
	
	//restituisce tutti gli schema types delle entry dello stato
	public ArrayList<SchemaType> getSchemaTypes() {
		ArrayList<SchemaType> schemaTypes = new ArrayList<SchemaType>();
		for(ParameterEntry curr : parameters) {
			schemaTypes.add(curr.schemaType);
		}
		return schemaTypes;
	}
	
	public ArrayList<OperationAndParameters> getOperationAndParameters() {
		return operationAndParameters;
	}

	public void addOperationAndParameters(OperationAndParameters operationAndParameter) {
		this.operationAndParameters.add(operationAndParameter);
	}

	//vedere se è possibile chiamare l'operation da questo stato
	public boolean canCallOperationFromKnowledge(WsdlOperation wsdlOperation) {
		ArrayList<SchemaType> schemaTypes = StrawberryUtils.getInputSchemaTypesByOperation(wsdlOperation);
		for (SchemaType curr : schemaTypes) {
			if (!existSchemaType(curr)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean existSchemaType (SchemaType schemaType) {
		for (ParameterEntry temp : parameters) {
			if (temp.schemaType.equals(schemaType)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean equals(Object vertex) {
 
		if (!this.getParameters().containsAll(((ProtocolAutomatonVertex)vertex).getParameters())) {
			return false;
		}
		if (!((ProtocolAutomatonVertex)vertex).getParameters().containsAll(this.getParameters())) {
			return false;
		}
		return true;
	}
	
	public void setAsVisited() {
		this.visited = true;
	}
	
	public boolean isVisited() {
		return this.visited;
	}
	
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("< ");
		for (ParameterEntry paramEntry : parameters) {
			//stringBuilder.append(paramEntry.schemaType.getName().getLocalPart() + " - ");
			if (paramEntry.value.xmlText().length() > 70)
				stringBuilder.append(paramEntry.value.xmlText().substring(0, 70) + " : "+ paramEntry.schemaType.getName().getLocalPart() + " | ");
			else 
				stringBuilder.append(paramEntry.value.xmlText() + " : "+ paramEntry.schemaType.getName().getLocalPart() + " | ");

			//stringBuilder.append(paramEntry.value.getDomNode().getFirstChild().getFirstChild().toString().substring(0, 13) + " : "+ paramEntry.schemaType.getName().getLocalPart() + " | ");
		}
		stringBuilder.append(" >\n");
		return stringBuilder.toString();
	}

}

class OperationAndParametersToTest {
	
	public OperationAndParameters operationAndParameters;
	public boolean tested;
	
	public OperationAndParametersToTest(
			OperationAndParameters operationAndParameters) {
		super();
		this.operationAndParameters = operationAndParameters;
		this.tested = false;
	}
	
	public boolean equals(Object op) {
	
		return this.operationAndParameters.equals(((OperationAndParametersToTest)op).operationAndParameters);
	}
}
