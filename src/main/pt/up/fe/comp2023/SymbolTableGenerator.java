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
        addVisit ("InstanceMethodDeclaration", this::instanceMethodDeclaration);
        addVisit ("MainMethodDeclaration", this::mainMethodDeclaration);
        addVisit ("FieldDeclaration", this::fieldDeclaration);
        setDefaultVisit(this::ignore);
    }
    private String ignore ( JmmNode jmmNode, String s) {
        return null;
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
            visit(child , null);
        }
        return null;
    }

    private String methodDeclaration ( JmmNode jmmNode , String s) {
        for (JmmNode child: jmmNode.getChildren()){
            visit(child , null);
        }

        return null;
    }

    private String instanceMethodDeclaration ( JmmNode jmmNode , String s) {
        String nameType =jmmNode.getJmmChild(0).get("typeDeclaration");
        Boolean isArray=false;
        if(jmmNode.getJmmChild(0).getObject("isArray").equals(true))
            isArray=true;
        Method method = new Method(jmmNode.get("methodName"), new Type(nameType, isArray));
        methods.put(jmmNode.get("methodName"), method);
        for (JmmNode child: jmmNode.getChildren()) {
            visit(child, jmmNode.get("methodName"));
        }
        return null;
    }

    private String fieldDeclaration ( JmmNode jmmNode , String s) {
        String nameType =jmmNode.getJmmChild(0).get("typeDeclaration");
        Boolean isArray=false;
        if(jmmNode.getJmmChild(0).getObject("isArray").equals(true))
            isArray=true;
        Type type= new Type(nameType, isArray);
        Method method = methods.get(s);
        SymbolExtended symbol = new SymbolExtended(type, jmmNode.get("name"), false);
        method.addVariable(symbol);
        methods.put(s, method);
        return null;
    }

    private String mainMethodDeclaration ( JmmNode jmmNode , String s) {

        Method method = new Method("main", new Type("void", false));
        SymbolExtended symbol = new SymbolExtended(new Type("String", true), jmmNode.get("var"), false );
        method.addVariable(symbol);
        methods.put("main", method);
        for (JmmNode child: jmmNode.getChildren()) {
            visit(child, "main");
        }
        return null;
    }

    private String varDeclaration ( JmmNode jmmNode , String s) {
        String nameType =jmmNode.getJmmChild(0).get("typeDeclaration");
        Boolean isArray=false;
        if(jmmNode.getJmmChild(0).getObject("isArray").equals(true))
            isArray=true;
        Type type= new Type(nameType, isArray);
        if(s==null){
            Symbol symbol= new Symbol(type, jmmNode.get("name"));
            fields.put(jmmNode.get("name"), symbol);
        }
        else{
            Method method = methods.get(s);
            SymbolExtended symbolExtended = new SymbolExtended(type, jmmNode.get("name"), true );
            method.addVariable(symbolExtended);
            methods.put(s, method);
        }
        return null;
    }


    public List<String> getImports() {
        return imports;
    }

    public String get_super() {
        return _super;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, Symbol> getFields() {
        return fields;
    }

    public Map<String, Method> getMethods() {
        return methods;
    }
}
