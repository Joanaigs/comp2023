package pt.up.fe.comp2023.semantic_analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class SymbolExtended extends Symbol{
    Boolean isLocal;
    public SymbolExtended(Type type, String name, Boolean local) {
        super(type, name);
        this.isLocal=local;
    }

    public Boolean getLocal() {
        return this.isLocal;
    }
}
