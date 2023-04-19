package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashMap;

public class MethodInstruction {

    private final ClassUnit classUnit;
    private HashMap<String, Descriptor> varTable;

    MethodInstruction(ClassUnit classUnit, Method method)
    {
        this.classUnit = classUnit;
        this.varTable =  method.getVarTable();
    }

    public String createInstructionCode(Instruction instruction){

        String code = "";
        System.out.println(instruction.getInstType());
        switch(instruction.getInstType()){
            case ASSIGN :
                // assign
                code += getAssignCode( (AssignInstruction) instruction);
                break;
            case CALL:
                // invoke methods
                code += getInvokeCode( (CallInstruction) instruction);
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
                code += getPutFieldCode((PutFieldInstruction) instruction);
                break;
            case GETFIELD:
                code += getGetFieldCode( (GetFieldInstruction) instruction);
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
        String code = "";
        var element = instruction.getSingleOperand();
        code += getLoadCode(element);

        return code;
    }
    
    public String getReturnCode(ReturnInstruction instruction){
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
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                code += getArithmeticCode(instruction, instructionType);
                break;
            default:
                break;
        }
        return code;
    }

    private String getAssignCode(AssignInstruction instruction) {
        String code  = "";

        Operand dest = (Operand) instruction.getDest();

        if (instruction.getRhs().getInstType() == InstructionType.BINARYOPER) {
            BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) instruction.getRhs();

            if (binaryOpInstruction.getOperation().getOpType() == OperationType.ADD) {
                boolean leftIsLiteral = binaryOpInstruction.getLeftOperand().isLiteral();
                boolean rightIsLiteral = binaryOpInstruction.getRightOperand().isLiteral();

                LiteralElement literal = null;
                Operand operand = null;

                if (leftIsLiteral && !rightIsLiteral) {
                    literal = (LiteralElement) binaryOpInstruction.getLeftOperand();
                    operand = (Operand) binaryOpInstruction.getRightOperand();
                } else if (!leftIsLiteral && rightIsLiteral) {
                    literal = (LiteralElement) binaryOpInstruction.getRightOperand();
                    operand = (Operand) binaryOpInstruction.getLeftOperand();
                }

                if (literal != null && operand != null) {
                    if (operand.getName().equals(dest.getName())) {
                        int literalValue = Integer.parseInt((literal).getLiteral());

                        if (literalValue >= -128 && literalValue <= 127) {
                            return "\tiinc " + varTable.get(operand.getName()).getVirtualReg() + " " + literalValue + "\n";
                        }
                    }
                }

            }
        }

        code += createInstructionCode(instruction.getRhs()) + getStoreCode(dest);

        return code;

    }

    private String getArithmeticCode(BinaryOpInstruction instruction, OperationType instructionType) {
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

    public String getInvokeCode(CallInstruction instruction) {

        switch (instruction.getInvocationType()) {
            case invokestatic:
                return getInvokeStaticCode(instruction);
            case invokevirtual:
                return getInvokeVirtualCode(instruction);
            case invokespecial:
                return getInvokeSpecialCode(instruction);
            case NEW:
                return getNewCode(instruction);
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
            code += getLoadCode(firstArg) + "\tinvokespecial " + superClassName;
        else
            code += getLoadCode(firstArg) + "\tinvokespecial " + Utils.getClassPath(((ClassType) instruction.getFirstArg().getType()).getName(), classUnit);

        code += "/<init>(";

        for (Element element : instruction.getListOfOperands()) {
            code += Utils.getType(element.getType(), classUnit);
        }

        code += ")" + Utils.getType(instruction.getReturnType(), classUnit) + "\n";

        return code;
    }

    private String getNewCode(CallInstruction instruction) {
        String code = "";

        for (Element element : instruction.getListOfOperands()) {
            code += getLoadCode(element);
        }

        return code + "\tnew " + Utils.getClassPath(((Operand) instruction.getFirstArg()).getName(), classUnit) + "\ndup\n";
    }

    public String getLoadCode(Element e){
        String code = "";

        if (e.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) e;
            var elementType = literalElement.getType().getTypeOfElement();
            switch (elementType) {
                case INT32:
                case BOOLEAN:
                    code += getIConstCode( literalElement.getLiteral() );
                    break;
                default:
                    code += "ldc " + literalElement.getLiteral();
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
                code += "aload_0\n" + "getfield " + className + "/" + operandName;
            }
            else{
                ElementType elementType = operand.getType().getTypeOfElement();
                switch (elementType) {
                    case INT32:
                    case BOOLEAN:
                        code += getIloadIstoreCode(id, true );
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
        return code + "\n";
    }

    public String getStoreCode(Element e){
        String code = "";

        if (e.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) e;
            switch (literalElement.getType().getTypeOfElement()) {
                case INT32:
                    code += getIloadIstoreCode( Integer.parseInt(literalElement.getLiteral()), false );
                    break;
                default:
                    code += "store " +  literalElement.getLiteral();
                    break;
            }
        } else {
            Operand operand = (Operand) e;
            int id = (operand.isParameter())? operand.getParamId() : this.varTable.get(operand.getName()).getVirtualReg();
            Type elemType = operand.getType();

            if (id < 0) {
                code += "putfield " + Utils.getType(elemType, classUnit) + "/" + operand.getName() + " " + Utils.getType(elemType, classUnit);
            }else
                switch (elemType.getTypeOfElement()) {
                    case INT32:
                    case BOOLEAN:
                        code += getIloadIstoreCode(id, false );
                        break;
                    case CLASS:
                    case STRING:
                    case ARRAYREF:
                    case OBJECTREF:
                        code += "astore" + (id <= 3 ? '_' : ' ') + id;
                        break;
                    case THIS:
                        code += "astore_0";
                        break;
                    case VOID:
                        break;
                }
        }

        return code + "\n";
    }

    public String getIloadIstoreCode(int id, boolean load){

        String code = (load) ?  "iload" : "istore";

        return code + ((id >= 4) ? " " + id : "_" + id);
    }

    public String getIConstCode(String constValue) {

        String code = "";

        int val = Integer.parseInt(constValue);
        if (val >= 0 && val < 6)
            code += "iconst_";
        else if (val >= 0 && val < 128)
            code += "bipush ";
        else if (val >= 0 && val < 32768)
            code += "sipush ";
        else
            code += "ldc ";

        return code + constValue;
    }

}
