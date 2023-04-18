package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

public class Utils {
    public int stackAtual;

    public String getType(ElementType elemType){
        switch (elemType){
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            case CLASS:
                return "CLASS";
            default:
                return "";
        }
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
