grammar Krash;

@header {
    import craicoverflow89.krash.components.*;
    import craicoverflow89.krash.components.expressions.*;
    import java.lang.StringBuffer;
    import java.util.ArrayList;
}

// Parser Rules
script returns [KrashScript result]
    :   {ArrayList<KrashCommand> data = new ArrayList();}
        (
            command {data.add($command.result);}
        )*
        EOF
        {$result = new KrashScript(data);}
    ;

line returns [KrashCommand result]
    :   command {$result = $command.result;}
        EOF
    ;

command returns [KrashCommand result]
    :   (
            commandComment {$result = $commandComment.result;}
        |
            commandDeclare {$result = $commandDeclare.result;}
        |
            commandExpression {$result = $commandExpression.result;}
        )
    ;

commandComment returns [KrashCommandComment result]
    :   (
            commandCommentMulti
            {$result = new KrashCommandComment($commandCommentMulti.text);}
        |
            commandCommentSingle
            {$result = new KrashCommandComment($commandCommentSingle.text);}
        )
    ;

commandCommentMulti
    :   COMMENT_MULTI
    ;

commandCommentSingle
    :   COMMENT_SINGLE
    ;

commandDeclare returns [KrashCommandDeclare result]
    :   expressionRefChars EQUAL expression
        // NOTE: could add list[1] and map["key"] update syntax
        {$result = new KrashCommandDeclare($expressionRefChars.text, $expression.result);}
    ;

commandExpression returns [KrashCommandExpression result]
    :   expression
        {$result = new KrashCommandExpression($expression.result);}
    ;

expression returns [KrashExpression result]
    :   {Boolean toString = false;}
        (
            BANG {toString = true;}
        )?
        expressionData
        {$result = new KrashExpressionData($expressionData.result, toString);}
    ;

expressionData returns [KrashExpression result]
    :   (
            expressionGlobal {$result = $expressionGlobal.result;}
        |
            expressionLit {$result = $expressionLit.result;}
        |
            expressionRef {$result = $expressionRef.result;}
        )
        (
            expressionIndex {$result = new KrashExpressionIndex($result, $expressionIndex.result);}
        |
            expressionInvoke {$result = new KrashExpressionInvoke($result, $expressionInvoke.result);}
        |
            expressionMember {$result = new KrashExpressionMember($result, $expressionMember.result);}
        |
            expressionOpAdd {$result = new KrashExpressionOperatorAddition($result, $expressionOpAdd.result);}
        |
            expressionOpDivide {$result = new KrashExpressionOperatorDivision($result, $expressionOpDivide.result);}
        |
            expressionOpMultiply {$result = new KrashExpressionOperatorMultiplication($result, $expressionOpMultiply.result);}
        |
            expressionOpSubtract {$result = new KrashExpressionOperatorSubtraction($result, $expressionOpSubtract.result);}
        )*
    ;

expressionGlobal returns [KrashExpressionGlobal result]
    :   expressionGlobalChars
        {$result = new KrashExpressionGlobal($expressionGlobalChars.text.replace("$", ""));}
    ;

expressionGlobalChars
    :   GLOBAL
    ;

expressionIndex returns [KrashExpression result]
    :   SQBR1
        (
            expressionLitInt {$result = $expressionLitInt.result;}
            // NOTE: could add array indexes like in Python [0, 2, -1]
        |
            expressionLitString {$result = $expressionLitString.result;}
        |
            expressionRef {$result = $expressionRef.result;}
        )
        SQBR2
    ;

expressionInvoke returns [ArrayList<KrashExpression> result]
    :   {ArrayList<KrashExpression> args = new ArrayList<KrashExpression>();}
        STBR1
        (
            a1 = expression {args.add($a1.result);}
            (
                COMMA
                a2 = expression {args.add($a2.result);}
            )*
        )?
        STBR2
        {$result = args;}
    ;

expressionLit returns [KrashExpressionLiteral result]
    :   (
            expressionLitArray {$result = $expressionLitArray.result;}
        |
            expressionLitBoolean {$result = $expressionLitBoolean.result;}
        |
            expressionLitCallable {$result = $expressionLitCallable.result;}
        |
            expressionLitDouble {$result = $expressionLitDouble.result;}
        |
            expressionLitInt {$result = $expressionLitInt.result;}
        |
            expressionLitMap {$result = $expressionLitMap.result;}
        |
            expressionLitNull {$result = $expressionLitNull.result;}
        |
            expressionLitString {$result = $expressionLitString.result;}
        )
    ;

expressionLitArray returns [KrashExpressionLiteralArray result]
    :   {ArrayList<KrashExpression> data = new ArrayList();}
        SQBR1
        (
            e1 = expression {data.add($e1.result);}
            (
                COMMA
                e2 = expression {data.add($e2.result);}
            )*
        )?
        SQBR2
        {$result = new KrashExpressionLiteralArray(data);}
    ;

expressionLitBoolean returns [KrashExpressionLiteralBoolean result]
    :   (
            'false' {$result = new KrashExpressionLiteralBoolean(false);}
        |
            'true' {$result = new KrashExpressionLiteralBoolean(true);}
        )
    ;

