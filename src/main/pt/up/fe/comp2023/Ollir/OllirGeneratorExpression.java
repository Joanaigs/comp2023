package pt.up.fe.comp2023.Ollir;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.SymbolTable;


import java.util.StringJoiner;


public class OllirGeneratorExpression extends AJmmVisitor<String, String> {
    String code = "";
    SymbolTable symbolTable;
    int numTemVars=0;
    public String getCode() {
        return code;
    }

    OllirGeneratorExpression(SymbolTable symbolTable) {
        this.symbolTable=symbolTable;
        code = "";
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
                Uteis.typeOllir(symbolTable.getReturnType(s)),
                ret
        );
        return "";
    }
    private String visitThis(JmmNode jmmNode, String s) {

        return "this";
    }




    private String visitIdentifier(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("value");
        Pair<Symbol, String> info= symbolTable.getSymbol(s, varName);
        if(info.b.equals("FIELD")){
            String newTempVar="t"+this.numTemVars++;
            String type = Uteis.typeOllir(info.a.getType());
            code+=String.format("%s.%s :=.%s getfield(this, %s.%s).%s;\n", newTempVar, type, type, varName, type, type);
            return  String.format("%s.%s", newTempVar, Uteis.typeOllir(info.a.getType()));
        }
        else if(info.b.equals("PARAM")){
            return  String.format("%s.%s",varName, Uteis.typeOllir(info.a.getType()));
        }
        else if(info.b.equals("LOCAL")){
            return  String.format("%s.%s", varName, Uteis.typeOllir(info.a.getType()));
        }
        else if(info.b.equals("IMPORT")){
            return  String.format("%s.%s", varName, Uteis.typeOllir(info.a.getType()));
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
        String type=Uteis.typeOllir(jmmNode);
        code+=String.format("%s.%s :=.%s %s %s.%s %s;\n",
                newTempVar,
                type,
                type,
                left,
                jmmNode.get("op"),
                type,
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
        String type=Uteis.typeOllir(jmmNode);
        String obj= visit(jmmNode.getJmmChild(0), s);
        if(!jmmNode.getJmmParent().getKind().equals("ExprStmt")){
            obj= visit(jmmNode.getJmmChild(0), s);
            String newTempVar="t"+this.numTemVars++;
            code+=(String.format("%s.%s :=.%s ", newTempVar, type, type));
            code+=String.format("invokevirtual(%s, \"%s\"",obj, jmmNode.get("value"));
            for(int i=1; i<jmmNode.getNumChildren(); i++){
                code+=", "+ visit(jmmNode.getJmmChild(i), s);
            }
            code += String.format(").%s;\n", type);
            return String.format("%s.%s", newTempVar, type);
        }
        else{
            if(!obj.equals("this"))
                obj=obj.split("[.]")[0];
            code+=String.format("invokestatic(%s, \"%s\"",obj, jmmNode.get("value"));
            for(int i=1; i<jmmNode.getNumChildren(); i++){
                code+=", "+ visit(jmmNode.getJmmChild(i), s);
            }
            code += String.format(").%s;\n", type);
        }
        return "";
    }

    private String visitArrayExp(JmmNode jmmNode, String s) {
        String name =jmmNode.getJmmChild(0).get("value");
        String index = visit(jmmNode.getJmmChild(1), s);
        String arrayIdx = index;
        if (!Uteis.LiteralorVariable(index)) {
            arrayIdx  = "t"+this.numTemVars++ + ".i32";
            code+=String.format("%s :=.i32 %s;\n", arrayIdx, index);
        }
        Pair<Symbol, String> info= symbolTable.getSymbol(s, name);
        if (info.b.equals("FIELD")) {
            String newTempVar="t"+this.numTemVars++ ;
            code+=String.format("%s.array.i32 :=.array.i32 getfield(this, %s.array.i32).array.i32;\n", newTempVar, name);
            return String.format("%s[%s].i32", newTempVar, arrayIdx);
        }
        else if (info.b.equals("PARAM")) {
            return String.format("%s[%s].i32", name, arrayIdx);
        }
        else if (info.b.equals("IMPORT")) {
            throw new RuntimeException("Class cannot be accessed as an array");
        }
        else {
            return String.format("%s[%s].i32", name, arrayIdx);
        }
    }

    private String visitParenthesisExpr(JmmNode jmmNode, String s) {
        return visit(jmmNode.getJmmChild(0));
    }


    private String visitNegateExpr(JmmNode jmmNode, String s) {
        String expr = visit(jmmNode.getJmmChild(0), s);
        String newTempVar="t"+this.numTemVars++ ;
        code+=String.format("%s.bool :=.bool !.bool %s;\n", newTempVar, expr);
        return newTempVar+".bool";
    }
    private String visitInitializeClass(JmmNode jmmNode, String s) {
        String newTempVar="t"+this.numTemVars++;
        String type= jmmNode.get("value");
        code+=String.format("%s.%s :=.%s new(%s).%s;\n", newTempVar, type, type, type, type);
        code+=String.format("invokespecial(%s.%s, \"<init>\").V;\n", newTempVar, type);
        return String.format("%s.%s", newTempVar, type);
    }

    private String visitCreateArray(JmmNode jmmNode, String s) {
        String size = visit(jmmNode.getJmmChild(0), s);
        String newTempVar="t"+this.numTemVars++;
        code+=(String.format("%s.array.i32 :=.array.i32 new(array, %s).array.i32;\n", newTempVar, size));
        return newTempVar + ".array.i32";
    }

    private String ignore (JmmNode jmmNode, String s) {
        return "";
    }
}
