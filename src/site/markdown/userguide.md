# Bindings

The following binding types can be defined in a binding model:

## Elements

#### SimpleType

#### SimpleFileType

#### SimpleArgument

#### Ignore

#### Choice

#### ComplexType

#### Element

#### ElementInjector

#### MapRepeater

#### Recursor

#### Reference

#### Repeater

#### Root

#### Sequence

## Attributes
An attribute is optional by default. When it is marked as required, it will
force an empty optional element that it's bound to, to be marshalled.

#### Attribute
An ***Attribute*** binds an xml attribute to Java. The value can be transformed to and from a specific Java type using a value converter. If no value converter is applied, the value is exchanged as Java String. An attribute is optional by
default, but it can be marked as required. A required attribute will be marshalled, even if there is no Java value. Currently, missing required attributes do not abort the unmarshalling with an exception. This will likely change in the future.

//TODO: add usage examples

#### AttributeInjector

#### StaticAttribute
A ***StaticAttribute*** ignores the value or presence of an attribute in xml.
Instead, it will always write a predefined value to xml, regardless of the
value or presence of such value in Java. Visa versa, it will always read the
predefined value from xml, regardless of the value or presence of the attribute.
The static predefined value is the text representation. A value converter can be used to transform the value to/from a specific Java type.

//TODO: add usage examples
