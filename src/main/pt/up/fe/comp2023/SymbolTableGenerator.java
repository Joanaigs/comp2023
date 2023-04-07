package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.HashMap;
import java.util.LinkedList;

import java.util.List;
import java.util.Map;

public class SymbolTableGenerator extends AJmmVisitor<String, String> {
    private List<String> imports = new LinkedList<>();
    private String superName=null;
    private String className= "";//nome da classe
    private Map<String, Symbol> fields = new HashMap<String, Symbol>();
    private Map<String, Method> methods = new HashMap<String, Method>();
    private List<Report> reports = new LinkedList<>();;

    public SymbolTable build(pt.up.fe.comp.jmm.ast.JmmNode root_node) {
        this.visit(root_node, null);
        return new SymbolTable(superName, className, imports, fields, methods, reports);
    }
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
        String imp= "";
        var lib =  (List<?>) jmmNode.getObject("library");
        for (int i =0; i<lib.size()-1; i++){
            imp+= lib.get(i)+ ".";
        }
        imports.add(imp+lib.get(lib.size()-1));
        return null;
    }

    private String classDeclaration ( JmmNode jmmNode , String s) {
        className=jmmNode.get("className");
        if(jmmNode.hasAttribute("extendsClass"))
            superName= jmmNode.get("extendsClass");
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
        String methodName = jmmNode.get("methodName");
        if (methods.containsKey(methodName)) {
            String reportMessage = "Method already declared - " + methodName;
            addReport(jmmNode, reportMessage);
            return null;
        }
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
        String nameType = jmmNode.getJmmChild(0).get("typeDeclaration");
        Boolean isArray=false;
        if(jmmNode.getJmmChild(0).getObject("isArray").equals(true))
            isArray=true;
        Type type= new Type(nameType, isArray);
        Method method = methods.get(s);
        SymbolExtended symbol = new SymbolExtended(type, jmmNode.get("name"), false);
        if(checkVariableAlreadyDeclared(jmmNode, symbol, method))
            return null;
        method.addVariable(symbol);
        methods.put(s, method);
        return null;
    }

    private String mainMethodDeclaration ( JmmNode jmmNode , String s) {
        if (methods.containsKey("main")) {
            String reportMessage = "Method already defined: main";
            addReport(jmmNode, reportMessage);
            return null;
        }
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
            Symbol symbol = new Symbol(type, jmmNode.get("name"));
            fields.put(jmmNode.get("name"), symbol);
        }
        else{
            Method method = methods.get(s);
            SymbolExtended symbolExtended = new SymbolExtended(type, jmmNode.get("name"), true );
            if(checkVariableAlreadyDeclared(jmmNode, symbolExtended, method))
                return null;
            method.addVariable(symbolExtended);
            methods.put(s, method);
        }
        return null;
    }

    private boolean checkVariableAlreadyDeclared(JmmNode node, SymbolExtended symbol, Method method) {
        String reportMessage = "Field already declared in " + method.getName();
        if(method.hasVariable(symbol)){
            addReport(node, reportMessage);
            return true;
        }
        return false;
    }

    private void addReport(JmmNode node, String message) {
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), message));
    }
}
