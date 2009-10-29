[#ftl]
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <title>Alfresco CMIS</title>
  <link rel="stylesheet" href="/css-boilerplate/screen.css" type="text/css" media="screen" charset="utf-8">
  <!--[if lte IE 6]><link rel="stylesheet" href="/css-boilerplate/lib/ie.css" type="text/css" media="screen" charset="utf-8"><![endif]-->
  <script type="text/javascript">
    function toggleDisplay(toggle)
    {
      var toggleBody = document.getElementById(toggle.id + "_body");
      if (!toggleBody) return true;
      if (toggleBody.style.display == "none")
      {
        toggleBody.style.display = "block";
        toggle.innerHTML = "-";
      }
      else
      {
        toggleBody.style.display = "none";
        toggle.innerHTML = "+";
      }
      return true;
    }
  </script>
</head>
<body>

<div id="page">
  <div id="header">
    <a href="http://www.alfresco.com"><img id="alflogo" src="/images/logo/AlfrescoLogo200.png"/></a><a href="http://www.oasis-open.org/committees/cmis"><img id="alflogo" height="55px" src="/images/logo/cmis_logo_100.png"/></a>
  </div>
  
  <div id="body" class="wrapper">
    <div id="introduction">
      <h2>Explore and Test CMIS</h2>
      <p>The CMIS (Content Management Interoperability Services) specification is under development but close to Public Review.</p>
      <p>This site hosts a <a href="#repo">CMIS Repository</a> and <a href="#testatompub">CMIS AtomPub TCK</a> to assist the development of the specification and promote interoperability between up-and-coming implementations. Feel free to use them for building and testing your own CMIS clients and servers.</p>
      <p>Frequent updates are made to both the Repository and TCK as issues are resolved or new capabilities added. Currently, <strong class="highlight">version ${cmisVersion}</strong> of the CMIS specification is supported.</p>
      
      <a name="repo"></a>
      <h3>Alfresco CMIS Repository</h3>
      <p>Point your CMIS client to one of the following Alfresco CMIS bindings (with <strong>user=admin</strong> and <strong>password=admin</strong>).</p>
      <ul>
        <li>CMIS AtomPub Binding: <a href="${url.serviceContext}/api/repository">AtomPub Service Document</a></li>
        <li>CMIS Web Services Binding: <a href="${url.context}/cmis">WSDL Documents</a></li>
      </ul>
      
      <h5><span id="repoinfo" class="toggle" onclick="return toggleDisplay(this)">+</span> CMIS Repository Information</h5>
      <table id="repoinfo_body" style="display: none;">
        <tr><td>Version Supported</td><td>${cmisVersion}</td></tr>
        <tr><td>Repository Id</td><td>${server.id}</td></tr>
        <tr><td>Repository Name</td><td>${server.name}</td></tr>
        <tr><td>Repository Relationship</td><td>Self</td></tr>
        <tr><td>Repository Description</td><td>[none]</td></tr>
        <tr><td>Vendor Name</td><td>Alfresco</td></tr>
        <tr><td>Product Name</td><td>Alfresco Repository (${server.edition})</td></tr>
        <tr><td>Product Version</td><td>${server.version}</td></tr>
      </table>
      
      <h5><span id="repocapabilities" class="toggle" onclick="return toggleDisplay(this)">+</span> CMIS Repository Capabilities</h5>
      <table id="repocapabilities_body" style="display: none;">
        <tr><td>ACL</td><td>none</td></tr>
        <tr><td>AllVersionsSearchable</td><td>${allVersionsSearchable?string}</td></tr>
        <tr><td>Changes</td><td>[#-- TODO --]none</td></tr>
        <tr><td>ContentStreamUpdatability</td><td>anytime</td></tr>
        <tr><td>GetDescendants</td><td>true</td></tr>
        <tr><td>Multifiling</td><td>true</td></tr>
        <tr><td>PWCSearchable</td><td>${pwcSearchable?string}</td></tr>
        <tr><td>PWCUpdateable</td><td>true</td></tr>
        <tr><td>Query</td><td>${querySupport}</td></tr>
        <tr><td>Join</td><td>${joinSupport}</td></tr>
        <tr><td>Renditions</td><td>none</td></tr>
        <tr><td>Unfiling</td><td>false</td></tr>
        <tr><td>VersionSpecificFiling</td><td>false</td></tr>
      </table>

      <p><em>Note: The contents of this repository may be cleaned at any time.</em><p>
      
      <a name="testatompub"></a>
      <h3>CMIS AtomPub TCK</h3>
      <p>Point the TCK (Test Compatibility Kit) at your CMIS Repository AtomPub Service Document. Provide credentials (or leave blank, if authentication not required) and adjust options as necessary. Hit the '<strong>Start TCK</strong>' button for a test report.</p>
      <p><em>Tip: Enable the 'Trace Reqs/Responses' option for examples of conversations with a CMIS Repository via AtomPub.</em></p>
      <p><em>Note: We have re-packaged and contributed this TCK to <a href="http://incubator.apache.org/chemistry/">Apache Chemistry</a>. We'll very soon replace this TCK with the Apache Chemistry version.</em></p>

      <form action="${url.serviceContext}/api/cmis/test" method="post" class="hform">
      <fieldset>
        <legend>CMIS Repository</legend>
        <p><label>Service Document</label><input type="text" name="url" size="50" value="${absurl(url.serviceContext)}/api/cmis"></p>
        <p><label>Username/Password</label><input type="text" name="user" value="admin/admin"></p>
      </fieldset>
      <fieldset>
        <legend>Options</legend>
        <p class="checkbox"><label>Validate Responses</label><input type="checkbox" name="validate" value="true" checked="checked"></p>
        <p class="checkbox"><label>Trace Reqs/Responses</label><input type="checkbox" name="trace" value="true"><p>
        <p><label>Tests (use * for wildcard)</label><input name="tests" value="*"></p>
        <p><label><span id="availtests" class="toggle" onclick="return toggleDisplay(this)">+</span> Available Tests</label>
        <table id="availtests_body" style="display: none;">
        [#list tests as test]<tr><td>${test}</td></tr>[/#list]
        </table></p>
      </fieldset>
      <p><input type="submit" name="submit" value="Start TCK" class="button"></p>
      </form>
   </div>
      
   <div id="resources">
      <h3>CMIS Resources</h3>
      <ul>
        <li><a href="http://www.oasis-open.org/committees/cmis">OASIS Technical Committee</a></li>
        <li><a href="http://xml.coverpages.org/cmis.html">Cover Pages</a></li>
        <li><a href="http://www.oasis-open.org/apps/org/workgroup/cmis/download.php/33431/Draft%2062f.zip"><strong>Specification v0.62</strong></a></li>
      </ul>
      <h3>Alfresco Resources</h3>
      <ul>
        <li><a href="http://wiki.alfresco.com/wiki/CMIS">CMIS Wiki</a></li>
        <li><a href="http://blogs.alfresco.com/cmis/">CMIS Blog</a></li>
        <li><a href="http://wiki.alfresco.com/wiki/Download_Community_Edition">Download</a> Repository and TCK</a></li>
        <li><a href="http://wiki.alfresco.com/wiki/Alfresco_SVN_Development_Environment">Source Code</a> for Repository and TCK</li>
      </ul>
      <h3>Provide Feedback</h3>
      <ul>
        <li><a href="http://forums.alfresco.com/en/viewforum.php?f=45">CMIS Forum</a></li>
        <li><a href="https://issues.alfresco.com/jira/secure/IssueNavigator.jspa?reset=true&mode=hide&pid=10103&sorter/order=DESC&sorter/field=priority&resolution=-1&component=10459">Find / Raise Issues</li>
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
