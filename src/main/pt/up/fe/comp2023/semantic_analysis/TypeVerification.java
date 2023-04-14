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

public class TypeVerification extends PostorderJmmVisitor<String, String> {
    private final SymbolTable symbolTable;
    private final List<Report> reports;

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
        addVisit("InitializeClass", this::initClass);
        addVisit("ParenthesisExpr", this::simpleExpr);
        addVisit("ExprStmt", this::simpleExpr);
        addVisit("NegateExpr", this::negation);
        addVisit("BinaryOp", this::binaryOp);
        addVisit("UnaryOp", this::unaryOp);
        addVisit("PostfixOp", this::unaryOp);
        addVisit("CreateArray", this::createArray);
        addVisit("ArrayExp", this::arrayAccess);
        addVisit("GetLength", this::getLength);
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


    private Report createReport(JmmNode node, String message) {
        return new Report(ReportType.ERROR, Stage.SEMANTIC, parseInt(node.get("lineStart")), parseInt(node.get("colStart")), message);
    }

     private boolean nodeIsOfType(JmmNode node, boolean isArray, String type) {
        String nodeType = node.get("type");
        if (node.getAttributes().contains("imported"))
            return true;
        if (this.symbolTable.getSuper() != null && this.symbolTable.getSuper().equals(type) && this.symbolTable.getClassName().equals(nodeType))
            return true;
        if (this.symbolTable.isImported(type) && this.symbolTable.isImported(nodeType))
            return true;
        if (node.getAttributes().contains("array") == isArray)
            return type.equals(nodeType);
        return false;
    }

    private Pair<Symbol, String> checkVariableIsDeclared(JmmNode node, String variable){
        String scope = node.get("scope");
        String var = node.get(variable);
        return this.symbolTable.getSymbol(scope, var);
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
        Pair<Symbol, String> var = checkVariableIsDeclared(node, "value");
        if(var == null) {
            String reportMessage = node.get("value") + " not defined";
            Report report = createReport(node, reportMessage);
            this.reports.add(report);
            throw new CompilerException(report);
        }
        else {
            Type type = var.a.getType();
            node.put("type", type.getName());
            if (type.isArray()) {
                node.put("array", "true");
            }
            return null;
        }
    }

