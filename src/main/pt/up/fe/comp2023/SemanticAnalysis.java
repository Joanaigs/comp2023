package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.semantic_analysis.*;

import java.util.LinkedList;
import java.util.List;

public class SemanticAnalysis implements JmmAnalysis {
    private final List<Report> reports = new LinkedList<>();

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        SymbolTableGenerator symbolTableGenerator = new SymbolTableGenerator();
        SymbolTable symbolTable = symbolTableGenerator.build(jmmParserResult.getRootNode());
        reports.addAll(symbolTableGenerator.getReports());
        try {
            new SemanticVerification(symbolTable, reports).visit(jmmParserResult.getRootNode());
        } catch (RuntimeException e) {}
        return new JmmSemanticsResult(jmmParserResult, symbolTable, reports);
    }
}
