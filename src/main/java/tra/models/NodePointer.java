package tra.models;

import java_cup.runtime.Symbol;

import java.util.*;

public class NodePointer {

    public String id;
    public Node node;
    public int tokenPointer;
    public int rulePointer;
    public int ruleTokenPointer;
    public int startPoint;
    public boolean foundMatch;
    public NodePointer backedBy;
    public int backedByPos;
    public List<Pair<Symbol, Object>> inputs = new ArrayList<>();
    public NodePointer prev;

    private Stack<Integer> repairHistory = new Stack<>();

    public NodePointer(String id, Node node, int tokenPointer, int rulePointer, int ruleTokenPointer, int startPoint, NodePointer backendBy, NodePointer prev) {
        this.id = id;
        this.node = node;
        this.tokenPointer = tokenPointer;
        this.rulePointer = rulePointer;
        this.ruleTokenPointer = ruleTokenPointer;
        this.startPoint = startPoint;
        this.foundMatch = false;
        this.backedBy = backendBy;
        if (backendBy != null)
            this.backedByPos = backendBy.rulePointer;
        this.prev = prev;
    }

    public List<Object> currentRule() {
        return this.node.subNodes.get(this.rulePointer).first;
    }

    public Action currentAction() {
        return this.node.subNodes.get(this.rulePointer).second;
    }

    public Object currentRuleToken() {
        return this.node.subNodes.get(this.rulePointer).first.get(this.ruleTokenPointer);
    }

    public boolean configRulePointerToRepair(Symbol nextSymbol) {
        if (this.ruleTokenPointer > 0) {
            for (int counter = this.rulePointer + 1; counter < this.node.subNodes.size(); counter++) {
                if (this.ruleTokenPointer < this.node.subNodes.get(counter).first.size()) {
                    boolean matched = true;
                    for (int counter2 = 0; counter2 < this.ruleTokenPointer; counter2++) {
                        if (this.node.subNodes.get(counter).first.get(counter2) instanceof String &&
                                this.node.subNodes.get(this.rulePointer).first.get(counter2) instanceof String) {
                            if (!this.node.subNodes.get(this.rulePointer).first.get(counter2).
                                    equals(this.node.subNodes.get(counter).first.get(counter2))) {
                                matched = false;
                                break;
                            }
                        } else if (this.node.subNodes.get(counter).first.get(counter2) instanceof Node &&
                                this.node.subNodes.get(this.rulePointer).first.get(counter2) instanceof Node) {
                            if (!((Node) this.node.subNodes.get(this.rulePointer).first.get(counter2)).name.
                                    equals(((Node) this.node.subNodes.get(counter).first.get(counter2)).name)) {
                                matched = false;
                                break;
                            }
                        } else {
                            matched = false;
                            break;
                        }
                    }
                    if (matched) {
                        if (sym.terminalNames[nextSymbol.sym].equals(this.node.subNodes.get(counter).first.get(this.ruleTokenPointer))) {
                            this.repairHistory.add(this.rulePointer);
                            this.rulePointer = counter;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void revertRepair() {
        if (!this.repairHistory.isEmpty()) {
            this.rulePointer = this.repairHistory.pop();
            this.ruleTokenPointer = 0;
        }
    }

    public boolean missingPast() {
        boolean foundWaysForward = false;
        for (int counter = this.ruleTokenPointer; counter < this.node.subNodes.get(this.rulePointer).first.size(); counter++) {
            if (this.node.subNodes.get(this.rulePointer).first.get(counter) instanceof Node) {
                foundWaysForward = true;
                break;
            }
        }
        boolean missingBackward = false;
        for (int counter = 0; counter < this.ruleTokenPointer; counter++) {
            if (this.node.subNodes.get(this.rulePointer).first.get(counter) instanceof Node) {
                missingBackward = true;
                break;
            }
        }
        return (missingBackward && !foundWaysForward);
    }

    public Object prevRuleToken() {
        if (this.ruleTokenPointer > 0)
            return this.node.subNodes.get(this.rulePointer).first.get(this.ruleTokenPointer - 1);
        else
            return null;
    }

    public int currentRuleSize() {
        return this.node.subNodes.get(this.rulePointer).first.size();
    }

    public boolean reachedCurrentRuleEnd() {
        if (this.ruleTokenPointer > this.node.subNodes.get(this.rulePointer).first.size() - 1) {
            return true;
        } else {
            if (this.node.subNodes.get(this.rulePointer).first.get(this.ruleTokenPointer) instanceof Node) {
                return this.ruleTokenPointer >= this.node.subNodes.get(this.rulePointer).first.size() - 1;
            } else {
                return this.ruleTokenPointer > this.node.subNodes.get(this.rulePointer).first.size() - 1;
            }
        }
    }

    public void forwardRule() {
        this.rulePointer++;
        this.ruleTokenPointer = 0;
    }

    public void forwardToken() {
        this.ruleTokenPointer++;
    }

    public boolean reachedAllRulesEnd() {
        return this.rulePointer >= this.node.subNodes.size() - 1;
    }

    public boolean nextRuleIsEnd() {
        return this.rulePointer + 1 >= this.node.subNodes.size() - 1;
    }

    public boolean readyToMatchRule() {
        return this.ruleTokenPointer + 1 > this.node.subNodes.get(this.rulePointer).first.size() - 1;
    }

    public String nodeName() {
        return this.node.name;
    }

    public NodePointer makeChildOfYourself() {
        return new NodePointer(
                this.id,
                this.node,
                this.tokenPointer,
                this.rulePointer,
                this.ruleTokenPointer,
                this.startPoint,
                this.backedBy,
                this);
    }
}
