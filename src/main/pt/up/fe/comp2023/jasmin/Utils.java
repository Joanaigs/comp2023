package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.ElementType;

public class Utils {
    ClassUnit classUnit;
    public int stackAtual;

    Utils(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public String getType(ElementType ollirType) {
        switch (ollirType){
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            default:
                return "";
        }
    }

    public String getReturnType(ElementType returnType) {
        switch (returnType) {
            case INT32:
                return "i";
            case BOOLEAN:
                return "i";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "";
            default:
                return "";
        }
    }

    public int getLimitStack(){ return 99; }

    public int getLimitLocals(){
        return 99;
    }

    public void sub2StackAtual() {
        this.stackAtual--;
    }


}
