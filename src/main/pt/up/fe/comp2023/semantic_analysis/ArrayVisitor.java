package pt.up.fe.comp2023.semantic_analysis;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SymbolTable;

import java.util.List;

import static pt.up.fe.specs.util.SpecsStrings.parseInt;

public class ArrayVisitor extends PostorderJmmVisitor<String, String> implements AnalyserVisitor{
    private final Utils utils;

    public ArrayVisitor(SymbolTable symbolTable) {
        this.utils = new Utils(symbolTable);
    }

    public List<Report> getReports() {
        return utils.getReports();
    }

    @Override
    protected void buildVisitor() {
        addVisit("ArrayExp", this::arrayAccess);
        addVisit("GetLength", this::getLength);
        setDefaultVisit(this::ignore);
    }
    private String ignore (JmmNode jmmNode, String s) {
        return null;
    }

    private String arrayAccess(JmmNode node, String s) {
        JmmNode firstChild = node.getJmmChild(0);
        JmmNode index = node.getJmmChild(1);
        if (!utils.nodeIsOfType(firstChild, true, "int")) {
            String reportMessage = "Array access can only be done over an array of Integers";
            throw new CompilerException(utils.addReport(node, reportMessage));
        }
        else if (!index.getKind().equals("Integer")) {
            if (!utils.nodeIsOfType(index, false, "int")) {
                throw new CompilerException(utils.addReport(node, "Array access index must be of type Integer"));
            }
            else return null;
        }
        else return null;
    }

    private String getLength(JmmNode node, String method) {
        JmmNode object = node.getJmmChild(0);
        if (object.getAttributes().contains("array")) {
            node.put("type", "int");
            return null;
        }
        String objectType = node.getJmmChild(0).get("type");
        String reportMessage = "Expected type array but found " + objectType + " instead";
        throw new CompilerException(utils.addReport(node, reportMessage));
    }

}