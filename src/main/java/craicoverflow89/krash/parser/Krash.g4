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
            commandEnum {$result = $commandEnum.result;}
        |
            commandExpression {$result = $commandExpression.result;}
        |
            commandFunction {$result = $commandFunction.result;}
        |
            commandKeyword {$result = $commandKeyword.result;}
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

commandEnum returns [KrashCommandEnum result]
    :   {ArrayList<String> valueList = new ArrayList();}
        'enum'
        name = commandEnumNameChars
        CUBR1
        (
            v1 = commandEnumValueChars {valueList.add($v1.text);}
            (
                COMMA
                v2 = commandEnumValueChars {valueList.add($v2.text);}
            )*
        )
        CUBR2
        {$result = new KrashCommandEnum($name.text, valueList);}
    ;

commandEnumNameChars
    :   ALPHA_U (ALPHA_L | ALPHA_U | DIGIT | UNDER)*
    ;

commandEnumValueChars
    :   ALPHA_U+
    ;

commandExpression returns [KrashCommandExpression result]
    :   {Boolean isReturn = false;}
        (
            'return' {isReturn = true;}
        )?
        expression
        {
            if(isReturn) $result = new KrashCommandExpression(new KrashExpressionReturn($expression.result));
            else $result = new KrashCommandExpression($expression.result);
        }
    ;

commandFunction returns [KrashCommandFunction result]
    :   'fun'
        name = expressionLitCallableNameChars
        args = expressionLitCallableArgList
        body = expressionLitCallableBody
        {$result = new KrashCommandFunction($name.text, $args.result, $body.result);}
    ;

commandKeyword returns [KrashCommandKeyword result]
    :   (
            'break' {$result = new KrashCommandKeyword(KrashCommandKeywordType.BREAK);}
        |
            'continue' {$result = new KrashCommandKeyword(KrashCommandKeywordType.CONTINUE);}
        )
    ;

