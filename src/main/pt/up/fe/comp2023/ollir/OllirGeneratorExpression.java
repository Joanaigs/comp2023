package pt.up.fe.comp2023.ollir;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.semantic_analysis.SymbolTable;


public class OllirGeneratorExpression extends AJmmVisitor<String, String> {
    StringBuilder code;
    SymbolTable symbolTable;
    int numTemVars = 0;

    public String getCode() {
        return code.toString();
    }

    OllirGeneratorExpression(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        code = new StringBuilder();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::ignore);
        addVisit("CreateArray", this::visitCreateArray);
        addVisit("InitializeClass", this::visitInitializeClass);
        addVisit("NegateExpr", this::visitNegateExpr);
        addVisit("ParenthesisExpr", this::visitParenthesisExpr);
        addVisit("ArrayExp", this::visitArrayExp);
        addVisit("CallFnc", this::visitCallFnc);
        addVisit("GetLength", this::visitGetLenght);
        addVisit("BinaryOp", this::visitBinaryOp);
        addVisit("Integer", this::visitInteger);
        addVisit("Boolean", this::visitBoolean);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("This", this::visitThis);
    }

    private String visitThis(JmmNode jmmNode, String s) {

        return "this."+symbolTable.getClassName();
    }

    private String visitIdentifier(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("value");
        Pair<Symbol, String> info = symbolTable.getSymbol(s, varName);
        switch (info.b) {
            case "FIELD" -> {
                String newTempVar = "t" + this.numTemVars++;
                String type = Utils.typeOllir(info.a.getType());
                code.append(String.format("%s.%s :=.%s getfield(this, %s.%s).%s;\n", newTempVar, type, type, varName, type, type));
                return String.format("%s.%s", newTempVar, Utils.typeOllir(info.a.getType()));
            }
            case "PARAM" -> {
                return String.format("$%d.%s.%s", symbolTable.getSymbolIndex(s, varName), varName, Utils.typeOllir(info.a.getType()));
            }
            case "LOCAL", "IMPORT" -> {
                return String.format("%s.%s", varName, Utils.typeOllir(info.a.getType()));
            }
        }
        return null;
    }

    private String visitBoolean(JmmNode jmmNode, String s) {
        if (jmmNode.get("bool").equals("true")) {
            return "1.bool";
        }

        return "0.bool";

    }

    private String visitInteger(JmmNode jmmNode, String s) {
        return jmmNode.get("value") + ".i32";
    }


    private String visitBinaryOp(JmmNode jmmNode, String s) {
        String left = visit(jmmNode.getJmmChild(0), s);
        String right = visit(jmmNode.getJmmChild(1), s);
        if(jmmNode.getJmmParent().getKind().equals("Assignment")){
            String typeOp = Utils.typeOllir(jmmNode.getJmmChild(0));
            return String.format("%s %s.%s %s", left, jmmNode.get("op"), typeOp, right);
        }
        String newTempVar = "t" + this.numTemVars++;
        String type = Utils.typeOllir(jmmNode);
        String typeOp = Utils.typeOllir(jmmNode.getJmmChild(0));
        code.append(String.format("%s.%s :=.%s %s %s.%s %s;\n", newTempVar, type, type, left, jmmNode.get("op"), typeOp, right));
        return newTempVar + "." + type;
    }


    private String visitGetLenght(JmmNode jmmNode, String s) {
        String array = visit(jmmNode.getJmmChild(0), s);
        String newTempVar = "t" + this.numTemVars++;
        code.append(String.format("%s.i32 :=.i32 arraylength(%s).i32;\n", newTempVar, array));
        return newTempVar + ".i32";
    }

    private String visitCallFnc(JmmNode jmmNode, String s) {
        String type = Utils.typeOllir(jmmNode);
        String obj = visit(jmmNode.getJmmChild(0), s);
        String _return = "";
        StringBuilder parameters= new StringBuilder();
        for (int i = 1; i < jmmNode.getNumChildren(); i++) {
            parameters.append(", ").append(visit(jmmNode.getJmmChild(i), s));
        }

        if (jmmNode.getJmmChild(0).getKind().equals("Identifier") && obj.split("[.]")[0].equals(jmmNode.get("type"))) {
            if (!jmmNode.getJmmParent().getKind().equals("ExprStmt")) {
                String newTempVar = "t" + this.numTemVars++;
                code.append(String.format("%s.V :=.V ", newTempVar));
                _return = String.format("%s.V", newTempVar);
            }
            if (!obj.contains("this")) obj = obj.split("[.]")[0];
            code.append(String.format("invokestatic(%s, \"%s\"", obj, jmmNode.get("value")));
            code.append(parameters);
            code.append(").V;\n");
        } else {
            if (!jmmNode.getJmmParent().getKind().equals("ExprStmt")) {
                String newTempVar = "t" + this.numTemVars++;
                code.append((String.format("%s.%s :=.%s ", newTempVar, type, type)));
                _return = String.format("%s.%s", newTempVar, type);
            }
            code.append(String.format("invokevirtual(%s, \"%s\"", obj, jmmNode.get("value")));
            code.append(parameters);
            code.append(String.format(").%s;\n", type));
        }



        return _return;
    }

    private String visitArrayExp(JmmNode jmmNode, String s) {
        String name = visit(jmmNode.getJmmChild(0), s);
        String index = visit(jmmNode.getJmmChild(1), s);
        if(jmmNode.getJmmChild(0).getKind().equals("Identifier")) {
            name=jmmNode.getJmmChild(0).get("value");
            Pair<Symbol, String> info = symbolTable.getSymbol(s, name);
            switch (info.b) {
                case "FIELD" -> {
                    String newTempVar = "t" + this.numTemVars++;
                    code.append((String.format("%s.array.i32 :=.array.i32 getfield(this, %s).array.i32;\n", newTempVar, name)));
                    return String.format("%s.i32", newTempVar);
                }
                case "PARAM" -> {
                    String newTempVar = "t" + this.numTemVars++ + ".i32";
                    code.append(String.format("%s :=.i32 $%d.%s[%s].i32;\n", newTempVar, symbolTable.getSymbolIndex(s, name), name, index));
                    return newTempVar;
                }
                case "IMPORT" -> throw new RuntimeException("Class cannot be accessed as an array");
                default -> {
                    String newTempVar = "t" + this.numTemVars++ + ".i32";
                    code.append(String.format("%s :=.i32 %s[%s].i32;\n", newTempVar, name, index));
                    return newTempVar;
                }
            }
        }
        else{
            name = name.split("[.]")[0];
            String newTempVar = "t" + this.numTemVars++ + ".i32";
            code.append(String.format("%s :=.i32 %s[%s].i32;\n", newTempVar, name, index));
            return newTempVar;
        }
    }

    private String visitParenthesisExpr(JmmNode jmmNode, String s) {
        return visit(jmmNode.getJmmChild(0), s);
    }


    private String visitNegateExpr(JmmNode jmmNode, String s) {
        String expr = visit(jmmNode.getJmmChild(0), s);
        String newTempVar = "t" + this.numTemVars++;
        code.append(String.format("%s.bool :=.bool !.bool %s;\n", newTempVar, expr));
        return newTempVar + ".bool";
    }

    private String visitInitializeClass(JmmNode jmmNode, String s) {
        String newTempVar = "t" + this.numTemVars++;
        String type = jmmNode.get("value");
        code.append(String.format("%s.%s :=.%s new(%s).%s;\n", newTempVar, type, type, type, type));
        code.append(String.format("invokespecial(%s.%s, \"<init>\").V;\n", newTempVar, type));
        return String.format("%s.%s", newTempVar, type);
    }

    private String visitCreateArray(JmmNode jmmNode, String s) {
        String size = visit(jmmNode.getJmmChild(0), s);
        String newTempVar = "t" + this.numTemVars++;
        code.append(String.format("%s.array.i32 :=.array.i32 new(array, %s).array.i32;\n", newTempVar, size));
        return newTempVar + ".array.i32";
    }

    private String ignore(JmmNode jmmNode, String s) {
        return "";
    }
}
