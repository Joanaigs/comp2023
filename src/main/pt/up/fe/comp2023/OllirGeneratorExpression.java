package pt.up.fe.comp2023;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;


public class OllirGeneratorExpression extends AJmmVisitor<String, String> {
    String code = "";
    SymbolTable symbolTable;
    int numTemVars=0;
    public String getCode() {
        return code;
    }

    OllirGeneratorExpression(SymbolTable symbolTable) {
        this.symbolTable=symbolTable;
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
        addVisit("GetLenght", this::visitGetLenght);
        addVisit("BinaryOp", this::visitBinaryOp);
        addVisit("Integer", this::visitInteger);
        addVisit("Boolean", this::visitBoolean);
        addVisit("Identifier", this::visitIdentifier);
        addVisit("Return", this::visitReturn);
        addVisit("This", this::visitThis);
    }

    private String visitReturn(JmmNode jmmNode, String s) {
        String ret = visit(jmmNode.getJmmChild(0), s);
        code+=String.format("ret.%s %s;\n",
                typeOllir(symbolTable.getReturnType(s)),
                ret
        );
        return "";
    }

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
            case "boolean":
                ollirType += "bool";
                break;
            default:
                ollirType += type.getName();
                break;
        };
        return ollirType;
    }
    private String visitThis(JmmNode jmmNode, String s) {

        return "This";
    }

    private String visitIdentifier(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("value");
        Pair<Symbol, String> info= symbolTable.getSymbol(s, varName);
        if(info.b.equals("FIELD")){
            String newTempVar="t"+this.numTemVars++;
            String type = typeOllir(info.a.getType());
            code+=String.format("%s.%s :=.%s getfield(this, %s.%s).%s;\n", newTempVar, type, type, varName, type, type);
            return  String.format("%s.%s", newTempVar, typeOllir(info.a.getType()));
        }
        else if(info.b.equals("PARAM")){
            return  String.format("$%d%s.%s",symbolTable.getSymbolIndex(s, varName), varName, typeOllir(info.a.getType()));
        }
        else if(info.b.equals("LOCAL")){
            return  String.format("%s.%s", varName, typeOllir(info.a.getType()));
        }
        return  null;
    }

    private String visitBoolean(JmmNode jmmNode, String s) {
        if(jmmNode.get("bool").equals("true")){
            return "1.bool";
        }

        return "0.bool";

    }

    private String visitInteger(JmmNode jmmNode, String s) {
        return jmmNode.get("value")+".i32";
    }

    private String visitBinaryOp(JmmNode jmmNode, String s) {
        String left = visit(jmmNode.getJmmChild(0), s);
        String right = visit(jmmNode.getJmmChild(1), s);
        String newTempVar="t"+this.numTemVars++;
        code+=String.format("%s.i32 :=i32 %s %s.i32 %s;\n",
                newTempVar,
                left,
                jmmNode.get("op"),
                right
        );
        return newTempVar+".i32";
    }

    private String visitGetLenght(JmmNode jmmNode, String s) {
        String array = visit(jmmNode.getJmmChild(0), s);
        String newTempVar="t"+this.numTemVars++ ;
        code+=String.format("%s.i32 :=.i32 arraylength(%s).i32;\n", newTempVar, array);
        return newTempVar+".i32";
    }

    private String visitCallFnc(JmmNode jmmNode, String s) {

        return "";
    }

    private String visitArrayExp(JmmNode jmmNode, String s) {
        return "";
    }

    private String visitParenthesisExpr(JmmNode jmmNode, String s) {
        return "";
    }

    private String visitNegateExpr(JmmNode jmmNode, String s) {
        String expr = visit(jmmNode.getJmmChild(0), s);
        String newTempVar="t"+this.numTemVars++ ;
        code+=String.format("%s.bool :=.bool !.bool %s;\n", newTempVar, expr);
        return newTempVar+".bool";

    }

    private String visitInitializeClass(JmmNode jmmNode, String s) {
        return "";
    }

    private String visitCreateArray(JmmNode jmmNode, String s) {
        return "";
    }

    private String ignore (JmmNode jmmNode, String s) {
        return "";
    }
}
