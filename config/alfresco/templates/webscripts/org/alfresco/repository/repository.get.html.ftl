<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Alfresco CMIS v${cmisVersion}</title> 
    <link rel="stylesheet" href="${url.context}/css/base.css" TYPE="text/css">
  </head>
  <body>
    <table>
     <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="title">Alfresco CMIS v${cmisVersion} (REST Binding)</span></nobr></td>
     </tr>
    </table>
    <br>
    <table>
      <tr><td><a href="${url.serviceContext}/index">Back to Web Scripts Home</a></td></tr>
    </table>
    <br>
    <span class="mainSubTitle">Introduction</span>
    <table>
        <tr>
           <td>The Content Management Interoperability Services (CMIS) standard defines a domain
               model and set of API bindings, such as Web Services and REST/Atom that can be used
               by applications to work with one or more Content Management repositories/systems.</td>
        </tr>
    </table>
    <br>
    <span class="mainSubTitle">REST Binding</span>
    <table>
        <tr><td>Alfresco's Content Repository provides the CMIS REST binding, an extension of the Atom Publishing Protocol. 
                All CMIS services are implemented as <a href="http://wiki.alfresco.com/wiki/Web Scripts">Web Scripts</a>, 
                therefore support all Web Script capabilities such as authentication, content negotiation, tunelling etc.</td></tr>
    </table>
    <br>
    <span class="mainSubTitle">CMIS Services</span>
    <table>
        <tr><td>A description of each CMIS service is available in the <a href="${url.serviceContext}/index/family/CMIS">reference documentation.</a></td></tr>
    </table>
    <br>
    <span class="mainSubTitle">CMIS Repository Information</span>
    <table>
        <tr><td>(also available as an <a href="${url.serviceContext}/api/repository">APP Service Document</a>)</td></tr>
    </table>
    <br>
    <table>
        <tr><td>Repository Id:</td><td>${server.id}</td></tr>
        <tr><td>Repository Name:</td><td>${server.name}</td></tr>
        <tr><td>Repository Description:</td><td>[none]</td></tr>
        <tr><td>Vendor Name:</td><td>Alfresco</td></tr>
        <tr><td>Product Name:</td><td>Alfresco Repository (${server.edition})</td></tr>
        <tr><td>Product Version:</td><td>${server.version}</td></tr>
        <tr><td>Multifiling:</td><td>true</td></tr>
        <tr><td>Unfiling:</td><td>false</td></tr>
        <tr><td>VersionSpecificFiling:</td><td>false</td></tr>        
        <tr><td>PWCUpdateable:</td><td>true</td></tr>
        <tr><td>AllVersionsSearchable:</td><td>false</td></tr>
        <tr><td>Join:</td><td>noJoin</td></tr>
        <tr><td>FullText:</td></td><td>fulltextandstructured</td></tr>
        <tr><td>VersionsSupported:</td><td>${cmisVersion}</td></tr>
        <tr><td>repositorySpecificInformation:</td><td>[none]</td></tr>
    </table>
  </body>    
</html>