package pt.up.fe.comp2023.ollir.optimization.liveness;

import pt.up.fe.comp2023.semantic_analysis.Method;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LivenessData {
    private Set<String> def;
    private Set<String> use;
    private Set<String> in;
    private Set<String> out;

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
