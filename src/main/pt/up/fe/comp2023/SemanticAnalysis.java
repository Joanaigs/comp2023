package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.semantic_analysis.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SemanticAnalysis implements JmmAnalysis {
    private final List<Report> reports = new ArrayList<>();

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        SymbolTableGenerator symbolTableGenerator = new SymbolTableGenerator();
        SymbolTable symbolTable = symbolTableGenerator.build(jmmParserResult.getRootNode());
        reports.addAll(symbolTableGenerator.getReports());
        List<AnalyserVisitor> visitors = new ArrayList<>();
        visitors.add(new TypeVisitor(symbolTable));
        visitors.add(new OperatorsVisitor(symbolTable));
        visitors.add(new ArrayVisitor(symbolTable));
        visitors.add(new ConditionVisitor(symbolTable));
        visitors.add(new AssignmentVisitor(symbolTable));
        visitors.add(new FunctionVisitor(symbolTable));

        for( AnalyserVisitor visitor : visitors){
            try {
                visitor.visit(jmmParserResult.getRootNode(), "");
            } catch (CompilerException e) {
                System.out.println(e.getReport().toString());
            }
        }

        for( AnalyserVisitor visitor : visitors){
            reports.addAll(visitor.getReports());
        }

        return new JmmSemanticsResult(jmmParserResult, symbolTable, this.reports);
    }
}
