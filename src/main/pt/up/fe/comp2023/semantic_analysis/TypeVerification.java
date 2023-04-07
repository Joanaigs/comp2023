package pt.up.fe.comp2023.semantic_analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.SymbolExtended;
import pt.up.fe.comp2023.Method;
import pt.up.fe.comp2023.SymbolTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TypeVerification extends PostorderJmmVisitor<String, String> {
    private final SymbolTable symbolTable;
    private List<Report> reports;

    public TypeVerification(SymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Integer", this::integer);
        addVisit("Boolean", this::bool);
        addVisit("Identifier", this::id);
        addVisit("This", this::keywordThis);
        addVisit("NegateExpr", this::negation);
        addVisit("CreateArray", this::createArray);
        //addVisit("ArrayExp", this::arrayExp); //array access
        //addVisit("CallFnc", this::fnCallOp);
        addVisit("GetLength", this::getLength);
        addVisit("BinaryOp", this::binaryOp);
        addVisit("UnaryOp", this::unaryOp);
        addVisit("PostfixOp", this::unaryOp);
        addVisit("IfStm", this::loop);
        addVisit("WhileSTm", this::loop);
        addVisit("Assignment", this::assignmentStm);
        addVisit("ArrayAssignStmt", this::arrayAssignStm);
    }

    private void addReport(JmmNode node, String message) {
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), message));
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
        String nodeScope = node.get("scope");
        String value = node.get("value");
        if (symbolTable.getSymbol(nodeScope, value) == null) {
            String messageReport = value + "not defined";
            addReport(node, messageReport);
            throw new SemanticAnalysisException();
        }
        Type type = symbolTable.getSymbol(nodeScope, value).a.getType();
        node.put("type", type.getName());
        if (type.isArray()){
            node.put("array", "true");
        }
        return null;
    }

    private String keywordThis(JmmNode node, String s) {
        node.put("type", symbolTable.getClassName());
        return null;
    }

    // not in Util because it needs the symbol table
    private boolean nodeIsOfType(JmmNode node, boolean isArray, String type) {
        String nodeType = node.get("type");
        if (node.getAttributes().contains("array") == isArray)
            return type.equals(nodeType);
        else if (symbolTable.getSuper() != null && symbolTable.getSuper().equals(type) && symbolTable.getClassName().equals(nodeType))
            return true;
        else if (symbolTable.isImported(type) && symbolTable.isImported(nodeType))
            return true;
        return false;
    }

    private String negation(JmmNode node, String s) {
        JmmNode exp = node.getJmmChild(0);
        if (!nodeIsOfType(exp, false, "boolean")) {
            String expType = exp.get("type");
            if(exp.getAttributes().contains("array"))
                expType += "[]";
            String reportMessage = "Type must be boolean to negate, " + expType + " found instead";
            addReport(node, reportMessage);
            throw new SemanticAnalysisException();
        }
        return null;
    }

    private String createArray(JmmNode node, String s) {
        JmmNode sizeOfArray = node.getJmmChild(0);
        if (nodeIsOfType(sizeOfArray, false, "int")) {
            node.put("type", "int");
            node.put("array", "true");
            return null;
        }
        String sizeOfArrayType = node.get("type");
        if(sizeOfArray.getAttributes().contains("array"))
            sizeOfArrayType += "[]";
        String reportMessage = "Size of array must be an Integer, type " + sizeOfArrayType + " found instead";
        addReport(node, reportMessage);
        throw new SemanticAnalysisException();
    }

    /*private String arrayExp(JmmNode node, String scope) {
        JmmNode underlyingVar = node.getJmmChild(0);
        Symbol symbol = symbolTable.getSymbol(underlyingVar.get("scope"), underlyingVar.get("image"));
        if (!symbol.getType().isArray()) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Tried to access '%s' as array", symbol.getType()))
            );
        }
        if (!matchExpectedType(node.getJmmChild(1), "int", false)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Array index must be of type int but '%s' was found instead", Util.prettyNodeTypeToString(node.getJmmChild(1)))
            ));
            throw new SemanticAnalysisException();
        }
        node.put("type", symbol.getType().getName());
        return null;
    }
*/
    private String getLength(JmmNode node, String method) {
        JmmNode object = node.getJmmChild(0);
        if (object.getAttributes().contains("array")) {
            node.put("type", "int");
            return null;
        }
        String objectType = node.getJmmChild(0).get("type");
        String reportMessage = "Expected type array but found " + objectType + " instead";
        addReport(node, reportMessage);
       throw new SemanticAnalysisException();
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
                throw new SemanticAnalysisException();
            } else if (!nodeIsOfType(rightChild, false, "boolean")) {
                String rightChildType = rightChild.get("type");
                if(rightChild.getAttributes().contains("array"))
                    rightChildType += "[]";
                String reportMessage = "Operand must be of type boolean, but found " + rightChildType + " instead";
                addReport(node, reportMessage);
                throw new SemanticAnalysisException();
            }
            else
                node.put("type", "boolean");    // both operands are boolean
        }
        else {
            String type = leftChild.get("type");
            if(!type.equals(rightChild.get("type"))){
                String reportMessage = "Operands must be of the same type";
                addReport(node, reportMessage);
                throw new SemanticAnalysisException();
            }
            else if(leftChild.getAttributes().contains("array") || rightChild.getAttributes().contains("array")){
                String reportMessage = "Array cannot be used in arithmetic operations";
                addReport(node, reportMessage);
                throw new SemanticAnalysisException();
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
            throw new SemanticAnalysisException();
        }
        node.put("type", type);
        return null;
    }

    private String loop(JmmNode node, String s) {
        JmmNode condition = node.getJmmChild(0);
        if (nodeIsOfType(condition, false, "boolean")) {
            node.put("type", "boolean");
            return null;
        }
        String reportMessage = "Conditions must be of boolean type";
        addReport(node, reportMessage);
        throw new SemanticAnalysisException();
    }
