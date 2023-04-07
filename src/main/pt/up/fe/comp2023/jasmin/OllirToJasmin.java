package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Field;
import org.specs.comp.ollir.Method;

import java.util.ArrayList;
import java.util.StringJoiner;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private final JasminUtils jasminUtils;


    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.jasminUtils = new JasminUtils(classUnit);
    }

    public String getCode() {
        String code = "";

        code += createHeader();
        code += createFields();
        code += createMethods();

        return code;
    }

    public String createHeader() {
        String className = classUnit.getClassName();
        String classPrivacy = classUnit.getClassAccessModifier().name();
        String atualClass = ".class " + classPrivacy + className + '\n';

        String superClassName = classUnit.getSuperClass();
        String superClass = ".super " + superClassName + '\n';

        return atualClass + superClass + "\n";
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
            createConstructMethod(method);
        else{
            code += createMethodDeclaration(method);
            code += createMethodBody(method) + "\n";
            code += ".end method\n";
        }

        return code;
    }

    public String createConstructMethod(Method method){
        String code = "";
        return code;
    }

    public String createMethodDeclaration(Method method){

        String code = ".method ";

        String methodPrivacy = method.getMethodAccessModifier().name();
        String methodAcessModifiers = createAccessModifiers(methodPrivacy, method.isFinalMethod(), method.isStaticMethod());
        String methodName = method.getMethodName();
        code += methodAcessModifiers + methodName + '(';

        for(Element param : method.getParams())
            code += param.getType().getTypeOfElement();

        String methodReturnType = jasminUtils.getType(method.getReturnType().getTypeOfElement());
        code += ')' + methodReturnType;

        return code;
    }

    public String createMethodBody(Method method){
        String code = "";

        return code;
    }

    public String createAccessModifiers(String privacy, Boolean isFinal, Boolean isStatic)
    {
        String code = "";

        if (!privacy.equals("DEFAULT"))
            code += privacy.toLowerCase() + " ";
        if (isFinal)
            code += "final ";
        if (isStatic)
            code += "static ";

        return code;
    }

}
