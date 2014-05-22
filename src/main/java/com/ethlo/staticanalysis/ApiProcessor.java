package com.ethlo.staticanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * 
 * @author mha
 */
public class ApiProcessor extends AbstractProcessor
{
    private Set<ClassDescriptor> collector = new LinkedHashSet<>();
    private Elements elementsUtil;
    private String targetDir;
    private Set<String> classMarkers;
    private Set<String> methodMarkers;
    private boolean excludeJavadoc = false;
    private Set<String> excludeAnnotations;
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        this.elementsUtil = processingEnv.getElementUtils();
        
        final ClassDescriptor classDesc = getClassDescriptor(annotations, roundEnv);
        
        for (TypeElement t : annotations)
        {
            for (Element e : roundEnv.getElementsAnnotatedWith(t))
            {
                if (ElementKind.METHOD.equals(e.getKind()))
                {
                    handleMethod(classDesc, (ExecutableElement) e);
                }
            }
        }
        
        if (roundEnv.getRootElements().isEmpty())
        {
            createReport();
        }
        
        return false;
    }
    
    @Override
    public Set<String> getSupportedOptions()
    {
        return new TreeSet<>(Arrays.asList("classMarkers", "methodMarkers", "target", "excludeJavadoc", "excludeAnnotations"));
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        return new TreeSet<>(Arrays.asList("*"));
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        
        final Map<String, String> options = processingEnv.getOptions();
        this.targetDir = options.get("target") != null ? options.get("target") : "target";
        
        this.classMarkers = StringUtils.commaDelimitedListToSet(options.get("classMarkers"));
        if (this.classMarkers.isEmpty())
        {
            throw new IllegalStateException("classMarkers must be set. Please specify one or more annotations that denotes your API classes");
        }
        
        this.methodMarkers = StringUtils.commaDelimitedListToSet(options.get("methodMarkers"));
        if (this.methodMarkers.isEmpty())
        {
            throw new IllegalStateException("methodMarkers must be set. Please specify one or more annotations that denotes your API methods");
        }

        this.excludeJavadoc = Boolean.valueOf(options.get("excludeJavadoc"));
        this.excludeAnnotations = StringUtils.commaDelimitedListToSet(options.get("excludeAnnotations"));
        
        info("target: " + targetDir);
        info("classMarkers: " + StringUtils.collectionToCommaDelimitedString(classMarkers));
        info("methodMarkers: " + StringUtils.collectionToCommaDelimitedString(methodMarkers));
        info("excludeJavadoc: " + excludeJavadoc);
        info("excludeAnnotions: " + excludeAnnotations);
    }

    private void info(String msg)
    {
        processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.NOTE, msg);
    }

    private void createReport()
    {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        
        final File targetFile = new File(targetDir, "api-doc.json");
        try (final Writer writer = new BufferedWriter(new FileWriter(targetFile)))
        {
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, this.collector);
            info("Wrote API documentation file to " + targetFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private ClassDescriptor getClassDescriptor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        for (TypeElement t : annotations)
        {
            for (Element e : roundEnv.getElementsAnnotatedWith(t))
            {
                if (ElementKind.CLASS.equals(e.getKind()))
                {
                    final List<AnnotationDescriptor> allClassAnnotations = wrapAnnotations(filterAnnotations(elementsUtil.getAllAnnotationMirrors(e)));
                    return new ClassDescriptor(elementsUtil.getPackageOf(e).toString(), e.getSimpleName().toString(), allClassAnnotations);
                }
            }
        }
        return null;
    }

    private void handleMethod(ClassDescriptor classDesc, ExecutableElement e)
    {
        if (! containsClassMarker(e))
        {
            return;
        }
        
        final String simpleName = e.getSimpleName().toString();
        final List<VariableDescriptor> params = wrapParams(e.getParameters());

        final List<AnnotationDescriptor> methodAnnotations = wrapAnnotations(filterAnnotations(elementsUtil.getAllAnnotationMirrors(e)));
            
        final List<TypeDescriptor> declaredExceptions = wrapTypes(e.getThrownTypes());
        final TypeDescriptor returnType = wrapType(e.getReturnType());
        final MethodDescriptor methodDesc = new MethodDescriptor(simpleName, methodAnnotations, params, returnType, declaredExceptions, this.excludeJavadoc ? null : elementsUtil.getDocComment(e));
        classDesc.addMethod(methodDesc);
        collector.add(classDesc);
    }

    private List<? extends AnnotationMirror> filterAnnotations(List<? extends AnnotationMirror> allAnnotationMirrors)
    {
        final List<? extends AnnotationMirror> filtered = new ArrayList<>(allAnnotationMirrors);
        for (AnnotationMirror ann : allAnnotationMirrors)
        {
            for (String excluded : this.excludeAnnotations)
            {
                if (ann.getAnnotationType().toString().equals(excluded))
                {
                    filtered.remove(ann);
                }
            }
        }
        return filtered;
    }

    private boolean containsClassMarker(ExecutableElement e)
    {
        if (this.classMarkers != null)
        {
            final List<? extends AnnotationMirror> classAnnotationsForMethod = elementsUtil.getAllAnnotationMirrors(e.getEnclosingElement());
            for (String annClass : this.classMarkers)
            {
                for (AnnotationMirror annMirror : classAnnotationsForMethod)
                {
                    if (annClass.equals(annMirror.getAnnotationType().toString()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private TypeDescriptor wrapType(TypeMirror type)
    {
        return new TypeDescriptor(type.toString());
    }

    private List<TypeDescriptor> wrapTypes(List<? extends TypeMirror> thrownTypes)
    {
        final List<TypeDescriptor> retVal = new ArrayList<>();
        for (TypeMirror typeMirror : thrownTypes)
        {
            retVal.add(wrapType(typeMirror));
        }
        return retVal;
    }

    private List<VariableDescriptor> wrapParams(List<? extends VariableElement> parameters)
    {
        final List<VariableDescriptor> retVal = new ArrayList<>();
        for (VariableElement variable : parameters)
        {
            final List<AnnotationDescriptor> paramAnnotations = wrapAnnotations(variable.getAnnotationMirrors());
            retVal.add(new VariableDescriptor(variable.getSimpleName().toString(), wrapType(variable.asType()), paramAnnotations));
        }
        return retVal;
    }

    private List<AnnotationDescriptor> wrapAnnotations(List<? extends AnnotationMirror> annotationMirrors)
    {
        final List<AnnotationDescriptor> retVal = new ArrayList<>();
        for (AnnotationMirror ann : annotationMirrors)
        {
            final Map<String, Object> properties = wrapAnnotation(ann);
            retVal.add(new AnnotationDescriptor(new TypeDescriptor(ann.getAnnotationType().toString()), properties));
        }
        return retVal;
    }

    private Map<String, Object> wrapAnnotation(AnnotationMirror ann)
    {
        final Map<String, Object> properties = new LinkedHashMap<>();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : ann.getElementValues().entrySet())
        {
            final String key = entry.getKey().getSimpleName().toString();
            final Object value = entry.getValue().getValue();
            
            // Convert to serializable object
            final Object conv = convert(value);
            properties.put(key, conv);
        }
        return properties;
    }

    private Object convert(Object value)
    {
        if (value instanceof String)
        {
            return value;
        }
        else if (value instanceof TypeMirror)
        {
            return wrapType((TypeMirror) value);
        }
        else if (value instanceof VariableElement)
        {
            return ((VariableElement)value).toString();
        }
        else if (value instanceof AnnotationMirror)
        {
            return wrapAnnotation((AnnotationMirror)value);
        }
        else if (value instanceof AnnotationValue)
        {
            return convert(((AnnotationValue)value).getValue());
        }
        else if (value instanceof List)
        {
            @SuppressWarnings("unchecked")
            final List<? extends AnnotationValue> list = (List<? extends AnnotationValue>) value;
            if (! list.isEmpty())
            {
                final List<Object> retVal = new ArrayList<>();
                for (AnnotationValue val : list)
                {
                    retVal.add(convert(val));
                }
                return retVal;
            }
            return null;
        }
        else
        {
            throw new IllegalArgumentException("Unhandled type: " + value + ". " + value.getClass());
        }
    }
}