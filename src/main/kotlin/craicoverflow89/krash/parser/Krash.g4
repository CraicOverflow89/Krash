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
    :   {ArrayList<KrashCommandInvokeArgument> argList = new ArrayList();}
        commandInvokeMethod
        STBR1
        (
            a1 = commandInvokeArgument {argList.add($a1.result);}
            (
                COMMA a2 = commandInvokeArgument {argList.add($a2.result);}
            )*
        )?
        STBR2
        {$result = new KrashCommandInvoke($commandInvokeMethod.result, argList);}
    ;

commandInvokeArgument returns [KrashCommandInvokeArgument result]
    :   value {$result = new KrashCommandInvokeArgument($value.result);}
    ;

commandInvokeMethod returns [KrashMethod result]
    :   (
            commandInvokeMethodNative {$result = new KrashMethodNative($commandInvokeMethodNative.result);}
        |
            ref {$result = new KrashMethodReference($ref.result);}
        )
        // NOTE: not so much in need of references to UDFs
        //       need to handle method name as strings when looking-up existing member functions
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
            valueInteger {$result = $valueInteger.result;}
        |
            valueRef {$result = $valueRef.result;}
        |
            valueString {$result = $valueString.result;}
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

valueRef returns [KrashValueReference result]
    :   ref {$result = new KrashValueReference($ref.result);}
    ;

valueString returns [KrashValueString result]
    :   {StringBuffer buffer = new StringBuffer();}
        QUOTE
        c1 = valueStringChars {buffer.append($c1.text);}
        (
            c2 = valueStringChars {buffer.append(" " + $c2.text);}
            // NOTE: this will not provide correct strings if double spacing or tabs appear
        )*
        QUOTE
        {$result = new KrashValueString(buffer.toString());}
    ;

valueStringChars
    :   (ALPHA | CHAR | COMMA | CUBR1 | CUBR2 | DIGIT | EQUAL | MINUS | SQBR1 | SQBR2 | STBR1 | STBR2 | UNDER)+
    ;

// Lexer Rules
ALPHA: [A-Za-z];
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