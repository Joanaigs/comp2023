package pt.up.fe.comp2023.semantic_analysis;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SymbolTable;

import java.util.List;


public class ConditionVisitor extends PostorderJmmVisitor<String, String> implements AnalyserVisitor{
    private final Utils utils;

    public ConditionVisitor(SymbolTable symbolTable) {
        this.utils = new Utils(symbolTable);
    }

    public List<Report> getReports() {
        return utils.getReports();
    }

    @Override
    protected void buildVisitor() {
        addVisit("IfStmt", this::condition);
        addVisit("WhileStmt", this::condition);
        setDefaultVisit(this::ignore);
    }
    private String ignore ( JmmNode jmmNode, String s) {
        return null;
    }

    private String condition(JmmNode node, String s) {
        JmmNode condition = node.getJmmChild(0);
        if (utils.nodeIsOfType(condition, false, "boolean")){
            node.put("type", "boolean");
            return null;
        }
        String reportMessage = "Conditions must be of boolean type";
        throw new CompilerException(utils.addReport(node, reportMessage));
    }
}