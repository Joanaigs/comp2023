package pt.up.fe.comp2023.ollir.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class DeadCodeElimination extends AJmmVisitor<String,Boolean> {

    @Override
    protected void buildVisitor() {
        addVisit("IfStmt", this::visitIfStmt);
        addVisit("WhileStmt", this::visitWhileStmt);
        setDefaultVisit(this::ignore);
    }

    private Boolean visitWhileStmt(JmmNode jmmNode, String s) {
        if (jmmNode.getJmmChild(0).getKind().equals("Boolean")) {
            if (jmmNode.getJmmChild(0).get("bool").equals("false")) {
                jmmNode.delete();
                return true;
            }
        }
        return visit(jmmNode.getJmmChild(1));
    }

    private Boolean visitIfStmt(JmmNode jmmNode, String s) {
        boolean hasChanges = false;
        JmmNode cond = jmmNode.getJmmChild(0);
        JmmNode ifNode = jmmNode.getJmmChild(1);
        JmmNode elseNode = jmmNode.getJmmChild(2);
        if (cond.getKind().equals("Boolean")) {
            String bool = cond.get("bool");
            if (bool.equals("true")) {
                hasChanges = deleateIf(jmmNode, ifNode);
                hasChanges = hasChanges||visit(ifNode);
            } else if (bool.equals("false")) {
                hasChanges = deleateIf(jmmNode, elseNode);
                hasChanges = hasChanges||visit(elseNode);
            }
        }
        else{
            hasChanges= visit(ifNode);
            hasChanges= hasChanges||visit(elseNode);
        }
        return hasChanges;
    }

    private boolean deleateIf(JmmNode jmmNode, JmmNode tochange) {
        if (jmmNode != null) {
            JmmNode newJmmNode = jmmNode.getJmmParent();
            int index = jmmNode.getIndexOfSelf();
            index++;
            for (JmmNode child : tochange.getChildren()) {
                newJmmNode.add(child, index);
                index++;
            }
            jmmNode.delete();
            return true;
        }
        return false;
    }

    private boolean ignore (JmmNode jmmNode, String s) {
        boolean hasChanges=false;
        for (JmmNode child: jmmNode.getChildren()){
            hasChanges=hasChanges||visit(child , s);
        }
        return hasChanges;
    }
}
