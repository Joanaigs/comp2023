package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private final Utils jasminUtils;


    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.jasminUtils = new Utils(classUnit);
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
        String isPublic = (classPrivacy == "DEFAULT")? "public " : "";
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
        String fieldType = jasminUtils.getType(field.getFieldType().getTypeOfElement()) + " ";
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

        return "\n.method public <init>()V\naload_0\ninvokespecial " + extendedClass +  ".<init>()V\nreturn\n.end method\n";
    }

    public String createMethodHeader(Method method){

        String code = ".method ";

        String methodPrivacy = method.getMethodAccessModifier().name();
        String methodAcessModifiers = createAccessModifiers(methodPrivacy, method.isFinalMethod(), method.isStaticMethod());
        String methodName = method.getMethodName();
        code += methodAcessModifiers + methodName + '(';

        for(Element param : method.getParams())
            code += this.jasminUtils.getType(param.getType().getTypeOfElement());

        String methodReturnType = this.jasminUtils.getType(method.getReturnType().getTypeOfElement());
        code += ')' + methodReturnType + "\n";

        return code;
    }

    public String createMethodBody(Method method){

        String code = ".limit stack 99\n";
        code += ".limit locals 99\n";

        for (int i = 0; i < method.getInstructions().size(); i++) {

            MethodInstruction jasminInstruction = new MethodInstruction(this.classUnit, method);
            var instruction = method.getInstr(i);

            code += jasminInstruction.createInstructionCode(instruction);
        }

        return code;
    }

    public String createAccessModifiers(String privacy, Boolean isFinal, Boolean isStatic)
    {
        String code = "";

        if (privacy != "DEFAULT")
            code += privacy.toLowerCase() + " ";
        if (isFinal)
            code += "final ";
        if (isStatic)
            code += "static ";

        return code;
    }

}
