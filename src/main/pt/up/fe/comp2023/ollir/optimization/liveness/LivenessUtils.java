package pt.up.fe.comp2023.ollir.optimization.liveness;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.specs.comp.ollir.*;

public class LivenessUtils {
    private final Method method;
    private final Set<String> excludeVars;

    LivenessUtils(Method method){
        this.method = method;
        this.excludeVars = new HashSet<>();
        setExcludeVars();
    }

    Optional<String> getName(Element element) {
        if(!element.isLiteral()) {
            Optional<String> name = Optional.ofNullable(((Operand) element).getName());
            if(name.isEmpty())
                return name;
            if(!isExcludeVar(name.get()) && (!element.getType().getTypeOfElement().equals(ElementType.THIS) ||
                    (element.getType().getTypeOfElement() == ElementType.OBJECTREF && ((Operand) element).getName().equals("this")))) {
                return name;
            }
        }
        return Optional.empty();
    }

    void setExcludeVars(){
        for(Element element: this.method.getParams()){
            excludeVars.add(((Operand) element).getName());
        }
    }

    boolean isExcludeVar(String var){
        return this.excludeVars.contains(var);
    }

}
