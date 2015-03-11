package it.univaq.strawberry.protocolAutomaton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.paukov.combinatorics.Generator;
import org.w3c.dom.Node;

import it.univaq.strawberry.StrawberryUtils;
import it.univaq.strawberry.protocolAutomaton.util.Combinatorics;
import it.univaq.strawberry.protocolAutomaton.util.OpResponse;
import it.univaq.strawberry.protocolAutomaton.util.OperationAndParameters;
import it.univaq.strawberry.protocolAutomaton.util.ParameterEntry;
import it.univaq.strawberry.protocolAutomaton.ProtocolAutomatonEdge;
import it.univaq.strawberry.protocolAutomaton.ProtocolAutomatonVertex;

import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.MessagePart.ContentPart;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;

public class ProtocolAutomaton extends AbstractBaseGraph<ProtocolAutomatonVertex, ProtocolAutomatonEdge>
								implements DirectedGraph<ProtocolAutomatonVertex, ProtocolAutomatonEdge> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2801741081358753349L;
	
	private ProtocolAutomatonVertex root;
	private boolean flattening;
	private int numberOfStates = 0;
	
	public ProtocolAutomatonVertex getRoot() {
		return root;
	}

	public ProtocolAutomaton(ProtocolAutomatonVertex root, boolean flattening) {
		super(new ClassBasedEdgeFactory<ProtocolAutomatonVertex, ProtocolAutomatonEdge>(
				ProtocolAutomatonEdge.class), true, true);
		
		this.root = root;
		//insert the root node that contains the instance pool
		this.addVertex(root);	
		
		this.flattening = flattening;
	}
	
//	public void automatonConstructionBaseStep(WsdlInterface wsdlInterface) {
//		this.automatonConstructionStep(wsdlInterface, this.root);
//	}
	
	public ProtocolAutomatonVertex automatonConstructionStep(ProtocolAutomatonVertex sourceVertex, OperationAndParameters operationAndParameters) {
	
		WsdlOperation wsdlOperation = operationAndParameters.getOperation();
		//l'operation ha uno o piu' parametri in input
		OpResponse opResponse;
		if (StrawberryUtils.numberOfInputs(wsdlOperation) > 0) {
			opResponse = StrawberryUtils.requestWithInputs(operationAndParameters);
		}
		//l'operation non ha parametri in input
		else {
			opResponse = StrawberryUtils.requestWithoutInputs(operationAndParameters);
		}

		ProtocolAutomatonVertex targetVertex = this.addNewVertex(opResponse, sourceVertex);
		if (targetVertex != null) {
			ProtocolAutomatonEdge newEdge = new ProtocolAutomatonEdge(wsdlOperation, opResponse.getParameterEntry());
			if (!this.existEdge(newEdge, sourceVertex, targetVertex)) 
				this.addEdge(sourceVertex, targetVertex, newEdge);
		}
		return targetVertex;
	}
	
	public void automatonResetStep(ProtocolAutomatonVertex sourceVertex, OperationAndParameters operationAndParameters) {
		
		if (operationAndParameters == null) return; 
		
		WsdlOperation wsdlOperation = operationAndParameters.getOperation();
		//l'operation ha uno o più parametri in input
		OpResponse opResponse;
		if (StrawberryUtils.numberOfInputs(wsdlOperation) > 0) {
			opResponse = StrawberryUtils.requestWithInputs(operationAndParameters);
		}
		//l'operation non ha parametri in input
		else {
			opResponse = StrawberryUtils.requestWithoutInputs(operationAndParameters);
		}
			
		Response response = opResponse.getResponse();	
		if (response != null && 
				!StrawberryUtils.isFaultResponse(response, wsdlOperation)) {
			ProtocolAutomatonEdge newEdge = new ProtocolAutomatonEdge(wsdlOperation, opResponse.getParameterEntry());
			if (!this.existEdge(newEdge, sourceVertex, this.root)) 
				this.addEdge(sourceVertex, this.root, newEdge);
		}
	}
	
	public void restart(ProtocolAutomatonVertex vertex) {
		//TODO durante l'operazione di 'restart' assumo che la sequenza di operazione la posso rieseguire correttamente
		//in generale si dovrebbe togliere l'arco se una operazione non va a buon fine quando sarebbe dovuta terminare con successo
		
		ArrayList<OperationAndParameters> newOperationAndParameters = new ArrayList<OperationAndParameters>(); //contiene le nuove operazioni prodotte durante l'operazione di restart
		for (OperationAndParameters op : vertex.getOperationAndParameters()) {
			OpResponse opResponse;
			if (op.getParameterEntries() != null) {
				opResponse = StrawberryUtils.requestWithInputs(op);
			}
			else {
				opResponse = StrawberryUtils.requestWithoutInputs(op);
			}
			
			//aggiorniamo la knowledge dello stato con evenutuali nuovi dati derivanti dalle nuove chiamate
			Response response = opResponse.getResponse();
			WsdlOperation wsdlOperation = op.getOperation();
			if (response != null && 
					!StrawberryUtils.isFaultResponse(response, wsdlOperation)) {
				
				checkRestartedOperation(vertex, wsdlOperation, response, newOperationAndParameters);
			}
			//l'operazione non è andata a buon fine, mentre la prima volta si, quindi pesco nella knowledge "arricchita" dello stato corrente e provo a richiamarla
			else {
				//TODO una chiamata va male dopo essere andata bene alla prima esecuzione
 				System.out.println("todo");
			}
		}
	}
	
	private void checkRestartedOperation (ProtocolAutomatonVertex vertex, WsdlOperation wsdlOperation, Response response, ArrayList<OperationAndParameters> newOperationAndParameters) {
		//TODO assumiamo per ora che sia solo "additivo" (tutte le operazioni arricchiscono la knowledge base)
		MessagePart[] messageParts = wsdlOperation.getDefaultResponseParts();
		//scorro gli output dell'operazione
		for (int i = 0; i < messageParts.length; i++) {
			if (messageParts[i] instanceof ContentPart) {
				SchemaType schemaType = ((ContentPart) messageParts[i]).getSchemaType();
				String outputPartName = ((ContentPart) messageParts[i]).getName();
				XmlObject xmlObject = StrawberryUtils.getNodeFromResponse(response, outputPartName);
				if (xmlObject != null) {
					//aggiungo alla knowledge base attuale
					//vertex.addParameter(outputPartName, schemaType, xmlObject, false);
					
					//vertex.addParameter(findName(outputPartName, wsdlOperation.getName()), schemaType, xmlObject, false);
					String mainTypeName = findName(outputPartName, schemaType.getName().getLocalPart());
					vertex.addParameter(mainTypeName, schemaType, xmlObject, mainTypeName, false);
					
					if (this.flattening) {
						ArrayList<SchemaProperty> schemaProperties = StrawberryUtils.getAllSubSchemaTypes(schemaType);
						for (SchemaProperty schemaProperty : schemaProperties) {
							SchemaType schemaTypeCurr = schemaProperty.getType();
							String schemaNameCurr = schemaProperty.getName().getLocalPart();
							ArrayList<XmlObject> xmlObjects = StrawberryUtils.getNodesFromResponse(response, schemaNameCurr);
							for (XmlObject xmObjectCurr : xmlObjects) {
								if (xmObjectCurr != null) {
									vertex.addParameter(schemaNameCurr, schemaTypeCurr, xmObjectCurr, mainTypeName, false);
								}
							}
						}
					}
				}
			}
		}
	}
	
