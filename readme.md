Krash Project
=============

Lightweight DSL for file manipulation (and more, later), built in Kotlin.

### Tasks

 - need to make it possible to wrap expressions in parens (so maths will be done in certain order)
 - custom parser error handling (without default printing to console)
 - should allow setting map/array values with `map[key] = value` and `array[pos] = value` syntax
 - global constants that cannot be overwritten and always exist
   - cwd (file)
   - home (file)
 - returned pair/list can be split into multiple variables like `(x, z) = getCoords()`
 - commands for file/directory moving/copying (recursion flag)
   - create
   - copy
   - move
   - rename
   - delete
   - read
   - write
 - look at removing `KrashReference` completely in favour of `KrashValueReference`
 - ensure that `null` is not being returned when errors should be thrown
 - all `KrashValue` classes should have private values and methods to get them
 - string buffer object
 - indexes for arrays/strings should be more capable like `list[2, 6, 2]` (start, end, step)
 - allow `@` and `*` chars in comments and provide meta data access
 - formatted string (able to use references inside)

### Issues

 - should allow any characters in comments (regardless of parser recognising pieces of code)
 - spaces in string literals are causing issues (whitespace)
 - loading script does not allow for absolute paths
 - references don't persist with `name = &data["name"]` since moving to experssions
 - need to configure callable arguments to work with byRef
 - trying to use `true` as reference throws parser exception instead of being handled
 - the map `contains` member function needs finishing

### See Also

 - [Krash Extension for Visual Studio Code](https://github.com/CraicOverflow89/VSC-Krash-Language)