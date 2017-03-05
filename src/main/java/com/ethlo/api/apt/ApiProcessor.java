package com.ethlo.api.apt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.ethlo.api.annotations.Api;
import com.ethlo.api.renderer.Renderer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.github.reinert.jjschema.v1.JsonSchemaFactory;
import com.github.reinert.jjschema.v1.JsonSchemaV4Factory;
import com.google.common.base.Throwables;

/**
 * 
 * @author mha
 */
public class ApiProcessor extends AbstractProcessor
{
    private static final Set<String> primitiveTypes = new HashSet<>(Arrays.asList("byte", "short", "int", "long", "float", "double", "boolean", "char"));
    private static final Map<Class<?>, String> simpleTypes = new HashMap<>();
    private static final JsonSchemaFactory schemaFactory = new JsonSchemaV4Factory();
    
    public static final ObjectMapper mapper;
    static
    {
        simpleTypes.put(String.class, "string");
        simpleTypes.put(Byte.class, "byte");
        simpleTypes.put(Short.class, "short");
        simpleTypes.put(Integer.class, "integer");
        simpleTypes.put(Long.class, "long");
        simpleTypes.put(Float.class, "float");
        simpleTypes.put(Double.class, "double");
        simpleTypes.put(Boolean.class, "boolean");
        simpleTypes.put(Character.class, "character");
        
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.setVisibility(mapper.getSerializationConfig()
            .getDefaultVisibilityChecker()
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
            .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
            .withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE));
        
