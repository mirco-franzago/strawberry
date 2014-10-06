package it.univaq.strawberry;

import java.util.ArrayList;

import org.w3c.dom.Document;

import com.eviware.soapui.impl.wsdl.WsdlOperation;

public class InstancePoolEntry {

	WsdlOperation operation;
	ArrayList<Document> instanceInputs;
	
	public InstancePoolEntry(WsdlOperation operation, ArrayList<Document> instanceInputs) {
		super();
		this.operation = operation;
		this.instanceInputs = instanceInputs;
	}
	
	
}
