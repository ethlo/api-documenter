API-Documenter
==============

APT powered generic API documentation allowing API documentation for almost any API.

## Motivation

There are a multitude of different API generators out there for both RESTful and SOAP services for a multitude of different architectures. The challenge arise when you want to extract information that the tool do not provide, like what security roles some custom annotation dictates.

My solution to this is to generate a meta-model (JSON based), that can easily be rendered by pretty much any view technology out there.

[![Build Status](https://travis-ci.org/ethlo/api-documenter.svg?branch=master)](https://travis-ci.org/ethlo/api-documenter)

To render the JSON file(s) created, please use [API Web UI](http://github.com/ethlo/api-ui/)

## Demo
[See this page for a minimalistic demo](http://ethlo.com/demo/api-ui/)

## Usage

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

### Maven usage

<script src="https://gist.github.com/ethlo/e83efe2be73e8b1386fc.js"></script>

## Example

### Input

<script src="https://gist.github.com/ethlo/5e4ac22c83cdf98f265a.js"></script>

### Output

<script src="https://gist.github.com/ethlo/ad2738910b2895ff05f5.js"></script>
