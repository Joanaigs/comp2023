package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.Objects;

public class Utils {

    static int stackLimit;
    static int currentStack;

    public static String getType(Type type, ClassUnit classUnit){
        switch (type.getTypeOfElement()){
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            case ARRAYREF:
                return "[" + getType(((ArrayType) type).getElementType(), classUnit);
            case OBJECTREF:
                return  "L" + getClassPath( ((ClassType) type).getName(), classUnit ) + ";";
            default:
                return "";
        }
    }

    public static String getClassPath(String className, ClassUnit classUnit) {

        if (Objects.equals(className, "this"))
            return classUnit.getClassName();

        for (String importName : classUnit.getImports()) {
            if (importName.endsWith(className)) {
                return importName.replaceAll("\\.", "/");
            }
        }

        return className;
    }


    public static String getReturnType(ElementType returnType) {
        return switch (returnType) {
            case INT32, BOOLEAN -> "i";
            case STRING -> "Ljava/lang/String;";
            case VOID -> "";
            default -> "a";
        };
    }

}
