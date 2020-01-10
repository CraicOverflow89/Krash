grammar Krash;

@header {
    import craicoverflow89.krash.components.*;
    import craicoverflow89.krash.components.expressions.*;
    import craicoverflow89.krash.components.objects.KrashValueClassModifier;
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
        |
            commandFunction {$result = $commandFunction.result;}
        )
    ;

commandComment returns [KrashCommandComment result]
    :   (
            commandCommentMulti
            {$result = new KrashCommandComment($commandCommentMulti.text.replace("/*", "").replace("*/", "").trim());}
        |
            commandCommentSingle
            {$result = new KrashCommandComment($commandCommentSingle.text.replace("//", "").trim());}
        )
    ;

commandCommentMulti
    :   COMMENT_MULTI
    ;

commandCommentSingle
    :   COMMENT_SINGLE
    ;

commandDeclare returns [KrashCommandDeclare result]
    :   commandDeclareRef EQUAL expression
        {$result = new KrashCommandDeclare($commandDeclareRef.result, $expression.result);}
    ;

commandDeclareRef returns [KrashCommandDeclareReference result]
    :   (
            commandDeclareRefIndex {$result = $commandDeclareRefIndex.result;}
        |
            commandDeclareRefSimple {$result = $commandDeclareRefSimple.result;}
        )
    ;

commandDeclareRefIndex returns [KrashCommandDeclareReferenceIndex result]
    :   expressionRef
        {KrashExpression expression = $expressionRef.result;}
        (
            indexMulti = expressionIndex {expression = new KrashExpressionIndex(expression, $indexMulti.result);}
        )*
        (
            indexFinal = expressionIndex
        )+
        {$result = new KrashCommandDeclareReferenceIndex(expression, $indexFinal.result);}
    ;

commandDeclareRefSimple returns [KrashCommandDeclareReferenceSimple result]
    :   expressionRefChars
        {$result = new KrashCommandDeclareReferenceSimple($expressionRefChars.text);}
    ;

commandExpression returns [KrashCommandExpression result]
    :   expression
        {$result = new KrashCommandExpression($expression.result);}
    ;

commandFunction returns [KrashCommandFunction result]
    :   'fun'
        name = expressionLitCallableNameChars
        args = expressionLitCallableArgList
        body = expressionLitCallableBody
        {$result = new KrashCommandFunction($name.text, $args.result, $body.result);}
    ;

