import java.util.*;
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

    boolean foundString = false;
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

        if (symNum == sym.IDENTIFIER)
            if (!keywords.contains(value))
                return new Symbol(symNum, line + 1, column + 1, value);

        return new Symbol(symNum, line + 1, column + 1, value);
    }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

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

TRY = try
CATCH = catch
FOR = for
FOREACH = foreach
WHILE = while
IF = if
ELSE = else
SWITCH = switch
CASE = case

CLASS = class
BEHAVIOUR = behaviour
BASED = based
ON = on
BEHAVES = behaves
LIKE = like
FUNCTION = function
OF = of
INSTANCE = instance
IS = is
EXEC=exec
ENCAPSULATE = encap
RETURN = return

SUM = \+
MINUS = \-
MULTIPLY = \*
DIVISION = \/
MOD = mod
POWER = \^
AND = and
OR = or
EMPTY = \[{WeakSplitter}empty{WeakSplitter}\]
LPAREN = \(
RPAREN = \)
NUMBER = {NumberString}

NumberString = {DecIntegerLiteral}|{HexIntegerLiteral}|{OctIntegerLiteral}|{DoubleLiteral}

WeakSplitter = ({LineTerminator}|{WhiteSpace})*

Identifier = [:jletter:][:jletterdigit:]*

%%

<YYINITIAL> {
{OF}                           {return exportToken(sym.OF, yytext(), yyline, yycolumn);}
{INSTANCE}                     {return exportToken(sym.INSTANCE, yytext(), yyline, yycolumn);}
{EMPTY}                        {return exportToken(sym.EMPTY, yytext(), yyline, yycolumn);}
"."                            {return exportToken(sym.CHAINSIGN, yytext(), yyline, yycolumn);}
{IS}                           {return exportToken(sym.IS, yytext(), yyline, yycolumn);}
"="                            {return exportToken(sym.ASSIGN, yytext(), yyline, yycolumn);}
"=="                           {return exportToken(sym.EQUAL, yytext(), yyline, yycolumn);}
{AND}                          {return exportToken(sym.AND, yytext(), yyline, yycolumn);}
{OR}                           {return exportToken(sym.OR, yytext(), yyline, yycolumn);}
{POWER}                        {return exportToken(sym.POWER, yytext(), yyline, yycolumn);}
{MOD}                          {return exportToken(sym.MOD, yytext(), yyline, yycolumn);}
{DIVISION}                     {return exportToken(sym.DIVISION, yytext(), yyline, yycolumn);}
{MULTIPLY}                     {return exportToken(sym.MULTIPLY, yytext(), yyline, yycolumn);}
{MINUS}                        {return exportToken(sym.SUBTRACT, yytext(), yyline, yycolumn);}
{SUM}                          {return exportToken(sym.SUM, yytext(), yyline, yycolumn);}
{FUNCTION}                     {return exportToken(sym.FUNCTION, yytext(), yyline, yycolumn);}
{EXEC}                         {return exportToken(sym.EXEC, yytext(), yyline, yycolumn);}
{ENCAPSULATE}                  {return exportToken(sym.ENCAPSULATE, yytext(), yyline, yycolumn);}
{CLASS}                        {return exportToken(sym.CLASS, yytext(), yyline, yycolumn);}
{BEHAVIOUR}                    {return exportToken(sym.BEHAVIOUR, yytext(), yyline, yycolumn);}
{BASED}                        {return exportToken(sym.BASED, yytext(), yyline, yycolumn);}
{ON}                           {return exportToken(sym.ON, yytext(), yyline, yycolumn);}
{BEHAVES}                      {return exportToken(sym.BEHAVES, yytext(), yyline, yycolumn);}
{LIKE}                         {return exportToken(sym.LIKE, yytext(), yyline, yycolumn);}
{TRY}                          {return exportToken(sym.TRY, yytext(), yyline, yycolumn);}
{CATCH}                        {return exportToken(sym.CATCH, yytext(), yyline, yycolumn);}
{FOR}                          {return exportToken(sym.FOR, yytext(), yyline, yycolumn);}
{FOREACH}                      {return exportToken(sym.FOREACH, yytext(), yyline, yycolumn);}
{WHILE}                        {return exportToken(sym.WHILE, yytext(), yyline, yycolumn);}
{IF}                           {return exportToken(sym.IF, yytext(), yyline, yycolumn);}
{ELSE}                         {return exportToken(sym.ELSE, yytext(), yyline, yycolumn);}
{SWITCH}                       {return exportToken(sym.SWITCH, yytext(), yyline, yycolumn);}
{CASE}                         {return exportToken(sym.CASE, yytext(), yyline, yycolumn);}
{RETURN}                       {return exportToken(sym.RETURN, yytext(), yyline, yycolumn);}
{LPAREN}                       {return exportToken(sym.LPAREN, yytext(), yyline, yycolumn);}
{RPAREN}                       {return exportToken(sym.RPAREN, yytext(), yyline, yycolumn);}
{NUMBER}                       {
    if (!foundString) return exportToken(sym.NUMBER, Double.parseDouble(yytext()), yyline, yycolumn);
    else string.append(yytext());
}
{Identifier}                   {
    if (!foundString) return exportToken(sym.IDENTIFIER, yytext(), yyline, yycolumn);
    else string.append(yytext());
}
"{"                            {return exportToken(sym.LBRACE, yytext(), yyline, yycolumn);}
"}"                            {return exportToken(sym.RBRACE, yytext(), yyline, yycolumn);}
";"                            {return exportToken(sym.SEMI, yytext(), yyline, yycolumn);}
":"                            {return exportToken(sym.COLON, yytext(), yyline, yycolumn);}
","                            {return exportToken(sym.COMMA, yytext(), yyline, yycolumn);}
"<"                            {return exportToken(sym.LT, yytext(), yyline, yycolumn);}
">"                            {return exportToken(sym.GT, yytext(), yyline, yycolumn);}
"<="                           {return exportToken(sym.LE, yytext(), yyline, yycolumn);}
">="                           {return exportToken(sym.GE, yytext(), yyline, yycolumn);}
"!="                           {return exportToken(sym.NE, yytext(), yyline, yycolumn);}
"->"                           {return exportToken(sym.ARROW, yytext(), yyline, yycolumn);}
{WhiteSpace}                   { /* ignore */ }
{Comment}                      { /* ignore */ }
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
{SingleCharacter}             { if (foundString) string.append(yytext()); }
}