    private String keywordThis(JmmNode node, String s) {
        String scope = node.get("scope");
        if(scope.equals("main")){
            String reportMessage = "this keyword is not allowed in main";
            Report report = createReport(node, reportMessage);
            this.reports.add(report);
            throw new CompilerException(report);
        }
        else {
            node.put("type", this.symbolTable.getClassName());
            return null;
        }
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

    private String negation(JmmNode node, String s) {
        JmmNode exp = node.getJmmChild(0);
        String expType = exp.get("type");
        if(exp.getAttributes().contains("array"))
            expType += "[]";
        else{
            if (expType.equals("boolean")) {
                return null;
            }
        }
        String reportMessage = "Type must be boolean to negate, " + expType + " found instead";
        Report report = createReport(node, reportMessage);
        this.reports.add(report);
        throw new CompilerException(report);
    }

    private String binaryOp(JmmNode node, String s) {
        JmmNode leftOperand = node.getJmmChild(0);
        JmmNode rightOperand = node.getJmmChild(1);
        if (leftOperand.getAttributes().contains("array") || rightOperand.getAttributes().contains("array")) {
            String reportMessage = "Operands can't be array";
            Report report = createReport(node, reportMessage);
            this.reports.add(report);
            throw new CompilerException(report);
        }
        else{
            String leftOperandType = leftOperand.get("type");
            String rightOperandType = rightOperand.get("type");
            if (node.get("op").equals("&&") || node.get("op").equals("||")) {   //boolean operations
                if (leftOperandType.equals("boolean") && rightOperandType.equals("boolean") && !leftOperand.getAttributes().contains("array") && !rightOperand.getAttributes().contains("array")) {
                    node.put("type", "boolean");    //both operands are boolean and are not arrays
                    return null;
                } else {
                    String reportMessage = "Both operands must be of type boolean";
                    Report report = createReport(node, reportMessage);
                    this.reports.add(report);
                    throw new CompilerException(report);
                }
            }
            else {
                if(!leftOperandType.equals(rightOperandType)){
                    String reportMessage = "Operands must be of the same type";
                    Report report = createReport(node, reportMessage);
                    this.reports.add(report);
                    throw new CompilerException(report);
                }
                else {
                    if (node.get("op").equals("<") || node.get("op").equals(">") || node.get("op").equals("<=") || node.get("op").equals(">=") || node.get("op").equals("==") || node.get("op").equals("!=")) {
                        node.put("type", "boolean");
                    } else
                        node.put("type", leftOperandType);
                }
            }
            return null;
        }
    }

    private String unaryOp(JmmNode node, String s) {
        JmmNode child = node.getJmmChild(0);
        String type = child.get("type");
        if (child.getAttributes().contains("array")) {
            String reportMessage = "Operand can't be array";
            Report report = createReport(node, reportMessage);
            this.reports.add(report);
            throw new CompilerException(report);
        }
        if (type.equals("boolean")){
            String reportMessage = "Operand can't be of type boolean";
            Report report = createReport(node, reportMessage);
            this.reports.add(report);
            throw new CompilerException(report);
        }
        node.put("type", type);
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
        Report report = createReport(node, reportMessage);
        this.reports.add(report);
        throw new CompilerException(report);
    }

    private String arrayAccess(JmmNode node, String s) {
        JmmNode firstChild = node.getJmmChild(0);
        JmmNode index = node.getJmmChild(1);
        Pair<Symbol, String> var = checkVariableIsDeclared(firstChild, "value");
        if(var == null) {
            String reportMessage = node.get("value") + " not defined";
            Report report = createReport(node, reportMessage);
            this.reports.add(report);
            throw new CompilerException(report);
        }
        else {
            Type firstChildType = var.a.getType();
            String typeName = firstChildType.getName();
            if (!firstChildType.isArray()) {
                String reportMessage = "Array access can only be done over an array, but found " + typeName;
                Report report = createReport(node, reportMessage);
                this.reports.add(report);
                throw new CompilerException(report);
            } else if (!index.get("type").equals("int") || index.getAttributes().contains("array")) {
                String reportMessage = "Array access index must be of type int";
                Report report = createReport(node, reportMessage);
                this.reports.add(report);
                throw new CompilerException(report);
            }
            node.put("type", typeName);
            return null;
        }
    }

    private String getLength(JmmNode node, String method) {
        JmmNode object = node.getJmmChild(0);
        if (object.getAttributes().contains("array")) {
            node.put("type", "int");
            return null;
        }
        String objectType = node.getJmmChild(0).get("type");
        String reportMessage = "Expected type array but found " + objectType + " instead";
        Report report = createReport(node, reportMessage);
        this.reports.add(report);
        throw new CompilerException(report);
    }

    private String loop(JmmNode node, String s) {
        JmmNode condition = node.getJmmChild(0);
        if (condition.getAttributes().contains("array")){
            String reportMessage = "Conditions can't be array";
            Report report = createReport(node, reportMessage);
            this.reports.add(report);
            throw new CompilerException(report);
        }
        else if (condition.get("type").equals("boolean")){
            node.put("type", "boolean");
            return null;
        }
        String reportMessage = "Conditions must be of boolean type";
        Report report = createReport(node, reportMessage);
        this.reports.add(report);
        throw new CompilerException(report);
    }

    private boolean checkAssignmentThis(JmmNode node, Pair<Symbol, String> var) {
        Type varType = var.a.getType();
        String className = this.symbolTable.getClassName();
        String superName = this.symbolTable.getSuper();
        if (varType.getName().equals(className)) {    // if the current class is the type of the variable that "this" is assigned to
            node.put("type", className);
            if (varType.isArray())
                node.put("array", "true");
            return false;
        } else if (varType.getName().equals(superName)) {    // if the current class extends the type of the variable
            node.put("type", superName);
            if (varType.isArray())
                node.put("array", "true");
            return false;
        } else return true;
    }

    private boolean checkAssignment(JmmNode node, Integer child, Pair<Symbol, String> var) {
        Type varType = var.a.getType();
        JmmNode exp = node.getJmmChild(child);
        boolean isArray = varType.isArray();
        String varTypeName = varType.getName();
        if (nodeIsOfType(exp, isArray, varTypeName)) {  //they have the same type
            node.put("type", varTypeName);
            if (isArray) {
                node.put("array", "true");
            }
            return false;
        }
        return true;
    }


    private String assignmentStm(JmmNode node, String s) {
        Pair<Symbol, String> var = checkVariableIsDeclared(node, "var");
        if(var == null) {
            String reportMessage = node.get("var") + " not defined";
            Report report = createReport(node, reportMessage);
            this.reports.add(report);
            throw new CompilerException(report);
        }
        else {
            JmmNode exp = node.getJmmChild(0);
            if (exp.getKind().equals("This")) {
                if (checkAssignmentThis(node, var)) {
                    String reportMessage = "Can't assign \"this\" keyword to " + var.a.getType().getName();
                    Report report = createReport(node, reportMessage);
                    this.reports.add(report);
                    throw new CompilerException(report);
                }
            } else if (checkAssignment(node, 0, var)) {
                String reportMessage = "Type of the assignee must be compatible with the assigned";
                Report report = createReport(node, reportMessage);
                this.reports.add(report);
                throw new CompilerException(report);
            }
            return null;
        }
    }

    private String arrayAssignStm(JmmNode node, String s) {
        JmmNode idx = node.getJmmChild(0);
        if(!idx.get("type").equals("int") || idx.getAttributes().contains("array")){
            String reportMessage = "Array index must be of type integer";
            Report report = createReport(node, reportMessage);
            this.reports.add(report);
            throw new CompilerException(report);
        }
        else {
            Pair<Symbol, String> var = checkVariableIsDeclared(node, "var");
            if(var == null) {
                String reportMessage = node.get("var") + " not defined";
                Report report = createReport(node, reportMessage);
                this.reports.add(report);
                throw new CompilerException(report);
            }
            else{
                JmmNode exp = node.getJmmChild(1);
                if (exp.getKind().equals("This")) {
                    if (checkAssignmentThis(node, var)) {
                        String reportMessage = "Can't assign \"this\" keyword to " + var.a.getType().getName();
                        Report report = createReport(node, reportMessage);
                        this.reports.add(report);
                        throw new CompilerException(report);
                    }
                } else if (checkAssignment(node, 1, var)) {
                    String reportMessage = "Type of the assignee must be compatible with the assigned";
                    Report report = createReport(node, reportMessage);
                    this.reports.add(report);
                    throw new CompilerException(report);
                }
                return null;
            }
        }
    }

    private String fnCallOp(JmmNode node, String s) {
        String className = node.getJmmChild(0).get("type");
        if (className.equals(this.symbolTable.getClassName())) {  //method is part of the current class
            Pair<Symbol, String> var = checkVariableIsDeclared(node, "value");
            if (var == null) {
                if (this.symbolTable.getSuper() == null) {     //can extend another class
                    String reportMessage = "Method does not exist";
                    Report report = createReport(node, reportMessage);
                    this.reports.add(report);
                    throw new CompilerException(report);
                } else {
                    node.put("type", className);
                    return null;
                }
            } else {
                String methodName = var.a.getType().getName();
                if (this.symbolTable.hasMethod(methodName)) {
                    List<Symbol> methodParameters = this.symbolTable.getParameters(methodName);     //check if method parameters and function arguments match
                    int numChildren = node.getNumChildren();
                    List<JmmNode> argumentNodes = new ArrayList<>();
                    if (numChildren > 1) {   // if it's not > 2, then the function has no arguments
                        for (int i = 1; i < numChildren; i++) {
                            argumentNodes.add(node.getJmmChild(i));
                        }
                    }
                    if (methodParameters.size() != argumentNodes.size()) {
                        String reportMessage = "Method parameters and function arguments don't match";
                        Report report = createReport(node, reportMessage);
                        this.reports.add(report);
                        throw new CompilerException(report);
                    }
                    for (int j = 0; j < methodParameters.size(); j++) {
                        Type paramType = methodParameters.get(j).getType();
                        if (!nodeIsOfType(argumentNodes.get(j), paramType.isArray(), paramType.getName())) {
                            String reportMessage = "Method parameters and function arguments don't match";
                            Report report = createReport(node, reportMessage);
                            this.reports.add(report);
                            throw new CompilerException(report);
                        }
                    }
                    Type methodReturn = this.symbolTable.getMethod(methodName).getReturnType();
                    node.put("type", methodReturn.getName());
                    if (methodReturn.isArray()) {
                        node.put("array", "true");
                        return null;
                    }
                } else {
                    String reportMessage = "Method does not exist";
                    Report report = createReport(node, reportMessage);
                    this.reports.add(report);
                    throw new CompilerException(report);
                }
            }
        }
        else if (this.symbolTable.isImported(className)) {
            node.put("type", className);
            node.put("imported", "true");
            return null;
        }
        String reportMessage = "Method not defined";
        Report report = createReport(node, reportMessage);
        this.reports.add(report);
        throw new CompilerException(report);
    }

    private String checkReturn(JmmNode node, String s) {
        String type = node.getJmmChild(0).get("typeDeclaration");
        JmmNode returnNode = node.getJmmChild(node.getNumChildren()-1);
        boolean isArray = node.getJmmChild(0).getObject("isArray").equals(true);
        if(nodeIsOfType(returnNode, isArray, type)){
            String typeReturn = returnNode.get("type");  //last child of the node (return expression)
            boolean isReturnArray = returnNode.getAttributes().contains("array");
            node.put("type", typeReturn);
            if(isReturnArray) {
                node.put("array", "true");
            }
            return null;
        }
        String reportMessage = "Incompatible return type";
        Report report = createReport(node, reportMessage);
        this.reports.add(report);
        throw new CompilerException(report);
    }

}