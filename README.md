# Description

A sample plugin to illustrate how to extend the Nuxeo Platform REST API

# How to build
```
git clone https://github.com/nuxeo-sandbox/nuxeo-resource-rest-api
cd nuxeo-resource-rest-api
mvn clean install
```

To build the plugin without building the Docker image, use:

```
mvn -DskipDocker=true clean install
```

# How to test locally with docker

```
docker compose up
```

Example of API call

```
curl --request GET 'http://localhost:8080/nuxeo/api/v1/resource/File/123?schemas=file' --header 'Authorization: Basic QWRtaW5pc3RyYXRvcjpBZG1pbmlzdHJhdG9y'
```

Where File is the document type and 123 is the value of the property `dc:title`


# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/), and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).
