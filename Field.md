# Java's Field Class #

The following is pasted from the javadocs at http://java.sun.com/j2se/1.4.2/docs/api/java/lang/reflect/Field.html.



---



java.lang.reflect
Class Field

java.lang.Object
> extended byjava.lang.reflect.AccessibleObject
> > extended byjava.lang.reflect.Field

All Implemented Interfaces:

> Member

public final class Field
extends AccessibleObject
implements Member

A Field provides information about, and dynamic access to, a single field of a class or an interface. The reflected field may be a class (static) field or an instance field.

A Field permits widening conversions to occur during a get or set access operation, but throws an IllegalArgumentException if a narrowing conversion would occur.

See Also:
> Member, Class, Class.getFields(), Class.getField(String), Class.getDeclaredFields(), Class.getDeclaredField(String)