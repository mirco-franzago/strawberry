package it.univaq.strawberry.protocolAutomaton.util;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.eviware.soapui.support.xml.XmlUtils;

public class ParameterEntry {
	public String name;
	public SchemaType schemaType;
	public XmlObject value;
	
	public SchemaType getSchemaType() {
		return schemaType;
	}

	public XmlObject getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setValue(XmlObject value) {
		this.value = value;
	}

	public ParameterEntry(String name, SchemaType schemaType, XmlObject value) {
		this.name = name;
		this.schemaType = schemaType;
		this.value = value;
	}
	
	public boolean equals (Object parameterEntry) {
		boolean result;
		result =  this.schemaType.equals(((ParameterEntry)parameterEntry).schemaType) &&
				//XmlUtils.serialize(this.value).equals(XmlUtils.serialize(parameterEntry.value));
				//this.value.valueEquals(((ParameterEntry)parameterEntry).value);
				//this.value.toString().equals(((ParameterEntry)parameterEntry).value.toString()) &&
				this.name.equals(((ParameterEntry)parameterEntry).name);
		return result;
	}
}
