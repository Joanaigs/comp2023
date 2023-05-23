package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.Objects;

public class OllirToJasmin {

    private final ClassUnit classUnit;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public String getCode() {
        String code = "";

        code += createClass();
        code += createExtendedClass();
        code += createFields();
        code += createMethods();

        return code;
    }

    public String createClass(){
        String className = classUnit.getClassName();
        String classPrivacy = classUnit.getClassAccessModifier().name();
        String isPublic = (classPrivacy.equals("DEFAULT"))? "public " : "";
        String acessModifiers = createAccessModifiers(classPrivacy, classUnit.isStaticClass(), classUnit.isFinalClass());
        String atualClass = ".class " + isPublic + acessModifiers + className + '\n';

        return atualClass;
    }

    public String createExtendedClass(){

        if (classUnit.getSuperClass() == null)
            return ".super java/lang/Object\n";

        return ".super " + classUnit.getSuperClass() + "\n";

    }

    public String createFields() {
        String code = "";

        for (Field field : classUnit.getFields())
            code += createOneField(field) +  '\n';


        return code + "\n";
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

        String code = "";

        for (Method method : classUnit.getMethods()) {
            code += createOneMethod(method);
        }

        return code;
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

        String code = ".method ";

        String methodPrivacy = method.getMethodAccessModifier().name();
        String methodAcessModifiers = createAccessModifiers(methodPrivacy, method.isFinalMethod(), method.isStaticMethod());
        String methodName = method.getMethodName();
        code += methodAcessModifiers + methodName + '(';

        for(Element param : method.getParams())
            code += Utils.getType(param.getType(), classUnit);

        String methodReturnType = Utils.getType(method.getReturnType(), classUnit);
        code += ')' + methodReturnType + "\n";

        return code;
    }

    private String getMethodLimits(Method method) {
        String code = "";

        int localLimit =  method.getVarTable().size() +
                (method.getVarTable().containsKey("this") || method.isStaticMethod() ? 0 : 1);


        code += ".limit stack " + Utils.stackLimit + "\n";
        code += ".limit locals " + localLimit + "\n";

        return code;
    }

    public String createMethodBody(Method method){

        String instructions = "";
        for (Instruction instruction : method.getInstructions()) {
            MethodInstruction jasminInstruction = new MethodInstruction(this.classUnit, method);
            instructions += jasminInstruction.createInstructionCode(instruction);
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
