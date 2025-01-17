package pt.up.fe.comp2023.semantic_analysis.visitors;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.semantic_analysis.CompilerException;
import pt.up.fe.comp2023.semantic_analysis.SymbolTable;
import pt.up.fe.comp2023.semantic_analysis.Utils;
import pt.up.fe.comp2023.semantic_analysis.visitors.AnalyserVisitor;

import java.util.List;
import java.util.Objects;


public class TypeVisitor extends PostorderJmmVisitor<String, String> implements AnalyserVisitor {
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
        addVisit("Integer", this::handleTypeInteger);
        addVisit("Boolean", this::handleTypeBool);
        addVisit("Identifier", this::handleTypeId);
        addVisit("This", this::handleTypeKeywordThis);
        addVisit("InitializeClass", this::handleTypeInitClass);
        addVisit("ParenthesisExpr", this::handleTypeSimpleExpr);
        addVisit("ExprStmt", this::handleTypeSimpleExpr);
        addVisit("NegateExpr", this::handleTypeNegation);
        addVisit("BinaryOp", this::handleTypeBinaryOp);
        addVisit("UnaryOp", this::handleTypeUnaryOp);
        addVisit("PostfixOp", this::handleTypeUnaryOp);
        addVisit("CreateArray", this::handleTypeCreateArray);
        addVisit("ArrayExp", this::handleTypeArrayAccess);
        addVisit("GetLength", this::handleTypeGetLength);
        addVisit("IfStmt", this::handleTypeCondition);
        addVisit("WhileStmt", this::handleTypeCondition);
        addVisit("Assignment", this::handleTypeAssignmentStm);
        addVisit("ArrayAssignStmt", this::handleTypeArrayAssignStm);
        addVisit("CallFnc", this::handleTypeFnCallOp);
        addVisit("InstanceMethodDeclaration", this::handleTypeReturn);
        setDefaultVisit(this::setDefaultVisit);
    }
    private String setDefaultVisit (JmmNode jmmNode, String s) {
        return null;
    }

    private String handleTypeInteger(JmmNode node, String s) {
        node.put("type", "int");
        return null;
    }

    private String handleTypeBool(JmmNode node, String s) {
        node.put("type", "boolean");
        return null;
    }

    private String handleTypeId(JmmNode node, String s) {
        Pair<Symbol, String> var = utils.checkVariableIsDeclared(node, "value");
        Type type = var.a.getType();
        node.put("type", type.getName());
        if (type.isArray()){
            node.put("array", "true");
        }
        return null;
    }

    private String handleTypeKeywordThis(JmmNode node, String s) {
        String scope = node.get("scope");
        if(scope.equals("main")){
            throw new CompilerException(utils.addReport(node, "this keyword is not allowed in main"));
        }
        else {
            node.put("type", symbolTable.getClassName());
            return null;
        }
    }

    private String handleTypeInitClass(JmmNode node, String s) {
        node.put("type", node.get("value"));
        return null;
    }

    private String handleTypeSimpleExpr(JmmNode node, String s) {
        JmmNode exp = node.getJmmChild(0);
        node.put("type", exp.get("type"));
        return null;
    }

    private String handleTypeNegation(JmmNode node, String s) {
        node.put("type", "boolean");
        return null;
    }

    private String handleTypeBinaryOp(JmmNode node, String s) {
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

    private String handleTypeUnaryOp(JmmNode node, String s) {
        node.put("type", "int");
        return null;
    }


    private String handleTypeCreateArray(JmmNode node, String s) {
        JmmNode sizeOfArray = node.getJmmChild(0);
        if (utils.nodeIsOfType(sizeOfArray, false, "int", false)) {
            node.put("type", "int");
            node.put("array", "true");
            return null;
        }
        throw new CompilerException(utils.addReport(node, "Size of array must be of type int"));
    }

    private String handleTypeArrayAccess(JmmNode node, String s) {
        node.put("type", node.getJmmChild(0).get("type"));
        return null;
    }

    private String handleTypeGetLength(JmmNode node, String method) {
        node.put("type", "int");
        return null;
    }

    private String handleTypeCondition(JmmNode node, String s) {
        node.put("type", "boolean");
        return null;
    }

    private boolean typeAssignmentThis(JmmNode node, Type varType) {
        String className = symbolTable.getClassName();
        String superName = symbolTable.getSuper();
        if(varType.getName().equals(className)){    // if the current class is the type of the variable that "this" is assigned to
            node.put("type", className);
            return false;
        }
        else if(varType.getName().equals(superName)) {   // if the current class extends the type of the variable
            node.put("type", superName);
            return false;
        }
        else
            return true;
    }

    private void typeAssignment(JmmNode node, Type varType) {
        String varTypeName = varType.getName();
        node.put("type", varTypeName);
    }

    private String handleTypeAssignmentStm(JmmNode node, String s) {
        Pair<Symbol, String> var = utils.checkVariableIsDeclared(node, "var");
        Type varType = var.a.getType();
        JmmNode exp = node.getJmmChild(0);
        if(exp.getKind().equals("This")) {
            if (typeAssignmentThis(node, varType)) {
                throw new CompilerException(utils.addReport(node, "Can't assign \"this\" keyword to " + varType.getName()));
            }
        }
        else typeAssignment(node, varType);
        return null;
    }

    private String handleTypeArrayAssignStm(JmmNode node, String s) {
        Pair<Symbol, String> var = utils.checkVariableIsDeclared(node, "var");
        Type varType = var.a.getType();
        JmmNode exp = node.getJmmChild(1);
        if(exp.getKind().equals("This")) {
            if (typeAssignmentThis(node, varType)) {
                throw new CompilerException(utils.addReport(node, "Can't assign \"this\" keyword to " + varType.getName()));
            }
        }
        else typeAssignment(node, varType);
        return null;
    }

    private String handleTypeFnCallOp(JmmNode node, String s) {
        String className = node.getJmmChild(0).get("type");
        String extendedClass = this.symbolTable.getSuper();

        if (className.equals(this.symbolTable.getClassName())) {  //class being called is the current class
            if (Objects.isNull(extendedClass)) {
                String methodName = node.get("value");
                if (this.symbolTable.hasMethod(methodName)) {
                    Type methodReturn = symbolTable.getMethod(methodName).getReturnType();
                    node.put("type", methodReturn.getName());
                    if (methodReturn.isArray()) {
                        node.put("array", "true");
                    }
                    return null;
                }
                else throw new CompilerException(utils.addReport(node, "Method not defined"));
            }
            else if (!symbolTable.isImported(extendedClass)) {  //extended class must be imported
                throw new CompilerException(utils.addReport(node, "Class is not defined"));
            }
            node.put("type", className);    //it's a method from an imported extended class
            node.put("extended", extendedClass);
            return null;
        }
        else if (!symbolTable.isImported(className)) {
            throw new CompilerException(utils.addReport(node, "Class is not defined"));
        }

        else if (className.equals(extendedClass)) {     //class is being extended and imported
            node.put("type", className);
            node.put("extended", extendedClass);
            return null;
        }

        node.put("type", className);     //class is being imported
        node.put("imported", "true");
        return null;
    }

    private String handleTypeReturn(JmmNode node, String s) {
        String typeReturn = node.getJmmChild(0).get("typeDeclaration");
        boolean isArray = node.getJmmChild(0).getObject("isArray").equals(true);
        node.put("type", typeReturn);
        if(isArray) {
            node.put("array", "true");
        }
        return null;
    }

}