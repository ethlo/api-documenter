package com.ethlo.api.apt;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author mha
 */
public class ClassDescriptor
{
    private String packageName;
    final String className;
    private Map<String, AnnotationDescriptor> annotations;
    private Set<MethodDescriptor> methods = new LinkedHashSet<>();

    public ClassDescriptor(String packageName, String className, List<AnnotationDescriptor> allClassAnnotations)
    {
        this.packageName = packageName;
        this.className = className;
        this.annotations = new LinkedHashMap<>(); 
        for (AnnotationDescriptor annotationDescriptor : allClassAnnotations)
        {
            this.annotations.put(annotationDescriptor.getAnnotationType().getType(), annotationDescriptor);
        }
    }
    
    public boolean addMethod(MethodDescriptor method)
    {
        for (MethodDescriptor desc : methods)
        {
            if ((method.getName() + "(" + method.getParams() + ")").equals(desc.getName() + "(" + desc.getParams() + ")"))
            {
                return false;
            }
        }
        this.methods.add(method);
        return true;
    }

    public String getName()
    {
        return className;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof ClassDescriptor))
        {
            return false;
        }
        final ClassDescriptor other = (ClassDescriptor) obj;
        return packageName.equals(other.packageName) 
             && className.equals(other.className);
    }
}
