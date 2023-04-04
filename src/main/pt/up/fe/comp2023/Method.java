package pt.up.fe.comp2023;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class Method {
    private String name;
    private Type returnType;
    private List<SymbolExtended> variables; //true if local variable false if parameter

    public List<SymbolExtended> getVariables() {
        return variables;
    }

    public Method(String name, Type returnType) {
        this.name = name;
        this.returnType = returnType;
        this.variables= new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<SymbolExtended> getParameters() {
        List<SymbolExtended> parameters = new LinkedList<>();
        for(SymbolExtended symbol: this.variables){
            if(!symbol.getLocal()){
                parameters.add(symbol);
            }
        }
        return parameters;
    }

    public List<SymbolExtended> getLocalVariables() {
        List<SymbolExtended> parameters = new LinkedList<>();
        for(SymbolExtended symbol: this.variables){
            if(symbol.getLocal()){
                parameters.add(symbol);
            }
        }
        return parameters;
    }

    public void addVariable(SymbolExtended variable) {
        this.variables.add(variable);
    }
}
