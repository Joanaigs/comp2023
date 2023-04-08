package pt.up.fe.comp2023.Ollir;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class Uteis {
    public static String typeOllir(Type type) {
        String ollirType = "";
        if (type.isArray()) {
            ollirType += "array.";
        }
        switch (type.getName()) {
            case "int":
                ollirType += "i32";
                break;
            case "void":
                ollirType += "V";
                break;
            case "boolean":
                ollirType += "bool";
                break;
            default:
                ollirType += type.getName();
                break;
        }
        return ollirType;
    }

    public static String typeOllir(JmmNode jmmNode) {
        String ollirType = "";
        if (jmmNode.getAttributes().contains("array")) {
            ollirType += "array.";
        }
        switch (jmmNode.get("type")) {
            case "int":
                ollirType += "i32";
                break;
            case "void":
                ollirType += "V";
                break;
            case "boolean":
                ollirType += "bool";
                break;
            default:
                ollirType += jmmNode.get("type");
                break;
        }
        return ollirType;
    }

    public static boolean LiteralorVariable(String id){
        return id.matches("(((_|[a-zA-z])(_|\\d|[a-zA-Z])*)\\.(([a-zA-z])(\\d|[a-zA-Z])*))|\\d|true|false|this");
    }
}
