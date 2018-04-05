
# Alfresco Content Services Community Packaging
This project is producing packaging for [Alfresco Content Services Repository](https://community.alfresco.com/docs/DOC-6385-project-overview-repository).

The SNAPSHOT version of the artifacts is **never** published.

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.

This produces the docker images for alfresco-content-repository-community and the distribution zip for the entire Alfresco Content Services Community product

# General

### Build:
* ```mvn clean install``` in the root of the project will build everything.

## Docker Alfresco
On official releases, the image is published: https://hub.docker.com/r/alfresco/alfresco-content-repository-community/tags/ 

For testing locally:
1. Go to docker-alfreco folder
2. Run *mvn clean install* if you have not done so
3. Build the docker image: ```docker build . --tag acr-community:6.0.tag```
4. Check that the image has been created locally with your desired name/tag: ```docker images```

### Docker-compose & Kubernetes
Use the deployment project if you want the sample docker-compose or helm: https://github.com/Alfresco/acs-community-deployment

## Distribution zip
In this folder the distribution zip is build. It contains all the war files, libraries, certificates and settings files you need to deploy Alfresco Content Services Community on the supported application servers.


