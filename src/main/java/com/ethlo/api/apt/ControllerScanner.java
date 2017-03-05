package com.ethlo.api.apt;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

public class ControllerScanner
{
    // Configuration
    private Class<? extends Annotation> methodMarker = RequestMapping.class;
    private Set<String> exclude = new TreeSet<>(Arrays.asList("org.springframework"));
    
    public ControllerScanner(Class<? extends Annotation> methodMarker)
    {
        this.methodMarker = methodMarker;
    }
    
    public Result findMethods()
    {
        final Map<String, ClassDescriptor> api = new LinkedHashMap<>();
        final FastClasspathScanner scanner = new FastClasspathScanner();
        final ScanResult result = scanner
            .enableMethodAnnotationIndexing()
            .scan(Runtime.getRuntime().availableProcessors());
        final List<String> classes = result.getNamesOfClassesWithMethodAnnotation(methodMarker);
        
        for (String cls : classes)
        {
            final Class<?> clazz = loadClass(cls);
            ClassDescriptor classDescriptor = api.get(clazz.getName());
            classDescriptor = new ClassDescriptor(clazz.getPackage().getName(), clazz.getSimpleName(), toAnnotationDescriptors(result, clazz.getAnnotations()));
            api.put(clazz.getName(), classDescriptor);
            
            for (Method method : clazz.getMethods())
            {
                if (method.getAnnotation(methodMarker) != null)
                {
                    classDescriptor.addMethod(new MethodDescriptor(method.getName(),
                        toAnnotationDescriptors(result, method.getAnnotations()), 
                        toParameterDescriptors(result, method.getParameters()), 
                        toTypeDescriptor(result, method.getReturnType()), 
                        toTypeDescriptors(result, method.getExceptionTypes()), null));
                }
            }
        }
        
        final Set<Class<?>> models = extractModels(api);
        
        return new Result(api, models);
    }

    private List<TypeDescriptor> toTypeDescriptors(ScanResult result, Class<?>[] exceptionTypes)
    {
        return Arrays.asList(exceptionTypes).stream().map(f->toTypeDescriptor(result, f)).collect(Collectors.toList());
    }
    
    private TypeDescriptor toTypeDescriptor(ScanResult result, Class<?> type)
    {
        final Set<Class<?>> subClasses = new HashSet<>();
        if (! type.isInterface())
        {
            subClasses.addAll(loadClasses(result.getNamesOfSubclassesOf(type)));
        }
        else
        {
            subClasses.addAll(loadClasses(result.getNamesOfClassesImplementing(type)));
        }
        return new TypeDescriptor(type.getName(), subClasses.stream().map(c->c.getName()).collect(Collectors.toSet()));
    }
    
    private List<VariableDescriptor> toParameterDescriptors(ScanResult result, Parameter[] parameters)
    {
        return Arrays.asList(parameters).stream().map(p->toParameterDescriptor(result, p)).collect(Collectors.toList());
    }
    
    private VariableDescriptor toParameterDescriptor(ScanResult result, Parameter p)
    {
        final int index = getIndex(p, p.getDeclaringExecutable().getParameters());
        final Annotation[][] annotations = p.getDeclaringExecutable().getParameterAnnotations();
        final Annotation[] paramAnnotations = annotations[index];
        return new VariableDescriptor(p.getName(), toTypeDescriptor(result, p.getType()), toAnnotationDescriptors(result, paramAnnotations));
    }

    private int getIndex(Parameter p, Parameter[] parameters)
    {
        for (int i = 0; i < parameters.length; i++)
        {
            if(p.equals(parameters[i]))
            {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }

    private List<AnnotationDescriptor> toAnnotationDescriptors(ScanResult result, Annotation[] annotations)
    {
        return Arrays.asList(annotations).stream().map(f->toAnnotationDescriptor(result, f)).collect(Collectors.toList());
    }

    private AnnotationDescriptor toAnnotationDescriptor(ScanResult result, Annotation a)
    {
        return new AnnotationDescriptor(toTypeDescriptor(result, a.annotationType()), wrapAnnotation(a));
    }

    private Map<String, Object> wrapAnnotation(Annotation ann)
    {
        final Map<String, Object> map = AnnotationUtils.getAnnotationAttributes(ann);
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext())
        {
            final Map.Entry<String, Object> e = it.next();
            Object value = e.getValue();
            if (e.getKey().equals("annotationType"))
            {
                it.remove();
            }
            else if (value == null || value.equals(""))
            {
                it.remove();
            }
            else if (value instanceof Collection)
            {
                if (((Collection<?>) value).isEmpty())
                {
                    it.remove();
                }
            }
            else if (value.getClass().isArray())
            {
                if (((Object[])value).length == 0)
                {
                    it.remove();
                }
            }
        }
        return map;
    }
    
    private Set<Class<?>> extractModels(Map<String, ClassDescriptor> api)
    {
        final Set<Class<?>> m = new HashSet<>(); 
        for (ClassDescriptor cls : api.values())
        {
            final Set<MethodDescriptor> methods = cls.getMethods();
            for (MethodDescriptor desc : methods)
            {
                m.add(loadClass(desc.getReturnType().getType()));
                for (VariableDescriptor param : desc.getParams())
                {
                    addIfAllowed(m, param.getType());
                }
                addIfAllowed(m, desc.getReturnType());
            }
        }
        return m;
    }

    private void addIfAllowed(Set<Class<?>> target, TypeDescriptor type)
    {
        final List<String> matches = exclude
             .stream()
             .filter((c)->type.getType().startsWith(c))
             .collect(Collectors.toList());
        if (matches.isEmpty())
        {
            final Class<?> clazz = loadClass(type.getType());
            if (clazz != null)
            {
                target.add(clazz);
            }
        }
        
        type.getSubTypes()
            .stream()
            .forEach(c->
            {
                addIfAllowed(target, new TypeDescriptor(c));
            });
    }

    private Class<?> loadClass(String className)
    {
        try
        {
            return Class.forName(className, false, getClass().getClassLoader());
        }
        catch (ClassNotFoundException exc)
        {
            //System.err.println(className);
            return null; 
        }
    }
    
    private Collection<? extends Class<?>> loadClasses(List<String> classNames)
    {
        return classNames.stream().map(c->loadClass(c)).collect(Collectors.toList());
    }

}
