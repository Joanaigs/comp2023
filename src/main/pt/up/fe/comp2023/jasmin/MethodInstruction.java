package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashMap;

public class MethodInstruction {

    private final ClassUnit classUnit;
    private Utils jasminUtils;
    private HashMap<String, Descriptor> varTable;

    MethodInstruction(ClassUnit classUnit, Method method)
    {
        this.classUnit = classUnit;
        this.jasminUtils = new Utils(this.classUnit);
        this.varTable =  method.getVarTable();
    }

    public String createInstructionCode(Instruction instruction){
        return this.getCode(instruction, false);
    }


    public String getCode(Instruction instruction, boolean isAssign){

        String code = "";

        switch(instruction.getInstType()){
            case ASSIGN :
                // assign
                code += getAssignCode( (AssignInstruction) instruction);
                break;
            case CALL:
                // invoke methods
                code += getInvokeCode( (CallInstruction) instruction, isAssign);
                break;
            case GOTO:
                break;
            case BRANCH:
                break;
            case RETURN:
                // return
                code += getReturnCode((ReturnInstruction) instruction);
                break;
            case PUTFIELD:
                break;
            case GETFIELD:
                break;
            case UNARYOPER:
                break;
            case BINARYOPER:
                // arithmetic operator
                code += getBinaryOperCode( (BinaryOpInstruction) instruction);
                break;
            case NOPER:
                // single operator instruction
                code += getNoperCode((SingleOpInstruction) instruction);
                break;
        }

        return code;
    }


    public String getNoperCode(SingleOpInstruction instruction){
        String code = "\t";
        var element = instruction.getSingleOperand();
        code += createLoadCode(element);

        return code;
    }

    public String getAssignCode(AssignInstruction instruction){
        String code = "";
        Operand o1 = (Operand) instruction.getDest();

        code += getCode(instruction.getRhs(), true);
        code += createStoreCode(o1);

        return code;
    }

    public String getReturnCode(ReturnInstruction instruction){
        String code = "";

        if(instruction.hasReturnValue()) {
            String loadCode = createLoadCode(instruction.getOperand());
            String returnType = jasminUtils.getReturnType(instruction.getOperand().getType().getTypeOfElement());
            code += "\t" + loadCode + "\t" + returnType;
            this.jasminUtils.sub2StackAtual();
        }

        return code + "return\n";
    }

    private String getBinaryOperCode(BinaryOpInstruction instruction) {
        String code = "";
        var instructionType  = instruction.getOperation().getOpType();

        switch (instructionType) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                code += createIntOperationCode(instruction, instructionType);
                break;
            default:
                break;
        }
        return code;
    }

    private String createIntOperationCode(BinaryOpInstruction instruction, OperationType instructionType) {
        String leftOperand = createLoadCode(instruction.getLeftOperand());
        String rightOperand = createLoadCode(instruction.getRightOperand());
        String operation;

        switch (instructionType) {
            case ADD -> operation = "iadd\n";
            case SUB -> operation = "isub\n";
            case MUL -> operation = "imul\n";
            case DIV -> operation = "idiv\n";
            default -> operation = "";
        }

        return leftOperand + rightOperand + operation;
    }

    public String getInvokeCode(CallInstruction instruction, boolean isAssign) {

        switch (instruction.getInvocationType()) {
            case invokestatic:
                return getInvokeStaticCode(instruction, isAssign);
            case invokevirtual:
                return getInvokeVirtualCode(instruction, isAssign);
            case invokespecial:
                return getInvokeSpecialCode(instruction, isAssign);
            case NEW:
                return getNewCode(instruction);
            case ldc:
                return getLdcCode(instruction);
            default:
                return "";
        }
    }


    private String getInvokeStaticCode(CallInstruction instruction, boolean isAssign) {
        return "";
    }

    private String getInvokeVirtualCode(CallInstruction instruction, boolean isAssign) {
        return "";
    }

    private String getInvokeSpecialCode(CallInstruction instruction, boolean isAssign) {
        return "";
    }

    private String getLdcCode(CallInstruction instruction) {
        return "";
    }

    private String getNewCode(CallInstruction instruction) {
        return "";
    }


    public String createLoadCode(Element e){
        String code = "";

        if (e.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) e;
            var elementType = literalElement.getType().getTypeOfElement();
            switch (elementType) {
                case INT32:
                case BOOLEAN:
                    code += createIConstCode( literalElement.getLiteral() );
                    break;
                default:
                    code += "\tldc " + literalElement.getLiteral();
                    break;
            }
        }
        else {
            Operand operand = (Operand) e;
            int id = (operand.isParameter())? operand.getParamId() : this.varTable.get(operand.getName()).getVirtualReg();

            if (id < 0) {
                // field element
                String className = this.classUnit.getClassName();
                String operandName = operand.getName();
                code += "aload_0\n" + "\tgetfield " + className + "/" + operandName;
            }
            else{
                ElementType elementType = operand.getType().getTypeOfElement();
                switch (elementType) {
                    case INT32:
                    case BOOLEAN:
                        code += createIloadIstoreCode(id, true );
                        break;
                    case CLASS:
                    case STRING:
                        code += "aload" + (id <= 3 ? '_' : ' ') + id;
                        break;
                    case THIS:
                        code += "aload_0";
                        break;
                    case VOID:
                        break;
                }
            }
        }
        this.jasminUtils.sub2StackAtual();
        return code + "\n";
    }

    public String createStoreCode(Element e){
        String code = "";

        if (e.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) e;
            switch (literalElement.getType().getTypeOfElement()) {
                case INT32:
                    code += "\t" + createIloadIstoreCode( Integer.parseInt(literalElement.getLiteral()), false );
                    break;
                default:
                    code += "\tstore " +  literalElement.getLiteral();
                    break;
            }
        } else {
            Operand operand = (Operand) e;
            int id = (operand.isParameter())? operand.getParamId() : this.varTable.get(operand.getName()).getVirtualReg();
            ElementType elemType = operand.getType().getTypeOfElement();

            if (id < 0) {
                code += "\tputfield " + this.jasminUtils.getType(elemType) + "/" + operand.getName() + " " + this.jasminUtils.getType(elemType);
            }else
                switch (elemType) {
                    case INT32:
                    case BOOLEAN:
                        code += "\t" + createIloadIstoreCode(id, false );
                        break;
                    case ARRAYREF:
                    case OBJECTREF:
                    case CLASS:
                    case STRING:
                        code += "\t astore" + (id <= 3 ? '_' : ' ') + id;
                        break;
                    case THIS:
                        code += "\t astore_0";
                        break;
                    case VOID:
                        break;
                }
            }

        this.jasminUtils.sub2StackAtual();
        return code + "\n";
    }


    public String createIloadIstoreCode(int id, boolean load){

        String code = (load) ?  "iload" : "istore";

        code += (id >= 4) ? " " + id : "_" + id;    // optimization

        return code;
    }

    public String createIConstCode(String constValue) {

        String code = "";

        int val = Integer.parseInt(constValue);
        if (val >= 0 && val < 6)
            code += "iconst_";
        else
            code += "ldc ";

        return code + constValue;
    }

}
