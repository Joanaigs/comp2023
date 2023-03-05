package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.LinkedList;

import java.util.List;
import java.util.Map;

public class SymbolTableGenerator extends AJmmVisitor<String, String> {
    private List<String> imports= new LinkedList<>();
    private String _super=null;
    private String className= "";//nome da classe
    private Map<String, Symbol> fields = new HashMap<String, Symbol>();
    private Map<String, Method> methods = new HashMap<String, Method>();
    protected void buildVisitor() {
        addVisit ("Program", this::dealWithProgram );
        addVisit ("ImportDeclaration", this::importDeclaration );
        addVisit ("ClassDeclaration", this::classDeclaration);
        addVisit ("VarDeclaration", this::varDeclaration);
        addVisit ("MethodDeclaration", this::methodDeclaration);
    }

    private String dealWithProgram ( JmmNode jmmNode, String s) {
        for (JmmNode child: jmmNode.getChildren()){
            visit(child , null);
        }
        return null;
    }

    private String importDeclaration ( JmmNode jmmNode , String s) {
        imports.add(jmmNode.get("library"));
        return null;
    }

    private String classDeclaration ( JmmNode jmmNode , String s) {
        className=jmmNode.get("className");
        if(jmmNode.hasAttribute("extendsClass"))
            _super= jmmNode.get("extendsClass");
        for (JmmNode child: jmmNode.getChildren()){
            visit(child , "classFields");
        }
        return null;
    }

    private String methodDeclaration ( JmmNode jmmNode , String s) {
        String methodName=jmmNode.get("methodName");


        return null;
    }

    private String varDeclaration ( JmmNode jmmNode , String s) {
        String nameType =jmmNode.getJmmChild(0).get("typeDeclaration");
        Boolean isArray=false;
        if(jmmNode.getJmmChild(0).hasAttribute("array"))
            isArray=true;
        Type type= new Type(nameType, isArray);
        Symbol symbol= new Symbol(type, jmmNode.get("name"));
        if(s=="classFields"){
            fields.put(jmmNode.get("name"), symbol);
        }
        return null;
    }



}
