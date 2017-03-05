package com.ethlo.ws;

import org.springframework.security.access.annotation.Secured;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mha
 */
@RestController
@RequestMapping("/system")
public class ExampleController
{
    @RequestMapping("/status")
    @Secured("ROLE_ADMIN")
    public String memoryStatus(ModelMap model)
    {
        return null;
    }
    
    @RequestMapping("/status/{section}")
    @Secured("ROLE_ADMIN")
    public String memoryStatusSection(@PathVariable("section") String sectionName)
    {
        return null;
    }
    
    @RequestMapping(value="/users", method=RequestMethod.POST)
    @Secured("ROLE_ADMIN")
    public void registerUser(Person person)
    {
        
    }
}