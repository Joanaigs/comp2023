package pt.up.fe.comp2023.ollir.optimization;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

public class ConstantFolding extends AJmmVisitor<String, Boolean> {

    @Override
    protected void buildVisitor()

    {

        addVisit("BinaryOp", this::visitBinaryOp);
        addVisit("NegateExpr", this::visitNegateExpr);
        setDefaultVisit(this::ignore);
    }

    private Boolean visitNegateExpr(JmmNode jmmNode, String s) {
        boolean hasChanges=visit(jmmNode.getJmmChild(0));

        if (jmmNode.getJmmChild(0).getKind().equals("Boolean")) {
            String bool = jmmNode.getJmmChild(0).get("bool");
            if (bool.equals("true")) {
                jmmNode.put("bool", "false");
            } else {
                jmmNode.put("bool", "true");
            }
            return true;
        }
        return hasChanges;
    }

    private boolean visitBinaryOp(JmmNode jmmNode, String s) {
        boolean hasChanges=visit(jmmNode.getJmmChild(0));
        hasChanges= hasChanges||visit(jmmNode.getJmmChild(1));
        JmmNode left = jmmNode.getJmmChild(0);
        JmmNode right = jmmNode.getJmmChild(1);
        String result="";

        if(right.getKind().equals("Boolean") && left.getKind().equals("Boolean")){
            Boolean leftValue = left.get("bool").equals("true");
            Boolean rightValue = right.get("bool").equals("true");
            switch (jmmNode.get("op")) {
                case "&&" -> result = leftValue && rightValue ? "true" : "false";
                case "||" -> result = leftValue || rightValue ? "true" : "false";
                case "&" -> result = leftValue & rightValue ? "true" : "false";
                case "^" -> result = leftValue ^ rightValue ? "true" : "false";
                case "|" -> result = leftValue | rightValue ? "true" : "false";
            }
            JmmNode newNode= new JmmNodeImpl("Boolean");
            newNode.put("bool", result);
            newNode.put("colEnd", jmmNode.get("colEnd"));
            newNode.put("colStart", jmmNode.get("colStart"));
            newNode.put("lineStart", jmmNode.get("lineStart"));
            if(jmmNode.hasAttribute("scope"))
                newNode.put("scope", jmmNode.get("scope"));
            if(jmmNode.hasAttribute("type"))
                newNode.put("type", jmmNode.get("type"));
            newNode.put("lineEnd", jmmNode.get("lineEnd"));
            jmmNode.replace(newNode);
            return true;
        }else if(right.getKind().equals("Integer") && left.getKind().equals("Integer")) {
            Integer leftValue =  Integer.parseInt(left.get("value"));
            Integer rightValue =  Integer.parseInt(right.get("value"));
            result = switch (jmmNode.get("op")) {
                case "*" -> String.valueOf(leftValue * rightValue);
                case "/" -> String.valueOf(leftValue / rightValue);
                case "%" -> String.valueOf(leftValue % rightValue);
                case "+" -> String.valueOf(leftValue + rightValue);
                case "-" -> String.valueOf(leftValue - rightValue);
                case "<<" -> String.valueOf(leftValue << rightValue);
                case ">>" -> String.valueOf(leftValue >> rightValue);
                case "<" -> leftValue < rightValue ? "true" : "false";
                case ">" -> leftValue > rightValue ? "true" : "false";
                case "<=" -> leftValue <= rightValue ? "true" : "false";
                case ">=" -> leftValue >= rightValue ? "true" : "false";
                case "==" -> leftValue == rightValue ? "true" : "false";
                case "!=" -> leftValue != rightValue ? "true" : "false";
                default -> result;
            };
            JmmNode newNode;
            if(result.equals("true")||result.equals("false")) {
                newNode= new JmmNodeImpl("Boolean");
                newNode.put("bool", result);
            }
            else {
                newNode = new JmmNodeImpl("Integer");
                newNode.put("value", result);
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
            return true;
        }

        return hasChanges;
    }

    private boolean ignore (JmmNode jmmNode, String s) {
        boolean hasChanges=false;
        for (JmmNode child: jmmNode.getChildren()){
            hasChanges = hasChanges || visit(child , s);
        }
        return hasChanges;
    }

}
