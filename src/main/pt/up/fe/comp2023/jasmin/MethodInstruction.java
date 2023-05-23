package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashMap;

import static org.specs.comp.ollir.ElementType.INT32;

public class MethodInstruction {

    private final ClassUnit classUnit;
    private boolean isAssign;
    private HashMap<String, Descriptor> varTable;
    private int conditionalID = 0;

    MethodInstruction(ClassUnit classUnit, Method method)
    {
        this.classUnit = classUnit;
        this.varTable =  method.getVarTable();
        this.isAssign = false;
    }

    public String createInstructionCode(Instruction instruction){

        String code = "";
        switch(instruction.getInstType()){
            case ASSIGN :
                // isAssign é para o POP -> callInstruction e ela retornar um objeto (!void)
                this.isAssign = true;
                code += getAssignCode( (AssignInstruction) instruction);
                this.isAssign = false;
                break;
            case CALL:
                code += getInvokeCode( (CallInstruction) instruction);
                if (((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID)
                    if (!this.isAssign) code += "pop\n";   // nao estou dentro,   invokevirtual (10 + 30) fica 40 na stack mas tenho de dar pop
                break;
            case GOTO:
                code += getGotoCode((GotoInstruction) instruction);
                break;
            case BRANCH:
                code += getBranchCode( (CondBranchInstruction) instruction);
                break;
            case RETURN:
                code += getReturnCode((ReturnInstruction) instruction);
                break;
            case PUTFIELD:
                code += getPutFieldCode((PutFieldInstruction) instruction);
                break;
            case GETFIELD:
                code += getGetFieldCode( (GetFieldInstruction) instruction);
                break;
            case UNARYOPER:
                break;
            case BINARYOPER:
                code += getBinaryOperCode( (BinaryOpInstruction) instruction);
                break;
            case NOPER:
                code += getNoperCode((SingleOpInstruction) instruction);
                break;
        }

        return code;
    }

    private String getNoperCode(SingleOpInstruction instruction){

        var element = instruction.getSingleOperand();
        return getLoadCode(element);
    }
    
    private String getReturnCode(ReturnInstruction instruction){
        String code = "";

        if(instruction.hasReturnValue()) {
            String loadCode = getLoadCode(instruction.getOperand());
            String returnType = Utils.getReturnType(instruction.getOperand().getType().getTypeOfElement());
            code +=  loadCode +  returnType;
        }

        return code + "return\n";
    }

    private String getBinaryOperCode(BinaryOpInstruction instruction) {
        String code = "";
        var instructionType  = instruction.getOperation().getOpType();

        switch (instructionType) {
            case ADD, SUB, MUL, DIV -> {
                code += getArithmeticOpCode(instruction, instructionType);
            }
            case LTH, GTE , ANDB, NOT -> {
                code += getBooleanOpCode(instruction, instructionType);
            }
                default ->{}
        }
        return code;
    }

    private String getAssignCode(AssignInstruction instruction) {
        String code  = "";

        Operand op = (Operand) instruction.getDest();
        code += createInstructionCode(instruction.getRhs()) +  getStoreCode(op);

        return code;
    }

    private String getTrueLabel() {
        return "myTrue" + this.conditionalID;
    }

    private String getEndIfLabel() {
        return "myEndIf" + this.conditionalID;
    }


    private String getArithmeticOpCode(BinaryOpInstruction instruction, OperationType instructionType) {
        String leftOperand = getLoadCode(instruction.getLeftOperand());
        String rightOperand = getLoadCode(instruction.getRightOperand());
        String op;

        switch (instructionType) {
            case ADD  -> op = "iadd\n";
            case SUB  -> op = "isub\n";
            case MUL  -> op = "imul\n";
            case DIV  -> op= "idiv\n";
            default  -> op = "";
        }

        return leftOperand + rightOperand + op;
    }

    private String getBooleanOpCode(BinaryOpInstruction instruction, OperationType instructionType) {
        String code = "";
        return code;
    }

    private String getPutFieldCode(PutFieldInstruction instruction){
        String code = "";

        Operand firstOperand = (Operand) instruction.getFirstOperand();
        Operand secondOperand = (Operand) instruction.getSecondOperand();
        Element element = instruction.getThirdOperand();
        String varType = Utils.getType(secondOperand.getType(), this.classUnit);

        code += getLoadCode(firstOperand) + getLoadCode(element) + "putfield ";

        return  code + classUnit.getClassName() + "/" + secondOperand.getName() + " " + varType + "\n";
    }

    private String getGetFieldCode(GetFieldInstruction instruction) {
        String code = "";

        Operand firstOperand = (Operand) instruction.getFirstOperand();
        Operand secondOperand = (Operand) instruction.getSecondOperand();
        String varType = Utils.getType(secondOperand.getType(), this.classUnit);

        code += getLoadCode(firstOperand) + "getfield ";

        return code + classUnit.getClassName() + "/" + secondOperand.getName() + " " + varType +  "\n";
    }

    private String getGotoCode(GotoInstruction instruction){

        return "\tgoto " + instruction.getLabel() + "\n";
    }

    private String getBranchCode(CondBranchInstruction instruction){

        return "";
    }

    public String getInvokeCode(CallInstruction instruction) {

        switch (instruction.getInvocationType()) {
            case invokestatic:
                return getInvokeStaticCode(instruction);
            case invokevirtual:
                return getInvokeVirtualCode(instruction);
            case invokespecial:
                return getInvokeSpecialCode(instruction);
            case NEW:
                return getInvokeNewCode(instruction);
            default:
                return "";
        }
    }

    private String getInvokeStaticCode(CallInstruction instruction) {
        String code = "";

        for (Element element : instruction.getListOfOperands())
            code += getLoadCode(element);


        code += "invokestatic "
                + Utils.getClassPath(((Operand) instruction.getFirstArg()).getName(), classUnit)
                + "/"
                + ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "")
                + "(";

        for (Element element : instruction.getListOfOperands()) {
            code += Utils.getType(element.getType(), classUnit);
        }


        return code + ")" + Utils.getType(instruction.getReturnType(), classUnit) + "\n";

    }

    private String getInvokeVirtualCode(CallInstruction instruction) {

        String code = getLoadCode(instruction.getFirstArg());
        for (Element element : instruction.getListOfOperands())
            code += getLoadCode(element);
        code += "invokevirtual "
                + Utils.getClassPath( ((ClassType) instruction.getFirstArg().getType()).getName(), classUnit)
                + "/"
                + ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "")
                + "(";

        for (Element element : instruction.getListOfOperands())
            code += Utils.getType(element.getType(), classUnit);

        return code + ")" + Utils.getType(instruction.getReturnType(), classUnit) + "\n";
    }

