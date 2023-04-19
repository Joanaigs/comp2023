package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

public class Utils {

    public String getType(Type type, ClassUnit classUnit){
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
            case OBJECTREF: {
                String classTypeName = ((ClassType) type).getName();
                return  "L" + getClassPath(classUnit.getClassName(), classUnit) + ";";
            }
            default:
                return "";
        }
    }

    public String getClassPath(String className, ClassUnit classUnit) {

        if (className == "this")
            return classUnit.getClassName();

        for (String importName : classUnit.getImports()) {
            if (importName.endsWith(className)) {
                return importName.replaceAll("\\.", "/");
            }
        }

        return className;
    }


    public String getReturnType(ElementType returnType) {
        switch (returnType) {
            case INT32:
            case BOOLEAN:
                return "i";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "";
            default:
                return "a";
        }
    }

    public int getLimitStack(){ return 99; }

    public int getLimitLocals(){
        return 99;
    }

}
