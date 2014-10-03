package com.ethlo.api.apt;

import java.util.Map;

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
