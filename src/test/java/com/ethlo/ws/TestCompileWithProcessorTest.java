package com.ethlo.ws;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

import com.ethlo.api.apt.ApiProcessor;

public class TestCompileWithProcessorTest
{
    @Test
    public void testCompileWithProcessor()
    {
        // Get an instance of java compiler
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // Get a new instance of the standard file manager implementation
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(
            new File("src/test/java/com/ethlo/ws/ExampleEndpoint.java"),
            new File("src/test/java/com/ethlo/ws/ExampleController.java")
            ));

        // Create the compilation task
        final Set<String> options = new HashSet<String>();
        options.add("-AmethodMarkers=" + RequestMapping.class.getCanonicalName() + "," + PayloadRoot.class.getCanonicalName());
        options.add("-AfilterAnnotations=" + Transactional.class.getCanonicalName() + "," + Controller.class.getCanonicalName());
        options.add("-AexcludeJavadoc=false");

        // Create the compilation task
        final CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits);

        // Create a list to hold annotation processors
        final List<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();

        // Add an annotation processor to the list
        processors.add(new ApiProcessor());

        // Set the annotation processor to the compiler task
        task.setProcessors(processors);

        // Perform the compilation task.
        task.call();
    }
}
