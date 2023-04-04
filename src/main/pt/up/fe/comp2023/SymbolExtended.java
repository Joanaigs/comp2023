package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class SymbolExtended extends Symbol{
    Boolean isLocal;
    String value = "";

    public String getValue() {
        return value;
    }

    public SymbolExtended(Type type, String name, Boolean local) {
        super(type, name);
        this.isLocal=local;
    }
    public SymbolExtended(Type type, String name) {
        super(type, name);
    }

    public Boolean getLocal() {
        return this.isLocal;
    }
}
