package com.ethlo.api.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author mha
 *
 */
public class AnnotationDescriptor
{
    private TypeDescriptor type;
    private Map<String, Object> properties;

    public AnnotationDescriptor(TypeDescriptor annotationType, Map<String, Object> properties)
    {
        this.type = annotationType;
        this.properties = properties;
    }

    @JsonIgnore
    public TypeDescriptor getAnnotationType()
    {
        return type;
    }
    
    @Override
    public String toString()
    {
        return "@" + type + (this.properties.isEmpty() ? "" : "(" + this.properties + ")");
    }
    
    public Map<String, Object> getProperties()
    {
        return this.properties;
    }
}
