package pt.up.fe.comp2023.semantic_analysis;

import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.Collection;
import java.util.List;
public final class Util {

   /*
    public static String buildParamTypes(List<Symbol> args) {
        StringBuilder argStr = new StringBuilder("(");
        if (args.size() != 0) {
            argStr.append(prettyTypeToString(args.get(0).getType()));
        }
        for (int i = 1; i < args.size(); i++) {
            argStr.append(String.format(", %s", prettyTypeToString(args.get(i).getType())));
        }
        argStr.append(")");
        return argStr.toString();
    }

    public static String buildArgTypes(List<JmmNode> args) {
        StringBuilder argStr = new StringBuilder("(");
        if (args.size() != 0) {
            argStr.append(prettyNodeTypeToString(args.get(0)));
        }
        for (int i = 1; i < args.size(); i++) {
            argStr.append(String.format(", %s", prettyNodeTypeToString(args.get(0))));
        }
        argStr.append(")");
        return argStr.toString();
    }

    */
}