    private String getInvokeSpecialCode(CallInstruction instruction) {

        var firstArg = instruction.getFirstArg();
        String superClassName = (classUnit.getSuperClass() == null)? "java/lang/Object\n" : (classUnit.getSuperClass() + "\n");
        String code = "";

        if (firstArg.getType().getTypeOfElement() == ElementType.THIS)
            code += getLoadCode(firstArg) + "invokespecial " + superClassName;
        else
            code += getLoadCode(firstArg) + "invokespecial " + Utils.getClassPath(((ClassType) instruction.getFirstArg().getType()).getName(), classUnit);

        code += "/<init>(";

        for (Element element : instruction.getListOfOperands()) {
            code += Utils.getType(element.getType(), classUnit);
        }

        code += ")" + Utils.getType(instruction.getReturnType(), classUnit) + "\n";

        return code;
    }

    private String getInvokeNewCode(CallInstruction instruction) {
        String code = "";

        for (Element element : instruction.getListOfOperands()) {
            code += getLoadCode(element);
        }

        return code + "new " + Utils.getClassPath(((Operand) instruction.getFirstArg()).getName(), classUnit) + "\ndup\n";
    }

    private String getLoadCode(Element e){
        String code = "";

        if (e.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) e;
            var elementType = literalElement.getType().getTypeOfElement();
            switch (elementType) {
                case INT32,BOOLEAN -> code += getIConstCode( literalElement.getLiteral() );
                default -> code += "ldc " + literalElement.getLiteral();
            }
        }
        else {
            Operand operand = (Operand) e;
            int id = (operand.isParameter())? operand.getParamId() : this.varTable.get(operand.getName()).getVirtualReg();

            if (id < 0) {
                // field element
                String className = this.classUnit.getClassName();
                String operandName = operand.getName();
                code += "aload_0\n" + "getfield " + className + "/" + operandName;
            }
            else{
                ElementType elementType = operand.getType().getTypeOfElement();
                switch (elementType) {
                    case INT32, BOOLEAN -> code += getIloadIstoreCode(id, true );
                    case CLASS,  STRING, OBJECTREF -> code += "aload" + (id <= 3 ? '_' : ' ') + id;
                    case THIS -> code += "aload_0";
                    case VOID -> {}
                }
            }
        }
        return code + "\n";
    }

    private String getStoreCode(Element e) {
        String code = "";

        if (e.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) e;
            if (literalElement.getType().getTypeOfElement() == INT32) {
                code += getIloadIstoreCode(Integer.parseInt(literalElement.getLiteral()), false);
            } else {
                code += "store " + literalElement.getLiteral();
            }
        } else {
            Operand operand = (Operand) e;
            int id = (operand.isParameter()) ? operand.getParamId() : this.varTable.get(operand.getName()).getVirtualReg();
            Type elemType = operand.getType();

            if (id < 0) {
                code += "putfield " + Utils.getType(elemType, classUnit) + "/" + operand.getName() + " " + Utils.getType(elemType, classUnit);
            } else {
                switch (elemType.getTypeOfElement()) {
                    case INT32, BOOLEAN -> code += getIloadIstoreCode(id, false);
                    case CLASS, STRING, ARRAYREF, OBJECTREF -> code += "astore" + (id <= 3 ? '_' : ' ') + id;
                    case THIS -> code += "astore_0";
                    case VOID -> { }
                }
            }
        }

        return code + "\n";
    }

    private String getIloadIstoreCode(int id, boolean load){

        String code = (load) ?  "iload" : "istore";

        return code + ((id >= 4) ? " " + id : "_" + id);
    }

    private String getIConstCode(String constValue) {

        int val = Integer.parseInt(constValue);

        if (val >= 0 && val < 6)
            return "iconst_" + constValue;
        else if (val >= 0 && val < 128)
            return "bipush " + constValue;
        else if (val >= 0 && val < 32768)
            return "sipush " + constValue;
        else
            return "ldc " + constValue;
    }

}
