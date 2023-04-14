package pt.up.fe.comp2023.semantic_analysis;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.SymbolTable;

import java.util.ArrayList;
import java.util.List;


public class FunctionVisitor extends PostorderJmmVisitor<String, String> implements AnalyserVisitor{
    private final SymbolTable symbolTable;
    private final Utils utils;

    public FunctionVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.utils = new Utils(this.symbolTable);
    }

    public List<Report> getReports() {
        return utils.getReports();
    }

    @Override
    protected void buildVisitor() {
        addVisit("CallFnc", this::fnCallOp);
        addVisit("InstanceMethodDeclaration", this::checkReturn);
        setDefaultVisit(this::ignore);
    }

    private String ignore (JmmNode jmmNode, String s) {
        return null;
    }

    private String fnCallOp(JmmNode node, String s) {
        if(!node.getJmmChild(0).getAttributes().contains("type")){
            String reportMessage = "Class not defined";
            throw new CompilerException(utils.addReport(node, reportMessage));
        }
        String className = node.getJmmChild(0).get("type");
        Pair<Symbol, String> var = utils.checkVariableIsDeclared(node, "value");
        if (var == null) {
            return null; //assume the call is right
        }
        Type functionType = var.a.getType();
        String methodName = functionType.getName();
        if (className.equals(symbolTable.getClassName()) && symbolTable.getSuper() == null) {  //method is part of the current class
            if (this.symbolTable.hasMethod(methodName)) {
                List<Symbol> methodParameters = symbolTable.getParameters(methodName);     //check if method parameters and function arguments match
                int numChildren = node.getNumChildren();
                List<JmmNode> argumentNodes = new ArrayList<>();
                if (numChildren > 1) {   // if it's not > 2, then the function has no arguments
                    for (int i = 1; i < numChildren; i++) {
                        argumentNodes.add(node.getJmmChild(i));
                    }
                }
                if (methodParameters.size() != argumentNodes.size()) {
                    String reportMessage = "Method parameters and function arguments don't match";
                    throw new CompilerException(utils.addReport(node, reportMessage));
                }
                for (int j = 0; j < methodParameters.size(); j++) {
                    Type paramType = methodParameters.get(j).getType();
                    if (!utils.nodeIsOfType(argumentNodes.get(j), paramType.isArray(), paramType.getName())) {
                        String reportMessage = "Method parameters and function arguments don't match";
                        throw new CompilerException(utils.addReport(node, reportMessage));
                    }
                }
                return null;
            }
            else{
                String reportMessage = "Method is not defined";
                throw new CompilerException(utils.addReport(node, reportMessage));
            }
        }
        return null;
    }

    private String checkReturn(JmmNode node, String s) {
        if(!utils.nodeIsOfType(node.getJmmChild(node.getNumChildren()-1), node.getJmmChild(0).getObject("isArray").equals(true), node.getJmmChild(0).get("typeDeclaration"))) {
            String reportMessage = "Incompatible return type";
            throw new CompilerException(utils.addReport(node, reportMessage));
        }
        return null;
    }

}