package pt.up.fe.comp2023.ollir.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.Map;

public class DeadCodeElimination extends AJmmVisitor<String,Boolean> {
    private final Map<String, String> constants=new HashMap<>();
    private boolean insideLoop=false;


    @Override
    protected void buildVisitor() {
        addVisit("IfStmt", this::visitIfStmt);
        addVisit("WhileStmt", this::visitWhileStmt);
        setDefaultVisit(this::ignore);
    }

    private Boolean visitWhileStmt(JmmNode jmmNode, String s) {

        // while (false) {}
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
        JmmNode condition = jmmNode.getJmmChild(0);
        JmmNode ifStatement = jmmNode.getJmmChild(1);
        JmmNode elseStatement = jmmNode.getJmmChild(2);


        if (condition.getKind().equals("Boolean")) {
            String value = condition.get("bool");
            if (value.equals("true")) {
                hasChanges= hasChanges|| deleateIf(jmmNode, ifStatement);
                hasChanges= hasChanges||visit(ifStatement);
            } else if (value.equals("false")) {
                hasChanges= hasChanges|| deleateIf(jmmNode, elseStatement);
                hasChanges= hasChanges||visit(elseStatement);
            }
        }
        else{
            hasChanges= hasChanges||visit(ifStatement);
            hasChanges= hasChanges||visit(elseStatement);
        }
        return hasChanges;
    }

    private boolean deleateIf(JmmNode jmmNode, JmmNode tochange) {
        if (jmmNode != null) {
            JmmNode newJmmNode = jmmNode.getJmmParent();
            int start_index = jmmNode.getIndexOfSelf();

            int i = 1;
            for (JmmNode child : tochange.getChildren()) {
                newJmmNode.add(child, start_index + i);
                i ++;
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
