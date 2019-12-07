package tra.models;

import java_cup.runtime.Symbol;
import tra.v3.TraLexer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Parser {

    private TraLexer lexer;
    private Node currNode = null;
    private boolean done = false;
    private Node mainNode = new Node("mainNode");

    private byte[] convertCodeToBytes(List<Codes.Code> codes) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            for (Codes.Code code : codes) {
                if (code instanceof Codes.Function) {
                    stream.write(new byte[]{0x51});
                    Codes.Function func = (Codes.Function) code;
                    stream.write(new byte[]{0x01});
                    byte[] name = func.getName().getBytes();
                    stream.write(name.length);
                    stream.write(name);
                    stream.write(new byte[]{0x02});
                    byte[] level = func.getLevel().toString().getBytes();
                    stream.write(level.length);
                    stream.write(level);
                    stream.write(new byte[]{0x03});
                    for (Codes.Identifier id : func.getParams()) {
                        byte[] idName = id.getName().getBytes();
                        stream.write(idName.length);
                        stream.write(idName);
                    }
                    stream.write(new byte[]{0x04});
                    stream.write(new byte[]{0x6f});
                    stream.write(convertCodeToBytes(func.getCodes()));
                    stream.write(new byte[]{0x6e});
                } else if (code instanceof Codes.If) {
                    stream.write(new byte[]{0x52});
                    Codes.If ifCode = (Codes.If) code;
                    stream.write(new byte[]{0x01});
                    stream.write(convertExpressionToBytes(ifCode.getCondition()));
                    stream.write(new byte[]{0x02});
                    stream.write(new byte[]{0x6f});
                    stream.write(convertCodeToBytes(ifCode.getCodes()));
                    stream.write(new byte[]{0x6e});
                    if (ifCode.getExtras() != null) {
                        for (Codes.Code elseCode : ifCode.getExtras()) {
                            if (elseCode instanceof Codes.ElseIf) {
                                stream.write(new byte[]{0x53});
                                Codes.ElseIf elseIfCode = (Codes.ElseIf) elseCode;
                                stream.write(new byte[]{0x01});
                                stream.write(convertExpressionToBytes(elseIfCode.getCondition()));
                                stream.write(new byte[]{0x02});
                                stream.write(new byte[]{0x6f});
                                stream.write(convertCodeToBytes(elseIfCode.getCodes()));
                                stream.write(new byte[]{0x6e});
                            } else if (elseCode instanceof Codes.Else) {
                                stream.write(new byte[]{0x54});
                                Codes.Else lastElseCode = (Codes.Else) elseCode;
                                stream.write(new byte[]{0x01});
                                stream.write(new byte[]{0x6f});
                                stream.write(convertCodeToBytes(lastElseCode.getCodes()));
                                stream.write(new byte[]{0x6e});
                            }
                        }
                    }
                } else if (code instanceof Codes.CounterFor) {
                    stream.write(new byte[]{0x53});
                    stream.write(new byte[]{0x01});
                    stream.write(convertExpressionToBytes(((Codes.CounterFor) code).getLimit()));
                    stream.write(new byte[]{0x02});
                    stream.write(convertExpressionToBytes(((Codes.CounterFor) code).getStep()));
                    stream.write(new byte[]{0x03});
                    stream.write(new byte[]{0x6f});
                    stream.write(convertCodeToBytes(((Codes.CounterFor) code).getCodes()));
                    stream.write(new byte[]{0x6e});
                } else if (code instanceof Codes.While) {
                    stream.write(new byte[]{0x54});
                    stream.write(new byte[]{0x01});
                    stream.write(convertExpressionToBytes(((Codes.While) code).getCondition()));
                    stream.write(new byte[]{0x02});
                    stream.write(new byte[]{0x6f});
                    stream.write(convertCodeToBytes(((Codes.While) code).getCodes()));
                    stream.write(new byte[]{0x6e});
                } else if (code instanceof Codes.Call) {
                    stream.write(new byte[]{0x55});
                    stream.write(new byte[]{0x01});
                    stream.write(convertExpressionToBytes(((Codes.Call) code).getFuncReference()));
                    stream.write(new byte[]{0x02});
                    for (Map.Entry<String, Codes.Code> entry : ((Codes.Call) code).getEntries().entrySet()) {
                        stream.write(new byte[]{0x03});
                        byte[] keyBytes = entry.getKey().getBytes();
                        stream.write(keyBytes.length);
                        stream.write(keyBytes);
                        byte[] valueBytes = convertExpressionToBytes(entry.getValue());
                        stream.write(valueBytes.length);
                        stream.write(valueBytes);
                    }
                } else if (code instanceof Codes.Assignment) {
                    stream.write(new byte[]{0x56});
                    stream.write(new byte[]{0x01});
                    byte[] varBytes = convertExpressionToBytes(((Codes.Assignment) code).getVar());
                    stream.write(varBytes);
                    stream.write(new byte[]{0x02});
                    byte[] valueBytes = convertExpressionToBytes(((Codes.Assignment) code).getValue());
                    stream.write(valueBytes);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return stream.toByteArray();
    }

    private byte[] convertExpressionToBytes(Codes.Code exp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            if (exp instanceof Codes.MathExpSum) {
                stream.write(new byte[]{0x71});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpSum) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpSum) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpSubstract) {
                stream.write(new byte[]{0x72});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpSubstract) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpSubstract) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpMultiply) {
                stream.write(new byte[]{0x73});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpMultiply) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpMultiply) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpDivide) {
                stream.write(new byte[]{0x74});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpDivide) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpDivide) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpMod) {
                stream.write(new byte[]{0x75});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpMod) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpMod) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpPower) {
                stream.write(new byte[]{0x76});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpPower) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpPower) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpAnd) {
                stream.write(new byte[]{0x77});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpAnd) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpAnd) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpOr) {
                stream.write(new byte[]{0x78});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpOr) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpOr) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpEqual) {
                stream.write(new byte[]{0x79});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpEqual) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpEqual) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpGT) {
                stream.write(new byte[]{0x7a});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpGT) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpGT) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpGE) {
                stream.write(new byte[]{0x7b});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpGE) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpGE) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpNE) {
                stream.write(new byte[]{0x7c});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpNE) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpNE) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpLE) {
                stream.write(new byte[]{0x7d});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpLE) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpLE) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.MathExpLT) {
                stream.write(new byte[]{0x7e});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.MathExpLT) exp).getValue1()));
                stream.write(new byte[]{0x02});
                stream.write(convertExpressionToBytes(((Codes.MathExpLT) exp).getValue2()));
                return stream.toByteArray();
            } else if (exp instanceof Codes.Call) {
                stream.write(new byte[]{0x55});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.Call) exp).getFuncReference()));
                stream.write(new byte[]{0x02});
                stream.write(((Codes.Call) exp).getEntries().size());
                for (Map.Entry<String, Codes.Code> entry : ((Codes.Call) exp).getEntries().entrySet()) {
                    stream.write(new byte[]{0x03});
                    byte[] keyBytes = entry.getKey().getBytes();
                    stream.write(keyBytes.length);
                    stream.write(keyBytes);
                    byte[] valueBytes = convertExpressionToBytes(entry.getValue());
                    stream.write(valueBytes.length);
                    stream.write(valueBytes);
                }
            } else if (exp instanceof Codes.Identifier) {
                stream.write(new byte[]{0x61});
                byte[] idName = ((Codes.Identifier) exp).getName().getBytes();
                stream.write(idName.length);
                stream.write(idName);
                return stream.toByteArray();
            } else if (exp instanceof Codes.Value) {
                if (((Codes.Value) exp).getValue() instanceof String) {
                    stream.write(new byte[]{0x62});
                    byte[] value = ((String) ((Codes.Value) exp).getValue()).getBytes();
                    stream.write(value.length);
                    stream.write(value);
                } else if (((Codes.Value) exp).getValue() instanceof Double) {
                    stream.write(new byte[]{0x63});
                    stream.write(convertDoubleToBytes((Double)((Codes.Value) exp).getValue()));
                } else if (((Codes.Value) exp).getValue() instanceof Float) {
                    stream.write(new byte[]{0x64});
                    stream.write(convertFloatToBytes((Float)((Codes.Value) exp).getValue()));
                } else if (((Codes.Value) exp).getValue() instanceof Short) {
                    stream.write(new byte[]{0x65});
                    stream.write(convertShortToBytes((Short)((Codes.Value) exp).getValue()));
                } else if (((Codes.Value) exp).getValue() instanceof Integer) {
                    stream.write(new byte[]{0x66});
                    stream.write(convertIntegerToBytes((Integer)((Codes.Value) exp).getValue()));
                } else if (((Codes.Value) exp).getValue() instanceof Long) {
                    stream.write(new byte[]{0x67});
                    stream.write(convertLongToBytes((Long)((Codes.Value) exp).getValue()));
                } else if (((Codes.Value) exp).getValue() instanceof Boolean) {
                    stream.write(new byte[]{0x68});
                    stream.write(convertBooleanToBytes((Boolean) ((Codes.Value) exp).getValue()));
                }
                return stream.toByteArray();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return stream.toByteArray();
    }

    private byte[] convertDoubleToBytes(double number) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        writer.print(number);
        writer.close();
        return stream.toByteArray();
    }

    private byte[] convertFloatToBytes(float number) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        writer.print(number);
        writer.close();
        return stream.toByteArray();
    }

    private byte[] convertShortToBytes(short number) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        writer.print(number);
        writer.close();
        return stream.toByteArray();
    }

    private byte[] convertIntegerToBytes(int number) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        writer.print(number);
        writer.close();
        return stream.toByteArray();
    }

    private byte[] convertLongToBytes(long number) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        writer.print(number);
        writer.close();
        return stream.toByteArray();
    }

    private byte[] convertBooleanToBytes(boolean number) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(stream);
        writer.print(number);
        writer.close();
        return stream.toByteArray();
    }

    public Parser(TraLexer lexer) {

        this.lexer = lexer;

        Node rootNode = new Node("rootNode");
        mainNode.next(Arrays.asList(sym.terminalNames[sym.STARTPROGRAM], sym.terminalNames[sym.START],
                rootNode, sym.terminalNames[sym.END], sym.terminalNames[sym.ENDPROGRAM]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        List<Codes.Code> codes = (List<Codes.Code>) prevResults.get(0).second;
                        return convertCodeToBytes(codes);
                    }
                });
        rootNode.next(Collections.singletonList(sym.terminalNames[sym.EMPTY]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<Codes.Code>();
                    }
                });
        Node expNode = new Node("expNode");
        expNode.next(Arrays.asList(sym.terminalNames[sym.LPAREN], expNode, sym.terminalNames[sym.RPAREN]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.SUM], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpSum sum = new Codes.MathExpSum();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.SUBTRACT], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpSubstract subtract = new Codes.MathExpSubstract();
                        subtract.setValue1((Codes.Code)prevResults.get(0).second);
                        subtract.setValue2((Codes.Code)prevResults.get(1).second);
                        return subtract;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.MULTIPLY], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMultiply multiply = new Codes.MathExpMultiply();
                        multiply.setValue1((Codes.Code)prevResults.get(0).second);
                        multiply.setValue2((Codes.Code)prevResults.get(1).second);
                        return multiply;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.DIVISION], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpDivide divide = new Codes.MathExpDivide();
                        divide.setValue1((Codes.Code)prevResults.get(0).second);
                        divide.setValue2((Codes.Code)prevResults.get(1).second);
                        return divide;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.AND], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpAnd and = new Codes.MathExpAnd();
                        and.setValue1((Codes.Code)prevResults.get(0).second);
                        and.setValue2((Codes.Code)prevResults.get(1).second);
                        return and;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.OR], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpOr or = new Codes.MathExpOr();
                        or.setValue1((Codes.Code)prevResults.get(0).second);
                        or.setValue2((Codes.Code)prevResults.get(1).second);
                        return or;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.EQUAL], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpEqual equalC = new Codes.MathExpEqual();
                        equalC.setValue1((Codes.Code)prevResults.get(0).second);
                        equalC.setValue2((Codes.Code)prevResults.get(1).second);
                        return equalC;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.POWER], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpPower pow = new Codes.MathExpPower();
                        pow.setValue1((Codes.Code)prevResults.get(0).second);
                        pow.setValue2((Codes.Code)prevResults.get(1).second);
                        return pow;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.MOD], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.GT], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.GE], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.NE], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.LE], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.LT], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });

        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.SUM], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpSum sum = new Codes.MathExpSum();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.SUBTRACT], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpSubstract subtract = new Codes.MathExpSubstract();
                        subtract.setValue1((Codes.Code)prevResults.get(0).second);
                        subtract.setValue2((Codes.Code)prevResults.get(1).second);
                        return subtract;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.MULTIPLY], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMultiply multiply = new Codes.MathExpMultiply();
                        multiply.setValue1((Codes.Code)prevResults.get(0).second);
                        multiply.setValue2((Codes.Code)prevResults.get(1).second);
                        return multiply;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.DIVISION], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpDivide divide = new Codes.MathExpDivide();
                        divide.setValue1((Codes.Code)prevResults.get(0).second);
                        divide.setValue2((Codes.Code)prevResults.get(1).second);
                        return divide;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.AND], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpAnd and = new Codes.MathExpAnd();
                        and.setValue1((Codes.Code)prevResults.get(0).second);
                        and.setValue2((Codes.Code)prevResults.get(1).second);
                        return and;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.OR], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpOr or = new Codes.MathExpOr();
                        or.setValue1((Codes.Code)prevResults.get(0).second);
                        or.setValue2((Codes.Code)prevResults.get(1).second);
                        return or;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.EQUAL], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpEqual equalC = new Codes.MathExpEqual();
                        equalC.setValue1((Codes.Code)prevResults.get(0).second);
                        equalC.setValue2((Codes.Code)prevResults.get(1).second);
                        return equalC;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.POWER], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpPower pow = new Codes.MathExpPower();
                        pow.setValue1((Codes.Code)prevResults.get(0).second);
                        pow.setValue2((Codes.Code)prevResults.get(1).second);
                        return pow;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.MOD], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.GT], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.GE], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.NE], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.LE], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.STRING], sym.terminalNames[sym.LT], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });

        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.SUM], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpSum sum = new Codes.MathExpSum();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.SUBTRACT], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpSubstract subtract = new Codes.MathExpSubstract();
                        subtract.setValue1((Codes.Code)prevResults.get(0).second);
                        subtract.setValue2((Codes.Code)prevResults.get(1).second);
                        return subtract;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.MULTIPLY], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMultiply multiply = new Codes.MathExpMultiply();
                        multiply.setValue1((Codes.Code)prevResults.get(0).second);
                        multiply.setValue2((Codes.Code)prevResults.get(1).second);
                        return multiply;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.DIVISION], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpDivide divide = new Codes.MathExpDivide();
                        divide.setValue1((Codes.Code)prevResults.get(0).second);
                        divide.setValue2((Codes.Code)prevResults.get(1).second);
                        return divide;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.AND], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpAnd and = new Codes.MathExpAnd();
                        and.setValue1((Codes.Code)prevResults.get(0).second);
                        and.setValue2((Codes.Code)prevResults.get(1).second);
                        return and;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.OR], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpOr or = new Codes.MathExpOr();
                        or.setValue1((Codes.Code)prevResults.get(0).second);
                        or.setValue2((Codes.Code)prevResults.get(1).second);
                        return or;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.EQUAL], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpEqual equalC = new Codes.MathExpEqual();
                        equalC.setValue1((Codes.Code)prevResults.get(0).second);
                        equalC.setValue2((Codes.Code)prevResults.get(1).second);
                        return equalC;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.POWER], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpPower pow = new Codes.MathExpPower();
                        pow.setValue1((Codes.Code)prevResults.get(0).second);
                        pow.setValue2((Codes.Code)prevResults.get(1).second);
                        return pow;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.MOD], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.GT], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.GE], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.NE], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.LE], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.NUMBER], sym.terminalNames[sym.LT], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod mod = new Codes.MathExpMod();
                        mod.setValue1((Codes.Code)prevResults.get(0).second);
                        mod.setValue2((Codes.Code)prevResults.get(1).second);
                        return mod;
                    }
                });

        expNode.next(Collections.singletonList(sym.terminalNames[sym.NUMBER]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        expNode.next(Collections.singletonList(sym.terminalNames[sym.STRING]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        expNode.next(Collections.singletonList(sym.terminalNames[sym.IDENTIFIER]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });

        Node ifNode = new Node("ifNode");
        rootNode.next(Arrays.asList(sym.terminalNames[sym.IF], ifNode, rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Code newLine = (Codes.Code) prevResults.get(0).second;
                        List<Codes.Code> codes = (List<Codes.Code>) prevResults.get(1).second;
                        codes.add(0, newLine);
                        return codes;
                    }
                });
        Node elseNode = new Node("elseNode");
        ifNode.next(Arrays.asList(expNode, sym.terminalNames[sym.THEN],
                sym.terminalNames[sym.START], rootNode, sym.terminalNames[sym.END], elseNode),
                new Action() {
                    @Override
                    public Codes.Code act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> condition = prevResults.get(0);
                        Pair<Symbol, Object> body = prevResults.get(1);
                        Object extras = null;
                        if (prevResults.size() > 2)
                            extras = prevResults.get(2).second;
                        Codes.If ifCode = new Codes.If();
                        ifCode.setCondition((Codes.Code)condition.second);
                        ifCode.setCodes((List<Codes.Code>)body.second);
                        ifCode.setExtras((List<Codes.Code>)extras);
                        return ifCode;
                    }
                });
        elseNode.next(Arrays.asList(sym.terminalNames[sym.ELSE], sym.terminalNames[sym.START],
                sym.terminalNames[sym.EMPTY], sym.terminalNames[sym.END]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> body = prevResults.get(0);
                        Codes.Else elseCode = new Codes.Else();
                        elseCode.setCodes((List<Codes.Code>)body);
                        return elseCode;
                    }
                });
        Node epsilon = new Node("epsilon");
        elseNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<Codes.Code>();
                    }
                });
        Node loopNode = new Node("loopNode");
        rootNode.next(Arrays.asList(sym.terminalNames[sym.LOOP], loopNode, rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Code newLine = (Codes.Code) prevResults.get(0).second;
                        List<Codes.Code> codes = (List<Codes.Code>) prevResults.get(1).second;
                        codes.add(0, newLine);
                        return codes;
                    }
                });
        Node forLoop = new Node("forNode");
        loopNode.next(Arrays.asList(sym.terminalNames[sym.FOR], forLoop),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        Node stepNode = new Node("stepNode");
        stepNode.next(Arrays.asList(sym.terminalNames[sym.BY], sym.terminalNames[sym.STEP], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        stepNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return null;
                    }
                });
        forLoop.next(Arrays.asList(expNode, sym.terminalNames[sym.TIMES], stepNode, sym.terminalNames[sym.START],
                rootNode, sym.terminalNames[sym.END]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> exp = prevResults.get(0);
                        Pair<Symbol, Object> step = prevResults.get(1);
                        Pair<Symbol, Object> body = prevResults.get(2);
                        Codes.CounterFor forCode = new Codes.CounterFor();
                        forCode.setLimit((Codes.Code)exp.second);
                        forCode.setCodes((List<Codes.Code>)body.second);
                        forCode.setStep((Codes.Code)step.second);
                        return forCode;
                    }
                });
        Node whileLoop = new Node("whileNode");
        loopNode.next(Arrays.asList(sym.terminalNames[sym.UNTIL], whileLoop),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        whileLoop.next(Arrays.asList(expNode, sym.terminalNames[sym.START], rootNode,
                sym.terminalNames[sym.END]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> exp = prevResults.get(0);
                        Pair<Symbol, Object> body = prevResults.get(1);
                        Codes.While whileCode = new Codes.While();
                        whileCode.setCondition((Codes.Code)exp.second);
                        whileCode.setCodes((List<Codes.Code>)body.second);
                        return whileCode;
                    }
                });
        Node rememberNode = new Node("remNode");
        rootNode.next(Arrays.asList(sym.terminalNames[sym.REMEMBER], rememberNode, rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Code newLine = (Codes.Code) prevResults.get(0).second;
                        List<Codes.Code> codes = (List<Codes.Code>) prevResults.get(1).second;
                        codes.add(0, newLine);
                        return codes;
                    }
                });
        rememberNode.next(Arrays.asList(expNode, sym.terminalNames[sym.AS], sym.terminalNames[sym.IDENTIFIER]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> exp = prevResults.get(0);
                        Pair<Symbol, Object> id = prevResults.get(1);
                        Codes.Assignment assignment = new Codes.Assignment();
                        assignment.setVar((Codes.Identifier)id.second);
                        assignment.setValue((Codes.Code)exp.second);
                        return assignment;
                    }
                });
        Node functionNode = new Node("funcNode");
        rootNode.next(Arrays.asList(sym.terminalNames[sym.DEFINE], sym.terminalNames[sym.FUNCTION], functionNode, rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Code newLine = (Codes.Code) prevResults.get(0).second;
                        List<Codes.Code> codes = (List<Codes.Code>) prevResults.get(1).second;
                        codes.add(0, newLine);
                        return codes;
                    }
                });
        Node funcLevelNode = new Node("funcLevelNode");
        Node funcLevelNameNode = new Node("funcLevelNameNode");
        funcLevelNode.next(Arrays.asList(sym.terminalNames[sym.OF], funcLevelNameNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        funcLevelNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return Codes.DataLevel.InstanceLevel;
                    }
                });
        funcLevelNameNode.next(Collections.singletonList(sym.terminalNames[sym.CLASS]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return Codes.DataLevel.ClassLevel;
                    }
                });
        funcLevelNameNode.next(Collections.singletonList(sym.terminalNames[sym.INSTANCE]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return Codes.DataLevel.InstanceLevel;
                    }
                });
        Node funcParams = new Node("funcParams");
        Node funcParamsList = new Node("funcParamsList");
        funcParams.next(Arrays.asList(sym.terminalNames[sym.WITH], sym.terminalNames[sym.PARAMS],
                sym.terminalNames[sym.IDENTIFIER], funcParamsList),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> identifier = prevResults.get(0);
                        Pair<Symbol, Object> identifierList = prevResults.get(1);
                        List<Codes.Identifier> idList = (List<Codes.Identifier>) identifierList.second;
                        idList.add((Codes.Identifier) identifier.second);
                        return idList;
                    }
                });
        funcParamsList.next(Arrays.asList(sym.terminalNames[sym.COMMA], sym.terminalNames[sym.IDENTIFIER], funcParamsList),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> identifier = prevResults.get(0);
                        Pair<Symbol, Object> identifierList = prevResults.get(1);
                        List<Codes.Identifier> idList = (List<Codes.Identifier>) identifierList.second;
                        idList.add((Codes.Identifier) identifier.second);
                        return idList;
                    }
                });
        funcParamsList.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<Codes.Identifier>();
                    }
                });
        functionNode.next(Arrays.asList(sym.terminalNames[sym.NAMED], sym.terminalNames[sym.IDENTIFIER],
                funcLevelNode, funcParams, sym.terminalNames[sym.START], rootNode, sym.terminalNames[sym.END]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> funcName = prevResults.get(0);
                        Pair<Symbol, Object> funcLevel = prevResults.get(1);
                        Pair<Symbol, Object> funcParams = prevResults.get(2);
                        Pair<Symbol, Object> funcCodes = prevResults.get(3);
                        Codes.Function func = new Codes.Function();
                        func.setName(((Codes.Identifier)funcName.second).getName());
                        func.setLevel(((Codes.DataLevel)funcLevel.second));
                        func.setParams((List<Codes.Identifier>)funcParams.second);
                        func.setCodes((List<Codes.Code>)funcCodes.second);
                        return func;
                    }
                });
        Node inputsNode = new Node("inputsNode");
        Node inputsExtraNode = new Node("inputsExtraNode");
        inputsNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], sym.terminalNames[sym.COLON],
                expNode, inputsExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Hashtable<String, Codes.Code> ids = (Hashtable<String, Codes.Code>) prevResults.get(2).second;
                        Codes.Code exp = (Codes.Code) prevResults.get(1).second;
                        String inputName = ((Codes.Identifier) prevResults.get(0).second).getName();
                        ids.put(inputName, exp);
                        return ids;
                    }
                });
        inputsNode.next(Arrays.asList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new Hashtable<>();
                    }
                });
        inputsExtraNode.next(Arrays.asList(sym.terminalNames[sym.COMMA], sym.terminalNames[sym.IDENTIFIER],
                sym.terminalNames[sym.COLON], expNode, inputsExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Hashtable<String, Codes.Code> ids = (Hashtable<String, Codes.Code>) prevResults.get(2).second;
                        Codes.Code exp = (Codes.Code) prevResults.get(1).second;
                        String inputName = ((Codes.Identifier) prevResults.get(0).second).getName();
                        ids.put(inputName, exp);
                        return ids;
                    }
                });
        inputsExtraNode.next(Arrays.asList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new Hashtable<>();
                    }
                });
        rootNode.next(Arrays.asList(sym.terminalNames[sym.DO], expNode,
                sym.terminalNames[sym.LPAREN], inputsNode, sym.terminalNames[sym.RPAREN], rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Call call = new Codes.Call();
                        call.setEntries((Hashtable<String, Codes.Code>) prevResults.get(1).second);
                        call.setFuncReference((Codes.Code)prevResults.get(0).second);
                        List<Codes.Code> restOfCode = (List<Codes.Code>) prevResults.get(2).second;
                        restOfCode.add(call);
                        return restOfCode;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.DO], expNode,
                sym.terminalNames[sym.LPAREN], inputsNode, sym.terminalNames[sym.RPAREN]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Call call = new Codes.Call();
                        call.setEntries((Hashtable<String, Codes.Code>) prevResults.get(1).second);
                        call.setFuncReference((Codes.Code)prevResults.get(0).second);
                        return call;
                    }
                });
        rootNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<>();
                    }
                });
    }

    public Object parse() {
        List<Symbol> tokens = new ArrayList<>();
        Symbol token;
        try {
            while ((token = this.lexer.next_token()).value != null) tokens.add(token);
            tokens.add(0, new Symbol(sym.START, 0, 0, null));
            tokens.add(0, new Symbol(sym.STARTPROGRAM, 0, 0, null));
            tokens.add(new Symbol(sym.END, 0, 0, null));
            tokens.add(new Symbol(sym.ENDPROGRAM, 0, 0, null));
            tokens.add(new Symbol(sym.EOF, 0, 0, null));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.currNode = mainNode;
        return this.internalParse(tokens.toArray(new Symbol[0]), 0, 0).second;
    }

    private Pair<Integer, Object> internalParse(Symbol[] contentStringParts, int counter, int level) {
        int savedCounter = 0;
        List<Pair<Symbol, Object>> data = new ArrayList<>();
        Action resultAction = null;
        for (Pair<List<Object>, Action> subNodesActions : currNode.subNodes) {
            data = new ArrayList<>();
            List<Object> subNodes = subNodesActions.first;
            Action action = subNodesActions.second;
            resultAction = action;
            counter -= savedCounter;
            savedCounter = 0;
            int progress = 0;
            for (Object subNode : subNodes) {
                Symbol token = contentStringParts[counter];
                if (token.sym == sym.EOF) {
                    done = true;
                    break;
                }
                if (subNode instanceof Node) {
                    if (((Node) subNode).name.equals("epsilon")) {
                        return new Pair<>(counter, action.act(new ArrayList<>()));
                    } else {
                        Node temp = currNode;
                        currNode = (Node) subNode;
                        Pair<Integer, Object> pair = internalParse(contentStringParts, counter, level + 1);
                        data.add(new Pair<>(new Symbol(0, 0, 0, 0), pair.second));
                        counter = pair.first;
                        if (done) break;
                        currNode = temp;
                    }
                } else if (subNode instanceof String) {
                    String value = (String) subNode;
                    if (value.equals(sym.terminalNames[token.sym])) {
                        counter++;
                        savedCounter++;
                        if (token.sym == sym.IDENTIFIER) {
                            Codes.Identifier id = new Codes.Identifier();
                            id.setName((String)token.value);
                            data.add(new Pair<>(new Symbol(token.sym, 0, 0, id.getName()), id));
                        } else if (token.sym == sym.NUMBER) {
                            Codes.Value number = new Codes.Value();
                            number.setValue(token.value);
                            data.add(new Pair<>(new Symbol(token.sym, 0, 0, number.getValue()), number));
                        } else if (token.sym == sym.STRING) {
                            Codes.Value string = new Codes.Value();
                            string.setValue(token.value);
                            data.add(new Pair<>(new Symbol(token.sym, 0, 0, string.getValue()), string));
                        }
                    } else {
                        break;
                    }
                }
                if (done) break;
                progress++;
            }
            if (progress == subNodes.size()) break;
            if (done) break;
        }

        return new Pair<>(counter, resultAction.act(data));
    }
}
