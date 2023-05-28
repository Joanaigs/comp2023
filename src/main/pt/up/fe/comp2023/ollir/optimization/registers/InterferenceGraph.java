package pt.up.fe.comp2023.ollir.optimization.registers;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.VarScope;
import pt.up.fe.comp2023.ollir.optimization.liveness.LivenessAnalysis;
import pt.up.fe.comp2023.ollir.optimization.liveness.LivenessData;

import java.util.*;

public class InterferenceGraph {

    LivenessAnalysis livenessAnalysis;
    Map<String, Set<String>> graph = new HashMap<>();
    Map<String, Descriptor> varTable;
    Stack<String> stack = new Stack<>();
    Map<String, Integer> registers = new HashMap<>();


    public InterferenceGraph(LivenessAnalysis livenessAnalysis){
        this.livenessAnalysis = livenessAnalysis;
        Map<Instruction, LivenessData> data = livenessAnalysis.getData();
        this.varTable = livenessAnalysis.getMethod().getVarTable();
        for (String varName : varTable.keySet()){
            if(varTable.get(varName).getScope().equals(VarScope.LOCAL) && !varTable.get(varName).getVarType().getTypeOfElement().equals(ElementType.THIS)) {
                graph.put(varName, new HashSet<>());
            }
        }

        for (Instruction instruction : data.keySet()){
            LivenessData instData =  data.get(instruction);
            Set<String> aliveOutDef = new HashSet<>(instData.getOut());
            aliveOutDef.addAll(instData.getDef());
            addEdge(aliveOutDef);
            addEdge(instData.getIn());
        }
    }

    private void addEdge(Set<String> alive) {
        for (String var1 : alive) {
            for (String var2 : alive) {
                if (!var1.equals(var2)) {
                    if (!graph.containsKey(var1)) {
                        graph.put(var1, new HashSet<>());
                    }
                    graph.get(var1).add(var2);
                    if (!graph.containsKey(var2)) {
                        graph.put(var2, new HashSet<>());
                    }
                    graph.get(var2).add(var1);
                }
            }
        }
    }

    private int numEdges(String node) {
        return graph.get(node).size();
    }

    private void removeNode(String node) {
        graph.remove(node);
        for (String var : graph.keySet()) {
            Set<String> interferences = graph.get(var);
            interferences.remove(node);
        }
    }

    private boolean findNodeLessKEdges(int k) {
        List<String> nodesToRemove = new ArrayList<>();

        for (String node : graph.keySet()) {
            if (numEdges(node) < k) {
                nodesToRemove.add(node);
                stack.add(node);
            }
        }

        for (String node : nodesToRemove) {
            removeNode(node);
        }

        return !graph.isEmpty();
    }

    private int checkFirstReg(){
        int firstReg = this.livenessAnalysis.getMethod().isStaticMethod() ? 0 : 1;
        for(String var : varTable.keySet()){
            if(varTable.get(var).getScope().equals(VarScope.PARAMETER) || var.equals("this") || varTable.get(var).getVarType().getTypeOfElement().equals(ElementType.THIS))
                firstReg++;
        }
        return firstReg;
    }

    private void allocateRegisters(int numColors, Map<String, Set<String>> graph_copy) {
        int firstReg = checkFirstReg();
        while(!this.stack.empty()){
            String var = this.stack.pop();
            Set<Integer> usedRegisters = new HashSet<>();
            for(String neighbor : graph_copy.get(var)){
                usedRegisters.add(this.registers.get(neighbor));
            }

            for (int i = firstReg; i <= numColors + firstReg; i++) {
                if(!usedRegisters.contains(i)){
                    this.registers.put(var, i);
                    break;
                }
            }
        }
    }

    public int coloring() {
        int numColors = 1;
        Map<String, Set<String>> graph_copy = new HashMap<>(graph);
        while (findNodeLessKEdges(numColors)){
            numColors++;
        }

        allocateRegisters(numColors, graph_copy);

        return numColors;
    }

    public Map<String, Integer> getRegisters() {
        return registers;
    }
}
