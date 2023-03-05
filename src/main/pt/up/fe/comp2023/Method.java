package pt.up.fe.comp2023;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Method {
    private String name;
    private Type returnType;
    private List<SymbolExtended> variables; //true if local variable false if parameter

    public Method() {
        this(null, null, null);
    }

    public Method(String name, Type returnType) {
        this(name, returnType, null);
    }

    public Method(String name, Type returnType, List<SymbolExtended> variables) {
        this.name = name;
        this.returnType = returnType;
        this.variables = variables;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<SymbolExtended> getParameters() {
        List<SymbolExtended> parameters = null;
        for(SymbolExtended symbol: this.variables){
            if(!symbol.getLocal()){
                parameters.add(symbol);
            }
        }
        return parameters;
    }

    public List<SymbolExtended> getLocalVariables() {
        List<SymbolExtended> parameters = null;
        for(SymbolExtended symbol: this.variables){
            if(symbol.getLocal()){
                parameters.add(symbol);
            }
        }
        return parameters;
    }

    public List<SymbolExtended> getVariables() {
        return variables;
    }

    public void addVariable(SymbolExtended variable) {
        this.variables.add(variable);
    }
}
