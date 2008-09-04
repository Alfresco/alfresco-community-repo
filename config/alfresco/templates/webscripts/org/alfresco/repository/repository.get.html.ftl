<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head> 
    <title>Alfresco CMIS v${cmisVersion}</title> 
    <link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">
  </head>
  <body>
    <table>
     <tr>
        <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
        <td><nobr><span class="title">Alfresco CMIS v${cmisVersion}</span></nobr></td>
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
        <tr><td>Alfresco's Content Repository provides the CMIS REST v${cmisVersion} binding, an extension of the Atom Publishing Protocol. 
                All <a href="${url.serviceContext}/index/family/CMIS">CMIS services</a> are implemented as <a href="http://wiki.alfresco.com/wiki/Web Scripts">Web Scripts</a>, 
                therefore support all Web Script capabilities such as authentication, content negotiation, tunelling etc.</td>
        </tr>
    </table>
    <br>
    <span class="mainSubTitle">REST TEST</span>
    <table>
        <tr><td>The following test harness exercises the CMIS REST binding.  By default, its parameters are setup to this
                repository, although any CMIS REST provider may be tested.</td></tr>
        <form action="${url.serviceContext}/api/cmis/test" method="post">
        <tr><td></td></tr>
        <tr><td>Service URI: <input name="url" size="50" value="http://localhost:8080/alfresco/service/api/cmis"></td></tr>
        <tr><td>Username/Password: <input name="user" value="username/password"></td></tr>
        <tr><td>Validate Responses: <input type="checkbox" name="validate" value="true" checked="checked"></td></tr>
        <tr><td>Trace Request/Responses: <input type="checkbox" name="trace" value="true"></td></tr>
        <tr><td>Argument Style: <select name="args"><option value="url" selected="selected">URL Arguments</option><option value="headers">Request Headers</option><option value="both">Both</option></select></td></tr>
        <tr><td>Tests (use * for wildcard in name): <input name="tests" value="*"></td></tr>
        <tr><td>(<#list tests as test>${test}<#if test_has_next>, </#if></#list>)</td></tr>
        <tr><td><input type="submit" value="Test"></td></tr>
        </form>
    </table>    
    <br>
    <span class="mainSubTitle">Web Services Binding</span>
    <table>
        <tr><td>Alfresco's Content Repository provides the CMIS Web Services v${cmisVersion} binding as defined by the following
                <a href="${url.serviceContext}/alfresco/cmis">WSDL</a>.</td></tr>
    </table>
    <br>
    <span class="mainSubTitle">Alfresco Repository Information</span>
    <table>
        <tr><td>(also available as an <a href="${url.serviceContext}/api/repository">CMIS/APP Service Document</a>)</td></tr>
    </table>
    <br>
    <table>
        <tr><td>Repository Id:</td><td>${server.id}</td></tr>
        <tr><td>Repository Name:</td><td>${server.name}</td></tr>
        <tr><td>Repository Relationship:</td><td>Self</td></tr>
        <tr><td>Repository Description:</td><td>[none]</td></tr>
        <tr><td>Vendor Name:</td><td>Alfresco</td></tr>
        <tr><td>Product Name:</td><td>Alfresco Repository (${server.edition})</td></tr>
        <tr><td>Product Version:</td><td>${server.version}</td></tr>
        <tr><td>Root Folder Id:</td><td>${absurl(url.serviceContext)}/api/path/${encodeuri(defaultRootFolderPath)}/children</td></tr> 
        <tr><td>Multifiling:</td><td>true</td></tr>
        <tr><td>Unfiling:</td><td>false</td></tr>
        <tr><td>VersionSpecificFiling:</td><td>false</td></tr>        
        <tr><td>PWCUpdateable:</td><td>true</td></tr>
        <tr><td>PWCSearchable:</td><td>${pwcSearchable?string}</td></tr>
        <tr><td>AllVersionsSearchable:</td><td>${allVersionsSearchable?string}</td></tr>
        <tr><td>Query:</td><td>${querySupport}</td></tr>
        <tr><td>Join:</td><td>${joinSupport}</td></tr>
        <tr><td>FullText:</td></td><td>${fullTextSupport}</td></tr>
        <tr><td>VersionsSupported:</td><td>${cmisVersion}</td></tr>
        <tr><td>repositorySpecificInformation:</td><td>[none]</td></tr>
    </table>
  </body>    
</html>