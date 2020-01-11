Krash Project
=============

[![Known Vulnerabilities](https://snyk.io//test/github/CraicOverflow89/Krash/badge.svg?targetFile=build.gradle)](https://snyk.io//test/github/CraicOverflow89/Krash?targetFile=build.gradle)

Scripting language with a sleek syntax and focus on functional file system operations, built with Kotlin and ANTLR. Source code can be distributed to different operating systems, with no compilation required.

### Features

 - runs on any operating system with JVM
 - interpreted language
 - dynamic with type inference
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
callable = fun(name) {
    echo("Hello $name")
}
```

Default values for arguments can be set using the `=` character. Note that functions can also be defined in the older style (where `hello` is the same as `callable` reference above, when used elsewhere);

```
fun hello(name = "James") {
    echo("Hello $name")
}
```

When the function contains just one expression, it can be shortened to the following;

```
fun(name = "James") = echo("Hello $name")
```

#### String Concatenation

Multiple `string` values can be combined using the `+` operator. Simple references can be included within (and cast to) a `string` using the `$` character;

```
message = "Hello $name!"
// is the same as
message = "Hello " + name + "!"
```

#### String Modifier

In addition to the `value.toString()` method, you can use the `@value` modifier to cast values to `string` type, for example;

```
echo("list contents = " + @list)
// is the same as
echo("list contents = $list")
// is the same as
echo("list contents = " list.toString())

@list.size()
// the modifier casts the entire expression
// so this would result in the list size (int) being cast to string

list.toString()
// here we are just casting the list (array) to a string
```

This can also be used in function arguments, like so;

```
map = {name: "James", age: 30}
map.filter(fun(k, @v) {
    // map values are cast to string here
    return v.startsWith("J")
})
```

You can cast expression blocks, like so;

```
result = @(7 * 5)
// result is "35" (int cast to string)
```

#### Reference Modifier

You can maintain references using the `&` modifier;

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

#### Implicit It

When there is only one value passed to a function, it is available as `it` implicitly;

```
// making use of it
list.each(fun() {
    echo(it)
})

// does the same as
list.each(fun(value) {
    echo(value)
})
```

#### Invoke Syntax

There are a number of different ways to invoke a function, to suit different situations. When the final argument is a `callable` then it can be defined after the parens;

```
// Example callable
fun execute(array, callable) {}

// Passing logic callable argument after parens
execute(["apple", "orange"]) {
    echo(it)
}

// is the same as these
execute(["apple", "orange"], fun() = echo(it))
execute(["apple", "orange"], fun() {
    echo(it)
})
```

When the function being invoked only takes **one argument of type callable**, you don't need to include any parens;

```
// The let method takes one argument of type callable
// value.let(callable)

// Invoking the let method with the single callable argument
"hello world".let {
    echo(it)
}

// is the same as
"hello world".let() {
    echo(it)
}
```

#### Control Structures

Krash supports standard _if / else_ statements;

```
// Single Expression
result = if(condition) "yes" else "no"
// if is an expression, so it can be used as a value

// Multiple Expressions
if(condition) {
    // logic here
}
else if(condition) {
    // logic here
}
else {
    // logic here
}
```

Standard _while_ loops;

```
// Single Expression
while(condition) logic()

// Multiple Expressions
while(condition) {

    // logic here
    logic()

    // jumps to next iteration
    continue

    // breaks out of loop
    break
}
```

There is no standard _for_ loop, as this exists in the form of member functions, for example;

```
// For element in array
array.each(echo)

// For element in array with index
array.eachIndexed(fun(i, v) {
    echo("value at position $i is $v")
})

// For key and value in map
map.each(fun(k, v) {
    echo("$k = $v")
})
```

The _when_ structure evaluates conditions and results against a value in place of an awkward _case/switch_ statement;

```
when(value) {
    condition1 -> result1
    condition2 -> result2
    else -> result3
}
``` 

#### Equality Operators

Conventional equality operators return `boolean` (can be used anywhere).

```
// Equal To
"abc" == "abc"
7 == 7

// Not Equal
"abc" != "def"
7 != 5

// Greater Than
7 > 5

// Lesser Than
5 < 7
```

#### Mathematical Operators

Conventional mathematical operators work with both `integer` and `double` values. The operation will result in an `integer` where possible, otherwise a `double` will be returned.

```
// Addition
7 + 5.0

// Subtraction
7 - 5.0

// Multiplication
7 * 5.0

// Division
7 / 5.0
```

#### Boolean Negation

It is possible to flip between `true` and `false` for boolean values like this;

```
notTrue = !true
notFalse = !false
notEven = !(7 == 7)
```

#### Classes

Krash supports typical _classes_ and _single inheritance_. There is no _interface_ due to the dynamic nature of the language.

```
// Abstract classes cannot be instantiated themselves
abstract class Object(id) {

    fun printID() {
        echo(id)
    }

}

// Open classes can be extended by other classes
open class Person(id, name): Object(id) {

    fun hello() {
        echo("Hello $name")
    }

}

// Final classes cannot be extended
class Developer(id, name, lang = []): Person(id, name) {

    fun toMap() {
        return {id: id, name: name, lang: lang}
    }

}

// Instantiate the class
author = Developer(0, "James", ["Kotlin", "ANTLR"])
```

#### Enums

A set of constants can be assigned to an _enum_ with the conventional syntax;

```
enum Direction {
    EAST, NORTH, SOUTH, WEST
}

// Accessing values
Direction.NORTH

// Using string
Direction.valueOf("NORTH")
```

#### Script Features

When calling a script, you can access the command line arguments with the `$ARGS` global;

```
echo(
    "arguments: " + @$ARGS,
    "count:     " + @$ARGS.size(),
    "first arg: " + $ARGS[0]
)
```

Provided that the runtime _channel_ supports it (the default does), you can read text from the command line (will throw `KrashChannelException` if custom channel does not support this);

```
echo("Please enter your name;")
name = read()
echo("Welcome $name!")
```

### Tasks

 - returned pair/list can be split into multiple variables like `(x, z) = getCoords()`
 - file commands
   - create
   - move
   - rename
   - delete
   - read
   - write
 - all `KrashValue` classes should have private values and methods to get them
 - string buffer object
 - indexes for arrays/strings should be more capable like `list[2, 6, 2]` (start, end, step)
 - provide meta data access for comments?
 - replace use of `toString` in unit tests with checking actual value of objects
 - need to add built-in JSON parse/stringify methods
 - add "see article ↗➚⬈⬀ for more info" links to readme that go to documentation
 - for all tests
    - also need to add `comparison` test that checks conditional operators
    - check all operators for type
 - all `KrashValueObject` instances should be capable of equality checks
    - if object type is same (eg: file and file) which will require id
    - have a method that serialises properties to string to perform string check
 - combine the equality and inequality condition classes (not DRY atm)
 - equality checks for `KrashValueClass` instances?
 - write tests to check equality comparison of `KrashValueObject` instances
 - ability to _include_ other scripts
 - exceptions in the expression classes (eg: extending a final class) are not strictly runtime
    - should create a new exception type (they're not syntax issues or runtime issues)
    - see also `KrashCommandKeyword.invoke` where `KrashRuntimeException` is being thrown when it's a structural thing
 - ability to spawn processes (blocking and background possible)
 - use equality logic for `when` structure
 - update class command / expressions
     - anonymous classes (to drop directly into argument of function)
     - old style class command (not an expression)
 - argument list for short style callables `map.each {k, v -> echo("$k: $v")}`

### Issues

 - nested updates of arrays/maps are not working
    - setting value `ref[index][index] = value`
    - incrementing value `ref[index][index] ++`
 - references don't persist with `name = &data["name"]` since moving to expressions
 - need to configure callable arguments to work with byRef
 - trying to use `true` as reference throws parser exception instead of being handled
 - greedy member behaviour takes chars from next expression
    - eg: `n = Direction.NORTH` then `echo(n)` fails with `NORTHecho`

### See Also

 - [Language Extension for Visual Studio Code](https://github.com/CraicOverflow89/VSC-Krash-Language)
 - [Run Script Extension for Visual Studio Code](https://github.com/CraicOverflow89/VSC-Krash-Run-Script)