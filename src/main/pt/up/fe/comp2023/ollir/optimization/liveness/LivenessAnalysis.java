package pt.up.fe.comp2023.ollir.optimization.liveness;


import org.specs.comp.ollir.*;


import java.util.*;

public class LivenessAnalysis {
    private final LivenessUtils utils;
    private final Method method;

    private final Map<Instruction, LivenessData> data;

    public LivenessAnalysis(Method method) {
        this.data = new HashMap<>();
        this.method = method;
        this.utils = new LivenessUtils(method);

        ArrayList<Instruction> instructions = method.getInstructions();

        for (Instruction instruction : instructions) {
            data.put(instruction, new LivenessData());
            defineDefs(instruction);
            this.data.get(instruction).addUse(defineUses(instruction));
        }

        lifeCycle(instructions);
    }

    public Method getMethod() {
        return this.method;
    }

    public Map<Instruction, LivenessData> getData() {
        return this.data;
    }

    private void lifeCycle(ArrayList<Instruction> instructions) {
        int changes = instructions.size();
        while(changes>0){
            changes = instructions.size();
            for (int i=instructions.size()-1; i>=0; i--) {
                Instruction instruction = instructions.get(i);
                Set<String> inBefore = new HashSet<>(data.get(instruction).getIn());
                Set<String> outBefore = new HashSet<>(data.get(instruction).getOut());

                Node successor1 = instruction.getSucc1();
                Set<String> out = new HashSet<>();
                if (successor1 == null){
                    changes--;
                    continue; //doesn't have a successor
                }
                if (successor1.getNodeType() != NodeType.END) {
                    Instruction instSuc = ((Instruction) successor1);
                    out.addAll(data.get(instSuc).getIn());
                    Node successor2 = instruction.getSucc2();
                    if (successor2 != null) {
                        out.addAll(data.get(successor2).getIn());
                    }
                }

                LivenessData instData = data.get(instruction);
                instData.addOut(out);

                Set<String> in = new HashSet<>(instData.getOut());
                Set<String> def = instData.getDef();

                in.removeAll(def);

                in.addAll(instData.getUse());
                instData.addIn(in);

                if (inBefore.equals(instData.getIn()) && outBefore.equals(instData.getOut())){
                    changes--;
                }
            }
        }
    }

    private void defineDefs(Instruction instruction) {
        if (instruction.getInstType() == InstructionType.ASSIGN) {
            this.utils.getName(((AssignInstruction) instruction).getDest()).ifPresent(name -> this.data.get(instruction).addDef(name));
        } else if (instruction.getInstType() == InstructionType.PUTFIELD) {
            this.utils.getName(((PutFieldInstruction) instruction).getThirdOperand()).ifPresent(name -> this.data.get(instruction).addDef(name));
        }
    }

    private Set<String> defineUses(Instruction instruction) {
        return switch (instruction.getInstType()) {
            case UNARYOPER -> buildUses((UnaryOpInstruction) instruction);
            case BINARYOPER -> buildUses((BinaryOpInstruction) instruction);
            case NOPER -> buildUses((SingleOpInstruction) instruction);
            case ASSIGN -> buildUses((AssignInstruction) instruction);
            case CALL -> buildUses((CallInstruction) instruction);
            case BRANCH -> buildUses((CondBranchInstruction) instruction);
            case RETURN -> buildUses((ReturnInstruction) instruction);
            case GETFIELD -> buildUses((GetFieldInstruction) instruction);
            case PUTFIELD -> buildUses((PutFieldInstruction) instruction);
            default -> new HashSet<>();
        };
    }

    private Set<String> buildUses(UnaryOpInstruction instruction) {
        Set<String> uses = new HashSet<>();
        Optional<String> opName = this.utils.getName(instruction.getOperand());
        opName.ifPresent(uses::add);
        return uses;
    }

    private Set<String> buildUses(BinaryOpInstruction instruction) {
        Set<String> uses = new HashSet<>();
        Optional<String> leftOp = this.utils.getName(instruction.getLeftOperand());
        leftOp.ifPresent(uses::add);
        Optional<String> rightOp = this.utils.getName(instruction.getRightOperand());
        rightOp.ifPresent(uses::add);
        return uses;
    }

    private Set<String> buildUses(SingleOpInstruction instruction) {
        Set<String> uses = new HashSet<>();
        Optional<String> opName = this.utils.getName(instruction.getSingleOperand());
        opName.ifPresent(uses::add);
        return uses;
    }

    private Set<String> buildUses(AssignInstruction instruction) {
        if(!instruction.getDest().getType().getTypeOfElement().equals(ElementType.ARRAYREF))
            return defineUses(instruction.getRhs());
        return new HashSet<>();
    }

    private Set<String> buildUses(CallInstruction instruction) {
        Set<String> uses = new HashSet<>();
        CallType callType = instruction.getInvocationType();
        if (callType.equals(CallType.invokevirtual) || callType.equals(CallType.invokespecial) ||  callType.equals(CallType.arraylength)) {
            Optional<String> opName = this.utils.getName(instruction.getFirstArg());
            opName.ifPresent(uses::add);
        }

        if (instruction.getNumOperands() > 1) {
            if (instruction.getInvocationType() != CallType.NEW) {
                Optional<String> opName = this.utils.getName(instruction.getSecondArg());
                opName.ifPresent(uses::add);
            }
            for (Element element : instruction.getListOfOperands()) {
                Optional<String> opName = this.utils.getName(element);
                opName.ifPresent(uses::add);
            }
        }
        return uses;
    }

    private Set<String> buildUses(CondBranchInstruction instruction) {
        return defineUses(instruction.getCondition());
    }

    private Set<String> buildUses(ReturnInstruction instruction) {
        Set<String> uses = new HashSet<>();
        if (instruction.hasReturnValue()) {
            Optional<String> opName = this.utils.getName(instruction.getOperand());
            opName.ifPresent(uses::add);
        }
        return uses;
    }

    private Set<String> buildUses(GetFieldInstruction instruction) {
        Set<String> uses = new HashSet<>();
        Optional<String> opName = this.utils.getName(instruction.getFirstOperand());
        opName.ifPresent(uses::add);
        return uses;
    }

    private Set<String> buildUses(PutFieldInstruction instruction) {
        Set<String> uses = new HashSet<>();
        Optional<String> opName = this.utils.getName(instruction.getThirdOperand());
        opName.ifPresent(uses::add);
        return uses;
    }

}
