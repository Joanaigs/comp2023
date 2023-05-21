package pt.up.fe.comp2023.ollir.optimization.liveness;


import org.specs.comp.ollir.*;

import java.util.*;

public class LinenessAnalysis {
    private LivenessUtils utils;
    private Method method;

    private Map<Instruction, LivenessData> data;

    public LinenessAnalysis(Method method) {
        this.data = new HashMap<>();
        this.method = method;
        this.utils = new LivenessUtils(method);

        for (Instruction instruction : method.getInstructions()) {
            data.put(instruction, new LivenessData());
            defineDefs(instruction);
            defineUses(instruction);
            this.data.get(instruction).addIn(data.get(instruction).getUse());

        }
    }

    private void defineDefs(Instruction instruction) {
        if (instruction.getInstType() == InstructionType.ASSIGN) {
            this.utils.getName(((AssignInstruction) instruction).getDest()).ifPresent(name -> this.data.get(instruction).addDef(name));
        } else if (instruction.getInstType() == InstructionType.PUTFIELD) {
            this.utils.getName(((PutFieldInstruction) instruction).getThirdOperand()).ifPresent(name -> this.data.get(instruction).addDef(name));
        }
    }

    private void defineUses(Instruction instruction) {
        switch (instruction.getInstType()) {
            case UNARYOPER:
                buildUses((UnaryOpInstruction) instruction);
            case BINARYOPER:
                buildUses((BinaryOpInstruction) instruction);
            case NOPER:
                buildUses((SingleOpInstruction) instruction);
            case ASSIGN:
                buildUses((AssignInstruction) instruction);
            case CALL:
                buildUses((CallInstruction) instruction);
            case BRANCH:
                buildUses((CondBranchInstruction) instruction);
            case RETURN:
                buildUses((ReturnInstruction) instruction);
            case GETFIELD:
                buildUses((GetFieldInstruction) instruction);
            case PUTFIELD:
                buildUses((PutFieldInstruction) instruction);
            default:
                break;
        }

    }

    private void buildUses(UnaryOpInstruction instruction) {
        Optional<String> opName = this.utils.getName(instruction.getOperand());
        opName.ifPresent(name -> this.data.get(instruction).addUse(name));
    }

    private void buildUses(BinaryOpInstruction instruction) {
        Optional<String> leftOp = this.utils.getName(instruction.getLeftOperand());
        leftOp.ifPresent(name -> this.data.get(instruction).addUse(name));
        Optional<String> rightOp = this.utils.getName(instruction.getLeftOperand());
        rightOp.ifPresent(name -> this.data.get(instruction).addUse(name));
    }

    private void buildUses(SingleOpInstruction instruction) {
        Optional<String> opName = this.utils.getName(instruction.getSingleOperand());
        opName.ifPresent(name -> this.data.get(instruction).addUse(name));
    }

    private void buildUses(AssignInstruction instruction) {
        defineUses(instruction.getRhs());
    }

    private void buildUses(CallInstruction instruction) {
        CallType callType = instruction.getInvocationType();
        if (callType.equals(CallType.invokevirtual) || callType.equals(CallType.invokespecial) ||  callType.equals(CallType.arraylength)) {
            Optional<String> opName = this.utils.getName(instruction.getFirstArg());
            opName.ifPresent(name -> this.data.get(instruction).addUse(name));
        }

        if (instruction.getNumOperands() > 1) {
            if (instruction.getInvocationType() != CallType.NEW) {
                Optional<String> opName = this.utils.getName(instruction.getSecondArg());
                opName.ifPresent(name -> this.data.get(instruction).addUse(name));
            }
            for (Element element : instruction.getListOfOperands()) {
                Optional<String> opName = this.utils.getName(element);
                opName.ifPresent(name -> this.data.get(instruction).addUse(name));
            }
        }

    }

    private void buildUses(CondBranchInstruction instruction) {
        defineUses(instruction.getCondition());
    }

    private void buildUses(ReturnInstruction instruction) {
        if (instruction.hasReturnValue()) {
            Optional<String> opName = this.utils.getName(instruction.getOperand());
            opName.ifPresent(name -> this.data.get(instruction).addUse(name));
        }
    }

    private void buildUses(GetFieldInstruction instruction) {
        Optional<String> opName = this.utils.getName(instruction.getFirstOperand());
        opName.ifPresent(name -> this.data.get(instruction).addUse(name));
    }

    private void buildUses(PutFieldInstruction instruction) {
        Optional<String> opName = this.utils.getName(instruction.getThirdOperand());
        opName.ifPresent(name -> this.data.get(instruction).addUse(name));
    }

}
