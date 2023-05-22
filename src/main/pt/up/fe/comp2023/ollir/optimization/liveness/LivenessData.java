package pt.up.fe.comp2023.ollir.optimization.liveness;

import java.util.HashSet;
import java.util.Set;

public class LivenessData {
    private final Set<String> def;
    private final Set<String> use;
    private final Set<String> in;
    private final Set<String> out;

    public LivenessData() {
        this.def = new HashSet<>();
        this.use = new HashSet<>();
        this.in = new HashSet<>();
        this.out = new HashSet<>();
    }

    public Set<String> getDef() {
        return this.def;
    }

    public Set<String> getUse() {
        return this.use;
    }

    public Set<String> getIn() {
        return this.in;
    }

    public Set<String> getOut() {
        return this.out;
    }

    public void addDef(String def) {
        this.def.add(def);
    }

    public void addUse(Set<String> use) {
        this.use.addAll(use);
    }

    public void addIn(Set<String> in) {
        this.in.addAll(in);
    }

    public void addOut(Set<String> out) {
        this.out.addAll(out);
    }


}
