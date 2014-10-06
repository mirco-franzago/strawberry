package it.univaq.strawberry.protocolAutomaton.util;

public class OperationSideEffect {
	
	String name;
	SideEffect sideEffect;
	
	public enum SideEffect {
		ADD, REMOVE, EDIT, RESET
	}

	public OperationSideEffect(String name, SideEffect sideEffect) {
		super();
		this.name = name;
		this.sideEffect = sideEffect;
	}
	
}
