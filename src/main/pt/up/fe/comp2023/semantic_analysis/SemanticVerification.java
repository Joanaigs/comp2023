package pt.up.fe.comp2023.semantic_analysis;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.SymbolTable;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.specs.util.SpecsStrings.parseInt;

public class SemanticVerification extends PostorderJmmVisitor<String, String> {
    private final SymbolTable symbolTable;
    private List<Report> reports;

    public SemanticVerification(SymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Integer", this::integer);
        addVisit("Boolean", this::bool);
        addVisit("Identifier", this::id);
        addVisit("This", this::keywordThis);
        addVisit("ParenthesisExpr", this::simpleExpr);
        addVisit("ExprStmt", this::simpleExpr);
        addVisit("NegateExpr", this::negation);
        addVisit("CreateArray", this::createArray);
        addVisit("ArrayExp", this::arrayAccess);
        addVisit("InitializeClass", this::initClass);
        addVisit("GetLength", this::getLength);
        addVisit("BinaryOp", this::binaryOp);
        addVisit("UnaryOp", this::unaryOp);
        addVisit("PostfixOp", this::unaryOp);
        addVisit("IfStmt", this::loop);
        addVisit("WhileStmt", this::loop);
        addVisit("Assignment", this::assignmentStm);
        addVisit("ArrayAssignStmt", this::arrayAssignStm);
        addVisit("CallFnc", this::fnCallOp);
        addVisit("InstanceMethodDeclaration", this::checkReturn);
        setDefaultVisit(this::ignore);
    }
    private String ignore ( JmmNode jmmNode, String s) {
        return null;
    }

