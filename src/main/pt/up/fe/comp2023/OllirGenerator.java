package pt.up.fe.comp2023;

import org.antlr.v4.runtime.misc.Pair;
import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.StringJoiner;

public class OllirGenerator extends AJmmVisitor<String, String> {
    SymbolTable symbolTable;
    String ollirCode;
    int label=0;
    OllirGeneratorExpression ollirGeneratorExpression;
    OllirGenerator(SymbolTable symbolTable) {
        this.symbolTable=symbolTable;
        ollirGeneratorExpression= new OllirGeneratorExpression(symbolTable);
        this.ollirCode="";
    }
    @Override
    protected void buildVisitor() {
        addVisit ("Program", this::dealWithProgram );
        addVisit ("ClassDeclaration", this::classDeclaration);
        addVisit ("MethodDeclaration", this::methodDeclaration);
        addVisit ("InstanceMethodDeclaration", this::instanceMethodDeclaration);
        addVisit ("MainMethodDeclaration", this::mainMethodDeclaration);
        addVisit("CodeBlockStmt", this::CodeBlockStmt);
        addVisit("IfStmt", this::IfStmt);
        addVisit("WhileStmt", this::WhileStmt);
        addVisit("ExprStmt", this::ExprStmt);
        addVisit("Assignment", this::Assignment);
        addVisit("ArrayAssignStmt", this::Assignment);
        setDefaultVisit(this::ignore);
    }

    private String Assignment(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("var");
        Pair<Symbol, String> info= symbolTable.getSymbol(s, varName);
        String expr = ollirGeneratorExpression.visit(jmmNode.getJmmChild(0));
        String code = ollirGeneratorExpression.getCode();
        ollirCode+=code;

        if(info.b.equals("FIELD")){
            ollirCode +="putfield(this, "+varName+"."+typeOllir(info.a.getType())+", "+expr+").V"+";\n";
            return s;
        }
        else if(info.b.equals("LOCAL") || info.b.equals("PARAM"))
            ollirCode+=varName+"."+typeOllir(info.a.getType())+" :=."+typeOllir(info.a.getType())+" "+ expr+";\n";
        return s;
    }

    private String ExprStmt(JmmNode jmmNode, String s) {
        ollirGeneratorExpression.visit(jmmNode.getJmmChild(0));
        ollirCode+=ollirGeneratorExpression.getCode();
        return s;
    }

    private String WhileStmt(JmmNode jmmNode, String s) {
        ollirCode+= "Loop: \n";
        ollirGeneratorExpression.visit(jmmNode.getJmmChild(0));
        ollirCode+=ollirGeneratorExpression.getCode();
        ollirCode+="End:\n";
        return s;
    }

    private String IfStmt(JmmNode jmmNode, String s) {
        String condition = ollirGeneratorExpression.visit(jmmNode.getJmmChild(0));
        String code = ollirGeneratorExpression.getCode();
        ollirCode+=code;
        ollirCode+="if (!.bool"+condition+") goto else;\n";
        ollirCode+=visit(jmmNode.getJmmChild(1));
        ollirCode+="goto endif;\n";
        ollirCode+="else: \n";
        ollirCode+=visit(jmmNode.getJmmChild(2));
        ollirCode+="endif: \n";
        return s;
    }

    private String CodeBlockStmt(JmmNode jmmNode, String s) {
        for (JmmNode child: jmmNode.getChildren()){
            ollirCode+=visit(child , "");
        }
        return s;
    }

    private String dealWithProgram ( JmmNode jmmNode, String s) {
        //imports
        for (String imprt: symbolTable.getImports()) {
            ollirCode+="import "+ imprt+ ";\n";
        }
        for (JmmNode child: jmmNode.getChildren()){
            visit(child , "");
        }
        return "";
    }

    public static String getFields(List<Symbol> fields) {
        String result="";
        for (var field: fields) {
            result+= ".field private "+ field.getName()+'.'+ typeOllir(field.getType() )+";\n";
        }
        result+="\n";
        return result;
    }

    private String classDeclaration ( JmmNode jmmNode , String s) {
        ollirCode+= symbolTable.getClassName() + (symbolTable.getSuper() != null ? (" extends " + symbolTable.getSuper()) : "") + "{\n";

        ollirCode+=getFields(symbolTable.getFields());
        ollirCode+=".construct "+symbolTable.getClassName()+"().V {\n" +
                "invokespecial(this, \"<init>\").V;\n" +
                "}\n";
        for (JmmNode child: jmmNode.getChildren()){
            visit(child , "");
        }
        ollirCode+="}";
        return s;
    }

    private String methodDeclaration ( JmmNode jmmNode , String s) {

        for (JmmNode child: jmmNode.getChildren()){
            visit(child , "");
        }

        return s;
    }

    private String instanceMethodDeclaration ( JmmNode jmmNode , String s) {
        String methodName = jmmNode.get("methodName");
        ollirCode+=".method public "+methodName+"(";
        List<Symbol> parameters= symbolTable.getParameters(methodName);
        StringJoiner sj = new StringJoiner(", ");

        for (Symbol parameter: parameters){
            sj.add(parameter.getName()+'.'+ typeOllir(parameter.getType()));
        }

        String parameterList = sj.toString();
        ollirCode+= parameterList + ")." + typeOllir(symbolTable.getReturnType(methodName))+ " {\n";
        for (JmmNode child: jmmNode.getChildren()) {
            visit(child, methodName);
        }
        ollirCode+="}\n";
        return s;
    }


    private String mainMethodDeclaration ( JmmNode jmmNode , String s) {
        ollirCode+=".method public static main("+jmmNode.get("var")+".array.String).V{\n";

        for (JmmNode child: jmmNode.getChildren()) {
            visit(child, "main");
        }
        ollirCode+="ret.V;\n}\n";
        return s;
    }



    private String ignore (JmmNode jmmNode, String s) {
        return "";
    }




    public String getOllirCode() {
        return ollirCode;
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
}

