package pt.up.fe.comp2023.semantic_analysis;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;
public class ThisKeywordInMain extends PostorderJmmVisitor<Object, Object> {
    List<Report> reports;

    public ThisKeywordInMain(List<Report> reports) {
        this.reports = reports;
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit("This", this::thisUsage);
    }


    private Object thisUsage(JmmNode node, Object dummy) {
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")),
                "this keyword was found in main"));
        return null;
    }
}
