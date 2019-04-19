import java.util.*;

%%

%class EogenCompiler
%standalone
%column
%line

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
            "if", "while", "switch", "for", "foreach", "class", "behaviour",
            "based", "on", "behaves", "like", "try", "catch", "function",
            "mod", "of", "instance", "empty", "is"
    ));
    List<Tuple> foundKeywords = new ArrayList<>();
%}

%eof{
    for (Tuple kwt : foundKeywords) {
       if (!keywords.contains(kwt.token.trim())) {
           System.out.println("detected a " + kwt.type + " : " + kwt.token.trim() + " , line = " + kwt.line + " , col = " + kwt.col);
       }
    }
%eof}

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

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

StringCharacter = [^\r\n\"\\]
SingleCharacter = [^\r\n\'\\]

TRY = {StrongSplitter}try{WeakSplitter}\{
CATCH = {StrongSplitter}catch{WeakSplitter}\({WeakSplitter}{Identifier}{WeakSplitter}\){WeakSplitter}\{
FOR = {StrongSplitter}for{WeakSplitter}\({WeakSplitter}
FOREACH = {StrongSplitter}foreach{WeakSplitter}\({WeakSplitter}
WHILE = {StrongSplitter}while{WeakSplitter}\({WeakSplitter}
IF = {StrongSplitter}if{WeakSplitter}\({WeakSplitter}
SWITCH = {StrongSplitter}switch{WeakSplitter}\(

CLASS = {StrongSplitter}class{WeakSplitter}\{
BEHAVIOUR = {StrongSplitter}behaviour{WeakSplitter}\{
BASEDON = {StrongSplitter}based{StrongSplitter}on{WeakSplitter}\{
BEHAVELIKE = {StrongSplitter}behave{StrongSplitter}like{WeakSplitter}\{
FUNCTIONKEY = {StrongSplitter}function{StrongSplitter}

SUM = {OperatorBorder}\+{OperatorBorder}
MINUS = {OperatorBorder}-{OperatorBorder}
MULTIPLY = {OperatorBorder}\*{OperatorBorder}
DIVISION = {OperatorBorder}\/{OperatorBorder}
MOD = {OperatorBorder}mod{OperatorBorder}
POWER = {OperatorBorder}\^{OperatorBorder}

NUMBER = (({LineTerminator}|{WhiteSpace})+|\+|-|\*|\/|mod|\^|\=){NumberString}(({LineTerminator}|{WhiteSpace})+|\+|-|\*|\/|mod|\^|\=)

OFCLASS = ({LineTerminator}|{WhiteSpace})+of({LineTerminator}|{WhiteSpace})+class(({LineTerminator}|{WhiteSpace})+|\=)
OFINSTANCE = ({LineTerminator}|{WhiteSpace})+of({LineTerminator}|{WhiteSpace})+instance(({LineTerminator}|{WhiteSpace})+|\=)
EMPTY = (({LineTerminator}|{WhiteSpace})+|\=|\()\[({LineTerminator}|{WhiteSpace})+empty({LineTerminator}|{WhiteSpace})+\](({LineTerminator}|{WhiteSpace})+|\))
FUNCTION = {StrongSplitter}{Identifier}{WeakSplitter}\({WeakSplitter}({Identifier}{WeakSplitter},{WeakSplitter})*{Identifier}{WeakSplitter}\){WeakSplitter}\{
CALLBACK = \({WeakSplitter}({Identifier}{WeakSplitter},{WeakSplitter})*{Identifier}{WeakSplitter}\){WeakSplitter}\{
FUNCTIONCALL = \({WeakSplitter}({Identifier}{WeakSplitter}:{WeakSplitter}{Identifier}{WeakSplitter},{WeakSplitter})*{Identifier}{WeakSplitter}:{WeakSplitter}{Identifier}{WeakSplitter}\){WeakSplitter}\{

ARRAY = \[{WeakSplitter}(({SingleCharacter}|{StringCharacter}|{NumberString}){WeakSplitter},{WeakSplitter})*\]

IS = {StrongSplitter}is{StrongSplitter}

NumberString = {DecIntegerLiteral}|{HexIntegerLiteral}|{OctIntegerLiteral}|{DoubleLiteral}
OperatorBorder = ({StrongSplitter}|{Identifier}|{NumberString})

StrongSplitter = ({LineTerminator}|{WhiteSpace})+
WeakSplitter = ({LineTerminator}|{WhiteSpace})*

Identifier = ({StrongSplitter}|\{|\[|\(|\=)[:jletter:][:jletterdigit:]*({StrongSplitter}|\}|\]|\))

%%

{OFCLASS}                      {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "static sign"));}
{OFINSTANCE}                   {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "non static sign"));}
{EMPTY}                        {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "empty (null)"));}
{CALLBACK}                     {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "callback"));}
{FUNCTIONCALL}                 {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "function call"));}
"."                            {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "chain sign"));}
IS                             {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "is"));}
{ARRAY}                        {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "array"));}
{POWER}                        {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "power"));}
{MOD}                          {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "division"));}
{DIVISION}                     {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "division"));}
{MULTIPLY}                     {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "multiply"));}
{MINUS}                        {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "minus"));}
{SUM}                          {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "sum"));}
{NUMBER}                       {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "number"));}
{FUNCTIONKEY}                  {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "function key"));}
{FUNCTION}                     {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "function"));}
{CLASS}                        {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "class"));}
{BEHAVIOUR}                    {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "behaviour"));}
{BASEDON}                      {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "based on"));}
{BEHAVELIKE}                   {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "behave like"));}
{TRY}                          {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "try"));}
{CATCH}                        {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "catch"));}
{FOR}                          {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "for"));}
{FOREACH}                      {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "foreach"));}
{WHILE}                        {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "while"));}
{IF}                           {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "if"));}
{SWITCH}                       {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "switch"));}
{Identifier}                   {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "identifier"));}
{WhiteSpace}                   { /* ignore */ }
{Comment}                      {foundKeywords.add(new Tuple(yyline, yycolumn, yytext(), "comment"));}