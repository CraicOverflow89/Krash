Krash Project
=============

Lightweight DSL for file manipulation (and more, later), built in Kotlin.

### Tasks

 - global constants that cannot be overwritten and always exist
   - cwd (file)
   - home (file)
 - returned pair/list can be split into multiple variables like `(x, z) = getCoords()`
 - prevent reserved words being used for map keys?
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
 - replace loose calls to `println` with a runtime method
 - all `KrashValue` classes should have private values and methods to get them
 - string buffer object
 - indexes for arrays/strings should be more capable like `list[2, 6, 2]` (start, end, step)
 - other stuff
   - vsc language pack
 - update runtime/heap stuff (don't need separate cwd for function scope)

### Issues

 - references don't persist with `name = &data["name"]` since moving to experssions
 - spaces in string literals are causing issues (whitespace)
 - trying to use `true` as reference throws parser exception instead of being handled
 - the map `contains` member function needs finishing