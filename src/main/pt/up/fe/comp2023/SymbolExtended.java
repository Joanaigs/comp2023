package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class SymbolExtended extends Symbol{
    Boolean local;
    public SymbolExtended(Type type, String name, Boolean local) {
        super(type, name);
        this.local=local;
    }

    public Boolean getLocal() {
        return this.local;
    }
}
