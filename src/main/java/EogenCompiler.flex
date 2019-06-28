import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import models.*;
import java_cup.runtime.*;

%%

%class EogenLexer
%standalone
%column
%line
%cup
%public
%unicode
%char

%{
    public class Tuple {
        public final int line;
        public final int col;
        public final String token;
        public final String type;

        public Tuple(int line, int col, String token, String type) {
            this.line = line;
            this.col = col;
            this.token = token;
            this.type = type;
        }
    }

    HashSet<String> keywords = new HashSet<>(Arrays.asList(
            "if", "else", "while", "switch", "case", "for", "foreach", "class", "behaviour",
            "based", "on", "behaves", "like", "try", "catch", "function",
            "mod", "of", "instance", "empty", "is"
            ));

    Queue<Symbol> symbolPipe = new ConcurrentLinkedQueue<Symbol>();

    boolean foundString = false;
    int prevLineTabCount = 0;
    StringBuilder string = new StringBuilder();

    public Symbol exportToken(int symNum, Object value, int line, int column) {

        if (value instanceof String && !(symNum == sym.LPAREN || symNum == sym.RPAREN)) {
            String text = (String) value;
            text = text.replace("(", "");
            text = text.replace("[", "");
            text = text.replace("{", "");
            text = text.replace(")", "");
            text = text.replace("]", "");
            text = text.replace("}", "");
            value = text;
        }

        if (symNum == sym.TAB)
            symbolPipe.add(new TabSymbol(symNum, line + 1, column + 1, value));
        else
            symbolPipe.add(new Symbol(symNum, line + 1, column + 1, value));

        if (!symbolPipe.isEmpty()) {
            return symbolPipe.poll();
        } else {
            return new Symbol(sym.EOF);
        }
    }

    public Symbol exportToken(int[] symNums, Object[] values) {

        for (int counter = 0; counter < symNums.length; counter++) {
            if (values[counter] instanceof String && !(symNums[counter] == sym.LPAREN || symNums[counter] == sym.RPAREN)) {
                String text = (String) values[counter];
                text = text.replace("(", "");
                text = text.replace("[", "");
                text = text.replace("{", "");
                text = text.replace(")", "");
                text = text.replace("]", "");
                text = text.replace("}", "");
                values[counter] = text;
            }
            if (symNums[counter] == sym.TAB)
                symbolPipe.add(new TabSymbol(symNums[counter], values[counter]));
            else
                symbolPipe.add(new Symbol(symNums[counter], values[counter]));
        }

        if (!symbolPipe.isEmpty()) {
            return symbolPipe.poll();
        } else {
            return new Symbol(sym.EOF);
        }
    }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace = [ \t\f]
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/*" "*"+ [^/*] ~"*/"
DecIntegerLiteral = 0 | [1-9][0-9]*
HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexDigit          = [0-9a-fA-F]
OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctDigit          = [0-7]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?
SingleCharacter = [^\r\n\'\\]
FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+
NUMBER = {NumberString}
NumberString = {DecIntegerLiteral}|{HexIntegerLiteral}|{OctIntegerLiteral}|{DoubleLiteral}
Identifier = [:jletter:][:jletterdigit:]*
Lbracket = \[
Rbracket = \]
Assign = "="
Equal = "=="
Sum = "+"
Minus = "-"
Multiply = "*"
Divide = "/"
Power = "^"
LParen = "("
RParen = ")"
LBrace = "{"
RBrace = "}"
Semicolon = ";"
Colon = ":"
Comma = ","
Gt = ">"
Lt = "<"
Ge = ">="
Le = "<="
Ne = "!="
Arrow = "->"

%%

<YYINITIAL> {
{Lbracket}                     {if (!foundString) return exportToken(sym.LBRACKET, yytext(), yyline, yycolumn); else string.append(yytext());}
{Rbracket}                     {if (!foundString) return exportToken(sym.RBRACKET, yytext(), yyline, yycolumn); else string.append(yytext());}
{Lbracket}                     {if (!foundString) return exportToken(sym.LBRACKET, yytext(), yyline, yycolumn); else string.append(yytext());}
{Rbracket}                     {if (!foundString) return exportToken(sym.RBRACKET, yytext(), yyline, yycolumn); else string.append(yytext());}
{Assign}                       {if (!foundString) return exportToken(sym.ASSIGN, yytext(), yyline, yycolumn); else string.append(yytext());}
{Equal}                        {if (!foundString) return exportToken(sym.EQUAL, yytext(), yyline, yycolumn); else string.append(yytext());}
{Power}                        {if (!foundString) return exportToken(sym.POWER, yytext(), yyline, yycolumn); else string.append(yytext());}
{Divide}                       {if (!foundString) return exportToken(sym.DIVISION, yytext(), yyline, yycolumn); else string.append(yytext());}
{Multiply}                     {if (!foundString) return exportToken(sym.MULTIPLY, yytext(), yyline, yycolumn); else string.append(yytext());}
{Minus}                        {if (!foundString) return exportToken(sym.SUBTRACT, yytext(), yyline, yycolumn); else string.append(yytext());}
{Sum}                          {if (!foundString) return exportToken(sym.SUM, yytext(), yyline, yycolumn); else string.append(yytext());}
{LParen}                       {if (!foundString) return exportToken(sym.LPAREN, yytext(), yyline, yycolumn); else string.append(yytext());}
{RParen}                       {if (!foundString) return exportToken(sym.RPAREN, yytext(), yyline, yycolumn); else string.append(yytext());}
{LBrace}                       {if (!foundString) return exportToken(sym.LBRACE, yytext(), yyline, yycolumn); else string.append(yytext());}
{RBrace}                       {if (!foundString) return exportToken(sym.RBRACE, yytext(), yyline, yycolumn); else string.append(yytext());}
{Semicolon}                    {if (!foundString) return exportToken(sym.SEMI, yytext(), yyline, yycolumn); else string.append(yytext());}
{Colon}                        {if (!foundString) return exportToken(sym.COLON, yytext(), yyline, yycolumn); else string.append(yytext());}
{Comma}                        {if (!foundString) return exportToken(sym.COMMA, yytext(), yyline, yycolumn); else string.append(yytext());}
{Lt}                           {if (!foundString) return exportToken(sym.LT, yytext(), yyline, yycolumn); else string.append(yytext());}
{Gt}                           {if (!foundString) return exportToken(sym.GT, yytext(), yyline, yycolumn); else string.append(yytext());}
{Le}                           {if (!foundString) return exportToken(sym.LE, yytext(), yyline, yycolumn); else string.append(yytext());}
{Ge}                           {if (!foundString) return exportToken(sym.GE, yytext(), yyline, yycolumn); else string.append(yytext());}
{Ne}                           {if (!foundString) return exportToken(sym.NE, yytext(), yyline, yycolumn); else string.append(yytext());}
<<EOF>>
{
    if (!symbolPipe.isEmpty()) {
        return symbolPipe.poll();
    } else {
        return new Symbol(sym.EOF);
    }
}
{LineTerminator}({WhiteSpace})*
{
    String text = yytext();
    if (text.length() > prevLineTabCount) {
        if ((text.length() - prevLineTabCount) / 2 == 1) {
            prevLineTabCount = text.length();
            return exportToken(sym.START, yytext(), yyline, yycolumn);
        } else {
            int[] syms = new int[(prevLineTabCount - text.length()) / 2];
            for (int counter = 0; counter < (prevLineTabCount - text.length()) / 2; counter++) {
                syms[counter] = sym.START;
            }
            Object[] values = new Object[(prevLineTabCount - text.length()) / 2];
            prevLineTabCount = text.length();
            return exportToken(syms, values);
        }
    } else if (text.length() < prevLineTabCount) {
        if ((prevLineTabCount - text.length()) / 2 == 1) {
            prevLineTabCount = text.length();
            return exportToken(sym.END, yytext(), yyline, yycolumn);
        } else {
            int[] syms = new int[(prevLineTabCount - text.length()) / 2];
            for (int counter = 0; counter < (prevLineTabCount - text.length()) / 2; counter++) {
                syms[counter] = sym.END;
            }
            Object[] values = new Object[(prevLineTabCount - text.length()) / 2];
            prevLineTabCount = text.length();
            return exportToken(syms, values);
        }
    }
}
{Arrow}                        {if (!foundString) return exportToken(sym.ARROW, yytext(), yyline, yycolumn); else string.append(yytext());}
\"                             {
    if (!foundString) {
        foundString = true;
        string.append(yytext());
    } else {
        foundString = false;
        string.append(yytext());
        String result = string.toString();
        string.setLength(0);
        string = new StringBuilder();
        return exportToken(sym.STRING, result, yyline, yycolumn);
    }
}

args                           {if (!foundString) return exportToken(sym.ARGS, yytext(), yyline, yycolumn); else string.append(yytext());}
item                           {if (!foundString) return exportToken(sym.ITEM, yytext(), yyline, yycolumn); else string.append(yytext());}
define                         {if (!foundString) return exportToken(sym.DEFINE, yytext(), yyline, yycolumn); else string.append(yytext());}
named                          {if (!foundString) return exportToken(sym.NAMED, yytext(), yyline, yycolumn); else string.append(yytext());}
with                           {if (!foundString) return exportToken(sym.WITH, yytext(), yyline, yycolumn); else string.append(yytext());}
params                         {if (!foundString) return exportToken(sym.PARAMS, yytext(), yyline, yycolumn); else string.append(yytext());}
conditions                     {if (!foundString) return exportToken(sym.CONDITIONS, yytext(), yyline, yycolumn); else string.append(yytext());}
EMPTY                          {if (!foundString) return exportToken(sym.EMPTY, yytext(), yyline, yycolumn); else string.append(yytext());}
true                           {if (!foundString) return exportToken(sym.TRUE, yytext(), yyline, yycolumn); else string.append(yytext());}
false                          {if (!foundString) return exportToken(sym.FALSE, yytext(), yyline, yycolumn); else string.append(yytext());}
are                            {if (!foundString) return exportToken(sym.ARE, yytext(), yyline, yycolumn); else string.append(yytext());}
satisfied                      {if (!foundString) return exportToken(sym.SATISFIED, yytext(), yyline, yycolumn); else string.append(yytext());}
then                           {if (!foundString) return exportToken(sym.THEN, yytext(), yyline, yycolumn); else string.append(yytext());}
loop                           {if (!foundString) return exportToken(sym.LOOP, yytext(), yyline, yycolumn); else string.append(yytext());}
times                          {if (!foundString) return exportToken(sym.TIMES, yytext(), yyline, yycolumn); else string.append(yytext());}
by                             {if (!foundString) return exportToken(sym.BY, yytext(), yyline, yycolumn); else string.append(yytext());}
step                           {if (!foundString) return exportToken(sym.STEP, yytext(), yyline, yycolumn); else string.append(yytext());}
as                             {if (!foundString) return exportToken(sym.AS, yytext(), yyline, yycolumn); else string.append(yytext());}
not                            {if (!foundString) return exportToken(sym.NOT, yytext(), yyline, yycolumn); else string.append(yytext());}
each                           {if (!foundString) return exportToken(sym.EACH, yytext(), yyline, yycolumn); else string.append(yytext());}
in                             {if (!foundString) return exportToken(sym.IN, yytext(), yyline, yycolumn); else string.append(yytext());}
remember                       {if (!foundString) return exportToken(sym.REMEMBER, yytext(), yyline, yycolumn); else string.append(yytext());}
props                          {if (!foundString) return exportToken(sym.PROPS, yytext(), yyline, yycolumn); else string.append(yytext());}
on                             {if (!foundString) return exportToken(sym.ON, yytext(), yyline, yycolumn); else string.append(yytext());}
object                         {if (!foundString) return exportToken(sym.OBJECT, yytext(), yyline, yycolumn); else string.append(yytext());}
action                         {if (!foundString) return exportToken(sym.ACTION, yytext(), yyline, yycolumn); else string.append(yytext());}
carefully                      {if (!foundString) return exportToken(sym.CAREFULLY, yytext(), yyline, yycolumn); else string.append(yytext());}
do                             {if (!foundString) return exportToken(sym.DO, yytext(), yyline, yycolumn); else string.append(yytext());}
error                          {if (!foundString) return exportToken(sym.ERROR, yytext(), yyline, yycolumn); else string.append(yytext());}
caught                         {if (!foundString) return exportToken(sym.CAUGHT, yytext(), yyline, yycolumn); else string.append(yytext());}
until                          {if (!foundString) return exportToken(sym.UNTIL, yytext(), yyline, yycolumn); else string.append(yytext());}
of                             {if (!foundString) return exportToken(sym.OF, yytext(), yyline, yycolumn); else string.append(yytext());}
instance                       {if (!foundString) return exportToken(sym.INSTANCE, yytext(), yyline, yycolumn); else string.append(yytext());}
is                             {if (!foundString) return exportToken(sym.IS, yytext(), yyline, yycolumn); else string.append(yytext());}
xand                           {if (!foundString) return exportToken(sym.AND, yytext(), yyline, yycolumn); else string.append(yytext());}
or                             {if (!foundString) return exportToken(sym.OR, yytext(), yyline, yycolumn); else string.append(yytext());}
mod                            {if (!foundString) return exportToken(sym.MOD, yytext(), yyline, yycolumn); else string.append(yytext());}
function                       {if (!foundString) return exportToken(sym.FUNCTION, yytext(), yyline, yycolumn); else string.append(yytext());}
encap                          {if (!foundString) return exportToken(sym.ENCAPSULATE, yytext(), yyline, yycolumn); else string.append(yytext());}
class                          {if (!foundString) return exportToken(sym.CLASS, yytext(), yyline, yycolumn); else string.append(yytext());}
behaviour                      {if (!foundString) return exportToken(sym.BEHAVIOUR, yytext(), yyline, yycolumn); else string.append(yytext());}
based                          {if (!foundString) return exportToken(sym.BASED, yytext(), yyline, yycolumn); else string.append(yytext());}
on                             {if (!foundString) return exportToken(sym.ON, yytext(), yyline, yycolumn); else string.append(yytext());}
behaves                        {if (!foundString) return exportToken(sym.BEHAVES, yytext(), yyline, yycolumn); else string.append(yytext());}
like                           {if (!foundString) return exportToken(sym.LIKE, yytext(), yyline, yycolumn); else string.append(yytext());}
try                            {if (!foundString) return exportToken(sym.TRY, yytext(), yyline, yycolumn); else string.append(yytext());}
catch                          {if (!foundString) return exportToken(sym.CATCH, yytext(), yyline, yycolumn); else string.append(yytext());}
for                            {if (!foundString) return exportToken(sym.FOR, yytext(), yyline, yycolumn); else string.append(yytext());}
if                             {if (!foundString) return exportToken(sym.IF, yytext(), yyline, yycolumn); else string.append(yytext());}
else                           {if (!foundString) return exportToken(sym.ELSE, yytext(), yyline, yycolumn); else string.append(yytext());}
return                         {if (!foundString) return exportToken(sym.RETURN, yytext(), yyline, yycolumn); else string.append(yytext());}
new                            {if (!foundString) return exportToken(sym.NEW, yytext(), yyline, yycolumn); else string.append(yytext());}
prop                           {if (!foundString) return exportToken(sym.PROP, yytext(), yyline, yycolumn); else string.append(yytext());}
value                          {if (!foundString) return exportToken(sym.VALUE, yytext(), yyline, yycolumn); else string.append(yytext());}
doing                          {if (!foundString) return exportToken(sym.DOING, yytext(), yyline, yycolumn); else string.append(yytext());}
{NUMBER}                       {
    if (!foundString) return exportToken(sym.NUMBER, Double.parseDouble(yytext()), yyline, yycolumn);
    else string.append(yytext());
}
{Identifier}                   {
    if (!foundString) return exportToken(sym.IDENTIFIER, yytext(), yyline, yycolumn);
    else string.append(yytext());
}
{SingleCharacter}              {if (foundString) string.append(yytext());}
{WhiteSpace}                   { /* ignore */ }
{Comment}                      { /* ignore */ }
}