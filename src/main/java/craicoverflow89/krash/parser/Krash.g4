grammar Krash;

@header {
    import craicoverflow89.krash.components.*;
    import java.lang.StringBuffer;
    import java.util.ArrayList;
}

// Parser Rules
script returns [KrashScript result]
    :   {ArrayList<KrashCommand> data = new ArrayList();}
        (
            c1 = command {data.add($c1.result);}
            (
                NLINE c2 = command {data.add($c2.result);}
            )*
        )?
        EOF
        {$result = new KrashScript(data);}
    ;

line returns [KrashCommand result]
    :   command {$result = $command.result;}
        EOF
    ;

command returns [KrashCommand result]
    :   (
            commandDeclare {$result = $commandDeclare.result;}
        |
            commandValue {$result = $commandValue.result;}
        )
    ;

commandDeclare returns [KrashCommandDeclare result]
    :   ref EQUAL value
        // NOTE: could add list[1] and map["key"] update syntax
        {$result = new KrashCommandDeclare($ref.result, $value.result);}
    ;

commandValue returns [KrashCommandValue result]
    :   value {$result = new KrashCommandValue($value.result);}
    ;

ref returns [KrashReference result]
    :   refChars {$result = new KrashReference($refChars.text);}
        // NOTE: where to implement x.y notation (properties and methods) ??
    ;

refChars
    :   (ALPHA | UNDER) (ALPHA | DIGIT | UNDER)*
    ;

value returns [KrashValue result]
    :   (
            valueArray {$result = $valueArray.result;}
        |
            valueBoolean {$result = $valueBoolean.result;}
        |
            valueCallable {$result = $valueCallable.result;}
        |
            valueInteger {$result = $valueInteger.result;}
        |
            valueMap {$result = $valueMap.result;}
        |
            valueNull {$result = $valueNull.result;}
        |
            valueRef {$result = $valueRef.result;}
        |
            valueString {$result = $valueString.result;}
        )
        (
            valueIndex {$result = new KrashValueIndex($result, $valueIndex.result);}
        |
            valueInvoke {$result = new KrashValueInvoke($result, $valueInvoke.result);}
        |
            valueMember {$result = new KrashValueMember($result, $valueMember.result);}
        )*
    ;

valueArray returns [KrashValueArray result]
    :   {ArrayList<KrashValue> data = new ArrayList();}
        SQBR1
        (
            v1 = value {data.add($v1.result);}
            (
                COMMA
                v2 = value {data.add($v2.result);}
            )*
        )?
        SQBR2
        {$result = new KrashValueArray(data);}
    ;

valueBoolean returns [KrashValueBoolean result]
    :   (
            'false' {$result = new KrashValueBoolean(false);}
        |
            'true' {$result = new KrashValueBoolean(true);}
        )
    ;

/*
valueCallable returns [KrashValueCallable result]
    :   'fun'
        {$result = new KrashValueCallable();}
    ;
*/

// NOTE: just doing this for now
valueCallable returns [KrashValue result]
    :   'fun'
        {$result = new KrashValueNull();}
    ;

valueIndex returns [KrashValueIndexPos result]
    :   SQBR1
        (
            valueInteger {$result = $valueInteger.result;}
            // NOTE: could add array indexes like in Python [0, 2, -1]
        |
            valueRef {$result = $valueRef.result;}
        |
            valueString {$result = $valueString.result;}
        )
        SQBR2
    ;

valueInteger returns [KrashValueInteger result]
    :   {boolean minus = false;}
        (
            MINUS {minus = true;}
        )?
        valueIntegerDigits
        {
            int value = Integer.parseInt($valueIntegerDigits.text);
            if(minus) value = -value;
            $result = new KrashValueInteger(value);
        }
    ;

valueIntegerDigits
    :   DIGIT+
    ;

valueInvoke returns [ArrayList<KrashValue> result]
    :   {ArrayList<KrashValue> args = new ArrayList<KrashValue>();}
        STBR1
        (
            a1 = value {args.add($a1.result);}
            (
                COMMA
                a2 = value {args.add($a2.result);}
            )*
        )?
        STBR2
        {$result = args;}
    ;

valueMap returns [KrashValueMap result]
    :   {ArrayList<KrashValueMapPair> data = new ArrayList<KrashValueMapPair>();}
        CUBR1
        (
            v1 = valueMapPair {data.add($v1.result);}
            (
                COMMA
                v2 = valueMapPair {data.add($v2.result);}
            )*
        )?
        CUBR2
        {$result = new KrashValueMap(data);}
    ;

valueMapPair returns [KrashValueMapPair result]
    :   k = valueMapPairKey COLON v = value
        {$result = new KrashValueMapPair($k.text, $v.result);}
    ;

valueMapPairKey
    :   (ALPHA | DIGIT | UNDER)+
    ;

valueMember returns [KrashValue result]
    :   FULLS value
        {$result = $value.result;}
    ;

valueNull returns [KrashValueNull result]
    :   'null' {$result = new KrashValueNull();}
    ;

valueRef returns [KrashValueReference result]
    :   {boolean byRef = false;}
        (
            AMPER {byRef = true;}
        )?
        ref {$result = new KrashValueReference($ref.result, byRef);}
    ;

valueString returns [KrashValueString result]
    :   {StringBuffer buffer = new StringBuffer();}
        QUOTE
        (
            chars = valueStringChars {buffer.append($chars.text);}
        )*
        QUOTE
        {$result = new KrashValueString(buffer.toString());}
    ;

valueStringChars
    :   (ALPHA | AMPER | APOST | CHAR | COLON | COMMA | CUBR1 | CUBR2 | DIGIT | EQUAL | FULLS | MINUS | SPACE | SQBR1 | SQBR2 | STBR1 | STBR2 | UNDER)+
    ;

// Lexer Rules
ALPHA: [A-Za-z];
AMPER: '&';
APOST: '\'';
COLON: ':';
COMMA: ',';
CUBR1: '{';
CUBR2: '}';
DIGIT: [0-9];
EQUAL: '=';
FULLS: '.';
MINUS: '-';
NLINE: [\n];
PLUS: '+';
SQBR1: '[';
SQBR2: ']';
STBR1: '(';
STBR2: ')';
UNDER: '_';
WHITESPACE: [ \t\r\n]+ -> skip;
SPACE: [ ]+;
QUOTE: '"';
CHAR: ~[ "];