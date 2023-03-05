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
        symbolTableGenerator.visit(jmmParserResult.getRootNode(), null);
        SymbolTable symbolTable= new SymbolTable(symbolTableGenerator.get_super(), symbolTableGenerator.getClassName(), symbolTableGenerator.getImports(), symbolTableGenerator.getFields(), symbolTableGenerator.getMethods());
        return new JmmSemanticsResult(jmmParserResult, symbolTable, new LinkedList<Report>());
    }
}
