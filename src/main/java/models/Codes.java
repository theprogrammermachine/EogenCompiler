package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Codes {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = If.class, name = "If"),
            @JsonSubTypes.Type(value = ElseIf.class, name = "ElseIf"),
            @JsonSubTypes.Type(value = Else.class, name = "Else"),
            @JsonSubTypes.Type(value = While.class, name = "While"),
            @JsonSubTypes.Type(value = For.class, name = "For"),
            @JsonSubTypes.Type(value = Foreach.class, name = "Foreach"),
            @JsonSubTypes.Type(value = Function.class, name = "Function"),
            @JsonSubTypes.Type(value = Value.class, name = "Value"),
            @JsonSubTypes.Type(value = Call.class, name = "Call"),
            @JsonSubTypes.Type(value = Assignment.class, name = "Assignment"),
            @JsonSubTypes.Type(value = Variable.class, name = "Variable"),
            @JsonSubTypes.Type(value = Definition.class, name = "Definition"),
            @JsonSubTypes.Type(value = MathExpSum.class, name = "MathExpSum"),
            @JsonSubTypes.Type(value = MathExpSubstract.class, name = "MathExpSubstract"),
            @JsonSubTypes.Type(value = MathExpMultiply.class, name = "MathExpMultiply"),
            @JsonSubTypes.Type(value = MathExpDivide.class, name = "MathExpDivide"),
            @JsonSubTypes.Type(value = MathExpMod.class, name = "MathExpMod"),
            @JsonSubTypes.Type(value = MathExpPower.class, name = "MathExpPower"),
            @JsonSubTypes.Type(value = MathExpUminus.class, name = "MathExpUminus"),
            @JsonSubTypes.Type(value = Increment.class, name = "Increment"),
            @JsonSubTypes.Type(value = Decrement.class, name = "Decrement"),
            @JsonSubTypes.Type(value = Return.class, name = "Return"),
            @JsonSubTypes.Type(value = Field.class, name = "Field"),
            @JsonSubTypes.Type(value = Instantiate.class, name = "Instantiate"),
            @JsonSubTypes.Type(value = Instance.class, name = "Instance"),
            @JsonSubTypes.Type(value = Array.class, name = "Array"),
            @JsonSubTypes.Type(value = ClassReference.class, name = "ClassReference"),
            @JsonSubTypes.Type(value = ArrayItem.class, name = "ArrayItem"),
            @JsonSubTypes.Type(value = ArrayPushItem.class, name = "ArrayPushItem"),
            @JsonSubTypes.Type(value = Chains.class, name = "Chains"),
            @JsonSubTypes.Type(value = Reference.class, name = "Reference"),
            @JsonSubTypes.Type(value = Parenthesis.class, name = "Parenthesis"),
            @JsonSubTypes.Type(value = MathExpEqual.class, name = "MathExpEqual"),
            @JsonSubTypes.Type(value = MathExpNE.class, name = "MathExpNE"),
            @JsonSubTypes.Type(value = MathExpGT.class, name = "MathExpGT"),
            @JsonSubTypes.Type(value = MathExpGE.class, name = "MathExpGE"),
            @JsonSubTypes.Type(value = MathExpLT.class, name = "MathExpLT"),
            @JsonSubTypes.Type(value = MathExpLE.class, name = "MathExpLE"),
            @JsonSubTypes.Type(value = MathExpAnd.class, name = "MathExpAnd"),
            @JsonSubTypes.Type(value = MathExpOr.class, name = "MathExpOr"),
            @JsonSubTypes.Type(value = Class.class, name = "Class"),
            @JsonSubTypes.Type(value = Behaviour.class, name = "Behaviour"),
            @JsonSubTypes.Type(value = Identifier.class, name = "Identifier"),
            @JsonSubTypes.Type(value = Switch.class, name = "Switch"),
            @JsonSubTypes.Type(value = Case.class, name = "Case"),
            @JsonSubTypes.Type(value = Is.class, name = "Is"),
            @JsonSubTypes.Type(value = Try.class, name = "Try"),
            @JsonSubTypes.Type(value = Of.class, name = "Of"),
            @JsonSubTypes.Type(value = On.class, name = "On"),
            @JsonSubTypes.Type(value = As.class, name = "As"),
            @JsonSubTypes.Type(value = CounterFor.class, name = "CounterFor"),
            @JsonSubTypes.Type(value = Prop.class, name = "Prop"),
            @JsonSubTypes.Type(value = NotSatisfied.class, name = "NotSatisfied"),
            @JsonSubTypes.Type(value = BasedOnExtension.class, name = "BasedOnExtension"),
            @JsonSubTypes.Type(value = BehavesLikeExtension.class, name = "BehavesLikeExtension")
    })
    public static class Code implements Serializable {

    }

    public enum DataType {
        @JsonProperty("Number") Number,
        @JsonProperty("String") String,
        @JsonProperty("Bool") Bool
    }

    public enum DataLevel {
        @JsonProperty("ClassLevel") ClassLevel,
        @JsonProperty("InstanceLevel") InstanceLevel
    }

    public static class NotSatisfied extends Code {

    }

    public static class Prop extends Code {

        private Identifier id;
        private Code value;
        private DataLevel level;

        public Identifier getId() {
            return id;
        }

        public void setId(Identifier id) {
            this.id = id;
        }

        public Code getValue() {
            return value;
        }

        public void setValue(Code value) {
            this.value = value;
        }

        public DataLevel getLevel() {
            return level;
        }

        public void setLevel(DataLevel level) {
            this.level = level;
        }
    }

    public static class CounterFor extends Code {

        private Codes.Code limit;
        private Codes.Code step;
        private List<Code> codes;

        public Code getLimit() {
            return limit;
        }

        public void setLimit(Code limit) {
            this.limit = limit;
        }

        public Code getStep() {
            return step;
        }

        public void setStep(Code step) {
            this.step = step;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class As extends Code {

        private Code code1;
        private Identifier id;

        public Code getCode1() {
            return code1;
        }

        public void setCode1(Code code1) {
            this.code1 = code1;
        }

        public Identifier getId() {
            return id;
        }

        public void setId(Identifier id) {
            this.id = id;
        }
    }

    public static class On extends Code {

        private Code code1;
        private Code code2;

        public Code getCode1() {
            return code1;
        }

        public void setCode1(Code code1) {
            this.code1 = code1;
        }

        public Code getCode2() {
            return code2;
        }

        public void setCode2(Code code2) {
            this.code2 = code2;
        }
    }

    public static class Of extends Code {

        private Code code1;
        private Code code2;

        public Code getCode1() {
            return code1;
        }

        public void setCode1(Code code1) {
            this.code1 = code1;
        }

        public Code getCode2() {
            return code2;
        }

        public void setCode2(Code code2) {
            this.code2 = code2;
        }
    }

    public static class Try extends Code {

        private List<Code> tryCode;
        private Identifier exVar;
        private List<Code> catchCode;

        public List<Code> getTryCode() {
            return tryCode;
        }

        public void setTryCode(List<Code> tryCode) {
            this.tryCode = tryCode;
        }

        public Identifier getExVar() {
            return exVar;
        }

        public void setExVar(Identifier exVar) {
            this.exVar = exVar;
        }

        public List<Code> getCatchCode() {
            return catchCode;
        }

        public void setCatchCode(List<Code> catchCode) {
            this.catchCode = catchCode;
        }
    }

    public static class If extends Code {

        private Code condition;
        private List<Code> codes;
        private List<Code> extras;

        public Code getCondition() {
            return condition;
        }

        public void setCondition(Code condition) {
            this.condition = condition;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }

        public List<Code> getExtras() {
            return extras;
        }

        public void setExtras(List<Code> extras) {
            this.extras = extras;
        }
    }

    public static class ElseIf extends Code {

        private Code condition;
        private List<Code> codes;

        public Code getCondition() {
            return condition;
        }

        public void setCondition(Code condition) {
            this.condition = condition;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class Else extends Code {

        private List<Code> codes;

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class Switch extends Code {

        private Code value;
        private List<Case> cases;

        public Code getValue() {
            return value;
        }

        public void setValue(Code value) {
            this.value = value;
        }

        public List<Case> getCases() {
            return cases;
        }

        public void setCases(List<Case> cases) {
            this.cases = cases;
        }
    }

    public static class Case extends Code {

        private Code value;
        private List<Code> codes;

        public Code getValue() {
            return value;
        }

        public void setValue(Code value) {
            this.value = value;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class While extends Code {

        private Code condition;
        private List<Code> codes;

        public Code getCondition() {
            return condition;
        }

        public void setCondition(Code condition) {
            this.condition = condition;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class For extends Code {

        private Code counter;
        private Code condition;
        private Code action;
        private List<Code> codes;

        public Code getCounter() {
            return counter;
        }

        public void setCounter(Code counter) {
            this.counter = counter;
        }

        public Code getCondition() {
            return condition;
        }

        public void setCondition(Code condition) {
            this.condition = condition;
        }

        public Code getAction() {
            return action;
        }

        public void setAction(Code action) {
            this.action = action;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class Foreach extends Code {

        private Identifier temp;
        private Code collection;
        private List<Code> codes;

        public Identifier getTemp() {
            return temp;
        }

        public void setTemp(Identifier temp) {
            this.temp = temp;
        }

        public Code getCollection() {
            return collection;
        }

        public void setCollection(Code collection) {
            this.collection = collection;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class Function extends Code {

        private String name;
        private DataLevel level;
        private List<Identifier> params;
        private List<Code> codes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DataLevel getLevel() {
            return level;
        }

        public void setLevel(DataLevel level) {
            this.level = level;
        }

        public List<Identifier> getParams() {
            return params;
        }

        public void setParams(List<Identifier> params) {
            this.params = params;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class Value extends Code {

        private Object value;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    public static class Call extends Code {

        private Code funcReference;
        private Hashtable<String, Code> entries;

        public Code getFuncReference() {
            return funcReference;
        }

        public void setFuncReference(Code funcReference) {
            this.funcReference = funcReference;
        }

        public Hashtable<String, Code> getEntries() {
            return entries;
        }

        public void setEntries(Hashtable<String, Code> entries) {
            this.entries = entries;
        }
    }

    public static class Assignment extends Code {

        private Code var;
        private Code value;

        public Code getVar() {
            return var;
        }

        public void setVar(Code var) {
            this.var = var;
        }

        public Code getValue() {
            return value;
        }

        public void setValue(Code value) {
            this.value = value;
        }
    }

    public static class Variable extends Code {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Definition extends Code {

        private String varName;
        private String dataType;

        public String getVarName() {
            return varName;
        }

        public void setVarName(String varName) {
            this.varName = varName;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }
    }

    public static class MathExpSum extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpSubstract extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpMultiply extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpDivide extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpMod extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpPower extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpUminus extends Code {

        private Code value;

        public Code getValue() {
            return value;
        }

        public void setValue(Code value) {
            this.value = value;
        }
    }

    public static class MathExpEqual extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpNE extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpGT extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpGE extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpLT extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpLE extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpAnd extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpOr extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class Increment extends Code {

        private Variable var;

        public Variable getVar() {
            return var;
        }

        public void setVar(Variable var) {
            this.var = var;
        }
    }

    public static class Decrement extends Code {

        private Variable var;

        public Variable getVar() {
            return var;
        }

        public void setVar(Variable var) {
            this.var = var;
        }
    }

    public static class Return extends Code {

        private Code value;

        public Code getValue() {
            return value;
        }

        public void setValue(Code value) {
            this.value = value;
        }
    }

    public static class Field extends Code {

        private String name;
        private Code value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Code getValue() {
            return value;
        }

        public void setValue(Code value) {
            this.value = value;
        }
    }

    public static class Instantiate extends Code {

        private Code classReference;
        private Hashtable<String, Code> entries;

        public Code getClassReference() {
            return classReference;
        }

        public void setClassReference(Code classReference) {
            this.classReference = classReference;
        }

        public Hashtable<String, Code> getEntries() {
            return entries;
        }

        public void setEntries(Hashtable<String, Code> entries) {
            this.entries = entries;
        }
    }

    public static class Instance extends Code {

        private Code classReference;
        private Hashtable<String, Code> data;

        public Code getClassReference() {
            return classReference;
        }

        public void setClassReference(Code classReference) {
            this.classReference = classReference;
        }

        public Hashtable<String, Code> getData() {
            return data;
        }

        public void setData(Hashtable<String, Code> data) {
            this.data = data;
        }
    }

    public static class Identifier extends Code {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Is extends Code {

        private Code code1;
        private Code code2;

        public Code getCode1() {
            return code1;
        }

        public void setCode1(Code code1) {
            this.code1 = code1;
        }

        public Code getCode2() {
            return code2;
        }

        public void setCode2(Code code2) {
            this.code2 = code2;
        }
    }

    public static class Behaviour extends Code {

        private String name;
        private List<Code> codes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class BasedOnExtension extends Code {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class BehavesLikeExtension extends Code {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Class extends Code {

        private String name;
        private List<Code> extensions;
        private List<Code> codes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Code> getExtensions() {
            return extensions;
        }

        public void setExtensions(List<Code> extensions) {
            this.extensions = extensions;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class Array extends Code {

        private List<Code> items;

        public Array() {
            this.items = new ArrayList<>();
        }

        public List<Code> getItems() {
            return items;
        }

        public void setItems(List<Code> items) {
            this.items = items;
        }
    }

    public static class ClassReference extends Code {

        private String className;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }

    public static class ArrayPushItem extends Code {

        private String arrName;
        private Code value;

        public String getArrName() {
            return arrName;
        }

        public void setArrName(String arrName) {
            this.arrName = arrName;
        }

        public Code getValue() {
            return value;
        }

        public void setValue(Code value) {
            this.value = value;
        }
    }

    public static class ArrayItem extends Code {

        private Code array;
        private Code index;

        public Code getArray() {
            return array;
        }

        public void setArray(Code array) {
            this.array = array;
        }

        public Code getIndex() {
            return index;
        }

        public void setIndex(Code index) {
            this.index = index;
        }
    }

    public static class Chains extends Code {

        private Code code1;
        private Code code2;

        public Code getCode1() {
            return code1;
        }

        public void setCode1(Code code1) {
            this.code1 = code1;
        }

        public Code getCode2() {
            return code2;
        }

        public void setCode2(Code code2) {
            this.code2 = code2;
        }
    }

    public static class Reference extends Code {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Parenthesis extends Code {

        private Code code;

        public Code getCode() {
            return code;
        }

        public void setCode(Code code) {
            this.code = code;
        }
    }
}
