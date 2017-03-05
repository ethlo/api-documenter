package com.ethlo.ws;

import java.io.IOException;

import org.junit.Test;

import com.ethlo.api.ApiBuilder;
import com.ethlo.api.Configuration;

public class SpringMvcScannerTest
{
    @Test
    public void findMethods() throws IOException
    {
        new ApiBuilder(new Configuration.Builder()
            .targetRenderedFile("/tmp/api.html")
            .targetModelFile("/tmp/api.json")
            .build())
        .build();
    }
}