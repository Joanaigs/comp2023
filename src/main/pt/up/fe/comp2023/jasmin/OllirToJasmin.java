package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;

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
        ArrayList<Field> fields = classUnit.getFields();

        for (Field field : fields) {
            code += createOneField(field) +  '\n';
        }

        return code + "\n";
    }

    public String createOneField(Field field) {

        String code = ".field ";

        String fieldPrivacy = field.getFieldAccessModifier().name();
        String fieldAccessModifiers = accessModifiers(fieldPrivacy, field.isFinalField(), field.isStaticField());
        String fieldName = field.getFieldName();
        String fieldType = jasminUtils.getType(field.getFieldType().getTypeOfElement());
        code += fieldPrivacy + fieldAccessModifiers + fieldName + fieldType;
        code += field.isInitialized() ? "=" + field.getInitialValue() : "";

        return code;
    }

    public String accessModifiers(String privacy, Boolean isFinal, Boolean isStatic)
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
