package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SymbolTable implements pt.up.fe.comp.jmm.analysis.table.SymbolTable{
    final String superName; //nome da classe que está a "extender"
    final String className;//nome da classe
    final List<String> imports; //imports do ficheiro
    final Map<String, Symbol> fields;
    final Map<String, Method> methods;

    public SymbolTable(String superName, String className, List<String> imports, Map<String, Symbol> fields, Map<String, Method> methods) {
        this.superName = superName;
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
}
