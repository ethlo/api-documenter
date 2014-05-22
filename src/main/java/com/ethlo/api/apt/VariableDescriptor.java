package com.ethlo.api.apt;

import java.util.List;

/**
 * 
 * @author mha
 */
public class VariableDescriptor
{
    private String name;
    private TypeDescriptor type;
    private List<AnnotationDescriptor> annotations;
   
    public VariableDescriptor(String paramName, TypeDescriptor type, List<AnnotationDescriptor> paramAnnotations)
    {
        this.name = paramName;
        this.type = type;
        this.annotations = paramAnnotations;
    }

    public TypeDescriptor asType()
    {
        return type;
    }

    public String getSimpleName()
    {
        return this.name;
    }

    public List<AnnotationDescriptor> getAnnotations()
    {
        return this.annotations;
    }
    
    @Override
    public String toString()
    {
        return this.type.toString() + " " + this.name;
    }
}
