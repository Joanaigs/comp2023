package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.ollir.optimization.ConstantPropagation;
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
        ConstantPropagation constantPropagation = new ConstantPropagation();
        constantPropagation.visit(semanticsResult.getRootNode(), "");
        return semanticsResult;
    }


}
