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
import java.util.LinkedList;
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
        addVisit("CreateArray", this::createArray);
        addVisit("ArrayExp", this::arrayAccess);
        addVisit("GetLength", this::getLength);
        setDefaultVisit(this::ignore);
    }
    private String ignore (JmmNode jmmNode, String s) {
        return null;
    }

    private String createArray(JmmNode node, String s) {
        JmmNode sizeOfArray = node.getJmmChild(0);
        if (utils.nodeIsOfType(sizeOfArray, false, "int")) {
            node.put("type", "int");
            node.put("array", "true");
            return null;
        }
        String sizeOfArrayType = node.get("type");
        if(sizeOfArray.getAttributes().contains("array"))
            sizeOfArrayType += "[]";
        String reportMessage = "Size of array must be of type int, but found type " + sizeOfArrayType + " instead";
        throw new CompilerException(utils.addReport(node, reportMessage));
    }

    private String arrayAccess(JmmNode node, String s) {
        JmmNode firstChild = node.getJmmChild(0);
        JmmNode index = node.getJmmChild(1);
        Pair<Symbol, String> var = utils.checkVariableIsDeclared(firstChild, "value");
        if(var == null) {
            throw new CompilerException(utils.addReport(node, node.get("value") + " not defined"));
        }
        else {
            Type firstChildType = var.a.getType();
            String typeName = firstChildType.getName();
            if (!firstChildType.isArray()) {
                String reportMessage = "Array access can only be done over an array, but found " + typeName;
                throw new CompilerException(utils.addReport(node, reportMessage));
            } else if (!utils.nodeIsOfType(index, false, "int")) {
                String reportMessage = "Array access index must be of type Integer, but found " + index.get("type") + " instead";
                throw new CompilerException(utils.addReport(node, reportMessage));
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
        throw new CompilerException(utils.addReport(node, reportMessage));
    }

}