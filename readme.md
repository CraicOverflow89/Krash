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

#### String Concatenation

Multiple `string` values can be combined using the `+` operator. Simple references can be included within (and cast to) a `string` using the `$` character;

```
message = "Hello $name"
// is the same as
message = "Hello " + name + "!"
```

#### String Modifier

In addition to the `value.toString()` method, you can use the `@value` modifier to cast values to `string` type, for example;

```
echo("list contents = " + @list)
// is the same as
echo("list contents = $list)
// is the same as
echo("list contents = " list.toString())

@list.size()
// size of list, as a string
// the modifier casts the entire expression

list.toString().size
// list as string; get size property
// this is NOT the same as the above
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

#### Implicit It

When there is only one value passed to a function, it is available as `it` implicitly;

```
list.each(fun() {
    echo(it)
})

// does the same as
list.each(fun(value) {
    echo(value)
})
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
}
```

There is no standard _for_ loop, as this exists in the form of member functions, for example;

```
// For element in array
array.each(echo)

// For element in array with index
array.eachIndexed(fun(i, v) {
    echo("value at position " @i + " is " + @v)
})

// For key and value in map
map.each(fun(k, @v) {
    echo(k + " = " + v)
})
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

Conventional mathematical operators work with both `integer` and `double` values. The result of the operation will result in an `integer` where possible, otherwise a `double` will be returned.

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
true_value = true
false_value = !true_value
```

#### Shell Scripts

When calling a script, you can access the optional arguments passed along with the `$ARGS` global;

```
echo(
    "arguments: $ARGS",
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

 - need to make it possible to wrap expressions in parens (so maths will be done in certain order)
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

### Issues

 - nested updates of arrays/maps are not working
    - setting value `ref[index][index] = value`
    - incrementing value `ref[index][index] ++`
 - looks like single comments inside of multiline comments cause errors
 - loading script does not allow for absolute paths
    - there should be a single `isAbsolutePath` method (already have this logic in file constructor)
 - references don't persist with `name = &data["name"]` since moving to experssions
 - need to configure callable arguments to work with byRef
 - trying to use `true` as reference throws parser exception instead of being handled

### See Also

 - [Krash Extension for Visual Studio Code](https://github.com/CraicOverflow89/VSC-Krash-Language)