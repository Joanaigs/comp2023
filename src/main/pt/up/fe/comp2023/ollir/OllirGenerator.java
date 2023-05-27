package pt.up.fe.comp2023.ollir;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.semantic_analysis.SymbolTable;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class OllirGenerator extends AJmmVisitor<String, String> {
    SymbolTable symbolTable;
    StringBuilder ollirCode;
    Integer ifs;

    Integer whiles;

    OllirGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.ollirCode = new StringBuilder();
        ifs=0;
        whiles=0;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
        addVisit("InstanceMethodDeclaration", this::visitInstanceMethodDeclaration);
        addVisit("MainMethodDeclaration", this::visitMainMethodDeclaration);
        addVisit("CodeBlockStmt", this::visitCodeBlockStmt);
        addVisit("IfStmt", this::visitIfStmt);
        addVisit("WhileStmt", this::visitWhileStmt);
        addVisit("ExprStmt", this::visitExprStmt);
        addVisit("Assignment", this::visitAssignment);
        addVisit("ArrayAssignStmt", this::visitArrayAssignStmt);
        setDefaultVisit(this::ignore);
    }

    private String visitAssignment(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("var");
        Pair<Symbol, String> info = symbolTable.getSymbol(s, varName);
        OllirGeneratorExpression ollirGeneratorExpression = new OllirGeneratorExpression(symbolTable);
        String expr = ollirGeneratorExpression.visit(jmmNode.getJmmChild(0), s);
        String code = ollirGeneratorExpression.getCode();
        ollirCode.append(code);

        switch (info.b) {
            case "FIELD" -> {
                ollirCode.append("putfield(this, ").append(varName).append(".").append(Utils.typeOllir(info.a.getType())).append(", ").append(expr).append(").V").append(";\n");
                return s;
            }
            case "LOCAL" ->
                    ollirCode.append(varName).append(".").append(Utils.typeOllir(info.a.getType())).append(" :=.").append(Utils.typeOllir(info.a.getType())).append(" ").append(expr).append(";\n");
            case "PARAM" ->
                    ollirCode.append('$').append(symbolTable.getSymbolIndex(s, varName)).append(".").append(varName).append(".").append(Utils.typeOllir(info.a.getType())).append(" :=.").append(Utils.typeOllir(info.a.getType())).append(" ").append(expr).append(";\n");
        }
        return s;
    }

    private String visitArrayAssignStmt(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("var");
        Pair<Symbol, String> info = symbolTable.getSymbol(s, varName);
        OllirGeneratorExpression ollirGeneratorExpression = new OllirGeneratorExpression(symbolTable);
        String idx = ollirGeneratorExpression.visit(jmmNode.getJmmChild(0), s);
        String code = ollirGeneratorExpression.getCode();
        ollirCode.append(code);
        OllirGeneratorExpression ollirGeneratorExpression2 = new OllirGeneratorExpression(symbolTable);
        String expr = ollirGeneratorExpression2.visit(jmmNode.getJmmChild(1), s);
        code = ollirGeneratorExpression2.getCode();
        ollirCode.append(code);

        switch (info.b) {
            case "FIELD" -> {
                ollirCode.append(String.format("putfield(this, %s[%s].i32, %s).V;\n", varName, idx, expr));
                return s;
            }
            case "LOCAL" ->
                    ollirCode.append(String.format("%s[%s].i32 :=.i32 %s;\n", varName, idx, expr));
            case "PARAM" ->
                    ollirCode.append(String.format("$%d.%s[%s].i32 :=.i32 %s;\n", symbolTable.getSymbolIndex(s, varName), varName, idx, expr));
        }
        return s;
    }

    private String visitExprStmt(JmmNode jmmNode, String s) {
        OllirGeneratorExpression ollirGeneratorExpression = new OllirGeneratorExpression(symbolTable);
        ollirGeneratorExpression.visit(jmmNode.getJmmChild(0), s);
        ollirCode.append(ollirGeneratorExpression.getCode());
        return s;
    }

    private String visitWhileStmt(JmmNode jmmNode, String s) {
        OllirGeneratorExpression ollirGeneratorExpression = new OllirGeneratorExpression(symbolTable);
        String condition=ollirGeneratorExpression.visit(jmmNode.getJmmChild(0), s);
        ollirCode.append(ollirGeneratorExpression.getCode());
        ollirCode.append("if (").append(condition).append(") goto whilebody_").append(whiles).append(";\n");
        ollirCode.append("goto endwhile_").append(whiles).append(";\n");
        ollirCode.append("whilebody_").append(whiles).append(": \n");
        visit(jmmNode.getJmmChild(1), s);
        ollirCode.append("if (").append(condition).append(") goto whilebody_").append(whiles).append(";\n");
        ollirCode.append("endwhile_").append(whiles).append(":\n");
        whiles++;
        return s;
    }

    private String visitIfStmt(JmmNode jmmNode, String s) {
        OllirGeneratorExpression ollirGeneratorExpression = new OllirGeneratorExpression(symbolTable);
        String condition = ollirGeneratorExpression.visit(jmmNode.getJmmChild(0), s);
        String code = ollirGeneratorExpression.getCode();
        ollirCode.append(code);
        ollirCode.append("if (").append(condition).append(") goto ifbody_").append(ifs).append(";\n");
        visit(jmmNode.getJmmChild(2), s);
        ollirCode.append("goto endif_").append(ifs).append(";\n");
        ollirCode.append("ifbody_").append(ifs).append(": \n");
        visit(jmmNode.getJmmChild(1), s);
        ollirCode.append("endif_").append(ifs).append(": \n");
        ifs++;
        return s;
    }

    private String visitCodeBlockStmt(JmmNode jmmNode, String s) {
        for (JmmNode child : jmmNode.getChildren()) {
            visit(child, s);
        }
        return s;
    }

    private String dealWithProgram(JmmNode jmmNode, String s) {
        //imports
        for (String imprt : symbolTable.getImports()) {
            ollirCode.append("import ").append(imprt).append(";\n");
        }
        for (JmmNode child : jmmNode.getChildren()) {
            visit(child, "");
        }
        return "";
    }

    private String visitClassDeclaration(JmmNode jmmNode, String s) {
        ollirCode.append(symbolTable.getClassName()).append(symbolTable.getSuper() != null ? (" extends " + symbolTable.getSuper()) : "").append("{\n");

        for (var field : symbolTable.getFields()) {
            ollirCode.append(".field ").append(field.getName()).append('.').append(Utils.typeOllir(field.getType())).append(";\n");
        }
        ollirCode.append(".construct ").append(symbolTable.getClassName()).append("().V {\n").append("invokespecial(this, \"<init>\").V;\n").append("}\n");
        for (JmmNode child : jmmNode.getChildren()) {
            visit(child, "");
        }
        ollirCode.append("}");
        return s;
    }

    private String visitMethodDeclaration(JmmNode jmmNode, String s) {

        for (JmmNode child : jmmNode.getChildren()) {
            visit(child, "");
        }

        return s;
    }

    private String visitInstanceMethodDeclaration(JmmNode jmmNode, String s) {
        String methodName = jmmNode.get("methodName");
        if (jmmNode.hasAttribute("access")) {
            String access = jmmNode.get("access");
            ollirCode.append(".method ").append(access).append(" ").append(methodName).append("(");
        } else ollirCode.append(".method ").append(methodName).append("(");
        List<Symbol> parameters = symbolTable.getParameters(methodName);
        StringJoiner sj = new StringJoiner(", ");

        for (Symbol parameter : parameters) {
            sj.add(parameter.getName() + '.' + Utils.typeOllir(parameter.getType()));
        }

        String parameterList = sj.toString();
        ollirCode.append(parameterList).append(").").append(Utils.typeOllir(symbolTable.getReturnType(methodName))).append(" {\n");
        for (int i = 0; i < jmmNode.getNumChildren() - 1; i++) {
            visit(jmmNode.getJmmChild(i), methodName);
        }
        OllirGeneratorExpression ollirGeneratorExpression = new OllirGeneratorExpression(symbolTable);
        String ret = ollirGeneratorExpression.visit(jmmNode.getJmmChild(jmmNode.getNumChildren() - 1), methodName);
        String code = ollirGeneratorExpression.getCode();
        ollirCode.append(code);
        String[] parts = ret.split("\\."); // Splitting the string at the dot
        String type = String.join(".", Arrays.copyOfRange(parts, 1, parts.length));
        ollirCode.append(String.format("ret.%s %s;\n}\n", type, ret));
        return s;
    }


    private String visitMainMethodDeclaration(JmmNode jmmNode, String s) {
        if (jmmNode.hasAttribute("access")) {
            String access = jmmNode.get("access");
            ollirCode.append(".method ").append(access).append(" static main(").append(jmmNode.get("var")).append(".array.String).V{\n");
        } else ollirCode.append(".method static main(").append(jmmNode.get("var")).append(".array.String).V{\n");
        for (JmmNode child : jmmNode.getChildren()) {
            visit(child, "main");
        }
        ollirCode.append("ret.V;\n}\n");
        return s;
    }


    private String ignore(JmmNode jmmNode, String s) {
        return "";
    }


    public String getOllirCode() {
        return ollirCode.toString();
    }


}

