package pt.up.fe.comp2023;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SymbolTable implements pt.up.fe.comp.jmm.analysis.table.SymbolTable{
    private String _super; //nome da classe que está a "extender"
    private String className;//nome da classe
    private List<String> imports; //imports do ficheiro
    private Map<String, Symbol> fields;
    private Map<String, Method> methods;

    public SymbolTable(String _super, String className, List<String> imports, Map<String, Symbol> fields, Map<String, Method> methods) {
        this._super = _super;
        this.className = className;
        this.imports = imports;
        this.fields = fields;
        this.methods = methods;
    }


    @Override
    public List<String> getImports() {
        return this.imports;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return this._super;
    }

    @Override
    public List<Symbol> getFields() { return new LinkedList<>(fields.values()); }

    @Override
    public List<String> getMethods() { return new LinkedList<>(methods.keySet()); }

    @Override
    public Type getReturnType(String s) {
        return methods.get(s).getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return new LinkedList<>(methods.get(s).getParameters());
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return new LinkedList<>(methods.get(s).getLocalVariables());
    }


    public Pair<Symbol, String> getSymbol(String methodName, String varName) {
        for(Symbol symbol:fields.values()){
            if(symbol.getName().equals(varName)){
                return new Pair<>(symbol, "FIELD");
            }
        }
        for(Symbol symbol:new LinkedList<>(methods.get(methodName).getParameters())){
            if(symbol.getName().equals(varName)){
                return new Pair<>(symbol, "PARAM");
            }
        }
        for(Symbol symbol:new LinkedList<>(methods.get(methodName).getLocalVariables())){
            if(symbol.getName().equals(varName)){
                return new Pair<>(symbol, "LOCAL");
            }
        }
        return null;
    }
    public int getSymbolIndex(String methodName, String varName) {
        LinkedList<SymbolExtended> Parameters = new LinkedList<>(methods.get(methodName).getParameters());
        for (int i = 0; i < Parameters.size(); i++) {
            SymbolExtended symbol = Parameters.get(i);
            if (symbol.getName().equals(varName)) {
                return i+1;
            }
        }
        return 0;
    }
}