expressionLitCallable returns [KrashExpressionLiteralCallable result]
    :   {
            ArrayList<KrashExpressionLiteralCallableArgument> args = new ArrayList();
            ArrayList<KrashExpressionLiteralCallableExpression> body = new ArrayList();
        }
        'fun'
        STBR1
        (
            arg1 = expressionLitCallableArg {args.add($arg1.result);}
            (
                COMMA
                arg2 = expressionLitCallableArg {args.add($arg2.result);}
            )*
        )?
        STBR2
        (
            CUBR1
            (
                {boolean isReturn = false;}
                (
                    'return' {isReturn = true;}
                )?
                exp1 = expression
                {body.add(new KrashExpressionLiteralCallableExpression($exp1.result, isReturn));}
            )*
            CUBR2
        |
            EQUAL exp2 = expression
            {body.add(new KrashExpressionLiteralCallableExpression($exp2.result, true));}
        )
        {$result = new KrashExpressionLiteralCallable(args, body);}
    ;

expressionLitCallableArg returns [KrashExpressionLiteralCallableArgument result]
    :   {KrashExpression defaultValue = null;}
        expressionRefChars
        (
            EQUAL expression
            {defaultValue = $expression.result;}
        )?
        {$result = new KrashExpressionLiteralCallableArgument($expressionRefChars.text, defaultValue);}
    ;

expressionLitDouble returns [KrashExpressionLiteralDouble result]
    :   {boolean minus = false;}
        (
            MINUS {minus = true;}
        )?
        expressionLitDoubleDigits
        {
            double value = Double.parseDouble($expressionLitDoubleDigits.text);
            if(minus) value = -value;
            $result = new KrashExpressionLiteralDouble(value);
        }
    ;

expressionLitDoubleDigits
    :   DIGIT+ FULLS DIGIT+
    ;

expressionLitInt returns [KrashExpressionLiteralInteger result]
    :   {boolean minus = false;}
        (
            MINUS {minus = true;}
        )?
        expressionLitIntDigits
        {
            int value = Integer.parseInt($expressionLitIntDigits.text);
            if(minus) value = -value;
            $result = new KrashExpressionLiteralInteger(value);
        }
    ;

expressionLitIntDigits
    :   DIGIT+
    ;

expressionLitMap returns [KrashExpressionLiteralMap result]
    :   {ArrayList<KrashExpressionLiteralMapPair> data = new ArrayList<KrashExpressionLiteralMapPair>();}
        CUBR1
        (
            p1 = expressionLitMapPair {data.add($p1.result);}
            (
                COMMA
                p2 = expressionLitMapPair {data.add($p2.result);}
            )*
        )?
        CUBR2
        {$result = new KrashExpressionLiteralMap(data);}
    ;

expressionLitMapPair returns [KrashExpressionLiteralMapPair result]
    :   k = expressionLitMapPairKey COLON v = expression
        {$result = new KrashExpressionLiteralMapPair($k.text, $v.result);}
    ;

expressionLitMapPairKey
    :   (ALPHA | DIGIT | UNDER)+
    ;

expressionLitNull returns [KrashExpressionLiteralNull result]
    :   'null' {$result = new KrashExpressionLiteralNull();}
    ;

expressionLitString returns [KrashExpressionLiteralString result]
    :   expressionLitStringChars
        {$result = new KrashExpressionLiteralString($expressionLitStringChars.text.replaceAll("\"", ""));}
    ;

expressionLitStringChars
    :   STRING
    ;

expressionMember returns [String result]
    :   FULLS expressionMemberChars
        {$result = $expressionMemberChars.text;}
    ;

expressionMemberChars
    :   (ALPHA | UNDER) (ALPHA | DIGIT | UNDER)*
    ;

expressionOpAdd returns [KrashExpression result]
    :   PLUS expression
        {$result = $expression.result;}
    ;

expressionOpDivide returns [KrashExpression result]
    :   SLASH expression
        {$result = $expression.result;}
    ;

expressionOpMultiply returns [KrashExpression result]
    :   ASTER expression
        {$result = $expression.result;}
    ;

expressionOpSubtract returns [KrashExpression result]
    :   MINUS expression
        {$result = $expression.result;}
    ;

expressionRef returns [KrashExpressionReference result]
    :   {Boolean byRef = false;}
        (
            AMPER {byRef = true;}
        )?
        expressionRefChars
        {$result = new KrashExpressionReference($expressionRefChars.text, byRef);}
    ;

expressionRefChars
    :   (ALPHA | UNDER) (ALPHA | DIGIT | UNDER)*
    ;

// Lexer Rules
ALPHA: [A-Za-z];
AMPER: '&';
APOST: '\'';
ASTER: '*';
AT: '@';
BANG: '!';
COLON: ':';
COMMA: ',';
CUBR1: '{';
CUBR2: '}';
DIGIT: [0-9];
EQUAL: '=';
FULLS: '.';
MINUS: '-';
PLUS: '+';
SLASH: '/';
SQBR1: '[';
SQBR2: ']';
STBR1: '(';
STBR2: ')';
STRING: '"' ~[\\"]* '"';
UNDER: '_';
WHITESPACE: [ \t\r\n]+ -> skip;
CHAR: ~[ "];
COMMENT_MULTI: '/*' .* '*/';
COMMENT_SINGLE: '//' ~[\r\n]*;
GLOBAL: '$' [A-Z]+;