expression returns [KrashExpression result]
    :   (
            expressionBody {$result = $expressionBody.result;}
        |
            expressionBlock {$result = $expressionBlock.result;}
        )
        (
            expressionCoE {$result = new KrashExpressionConditionEquality($result, $expressionCoE.result);}
        |
            expressionCoGT {$result = new KrashExpressionConditionGreaterThan($result, $expressionCoGT.result);}
        |
            expressionCoGTE {$result = new KrashExpressionConditionGreaterEqual($result, $expressionCoGTE.result);}
        |
            expressionCoIs {$result = new KrashExpressionConditionIs($result, $expressionCoIs.result);}
        |
            expressionCoLT {$result = new KrashExpressionConditionLesserThan($result, $expressionCoLT.result);}
        |
            expressionCoLTE {$result = new KrashExpressionConditionLesserEqual($result, $expressionCoLTE.result);}
        |
            expressionCoNE {$result = new KrashExpressionConditionInequality($result, $expressionCoNE.result);}
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

expressionCoE returns [KrashExpression result]
    :   EQUAL EQUAL expression
        {$result = $expression.result;}
    ;

expressionCoGT returns [KrashExpression result]
    :   '>' expression
        {$result = $expression.result;}
    ;

expressionCoGTE returns [KrashExpression result]
    :   '>=' expression
        {$result = $expression.result;}
    ;

expressionCoIs returns [String result]
    :   'is' expressionRefChars
        {$result = $expressionRefChars.text;}
    ;

expressionCoLT returns [KrashExpression result]
    :   '<' expression
        {$result = $expression.result;}
    ;

expressionCoLTE returns [KrashExpression result]
    :   '<=' expression
        {$result = $expression.result;}
    ;

expressionCoNE returns [KrashExpression result]
    :   BANG EQUAL expression
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
    :   {ArrayList<KrashExpression> args = new ArrayList();}
        (
            STBR1
            (
                a1 = expression {args.add($a1.result);}
                (
                    COMMA
                    a2 = expression {args.add($a2.result);}
                )*
            )?
            STBR2
            (
                s1 = expressionInvokeShort
                {args.add($s1.result);}
            )?
        |
            s2 = expressionInvokeShort
            {args.add($s2.result);}
        )
        {$result = args;}
    ;

expressionInvokeShort returns [KrashExpressionLiteralCallable result]
    :   {
            ArrayList<KrashExpressionLiteralCallableArgument> args = new ArrayList();
            ArrayList<KrashCommand> exp = new ArrayList();
        }
        CUBR1
        (
            arg1 = expressionLitCallableArg {args.add($arg1.result);}
            (
                COMMA
                arg2 = expressionLitCallableArg {args.add($arg2.result);}
            )*
            '->'
        )?
        (
            c1 = command {exp.add($c1.result);}
        )*?
        c2 = expression
        {exp.add(new KrashCommandExpression(new KrashExpressionReturn($c2.result)));}
        CUBR2
        {$result = new KrashExpressionLiteralCallable(args, exp);}
    ;

expressionLit returns [KrashExpressionLiteral result]
    :   (
            expressionLitArray {$result = $expressionLitArray.result;}
        |
            expressionLitBoolean {$result = $expressionLitBoolean.result;}
        |
            expressionLitCallable {$result = $expressionLitCallable.result;}
        |
            /*expressionLitClass {$result = $expressionLitClass.result;}
        |*/
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

expressionLitCallableBody returns [ArrayList<KrashCommand> result]
    :   {ArrayList<KrashCommand> body = new ArrayList();}
        (
            CUBR1
            (
                c1 = command
                {body.add($c1.result);}
            )*
            CUBR2
        |
            EQUAL c2 = expression
            {body.add(new KrashCommandExpression(new KrashExpressionReturn($c2.result)));}
        )
        {$result = body;}
    ;

expressionLitCallableNameChars
    :   ALPHA_L (ALPHA_L | ALPHA_U | DIGIT | UNDER)*
    ;

/*expressionLitClass returns [KrashExpressionLiteralClass result]
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
            commandComment {body.add(new KrashExpressionLiteralClassExpressionComment($commandComment.result));}
        |
            method = expressionLitClassBodyMethod {body.add($method.result);}
        |
            property = expressionLitClassBodyProperty {body.add($property.result);}
        )*
        {$result = body;}
    ;

expressionLitClassBodyMethod returns [KrashExpressionLiteralClassExpressionMethod result]
    :   'fun'
        name = expressionLitCallableNameChars
        args = expressionLitCallableArgList
        body = expressionLitCallableBody
        {$result = new KrashExpressionLiteralClassExpressionMethod($name.text, new KrashExpressionLiteralCallable($args.result, $body.result));}
    ;

expressionLitClassBodyProperty returns [KrashExpressionLiteralClassExpressionProperty result]
    :   expressionRefChars EQUAL expression
        {$result = new KrashExpressionLiteralClassExpressionProperty($expressionRefChars.text, $expression.result);}
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
    :   ALPHA_U (ALPHA_L | ALPHA_U | DIGIT | UNDER)*
    ;*/

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
    :   (ALPHA_L | ALPHA_U | DIGIT | UNDER)+
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
    :   (ALPHA_L | ALPHA_U | UNDER) (ALPHA_L | ALPHA_U | DIGIT | UNDER)*
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
    :   (ALPHA_L | ALPHA_U | UNDER) (ALPHA_L | ALPHA_U | DIGIT | UNDER)*
    ;

expressionStruct returns [KrashExpressionStructure result]
    :   (
            expressionStructIf {$result = $expressionStructIf.result;}
        |
            expressionStructWhen {$result = $expressionStructWhen.result;}
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

expressionStructWhen returns [KrashExpressionStructureWhen result]
    :   {
            ArrayList<KrashExpressionStructureWhenCase> caseList = new ArrayList();
            KrashExpression expElse = null;
        }
        'when' STBR1 value = expression STBR2
        CUBR1
        (
            expCondition = expression '->' expResult = expression
            {caseList.add(new KrashExpressionStructureWhenCase($expCondition.result, $expResult.result));}
        |
            'else' '->' expElse = expression
            {expElse = $expElse.result;}
        )+
        CUBR2
        {$result = new KrashExpressionStructureWhen($value.result, caseList, expElse);}
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
ALPHA_L: [a-z];
ALPHA_U: [A-Z];
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