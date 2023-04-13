package pt.up.fe.comp2023.semantic_analysis;

import pt.up.fe.comp.jmm.report.Report;

public class CompilerException extends RuntimeException{
    private final Report report;

    public CompilerException(Report report) {
        this.report = report;
    }

    public Report getReport() {return this.report;}

}
