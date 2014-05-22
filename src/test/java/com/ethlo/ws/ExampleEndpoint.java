package com.ethlo.ws;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Transactional
@Endpoint
@RequestMapping("/basepath")
public class ExampleEndpoint
{
    public static final String NS = "urn://foo/bar";
    
    /**
     * Allow the reading of an item by id
     * @param id The id of the item
     * @return The item
     */
    @Secured("ROLE_READER")
    @RequestMapping(method=RequestMethod.GET, value="/items/{id}")
    @PayloadRoot(localPart = "GetItemRequest", namespace = NS)
    public @ResponsePayload String read(@RequestPayload int id)
    {
        return null;
    }
    
    @Secured("ROLE_WRITER")
    @RequestMapping(method=RequestMethod.PUT, value="/items/{id}") 
    public String write(int id, String content) throws FileNotFoundException, IOException
    {
        if (id == 0)
        {
            throw new IOException("Nope, don't like that one");
        }
        
        return null;
    }
}