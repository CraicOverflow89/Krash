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
            commandInvoke {$result = $commandInvoke.result;}
        )
    ;

commandDeclare returns [KrashCommandDeclare result]
    :   ref EQUAL value
        // NOTE: could add list[1] and map["key"] update syntax
        {$result = new KrashCommandDeclare($ref.result, $value.result);}
    ;

commandInvoke returns [KrashCommandInvoke result]
    :   {ArrayList<KrashValue> argList = new ArrayList();}
        commandInvokeMethod
        STBR1
        (
            arg1 = value {argList.add($arg1.result);}
            (
                COMMA arg2 = value {argList.add($arg2.result);}
            )*
        )?
        STBR2
        {$result = new KrashCommandInvoke($commandInvokeMethod.result, argList);}
    ;

commandInvokeMethod returns [KrashMethod result]
    :   (
            commandInvokeMethodNative {$result = new KrashMethodNative($commandInvokeMethodNative.result);}
        |
            value {$result = new KrashMethodValue($value.result);}
            // NOTE: need to use x.y notation for calling member functions
        )
    ;

commandInvokeMethodNative returns [KrashMethodNativeType result]
    :   (
            'echo' {$result = KrashMethodNativeType.ECHO;}
        |
            'file' {$result = KrashMethodNativeType.FILE;}
        )
    ;

ref returns [KrashReference result]
    :   refChars {$result = new KrashReference($refChars.text);}
        // NOTE: where to implement x.y notation (properties and methods) ??
    ;

refChars
    :   (ALPHA | DIGIT | UNDER)+
    ;

value returns [KrashValue result]
    :   (
            valueArray {$result = $valueArray.result;}
        |
            valueIndex {$result = $valueIndex.result;}
        |
            valueBoolean {$result = $valueBoolean.result;}
        |
            valueCallable {$result = $valueCallable.result;}
        |
            valueInteger {$result = $valueInteger.result;}
        |
            valueInvoke {$result = $valueInvoke.result;}
        |
            valueMap {$result = $valueMap.result;}
        |
            valueNull {$result = $valueNull.result;}
        |
            valueRef {$result = $valueRef.result;}
        |
            valueString {$result = $valueString.result;}
        )
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

valueCallable returns [KrashValueCallable result]
    :   'fun'
        {$result = new KrashValueCallable();}
    ;

valueIndex returns [KrashValueIndex result]
    :   valueRef SQBR1 valueIndexPos SQBR2
        // NOTE: what about array/map literal with index positions
        {$result = new KrashValueIndex($valueRef.result, $valueIndexPos.result);}
        // NOTE: need to change parseInt to class when parsing indexes like [0, 2, -1]
    ;

valueIndexPos returns [String result]
    :   (
            valueIndexPosDigits {$result = $valueIndexPosDigits.text;}
        |
            valueIndexPosKey {$result = $valueIndexPosKey.result;}
        )
    ;

valueIndexPosDigits
    :   DIGIT+
    ;

valueIndexPosKey returns [String result]
    :   QUOTE valueMapPairKey QUOTE
        {$result = $valueMapPairKey.text;}
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

valueInvoke returns [KrashValueInvoke result]
    :   {ArrayList<KrashValue> args = new ArrayList<KrashValue>();}
        valueRef
        STBR1
        (
            a1 = value {args.add($a1.result);}
            (
                COMMA
                a2 = value {args.add($a2.result);}
            )*
        )?
        STBR2
        {$result = new KrashValueInvoke($valueRef.result, args);}
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
        c1 = valueStringChars {buffer.append($c1.text);}
        (
            c2 = valueStringChars {buffer.append(" " + $c2.text);}
            // NOTE: this will not provide correct strings if double spacing or tabs appear
            //       not even a single space is appearing atm (just disappearing as whitespace)
        )*
        QUOTE
        {$result = new KrashValueString(buffer.toString());}
    ;

valueStringChars
    :   (ALPHA | AMPER | APOST | CHAR | COLON | COMMA | CUBR1 | CUBR2 | DIGIT | EQUAL | MINUS | SQBR1 | SQBR2 | STBR1 | STBR2 | UNDER)+
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
MINUS: '-';
NLINE: [\n];
PLUS: '+';
SQBR1: '[';
SQBR2: ']';
STBR1: '(';
STBR2: ')';
UNDER: '_';
WHITESPACE: [ \t\r\n]+ -> skip;
QUOTE: '"';
CHAR: ~[ "];