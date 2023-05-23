package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashMap;

import static org.specs.comp.ollir.ElementType.*;

public class MethodInstruction {

    private final ClassUnit classUnit;
    private boolean isAssign;
    private HashMap<String, Descriptor> varTable;

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
                this.isAssign = true;
                code += getAssignCode( (AssignInstruction) instruction);
                this.isAssign = false;
                break;
            case CALL:
                code += getInvokeCode( (CallInstruction) instruction);
                if (((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID)
                    if (!this.isAssign){
                        code += "pop\n";
                        Utils.updateStackLimits(-1);
                    }
                break;
            case GOTO:
                break;
            case BRANCH:
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

    public String getNoperCode(SingleOpInstruction instruction){

        var element = instruction.getSingleOperand();
        return getLoadCode(element);
    }

    public String getReturnCode(ReturnInstruction instruction){
        String code = "";

        if(instruction.hasReturnValue()) {
            String loadCode = getLoadCode(instruction.getOperand());
            ElementType elementType = instruction.getOperand().getType().getTypeOfElement();
            String returnType = Utils.getReturnType(elementType);

            code +=  loadCode +  returnType;
        }

        return code + "return\n";
    }

    private String getBinaryOperCode(BinaryOpInstruction instruction) {
        String code = "";
        var instructionType  = instruction.getOperation().getOpType();

        switch (instructionType) {
            case ADD, SUB, MUL, DIV -> {
                code += getArithmeticCode(instruction, instructionType);
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
        Utils.updateStackLimits(-2);

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
                return getInvokeNewCode(instruction);
            default:
                return "";
        }
    }

    private String getInvokeStaticCode(CallInstruction instruction) {
        String code = "";

        for (Element element : instruction.getListOfOperands()) {
            code += getLoadCode(element);
            Utils.updateStackLimits(1);
        }


        code += "invokestatic "
                + Utils.getClassPath(((Operand) instruction.getFirstArg()).getName(), classUnit)
                + "/"
                + ((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "")
                + "(";

        for (Element element : instruction.getListOfOperands())
            code += Utils.getType(element.getType(), classUnit);


        return code + ")" + Utils.getType(instruction.getReturnType(), classUnit) + "\n";

    }

    private String getInvokeVirtualCode(CallInstruction instruction) {

        String code = getLoadCode(instruction.getFirstArg());
        Utils.updateStackLimits(1);

        for (Element element : instruction.getListOfOperands()) {
            code += getLoadCode(element);
            Utils.updateStackLimits(1);
        }

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

        if (firstArg.getType().getTypeOfElement() == ElementType.THIS) {
            code += getLoadCode(firstArg) + "invokespecial " + superClassName;
            Utils.updateStackLimits(1);
        }
        else {
            code += getLoadCode(firstArg) + "invokespecial " + Utils.getClassPath(((ClassType) instruction.getFirstArg().getType()).getName(), classUnit);
            Utils.updateStackLimits(1);
        }

        code += "/<init>(";

        for (Element element : instruction.getListOfOperands()) {
            code += Utils.getType(element.getType(), classUnit);
        }

        code += ")" + Utils.getType(instruction.getReturnType(), classUnit) + "\n";

        return code;
    }

    private String getInvokeNewCode(CallInstruction instruction) {
        String code = "";

        Element e = instruction.getFirstArg();

        if (e.getType().getTypeOfElement().equals(ElementType.ARRAYREF)) {
            code += getLoadCode(instruction.getListOfOperands().get(0)) +  "newarray int\n";
        }
        else if (e.getType().getTypeOfElement().equals(ElementType.OBJECTREF)){
            code += "new " + Utils.getClassPath(((Operand) instruction.getFirstArg()).getName(), classUnit) + "\ndup\n";
            Utils.updateStackLimits(1);
        }

        return code;
    }

    public String getLoadCode(Element e){
        String code = "";

        if (e.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) e;
            var elementType = literalElement.getType().getTypeOfElement();
            switch (elementType) {
                case INT32,BOOLEAN -> code += getIConstCode( literalElement.getLiteral() );
                default -> code += "ldc " + literalElement.getLiteral();
            }
            Utils.updateStackLimits(1);
        }
        else if (e instanceof ArrayOperand) {
            ArrayOperand operand = (ArrayOperand) e;

            // Load array
            int virtualReg =  varTable.get(((ArrayOperand) e).getName()).getVirtualReg();
            code +=  "aload%s\n" + ((virtualReg > 3)? " " + virtualReg :  "_" + virtualReg).toString();
            Utils.updateStackLimits(1);

            // Load index
            code += getLoadCode(operand.getIndexOperands().get(0));
            Utils.updateStackLimits(-1);

            code += "iaload\n";
        }
        else if (e instanceof Operand){
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
                    case INT32, BOOLEAN -> {
                        code += getIloadIstoreCode(id, true );
                        Utils.updateStackLimits(1);
                    }
                    case CLASS -> {
                        code += "";
                    }
                    case STRING, OBJECTREF -> {
                        code += "aload" + (id <= 3 ? '_' : ' ') + id;
                        Utils.updateStackLimits(1);
                    }
                    case THIS -> {
                        code += "aload_0";
                        Utils.updateStackLimits(1);
                    }
                    case VOID -> {}
                }
            }
        }
        return code + "\n";
    }

    public String getStoreCode(Element e) {
        String code = "";

        if (e.isLiteral()) {
            LiteralElement literalElement = (LiteralElement) e;
            if (literalElement.getType().getTypeOfElement() == INT32) {
                code += getIloadIstoreCode(Integer.parseInt(literalElement.getLiteral()), false);
            } else {
                code += "store " + literalElement.getLiteral();
            }
            Utils.updateStackLimits(-1);
        }
        else if (e instanceof ArrayOperand) {
            Utils.updateStackLimits(-3);
            code += "iastore\n";
        }
        else {
            Operand operand = (Operand) e;
            int id = (operand.isParameter()) ? operand.getParamId() : this.varTable.get(operand.getName()).getVirtualReg();
            Type elemType = operand.getType();

            if (id < 0) {
                code += "putfield " + Utils.getType(elemType, classUnit) + "/" + operand.getName() + " " + Utils.getType(elemType, classUnit);
            } else {
                switch (elemType.getTypeOfElement()) {
                    case INT32, BOOLEAN -> {
                        code += getIloadIstoreCode(id, false);
                        Utils.updateStackLimits(-1);
                    }
                    case CLASS, STRING, ARRAYREF, OBJECTREF -> {
                        code += "astore" + (id <= 3 ? '_' : ' ') + id;
                        if(elemType.getTypeOfElement().equals(ARRAYREF) || elemType.getTypeOfElement().equals(OBJECTREF))
                            Utils.updateStackLimits(-1);
                    }
                    case THIS -> {
                        code += "astore_0";
                    }
                    case VOID -> {
                    }
                }
            }
            if(varTable.get(operand.getName()).getVarType().getTypeOfElement() == ElementType.ARRAYREF) Utils.updateStackLimits(-3);
            else Utils.updateStackLimits(-1);
        }


        return code + "\n";
    }

    public String getIloadIstoreCode(int id, boolean load){

        String code = (load) ?  "iload" : "istore";

        return code + ((id >= 4) ? " " + id : "_" + id);
    }

    public String getIConstCode(String constValue) {

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
