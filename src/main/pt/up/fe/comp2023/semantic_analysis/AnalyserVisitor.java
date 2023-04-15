package pt.up.fe.comp2023.semantic_analysis;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public interface AnalyserVisitor {
    List<Report> getReports();

    String visit(JmmNode jmmNode, String s);
}
