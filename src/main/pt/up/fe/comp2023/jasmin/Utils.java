package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.Objects;
public class Utils {

    static int stackLimit;
    static int tempStack;

    public static String getType(Type type, ClassUnit classUnit){
        return switch (type.getTypeOfElement()) {
            case INT32 -> "I";
            case BOOLEAN -> "Z";
            case STRING -> "Ljava/lang/String;";
            case VOID -> "V";
            case ARRAYREF -> "[" + getType(((ArrayType) type).getElementType(), classUnit);
            case OBJECTREF -> "L" + getClassPath(((ClassType) type).getName(), classUnit) + ";";
            default -> "";
        };
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
            case ARRAYREF, OBJECTREF -> "a";
            default -> "";
        };
    }

    public static void updateStackLimits(int value) {
        Utils.tempStack += value;
        Utils.stackLimit = Math.max(Utils.stackLimit, Utils.tempStack);
    }

    public static void resetStackLimits() {
        Utils.tempStack = 0;
        Utils.stackLimit = 0;
    }


}
