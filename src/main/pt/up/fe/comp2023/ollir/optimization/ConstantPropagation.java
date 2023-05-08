package pt.up.fe.comp2023.ollir.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2023.semantic_analysis.SymbolTable;

import java.util.HashMap;
import java.util.Map;

public class ConstantPropagation extends AJmmVisitor<String, String> {
    private Map<String, String> constants=new HashMap<>();
    private boolean insideLoop=false;


    @Override
    protected void buildVisitor() {

        addVisit("InstanceMethodDeclaration", this::visitInstanceMethodDeclaration);
        addVisit("MainMethodDeclaration", this::visitMethodDeclaration);
        addVisit("IfStmt", this::visitIfStmt);
        addVisit("WhileStmt", this::visitWhileStmt);
        addVisit("Assignment", this::visitAssignment);
        addVisit("ArrayAssignStmt", this::visitArrayAssignStmt);
        addVisit("Identifier", this::visitIdentifier);
        setDefaultVisit(this::ignore);
    }



    private String visitIdentifier(JmmNode jmmNode, String s) {
        if (insideLoop) {
            return null;
        }
        String varName = jmmNode.get("value");
        if(constants.containsKey(varName)){
            JmmNode newNode;
            String expr= constants.get(varName);
            switch (expr) {
                case "true", "false" -> {
                    newNode = new JmmNodeImpl("Boolean");
                    newNode.put("bool", expr);
                }
                default -> {
                    newNode = new JmmNodeImpl("Integer");
                    newNode.put("value", expr);
                }
            }
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
        return null;
    }


    private String visitAssignment(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("var");
        if(insideLoop){
            constants.remove(varName);
            return null;
        }
        if(jmmNode.getJmmChild(0).getKind().equals("Integer")){
            constants.put(varName, jmmNode.getJmmChild(0).get("value"));
            return null;
        }
        else if(jmmNode.getJmmChild(0).getKind().equals("Boolean")){
            constants.put(varName, jmmNode.getJmmChild(0).get("bool"));
            return null;
        }
        visit(jmmNode.getJmmChild(0));
        return s;
    }

    private String visitArrayAssignStmt(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("var");
        if(insideLoop){
            constants.remove(varName);
            return null;
        }
        if(jmmNode.getJmmChild(1).getKind().equals("Integer")){
            constants.put(varName, jmmNode.getJmmChild(1).get("value"));
            return null;
        }
        else if(jmmNode.getJmmChild(1).getKind().equals("Boolean")){
            constants.put(varName, jmmNode.getJmmChild(1).get("bool"));
            return null;
        }
        visit(jmmNode.getJmmChild(0));
        visit(jmmNode.getJmmChild(1));
        return s;
    }

    private String visitWhileStmt(JmmNode jmmNode, String s) {
        JmmNode cond = jmmNode.getJmmChild(0);
        JmmNode statm = jmmNode.getJmmChild(1);
        visit(cond, s);
        if(!insideLoop) {
            this.insideLoop = true;
            visit(statm, s);
            this.insideLoop = false;
        }
        else{
            visit(statm, s);
        }
        return s;
    }

    private String visitIfStmt(JmmNode jmmNode, String s) {
        JmmNode cond = jmmNode.getJmmChild(0);
        JmmNode ifNode = jmmNode.getJmmChild(1);
        JmmNode elseNode = jmmNode.getJmmChild(2);
        visit(cond, s);
        if(!insideLoop) {
            this.insideLoop = true;
            visit(ifNode, s);
            visit(elseNode, s);
            this.insideLoop = false;
        }
        else{
            visit(ifNode, s);
            visit(elseNode, s);
        }
        return s;
    }

    private String visitMethodDeclaration(JmmNode jmmNode, String s) {
        constants.clear();
        ignore(jmmNode, s);
        return s;
    }
    private String visitInstanceMethodDeclaration(JmmNode jmmNode, String s) {
        constants.clear();
        ignore(jmmNode, s);
        return s;
    }

    private String ignore (JmmNode jmmNode, String s) {
        for (JmmNode child: jmmNode.getChildren()){
            visit(child , s);
        }
        return null;
    }
}
