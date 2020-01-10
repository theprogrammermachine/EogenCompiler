package tra.v3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java_cup.runtime.Symbol;
import tra.helpers.JsonHelper;
import tra.models.*;
import tra.models.Action;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

public class TraParser {

    private TraLexer lexer;
    private Node mainNode = new Node("mainNode");
    Node epsilon = new Node("epsilon");

    private byte[] convertCodeToBytes(List<Codes.Code> codes) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            for (Codes.Code code : codes) {
                if (code instanceof Codes.Function) {
                    stream.write(new byte[]{0x51});
                    Codes.Function func = (Codes.Function) code;
                    stream.write(new byte[]{0x01});
                    byte[] name = func.getName().getBytes();
                    stream.write(convertIntegerToBytes(name.length + 1));
                    stream.write(name);
                    stream.write('\0');
                    stream.write(new byte[]{0x02});
                    byte[] level = func.getLevel().toString().getBytes();
                    stream.write(convertIntegerToBytes(level.length + 1));
                    stream.write(level);
                    stream.write('\0');
                    stream.write(new byte[]{0x03});
                    stream.write(convertIntegerToBytes(func.getParams().size()));
                    for (Codes.Identifier id : func.getParams()) {
                        byte[] idName = id.getName().getBytes();
                        stream.write(convertIntegerToBytes(idName.length));
                        stream.write(idName);
                    }
                    stream.write(new byte[]{0x04});
                    stream.write(new byte[]{0x6f});
                    byte[] cs = convertCodeToBytes(func.getCodes());
                    stream.write(convertIntegerToBytes(cs.length));
                    stream.write(cs);
                    stream.write(new byte[]{0x6e});
                } else if (code instanceof Codes.If) {
                    stream.write(new byte[]{0x52});
                    Codes.If ifCode = (Codes.If) code;
                    stream.write(new byte[]{0x01});
                    stream.write(convertExpressionToBytes(ifCode.getCondition()));
                    stream.write(new byte[]{0x02});
                    stream.write(new byte[]{0x6f});
                    byte[] cs = convertCodeToBytes(ifCode.getCodes());
                    stream.write(convertIntegerToBytes(cs.length));
                    stream.write(cs);
                    stream.write(new byte[]{0x6e});
                    if (ifCode.getExtras() != null) {
                        stream.write(new byte[] {0x03});
                        stream.write(convertIntegerToBytes(ifCode.getExtras().size()));
                        for (Codes.Code elseCode : ifCode.getExtras()) {
                            if (elseCode instanceof Codes.ElseIf) {
                                stream.write(new byte[]{0x53});
                                Codes.ElseIf elseIfCode = (Codes.ElseIf) elseCode;
                                stream.write(new byte[]{0x01});
                                stream.write(convertExpressionToBytes(elseIfCode.getCondition()));
                                stream.write(new byte[]{0x02});
                                stream.write(new byte[]{0x6f});
                                cs = convertCodeToBytes(elseIfCode.getCodes());
                                stream.write(convertIntegerToBytes(cs.length));
                                stream.write(cs);
                                stream.write(new byte[]{0x6e});
                            } else if (elseCode instanceof Codes.Else) {
                                stream.write(new byte[]{0x54});
                                Codes.Else lastElseCode = (Codes.Else) elseCode;
                                stream.write(new byte[]{0x01});
                                stream.write(new byte[]{0x6f});
                                cs = convertCodeToBytes(lastElseCode.getCodes());
                                stream.write(convertIntegerToBytes(cs.length));
                                stream.write(cs);
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
                    byte[] cs = convertCodeToBytes(((Codes.CounterFor) code).getCodes());
                    stream.write(convertIntegerToBytes(cs.length));
                    stream.write(cs);
                    stream.write(new byte[]{0x6e});
                } else if (code instanceof Codes.While) {
                    stream.write(new byte[]{0x54});
                    stream.write(new byte[]{0x01});
                    stream.write(convertExpressionToBytes(((Codes.While) code).getCondition()));
                    stream.write(new byte[]{0x02});
                    stream.write(new byte[]{0x6f});
                    byte[] cs = convertCodeToBytes(((Codes.While) code).getCodes());
                    stream.write(convertIntegerToBytes(cs.length));
                    stream.write(cs);
                    stream.write(new byte[]{0x6e});
                } else if (code instanceof Codes.Call) {
                    stream.write(new byte[]{0x55});
                    stream.write(new byte[]{0x01});
                    Codes.Code temp = ((Codes.Call) code).getFuncReference();
                    stream.write(new byte[]{0x7f});
                    int counter = 0;
                    while (temp != null) {
                        if (temp instanceof Codes.Reference) {
                            temp = ((Codes.Reference) temp).getRestOfChains();
                        } else if (temp instanceof Codes.Index) {
                            temp = ((Codes.Index) temp).getIndex();
                        }
                        counter++;
                    }
                    stream.write(convertIntegerToBytes(counter));
                    temp = ((Codes.Call) code).getFuncReference();
                    while (temp != null) {
                        if (temp instanceof Codes.Reference) {
                            byte[] idNameBytes = ((Codes.Reference) temp).getCurrentChain().getName().getBytes();
                            stream.write(convertIntegerToBytes(idNameBytes.length + 1));
                            stream.write(idNameBytes);
                            stream.write('\0');
                            temp = ((Codes.Reference) temp).getRestOfChains();
                        } else if (temp instanceof Codes.Index) {
                            stream.write(new byte[]{0x6d});

                            temp = ((Codes.Index) temp).getVar();

                        }
                    }
                    stream.write(new byte[]{0x02});
                    stream.write(convertIntegerToBytes(((Codes.Call) code).getEntries().size()));
                    for (Map.Entry<String, Codes.Code> entry : ((Codes.Call) code).getEntries().entrySet()) {
                        stream.write(new byte[]{0x03});
                        byte[] keyBytes = entry.getKey().getBytes();
                        stream.write(convertIntegerToBytes(keyBytes.length + 1));
                        stream.write(keyBytes);
                        stream.write('\0');
                        byte[] valueBytes = convertExpressionToBytes(entry.getValue());
                        stream.write(convertIntegerToBytes(valueBytes.length));
                        stream.write(valueBytes);
                    }
                } else if (code instanceof Codes.Assignment) {
                    stream.write(new byte[]{0x56});
                    stream.write(new byte[]{0x01});
                    stream.write(convertExpressionToBytes(((Codes.Assignment) code).getVar()));
                    stream.write(new byte[]{0x02});
                    stream.write(convertExpressionToBytes(((Codes.Assignment) code).getValue()));
                } else if (code instanceof Codes.Instantiate) {
                    stream.write(new byte[]{0x57});
                    stream.write(new byte[]{0x01});
                    stream.write(convertExpressionToBytes(((Codes.Instantiate) code).getClassReference()));
                    stream.write(new byte[]{0x02});
                    stream.write(convertIntegerToBytes(((Codes.Instantiate) code).getEntries().size()));
                    for (Map.Entry<String, Codes.Code> entry : ((Codes.Instantiate) code).getEntries().entrySet()) {
                        stream.write(new byte[]{0x03});
                        byte[] keyBytes = entry.getKey().getBytes();
                        stream.write(convertIntegerToBytes(keyBytes.length + 1));
                        stream.write(keyBytes);
                        stream.write('\0');
                        byte[] valueBytes = convertExpressionToBytes(entry.getValue());
                        stream.write(convertIntegerToBytes(valueBytes.length));
                        stream.write(valueBytes);
                    }
                } else if (code instanceof Codes.Class) {
                    stream.write(new byte[]{0x58});
                    stream.write(new byte[]{0x01});
                    byte[] nameBytes = ((Codes.Class) code).getName().getBytes();
                    stream.write(convertIntegerToBytes(nameBytes.length + 1));
                    stream.write(nameBytes);
                    stream.write('\0');
                    stream.write(new byte[]{0x02});
                    stream.write(convertIntegerToBytes(((Codes.Class) code).getInheritance().size()));
                    for (Codes.Identifier entry : ((Codes.Class) code).getInheritance()) {
                        byte[] keyBytes = entry.getName().getBytes();
                        stream.write(convertIntegerToBytes(keyBytes.length + 1));
                        stream.write(keyBytes);
                        stream.write('\0');
                    }
                    stream.write(new byte[]{0x03});
                    stream.write(convertIntegerToBytes(((Codes.Class) code).getBehavior().size()));
                    for (Codes.Identifier entry : ((Codes.Class) code).getBehavior()) {
                        byte[] keyBytes = entry.getName().getBytes();
                        stream.write(convertIntegerToBytes(keyBytes.length + 1));
                        stream.write(keyBytes);
                        stream.write('\0');
                    }
                    stream.write(new byte[]{0x04});
                    stream.write(convertIntegerToBytes(((Codes.Class) code).getProperties().size()));
                    for (Codes.Prop prop : ((Codes.Class) code).getProperties()) {
                        byte[] propName = prop.getId().getName().getBytes();
                        stream.write(convertIntegerToBytes(propName.length + 1));
                        stream.write(propName);
                        stream.write('\0');
                        byte[] propValue = convertExpressionToBytes(prop.getValue());
                        stream.write(convertIntegerToBytes(propValue.length));
                        stream.write(propValue);
                    }
                    stream.write(new byte[]{0x05});
                    stream.write(convertIntegerToBytes(((Codes.Class) code).getFunctions().size()));
                    for (Codes.Function func : ((Codes.Class) code).getFunctions()) {
                        stream.write(new byte[]{0x51});
                        stream.write(new byte[]{0x01});
                        byte[] name = func.getName().getBytes();
                        stream.write(convertIntegerToBytes(name.length + 1));
                        stream.write(name);
                        stream.write('\0');
                        stream.write(new byte[]{0x02});
                        byte[] level = func.getLevel().toString().getBytes();
                        stream.write(convertIntegerToBytes(level.length + 1));
                        stream.write(level);
                        stream.write('\0');
                        stream.write(new byte[]{0x03});
                        stream.write(convertIntegerToBytes(func.getParams().size()));
                        for (Codes.Identifier id : func.getParams()) {
                            byte[] idName = id.getName().getBytes();
                            stream.write(convertIntegerToBytes(idName.length));
                            stream.write(idName);
                        }
                        stream.write(new byte[]{0x04});
                        stream.write(new byte[]{0x6f});
                        byte[] cs = convertCodeToBytes(func.getCodes());
                        stream.write(convertIntegerToBytes(cs.length));
                        stream.write(cs);
                        stream.write(new byte[]{0x6e});
                    }
                    stream.write(new byte[]{0x05});
                    Codes.Constructor constructor = ((Codes.Class) code).getConstructor();
                    stream.write(convertIntegerToBytes(constructor.getParams().size()));
                    for (Codes.Identifier id : constructor.getParams()) {
                        byte[] idNameBytes = id.getName().getBytes();
                        stream.write(convertIntegerToBytes(idNameBytes.length + 1));
                        stream.write(idNameBytes);
                        stream.write('\0');
                    }
                    stream.write(new byte[]{0x06});
                    stream.write(new byte[]{0x6f});
                    byte[] cs = convertCodeToBytes(constructor.getBody());
                    stream.write(convertIntegerToBytes(cs.length));
                    stream.write(cs);
                    stream.write(new byte[]{0x6e});
                } else if (code instanceof Codes.Return) {
                    stream.write(new byte[]{0x59});
                    stream.write(new byte[]{0x01});
                    stream.write(convertExpressionToBytes(((Codes.Return) code).getValue()));
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
                stream.write(convertIntegerToBytes(((Codes.Call) exp).getEntries().size()));
                for (Map.Entry<String, Codes.Code> entry : ((Codes.Call) exp).getEntries().entrySet()) {
                    stream.write(new byte[]{0x03});
                    byte[] keyBytes = entry.getKey().getBytes();
                    stream.write(convertIntegerToBytes(keyBytes.length + 1));
                    stream.write(keyBytes);
                    stream.write('\0');
                    byte[] valueBytes = convertExpressionToBytes(entry.getValue());
                    stream.write(convertIntegerToBytes(valueBytes.length));
                    stream.write(valueBytes);
                }
            } else if (exp instanceof Codes.Instantiate) {
                stream.write(new byte[]{0x57});
                stream.write(new byte[]{0x01});
                stream.write(convertExpressionToBytes(((Codes.Instantiate) exp).getClassReference()));
                stream.write(new byte[]{0x02});
                stream.write(convertIntegerToBytes(((Codes.Instantiate) exp).getEntries().size()));
                for (Map.Entry<String, Codes.Code> entry : ((Codes.Instantiate) exp).getEntries().entrySet()) {
                    stream.write(new byte[]{0x03});
                    byte[] keyBytes = entry.getKey().getBytes();
                    stream.write(convertIntegerToBytes(keyBytes.length + 1));
                    stream.write(keyBytes);
                    stream.write('\0');
                    byte[] valueBytes = convertExpressionToBytes(entry.getValue());
                    stream.write(convertIntegerToBytes(valueBytes.length));
                    stream.write(valueBytes);
                }
            } else if (exp instanceof Codes.Identifier) {
                stream.write(new byte[]{0x61});
                byte[] idName = ((Codes.Identifier) exp).getName().getBytes();
                byte[] idNameLengthArr = convertIntegerToBytes(idName.length + 1);
                stream.write(idNameLengthArr);
                stream.write(idName);
                stream.write('\0');
                return stream.toByteArray();
            } else if (exp instanceof Codes.Reference) {
                stream.write(new byte[]{0x7f});
                stream.write(convertExpressionToBytes(((Codes.Reference) exp).getCurrentChain()));
                if (((Codes.Reference) exp).getRestOfChains() != null) {
                    byte[] cs = convertExpressionToBytes(((Codes.Reference) exp).getRestOfChains());
                    stream.write(convertIntegerToBytes(cs.length));
                    stream.write(cs);
                }
                return stream.toByteArray();
            } else if (exp instanceof Codes.Index) {
                stream.write(new byte[]{0x6d});
                byte[] cs = convertExpressionToBytes(((Codes.Index) exp).getVar());
                stream.write(convertIntegerToBytes(cs.length));
                stream.write(cs);
                cs = convertExpressionToBytes(((Codes.Index) exp).getIndex());
                stream.write(convertIntegerToBytes(cs.length));
                stream.write(cs);
                cs = convertExpressionToBytes(((Codes.Index) exp).getRestOfChains());
                stream.write(convertIntegerToBytes(cs.length));
                stream.write(cs);
                return stream.toByteArray();
            }
            else if (exp instanceof Codes.Value) {
                if (((Codes.Value) exp).getValue() instanceof String) {
                    stream.write(new byte[]{0x62});
                    byte[] value = ((String) ((Codes.Value) exp).getValue()).getBytes();
                    stream.write(convertIntegerToBytes(value.length + 1));
                    stream.write(value);
                    stream.write('\0');
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
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putDouble(number);
        return bb.array();
    }

    private byte[] convertFloatToBytes(float number) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putFloat(number);
        return bb.array();
    }
    
    private byte[] convertShortToBytes(short number) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.putShort(number);
        return bb.array();
    }
    
    private byte[] convertIntegerToBytes(int number) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(number);
        return bb.array();
    }

    private byte[] convertLongToBytes(long number) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(number);
        return bb.array();
    }

    private byte[] convertBooleanToBytes(boolean number) {
        return new byte[]{(byte) (number?1:0)};
    }

    public TraParser(TraLexer lexer) {

        this.lexer = lexer;

        Node rootNode = new Node("rootNode");
        mainNode.next(Collections.singletonList(rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        List<Codes.Code> codes = (List<Codes.Code>) prevResults.get(0).second;
                        String uglyJSONString = JsonHelper.toJson(codes);
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        JsonParser jp = new JsonParser();
                        JsonElement je = jp.parse(uglyJSONString);
                        System.out.println(gson.toJson(je));
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
        expNode.next(Collections.singletonList(sym.terminalNames[sym.TRUE]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Value value = new Codes.Value();
                        value.setValue(true);
                        return value;
                    }
                });
        expNode.next(Collections.singletonList(sym.terminalNames[sym.FALSE]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Value value = new Codes.Value();
                        value.setValue(false);
                        return value;
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
        Node inputsNode = new Node("inputsNode");
        expNode.next(Arrays.asList(sym.terminalNames[sym.LBRACKET], inputsNode, sym.terminalNames[sym.RBRACKET]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.AnonymousObject ao = new Codes.AnonymousObject();
                        ao.setContent((Hashtable<String, Codes.Code>) prevResults.get(0).second);
                        return ao;
                    }
                });
        Node refExtraNode = new Node("refExtraNode");
        expNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], refExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        if (prevResults.get(1).second == null || prevResults.get(1).second instanceof Codes.Reference) {
                            Codes.Reference ref = new Codes.Reference();
                            ref.setCurrentChain((Codes.Identifier) prevResults.get(0).second);
                            ref.setRestOfChains((Codes.Code) prevResults.get(1).second);
                            return ref;
                        } else {
                            Codes.Index index = ((Pair<Codes.Index, Codes.Reference>)prevResults.get(1).second).first;
                            index.setVar((Codes.Identifier) prevResults.get(0).second);
                            index.setRestOfChains(((Pair<Codes.Index, Codes.Reference>)prevResults.get(1).second).second);
                            return index;
                        }
                    }
                });
        Node expChainNode = new Node("expChainNode");
        refExtraNode.next(Arrays.asList(sym.terminalNames[sym.LBRACE], expChainNode, sym.terminalNames[sym.RBRACE], refExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Index index = new Codes.Index();
                        index.setIndex((Codes.Code)prevResults.get(0).second);
                        index.setRestOfChains((Codes.Code)prevResults.get(1).second);
                        return index;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.LPAREN], expNode, sym.terminalNames[sym.RPAREN]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.LBRACE], expChainNode, sym.terminalNames[sym.RBRACE]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Array array = new Codes.Array();
                        array.setItems((List<Codes.Code>)prevResults.get(0).second);
                        return array;
                    }
                });
        Node expChainExtraNode = new Node("expChainExtraNode");
        Node periodNode = new Node("periodNode");
        expChainNode.next(Arrays.asList(expNode, periodNode, expChainExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        List<Codes.Code> ids = (List<Codes.Code>) prevResults.get(2).second;
                        if (prevResults.get(1).second != null) {
                            Codes.Period period = new Codes.Period();
                            period.setStart((Codes.Code) prevResults.get(0).second);
                            period.setEnd((Codes.Code)prevResults.get(1).second);
                            ids.add(period);
                        } else {
                            Codes.Code exp = (Codes.Code) prevResults.get(0).second;
                            ids.add(exp);
                        }
                        return ids;
                    }
                });
        periodNode.next(Arrays.asList(sym.terminalNames[sym.COLON], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        periodNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return null;
                    }
                });
        expChainNode.next(Arrays.asList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<>();
                    }
                });
        expChainExtraNode.next(Arrays.asList(sym.terminalNames[sym.COMMA], expNode, expChainExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        List<Codes.Code> ids = (List<Codes.Code>) prevResults.get(1).second;
                        Codes.Code exp = (Codes.Code) prevResults.get(0).second;
                        ids.add(exp);
                        return ids;
                    }
                });
        expChainExtraNode.next(Arrays.asList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<>();
                    }
                });
        Node refNode = new Node("refNode");
        expNode.next(Arrays.asList(sym.terminalNames[sym.INSTANCE], sym.terminalNames[sym.OF],
                refNode, sym.terminalNames[sym.LPAREN], inputsNode, sym.terminalNames[sym.RPAREN]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> classRef = prevResults.get(0);
                        Pair<Symbol, Object> entries = prevResults.get(1);
                        Codes.Instantiate instantiate = new Codes.Instantiate();
                        instantiate.setClassReference((Codes.Reference)classRef.second);
                        instantiate.setEntries((Hashtable<String, Codes.Code>)entries.second);
                        return instantiate;
                    }
                });
        expNode.next(Arrays.asList(sym.terminalNames[sym.DO], refNode,
                sym.terminalNames[sym.LPAREN], inputsNode, sym.terminalNames[sym.RPAREN]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Call call = new Codes.Call();
                        call.setEntries((Hashtable<String, Codes.Code>) prevResults.get(1).second);
                        call.setFuncReference((Codes.Reference)prevResults.get(0).second);
                        return call;
                    }
                });
        Node secondParamNode = new Node("secondParamNode");
        secondParamNode.next(Collections.singletonList(sym.terminalNames[sym.TRUE]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Value value = new Codes.Value();
                        value.setValue(true);
                        return value;
                    }
                });
        secondParamNode.next(Collections.singletonList(sym.terminalNames[sym.FALSE]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Value value = new Codes.Value();
                        value.setValue(false);
                        return value;
                    }
                });
        secondParamNode.next(Collections.singletonList(sym.terminalNames[sym.NUMBER]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        secondParamNode.next(Collections.singletonList(sym.terminalNames[sym.STRING]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        secondParamNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], refExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        if (prevResults.get(1).second == null || prevResults.get(1).second instanceof Codes.Reference) {
                            Codes.Reference ref = new Codes.Reference();
                            ref.setCurrentChain((Codes.Identifier) prevResults.get(0).second);
                            ref.setRestOfChains((Codes.Code) prevResults.get(1).second);
                            return ref;
                        } else {
                            Codes.Index index = ((Pair<Codes.Index, Codes.Reference>)prevResults.get(1).second).first;
                            index.setVar((Codes.Identifier) prevResults.get(0).second);
                            index.setRestOfChains(((Pair<Codes.Index, Codes.Reference>)prevResults.get(1).second).second);
                            return index;
                        }
                    }
                });
        secondParamNode.next(Arrays.asList(sym.terminalNames[sym.LPAREN], expNode, sym.terminalNames[sym.RPAREN]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        secondParamNode.next(Arrays.asList(expNode, sym.terminalNames[sym.IS], sym.terminalNames[sym.NOT],
                sym.terminalNames[sym.SATISFIED]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpNot sum = new Codes.MathExpNot();
                        sum.setValue((Codes.Code)prevResults.get(0).second);
                        return sum;
                    }
                });
        secondParamNode.next(Arrays.asList(sym.terminalNames[sym.DO], refNode,
                sym.terminalNames[sym.LPAREN], inputsNode, sym.terminalNames[sym.RPAREN]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Call call = new Codes.Call();
                        call.setEntries((Hashtable<String, Codes.Code>) prevResults.get(1).second);
                        call.setFuncReference((Codes.Reference)prevResults.get(0).second);
                        return call;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.IS], sym.terminalNames[sym.NOT],
                sym.terminalNames[sym.SATISFIED]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpNot sum = new Codes.MathExpNot();
                        sum.setValue((Codes.Code)prevResults.get(0).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.SUM], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpSum sum = new Codes.MathExpSum();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.SUBTRACT], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpSubstract sum = new Codes.MathExpSubstract();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.MULTIPLY], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMultiply sum = new Codes.MathExpMultiply();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.DIVISION], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpDivide sum = new Codes.MathExpDivide();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.AND], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpAnd sum = new Codes.MathExpAnd();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.OR], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpOr sum = new Codes.MathExpOr();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.POWER], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpPower sum = new Codes.MathExpPower();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.MOD], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpMod sum = new Codes.MathExpMod();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.EQUAL], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpEqual sum = new Codes.MathExpEqual();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.LT], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpLT sum = new Codes.MathExpLT();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.LE], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpLE sum = new Codes.MathExpLE();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.GE], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpGE sum = new Codes.MathExpGE();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.GT], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpGT sum = new Codes.MathExpGT();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });
        expNode.next(Arrays.asList(expNode, sym.terminalNames[sym.NE], secondParamNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.MathExpNE sum = new Codes.MathExpNE();
                        sum.setValue1((Codes.Code)prevResults.get(0).second);
                        sum.setValue2((Codes.Code)prevResults.get(1).second);
                        return sum;
                    }
                });

        Node ifNode = new Node("ifNode");
        Node elseIfNode = new Node("elseIf");
        Node elseIfElseNode = new Node("elseIfElse");
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
        ifNode.next(Arrays.asList(expNode, sym.terminalNames[sym.THEN],
                sym.terminalNames[sym.START], rootNode, sym.terminalNames[sym.END], elseIfNode),
                new Action() {
                    @Override
                    public Codes.Code act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> condition = prevResults.get(0);
                        Pair<Symbol, Object> body = prevResults.get(1);
                        Pair<Symbol, Object> extras = prevResults.get(2);
                        Codes.If ifCode = new Codes.If();
                        ifCode.setCondition((Codes.Code)condition.second);
                        ifCode.setCodes((List<Codes.Code>)body.second);
                        ifCode.setExtras((List<Codes.Code>)extras.second);
                        return ifCode;
                    }
                });
        elseIfNode.next(Arrays.asList(sym.terminalNames[sym.ELSE], elseIfElseNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return prevResults.get(0).second;
                    }
                });
        elseIfElseNode.next(Arrays.asList(sym.terminalNames[sym.IF], expNode, sym.terminalNames[sym.THEN],
                sym.terminalNames[sym.START], rootNode, sym.terminalNames[sym.END], elseIfNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Code condition = (Codes.Code) prevResults.get(0).second;
                        List<Codes.Code> elseIfCodes = (List<Codes.Code>)prevResults.get(1).second;
                        List<Codes.Code> otherElses = (List<Codes.Code>)prevResults.get(2).second;
                        Codes.ElseIf elseIf = new Codes.ElseIf();
                        elseIf.setCondition(condition);
                        elseIf.setCodes(elseIfCodes);
                        otherElses.add(0, elseIf);
                        return otherElses;
                    }
                });
        elseIfElseNode.next(Arrays.asList(sym.terminalNames[sym.START],
                rootNode, sym.terminalNames[sym.END]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> body = prevResults.get(0);
                        Codes.Else elseCode = new Codes.Else();
                        elseCode.setCodes((List<Codes.Code>)body.second);
                        return new ArrayList<Codes.Code>(Collections.singletonList(elseCode));
                    }
                });
        elseIfNode.next(Collections.singletonList(epsilon),
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
        rememberNode.next(Arrays.asList(expNode, sym.terminalNames[sym.AS], refNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> exp = prevResults.get(0);
                        Pair<Symbol, Object> id = prevResults.get(1);
                        Codes.Assignment assignment = new Codes.Assignment();
                        assignment.setVar((Codes.Reference)id.second);
                        assignment.setValue((Codes.Code)exp.second);
                        return assignment;
                    }
                });
        Node functionNode = new Node("funcNode");
        Node funcLevelNode = new Node("funcLevelNode");
        rootNode.next(Arrays.asList(sym.terminalNames[sym.DEFINE], sym.terminalNames[sym.FUNCTION], funcLevelNode, functionNode, rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> funcLevel = prevResults.get(0);
                        Codes.Function newLine = (Codes.Function) prevResults.get(1).second;
                        List<Codes.Code> codes = (List<Codes.Code>) prevResults.get(2).second;
                        newLine.setLevel(((Codes.DataLevel)funcLevel.second));
                        codes.add(0, newLine);
                        return codes;
                    }
                });
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
        funcParams.next(Collections.singletonList(epsilon),
                new Action() {
            @Override
            public Object act(List<Pair<Symbol, Object>> prevResults) {
                return new ArrayList<Codes.Identifier>();
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
                funcParams, sym.terminalNames[sym.START], rootNode, sym.terminalNames[sym.END]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> funcName = prevResults.get(0);
                        Pair<Symbol, Object> funcParams = prevResults.get(1);
                        Pair<Symbol, Object> funcCodes = prevResults.get(2);
                        Codes.Function func = new Codes.Function();
                        func.setName(((Codes.Identifier)funcName.second).getName());
                        func.setParams((List<Codes.Identifier>)funcParams.second);
                        func.setCodes((List<Codes.Code>)funcCodes.second);
                        return func;
                    }
                });
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
        refNode.next(Arrays.asList(sym.terminalNames[sym.IDENTIFIER], refExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Reference ref = new Codes.Reference();
                        ref.setCurrentChain((Codes.Identifier) prevResults.get(0).second);
                        ref.setRestOfChains((Codes.Reference)prevResults.get(1).second);
                        return ref;
                    }
                });
        refExtraNode.next(Arrays.asList(sym.terminalNames[sym.DOT], sym.terminalNames[sym.IDENTIFIER], refExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Reference lastRef = prevResults.get(1).second != null ? ((Codes.Reference) prevResults.get(1).second) : null;
                        Codes.Identifier id = (Codes.Identifier) prevResults.get(0).second;
                        Codes.Reference newRef = new Codes.Reference();
                        newRef.setCurrentChain(id);
                        newRef.setRestOfChains(lastRef);
                        return newRef;
                    }
                });
        refExtraNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return null;
                    }
                });
        rootNode.next(Arrays.asList(sym.terminalNames[sym.DO], refNode,
                sym.terminalNames[sym.LPAREN], inputsNode, sym.terminalNames[sym.RPAREN], rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Call call = new Codes.Call();
                        call.setFuncReference((Codes.Reference)prevResults.get(0).second);
                        call.setEntries((Hashtable<String, Codes.Code>) prevResults.get(1).second);
                        List<Codes.Code> restOfCode = (List<Codes.Code>) prevResults.get(2).second;
                        restOfCode.add(0, call);
                        return restOfCode;
                    }
                });
        Node inheritanceNode = new Node("inheritanceNode");
        Node inheritanceExtraNode = new Node("inheritanceExtraNode");
        Node behaviorNode = new Node("behaviorNode");
        Node behaviorExtraNode = new Node("behaviorExtraNode");
        Node classContentNode = new Node("classContentNode");
        rootNode.next(Arrays.asList(sym.terminalNames[sym.DEFINE], sym.terminalNames[sym.CLASS], sym.terminalNames[sym.IDENTIFIER],
                inheritanceNode, behaviorNode, sym.terminalNames[sym.START], classContentNode, sym.terminalNames[sym.END], rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Identifier classId = (Codes.Identifier) prevResults.get(0).second;
                        List<Codes.Identifier> inheritanceIds = (List<Codes.Identifier>) prevResults.get(1).second;
                        List<Codes.Identifier> behaviorIds = (List<Codes.Identifier>) prevResults.get(2).second;
                        Triple<List<Codes.Prop>, List<Codes.Function>, Codes.Constructor> body
                                = (Triple<List<Codes.Prop>, List<Codes.Function>, Codes.Constructor>)
                                prevResults.get(3).second;
                        List<Codes.Code> restOfTheCode = (List<Codes.Code>) prevResults.get(4).second;
                        Codes.Class classObj = new Codes.Class();
                        classObj.setName(classId.getName());
                        classObj.setInheritance(inheritanceIds);
                        classObj.setBehavior(behaviorIds);
                        classObj.setProperties(body.a);
                        classObj.setFunctions(body.b);
                        classObj.setConstructor(body.c);
                        restOfTheCode.add(0, classObj);
                        return restOfTheCode;
                    }
                });
        classContentNode.next(Collections.singletonList(sym.terminalNames[sym.EMPTY]),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new Pair<List<Codes.Identifier>, List<Codes.Function>>(
                                new ArrayList<Codes.Identifier>(), new ArrayList<Codes.Function>());
                    }
                });
        Node classContentExtraNode = new Node("classContentExtraNode");
        classContentNode.next(Arrays.asList(sym.terminalNames[sym.ON], sym.terminalNames[sym.CREATED], funcParams,
                sym.terminalNames[sym.START], rootNode, sym.terminalNames[sym.END], classContentExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        List<Codes.Code> body = (List<Codes.Code>) prevResults.get(1).second;
                        List<Codes.Identifier> ids = (List<Codes.Identifier>) prevResults.get(0).second;
                        Pair<List<Codes.Prop>, List<Codes.Function>> content
                                = (Pair<List<Codes.Prop>, List<Codes.Function>>) prevResults.get(2).second;
                        Codes.Constructor constructor = new Codes.Constructor();
                        constructor.setParams(ids);
                        constructor.setBody(body);
                        return new Triple<>(
                                content.first,
                                content.second,
                                constructor
                        );
                    }
                });
        classContentNode.next(Arrays.asList(sym.terminalNames[sym.DEFINE], sym.terminalNames[sym.PROP], sym.terminalNames[sym.IDENTIFIER],
                sym.terminalNames[sym.WITH], sym.terminalNames[sym.VALUE], sym.terminalNames[sym.COLON], expNode, classContentExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> propName = prevResults.get(0);
                        Pair<Symbol, Object> propValue = prevResults.get(1);
                        Codes.Prop prop = new Codes.Prop();
                        prop.setId((Codes.Identifier)propName.second);
                        prop.setValue((Codes.Code)propValue.second);
                        prop.setLevel(Codes.DataLevel.InstanceLevel);
                        Pair<List<Codes.Prop>, List<Codes.Function>> content
                                = (Pair<List<Codes.Prop>, List<Codes.Function>>) prevResults.get(2).second;
                        content.first.add(0, prop);
                        return content;
                    }
                });
        classContentNode.next(Arrays.asList(sym.terminalNames[sym.DEFINE], sym.terminalNames[sym.FUNCTION], funcLevelNode, functionNode,
                classContentExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> funcLevel = prevResults.get(0);
                        Codes.Function newLine = (Codes.Function) prevResults.get(1).second;
                        Pair<List<Codes.Prop>, List<Codes.Function>> content
                                = (Pair<List<Codes.Prop>, List<Codes.Function>>) prevResults.get(2).second;
                        newLine.setLevel(((Codes.DataLevel)funcLevel.second));
                        content.second.add(0, newLine);
                        return content;
                    }
                });
        classContentExtraNode.next(Arrays.asList(sym.terminalNames[sym.DEFINE], sym.terminalNames[sym.PROP], sym.terminalNames[sym.IDENTIFIER],
                sym.terminalNames[sym.WITH], sym.terminalNames[sym.VALUE], sym.terminalNames[sym.COLON], expNode, classContentExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> propName = prevResults.get(0);
                        Pair<Symbol, Object> propValue = prevResults.get(1);
                        Codes.Prop prop = new Codes.Prop();
                        prop.setId((Codes.Identifier)propName.second);
                        prop.setValue((Codes.Code)propValue.second);
                        prop.setLevel(Codes.DataLevel.InstanceLevel);
                        Pair<List<Codes.Prop>, List<Codes.Function>> content
                                = (Pair<List<Codes.Prop>, List<Codes.Function>>) prevResults.get(2).second;
                        content.first.add(0, prop);
                        return content;
                    }
                });
        classContentExtraNode.next(Arrays.asList(sym.terminalNames[sym.DEFINE], sym.terminalNames[sym.FUNCTION], funcLevelNode, functionNode,
                classContentExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> funcLevel = prevResults.get(0);
                        Codes.Function newLine = (Codes.Function) prevResults.get(1).second;
                        Pair<List<Codes.Prop>, List<Codes.Function>> content
                                = (Pair<List<Codes.Prop>, List<Codes.Function>>) prevResults.get(2).second;
                        newLine.setLevel(((Codes.DataLevel)funcLevel.second));
                        content.second.add(0, newLine);
                        return content;
                    }
                });
        classContentExtraNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new Pair<List<Codes.Identifier>, List<Codes.Function>>(
                                new ArrayList<Codes.Identifier>(), new ArrayList<Codes.Function>());
                    }
                });
        inheritanceNode.next(Arrays.asList(sym.terminalNames[sym.BASED], sym.terminalNames[sym.ON], sym.terminalNames[sym.IDENTIFIER],
                inheritanceExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Identifier id = (Codes.Identifier) prevResults.get(0).second;
                        List<Codes.Identifier> restOfTheIds = (List<Codes.Identifier>) prevResults.get(1).second;
                        restOfTheIds.add(id);
                        return restOfTheIds;
                    }
                });
        inheritanceExtraNode.next(Arrays.asList(sym.terminalNames[sym.COMMA], sym.terminalNames[sym.IDENTIFIER], inheritanceExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Identifier id = (Codes.Identifier) prevResults.get(0).second;
                        List<Codes.Identifier> restOfTheIds = (List<Codes.Identifier>) prevResults.get(1).second;
                        restOfTheIds.add(id);
                        return restOfTheIds;
                    }
                });
        inheritanceExtraNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<Codes.Identifier>();
                    }
                });
        inheritanceNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<Codes.Identifier>();
                    }
                });
        behaviorNode.next(Arrays.asList(sym.terminalNames[sym.BEHAVES], sym.terminalNames[sym.LIKE], sym.terminalNames[sym.IDENTIFIER],
                behaviorExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Identifier id = (Codes.Identifier) prevResults.get(0).second;
                        List<Codes.Identifier> restOfTheIds = (List<Codes.Identifier>) prevResults.get(1).second;
                        restOfTheIds.add(id);
                        return restOfTheIds;
                    }
                });
        behaviorExtraNode.next(Arrays.asList(sym.terminalNames[sym.COMMA], sym.terminalNames[sym.IDENTIFIER], behaviorExtraNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Identifier id = (Codes.Identifier) prevResults.get(0).second;
                        List<Codes.Identifier> restOfTheIds = (List<Codes.Identifier>) prevResults.get(1).second;
                        restOfTheIds.add(id);
                        return restOfTheIds;
                    }
                });
        behaviorExtraNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<Codes.Identifier>();
                    }
                });
        behaviorNode.next(Collections.singletonList(epsilon),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        return new ArrayList<Codes.Identifier>();
                    }
                });
        rootNode.next(Arrays.asList(sym.terminalNames[sym.CREATE], sym.terminalNames[sym.INSTANCE], sym.terminalNames[sym.OF],
                refNode, sym.terminalNames[sym.LPAREN], inputsNode, sym.terminalNames[sym.RPAREN], rootNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Pair<Symbol, Object> classRef = prevResults.get(0);
                        Pair<Symbol, Object> entries = prevResults.get(1);
                        List<Codes.Code> restOfCode = (List<Codes.Code>)prevResults.get(2).second;
                        Codes.Instantiate instantiate = new Codes.Instantiate();
                        instantiate.setClassReference((Codes.Reference)classRef.second);
                        instantiate.setEntries((Hashtable<String, Codes.Code>)entries.second);
                        restOfCode.add(0, instantiate);
                        return restOfCode;
                    }
                });
        rootNode.next(Arrays.asList(sym.terminalNames[sym.RETURN], expNode),
                new Action() {
                    @Override
                    public Object act(List<Pair<Symbol, Object>> prevResults) {
                        Codes.Return returnObj = new Codes.Return();
                        returnObj.setValue((Codes.Code)prevResults.get(0).second);
                        List<Codes.Code> list = new ArrayList<>();
                        list.add(returnObj);
                        return list;
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
            tokens.add(new Symbol(sym.EOF, 0, 0, null));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return this.theThirdParse(tokens.toArray(new Symbol[0]));
    }

    private boolean investigateLoop(HashSet<String> blacklist, String firstToken, Node node) {
        for (Pair<List<Object>, Action> subNodesAction : node.subNodes) {
            Object obj = subNodesAction.first.get(0);
            if (obj instanceof Node) {
                if (blacklist.contains(((Node) obj).name)) continue;
                else blacklist.add(((Node) obj).name);
                if (investigateLoop(blacklist, firstToken, (Node) obj)) {
                    return true;
                }
            } else if (obj instanceof String) {
                if (firstToken.equals(obj)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void printStack(NodePointer currentPointer) {
        NodePointer finalPointer = currentPointer;
        int rule = 0;
        while (finalPointer != null) {
            System.out.println(finalPointer.inputs.size() + " " + finalPointer.id + " " + finalPointer.nodeName() +
                    " - " + finalPointer.rulePointer + " - " + finalPointer.foundMatch);
            rule = finalPointer.backedByPos;
            finalPointer = finalPointer.backedBy;
            if (finalPointer != null)
                finalPointer.rulePointer = rule;
        }
    }

    public Object theThirdParse(Symbol[] tokens) {
        HashSet<Integer> inCorrectPoints = new HashSet<>();
        NodePointer currentPointer = new NodePointer(UUID.randomUUID().toString(), mainNode, 0, 0,
                0, 0, null, null);
        int counter = 0;
        boolean done = false;
        while (counter < 1000) {
            System.out.println("inputs dictionary size : " + currentPointer.inputs.size() + " - " + currentPointer.nodeName() + " - " + currentPointer.rulePointer + " - " + currentPointer.ruleTokenPointer + " - " + currentPointer.tokenPointer);
            printStack(currentPointer);
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(currentPointer.inputs));
            if (done) {
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(currentPointer.inputs));
                HashSet<String> seen = new HashSet<>();
                while (true) {
                    currentPointer.forwardToken();
                    if (currentPointer.ruleTokenPointer < currentPointer.currentRuleSize() &&
                            currentPointer.currentRuleToken() instanceof Node) {
                        System.out.println(((Node) currentPointer.currentRuleToken()).name + "moving on epsilon...");
                        for (int i = 0; i < ((Node) currentPointer.currentRuleToken()).subNodes.size(); i++) {
                            if (((Node) currentPointer.currentRuleToken()).subNodes.get(i).first.get(0) instanceof Node &&
                                    ((Node) ((Node) currentPointer.currentRuleToken()).subNodes.get(i).first.get(0)).name.equals("epsilon")) {
                                System.out.println("resolved epsilon.");
                                Object result = ((Node) currentPointer.currentRuleToken()).subNodes.get(i).second.act(new ArrayList<>());
                                currentPointer.inputs.add(new Pair<>(null, result));
                                currentPointer.forwardToken();
                                break;
                            }
                        }
                    }
                    System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(currentPointer.inputs));
                    Object result = currentPointer.currentAction().act(currentPointer.inputs);
                    NodePointer temp = currentPointer;
                    seen.add(currentPointer.id);
                    printStack(currentPointer);
                    currentPointer = currentPointer.backedBy;
                    while (currentPointer != null && (seen.contains(currentPointer.id) || currentPointer.foundMatch)) {
                        temp = currentPointer;
                        seen.add(currentPointer.id);
                        currentPointer = currentPointer.backedBy;
                        currentPointer.rulePointer = temp.backedByPos;
                        System.out.println(temp.nodeName());
                    }
                    if (currentPointer == null) {
                        temp.inputs = Collections.singletonList(new Pair<>(null, result));
                        return temp.currentAction().act(temp.inputs);
                    } else if (currentPointer.backedBy == null) {
                        currentPointer.inputs.add(new Pair<>(null, result));
                        return currentPointer.currentAction().act(currentPointer.inputs);
                    } else {
                        currentPointer.inputs.add(new Pair<>(null, result));
                    }
                }
            }
            Object ruleToken = currentPointer.currentRuleToken();
            if (ruleToken instanceof Node) {
                System.out.println(((Node) ruleToken).name);
                if (((Node) ruleToken).name.equals("epsilon")) {
                    System.out.println("removing epsilon...");
                    NodePointer temp = currentPointer.backedBy;
                    HashSet<String> seen = new HashSet<>();
                    seen.add(temp.id);
                    temp = temp.backedBy;
                    NodePointer saved;
                    while (seen.contains(temp.id) || temp.reachedCurrentRuleEnd()) {
                        saved = temp;
                        temp = temp.backedBy;
                        temp.inputs.add(new Pair<>(null, saved.currentAction().act(saved.inputs)));
                    }
                    NodePointer next;
                    if (temp.currentRuleToken() instanceof Node) {
                        next = new NodePointer(temp.id, temp.node,
                                temp.tokenPointer, temp.rulePointer,
                                temp.ruleTokenPointer + 1,
                                temp.startPoint, temp,
                                temp);
                    }
                    else {
                        next = new NodePointer(temp.id, temp.node,
                                temp.tokenPointer, temp.rulePointer,
                                temp.ruleTokenPointer, temp.startPoint, temp, temp);
                    }
                    next.inputs = new ArrayList<>(temp.inputs);
                    currentPointer = next;
                }
                else {
                    HashSet<String> blacklist = new HashSet<>();
                    blacklist.add(currentPointer.nodeName());
                    if (investigateLoop(blacklist, sym.terminalNames[tokens[currentPointer.tokenPointer].sym], (Node) ruleToken)) {
                        System.out.println("opening node " + ((Node) ruleToken).name);
                        NodePointer current = currentPointer;
                        currentPointer = new NodePointer(UUID.randomUUID().toString(), (Node)ruleToken,
                                current.tokenPointer, 0, 0, current.tokenPointer,
                                current, current);
                    } else {
                        boolean foundEps = false;
                        for (Pair<List<Object>, Action> subNodesAction : ((Node)ruleToken).subNodes) {
                            Object obj = subNodesAction.first.get(0);
                            if (obj instanceof Node && ((Node) obj).name.equals("epsilon")) {
                                foundEps = true;
                                break;
                            }
                        }
                        if (!foundEps) {
                            System.out.println("no legal way. getting back ....");
                            String id = currentPointer.id;
                            System.out.println(id);
                            NodePointer pointer = currentPointer;
                            while (pointer.id.equals(id)) {
                                pointer = pointer.prev;
                            }
                            pointer.ruleTokenPointer = 0;
                            pointer.rulePointer++;
                            while (true) {
                                if (pointer.reachedAllRulesEnd()) {
                                    System.out.println("popping due to node rules storage end...");
                                    pointer = pointer.prev;
                                    if (pointer.reachedCurrentRuleEnd()) {
                                        System.out.println("going to next rule in node...");
                                        pointer.forwardRule();
                                    }
                                } else {
                                    break;
                                }
                            }
                            currentPointer = pointer;
                        }
                        else {
                            System.out.println("opening node targeting epsilon... " + ((Node) ruleToken).name);
                            for (int i = 0; i < ((Node) ruleToken).subNodes.size(); i++) {
                                if (((Node) ruleToken).subNodes.get(i).first.get(0) instanceof Node &&
                                        ((Node) ((Node) ruleToken).subNodes.get(i).first.get(0)).name.equals("epsilon")) {
                                    System.out.println("resolved epsilon node action.");
                                    currentPointer.inputs.add(new Pair<>(null, ((Node) ruleToken).subNodes.get(i).second.act(new ArrayList<>())));
                                    break;
                                }
                            }
                            NodePointer pointer = currentPointer;
                            NodePointer temp = pointer;
                            HashSet<String> seen = new HashSet<>();
                            Object res = null;
                            boolean found = false;
                            int tokenPointer = pointer.tokenPointer;
                            while (true) {
                                printStack(pointer);
                                System.out.println("executing finding match after epsilon resolving... , " + sym.terminalNames[tokens[tokenPointer].sym]);
                                int ruleIndex = 0;
                                for (Pair<List<Object>, Action> subNodes : pointer.node.subNodes) {
                                    if (subNodes.first.get(0) instanceof Node &&
                                            ((Node) subNodes.first.get(0)).name.equals(pointer.nodeName()) &&
                                            subNodes.first.get(1) instanceof String &&
                                            subNodes.first.get(1).equals(sym.terminalNames[tokens[tokenPointer].sym])) {
                                        System.out.println("found match for expansion.");
                                        found = true;
                                        pointer.forwardToken();
                                        NodePointer pointerP = new NodePointer(pointer.id,
                                                pointer.node, tokenPointer,
                                                ruleIndex, 1, pointer.tokenPointer +
                                                (tokens[pointer.tokenPointer + 1].sym == sym.EOF ? 0 : 1),
                                                pointer, pointer);
                                        if (res != null) {
                                            pointer.inputs.add(new Pair<>(null, res));
                                            res = null;
                                        }
                                        pointerP.inputs.add(new Pair<>(null, pointer.currentAction().act(pointer.inputs)));
                                        pointer.foundMatch = true;
                                        pointer = pointerP;
                                        currentPointer = pointer;
                                        if (tokens[pointer.tokenPointer].sym == sym.EOF) {
                                            System.out.println("finished compile.");
                                            done = true;
                                            continue;
                                        }
                                        break;
                                    }
                                    ruleIndex++;
                                }
                                printStack(pointer);
                                if (found) break;
                                if (seen.contains(pointer.id)) {
                                    System.out.println("skipping...");
                                    temp = pointer;
                                    pointer = pointer.backedBy;
                                    System.out.println(pointer.nodeName() + " " + pointer.rulePointer);
                                    pointer.rulePointer = temp.backedByPos;
                                    if (pointer.id.equals(temp.id))
                                        pointer.inputs = new ArrayList<>(temp.inputs);
                                    else {
                                        pointer.inputs.add(new Pair<>(null, res));
                                        res = null;
                                    }
                                }
                                else if (pointer.reachedCurrentRuleEnd()) {
                                    System.out.println("resolving...");
                                    seen.add(pointer.id);
                                    temp = pointer;
                                    if (res != null)
                                        pointer.inputs.add(new Pair<>(null, res));
                                    res = pointer.currentAction().act(pointer.inputs);
                                    pointer = pointer.backedBy;
                                    pointer.rulePointer = temp.backedByPos;
                                }
                                else {
                                    if (res != null)
                                        pointer.inputs.add(new Pair<>(null, res));
                                    break;
                                }
                            }
                            if (!found) {
                                NodePointer next;
                                if (pointer.currentRuleToken() instanceof Node) {
                                    next = new NodePointer(pointer.id, pointer.node,
                                            currentPointer.tokenPointer, pointer.rulePointer,
                                            pointer.ruleTokenPointer + 1,
                                            pointer.startPoint, pointer,
                                            currentPointer);
                                } else {
                                    next = new NodePointer(pointer.id, pointer.node,
                                            currentPointer.tokenPointer, pointer.rulePointer,
                                            pointer.ruleTokenPointer, pointer.startPoint, pointer, currentPointer);
                                }
                                next.inputs = new ArrayList<>(pointer.inputs);
                                currentPointer = next;
                            }
                        }
                    }
                }
            }
            else if (ruleToken instanceof String) {
                Symbol token = tokens[currentPointer.tokenPointer];
                System.out.println(sym.terminalNames[token.sym] + " " + " line : " + token.left + " , word : " + token.right);
                System.out.println("comparing " + sym.terminalNames[token.sym] + " with " + ruleToken);
                if (token.sym == sym.EOF) {
                    System.out.println("finished compiling.");
                    done = true;
                    continue;
                }
                if (ruleToken.equals(sym.terminalNames[token.sym])) {
                    if (currentPointer.readyToMatchRule()) {
                        System.out.println("matched rule. trying to opening way...");
                        System.out.println("executing found match...");
                        int ruleIndex = 0;
                        boolean found = false;
                        for (Pair<List<Object>, Action> subNodes : currentPointer.node.subNodes) {
                            if (subNodes.first.get(0) instanceof Node &&
                                    ((Node) subNodes.first.get(0)).name.equals(currentPointer.nodeName()) &&
                                    subNodes.first.get(1) instanceof String &&
                                    subNodes.first.get(1).equals(sym.terminalNames[tokens[currentPointer.tokenPointer + 1].sym])) {
                                found = true;
                                if (currentPointer.currentRuleToken() instanceof String) {
                                    System.out.println("found value.");
                                    if (token.sym == sym.NUMBER) {
                                        System.out.println("found number.");
                                        Codes.Value value = new Codes.Value();
                                        value.setValue(token.value);
                                        currentPointer.inputs.add(new Pair<>(null, value));
                                    } else if (token.sym == sym.STRING) {
                                        System.out.println("found string.");
                                        Codes.Value value = new Codes.Value();
                                        value.setValue(token.value);
                                        currentPointer.inputs.add(new Pair<>(null, value));
                                    } else if (token.sym == sym.IDENTIFIER) {
                                        System.out.println("found id.");
                                        Codes.Identifier identifier = new Codes.Identifier();
                                        identifier.setName((String) token.value);
                                        currentPointer.inputs.add(new Pair<>(null, identifier));
                                    }
                                }
                                currentPointer.forwardToken();
                                NodePointer tempP = new NodePointer(currentPointer.id,
                                        currentPointer.node, currentPointer.tokenPointer + 1,
                                        ruleIndex, 1, currentPointer.tokenPointer +
                                        (tokens[currentPointer.tokenPointer + 1].sym == sym.EOF ? 0 : 1),
                                        currentPointer, currentPointer);
                                tempP.inputs.add(new Pair<>(null, currentPointer.currentAction().act(currentPointer.inputs)));
                                currentPointer.foundMatch = true;
                                currentPointer = tempP;
                                if (tokens[currentPointer.tokenPointer].sym == sym.EOF) {
                                    System.out.println("finished compile.");
                                    done = true;
                                    continue;
                                }
                                break;
                            }
                            ruleIndex++;
                        }
                        if (!found) {
                            System.out.println("finding match failed.");
                            System.out.println(currentPointer.prev.nodeName());
                            Object result = null;
                            if (currentPointer.currentRuleToken() instanceof String) {
                                System.out.println("found value.");
                                if (token.sym == sym.NUMBER) {
                                    System.out.println("found number.");
                                    Codes.Value value = new Codes.Value();
                                    value.setValue(token.value);
                                    currentPointer.inputs.add(new Pair<>(null, value));
                                } else if (token.sym == sym.STRING) {
                                    System.out.println("found string.");
                                    Codes.Value value = new Codes.Value();
                                    value.setValue(token.value);
                                    currentPointer.inputs.add(new Pair<>(null, value));
                                } else if (token.sym == sym.IDENTIFIER) {
                                    System.out.println("found id.");
                                    Codes.Identifier identifier = new Codes.Identifier();
                                    identifier.setName((String) token.value);
                                    currentPointer.inputs.add(new Pair<>(null, identifier));
                                }
                            }
                            currentPointer.forwardToken();
                            NodePointer pointer = currentPointer;
                            HashSet<String> seen = new HashSet<>();
                            NodePointer temp = pointer;
                            Object res = null;
                            found = false;
                            int tokenPointer = pointer.tokenPointer + 1;
                            while (true) {
                                System.out.println("hello " + pointer.nodeName() + " " + pointer.rulePointer);
                                System.out.println("hi " + new GsonBuilder().setPrettyPrinting().create().toJson(pointer.inputs));
                                System.out.println("executing finding match after epsilon resolving... , " + pointer.nodeName() + " " + sym.terminalNames[tokens[tokenPointer].sym]);
                                ruleIndex = 0;
                                for (Pair<List<Object>, Action> subNodes : pointer.node.subNodes) {
                                    if (subNodes.first.get(0) instanceof Node &&
                                            ((Node) subNodes.first.get(0)).name.equals(pointer.nodeName()) &&
                                            subNodes.first.get(1) instanceof String &&
                                            subNodes.first.get(1).equals(sym.terminalNames[tokens[tokenPointer].sym])) {
                                        System.out.println("found match for expansion.");
                                        found = true;
                                        pointer.forwardToken();
                                        NodePointer pointerP = new NodePointer(pointer.id,
                                                pointer.node, tokenPointer,
                                                ruleIndex, 1, pointer.tokenPointer +
                                                (tokens[pointer.tokenPointer + 1].sym == sym.EOF ? 0 : 1),
                                                pointer, pointer);
                                        if (res != null) {
                                            pointer.inputs.add(new Pair<>(null, res));
                                            res = null;
                                        }
                                        pointerP.inputs.add(new Pair<>(null, pointer.currentAction().act(pointer.inputs)));
                                        pointer.foundMatch = true;
                                        pointer = pointerP;
                                        currentPointer = pointer;
                                        if (tokens[pointer.tokenPointer].sym == sym.EOF) {
                                            System.out.println("finished compile.");
                                            done = true;
                                            continue;
                                        }
                                        break;
                                    }
                                    ruleIndex++;
                                }
                                if (found) break;
                                if (seen.contains(pointer.id) || pointer.foundMatch) {
                                    System.out.println("skipping...");
                                    seen.add(pointer.id);
                                    temp = pointer;
                                    pointer = pointer.backedBy;
                                    System.out.println(pointer.nodeName() + " " + pointer.rulePointer);
                                    pointer.rulePointer = temp.backedByPos;
                                    if (pointer.id.equals(temp.id) || pointer.foundMatch)
                                        pointer.inputs = new ArrayList<>(temp.inputs);
                                    else {
                                        pointer.inputs.add(new Pair<>(null, res));
                                        res = null;
                                    }
                                }
                                else if (pointer.reachedCurrentRuleEnd() || pointer.foundMatch) {
                                    System.out.println("resolving...");
                                    seen.add(pointer.id);
                                    temp = pointer;
                                    if (res != null)
                                        pointer.inputs.add(new Pair<>(null, res));
                                    res = pointer.currentAction().act(pointer.inputs);
                                    pointer = pointer.backedBy;
                                    pointer.rulePointer = temp.backedByPos;
                                }
                                else {
                                    if (res != null) {
                                        System.out.println("Hello");
                                        pointer.inputs.add(new Pair<>(null, res));
                                    }
                                    break;
                                }
                            }
                            if (!found) {
                                NodePointer tempP = new NodePointer(pointer.id,
                                        pointer.node, currentPointer.tokenPointer + 1,
                                        pointer.rulePointer, pointer.ruleTokenPointer +
                                        (tokens[currentPointer.tokenPointer + 1].sym == sym.EOF ? 0 : 1),
                                        pointer.startPoint, pointer, pointer);
                                tempP.inputs = new ArrayList<>(pointer.inputs);
                                currentPointer = tempP;
                                if (tokens[currentPointer.tokenPointer].sym == sym.EOF) {
                                    System.out.println("finished compile.");
                                    done = true;
                                    continue;
                                }
                            }
                        }
                    } else {
                        System.out.println("matched token. advancing...");
                        Object result = null;
                        if (currentPointer.currentRuleToken() instanceof String) {
                            System.out.println("found value.");
                            if (token.sym == sym.NUMBER) {
                                System.out.println("found number.");
                                Codes.Value value = new Codes.Value();
                                value.setValue(token.value);
                                result = value;
                            } else if (token.sym == sym.STRING) {
                                System.out.println("found string.");
                                Codes.Value value = new Codes.Value();
                                value.setValue(token.value);
                                result = value;
                            } else if (token.sym == sym.IDENTIFIER) {
                                System.out.println("found id.");
                                Codes.Identifier identifier = new Codes.Identifier();
                                identifier.setName((String) token.value);
                                result = identifier;
                            }
                        } else {
                            result = currentPointer.currentAction().act(currentPointer.inputs);
                        }
                        currentPointer = new NodePointer(currentPointer.id, currentPointer.node,
                                currentPointer.tokenPointer + 1, currentPointer.rulePointer,
                                currentPointer.ruleTokenPointer +
                                        (tokens[currentPointer.tokenPointer + 1].sym == sym.EOF ? 0 : 1),
                                currentPointer.startPoint, currentPointer.tokenPointer == 0 ? currentPointer :
                                currentPointer.backedBy, currentPointer);
                        currentPointer.inputs = new ArrayList<>(currentPointer.prev.inputs);
                        if (result != null) {
                            Pair<Symbol, Object> r = new Pair<>(null, result);
                            currentPointer.inputs.add(r);
                        }
                        if (tokens[currentPointer.tokenPointer].sym == sym.EOF) {
                            System.out.println("finished compile.");
                            done = true;
                            continue;
                        }
                    }
                }
                else {
                    System.out.println("NOT matched rule. getting back...");
                    NodePointer pointer = currentPointer;
                    HashSet<String> seen = new HashSet<>();
                    while (pointer.prevRuleToken() != null &&
                            pointer.prevRuleToken() instanceof Node) {
                        System.out.println("current node is not stable. going deeper...");
                        String id = pointer.id;
                        while (pointer.id.equals(id)) {
                            pointer = pointer.prev;
                        }
                    }
                    System.out.println("reached " + pointer.node.name);
                    boolean movedToNextRule = false;
                    while (true) {
                        System.out.println("reached " + pointer.node.name + " " + pointer.id + " " +
                                pointer.tokenPointer + " " + pointer.rulePointer + " " + pointer.ruleTokenPointer +
                                " " + pointer.foundMatch);
                        if (seen.contains(pointer.id)) {
                            pointer = pointer.prev;
                            System.out.println("looping...");
                        }
                        else if (pointer.configRulePointerToRepair(token)) {
                            System.out.println("repaired node with new similar rule.");
                            seen.add(pointer.id);
                            movedToNextRule = true;
                            break;
                        }
                        else if (!pointer.reachedAllRulesEnd()) {
                            break;
                        } else {
                            seen.add(pointer.id);
                            pointer = pointer.prev;
                            System.out.println("looping...");
                        }
                    }
                    if (!movedToNextRule) pointer.forwardRule();
                    currentPointer = pointer.makeChildOfYourself();
                    currentPointer.inputs = new ArrayList<>(pointer.inputs);
                    if (!movedToNextRule) currentPointer.tokenPointer = currentPointer.startPoint;
                }
            }
            counter++;
        }
        return null;
    }
}