        schemaFactory.setAutoPutDollarSchema(true);
    }
    
    private Elements elementsUtil;
    private String srcDir;
    private boolean createTypeSchemas = true;
    private Set<String> methodMarkers;
    private boolean excludeJavadoc = false;
    private Set<String> filterAnnotations;
    
    private String target;
    private Result result = new Result();
    private Set<String> includeGroups;
    private Set<String> excludeGroups;
    
    private Set<String> excludedTypes = new TreeSet<>(Arrays.asList("org.springframework.ui.ModelMap"));
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        this.elementsUtil = processingEnv.getElementUtils();
        
        for (TypeElement t : annotations)
        {
            Set<? extends Element> annWith = new TreeSet<>();
            try
            {
                annWith = roundEnv.getElementsAnnotatedWith(t);
            }
            catch (IllegalArgumentException exc)
            {
                info(exc.toString());
            }
            
            for (Element e : annWith)
            {
                if (ElementKind.METHOD.equals(e.getKind()))
                { 
                    handleMethod(getClassDescriptor(e.getEnclosingElement()), (ExecutableElement) e);
                }
            }
        }
        
        if (roundEnv.getRootElements().isEmpty())
        {
            // We are done, start assembling report
            this.result.types.keySet()
                .stream()
                .forEach(t->System.out.println(t));
            
            createReport();
        }
        
        return false;
    }

    private ClassDescriptor getClassDescriptor(Element e)
    {
        if (result.containsClass(e))
        {
            return result.getClass(e);
        }
        final String packageName = elementsUtil.getPackageOf(e).toString();
        final String name = e.getSimpleName().toString();
        final List<? extends AnnotationMirror> allAnnotations = elementsUtil.getAllAnnotationMirrors(e);
        final List<? extends AnnotationMirror> filteredAnnotations = filterAnnotations(allAnnotations);
        final List<AnnotationDescriptor> annotations = wrapAnnotations(filteredAnnotations);
        final ClassDescriptor candidate = new ClassDescriptor(packageName, name, annotations);
        result.addClass(candidate);
        return candidate;
    }

    @Override
    public Set<String> getSupportedOptions()
    {
        return new TreeSet<>(Arrays.asList("methodMarkers", "target", "excludeJavadoc", "filterAnnotations", "includeGroups", "excludeGroups"));
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
        
        this.srcDir = options.get("source") != null ? options.get("source") : "src/main/java";
        this.target = options.get("target") != null ? options.get("target") : "target/classes/api.json";
        this.methodMarkers = StringUtils.commaDelimitedListToSet(options.get("methodMarkers"));
        if (this.methodMarkers.isEmpty())
        {
            throw new IllegalStateException("methodMarkers must be set. Please specify one or more annotations that denotes your API methods");
        }
        this.includeGroups = StringUtils.commaDelimitedListToSet(options.get("includeGroups"));
        this.excludeGroups = StringUtils.commaDelimitedListToSet(options.get("excludeGroups"));
        this.excludeJavadoc = Boolean.valueOf(options.get("excludeJavadoc"));
        this.filterAnnotations = StringUtils.commaDelimitedListToSet(options.get("filterAnnotations"));
        
        info("methodMarkers: " + StringUtils.collectionToCommaDelimitedString(methodMarkers));
        info("excludeJavadoc: " + excludeJavadoc);
        info("filterAnnotions: " + filterAnnotations);
        info("includeGroups: " + StringUtils.collectionToCommaDelimitedString(includeGroups));
        info("excludeGroups: " + StringUtils.collectionToCommaDelimitedString(excludeGroups));
    }

    private void info(String msg)
    {
        processingEnv.getMessager().printMessage(javax.tools.Diagnostic.Kind.NOTE, msg);
    }

    private void createReport()
    {
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        final AnnotationIntrospector introspector =  new AnnotationIntrospectorPair(new JaxbAnnotationIntrospector(mapper.getTypeFactory()), new JacksonAnnotationIntrospector());
        mapper.setAnnotationIntrospector(introspector);
        
        removeEmptyTypes(result);
        
        final File targetFile = new File(target);
        
        // TODO: Make output filename configurable
        final File renderedTargetFile = new File(target + ".html");
        
        writeFile(targetFile, this.result);
        writeRendered(renderedTargetFile, result);
    }
    
    private void writeRendered(File target, Result result)
    {
        final Map<String, Object> model = new LinkedHashMap<>();
        model.put("api", result.api);
        model.put("types", result.types);
        
        try (final Writer out = new FileWriter(target))
        {
            // TODO: Make this configurable
            Renderer.render(model, "oai.tpl.html", out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        } 
    }

    private void removeEmptyTypes(Result result)
    {
        final Iterator<Entry<String, ClassDescriptor>> iter = result.getClasses().entrySet().iterator();
        while (iter.hasNext())
        {
            final Entry<String, ClassDescriptor> entry = iter.next();
            if (entry.getValue().getMethods().isEmpty())
            {
                iter.remove();
            }
        }
    }

    private void writeFile(File targetFile, Object obj)
    {
        targetFile.getParentFile().mkdirs();
        try (final Writer writer = new BufferedWriter(new FileWriter(targetFile)))
        {
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, obj);
            info("Wrote file " + targetFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }        
    }

    private void handleMethod(ClassDescriptor classDesc, ExecutableElement e)
    {
        final String simpleName = e.getSimpleName().toString();
        final List<VariableDescriptor> params = wrapParams(e.getParameters());

        final List<? extends AnnotationMirror> allMethodAnnotations = elementsUtil.getAllAnnotationMirrors(e);
        if (! containsMethodMarker(allMethodAnnotations))
        {
            return;
        }
        
        final List<AnnotationDescriptor> methodAnnotations = wrapAnnotations(filterAnnotations(allMethodAnnotations));
        
        if (includeGroups != null && !includeGroups.isEmpty())
        {
            if (! containsGroup(findAnnotation(Api.class, methodAnnotations), includeGroups))
            {
                return;
            }
        }
        
        if (excludeGroups != null && !excludeGroups.isEmpty())
        {
            if (containsGroup(findAnnotation(Api.class, methodAnnotations), excludeGroups))
            {
                return;
            }
        }
            
        final List<TypeDescriptor> declaredExceptions = wrapTypes(e.getThrownTypes());
        final TypeDescriptor returnType = wrapType(e.getReturnType());
        
        if (this.createTypeSchemas )
        {
            compile(e);
        }
        
        final String javadocOrNull = this.excludeJavadoc ? null : elementsUtil.getDocComment(e);
        final MethodDescriptor methodDesc = new MethodDescriptor(simpleName, methodAnnotations, params, returnType, declaredExceptions, javadocOrNull);
        classDesc.addMethod(methodDesc);
    }

    private boolean containsGroup(AnnotationDescriptor ann, Collection<String> groups)
    {
        if (ann != null)
        {
            @SuppressWarnings("unchecked")
            final Collection<String> annGroups = (Collection<String>) ann.getProperties().get("group");
            for (String annGroup : annGroups)
            {
                if (groups.contains(annGroup))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    private AnnotationDescriptor findAnnotation(Class<?> type, List<AnnotationDescriptor> methodAnnotations)
    {
        for (AnnotationDescriptor desc : methodAnnotations)
        {
            final String annType = desc.getAnnotationType().getType();
            if (annType.equals(type.getCanonicalName()))
            {
                return desc;
            }
        }
        return null;
    }

    private List<? extends AnnotationMirror> filterAnnotations(List<? extends AnnotationMirror> allAnnotationMirrors)
    {
        final List<? extends AnnotationMirror> filtered = new ArrayList<>(allAnnotationMirrors);
        for (AnnotationMirror ann : allAnnotationMirrors)
        {
            for (String excluded : this.filterAnnotations)
            {
                if (ann.getAnnotationType().toString().equals(excluded))
                {
                    filtered.remove(ann);
                }
            }
        }
        return filtered;
    }
    
    private AnnotationMirror find(List<? extends AnnotationMirror> annotationMirrors, String fqn)
    {
        for (AnnotationMirror ann : annotationMirrors)
        {
            if (ann.getAnnotationType().toString().equals(fqn))
            {
                return ann;
            }
        }
        return null;
    }

    private boolean containsMethodMarker(List<? extends AnnotationMirror> classAnnotationsForMethod)
    {
        for (String annClass : this.methodMarkers)
        {
            for (AnnotationMirror annMirror : classAnnotationsForMethod)
            {
                if (annClass.equals(annMirror.getAnnotationType().toString()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private TypeDescriptor wrapType(TypeMirror type)
    {
        final TypeDescriptor td = new TypeDescriptor(type.toString(), null);
        
        if (type.getKind().equals(TypeKind.DECLARED))
        {
            final List<? extends AnnotationMirror> mirrors = this.elementsUtil.getAllAnnotationMirrors(((DeclaredType)type).asElement());
            final AnnotationMirror mirror = find(mirrors, JsonSubTypes.class.getCanonicalName());
            if (mirror != null)
            {
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : mirror.getElementValues().entrySet())
                {
                    @SuppressWarnings({ "unchecked"})
                    final List<AnnotationMirror> values = (List<AnnotationMirror>) e.getValue().getValue();
                    final List<AnnotationDescriptor> annoDesc = wrapAnnotations(values);
                    
                    for (AnnotationDescriptor ann : annoDesc)
                    {
                        final Map<String, Object> props = ann.getProperties();
                        final String alias = props.get("name").toString();
                        final String className = props.get("value").toString();
                        
                        // Need to compile the sub-class
                        final List<File> files = new ArrayList<>();
                        doCompile(files, Arrays.asList(className));
                        final String schema = result.types.get(className);
                        
                        result.types.put(className, schema);
                        td.getSubTypes().add(className);
                    }
                }
            }
        }
        
        return td;
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
        for (VariableElement param : parameters)
        {
            final List<AnnotationDescriptor> paramAnnotations = wrapAnnotations(param.getAnnotationMirrors());
            retVal.add(new VariableDescriptor(param.getSimpleName().toString(), wrapType(param.asType()), paramAnnotations));
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
        else if (ClassUtils.isPrimitiveOrWrapper(value.getClass()))
        {
            return value;
        }
        else
        {
            throw new IllegalArgumentException("Unhandled type: " + value + ". " + value.getClass());
        }
    }
    
    private void compile(ExecutableElement e)
    {
        final List<? extends VariableElement> params = e.getParameters();
        final List<File> files = new ArrayList<>();
        final List<String> classNames = new ArrayList<>();
        for (VariableElement param : params)
        {
            final String fqn = param.asType().toString();
            addType(files, classNames, fqn);
        }
        final String fqn = e.getReturnType().toString();
        addType(files, classNames, fqn);
        doCompile(files, classNames);
    }

    private void doCompile(final List<File> files, List<String> classNames)
    {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        try
        {
            final File compileDir = new File("compile-temp");
            compileDir.mkdir();
            compileDir.deleteOnExit();
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(compileDir));
        }
        catch (IOException exc)
        {
            throw new RuntimeException(exc);
        }
        
        if (! files.isEmpty())
        {
            final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
            final CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
            task.call();
        }
        
        for (String className : classNames)
        {
            if (! primitiveTypes.contains(className))
            {
                try
                {
                    final Class<?> clazz = Class.forName(className);
                    //final ValidationSchemaFactoryWrapper visitor = new ValidationSchemaFactoryWrapper();
                    //final SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
                    //mapper.acceptJsonFormatVisitor(clazz, visitor);
                    //final JsonSchema schema = visitor.finalSchema();
                    //final String strJsonSchema = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

                    final String schema = createSchema(clazz);
                    
                    result.addType(className, schema);
                }
                catch (StackOverflowError err)
                {
                    info("Circular data type: " + className);
                }
                catch (ClassNotFoundException exc)
                {
                    info(exc.getMessage());
                }
            }
        }
    }

    private String createSchema(Class<?> clazz)
    {
        final JsonNode schema = schemaFactory.createSchema(clazz);
        try
        {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        }
        catch (JsonProcessingException exc)
        {
            throw Throwables.propagate(exc);
        }
    }

    private boolean addType(List<File> files, List<String> classNames, String fqn)
    {
        if (result.containsType(fqn) || excludedTypes.contains(fqn))
        {
            return false;
        }
        
        final String relPath = fqn.replaceAll("\\.", "/");
        final File file = new File(this.srcDir, relPath + ".java");
        final boolean isPrimitive = primitiveTypes.contains(fqn);
        if (!isPrimitive && file.exists())
        {
            files.add(file);
        }
        classNames.add(fqn);
        return true;
    }
    
    private class Result
    {
        private Map<String, ClassDescriptor> api = new LinkedHashMap<>();
        private Map<String, String> types = new LinkedHashMap<>();
        
        public boolean containsType(String fqn)
        {
            return this.types.containsKey(fqn);
        }

        public Map<String, ClassDescriptor> getClasses()
        {
            return this.api;
        }

        public ClassDescriptor addClass(ClassDescriptor candidate)
        {
            return this.api.put(candidate.getPackageName() + "." + candidate.getName(), candidate);
        }

        public ClassDescriptor getClass(Element e)
        {
            final String key = elementsUtil.getPackageOf(e).toString() + "." +  e.getSimpleName().toString();
            return this.api.get(key);
        }

        public boolean containsClass(Element e)
        {
            final String key = elementsUtil.getPackageOf(e).toString() + "." +  e.getSimpleName().toString();
            return this.api.containsKey(key);
        }

        public void addType(String fqn, String jsonSchema)
        {
            this.types.put(fqn, jsonSchema);
        }
    }
}