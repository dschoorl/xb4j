# Introduction
Xb4j, short for 'Xml Bindings for Java' is a library that supports making Java
objects out of xml and visa versa, where Java objects describe a domain model
and are the Java counter part of the domain entities described in xml. This
documentation describes the few simple concepts behind xb4j, a user guide and a
guide to extend the framework to suite your specific needs.

## Core concepts
Xb4j treats Java and Xml as equals. It does not assume one being derived from
the other. Therefore, xb4j generates no Java classes from an xml schema and it
generates no xml schema from Java classes. Xb4j assumes both are provided.

With xb4j, the Java developer writes in Java code, in a binding model, how his
classes bind to the xml instances. These bindings can be used for both reading
and writing xml. By writing multiple binding models, the developer can bind
Java classes to multiple xml schemas or multiple Java classes to the same xml
schema. This is the central problem that xb4j tries to solve.

Xb4j provides a limited level of validation of the xml: it requires that the
developer describes all the xml elements that can or must occur in the xml in
the order they may appear in the xml. An xml schema provides a good basis for
writing a binding model.

Xb4j does not want to impose restrictions and limitations on both the Java
domain classes nor the xml schemas. Xb4j tries to solve this by providing rich
binding functionality, E.g. bind xml elements or attributes to constructor parameters instead of demanding default constructors etc.

It is easy to envision tool support, E.g. via an IDE plugin, to visually
support binding Java classes to xml, by reading an xml schema and the Java
class path and provide a GUI to bind the two together, generating the binding
model.Such tool support is currently no more than an idea for which time must
be found.

## Benefits
TODO: describe the benefits of the xb4j approach to processing xml in Java.
