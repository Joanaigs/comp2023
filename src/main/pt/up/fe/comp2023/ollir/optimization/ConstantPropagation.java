package pt.up.fe.comp2023.ollir.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.HashMap;
import java.util.Map;

import static pt.up.fe.comp2023.ollir.Utils.addNewNodeInfo;

public class ConstantPropagation extends AJmmVisitor<String,Boolean> {
    private final Map<String, String> constants=new HashMap<>();
    private boolean insideLoop=false;


    @Override
    protected void buildVisitor() {
        addVisit("InstanceMethodDeclaration", this::visitMethodDeclaration);
        addVisit("MainMethodDeclaration", this::visitMethodDeclaration);
        addVisit("IfStmt", this::visitIfStmt);
        addVisit("WhileStmt", this::visitWhileStmt);
        addVisit("Assignment", this::visitAssignment);
        addVisit("ArrayAssignStmt", this::visitArrayAssignStmt);
        addVisit("Identifier", this::visitIdentifier);
        setDefaultVisit(this::ignore);
    }



    private boolean visitIdentifier(JmmNode jmmNode, String s) {
        if (insideLoop) {
            return false;
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
            addNewNodeInfo(jmmNode, newNode);
            return true;
        }
        return false;
    }


    private boolean visitAssignment(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("var");
        if(insideLoop){
            constants.remove(varName);
            return false;
        }
        boolean hasChanges = visit(jmmNode.getJmmChild(0));
        if(jmmNode.getJmmChild(0).getKind().equals("Integer")){
            constants.put(varName, jmmNode.getJmmChild(0).get("value"));
            return hasChanges;
        }
        else if(jmmNode.getJmmChild(0).getKind().equals("Boolean")){
            constants.put(varName, jmmNode.getJmmChild(0).get("bool"));
            return hasChanges;
        }
        return hasChanges;
    }

    private boolean visitArrayAssignStmt(JmmNode jmmNode, String s) {
        String varName = jmmNode.get("var");
        if(insideLoop){
            constants.remove(varName);
            return false;
        }
        boolean hasChanges = visit(jmmNode.getJmmChild(0));
        hasChanges = hasChanges || visit(jmmNode.getJmmChild(1));
        return hasChanges;
    }

    private boolean visitWhileStmt(JmmNode jmmNode, String s) {
        JmmNode cond = jmmNode.getJmmChild(0);
        JmmNode stat = jmmNode.getJmmChild(1);
        if(!insideLoop) {
            this.insideLoop = true;
            visit(stat, s);
            this.insideLoop = false;
        }
        else{
            visit(stat, s);
        }
        return visit(cond, s);
    }

    private boolean visitIfStmt(JmmNode jmmNode, String s) {
        JmmNode cond = jmmNode.getJmmChild(0);
        JmmNode ifNode = jmmNode.getJmmChild(1);
        JmmNode elseNode = jmmNode.getJmmChild(2);
        boolean hasChanges = visit(cond, s);
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
        return hasChanges;
    }

    private boolean visitMethodDeclaration(JmmNode jmmNode, String s) {
        constants.clear();
        return ignore(jmmNode, s);
    }
    private boolean ignore (JmmNode jmmNode, String s) {
        boolean hasChanges=false;
        for (JmmNode child: jmmNode.getChildren()){
            hasChanges=hasChanges||visit(child , s);
        }
        return hasChanges;
    }

}
