package pt.up.fe.comp2023.ollir;

import org.antlr.v4.runtime.misc.Pair;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.semantic_analysis.SymbolTable;

import java.util.List;
import java.util.StringJoiner;

public class OllirGenerator extends AJmmVisitor<String, String> {
    SymbolTable symbolTable;
    String ollirCode;
    Integer ifs;

    Integer whiles;

    OllirGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.ollirCode = "";
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
        addVisit("ExprStmt", this::visitExprStmt);
        addVisit("Assignment", this::visitAssignment);
        setDefaultVisit(this::ignore);
    }

    private String visitAssignment(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("var");
        Pair<Symbol, String> info = symbolTable.getSymbol(s, varName);
        OllirGeneratorExpression ollirGeneratorExpression = new OllirGeneratorExpression(symbolTable);
        String expr = ollirGeneratorExpression.visit(jmmNode.getJmmChild(0), s);
        String code = ollirGeneratorExpression.getCode();
        ollirCode += code;

        switch (info.b) {
            case "FIELD" -> {
                ollirCode += "putfield(this, " + varName + "." + Utils.typeOllir(info.a.getType()) + ", " + expr + ").V" + ";\n";
                return s;
            }
            case "LOCAL" ->
                    ollirCode += varName + "." + Utils.typeOllir(info.a.getType()) + " :=." + Utils.typeOllir(info.a.getType()) + " " + expr + ";\n";
            case "PARAM" ->
                    ollirCode += '$' + Integer.toString(symbolTable.getSymbolIndex(s, varName)) + "." + varName + "." + Utils.typeOllir(info.a.getType()) + " :=." + Utils.typeOllir(info.a.getType()) + " " + expr + ";\n";
        }
        return s;
    }

    private String visitExprStmt(JmmNode jmmNode, String s) {
        OllirGeneratorExpression ollirGeneratorExpression = new OllirGeneratorExpression(symbolTable);
        ollirGeneratorExpression.visit(jmmNode.getJmmChild(0), s);
        ollirCode += ollirGeneratorExpression.getCode();
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
            ollirCode += "import " + imprt + ";\n";
        }
        for (JmmNode child : jmmNode.getChildren()) {
            visit(child, "");
        }
        return "";
    }

    private String visitClassDeclaration(JmmNode jmmNode, String s) {
        ollirCode += symbolTable.getClassName() + (symbolTable.getSuper() != null ? (" extends " + symbolTable.getSuper()) : "") + "{\n";

        for (var field : symbolTable.getFields()) {
            ollirCode += ".field " + field.getName() + '.' + Utils.typeOllir(field.getType()) + ";\n";
        }
        ollirCode += ".construct " + symbolTable.getClassName() + "().V {\n" + "invokespecial(this, \"<init>\").V;\n" + "}\n";
        for (JmmNode child : jmmNode.getChildren()) {
            visit(child, "");
        }
        ollirCode += "}";
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
            ollirCode += ".method " + access + " " + methodName + "(";
        } else ollirCode += ".method " + methodName + "(";
        List<Symbol> parameters = symbolTable.getParameters(methodName);
        StringJoiner sj = new StringJoiner(", ");

        for (Symbol parameter : parameters) {
            sj.add(parameter.getName() + '.' + Utils.typeOllir(parameter.getType()));
        }

        String parameterList = sj.toString();
        ollirCode += parameterList + ")." + Utils.typeOllir(symbolTable.getReturnType(methodName)) + " {\n";
        for (int i = 0; i < jmmNode.getNumChildren() - 1; i++) {
            visit(jmmNode.getJmmChild(i), methodName);
        }
        OllirGeneratorExpression ollirGeneratorExpression = new OllirGeneratorExpression(symbolTable);
        String ret = ollirGeneratorExpression.visit(jmmNode.getJmmChild(jmmNode.getNumChildren() - 1), methodName);
        String code = ollirGeneratorExpression.getCode();
        ollirCode += code;
        ollirCode += String.format("ret.%s %s;\n}\n", Utils.typeOllir(symbolTable.getReturnType(methodName)), ret);
        return s;
    }


    private String visitMainMethodDeclaration(JmmNode jmmNode, String s) {
        if (jmmNode.hasAttribute("access")) {
            String access = jmmNode.get("access");
            ollirCode += ".method " + access + " static main(" + jmmNode.get("var") + ".array.String).V{\n";
        } else ollirCode += ".method static main(" + jmmNode.get("var") + ".array.String).V{\n";
        for (JmmNode child : jmmNode.getChildren()) {
            visit(child, "main");
        }
        ollirCode += "ret.V;\n}\n";
        return s;
    }


    private String ignore(JmmNode jmmNode, String s) {
        return "";
    }


    public String getOllirCode() {
        return ollirCode;
    }


}

