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
        addVisit("NegateExpr", this::handleNegation);
        addVisit("BinaryOp", this::handleBinaryOp);
        addVisit("UnaryOp", this::handleUnaryOp);
        addVisit("PostfixOp", this::handleUnaryOp);
        setDefaultVisit(this::setDefaultVisit);
    }
    private String setDefaultVisit (JmmNode jmmNode, String s) {
        return null;
    }

    private String handleNegation(JmmNode node, String s) {
        JmmNode exp = node.getJmmChild(0);
        if (!utils.nodeIsOfType(exp, false, "boolean", false)) {
            String expType = exp.get("type");
            if(exp.getAttributes().contains("array"))
                expType += "[]";
            throw new CompilerException(utils.addReport(node, "Type must be boolean to negate, " + expType + " found instead"));
        }
        return null;
    }

    private String handleBinaryOp(JmmNode node, String s) {
        JmmNode leftOperand = node.getJmmChild(0);
        JmmNode rightOperand = node.getJmmChild(1);
        if (leftOperand.getAttributes().contains("array") || rightOperand.getAttributes().contains("array"))
            throw new CompilerException(utils.addReport(node, "Operand can't be array"));

        if (node.get("op").equals("&&") || node.get("op").equals("||")) {   //boolean operations
            if (utils.nodeIsOfType(leftOperand, false, "boolean", false) && utils.nodeIsOfType(rightOperand, false, "boolean", false))
                return null;
            else
                throw new CompilerException(utils.addReport(node, "Operand must be of type boolean"));
        }
        else {
            if (utils.nodeIsOfType(leftOperand, false, "int", false) && utils.nodeIsOfType(rightOperand, false, "int", false))
                return null;
            else
                throw new CompilerException(utils.addReport(node, "Operands must be of type int"));
        }
    }

    private String handleUnaryOp(JmmNode node, String s) {
        JmmNode child = node.getJmmChild(0);
        String type = child.get("type");
        if (child.getAttributes().contains("array"))
            throw new CompilerException(utils.addReport(node, "Operand can't be array"));
        if (type.equals("boolean"))
            throw new CompilerException(utils.addReport(node, "Operand can't be of type boolean"));
        return null;
    }
}