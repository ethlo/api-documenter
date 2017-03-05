package com.ethlo.api.apt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 
 * @author Morten Haraldsen
 *
 */
public class MethodDescriptor implements Comparable<MethodDescriptor>
{
    private String methodName;
    private Map<String, AnnotationDescriptor> annotations;
    private List<VariableDescriptor> params;
    private List<TypeDescriptor> declaredExceptions;
    private TypeDescriptor returnType;
    private String javadoc;

    public MethodDescriptor(String methodName, List<AnnotationDescriptor> methodAnnotations, List<VariableDescriptor> params, TypeDescriptor returnType, List<TypeDescriptor> declaredExceptions, String javadoc)
    {
        this.methodName = methodName;
        this.annotations = new LinkedHashMap<>();
        for (AnnotationDescriptor ann : methodAnnotations)
        {
            this.annotations.put(ann.getAnnotationType().getType(), ann);
        }
        this.returnType = returnType;
        this.params = params;
        this.declaredExceptions = declaredExceptions;
        this.javadoc = javadoc;
    }

    public String getName()
    {
        return this.methodName;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public List<VariableDescriptor> getParams()
    {
        return params;
    }

    public List<? extends TypeDescriptor> getDeclaredExceptions()
    {
        return declaredExceptions;
    }

    public TypeDescriptor getReturnType()
    {
        return returnType;
    }
    
    public String getJavadoc()
    {
        return this.javadoc;
    }
    
    @Override
    public String toString()
    {
        return StringUtils.collectionToDelimitedString(annotations.values(), "\n") + "\n" 
                + this.returnType.toString() + " " 
                + this.methodName + "(" + getArgsAsString() + ")" + getThrowsAsString();
    }

    private String getThrowsAsString()
    {
        if (! CollectionUtils.isEmpty(this.declaredExceptions))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(" throws ");
            
            int index = 0;
            for (TypeDescriptor exc : declaredExceptions)
            {
                if (index > 0)
                {
                    sb.append(", ");
                }
                sb.append(exc.toString());
                index++;
            }
            return sb.toString();
        }
        return "";
    }

    private String getArgsAsString()
    {
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (VariableDescriptor param : params)
        {
            if (index > 0)
            {
                sb.append(", ");
            }
            final List<? extends AnnotationDescriptor> paramAnnotations = param.getAnnotations();
            if (! paramAnnotations.isEmpty())
            {
                sb.append(StringUtils.collectionToCommaDelimitedString(paramAnnotations));
                sb.append(" ");
            }
            
            sb.append(param.getType());
            sb.append(" ");
            sb.append(param.getName());
            index++;
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        //result = prime * result + ((params == null) ? 0 : params.hashCode());
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
        
        if (!(obj instanceof MethodDescriptor))
        {
            return false;
        }
        
        MethodDescriptor other = (MethodDescriptor) obj;
        
        if ( !methodName.equals(other.methodName))
        {
            return false;
        }
        
        if (!params.equals(other.params))
        {
            return false;
        }
        
        return true;
    }

    public Map<String, AnnotationDescriptor> getAnnotations()
    {
        return annotations;
    }

    @Override
    public int compareTo(MethodDescriptor o)
    {
        return methodName.compareTo(o.getMethodName());
    }
}
