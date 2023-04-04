package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.LinkedList;

public class Optimization implements pt.up.fe.comp.jmm.ollir.JmmOptimization {

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return pt.up.fe.comp.jmm.ollir.JmmOptimization.super.optimize(semanticsResult);
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        OllirGenerator ollirGenerator = new OllirGenerator((SymbolTable) jmmSemanticsResult.getSymbolTable());
        ollirGenerator.visit(jmmSemanticsResult.getRootNode(), null);
        return new OllirResult(jmmSemanticsResult, ollirGenerator.getOllirCode(), new LinkedList<Report>());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return pt.up.fe.comp.jmm.ollir.JmmOptimization.super.optimize(ollirResult);
    }
}
