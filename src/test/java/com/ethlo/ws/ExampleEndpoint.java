package com.ethlo.ws;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.ethlo.api.annotations.Api;

@Transactional
@Endpoint
@RequestMapping("/basepath")
@Api(group="public")
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
    
    @Api(group="private")
    @Secured("ROLE_ADMIN")
    @RequestMapping(method=RequestMethod.PUT, value="/admin/stats") 
    public @ResponseBody @ResponsePayload ResponseObject stats(@RequestPayload @RequestBody RequestObject req)
    {
        return null;
    }
    
    /**
     * This method is used to load detailed information about a person.
     * 
     * @param id The id of the person one wants to load information about
     * @return The serialized person object
     */
    @Api(group="public")
    @Secured("ROLE_USER")
    @RequestMapping(method=RequestMethod.PUT, value="/person/{id}") 
    public @ResponseBody @ResponsePayload Person getPerson(@PathVariable("id") int id)
    {
        return null;
    }
}