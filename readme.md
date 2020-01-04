Krash Project
=============

Lightweight DSL for file manipulation (and more, later), built in Kotlin.

### Tasks

 - callable literals
 - prevent reserved words being used for map keys?
 - commands for file/directory moving/copying (recursion flag)
 - look at removing `KrashReference` completely in favour of `KrashValueReference`
 - separate literals and expressions in parser
 - ensure that `null` is not being returned when errors should be thrown

### Issues

 - spaces in string literals are causing issues (whitespace)
 - trying to use `true` as reference throws parser exception instead of being handled