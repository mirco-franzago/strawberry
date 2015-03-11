package it.univaq.strawberry.protocolAutomaton.util;

import it.univaq.strawberry.protocolAutomaton.util.SideEffect.SideEffectType;

import java.util.ArrayList;

public class OperationSideEffect {
	
	private static ArrayList<SideEffect> sideEffects = new ArrayList<SideEffect>();

	public void add(String operationName, SideEffectType sideEffectType, String[] parametersToRemove) {
		sideEffects.add(new SideEffect(operationName, sideEffectType, parametersToRemove));
	}
	
	public void add(String operationName, SideEffectType sideEffectType) {
		sideEffects.add(new SideEffect(operationName, sideEffectType));
	}
	
	public static boolean isIgnore(String operationName) {
		for (SideEffect sideEffect : sideEffects) {
			if (sideEffect.operationName.equals(operationName) &&
					sideEffect.sideEffectType == SideEffectType.IGNORE) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isRemove(String operationName) {
		for (SideEffect sideEffect : sideEffects) {
			if (sideEffect.operationName.equals(operationName) &&
					sideEffect.sideEffectType == SideEffectType.REMOVE) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isReset(String operationName) {
		for (SideEffect sideEffect : sideEffects) {
			if (sideEffect.operationName.equals(operationName) &&
					sideEffect.sideEffectType == SideEffectType.RESET) {
				return true;
			}
		}
		return false;
	}
	
	//è NORMAL se non è presente oppure se è diversa da NORMAL
	public static boolean isNormal(String operationName) {
		for (SideEffect sideEffect : sideEffects) {
			if (sideEffect.operationName.equals(operationName)) {
				if (sideEffect.sideEffectType == SideEffectType.NORMAL)
					return true;
				else return false;
			}
		}
		return true;
	}
	
}
