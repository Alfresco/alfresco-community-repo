<h1>        Release Notes - Alfresco - Version Community Edition 201910 EA
</h1>
<h2>
  New Features
</h2>
<ul>
  <li>
    <h4>Custom Transforms and Renditions</h4>
    <p>Alfresco Content Services (ACS) provides a number of content
     transforms, but also allows custom transforms to be added.
    <p>It is now possible to create custom transforms that run in 
    separate processes known as T-Engines (short for Transformer
    Engines). The same engines may be used in Community and 
    Enterprise Editions. They may be directly connected to the ACS 
    repository as Local Transforms, but in the Enterprise edition there 
    is the option to include them as part of the Transform Service 
    which provides more balanced throughput and better administration 
    capabilities.
    <p>For more information see <a href='https://github.com/Alfresco/acs-packaging/blob/master/docs/custom-transforms-and-renditions.md'>Custom Transforms and Renditions</a>
  </li>
      <li>
      <h4>Open-source Transformation Engines</h4>
      <p>The core T-Engine images can now be used in Community
       Edition.</p>
       <p>T-Engines code has been Open-Sourced and is available on Github:</p>
       <ul><a href='https://github.com/Alfresco/alfresco-transform-core'>alfresco/alfresco-transform-core</a></ul>
       <p>Images are available on Docker Hub:</p>
       <ul><a href='https://hub.docker.com/r/alfresco/alfresco-imagemagick'>alfresco/alfresco-imagemagick</a></ul>
       <ul><a href='https://hub.docker.com/r/alfresco/alfresco-pdf-renderer'>alfresco/alfresco-pdf-renderer</a></ul>
       <ul><a href='https://hub.docker.com/r/alfresco/alfresco-libreoffice'>alfresco/alfresco-libreoffice</a></ul>
       <ul><a href='https://hub.docker.com/r/alfresco/alfresco-tika'>alfresco/alfresco-tika</a></ul>
       <ul><a href='https://hub.docker.com/r/alfresco/alfresco-transform-misc'>alfresco/alfresco-transform-misc</a></ul>
       </p>
    </li>
    <li>
    <h4>Removal of external executables from docker image</h4>
    <p>With the introduction of the new Local Transform Service
    in Alfresco Community Edition, the capability of executing
    remote transformations on T-Engines was enabled. Because of
    this, the external executables (Alfresco-Pdf-renderer, Libreoffice
    and Imagemagick) have been removed from the docker container to
    facilitate the usage of out-of-process transformations.
     </p>
  </li>
</ul>
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22013'>ALF-22013</a>] -         Docker Image for Base Tomcat locale is POSIX
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22060'>ALF-22060</a>] -         Reader on the backing store is obtained twice in CachingContentStore
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22056'>ALF-22056</a>] -         onCopyCompleteBehaviour not called in order of copy-action
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22073'>ALF-22073</a>] -         MailActionExecutor doesn't consider email bodies with a HTML doctype as HTML
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-21988'>ALF-21988</a>] -         Tab order for number ranges not ok
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22097'>ALF-22097</a>] -         T Engine - add source nodeId parameter
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/MNT-20714'>MNT-20714</a>] -         REST API /nodes/{nodeId}/content endpoint fails for content created by a deleted user
</li>
</ul>
<h2>        Improvement
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-4318'>REPO-4318</a>] -         [COMPLETE] Extraction of transformers and metadata extractors
</li>
</ul>
<h2>

<h1>        Release Notes - Alfresco - Version Community Edition 201901 GA
</h1>
<h2>
  New Features
</h2>
<ul>
  <li>
    <h4>ActiveMQ:</h4>
    Alfresco ActiveMQ Docker images: <a href='https://github.com/Alfresco/alfresco-docker-activemq'>GitHub Repo</a> <a href='https://hub.docker.com/r/alfresco/alfresco-activemq/'>DockerHub Repo</a><p>
  </li>
    <li>
    <h4>Alfresco Benchmark Framework:</h4>
    <p>The benchmark framework project provides a way to run highly scalable, easy-to-run Java-based load and benchmark tests on an Alfresco instance.</p>
    <p>It comprises the following: <a href='https://github.com/Alfresco/alfresco-bm-manager'>Alfresco BM Manager</a> and Alfresco BM Drivers.</p> 
    <p>The currently provided drivers are:</p>
      <ul>
        <li><a href='https://github.com/Alfresco/alfresco-bm-load-data'>Alfresco Benchmark Load Data</a></li>
        <li><a href='https://github.com/Alfresco/alfresco-bm-rest-api'>Alfresco Benchmark Rest Api</a></li>
        <li><a href='https://github.com/Alfresco/alfresco-bm-load-users'>Alfresco Benchmark Load Users</a></li>
      </ul>	 
  </li>
    <li>
    <h4>Java 11 support</h4>
    <p>ACS is now runnable with OpenJDK 11.0.1. It still remains compatible with JDK 1.8.</p>
  </li>
