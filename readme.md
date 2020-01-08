Krash Project
=============

Lightweight DSL for file manipulation (and more, later), built in Kotlin.

### Tasks

 - need to make it possible to wrap expressions in parens (so maths will be done in certain order)
 - custom parser error handling (without default printing to console)
 - should allow setting map/array values with `map[key] = value` and `array[pos] = value` syntax
 - returned pair/list can be split into multiple variables like `(x, z) = getCoords()`
 - commands for file/directory moving/copying (recursion flag)
   - create
   - copy
   - move
   - rename
   - delete
   - read
   - write
 - all `KrashValue` classes should have private values and methods to get them
 - string buffer object
 - indexes for arrays/strings should be more capable like `list[2, 6, 2]` (start, end, step)
 - provide meta data access for comments?
 - formatted string (able to use references inside)

### Issues

 - loading script does not allow for absolute paths
 - references don't persist with `name = &data["name"]` since moving to experssions
 - need to configure callable arguments to work with byRef
 - trying to use `true` as reference throws parser exception instead of being handled

### See Also

 - [Krash Extension for Visual Studio Code](https://github.com/CraicOverflow89/VSC-Krash-Language)