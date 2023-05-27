package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class OllirToJasmin {

    private final ClassUnit classUnit;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public String getCode() {

        return  createClass() +
                createExtendedClass() +
                createFields() +
                createMethods();
    }

    public String createClass(){
        String className = classUnit.getClassName();
        String classPrivacy = classUnit.getClassAccessModifier().name();
        String isPublic = (classPrivacy.equals("DEFAULT"))? "public " : "";
        String acessModifiers = createAccessModifiers(classPrivacy, classUnit.isStaticClass(), classUnit.isFinalClass());

        return ".class " + isPublic + acessModifiers + className + '\n';
    }

    public String createExtendedClass(){

        if (classUnit.getSuperClass() == null)
            return ".super java/lang/Object\n";

        return ".super " + classUnit.getSuperClass() + "\n";

    }

    public String createFields() {
        StringBuilder code = new StringBuilder();

        for (Field field : classUnit.getFields())
            code.append(createOneField(field)).append('\n');


        return (code.append("\n")).toString();
    }

    public String createOneField(Field field) {
        StringBuilder code = new StringBuilder(".field ");

        String fieldPrivacy = field.getFieldAccessModifier().name();
        String fieldAccessModifiers = createAccessModifiers(fieldPrivacy, field.isFinalField(), field.isStaticField());
        String fieldName = field.getFieldName() + " ";
        String fieldType = Utils.getType(field.getFieldType(), classUnit) + " ";

        code.append(fieldAccessModifiers)
                    .append(fieldName)
                    .append(fieldType)
                    .append(field.isInitialized() ? "=" + field.getInitialValue() : "");

        return code.toString();
    }


    public String createMethods() {

        StringBuilder code = new StringBuilder();

        for (Method method : classUnit.getMethods()) {
            code.append(createOneMethod(method));
        }

        return code.toString();
    }

    public String createOneMethod(Method method) {
        StringBuilder code = new StringBuilder();

        if (method.isConstructMethod()) {
            code.append(createConstructMethod());
        } else {
            code.append(createMethodHeader(method))
                        .append(createMethodBody(method))
                        .append(".end method\n");
        }

        return code.toString();
    }


    public String createConstructMethod(){
        String extendedClass = (classUnit.getSuperClass() == null)? "java/lang/Object" : classUnit.getSuperClass();

        return "\n.method public <init>()V\naload_0\ninvokespecial " + extendedClass +  "/<init>()V\nreturn\n.end method\n";
    }

    public String createMethodHeader(Method method){

        StringBuilder code = new StringBuilder(".method ");

        String methodPrivacy = method.getMethodAccessModifier().name();
        String methodAcessModifiers = createAccessModifiers(methodPrivacy, method.isFinalMethod(), method.isStaticMethod());
        String methodName = method.getMethodName();
        code.append(methodAcessModifiers).append(methodName).append('(');

        for(Element param : method.getParams())
            code.append(Utils.getType(param.getType(), classUnit));

        String methodReturnType = Utils.getType(method.getReturnType(), classUnit);
        code.append(')').append(methodReturnType).append("\n");

        return code.toString();
    }

    private String getMethodLimits(Method method) {
        StringBuilder code = new StringBuilder();

        Set<Integer> registers = new HashSet<>();
        registers.add(0);

        for (Descriptor descriptor : method.getVarTable().values()) {
            registers.add(descriptor.getVirtualReg());
        }
        int localLimit = registers.size();

        code.append(".limit stack ").append(Utils.stackLimit).append("\n")
                .append(".limit locals ").append(localLimit).append("\n");

        return code.toString();
    }


    public String createMethodBody(Method method){

        Utils.resetStackLimits();

        StringBuilder instructions = new StringBuilder();
        for (Instruction instruction : method.getInstructions()) {
            var labels = method.getLabels(instruction);
            for(String label : labels){
                instructions.append(label).append(":\n");
            }
            MethodInstruction jasminInstruction = new MethodInstruction(this.classUnit, method);
            instructions.append(jasminInstruction.createInstructionCode(instruction));
        }

        return getMethodLimits(method) + instructions;
    }

    public String createAccessModifiers(String privacy, Boolean isFinal, Boolean isStatic)
    {
        String code = "";

        if (!Objects.equals(privacy, "DEFAULT"))
            code += privacy.toLowerCase() + " ";
        if (isFinal)
            code += "final ";
        if (isStatic)
            code += "static ";

        return code;
    }


}