//	public void automatonConstructionStep(WsdlInterface wsdlInterface, ProtocolAutomatonVertex sourceVertex) {
//		
//		List<Operation> operations = wsdlInterface.getOperationList();
//		for (Operation operation : operations) {
//			WsdlOperation wsdlOperation = wsdlInterface.getOperationByName(operation.getName());
//			//è possibile chiamare l'operation da questo stato?
//			if (sourceVertex.canCallOperationFromKnowledge(wsdlOperation)) {
//				//l'operation ha uno o più parametri in input
//				ArrayList<OpResponse> opResponses;
//				if (StrawberryUtils.numberOfInputs(wsdlOperation) > 0) {
//					Generator<ParameterEntry> matchingParams = Combinatorics.combinParams(sourceVertex, wsdlOperation);
//					opResponses = StrawberryUtils.requestsWithInputs(matchingParams, wsdlOperation);
//				}
//				//l'operation non ha parametri in input
//				else {
//					opResponses = StrawberryUtils.requestWithoutInputs(wsdlOperation);
//				}
//				for (OpResponse opResponse : opResponses) {
//					ProtocolAutomatonVertex targetVertex = this.addNewVertex(opResponse, sourceVertex);
//					if (targetVertex != null) {
//						this.addEdge(sourceVertex, targetVertex, new ProtocolAutomatonEdge(wsdlOperation, opResponse.getParameterEntry()));
//					}
//				}
//			}
//		}
//		sourceVertex.setAsVisited();
//	}
	
	//se la risposta è positiva, possiamo aggiungere un nuovo stato (se non esiste già)
	private ProtocolAutomatonVertex addNewVertex (OpResponse opResponse, ProtocolAutomatonVertex sourceVertex) {

		Response response = opResponse.getResponse();
		WsdlOperation wsdlOperation = opResponse.getOperation();
		
		if (response != null) {
			if (!StrawberryUtils.isFaultResponse(response, wsdlOperation)) {
				System.err.println("non fault: " + wsdlOperation.getName());
				//la richiesta è andata a buon fine, possiamo aggiungere lo stato e la transizione
				ProtocolAutomatonVertex targetVertex = new ProtocolAutomatonVertex(sourceVertex);
				//TODO assumiamo per ora che sia solo "additivo" (tutte le operazioni arricchiscono la knowledge base
				MessagePart[] messageParts = wsdlOperation.getDefaultResponseParts();
				//scorro gli output dell'operazione
				for (int i = 0; i < messageParts.length; i++) {
					if (messageParts[i] instanceof ContentPart) {
						SchemaType schemaType = ((ContentPart) messageParts[i]).getSchemaType();
						
						String outputPartName = ((ContentPart) messageParts[i]).getName();
						XmlObject xmlObject = StrawberryUtils.getNodeFromResponse(response, outputPartName);
						if (xmlObject != null) {
							//aggiungo alla knowledge base attuale
							//targetVertex.addParameter(outputPartName, schemaType, xmlObject, true);
							//targetVertex.addOperationAndParameters(opResponse.getOperationAndParameters());
							
							//targetVertex.addParameter(findName(outputPartName, wsdlOperation.getName()), schemaType, xmlObject, true);
							String mainTypeName = findName(outputPartName, schemaType.getName().getLocalPart());
							targetVertex.addParameter(mainTypeName, schemaType, xmlObject, mainTypeName, true);
							
							if (this.flattening) {
								ArrayList<SchemaProperty> schemaProperties = StrawberryUtils.getAllSubSchemaTypes(schemaType);
								for (SchemaProperty schemaProperty : schemaProperties) {
									SchemaType schemaTypeCurr = schemaProperty.getType();
									String schemaNameCurr = schemaProperty.getName().getLocalPart();
									ArrayList<XmlObject> xmlObjects = StrawberryUtils.getNodesFromResponse(response, schemaNameCurr);
									for (XmlObject xmlObjectCurr : xmlObjects) {
										if (xmlObjectCurr != null) {
											targetVertex.addParameter(schemaNameCurr, schemaTypeCurr, xmlObjectCurr, mainTypeName, true);
										}
									}
								}
							}
						}
					}
				}

				//TODO qui il codice con le condizioni di aggiunta di un nuovo stato, come è ora aggiungo sempre se incremento la knowledge
				if (sourceVertex.addedParameter(targetVertex)) {
					ProtocolAutomatonVertex temp = this.getVertex(targetVertex);
					if (temp == null) {
						targetVertex.addOperationAndParameters(opResponse.getOperationAndParameters());
						this.addVertex(targetVertex);
						numberOfStates++;
						System.err.println("Aggiunto lo stato #" + numberOfStates);
						return targetVertex;
					}
					else {
						//lo stato già esiste nel grafo
						//sourceVertex.setOpToTest(targetVertex.getOpToTest());
						return temp;
					}
				}
				sourceVertex.addOperationAndParameters(opResponse.getOperationAndParameters());
				return sourceVertex;
			}
		}
		return null;
	}
	
	//qui bisogna verificare se il nome del parametro può essere sostituito con uno di quelli presenti nelle informazioni "semantiche" aggiuntive, 
	//ovvero se il valore matcha con una delle espressioni regolari
	public String findName (String name, String wsdlOperationName) {
		if (name.equals("return")) return "return_" + wsdlOperationName;
		else return name;
	}
	
	//TODO forse bisogna raffinare le condizioni di equivalenza fra archi (influisce sull'aggiunta o meno di un nuovo arco
	public boolean existEdge(ProtocolAutomatonEdge edge, ProtocolAutomatonVertex sourceVertex, ProtocolAutomatonVertex targetVertex) {
		Set<ProtocolAutomatonEdge> set = this.getAllEdges(sourceVertex, targetVertex);
		for (ProtocolAutomatonEdge currEdge : set) {
			if (edge.operation.getName().equals(currEdge.operation.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean existVertex (ProtocolAutomatonVertex vertex) {
		Set<ProtocolAutomatonVertex> set = this.vertexSet();
		for (ProtocolAutomatonVertex currVertex : set) {
			if (currVertex.equals(vertex)) {
				return true;
			}
		}
		return false;
	}
	
	public ProtocolAutomatonVertex getVertex (ProtocolAutomatonVertex vertex) {
		Set<ProtocolAutomatonVertex> set = this.vertexSet();
		Iterator<ProtocolAutomatonVertex> iterator = set.iterator();
		while (iterator.hasNext()) {
			ProtocolAutomatonVertex curr = iterator.next();
			if (curr.equals(vertex))
				return curr; 
		}
		return null;
	}
	
//	public ProtocolAutomatonVertex getNextNonVisited () {
//		Set<ProtocolAutomatonVertex> set = this.vertexSet();
//		for (ProtocolAutomatonVertex vertex : set) {
//			if (!vertex.isVisited()) {
//				return vertex;
//			}
//		}
//		return null;
//	}

}
