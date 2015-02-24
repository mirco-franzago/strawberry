package it.univaq.strawberry.protocolAutomaton.util;

import java.util.ArrayList;

import org.apache.xmlbeans.SchemaType;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import it.univaq.strawberry.StrawberryUtils;
import it.univaq.strawberry.protocolAutomaton.util.ParameterEntry;
import it.univaq.strawberry.protocolAutomaton.ProtocolAutomatonVertex;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.iface.Operation;

public class Combinatorics {

	//restituisce tutte le entry dello stato corrente i cui schematype matchano con quelli in input dell'operazione
	private static ArrayList<ParameterEntry> matchingParams(ArrayList<ParameterEntry> sourceVertexParameters, WsdlOperation wsdlOperation) {
		
		ArrayList<SchemaType> operationSchemaTypes = StrawberryUtils.getInputSchemaTypesByOperation(wsdlOperation);
		
		ArrayList<ParameterEntry> matchingParameters = new ArrayList<ParameterEntry>();
		
		for (ParameterEntry curr : sourceVertexParameters) {
			if (operationSchemaTypes.contains(curr.schemaType)) {
				matchingParameters.add(curr);
			}
		}
		return matchingParameters;
	}

	/* genera le possibili permutazioni */
	public static Generator<ParameterEntry> combinParams (ArrayList<ParameterEntry> sourceVertexParameters, WsdlOperation wsdlOperation) {
		
		ArrayList<ParameterEntry> matchingParameters = matchingParams(sourceVertexParameters, wsdlOperation);
		
		//Create the initial vector
	   ICombinatoricsVector<ParameterEntry> originalVector = Factory.createVector(matchingParameters);

	   // Create the generator by calling the appropriate method in the Factory class. 
	   // Set the second parameter as n, since we will generate n-elemets permutations
	   Generator<ParameterEntry> gen = Factory.createPermutationWithRepetitionGenerator(originalVector, StrawberryUtils.numberOfInputs(wsdlOperation));

//	   for (ICombinatoricsVector<String> perm : gen)
//	      System.out.println( perm );
	   return gen;
	}
	
}
