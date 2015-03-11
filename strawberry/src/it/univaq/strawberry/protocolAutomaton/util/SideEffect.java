package it.univaq.strawberry.protocolAutomaton.util;

import java.util.ArrayList;

public class SideEffect {
	
	String operationName;
	SideEffectType sideEffectType;
	String[] parametersToRemove;
	
	//NORMAL = ADD or EDIT
	public enum SideEffectType {
		NORMAL, REMOVE, IGNORE, RESET
	}

	//usare questo con side effect REMOVE
	public SideEffect(String operationName, SideEffectType sideEffectType, String[] parametersToRemove) {
		this.operationName = operationName;
		this.sideEffectType = sideEffectType;
		this.parametersToRemove = parametersToRemove;
	}
	
	public SideEffect(String operationName, SideEffectType sideEffectType) {
		this.operationName = operationName;
		this.sideEffectType = sideEffectType;
		this.parametersToRemove = null;
	}

}