package pt.up.fe.comp2023;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.lang.constant.Constable;
import java.util.Arrays;
import java.util.List;

public class OllirGeneratorExpression extends AJmmVisitor<String, List<String>> {
    StringBuilder code = new StringBuilder();
    SymbolTable symbolTable;
    int numTemVars=0;
    public String getCode() {
        return code.toString();
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
        addVisit("This", this::visitThis);
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
    private List<String> visitThis(JmmNode jmmNode, String s) {

        return Arrays.asList("This");
    }

    private List<String> visitIdentifier(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("value");
        Pair<SymbolExtended, String> info= symbolTable.getSymbol(s, varName);
        if(info.b.equals("FIELD")){
            String newTempVar="t"+this.numTemVars++;
            String type = typeOllir(info.a.getType());
            code.append(String.format("%s.%s :=.%s getfield(this, %s.%s).%s;\n", newTempVar, type, type, varName, type, type));
            return  Arrays.asList(String.format("%s.%s", newTempVar, typeOllir(info.a.getType())));
        }
        else if(info.b.equals("PARAM")){
            return  Arrays.asList(String.format("$%d%s.%s",symbolTable.getSymbolIndex(s, varName), varName, typeOllir(info.a.getType())), info.a.getValue());
        }
        else if(info.b.equals("LOCAL")){
            if(!info.a.value.equals(""))
                return  Arrays.asList(String.format("%s.%s", varName, typeOllir(info.a.getType())),"Constant", info.a.getValue());
            else
                return  Arrays.asList(String.format("%s.%s", varName, typeOllir(info.a.getType())));
        }
        return  null;
    }

    private List<String> visitBoolean(JmmNode jmmNode, String s) {
        if(jmmNode.get("bool").equals("true")){
            return Arrays.asList("1.bool","Constant", "1");
        }

        return Arrays.asList("0.bool", "Constant", "1");

    }

    private List<String> visitInteger(JmmNode jmmNode, String s) {
        return Arrays.asList(jmmNode.get("value")+".i32", "Constant", jmmNode.get("value"));
    }

    private List<String> visitBinaryOp(JmmNode jmmNode, String s) {
        List<String> left = visit(jmmNode.getJmmChild(0));
        List<String> right = visit(jmmNode.getJmmChild(1));
        Integer operation=null;
        String newTempVar="t"+this.numTemVars++;
        String constant = "NotConstant";
        if(left.get(1).equals("Constant") && right.get(1).equals("Constant")) {
            constant="Constant";
            switch (jmmNode.get("op")) {
                case "*":
                    operation = (Integer.parseInt(left.get(2)) * Integer.parseInt(right.get(2)));
                    break;
                case "/":
                    operation = (Integer.parseInt(left.get(2)) / Integer.parseInt(right.get(2)));
                    break;
                case "%":
                    operation = (Integer.parseInt(left.get(2)) % Integer.parseInt(right.get(2)));
                    break;
                case "+":
                    operation = (Integer.parseInt(left.get(2)) + Integer.parseInt(right.get(2)));
                    break;
                case "-":
                    operation = (Integer.parseInt(left.get(2)) - Integer.parseInt(right.get(2)));
                    break;
                case "<<":
                    operation = (Integer.parseInt(left.get(2)) << Integer.parseInt(right.get(2)));
                    break;
                case ">>":
                    operation = (Integer.parseInt(left.get(2)) >> Integer.parseInt(right.get(2)));
                    break;
                case "<":
                    operation = (Integer.parseInt(left.get(2)) < Integer.parseInt(right.get(2))) ? 1 : 0;
                    break;
                case ">":
                    operation = (Integer.parseInt(left.get(2)) > Integer.parseInt(right.get(2))) ? 1 : 0;
                    break;
                case "<=":
                    operation = (Integer.parseInt(left.get(2)) <= Integer.parseInt(right.get(2))) ? 1 : 0;
                    break;
                case ">=":
                    operation = (Integer.parseInt(left.get(2)) >= Integer.parseInt(right.get(2))) ? 1 : 0;
                    break;
                case "==":
                    operation = (Integer.parseInt(left.get(2)) == Integer.parseInt(right.get(2))) ? 1 : 0;
                    break;
                case "!=":
                    operation = (Integer.parseInt(left.get(2)) != Integer.parseInt(right.get(2))) ? 1 : 0;
                    break;
                case "&":
                    operation = ((Integer.parseInt(left.get(2))>=1) & (Integer.parseInt(right.get(2)))>=1) ? 1 : 0;
                    break;
                case "^":
                    operation = ((Integer.parseInt(left.get(2))>=1) ^ (Integer.parseInt(right.get(2)))>=1) ? 1 : 0;
                    break;
                case "|":
                    operation = ((Integer.parseInt(left.get(2))>=1) | (Integer.parseInt(right.get(2)))>=1) ? 1 : 0;
                    break;
                case "&&":
                    operation = ((Integer.parseInt(left.get(2))>=1) && (Integer.parseInt(right.get(2)))>=1) ? 1 : 0;
                    break;
                case "||":
                    operation = ((Integer.parseInt(left.get(2))>=1) || (Integer.parseInt(right.get(2)))>=1) ? 1 : 0;
                    break;

                default:
                    throw new RuntimeException("To make the compiler calm");
            }
        }
        code.append(String.format("%s.i32 :=i32 %s %s.i32 %s;\n",
                newTempVar,
                left.get(0),
                jmmNode.get("op"),
                right.get(0)
        ));
        if (operation != null)
            return Arrays.asList(newTempVar+".i32", constant, operation.toString());
        return Arrays.asList(newTempVar+".i32");
    }

    private List<String> visitGetLenght(JmmNode jmmNode, String s) {
        return Arrays.asList("");
    }

    private List<String> visitCallFnc(JmmNode jmmNode, String s) {
        return Arrays.asList("");
    }

    private List<String> visitArrayExp(JmmNode jmmNode, String s) {
        return Arrays.asList("");
    }

    private List<String> visitParenthesisExpr(JmmNode jmmNode, String s) {
        return Arrays.asList("");
    }

    private List<String> visitNegateExpr(JmmNode jmmNode, String s) {
        return Arrays.asList("");
    }

    private List<String> visitInitializeClass(JmmNode jmmNode, String s) {
        return Arrays.asList("");
    }

    private List<String> visitCreateArray(JmmNode jmmNode, String s) {
        return Arrays.asList("");
    }

    private List<String> ignore (JmmNode jmmNode, String s) {
        return Arrays.asList("");
    }
}
