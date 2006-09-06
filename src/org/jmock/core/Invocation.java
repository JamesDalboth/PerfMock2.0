/*  Copyright (c) 2000-2004 jMock.org
 */
package org.jmock.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.hamcrest.StringDescription;


/**
 * The static details about a method and the run-time details of its invocation.
 * 
 * @since 1.0
 */
public class Invocation implements SelfDescribing {
    public static final Object[] NO_PARAMETERS = null;

    private final Object invokedObject;
    private final Method invokedMethod;
    private final List<Object> parameterValues;

    // Yuck, but there doesn't seem to be a better way.
    private static final Map BOX_TYPES = makeBoxTypesMap();

    
    public Invocation(Object invoked, Method method, Object[] parameterValues) {
        this.invokedObject = invoked;
        this.invokedMethod = method;
        this.parameterValues = (parameterValues == NO_PARAMETERS) 
            ? Collections.emptyList()
            : Collections.unmodifiableList(Arrays.asList(parameterValues));
    }
    
    public String toString() {
        return super.toString() + "[" + StringDescription.toString(this) + "]";
    }
    
    public boolean equals(Object other) {
        return (other instanceof Invocation) && this.equals((Invocation)other);
    }

    public boolean equals(Invocation other) {
        return other != null && invokedObject == other.invokedObject
            && invokedMethod.equals(other.invokedMethod)
            && parameterValues.equals(other.parameterValues);
    }

    public int hashCode() {
        return invokedObject.hashCode() ^ invokedMethod.hashCode()
            ^ parameterValues.hashCode();
    }

    public void describeTo(Description description) {
        description.appendText(invokedObject.toString());
        description.appendText(".");
        description.appendText(invokedMethod.getName());
        description.appendValueList("(", ", ", ")", parameterValues);
    }

    public Object getInvokedObject() {
        return invokedObject;
    }

    public Method getInvokedMethod() {
        return invokedMethod;
    }

    public int getParameterCount() {
        return parameterValues.size();
    }

    public Object getParameter(int i) {
        return parameterValues.get(i);
    }

    public Object[] getParametersAsArray() {
        return parameterValues.toArray();
    }

    public Object applyTo(Object target) throws Throwable {
        try {
            return invokedMethod.invoke(target, getParametersAsArray());
        }
        catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    public void checkReturnTypeCompatibility(final Object value) {
        Class returnType = invokedMethod.getReturnType();
        if (returnType == void.class) {
            failIfReturnTypeIsNotNull(value);
        }
        else if (value == null) {
            failIfReturnTypeIsPrimitive();
        }
        else {
            Class valueType = value.getClass();
            if (!isCompatible(returnType, valueType)) {
                reportTypeError(returnType, valueType);
            }
        }
    }

    private boolean isCompatible(Class<?> returnType, Class<?> valueType) {
        if (returnType.isPrimitive()) {
            // The reflection API doesn't reflect Java's autoboxing.
            return isBoxedType(returnType, valueType);
        }
        return returnType.isAssignableFrom(valueType);
    }

    private boolean isBoxedType(Class primitiveType, Class referenceType) {
        return BOX_TYPES.get(primitiveType) == referenceType;
    }

    private void failIfReturnTypeIsNotNull(final Object result) {
        if (result != null) {
            throw new IllegalStateException("tried to return a value from a void method");
        }
    }

    private void failIfReturnTypeIsPrimitive() {
        Class returnType = invokedMethod.getReturnType();
        if (returnType.isPrimitive()) {
            throw new IllegalStateException(
                "tried to return null value from method returning " + returnType.getName());
        }
    }

    private void reportTypeError(Class returnType, Class valueType) {
        throw new IllegalStateException(
            "tried to return an incompatible value: " +
            "expected a " + returnType.getName() +
            " but returned a " + valueType.getName());
    }

    private static Map<Class<?>, Class<?>> makeBoxTypesMap() {
        HashMap<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Byte.class);
        map.put(char.class, Character.class);
        map.put(short.class, Short.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(float.class, Float.class);
        map.put(double.class, Double.class);
        return map;
    }
}