expression returns [KrashExpression result]
    :   (
            expressionBody {$result = $expressionBody.result;}
        |
            expressionBlock {$result = $expressionBlock.result;}
        )
        (
            expressionCoEqual {$result = new KrashExpressionConditionEquality($result, $expressionCoEqual.result);}
        |
            expressionCoGreater {$result = new KrashExpressionConditionGreater($result, $expressionCoGreater.result);}
        |
            expressionCoInequal {$result = new KrashExpressionConditionInequality($result, $expressionCoInequal.result);}
        |
            expressionCoLesser {$result = new KrashExpressionConditionLesser($result, $expressionCoLesser.result);}
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

expressionBlock returns [KrashExpression result]
    :   {
            Boolean toString = false;
            Boolean negate = false;
        }
        (
            AT {toString = true;}
        )?
        (
            expressionOpNegation {negate = true;}
        )?
        STBR1
        expression {$result = $expression.result;}
        STBR2
        {
            $result = new KrashExpressionData($result, toString);
            if(negate) $result = new KrashExpressionOperatorNegation($result);
        }
    ;

expressionBody returns [KrashExpression result]
    :   {
            Boolean toString = false;
            Boolean negate = false;
        }
        (
            AT {toString = true;}
        )?
        (
            expressionOpNegation {negate = true;}
        )?
        expressionData
        {$result = $expressionData.result;}
        (
            expressionIndex {$result = new KrashExpressionIndex($result, $expressionIndex.result);}
        |
            expressionInvoke {$result = new KrashExpressionInvoke($result, $expressionInvoke.result);}
        |
            expressionMember {$result = new KrashExpressionMember($result, $expressionMember.result);}
        )*
        {
            $result = new KrashExpressionData($result, toString);
            if(negate) $result = new KrashExpressionOperatorNegation($result);
        }
    ;

expressionCoEqual returns [KrashExpression result]
    :   EQUAL EQUAL expression
        {$result = $expression.result;}
    ;

expressionCoGreater returns [KrashExpression result]
    :   '>' expression
        {$result = $expression.result;}
    ;

expressionCoInequal returns [KrashExpression result]
    :   BANG EQUAL expression
        {$result = $expression.result;}
    ;

expressionCoLesser returns [KrashExpression result]
    :   '<' expression
        {$result = $expression.result;}
    ;

expressionData returns [KrashExpression result]
    :   (
            expressionGlobal {$result = $expressionGlobal.result;}
        |
            expressionInc {$result = $expressionInc.result;}
        |
            expressionLit {$result = $expressionLit.result;}
        |
            expressionRef {$result = $expressionRef.result;}
        |
            expressionStruct {$result = $expressionStruct.result;}
        )
    ;

expressionGlobal returns [KrashExpressionGlobal result]
    :   expressionGlobalChars
        {$result = new KrashExpressionGlobal($expressionGlobalChars.text.replace("$", ""));}
    ;

expressionGlobalChars
    :   GLOBAL
    ;

expressionInc returns [KrashExpression result]
    :   (
            e1 = expressionIncRef expressionOpIncrement
            {$result = new KrashExpressionOperatorIncrement($e1.result, KrashExpressionOperatorIncrementType.PLUS);}
        |
            e2 = expressionIncRef expressionOpDecrement
            {$result = new KrashExpressionOperatorIncrement($e2.result, KrashExpressionOperatorIncrementType.MINUS);}
        )
    ;

expressionIncRef returns [KrashExpressionOperatorIncrementValue result]
    :   (
            expressionIncRefIndex {$result = $expressionIncRefIndex.result;}
        |
            expressionIncRefSimple {$result = $expressionIncRefSimple.result;}
        )
    ;

expressionIncRefIndex returns [KrashExpressionOperatorIncrementIndex result]
    :   expressionRef
        {KrashExpression expression = $expressionRef.result;}
        (
            indexMulti = expressionIndex {expression = new KrashExpressionIndex(expression, $indexMulti.result);}
        )*
        indexFinal = expressionIndex
        {$result = new KrashExpressionOperatorIncrementIndex(expression, $indexFinal.result);}
    ;

expressionIncRefSimple returns [KrashExpressionOperatorIncrementReference result]
    :   expressionRefChars
        {$result = new KrashExpressionOperatorIncrementReference($expressionRefChars.text);}
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
            expressionLitClass {$result = $expressionLitClass.result;}
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
    :   'fun'
        args = expressionLitCallableArgList
        body = expressionLitCallableBody
        {$result = new KrashExpressionLiteralCallable($args.result, $body.result);}
    ;

expressionLitCallableArg returns [KrashExpressionLiteralCallableArgument result]
    :   {
            KrashExpressionLiteralCallableArgumentModifier modifier = KrashExpressionLiteralCallableArgumentModifier.NONE;
            KrashExpression defaultValue = null;
        }
        (
            AMPER {modifier = KrashExpressionLiteralCallableArgumentModifier.REF;}
        |
            AT {modifier = KrashExpressionLiteralCallableArgumentModifier.STRING;}
        )?
        expressionRefChars
        (
            EQUAL expression
            {defaultValue = $expression.result;}
        )?
        {$result = new KrashExpressionLiteralCallableArgument($expressionRefChars.text, defaultValue, modifier);}
    ;

expressionLitCallableArgList returns [ArrayList<KrashExpressionLiteralCallableArgument> result]
    :   {ArrayList<KrashExpressionLiteralCallableArgument> args = new ArrayList();}
        STBR1
        (
            arg1 = expressionLitCallableArg {args.add($arg1.result);}
            (
                COMMA
                arg2 = expressionLitCallableArg {args.add($arg2.result);}
            )*
        )?
        STBR2
        {$result = args;}
    ;

expressionLitCallableBody returns [ArrayList<KrashExpressionLiteralCallableExpression> result]
    :   {ArrayList<KrashExpressionLiteralCallableExpression> body = new ArrayList();}
        (
            CUBR1
            (
                {boolean isReturn = false;}
                (
                    'return' {isReturn = true;}
                )?
                c1 = command
                {body.add(new KrashExpressionLiteralCallableExpression($c1.result, isReturn));}
            )*
            CUBR2
        |
            EQUAL c2 = command
            {body.add(new KrashExpressionLiteralCallableExpression($c2.result, true));}
        )
        {$result = body;}
    ;

expressionLitCallableNameChars
    :   ALPHA (ALPHA | DIGIT | UNDER)*
    ;

expressionLitClass returns [KrashExpressionLiteralClass result]
    :   {KrashExpressionLiteralClassInherit inherit = null;}
        mod = expressionLitClassModifier
        'class'
        name = expressionLitClassNameChars
        args = expressionLitCallableArgList
        (
            COLON
            inherit = exporessionLitClassInherit
            {inherit = $inherit.result;}
        )?
        CUBR1
        body = expressionLitClassBody
        CUBR2
        {$result = new KrashExpressionLiteralClass($name.text, $mod.result, $args.result, inherit, $body.result);}
    ;

expressionLitClassBody returns [ArrayList<KrashExpressionLiteralClassExpression> result]
    :   {ArrayList<KrashExpressionLiteralClassExpression> body = new ArrayList();}
        (
            commandComment {body.add(new KrashExpressionLiteralClassExpression($commandComment.result));}
        |
            commandDeclare {body.add(new KrashExpressionLiteralClassExpression($commandDeclare.result));}
        |
            commandFunction {body.add(new KrashExpressionLiteralClassExpression($commandFunction.result));}
        )*
        {$result = body;}
    ;

exporessionLitClassInherit returns [KrashExpressionLiteralClassInherit result]
    :   name = expressionLitClassNameChars
        args = expressionInvoke
        {$result = new KrashExpressionLiteralClassInherit($name.text, $args.result);}
    ;

expressionLitClassModifier returns [KrashValueClassModifier result]
    :   {KrashValueClassModifier modifier = KrashValueClassModifier.NONE;}
        (
            'abstract' {modifier = KrashValueClassModifier.ABSTRACT;}
        |
            'open' {modifier = KrashValueClassModifier.OPEN;}
        )?
        {$result = modifier;}
    ;

expressionLitClassNameChars
    :   ALPHA (ALPHA | DIGIT | UNDER)*
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
        {
            String text = $expressionLitStringChars.text.replaceAll("\\\\\"", "\"");
            $result = new KrashExpressionLiteralString(text.substring(1, text.length() - 1));
        }
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

expressionOpDecrement
    :   MINUS MINUS
    ;

expressionOpDivide returns [KrashExpression result]
    :   SLASH expression
        {$result = $expression.result;}
    ;

expressionOpIncrement
    :   PLUS PLUS
    ;

expressionOpMultiply returns [KrashExpression result]
    :   ASTER expression
        {$result = $expression.result;}
    ;

expressionOpNegation
    :   BANG
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

expressionStruct returns [KrashExpressionStructure result]
    :   (
            expressionStructIf {$result = $expressionStructIf.result;}
        |
            expressionStructWhile {$result = $expressionStructWhile.result;}
        )
    ;

expressionStructIf returns [KrashExpressionStructureIf result]
    :   {
            ArrayList<KrashCommand> bodyTrue = new ArrayList();
            ArrayList<KrashCommand> bodyElse = new ArrayList();
        }
        'if' STBR1 condition = expression STBR2
        (
            b1 = command {bodyTrue.add($b1.result);}
        |
            CUBR1
            (
                b2 = command {bodyTrue.add($b2.result);}
            )*
            CUBR2
        )
        (
            'else'
            (
                e1 = command {bodyElse.add($e1.result);}
            |
                CUBR1
                (
                    e2 = command {bodyElse.add($e2.result);}
                )*
                CUBR2
            )
        )?
        {$result = new KrashExpressionStructureIf($condition.result, bodyTrue, bodyElse);}
    ;

expressionStructWhile returns [KrashExpressionStructureWhile result]
    :   {ArrayList<KrashCommand> body = new ArrayList();}
        'while' STBR1 condition = expression STBR2
        (
            b1 = command {body.add($b1.result);}
        |
            CUBR1
            (
                b2 = command {body.add($b2.result);}
            )*
            CUBR2
        )
        {$result = new KrashExpressionStructureWhile($condition.result, body);}
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
PIPE: '|';
PLUS: '+';
SLASH: '/';
SQBR1: '[';
SQBR2: ']';
STBR1: '(';
STBR2: ')';
STRING: '"' (~[\\"] | '\\' .)* '"';
UNDER: '_';
WHITESPACE: [ \t\r\n]+ -> skip;
CHAR: ~[ "];
COMMENT_MULTI: '/*' .*? '*/';
COMMENT_SINGLE: '//' ~[\r\n]*;
GLOBAL: '$' [A-Z]+;