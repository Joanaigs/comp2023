package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class OllirToJasmin {

    private final ClassUnit classUnit;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public String getCode() {
        StringBuilder codeBuilder = new StringBuilder();

        codeBuilder.append(createClass())
                .append(createExtendedClass())
                .append(createFields())
                .append(createMethods());

        return codeBuilder.toString();
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

        String code = ".field ";

        String fieldPrivacy = field.getFieldAccessModifier().name();
        String fieldAccessModifiers = createAccessModifiers(fieldPrivacy, field.isFinalField(), field.isStaticField());
        String fieldName = field.getFieldName() + " ";
        String fieldType = Utils.getType(field.getFieldType(), classUnit) + " ";
        code += fieldAccessModifiers +  fieldName + fieldType;
        code += field.isInitialized() ? "=" + field.getInitialValue() : "";

        return code;
    }

    public String createMethods() {

        StringBuilder code = new StringBuilder();

        for (Method method : classUnit.getMethods()) {
            code.append(createOneMethod(method));
        }

        return code.toString();
    }

    public String createOneMethod(Method method){
        String code = "";

        if(method.isConstructMethod())
            code += createConstructMethod();
        else{
            code += createMethodHeader(method);
            code += createMethodBody(method);
            code += ".end method\n";
        }

        return code;
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
        String code = "";

        Set<Integer> registers = new HashSet<>();
        registers.add(0);

        for(Map.Entry<String, Descriptor> var: method.getVarTable().entrySet()){
           registers.add(var.getValue().getVirtualReg());
        }
        int localLimit =  registers.size();

        code += ".limit stack " + Utils.stackLimit + "\n";
        code += ".limit locals " + localLimit + "\n";

        return code;
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