    private void addReport(JmmNode node, String message) {
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, parseInt(node.get("lineStart")), parseInt(node.get("colStart")), message));
    }

    // not in Util because it needs the symbol table
    private boolean nodeIsOfType(JmmNode node, boolean isArray, String type) {
        String nodeType = node.get("type");
        if (node.getAttributes().contains("imported"))
            return true;
        if (symbolTable.getSuper() != null && symbolTable.getSuper().equals(type) && symbolTable.getClassName().equals(nodeType))
            return true;
        if (symbolTable.isImported(type) && symbolTable.isImported(nodeType))
            return true;
        if (node.getAttributes().contains("array") == isArray)
            return type.equals(nodeType);
        return false;
    }

    private Pair<Symbol, String> checkVariableIsDeclared(JmmNode node, String variable){
        String scope = node.get("scope");
        String var = node.get(variable);
        Pair<Symbol, String> symbolStringPair = symbolTable.getSymbol(scope, var);
        if (symbolStringPair == null) {
            String messageReport = var + " not defined";
            addReport(node, messageReport);
            throw new RuntimeException();
        }
        return symbolStringPair;
    }

    private String initClass(JmmNode node, String s) {
        node.put("type", node.get("value"));
        return null;
    }

    private String simpleExpr(JmmNode node, String s) {
        JmmNode exp = node.getJmmChild(0);
        node.put("type", exp.get("type"));
        return null;
    }

    private String integer(JmmNode node, String s) {
        node.put("type", "int");
        return null;
    }

    private String bool(JmmNode node, String s) {
        node.put("type", "boolean");
        return null;
    }

    private String id(JmmNode node, String s) {
        Type type = checkVariableIsDeclared(node, "value").a.getType();
        node.put("type", type.getName());
        if (type.isArray()){
            node.put("array", "true");
        }
        return null;
    }

    private String keywordThis(JmmNode node, String s) {
        String scope = node.get("scope");
        if(scope.equals("main")){
            String reportMessage = "this keyword is not allowed in main";
            addReport(node, reportMessage);
            throw new RuntimeException();
        }
        else {
            node.put("type", symbolTable.getClassName());
            return null;
        }
    }

    private String negation(JmmNode node, String s) {
        JmmNode exp = node.getJmmChild(0);
        if (!nodeIsOfType(exp, false, "boolean")) {
            String expType = exp.get("type");
            if(exp.getAttributes().contains("array"))
                expType += "[]";
            String reportMessage = "Type must be boolean to negate, " + expType + " found instead";
            addReport(node, reportMessage);
            throw new RuntimeException();
        }
        return null;
    }

    private String createArray(JmmNode node, String s) {
        JmmNode sizeOfArray = node.getJmmChild(0);
        if (nodeIsOfType(sizeOfArray, false, "int")) {
            node.put("type", "int");
            return null;
        }
        String sizeOfArrayType = node.get("type");
        if(sizeOfArray.getAttributes().contains("array"))
            sizeOfArrayType += "[]";
        String reportMessage = "Size of array must be an Integer, type " + sizeOfArrayType + " found instead";
        addReport(node, reportMessage);
        throw new RuntimeException();
    }

    private String arrayAccess(JmmNode node, String s) {
        JmmNode firstChild = node.getJmmChild(0);
        JmmNode secondChild = node.getJmmChild(1);
        Type firstChildType = checkVariableIsDeclared(firstChild, "value").a.getType();
        String typeName = firstChildType.getName();
        if(!firstChildType.isArray()){
            String reportMessage = "Array access can only be done over an array, but found " + typeName;
            addReport(node, reportMessage);
            throw new RuntimeException();
        }
        else if (!secondChild.get("type").equals("int")){
            String reportMessage = "Array access index must be of type Integer, but found " + secondChild.get("type") + " instead";
            addReport(node, reportMessage);
            throw new RuntimeException();
        }
        node.put("type", typeName);
        return null;
    }


    private String getLength(JmmNode node, String method) {
        JmmNode object = node.getJmmChild(0);
        if (object.getAttributes().contains("array")) {
            node.put("type", "int");
            return null;
        }
        String objectType = node.getJmmChild(0).get("type");
        String reportMessage = "Expected type array but found " + objectType + " instead";
        addReport(node, reportMessage);
       throw new RuntimeException();
    }


    private String binaryOp(JmmNode node, String s) {
        JmmNode leftChild = node.getJmmChild(0);
        JmmNode rightChild = node.getJmmChild(1);
        if (node.get("op").equals("&&") || node.get("op").equals("||")) {
            if (!nodeIsOfType(leftChild, false, "boolean")) {
                String leftChildType = leftChild.get("type");
                if(leftChild.getAttributes().contains("array"))
                    leftChildType += "[]";
                String reportMessage = "Operand must be of type boolean, but found " + leftChildType + " instead";
                addReport(node, reportMessage);
                throw new RuntimeException();
            } else if (!nodeIsOfType(rightChild, false, "boolean")) {
                String rightChildType = rightChild.get("type");
                if(rightChild.getAttributes().contains("array"))
                    rightChildType += "[]";
                String reportMessage = "Operand must be of type boolean, but found " + rightChildType + " instead";
                addReport(node, reportMessage);
                throw new RuntimeException();
            }
            else
                node.put("type", "boolean");    // both operands are boolean
        }
        else {
            String type = leftChild.get("type");
            if(!type.equals(rightChild.get("type"))){
                String reportMessage = "Operands must be of the same type";
                addReport(node, reportMessage);
                throw new RuntimeException();
            }
            else if(leftChild.getAttributes().contains("array") || rightChild.getAttributes().contains("array")){
                String reportMessage = "Array cannot be used in arithmetic operations";
                addReport(node, reportMessage);
                throw new RuntimeException();
            }
            else
                node.put("type", type);
        }
        return null;
    }

    private String unaryOp(JmmNode node, String s) {
        JmmNode child = node.getJmmChild(0);
        String type = child.get("type");
        if (type.equals("boolean")){
            String reportMessage = "Operand can't be of type boolean";
            addReport(node, reportMessage);
            throw new RuntimeException();
        }
        node.put("type", type);
        return null;
    }

    private String loop(JmmNode node, String s) {
        JmmNode condition = node.getJmmChild(0);
        if (condition.getAttributes().contains("array")){
            String reportMessage = "Conditions must be of boolean type";
            addReport(node, reportMessage);
            throw new RuntimeException();
        }
        else if (condition.get("type").equals("boolean")){
            node.put("type", "boolean");
            return null;
        }
        String reportMessage = "Conditions must be of boolean type";
        addReport(node, reportMessage);
        throw new RuntimeException();
    }

    private String assignment(JmmNode node, Integer child) {
        Type varType = checkVariableIsDeclared(node, "var").a.getType();
        JmmNode exp = node.getJmmChild(child);
        if(exp.getKind().equals("This")){
            String className = symbolTable.getClassName();
            String superName = symbolTable.getSuper();
            if(varType.equals(className) || varType.equals(superName))
                return null;
            else{
                String reportMessage = "Can't assign \"this\" keyword to " + varType.getName();
                addReport(node, reportMessage);
                throw new RuntimeException();
            }
        }
        else {
            boolean isArray = varType.isArray();
            String varTypeName = varType.getName();
            String expType = exp.get("type");
            if (nodeIsOfType(exp, isArray, varTypeName)) {
                node.put("type", varTypeName);
                if (isArray) {
                    node.put("array", "true");
                }
                return null;
            }
            if (exp.getAttributes().contains("array"))
                expType += "[]";
            if (isArray)
                varTypeName += "[]";
            String reportMessage = "Can't assign " + expType + " to " + varTypeName;
            addReport(node, reportMessage);
            throw new RuntimeException();
        }
    }

    private String assignmentStm(JmmNode node, String s) {
        assignment(node, 0);
        return null;
    }

    private String arrayAssignStm(JmmNode node, String s) {
        JmmNode idx = node.getJmmChild(0);
        if(!idx.get("type").equals("int")){
            String reportMessage = "Array index must be of type integer";
            addReport(node, reportMessage);
            throw new RuntimeException();
        }
        else {
            assignment(node, 1);
            return null;
        }
    }

    private String fnCallOp(JmmNode node, String s) {
        String className = node.getJmmChild(0).get("type");
        String methodName = node.get("value");
        if (className.equals(symbolTable.getClassName())) {      //method is part of the current class
            if (!symbolTable.hasMethod(methodName)) {
                if (symbolTable.getSuper() == null) {     //can extend another class
                    String reportMessage = "Method does not exist";
                    addReport(node, reportMessage);
                    throw new RuntimeException();
                } else {
                    node.put("type", className);
                    return null;
                }
            }
            List<Symbol> methodParameters = symbolTable.getParameters(methodName);     //check if method parameters and function arguments match
            int numChildren = node.getNumChildren();
            List<JmmNode> argumentNodes = new ArrayList<>();
            if (numChildren > 1) {   // if it's not > 2, then the function has no arguments
                for (int i = 1; i < numChildren; i++) {
                    argumentNodes.add(node.getJmmChild(i));
                }
            }
            if (methodParameters.size() != argumentNodes.size()) {
                String reportMessage = "Method parameters and function arguments don't match";
                addReport(node, reportMessage);
                throw new RuntimeException();
            }
            for (int j = 0; j < methodParameters.size(); j++) {
                Type paramType = methodParameters.get(j).getType();
                if (!nodeIsOfType(argumentNodes.get(j), paramType.isArray(), paramType.getName())) {
                    String reportMessage = "Method parameters and function arguments don't match";
                    addReport(node, reportMessage);
                    throw new RuntimeException();
                }
            }
            Type methodReturn = symbolTable.getMethod(methodName).getReturnType();
            node.put("type", methodReturn.getName());
            if (methodReturn.isArray()) {
                node.put("array", "true");
            }
            return null;

        } else if (!symbolTable.isImported(className)) {
            String reportMessage = "Class is not defined";
            addReport(node, reportMessage);
            throw new RuntimeException();
        }
        node.put("type", className);
        node.put("imported", "true");
        return null;
    }

    private String checkReturn(JmmNode node, String s) {
        String type = node.getJmmChild(0).get("typeDeclaration");
        Boolean isArray = node.getJmmChild(0).getObject("isArray").equals(true);
        if(nodeIsOfType(node.getJmmChild(node.getNumChildren()-1), isArray, type)){
            String typeReturn = node.getJmmChild(node.getNumChildren()-1).get("type");
            Boolean isArrayReturn = node.getJmmChild(node.getNumChildren()-1).getAttributes().contains("array");
            node.put("type", typeReturn);
            if(isArrayReturn) {
                node.put("array", "true");
            }
            return null;
        }
        String reportMessage = "Incompatible return type";
        addReport(node, reportMessage);
        throw new RuntimeException();
    }

}