package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashMap;

import static org.specs.comp.ollir.ElementType.*;

public class MethodInstruction {

    private final ClassUnit classUnit;
    private boolean isAssign;
    private HashMap<String, Descriptor> varTable;
    private static int conditionalID;


    MethodInstruction(ClassUnit classUnit, Method method)
    {
        this.classUnit = classUnit;
        this.varTable =  method.getVarTable();
        this.isAssign = false;
    }

    public String createInstructionCode(Instruction instruction){

        String code = "";
        switch(instruction.getInstType()){
            case ASSIGN:
                this.isAssign = true;
                code += getAssignCode((AssignInstruction) instruction);
                this.isAssign = false;
                break;
            case CALL:
                code += getInvokeCode( (CallInstruction) instruction);
                if (((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID)
                    if (!this.isAssign){
                        Utils.updateStackLimits(-1);
                        code += "pop\n";
                    }
                break;
            case GOTO:
                return getGotoCode((GotoInstruction) instruction);
            case BRANCH:
                return getBranchCode( (CondBranchInstruction) instruction);
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
                code += getUnaryOperCode( (UnaryOpInstruction) instruction);
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


    private boolean checkOpLiteral(Operand lhsOperand, Operand rhsOperand, LiteralElement literalElement, OperationType operationType) {
        if (lhsOperand.getName().equals(rhsOperand.getName())) {
            int literalValue = Integer.parseInt(literalElement.getLiteral());
            if(operationType.equals(OperationType.ADD))
                return (literalValue >= 0 && literalValue <= 127);
            else if(operationType.equals(OperationType.SUB))
                return (literalValue >= 0 && literalValue <= 128);
        }
        return false;
    }

    private String getAssignCode(AssignInstruction instruction) {
        String code  = "";

        // duvida no update
        Operand op = (Operand) instruction.getDest();

        if(op instanceof ArrayOperand){
            int virtualReg =  varTable.get((op).getName()).getVirtualReg();
            ArrayOperand operand = (ArrayOperand) op;
            code +=  "aload" + ((virtualReg > 3)? " " + virtualReg :  "_" + virtualReg) + "\n";  // Load array
            code += getLoadCode(operand.getIndexOperands().get(0)); // Load index
            code += createInstructionCode(instruction.getRhs());
            code += "iastore\n";
            Utils.updateStackLimits(2);
            return code;
        }
        code += createInstructionCode(instruction.getRhs()) + getStoreCode(op);


        if (varTable.get(op.getName()).getVarType().getTypeOfElement() == ElementType.ARRAYREF)
            Utils.updateStackLimits(-3);
        else {
            if (instruction.getRhs() instanceof BinaryOpInstruction binaryOpInstruction) {
                if (binaryOpInstruction.getOperation().getOpType().equals(OperationType.ADD) || binaryOpInstruction.getOperation().getOpType().equals(OperationType.SUB)) {
                    Element leftOp = binaryOpInstruction.getLeftOperand();
                    Element rightOp = binaryOpInstruction.getRightOperand();
                    Operand operand = null;
                    LiteralElement literal = null;
                    if (leftOp.isLiteral() && !rightOp.isLiteral()) {
                        literal = (LiteralElement) leftOp;
                        operand = (Operand) rightOp;

                    } else if (!leftOp.isLiteral() && rightOp.isLiteral()) {
                        literal = (LiteralElement) rightOp;
                        operand = (Operand) leftOp;
                    }

                    OperationType operationType = binaryOpInstruction.getOperation().getOpType();
                    if(operand != null && checkOpLiteral(op, operand, literal, operationType)){
                        String posOrNeg = " ";
                        if(operationType.equals(OperationType.SUB))
                            posOrNeg = " -";
                        return "iinc " + varTable.get(operand.getName()).getVirtualReg() + posOrNeg + Integer.parseInt(literal.getLiteral()) + "\n";
                    }
                }
            }
        }
        return code;
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

    public String getNoperCode(SingleOpInstruction instruction){

        var element = instruction.getSingleOperand();
        return getLoadCode(element);
    }

    private String getGotoCode(GotoInstruction instruction){
        return "goto " + instruction.getLabel() + "\n";
    }

    private String getBranchCode(CondBranchInstruction instruction) {

        Instruction condition = instruction.getCondition();
        return createInstructionCode(condition) + "ifne " + instruction.getLabel() + "\n";
    }

    private String getUnaryOperCode(UnaryOpInstruction instruction) {
        return getLoadCode(instruction.getOperand()) +  getBooleanOpResultCode("ifeq");
    }

    private String getBinaryOperCode(BinaryOpInstruction instruction) {
        String code = "";
        var instructionType  = instruction.getOperation().getOpType();

        switch (instructionType) {
            case ADD, SUB, MUL, DIV -> {
                code += getArithmeticCode(instruction, instructionType);
            }
            case EQ, NEQ, GTH, GTE, LTH, LTE, AND, ANDB -> {
                code += getBooleanCode(instruction, instructionType);
            }
            default ->{}
        }
        return code;
    }

    private String getArithmeticCode(BinaryOpInstruction instruction, OperationType instructionType) {

        String leftOperand = getLoadCode(instruction.getLeftOperand());
        String rightOperand = getLoadCode(instruction.getRightOperand());
        Utils.updateStackLimits(-1);
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

    private String getBooleanOpResultCode(String op){
        StringBuilder code = new StringBuilder();

        code.append(op + " TRUE" + conditionalID + "\n" );
        code.append("iconst_0\n");
        code.append("goto FALSE" + conditionalID +"\n");
        code.append("TRUE" + conditionalID + ":\n");
        code.append("iconst_1\n");
        code.append("FALSE" + conditionalID + ":\n");
        //update
        conditionalID++;

        return code.toString();
    }

    private int getBooleanBothLiteralCode(LiteralElement leftOperand, LiteralElement rightOperand, OperationType operationType){

        boolean left  = leftOperand.getLiteral().equals("1");
        boolean right = rightOperand.getLiteral().equals("1");
        boolean result;

        switch (operationType) {
            case AND, ANDB -> result = left && right;
            case OR, ORB -> result = left || right;
            case EQ ->  result = left  ==  right;
            case NEQ -> result = left != right;
            case GTE, GTH ->  result =  Integer.parseInt(leftOperand.getLiteral()) > Integer.parseInt(rightOperand.getLiteral());
            case LTE, LTH ->  result =  Integer.parseInt(leftOperand.getLiteral()) < Integer.parseInt(rightOperand.getLiteral());
            default ->
                result = false;
        }

        return (result)? 1: 0;
    }


    private String getBooleanCode(BinaryOpInstruction instruction, OperationType operationType) {

        String code = "";
        Element leftOperand = instruction.getLeftOperand();
        Element rightOperand = instruction.getRightOperand();

        if (leftOperand.isLiteral() && rightOperand.isLiteral()) {
            int value = getBooleanBothLiteralCode( (LiteralElement) leftOperand, (LiteralElement) rightOperand, operationType);
            code += "iconst_" + value + "\n";
        }
        else {
            code += getLoadCode(leftOperand) + getLoadCode(rightOperand);
            switch (operationType) {
                case ANDB, AND -> code += "iand\n";
                case LTH, LTE -> code += getBooleanOpResultCode("if_icmplt");
                case GTH, GTE -> code += getBooleanOpResultCode("if_icmpgt");
                case EQ  -> code += getBooleanOpResultCode("ifeq");
                case NEQ -> code += getBooleanOpResultCode("ifneq");
                default -> code += "";
            }
        }
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

    public String getInvokeCode(CallInstruction instruction) {

        switch (instruction.getInvocationType()) {
            case invokestatic:
                Utils.updateStackLimits(-1);
                return getInvokeStaticCode(instruction);
            case invokevirtual:
                return getInvokeVirtualCode(instruction);
            case invokespecial:
                return getInvokeSpecialCode(instruction);
            case arraylength:
                return getLoadCode(instruction.getFirstArg()) + "arraylength\n";
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

        for (Element element : instruction.getListOfOperands()) {
            code += getLoadCode(element);
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
        }
        else {
            code += getLoadCode(firstArg) + "invokespecial " + Utils.getClassPath(((ClassType) instruction.getFirstArg().getType()).getName(), classUnit);
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
            Utils.updateStackLimits(1);
            code += "new " + Utils.getClassPath(((Operand) instruction.getFirstArg()).getName(), classUnit) + "\ndup\n";
        }
        return code;
    }

    public String getLoadCode(Element e){
        String code = "";


        if (e.isLiteral()) {
            Utils.updateStackLimits(1);
            LiteralElement literalElement = (LiteralElement) e;
            var elementType = literalElement.getType().getTypeOfElement();
            switch (elementType) {
                case INT32,BOOLEAN -> code += getIConstCode( literalElement.getLiteral() );
                default -> code += "ldc " + literalElement.getLiteral();
            }
        }
        else if (e instanceof ArrayOperand) {
            ArrayOperand operand = (ArrayOperand) e;

            int virtualReg =  varTable.get(((ArrayOperand) e).getName()).getVirtualReg();
            code +=  "aload" + ((virtualReg > 3)? " " + virtualReg :  "_" + virtualReg) + "\n";  // Load array
            code += getLoadCode(operand.getIndexOperands().get(0)) + "iaload\n"; // Load index

        }
        else if (e instanceof Operand){
            Operand operand = (Operand) e;
            Utils.updateStackLimits(1);
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
                    }
                    case CLASS -> {
                        code += "";
                    }
                    case STRING, OBJECTREF, ARRAYREF-> {
                        code += "aload" + (id <= 3 ? '_' : ' ') + id;
                    }
                    case THIS -> {
                        code += "aload_0";
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
        }
        else if (e instanceof ArrayOperand) {
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
                    }
                    case CLASS, STRING, ARRAYREF, OBJECTREF -> {
                        code += "astore" + (id <= 3 ? '_' : ' ') + id;
                    }
                    case THIS -> {
                        code += "astore_0";
                    }
                    case VOID -> {
                    }
                }
            }
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
