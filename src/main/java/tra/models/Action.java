package tra.models;

import java_cup.runtime.Symbol;

import java.util.List;

public interface Action {

    public Object act(List<Pair<Symbol, Object>> prevResults);
}
