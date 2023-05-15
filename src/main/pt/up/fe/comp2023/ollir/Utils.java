package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class Utils {
    public static String typeOllir(Type type) {
        String ollirType = "";
        if (type.isArray()) {
            ollirType += "array.";
        }
        switch (type.getName()) {
            case "int" -> ollirType += "i32";
            case "void" -> ollirType += "V";
            case "boolean" -> ollirType += "bool";
            default -> ollirType += type.getName();
        }
        return ollirType;
    }

    public static String typeOllir(JmmNode jmmNode) {
        String ollirType = "";
        if (jmmNode.getAttributes().contains("array")) {
            ollirType += "array.";
        }
        switch (jmmNode.get("type")) {
            case "int" -> ollirType += "i32";
            case "void" -> ollirType += "V";
            case "boolean" -> ollirType += "bool";
            default -> ollirType += jmmNode.get("type");
        }
        return ollirType;
    }

    public static void addNewNodeInfo(JmmNode jmmNode, JmmNode newNode) {
        newNode.put("colEnd", jmmNode.get("colEnd"));
        newNode.put("colStart", jmmNode.get("colStart"));
        newNode.put("lineStart", jmmNode.get("lineStart"));
        if(jmmNode.hasAttribute("scope"))
            newNode.put("scope", jmmNode.get("scope"));
        if(jmmNode.hasAttribute("type"))
            newNode.put("type", jmmNode.get("type"));
        newNode.put("lineEnd", jmmNode.get("lineEnd"));
        jmmNode.replace(newNode);
    }
}
