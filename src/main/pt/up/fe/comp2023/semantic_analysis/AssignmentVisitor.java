package pt.up.fe.comp2023.semantic_analysis;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SymbolTable;

import java.util.List;


public class AssignmentVisitor extends PostorderJmmVisitor<String, String> implements AnalyserVisitor{
    private final Utils utils;

    public AssignmentVisitor(SymbolTable symbolTable) {
        this.utils = new Utils(symbolTable);
    }

    public List<Report> getReports() {
        return utils.getReports();
    }

    @Override
    protected void buildVisitor() {
        addVisit("Assignment", this::handleAssignmentStm);
        addVisit("ArrayAssignStmt", this::handleArrayAssignStm);
        setDefaultVisit(this::ignore);
    }
    private String ignore (JmmNode jmmNode, String s) {
        return null;
    }

    private boolean checkAssignment(JmmNode node, int child, Type varType, boolean shouldBeArray){
        JmmNode exp = node.getJmmChild(child);
        String varTypeName = varType.getName();
        return !utils.nodeIsOfType(exp, shouldBeArray, varTypeName);
    }

    private String handleAssignmentStm(JmmNode node, String s) {
        Pair<Symbol, String> var = utils.checkVariableIsDeclared(node, "var");
        Type varType = var.a.getType();
        if(var.b.equals("FIELD") && node.get("scope").equals("main")){
            throw new CompilerException(utils.addReport(node, "Cannot assign field in static method"));
        }
        if(checkAssignment(node, 0, varType, varType.isArray())){
            throw new CompilerException(utils.addReport(node, "Type of the assignee must be compatible with the assigned"));
        }
        return null;
    }

    private String handleArrayAssignStm(JmmNode node, String s) {
        JmmNode idx = node.getJmmChild(0);
        if(!utils.nodeIsOfType(idx, false, "int")){
            throw new CompilerException(utils.addReport(node, "Array index must be of type integer"));
        }
        else {
            Pair<Symbol, String> var = utils.checkVariableIsDeclared(node, "var");
            Type varType = var.a.getType();
            if(checkAssignment(node, 1, varType, false)){
                throw new CompilerException(utils.addReport(node, "Type of the assignee must be compatible with the assigned"));
            }
            return null;
        }
    }

}