/*
    private boolean checkVarHasMethod(JmmNode node) {
        String subjectType = node.getJmmChild(0).get("type");
        Method calledMethod = symbolTable.getMethodScope(node.getJmmChild(1).get("image"));
        if ((subjectType.equals(symbolTable.getClassName()) && calledMethod != null) || cantDetermineArgsType(node)) {
            return true;
        }
        return false;
    }

    private boolean cantDetermineArgsType(JmmNode node) {
        String subjectType = node.getJmmChild(0).get("type");
        String calledMethod = node.getJmmChild(1).get("image");
        return symbolTable.hasSymbolInImportPath(subjectType) || // Symbol was imported
                (
                        subjectType.equals(symbolTable.getClassName()) && // Symbol is the current class
                                symbolTable.getMethodScope(calledMethod) == null && // The called method isn't available in the scope
                                symbolTable.getSuper() != null // However, the class extends another class
                );
    }

    private boolean methodArgsCompatible(JmmNode node) {
        String subjectType = node.getJmmChild(0).get("type");
        List<SymbolExtended> params = symbolTable.getMethodScope(node.getJmmChild(1).get("image")).getParameters();
        List<JmmNode> args = node.getJmmChild(2).getChildren();
        if (args.size() != params.size()) {
            return false;
        }
        for (int i = 0; i < args.size(); i++) {
            Type currentType = params.get(i).getType();
            if (!matchExpectedType(args.get(i), currentType.getName(), currentType.isArray())) {
                return false;
            }
        }
        return true;
    }

    private String fnCallOp(JmmNode node, String s) {
        if (!checkVarHasMethod(node)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Method '%s' not found in '%s' definition", node.getJmmChild(1).get("image"), node.getJmmChild(0).get("type"))
            ));
            throw new SemanticAnalysisException();
        }
        if (cantDetermineArgsType(node)) {
            node.put("type", Constants.ANY_TYPE);
            return null;
        }
        List<Symbol> expectedParams = new ArrayList<>(symbolTable.getMethodScope(node.getJmmChild(1).get("image")).getParameters());
        List<JmmNode> args = node.getJmmChild(2).getChildren();
        if (!methodArgsCompatible(node)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Gave %s but method expected %s", Util.buildArgTypes(args), Util.buildParamTypes(expectedParams))
            ));
            throw new SemanticAnalysisException();
        }
        Method methodBeingCalled = symbolTable.getMethodScope(node.getJmmChild(1).get("image"));
        node.put("type", methodBeingCalled.getReturnType().getName());
        if (methodBeingCalled.getReturnType().isArray()) {
            node.put("array", "true");
        }
        return null;
    }
*/
    private String assignment(JmmNode node, Integer child) {
        String nodeScope = node.get("scope");
        String var = node.get("var");
        Type varType = symbolTable.getSymbol(nodeScope, var).a.getType();
        JmmNode exp = node.getJmmChild(child);
        if(exp.getKind().equals("This")){
            String className = symbolTable.getClassName();
            String superName = symbolTable.getSuper();
            if(varType.equals(className) || varType.equals(superName))
                return null;
            else{
                String reportMessage = "Can't assign \"this\" keyword to " + varType.getName();
                addReport(node, reportMessage);
                throw new SemanticAnalysisException();
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
            throw new SemanticAnalysisException();
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
            throw new SemanticAnalysisException();
        }
        else {
            assignment(node, 1);
            return null;
        }
    }
}