Krash Project
=============

[![Known Vulnerabilities](https://snyk.io//test/github/CraicOverflow89/Krash/badge.svg?targetFile=build.gradle)](https://snyk.io//test/github/CraicOverflow89/Krash?targetFile=build.gradle)

Lightweight file manipulation DSL, built with Kotlin and ANTLR.

### Features

 - runs on any operating system with JVM
 - interpreted language
 - uses type inference
 - uses object-oriented paradigm
 - functions are first class (both BIFs and UDFs)

#### Literals

Variables can be dynamically created with conventional literal syntax;

```
string = "chars"
int = 7
double = 7.7
bool = true
nothing = null
list = [0, "element"]
map = {key: "value"}
callable = fun(name = "James") {
    echo("Hello " + name)
}
```

#### String Modifier

In addition to the `value.toString()` method, you can use the `@value` modifier to cast values to strings, for example;

```
echo("list contents = " + @list)
```

This can also be used in function arguments, like so;

```
map = {name: "James", age: 30}
map.filter(fun(k, @v) {
    // map values are cast to string here
    return v.startsWith("J")
})
```

#### Reference Modifier

You can maintain references using the `&reference` modifier;

```
// exmaple list
list = [100, 200]

first_value = list[0]
// copies the value of 100
// changes to list have no effect on first_value
// changes to first_value have no effect on list

first_reference = &list[0]
// references the first element
// changes to list (at position 0) are respected
// changes to first_reference update list (position 0)
```

#### Control Structures

Krash supports standard _if / else_ and _while_ loops;

```
// If with single expression
result = if(condition) "yes" else "no"
// if is an expression, so it can be used as a value

// If with multiple expressions
if(condition) {
    // logic here
}
else {
    // logic here
}

// While with single expression
while(condition) logic()

// While with multiple expressions
while(condition) {
    // logic here
}
```

There is no standard _for_ loop, as this exists in the form of member functions, for example;

```
array.each(echo)
map.each(fun(k, @v) {
    echo(k + " = " + v)
})
```

### Tasks

 - need to make it possible to wrap expressions in parens (so maths will be done in certain order)
 - custom parser error handling (without default printing to console)
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
 - the lexer/parser logic isn't DRY (they're created in multiple places)
 - replace use of `toString` in unit tests with checking actual value of objects
 - need to add `!` boolean negation
 - need to add built-in JSON parse/stringify methods
 - need to add http get capabilities

### Issues

 - loading script does not allow for absolute paths
    - there should be a single `isAbsolutePath` method (already have this logic in file constructor)
 - references don't persist with `name = &data["name"]` since moving to experssions
 - need to configure callable arguments to work with byRef
 - trying to use `true` as reference throws parser exception instead of being handled

### See Also

 - [Krash Extension for Visual Studio Code](https://github.com/CraicOverflow89/VSC-Krash-Language)