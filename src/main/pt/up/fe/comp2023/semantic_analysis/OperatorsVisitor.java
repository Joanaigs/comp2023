package pt.up.fe.comp2023.semantic_analysis;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SymbolTable;

import java.util.List;


public class OperatorsVisitor extends PostorderJmmVisitor<String, String> implements AnalyserVisitor{
    private final Utils utils;

    public OperatorsVisitor(SymbolTable symbolTable) {
        this.utils = new Utils(symbolTable);
    }

    public List<Report> getReports() {
        return utils.getReports();
    }

    @Override
    protected void buildVisitor() {
        addVisit("NegateExpr", this::negation);
        addVisit("BinaryOp", this::binaryOp);
        addVisit("UnaryOp", this::unaryOp);
        addVisit("PostfixOp", this::unaryOp);
        setDefaultVisit(this::ignore);
    }
    private String ignore (JmmNode jmmNode, String s) {
        return null;
    }

    private String negation(JmmNode node, String s) {
        JmmNode exp = node.getJmmChild(0);
        if (!utils.nodeIsOfType(exp, false, "boolean")) {
            String expType = exp.get("type");
            if(exp.getAttributes().contains("array"))
                expType += "[]";
            String reportMessage = "Type must be boolean to negate, " + expType + " found instead";
            throw new CompilerException(utils.addReport(node, reportMessage));
        }
        return null;
    }

    private String binaryOp(JmmNode node, String s) {
        JmmNode leftOperand = node.getJmmChild(0);
        JmmNode rightOperand = node.getJmmChild(1);
        if (leftOperand.getAttributes().contains("array") || rightOperand.getAttributes().contains("array")) {
            String reportMessage = "Operand can't be array";
            throw new CompilerException(utils.addReport(node, reportMessage));
        }
        if (node.get("op").equals("&&") || node.get("op").equals("||")) {   //boolean operations
            if (utils.nodeIsOfType(leftOperand, false, "boolean") && utils.nodeIsOfType(rightOperand, false, "boolean")) return null;
            else {
                String reportMessage = "Operand must be of type boolean";
                throw new CompilerException(utils.addReport(node, reportMessage));
            }
        }
        else {
            if (utils.nodeIsOfType(leftOperand, false, "int") && utils.nodeIsOfType(rightOperand, false, "int")) return null;
            else {
                String reportMessage = "Operands must be of type int";
                throw new CompilerException(utils.addReport(node, reportMessage));
            }
        }
    }

    private String unaryOp(JmmNode node, String s) {
        JmmNode child = node.getJmmChild(0);
        String type = child.get("type");
        if (child.getAttributes().contains("array")) {
            String reportMessage = "Operand can't be array";
            throw new CompilerException(utils.addReport(node, reportMessage));
        }
        if (type.equals("boolean")){
            String reportMessage = "Operand can't be of type boolean";
            throw new CompilerException(utils.addReport(node, reportMessage));
        }
        return null;
    }
}