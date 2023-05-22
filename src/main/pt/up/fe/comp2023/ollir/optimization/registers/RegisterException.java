package pt.up.fe.comp2023.ollir.optimization.registers;

import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;


public class RegisterException extends RuntimeException{
    private final Report report;

    public RegisterException(String reportMessage) {
        this.report = new Report(ReportType.ERROR, Stage.OPTIMIZATION, -1, reportMessage);
    }

    public Report getReport() {
        return this.report;
    }
}
