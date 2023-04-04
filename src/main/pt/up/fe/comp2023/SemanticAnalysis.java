package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.LinkedList;

public class SemanticAnalysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        SymbolTableGenerator symbolTableGenerator = new SymbolTableGenerator();
        SymbolTable symbolTable = symbolTableGenerator.build(jmmParserResult.getRootNode());
        return new JmmSemanticsResult(jmmParserResult, symbolTable, new LinkedList<Report>());
    }
}