</ul>
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22049'>ALF-22049</a>] -         Alfresco does not start
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22041'>ALF-22041</a>] -         EKS deployment - SOLR_ALFRESCO_HOST set to wrong host name
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22031'>ALF-22031</a>] -         REST API calls silently rollback after the returning a success status
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-21963'>ALF-21963</a>] -         Workflow - backslash in nodeRef properties url.
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-21803'>ALF-21803</a>] -         Unable to add users to sites whose 'short name' is a substring of 'site'
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-21664'>ALF-21664</a>] -         Exception on workflow image by REST API
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-20854'>ALF-20854</a>] -         webdav error opening Spanish Accent files
</li>
</ul>
<h2>        Improvement
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-3668'>REPO-3668</a>] -         Renditions: Transform event consumer
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-7'>REPO-7</a>] -         Embed ActiveMQ in the Platform
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-1957'>REPO-1957</a>] -         Transformations Improvement Plan
</li>
</ul>
<h2>
  Deprecations
</h2>
<ul>
  <li>
    TransformService and RenditionService: All Java APIs related to TransformService and RenditionService have been deprecated; the ability to perform arbitrary transformations will be phased out as the new DBP Transform Service takes effect.  Renditions can be triggered using the existing repository REST API but will be processed asynchronously using the new services.<br/>
  </li>
</ul>
<h2>
  Known issues
</h2>
<ul>
  <li>
    Due to the changes to the RenditionService the Media Management AMP is not supported yet.<br/>
  </li>
</ul>
<h2>

<h1>        Release Notes - Alfresco - Version Community Edition 201810 EA
</h1>  

<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-21783'>ALF-21783</a>] -         ScriptAuthorityService: No way to get more than 100 results with some methods
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-21917'>ALF-21917</a>] -         Document list edit metadata incorrect url escaping
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22001'>ALF-22001</a>] -         Faceted search does not work in Japanese
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22030'>ALF-22030</a>] -         ADF UI freezes noticeably on a periodic basis during KeyCloak auth requests
</li>
</ul>                                                                                
<h2>        Improvement
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-2491'>REPO-2491</a>] -         Renditions: Rendition Testing
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-3651'>REPO-3651</a>] -         AWS Load Tests: Infrastructure and Revamp
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-3663'>REPO-3663</a>] -         AWS Load Tests: Initial AWS Cost Estimation with BMF
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-3667'>REPO-3667</a>] -         Renditions: Transform event producer
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-3677'>REPO-3677</a>] -         AWS Services: Basic Deployment
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/REPO-3703'>REPO-3703</a>] -         AWS Services: Native Services of ACS
</li>
</ul>
<h2>

<h1>        Release Notes - Alfresco - Version Community Edition 201808 EA
</h1>                                                                                                                                                                                                                                                            
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-21992'>ALF-21992</a>] -         BehaviourFilterImpl.isEnabled(NodeRef, QName) is checking wrong QName in case of subClass
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22006'>ALF-22006</a>] -         VersionServicePolicies cannot be disabled on a specific node
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22007'>ALF-22007</a>] -         TransactionListeners are executed in unpredictable order
</li>
</ul>
<h2>        Improvement
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22011'>ALF-22011</a>] -         Upgrade to XMLBeans 3.0.0
</li>
</ul>

<h1>        Release Notes - Alfresco - Version Community Edition 201806 GA
</h1>                                                                                                                                                                                                                                                            
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22000'>ALF-22000</a>] -         Docker: HTML link 693ce565f4c4:8080/share but name 
</li>
<li>[<a href='https://issues.alfresco.com/jira/browse/ALF-22008'>ALF-22008</a>] -         It is not possible to upload a document without versions using CMIS
</li>
</ul>
