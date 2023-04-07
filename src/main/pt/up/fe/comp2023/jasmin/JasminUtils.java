package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Type;

public class JasminUtils {
    ClassUnit classUnit;

    JasminUtils(ClassUnit classUnit) {
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

}
