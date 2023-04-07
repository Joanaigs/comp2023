package pt.up.fe.comp2023.semantic_analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.SymbolExtended;
import pt.up.fe.comp2023.Method;
import pt.up.fe.comp2023.SymbolTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FunctionVerification extends PostorderJmmVisitor<String, String> {
    @Override
    protected void buildVisitor() {

    }
    /*
    private final SymbolTable symbolTable;
    private List<Report> reports;

    public FunctionVerification(SymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
    }

    @Override
    protected void buildVisitor() {
        addVisit("CallFnc", this::fnCallOp);
        setDefaultVisit(this::ignore);
    }
    private String ignore (JmmNode jmmNode, String s) {
        return null;
    }

    private void addReport(JmmNode node, String message) {
        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("column")), message));
    }


    // not in Util because it needs the symbol table
    private boolean nodeIsOfType(JmmNode node, boolean isArray, String type) {
        String nodeType = node.get("type");
        if (node.getAttributes().contains("array") == isArray)
            return type.equals(nodeType);
        else if (symbolTable.getSuper() != null && symbolTable.getSuper().equals(type) && symbolTable.getClassName().equals(nodeType))
            return true;
        else if (symbolTable.isImported(type) && symbolTable.isImported(nodeType))
            return true;
        return false;
    }

    private boolean checkVarHasMethod(JmmNode node) {
        String subjectType = node.getJmmChild(0).get("type");
        Method calledMethod = symbolTable.getMethodScope(node.getJmmChild(1).get("image"));
        if ((subjectType.equals(symbolTable.getClassName()) && calledMethod != null) || cantDetermineArgsType(node)) {
            return true;
        }
        return false;
    }

    private boolean cantDetermineArgsType(JmmNode node) {
        String subjectType = node.getJmmChild(0).get("type");
        String calledMethod = node.getJmmChild(1).get("image");
        return symbolTable.hasSymbolInImportPath(subjectType) || // Symbol was imported
                (
                      subjectType.equals(symbolTable.getClassName()) && // Symbol is the current class
                              symbolTable.getMethodScope(calledMethod) == null && // The called method isn't available in the scope
                              symbolTable.getSuper() != null // However, the class extends another class
                );
    }

    private boolean methodArgsCompatible(JmmNode node) {
        String subjectType = node.getJmmChild(0).get("type");
        List<SymbolExtended> params = symbolTable.getMethodScope(node.getJmmChild(1).get("image")).getParameters();
        List<JmmNode> args = node.getJmmChild(2).getChildren();
        if (args.size() != params.size()) {
            return false;
        }
        for (int i = 0; i < args.size(); i++) {
            Type currentType = params.get(i).getType();
            if (!matchExpectedType(args.get(i), currentType.getName(), currentType.isArray())) {
                return false;
            }
        }
        return true;
    }

    private String fnCallOp(JmmNode node, String s) {
        if (!checkVarHasMethod(node)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Method '%s' not found in '%s' definition", node.getJmmChild(1).get("image"), node.getJmmChild(0).get("type"))
            ));
            throw new SemanticAnalysisException();
        }
        if (cantDetermineArgsType(node)) {
            node.put("type", Constants.ANY_TYPE);
            return null;
        }
        List<Symbol> expectedParams = new ArrayList<>(symbolTable.getMethodScope(node.getJmmChild(1).get("image")).getParameters());
        List<JmmNode> args = node.getJmmChild(2).getChildren();
        if (!methodArgsCompatible(node)) {
            reports.add(new Report(
                    ReportType.ERROR,
                    Stage.SEMANTIC,
                    Integer.parseInt(node.get("line")),
                    Integer.parseInt(node.get("column")),
                    String.format("Gave %s but method expected %s", Util.buildArgTypes(args), Util.buildParamTypes(expectedParams))
            ));
            throw new SemanticAnalysisException();
        }
        Method methodBeingCalled = symbolTable.getMethodScope(node.getJmmChild(1).get("image"));
        node.put("type", methodBeingCalled.getReturnType().getName());
        if (methodBeingCalled.getReturnType().isArray()) {
            node.put("array", "true");
        }
        return null;
    }
    */


}