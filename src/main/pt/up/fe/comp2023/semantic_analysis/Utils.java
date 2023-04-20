package pt.up.fe.comp2023.semantic_analysis;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static pt.up.fe.specs.util.SpecsStrings.parseInt;

public class Utils {
    private final SymbolTable symbolTable;
    private final List<Report> reportList = new LinkedList<>();

    public Utils(SymbolTable symbolTable){
        this.symbolTable = symbolTable;
    }

    public List<Report> getReports() {
        return this.reportList;
    }

    public boolean nodeIsOfType(JmmNode node, boolean isArray, String type, boolean isAssignment) {
        if (node.getAttributes().contains("imported"))
            return true;
        if (node.getAttributes().contains("extended"))
            return true;
        if(!node.getAttributes().contains("type"))
            return false;
        String nodeType = node.get("type");
        if (!Objects.isNull(this.symbolTable.getSuper()) && this.symbolTable.getSuper().equals(type) && this.symbolTable.getClassName().equals(nodeType))
            return true;
        if ((this.symbolTable.isImported(type) && this.symbolTable.isImported(nodeType)))
            return true;
        if (isAssignment && this.symbolTable.isImported(nodeType) && (Objects.isNull(this.symbolTable.getSuper()) || !this.symbolTable.getSuper().equals(nodeType))) {
            return true;     //if the assignee is imported and the current class does not extend it, then assume it's possible
        }
        if (node.getAttributes().contains("array") == isArray)
            return type.equals(nodeType);
        return false;
    }

    public Pair<Symbol, String> checkVariableIsDeclared(JmmNode node, String variable){
        String scope = node.get("scope");
        String var = node.get(variable);
        String extendedClass = this.symbolTable.getSuper();
        if (!Objects.isNull(this.symbolTable.getSuper()) && this.symbolTable.getSuper().equals(var))
            return new Pair<>(new Symbol(new Type(extendedClass, true), extendedClass), "");
        Pair<Symbol, String> symbolStringPair = this.symbolTable.getSymbol(scope, var);
        if(!isNull(symbolStringPair))
            return symbolStringPair;
        throw new CompilerException(addReport(node, var + " not defined"));
    }

    public Report addReport(JmmNode node, String reportMessage){
        Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, parseInt(node.get("lineStart")), parseInt(node.get("colStart")), reportMessage);
        this.reportList.add(report);
        return report;
    }
}
