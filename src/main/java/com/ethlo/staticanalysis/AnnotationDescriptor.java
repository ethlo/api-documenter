package com.ethlo.staticanalysis;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author mha
 *
 */
public class AnnotationDescriptor
{
    @JsonIgnore
    private TypeDescriptor type;
    private Map<String, Object> properties;

    public AnnotationDescriptor(TypeDescriptor annotationType, Map<String, Object> properties)
    {
        this.type = annotationType;
        this.properties = properties;
    }

    public TypeDescriptor getAnnotationType()
    {
        return type;
    }
    
    @Override
    public String toString()
    {
        return "@" + type + (this.properties.isEmpty() ? "" : "(" + this.properties + ")");
    }
}
