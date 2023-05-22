package pt.up.fe.comp2023.ollir.optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.*;
import pt.up.fe.comp2023.ollir.optimization.liveness.LivenessAnalysis;
import pt.up.fe.comp2023.ollir.optimization.registers.InterferenceGraph;
import pt.up.fe.comp2023.ollir.optimization.registers.RegisterException;

import java.util.Map;

public class RegisterAllocation {
    private final ClassUnit classUnit;

    public RegisterAllocation(OllirResult ollirResult) {
        this.classUnit = ollirResult.getOllirClass();
    }

    public void optimize(Integer numRegisters) {
        classUnit.buildCFGs();
        classUnit.buildVarTables();

        for(Method method : classUnit.getMethods()) {
            LivenessAnalysis livenessAnalysis = new LivenessAnalysis(method);
            InterferenceGraph interferenceGraph = new InterferenceGraph(livenessAnalysis);
            int numColors = interferenceGraph.coloring();
            if (numColors > numRegisters && numRegisters > 0) {
                throw new RegisterException("Number of registers not enough, needed " + numColors + " registers");
            }

            Map<String, Integer> registers = interferenceGraph.getRegisters();
            Map<String, Descriptor> varTable = method.getVarTable();
            for(String var : registers.keySet()){
                varTable.get(var).setVirtualReg(registers.get(var));
            }
        }

    }
}
