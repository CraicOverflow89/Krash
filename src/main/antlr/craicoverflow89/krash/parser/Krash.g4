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
            ref {$result = new KrashMethodReference($ref.result);}
            // NOTE: need to use x.y notation for calling member functions
        )
    ;

commandInvokeMethodNative returns [KrashMethodNativeType result]
    :   (
            'echo' {$result = KrashMethodNativeType.ECHO;}
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
            valueBoolean {$result = $valueBoolean.result;}
        |
            valueInteger {$result = $valueInteger.result;}
        |
            valueNull {$result = $valueNull.result;}
        |
            valueRef {$result = $valueRef.result;}
        |
            valueString {$result = $valueString.result;}
        )
    ;

valueBoolean returns [KrashValueBoolean result]
    :   (
            'false' {$result = new KrashValueBoolean(false);}
        |
            'true' {$result = new KrashValueBoolean(true);}
        )
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
    :   (ALPHA | AMPER | CHAR | COMMA | CUBR1 | CUBR2 | DIGIT | EQUAL | MINUS | SQBR1 | SQBR2 | STBR1 | STBR2 | UNDER)+
    ;

// Lexer Rules
ALPHA: [A-Za-z];
AMPER: '&';
COMMA: ',';
CUBR1: '{';
CUBR2: '}';
DIGIT: [0-9];
EQUAL: '=';
MINUS: '-';
NLINE: [\n];
SQBR1: '[';
SQBR2: ']';
STBR1: '(';
STBR2: ')';
UNDER: '_';
WHITESPACE: [ \t\r\n]+ -> skip;
QUOTE: '"';
CHAR: ~[ "];