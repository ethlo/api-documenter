API-Documenter
==============

APT powered generic API documentation allowing API documentation for almost any API.

## Motivation

There are a multitude of different API generators out there for both RESTful and SOAP services for a multitude of different architectures. The challenge arise when you want to extract information that the tool do not provide, like what security roles some custom annotation dictates.

My solution to this is to generate a meta-model (JSON based), that can easily be rendered by pretty much any view technology out there.

[![Build Status](https://travis-ci.org/ethlo/api-documenter.svg?branch=master)](https://travis-ci.org/ethlo/api-documenter)

## Example

### Input
```java
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
        ...
    }
    
    @Secured("ROLE_WRITER")
    @RequestMapping(method=RequestMethod.PUT, value="/items/{id}") 
    public String write(int id, String content) throws FileNotFoundException, IOException
    {
        ...
    }
    
    @Api(group="hidden")
    @Secured("ROLE_ADMIN")
    @RequestMapping(method=RequestMethod.PUT, value="/admin/stats") 
    public @ResponseBody @ResponsePayload ResponseObject stats(@RequestPayload @RequestBody RequestObject req)
    {
        ...
    }
}
```

### Output

#### api.json
```json
{
  "com.ethlo.ws.ExampleEndpoint" : {
    "packageName" : "com.ethlo.ws",
    "className" : "ExampleEndpoint",
    "annotations" : {
      "org.springframework.ws.server.endpoint.annotation.Endpoint" : { },
      "org.springframework.web.bind.annotation.RequestMapping" : {
        "properties" : {
          "value" : [ "/basepath" ]
        }
      },
      "com.ethlo.api.annotations.Api" : {
        "properties" : {
          "group" : [ "public" ]
        }
      }
    },
    "methods" : [ {
      "methodName" : "read",
      "annotations" : {
        "org.springframework.security.access.annotation.Secured" : {
          "properties" : {
            "value" : [ "ROLE_READER" ]
          }
        },
        "org.springframework.web.bind.annotation.RequestMapping" : {
          "properties" : {
            "method" : [ "GET" ],
            "value" : [ "/items/{id}" ]
          }
        },
        "org.springframework.ws.server.endpoint.annotation.PayloadRoot" : {
          "properties" : {
            "localPart" : "GetItemRequest",
            "namespace" : "urn://foo/bar"
          }
        },
        "org.springframework.ws.server.endpoint.annotation.ResponsePayload" : { }
      },
      "params" : [ {
        "name" : "id",
        "type" : {
          "type" : "int"
        },
        "annotations" : [ { } ]
      } ],
      "returnType" : {
        "type" : "java.lang.String"
      },
      "javadoc" : " Allow the reading of an item by id\n @param id The id of the item\n @return The item\n"
    }, {
      "methodName" : "stats",
      "annotations" : {
        "com.ethlo.api.annotations.Api" : {
          "properties" : {
            "group" : [ "private" ]
          }
        },
        "org.springframework.security.access.annotation.Secured" : {
          "properties" : {
            "value" : [ "ROLE_ADMIN" ]
          }
        },
        "org.springframework.web.bind.annotation.RequestMapping" : {
          "properties" : {
            "method" : [ "PUT" ],
            "value" : [ "/admin/stats" ]
          }
        },
        "org.springframework.web.bind.annotation.ResponseBody" : { },
        "org.springframework.ws.server.endpoint.annotation.ResponsePayload" : { }
      },
      "params" : [ {
        "name" : "req",
        "type" : {
          "type" : "com.ethlo.ws.RequestObject"
        },
        "annotations" : [ { }, { } ]
      } ],
      "returnType" : {
        "type" : "com.ethlo.ws.ResponseObject"
      }
    }, {
      "methodName" : "write",
      "annotations" : {
        "org.springframework.security.access.annotation.Secured" : {
          "properties" : {
            "value" : [ "ROLE_WRITER" ]
          }
        },
        "org.springframework.web.bind.annotation.RequestMapping" : {
          "properties" : {
            "method" : [ "PUT" ],
            "value" : [ "/items/{id}" ]
          }
        }
      },
      "params" : [ {
        "name" : "id",
        "type" : {
          "type" : "int"
        }
      }, {
        "name" : "content",
        "type" : {
          "type" : "java.lang.String"
        }
      } ],
      "declaredExceptions" : [ {
        "type" : "java.io.FileNotFoundException"
      }, {
        "type" : "java.io.IOException"
      } ],
      "returnType" : {
        "type" : "java.lang.String"
      }
    } ]
  }
}
```
#### data-types.json
```
{
  "com.ethlo.ws.RequestObject": "{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"filter\" : {\n      \"type\" : \"string\"\n    }\n  }\n}",
  "com.ethlo.ws.ResponseObject": "{\n  \"type\" : \"object\",\n  \"properties\" : {\n    \"age\" : {\n      \"type\" : \"integer\"\n    },\n    \"name\" : {\n      \"type\" : \"string\"\n    }\n  }\n}"
}
```
## Usage

### Maven plugin
```xml
<plugin>
	<groupId>org.bsc.maven</groupId>
	<artifactId>maven-processor-plugin</artifactId>
	<version>2.2.4</version>
	<executions>
		<execution>
			<id>process</id>
			<goals>
				<goal>process</goal>
			</goals>
			<phase>generate-sources</phase>
			<configuration>
				<processors>
					<processor>com.ethlo.api.apt.ApiProcessor</processor>
				</processors>
				<options>
					<classMarkers>org.springframework.ws.server.endpoint.annotation.Endpoint</classMarkers>
					<methodMarkers>org.springframework.web.bind.annotation.RequestMapping,org.springframework.ws.server.endpoint.annotation.PayloadRoot</methodMarkers>
					<excludeJavadoc>false</excludeJavadoc>
					<target>${project.build.directory}</target>
				</options>
			</configuration>
		</execution>
	</executions>
	<dependencies>
		<dependency>
			<groupId>com.ethlo.doc</groupId>
			<artifactId>api-documenter</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
	</dependencies>
</plugin>
```

## Maven repository
http://ethlo.com/maven

## Maven artifact
```xml
<dependency>
  <groupId>com.ethlo.doc</groupId>
  <artifactId>api-documenter</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## Status

## TODO
