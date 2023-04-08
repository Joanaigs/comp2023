package pt.up.fe.comp2023;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SymbolTable implements pt.up.fe.comp.jmm.analysis.table.SymbolTable{
    private final String superName; //nome da classe que está a "extender"
    private final String className;//nome da classe
    private final List<String> imports; //imports do ficheiro
    private final Map<String, Symbol> fields;
    private final Map<String, Method> methods;
    private final List<Report> reports;

    public SymbolTable(String superName, String className, List<String> imports, Map<String, Symbol> fields, Map<String, Method> methods, List<Report> reports) {
        this.superName = superName;
        this.className = className;
        this.imports = imports;
        this.fields = fields;
        this.methods = methods;
        this.reports = reports;
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
        return this.superName;
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
        for (var importPath: imports) {
            String[] parts = importPath.split("-");
            if (parts[parts.length-1].equals(varName)) {
                return new Pair<>(new Symbol(new Type(varName, false), varName), "IMPORT");
            }
        }

        return null;
    }

    public Boolean isImported(String symbol) {
        for (var importPath: imports) {
            String[] parts = importPath.split("-");
            if (parts[parts.length-1].equals(symbol)) {
                return true;
            }
        }
        return false;
    }

    public Boolean hasMethod (String method) {
        return methods.containsKey(method);
    }

    public Method getMethod (String method) {
        return methods.get(method);
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
