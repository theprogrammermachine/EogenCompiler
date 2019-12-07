package tra.models;

import java_cup.runtime.Symbol;

public class TabSymbol extends Symbol {

    private int tabLength;

    public TabSymbol(int id, Symbol left, Symbol right, Object o) {
        super(id, left, right, o);
        this.tabLength = ((String) o).length();
    }

    public TabSymbol(int id, Symbol left, Symbol right) {
        super(id, left, right);
    }

    public TabSymbol(int id, int l, int r, Object o) {
        super(id, l, r, o);
        this.tabLength = ((String) o).length();
    }

    public TabSymbol(int id, Object o) {
        super(id, o);
        this.tabLength = ((String) o).length();
    }

    public TabSymbol(int id, int l, int r) {
        super(id, l, r);
    }

    public TabSymbol(int sym_num) {
        super(sym_num);
    }
}
