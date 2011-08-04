[#ftl]
[#import "/org/alfresco/cmis/lib/links.lib.atom.ftl" as linksLib/]
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <title>Alfresco CMIS</title>
  <link rel="stylesheet" href="${url.context}/css-boilerplate/screen.css" type="text/css" media="screen" charset="utf-8">
  <!--[if lte IE 6]><link rel="stylesheet" href="${url.context}/css-boilerplate/lib/ie.css" type="text/css" media="screen" charset="utf-8"><![endif]-->
  <script type="text/javascript">
    function toggleDisplay(toggle)
    {
      var toggleBody = document.getElementById(toggle.id + "_body");
      if (!toggleBody) return true;
      if (toggleBody.style.display == "none")
      {
        toggleBody.style.display = "block";
        toggle.innerHTML = "[-]";
      }
      else
      {
        toggleBody.style.display = "none";
        toggle.innerHTML = "[+]";
      }
      return true;
    }
  </script>
</head>
<body>

<div id="page">
  <div id="header">
    <a href="http://www.alfresco.com"><img id="alflogo" src="${url.context}/images/logo/AlfrescoLogo200.png"/></a><a href="http://www.oasis-open.org/committees/cmis"><img id="alflogo" height="55px" src="${url.context}/images/logo/cmis_logo_100.png"/></a>
  </div>
  
  <div id="body" class="wrapper">
    <div id="introduction">
      <p/>
      CMIS (Content Management Interoperability Services) is a standard for improving interoperability between ECM systems.
      It specifies a domain model plus a set of services and protocol bindings for Web Services (SOAP) and AtomPub.
      <p/>
      This Alfresco server supports CMIS <a href="http://docs.oasis-open.org/cmis/CMIS/v1.0/os/cmis-spec-v1.0.doc"><b>${cmisSpecTitle}</b></a>.
      <p/>
      <a name="repo"></a>
      <h3>Alfresco CMIS Content Repository</h3>
      <p>Point your CMIS client to one of the following Alfresco CMIS bindings (with <strong>user=admin</strong> and <strong>password=admin</strong>).</p>
      <ul>
        <li>CMIS AtomPub Binding: <a href="${url.context}/cmisatom">AtomPub Service Document</a></li>
        </li>
        <li>CMIS Web Services Binding: <a href="${url.context}/cmisws/RepositoryService">WSDL Documents</a></li>
      </ul>
      
      <h5><span id="repoinfo" class="toggle" onclick="return toggleDisplay(this)">[+]</span> CMIS Repository Information</h5>
      <table id="repoinfo_body" style="display: none;">
        <tr><td>Version Supported</td><td>${cmisVersion}</td></tr>
        <tr><td>Specification Title</td><td>CMIS ${cmisSpecTitle}</td></tr>
        <tr><td>Repository Id</td><td>${server.id}</td></tr>
        <tr><td>Repository Name</td><td>${server.name}</td></tr>
        <tr><td>Repository Description</td><td>[none]</td></tr>
        <tr><td>Vendor Name</td><td>Alfresco</td></tr>
        <tr><td>Product Name</td><td>Alfresco Repository (${server.edition})</td></tr>
        <tr><td>Product Version</td><td>${server.version}</td></tr>
      </table>
      
      <h5><span id="repocapabilities" class="toggle" onclick="return toggleDisplay(this)">[+]</span> CMIS Repository Capabilities</h5>
      <table id="repocapabilities_body" style="display: none;">
        <tr><td>ACL</td><td>${aclCapability}</td></tr>
        <tr><td>AllVersionsSearchable</td><td>${allVersionsSearchable?string}</td></tr>
        <tr><td>Changes</td><td>objectidsonly</td></tr>
        <tr><td>ContentStreamUpdatability</td><td>anytime</td></tr>
        <tr><td>GetDescendants</td><td>true</td></tr>
        <tr><td>GetFolderTree</td><td>true</td></tr>
        <tr><td>Multifiling</td><td>true</td></tr>
        <tr><td>PWCSearchable</td><td>${pwcSearchable?string}</td></tr>
        <tr><td>PWCUpdateable</td><td>true</td></tr>
        <tr><td>Query</td><td>${querySupport}</td></tr>
        <tr><td>Join</td><td>${joinSupport}</td></tr>
        <tr><td>Renditions</td><td>read</td></tr>
        <tr><td>Unfiling</td><td>false</td></tr>
        <tr><td>VersionSpecificFiling</td><td>false</td></tr>
        <tr><td>SupportedPermissions</td><td>${aclSupportedPermissions}</td></tr>
        <tr><td>PermissionPropagation</td><td>${aclPropagation}</td></tr>
      </table>

      <p>You can also browse this repository via the <a href="${url.context}/cmisbrowse?url=${absurl(url.context)}/cmisatom">OpenCMIS Browser</a>.</p>

      <h3>Alfresco 3.x CMIS Content Repository (obsolete)</h3>
      <p>The Alfresco 3.x CMIS URLs are still available but should not be used.</p>
      <ul>
        <li>CMIS AtomPub Binding: <a href="${url.serviceContext}/cmis">AtomPub Service Document</a> (<a href="${absurl(url.serviceContext)}/index/package/org/alfresco/cmis/doc">API reference</a>)</li>
        </li>
        <li>CMIS Web Services Binding: <a href="${url.context}/cmis">WSDL Documents</a></li>
      </ul>

      <a name="testatompub"></a>
       <h3>OpenCMIS TCK</h3>
       <p>Point the OpenCMIS TCK (Test Compatibility Kit) at your CMIS Repository AtomPub Service Document. Provide credentials (or leave blank, if authentication not required) and hit the '<strong>Start TCK</strong>' button for a test report.</p>
       <form action="${url.context}/cmistck" method="POST"> 
       <input type="hidden" name="org.apache.chemistry.opencmis.binding.spi.type" value="atompub">
       <fieldset> 
         <legend>CMIS Repository</legend> 
         <table> 
           <tr><td>AtomPub URL:</td><td><input type="text" name="org.apache.chemistry.opencmis.binding.atompub.url" value="${absurl(url.context)}/cmisatom" size="50"></td></tr> 
           <tr><td>Username:</td><td><input type="text" name="org.apache.chemistry.opencmis.user" value="admin" size="50"></td></tr> 
           <tr><td>Password:</td><td><input type="password" name="org.apache.chemistry.opencmis.password"  value="admin" size="50"></td></tr> 
           <tr><td>Repository Id:</td><td><input type="text" name="org.apache.chemistry.opencmis.session.repository.id" size="50"></td></tr> 
          </table>
        </fieldset>
        <p><input type="submit" name="submit" value="Start TCK" class="button"></p>
      </form>
   </div>

   <div id="resources">
      <h3>CMIS Resources</h3>
      <ul>
        <li><a href="http://www.oasis-open.org/committees/cmis">OASIS Technical Committee</a></li>
        <li><a href="http://xml.coverpages.org/cmis.html">Cover Pages</a></li>
      </ul>
      <h3>CMIS v1.0</h3>
      <ul>
        <li><a href="http://docs.oasis-open.org/cmis/CMIS/v1.0/os/cmis-spec-v1.0.doc">cmis-spec-v1.0.doc (Authoritative)</a></li>
        <li><a href="http://docs.oasis-open.org/cmis/CMIS/v1.0/os/cmis-spec-v1.0.pdf">cmis-spec-v1.0.pdf</a></li>
        <li><a href="http://docs.oasis-open.org/cmis/CMIS/v1.0/os/cmis-spec-v1.0.html">cmis-spec-v1.0.html</a></li>
        <li><a href="http://docs.oasis-open.org/cmis/CMIS/v1.0/os/CMIS-Core.xsd">CMIS-Core.xsd</a></li>
        <li><a href="http://docs.oasis-open.org/cmis/CMIS/v1.0/os/CMIS-Messaging.xsd">CMIS-Messaging.xsd</a></li>
        <li><a href="http://docs.oasis-open.org/cmis/CMIS/v1.0/os/CMIS-RestAtom.xsd">CMIS-RestAtom.xsd</a></li>
      </ul>
      <h3>Alfresco Resources</h3>
      <ul>
        <li><a href="http://wiki.alfresco.com/wiki/CMIS">CMIS Wiki</a></li>
        <li><a href="http://blogs.alfresco.com/cmis/">CMIS Blog</a></li>
        <li><a href="http://wiki.alfresco.com/wiki/Download_Community_Edition">Download</a> Repository</a></li>
        <li><a href="http://wiki.alfresco.com/wiki/Alfresco_SVN_Development_Environment">Source Code</a> for Repository</li>
      </ul>
      <h3>Apache Chemistry</h3>
      <ul>
        <li><a href="http://chemistry.apache.org/">Home Page</a></li>
        <li><a href="http://svn.apache.org/viewvc/chemistry/tck/trunk/">Source Code</a> for TCK</li>
        <li><a href="http://svn.apache.org/viewvc/chemistry/opencmis/trunk/chemistry-opencmis-test/chemistry-opencmis-test-browser/">Source Code</a> for Browser</li>
      </ul>
      <h3>Provide Feedback</h3>
      <ul>
        <li><a href="http://forums.alfresco.com/en/viewforum.php?f=45">CMIS Forum</a></li>
        <li><a href="https://issues.alfresco.com/jira/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+ALF+AND+resolution+%3D+Unresolved+AND+component+%3D+CMIS+ORDER+BY+priority+DESC&mode=hide">Find / Raise Issues</li>
        <li><a href="http://groups.google.com/group/cmis-interop">CMIS Interop Group</a></li>
      </ul>
    </div>
  </div>
  
  <div id="footer">
    <p>Last Updated: $Date$, Alfresco Software, Inc</p>
  </div>
</div>

</body>
</html>
