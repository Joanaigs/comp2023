package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.ollir.optimization.ConstantFolding;
import pt.up.fe.comp2023.ollir.optimization.ConstantPropagation;
import pt.up.fe.comp2023.ollir.optimization.DeadCodeElimination;
import pt.up.fe.comp2023.ollir.optimization.RegisterAllocation;

import pt.up.fe.comp2023.semantic_analysis.SymbolTable;

import java.util.LinkedList;

public class Optimization implements pt.up.fe.comp.jmm.ollir.JmmOptimization {


    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        OllirGenerator ollirGenerator = new OllirGenerator((SymbolTable) jmmSemanticsResult.getSymbolTable());
        ollirGenerator.visit(jmmSemanticsResult.getRootNode(), null);
        return new OllirResult(jmmSemanticsResult, ollirGenerator.getOllirCode(), new LinkedList<>());
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        boolean optimize = semanticsResult.getConfig().get("optimize") != null && semanticsResult.getConfig().get("optimize").equals("true");
        if (!optimize) return semanticsResult;
        boolean hasChanges=true;
        while (hasChanges) {
            DeadCodeElimination deadCodeElimination = new DeadCodeElimination();
            hasChanges = deadCodeElimination.visit(semanticsResult.getRootNode(), "");
            ConstantPropagation constantPropagation = new ConstantPropagation();
            hasChanges = hasChanges || constantPropagation.visit(semanticsResult.getRootNode(), "");
            ConstantFolding constantFolding = new ConstantFolding();
            hasChanges = hasChanges || constantFolding.visit(semanticsResult.getRootNode(), "");
        }
        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult){
        String numberLocalVariablesString = ollirResult.getConfig().get("registerAllocation");
        int localVariableNum = numberLocalVariablesString == null? -1 : Integer.parseInt(numberLocalVariablesString);

        if (localVariableNum > -1) {
            RegisterAllocation registerAllocation = new RegisterAllocation(ollirResult);
            registerAllocation.optimize(localVariableNum);

        }
        return ollirResult;
    }



}
