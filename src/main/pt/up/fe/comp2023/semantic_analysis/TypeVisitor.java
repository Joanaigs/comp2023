package pt.up.fe.comp2023.semantic_analysis;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SymbolTable;

import java.util.List;


public class TypeVisitor extends PostorderJmmVisitor<String, String> implements AnalyserVisitor{
    private final SymbolTable symbolTable;

    private final Utils utils;

    public TypeVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.utils = new Utils(this.symbolTable);
    }

    public List<Report> getReports() {
        return utils.getReports();
    }


    @Override
    protected void buildVisitor() {
        addVisit("Integer", this::typeInteger);
        addVisit("Boolean", this::typeBool);
        addVisit("Identifier", this::typeId);
        addVisit("This", this::typeKeywordThis);
        addVisit("InitializeClass", this::typeInitClass);
        addVisit("ParenthesisExpr", this::typeSimpleExpr);
        addVisit("ExprStmt", this::typeSimpleExpr);
        addVisit("NegateExpr", this::typeNegation);
        addVisit("BinaryOp", this::typeBinaryOp);
        addVisit("UnaryOp", this::typeUnaryOp);
        addVisit("PostfixOp", this::typeUnaryOp);
        addVisit("CreateArray", this::typeCreateArray);
        addVisit("ArrayExp", this::typeArrayAccess);
        addVisit("GetLength", this::typeGetLength);
        addVisit("IfStmt", this::typeCondition);
        addVisit("WhileStmt", this::typeCondition);
        addVisit("Assignment", this::typeAssignmentStm);
        addVisit("ArrayAssignStmt", this::typeArrayAssignStm);
        addVisit("CallFnc", this::typeFnCallOp);
        addVisit("InstanceMethodDeclaration", this::typeReturn);
        setDefaultVisit(this::ignore);
    }
    private String ignore (JmmNode jmmNode, String s) {
        return null;
    }

    private String typeInteger(JmmNode node, String s) {
        node.put("type", "int");
        return null;
    }

    private String typeBool(JmmNode node, String s) {
        node.put("type", "boolean");
        return null;
    }

    private String typeId(JmmNode node, String s) {
        Pair<Symbol, String> var = utils.checkVariableIsDeclared(node, "value");
        if(var == null) {
            throw new CompilerException(utils.addReport(node, node.get("value") + " not defined"));
        }
        Type type = var.a.getType();
        node.put("type", type.getName());
        if (type.isArray()){
            node.put("array", "true");
        }
        return null;
    }

    private String typeKeywordThis(JmmNode node, String s) {
        String scope = node.get("scope");
        if(scope.equals("main")){
            String reportMessage = "this keyword is not allowed in main";
            throw new CompilerException(utils.addReport(node, reportMessage));
        }
        else {
            node.put("type", symbolTable.getClassName());
            return null;
        }
    }

    private String typeInitClass(JmmNode node, String s) {
        node.put("type", node.get("value"));
        return null;
    }

    private String typeSimpleExpr(JmmNode node, String s) {
        JmmNode exp = node.getJmmChild(0);
        node.put("type", exp.get("type"));
        return null;
    }

    private String typeNegation(JmmNode node, String s) {
        node.put("type", "boolean");
        return null;
    }

    private String typeBinaryOp(JmmNode node, String s) {
        if (node.get("op").equals("&&") || node.get("op").equals("||")) {
            node.put("type", "boolean");
        }
        else if (node.get("op").equals("<") || node.get("op").equals(">") || node.get("op").equals("<=") || node.get("op").equals(">=") || node.get("op").equals("==") || node.get("op").equals("!=")) {
            node.put("type", "boolean");
        }
        else
            node.put("type", "int");
        return null;
    }

    private String typeUnaryOp(JmmNode node, String s) {
        node.put("type", "int");
        return null;
    }


    private String typeCreateArray(JmmNode node, String s) {
        JmmNode sizeOfArray = node.getJmmChild(0);
        if (utils.nodeIsOfType(sizeOfArray, false, "int")) {
            node.put("type", "int");
            node.put("array", "true");
            return null;
        }
        throw new CompilerException(utils.addReport(node, "Size of array must be of type int"));
    }

    private String typeArrayAccess(JmmNode node, String s) {
        node.put("type", node.getJmmChild(0).get("type"));
        return null;
    }

    private String typeGetLength(JmmNode node, String method) {
        node.put("type", "int");
        return null;
    }

    private String typeCondition(JmmNode node, String s) {
        node.put("type", "boolean");
        return null;
    }

    private boolean typeAssignment(JmmNode node, int child, Type varType) {
        JmmNode exp = node.getJmmChild(child);
        if(exp.getKind().equals("This")){
            String className = symbolTable.getClassName();
            String superName = symbolTable.getSuper();
            if(varType.getName().equals(className)){    // if the current class is the type of the variable that "this" is assigned to
                node.put("type", className);
                if(varType.isArray())
                    node.put("array", "true");
                return true;
            }
            else if(varType.getName().equals(superName)) {   // if the current class extends the type of the variable
                node.put("type", superName);
                if(varType.isArray())
                    node.put("array", "true");
                return true;
            }
            else return false;
        }
        else {
            boolean isArray = varType.isArray();
            String varTypeName = varType.getName();
            node.put("type", varTypeName);
            if (isArray) {
                node.put("array", "true");
            }
            return true;
        }
    }

    private String typeAssignmentStm(JmmNode node, String s) {
        Pair<Symbol, String> var = utils.checkVariableIsDeclared(node, "var");
        if(var == null) {
            throw new CompilerException(utils.addReport(node, node.get("var") + " not defined"));
        }
        else {
            Type varType = var.a.getType();
            if(!typeAssignment(node, 0, varType)){
                throw new CompilerException(utils.addReport(node, "Can't assign \"this\" keyword to " + varType.getName()));
            }
        }
        return null;
    }

    private String typeArrayAssignStm(JmmNode node, String s) {
        Pair<Symbol, String> var = utils.checkVariableIsDeclared(node, "var");
        if(var == null) {
            throw new CompilerException(utils.addReport(node, node.get("var") + " not defined"));
        }
        else {
            Type varType = var.a.getType();
            if(typeAssignment(node, 1, varType)){
                throw new CompilerException(utils.addReport(node, "Can't assign \"this\" keyword to " + varType.getName()));
            }
        }
        return null;
    }

    private String typeFnCallOp(JmmNode node, String s) {
        String className = node.getJmmChild(0).get("type");
        if (className.equals(this.symbolTable.getClassName())) {  //method is part of the current class
            if (this.symbolTable.getSuper() == null) {     //can extend another class
                String methodName = node.get("value");
                if (this.symbolTable.hasMethod(methodName)) {
                    Type methodReturn = symbolTable.getMethod(methodName).getReturnType();
                    node.put("type", methodReturn.getName());
                    if (methodReturn.isArray()) {
                        node.put("array", "true");
                    }
                    return null;
                }
                else {
                    String reportMessage = "Method not defined";
                    throw new CompilerException(utils.addReport(node, reportMessage));
                }
            }
            else if (!symbolTable.isImported(this.symbolTable.getSuper())) {
                String reportMessage = "Class is not defined";
                throw new CompilerException(utils.addReport(node, reportMessage));
            }
            node.put("type", className);    //it's a method from an extended class
            return null;
        }
        else if (!symbolTable.isImported(className)) {
            String reportMessage = "Class is not defined";
            throw new CompilerException(utils.addReport(node, reportMessage));
        }
        node.put("type", className);
        node.put("imported", "true");
        return null;
    }

    private String typeReturn(JmmNode node, String s) {
        String typeReturn = node.getJmmChild(0).get("typeDeclaration");
        boolean isArray = node.getJmmChild(0).getObject("isArray").equals(true);
        node.put("type", typeReturn);
        if(isArray) {
            node.put("array", "true");
        }
        return null;
    }

}