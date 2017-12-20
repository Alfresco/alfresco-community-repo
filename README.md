# Full Community Packaging for Alfresco Content Services

This project contains the code for packaging the entire Alfresco Content Services product Community edition.

## General
### Build:
* ```mvn clean install``` in the root of the project will build everything.
Note: It is not easy to build the full installer locally (and impossible on Windows) so, most likely you don't want to create the installer locally. Comment out the <module>installer</module> in the root pom.xml in order to avoid building the installer locally.

## Docker-compose & Kubernetes
Build and start Alfresco Content Services Community using docker-compose or Kubernetes, containing:
1. Alfresco Repository for community, with:  
1.1. Alfresco Share Services amp  
1.2. Alfresco AOS amp  
1.3. Alfresco vti-bin war - that helps with AOS integration  
1.4. Alfresco Google Docs Repo amp  
2. Alfresco Share, with:  
2.1 Alfresco Google Docks Share amp  
3. A Postgres DB  
4. Alfresco Solr6  

### Docker Compose Instructions:
#### Prerequisite: 
* Docker installed locally 
* Access to docker-internal.alfresco.com and quay.io repositories - Platform Services team is working on getting the images in [Docker Hub](https://hub.docker.com/u/alfresco/) registry.

#### Steps
1. Go to **docker-compose** folder
2. Run ```docker-compose up``` 
3. Check that everything starts up with the browser: http://localhost:8082/alfresco and http://localhost:8080/share and http://localhost:8083/solr/

#### Notes:
* Make sure the local machine has the ports (5432, 8080, 8082, 8083) set up in the docker-compose.yml file free.
* The images used in the docker-compose.yml are images that are build in the 'docker-alfresco' and 'docker-share' subfolders of the project - see the relevant sections below
* If you don't have access to the docker-internal.alfresco.com and quay.io images, or if you want custom data in your docker images, you can use the 'docker-alfresco' and 'docker-share' folders to customize and build your customized docker images that are used in the docker-compose project. Just make sure you use proper tags when you create the images and update the docker-compose.yml with these proper tags that you created.

### Kubernetes Instructions:
#### Prerequisite: 
* A minikube/aws k8s cluster up and ready (minimum 5GB memory)
* Access to docker-internal.alfresco.com and quay.io repositories - Platform Services team is working on getting the images in [Docker Hub](https://hub.docker.com/u/alfresco/) registry.
* A kubernetes secret (quay-registry-secret) with the above mentioned credentials created in your cluster.

#### Steps
1. Go to **helm** folder
2. Run ```helm dependency update alfresco-content-services && helm install alfresco-content-services``` 
3. After deploying the helm chart you will get information for obtaining the URL for repository, share and solr.

#### Notes:

* The images used in the alfresco-content-services/values.yml are images that are built in the 'docker-alfresco' and 'docker-share' subfolders of the project - see the relevant sections below.
* If you don't have access to the docker-internal.alfresco.com and quay.io images, or if you want custom data in your docker images, you can use the 'docker-alfresco' and 'docker-share' folders to customize and build your customized docker images that are used in the docker-compose project. Just make sure you build them in the minikube docker environment and update the alfresco-content-services/values.yml with the tags that you created.
* You can also change those values when deploying the helm chart by running ```helm install alfresco-content-services --set repository.image.tag="yourTag" --set share.image.tag="yourTag"```.
* Hint: Run  ```eval $(minikube docker-env)``` to switch to your minikube docker environment on osx.

## Docker images
These images are used to build the images used by the docker-compose.yml project to bring up an ACS Community, similar to what the installer did/does.  
The images are based on *pure* _content services_ and _share_ images done by the _acs-packaging_ and _share_ projects and adds the amps and settings necessary for running the images in a similar fashion to what the ACS deployment with the installer did/does.

### Docker Alfresco
1. Go to docker-alfreco folder
2. Run *mvn clean install* if you have not done so
3. Build the docker image: ```docker build . --tag my-acs-repo:6.0.test```
4. Check that the image has been created locally with your desired name/tag: ```docker images```

### Docker Share
1. Go to docker-share folder
2. Run *mvn clean install* if you have not done so
3. Build the docker image: ```docker build . --tag my-share:5.2.test```
4. Check that the image has been created locally with your desired name/tag: ```docker images```


## Distribution zip

In this folder the distribution zip is build. It contains all the war files, libraries, certificates and settings files you need to deploy alfresco on the supported application servers.

## Installer

In this folder the installer binaries are built for all the supported platforms.
