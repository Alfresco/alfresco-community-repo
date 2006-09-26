<jsp:directive.page import="java.io.*"/>
<jsp:directive.page import="java.util.*"/>
<jsp:directive.page import="org.alfresco.jndi.*"/>
<jsp:directive.page import="org.alfresco.repo.avm.AVMRemote"/>
<jsp:directive.page import="org.alfresco.service.cmr.avm.AVMNodeDescriptor"/>
<jsp:directive.page import="org.w3c.dom.*"/>
<jsp:directive.page import="javax.xml.parsers.*"/>
<jsp:directive.page import="java.text.*"/>
<%!
class PressRelease
{
    public final String title;
    public final String theAbstract;
    public final Date date;
    public final String href;

    public PressRelease(String title, String theAbstract, Date d, String href)
    {
        this.title = title;
        this.theAbstract = theAbstract;
        this.date = d;
        this.href = href;
    }
}

public List<PressRelease> getPressReleases(HttpServletRequest request, ServletContext servletContext, JspWriter out)
   throws Exception
{
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setValidating(false);
    final DocumentBuilder db = dbf.newDocumentBuilder();

    // The real_path will look somethign like this:
    //   /alfresco.avm/avm.alfresco.localhost/$-1$alfreco-guest-main:/appBase/avm_webapps/my_webapp
    //
    String real_path = servletContext.getRealPath("/media/releases/content");

    // The avm_path to the root of the context will look soemthign like this:
    //    alfreco-guest-main:/appBase/avm_webapps/my_webapp
    //
    String avm_path = real_path.substring( real_path.indexOf('$', real_path.indexOf('$') + 1)  + 1 );

    AVMRemote  avm_remote = AVMFileDirContext.getAVMRemote();
    Map< String, AVMNodeDescriptor> entries = avm_remote.getDirectoryListing(-1, avm_path);

    List<PressRelease> result = new LinkedList<PressRelease>();
    for ( Map.Entry<String, AVMNodeDescriptor> entry : entries.entrySet() )
    {
        String entry_name = entry.getKey();
        AVMNodeDescriptor entry_node = entry.getValue();
        if (entry_node.isFile())
        {
             InputStream istream = new AVMRemoteInputStream(  avm_remote.getInputHandle( -1, avm_path + '/' + entry_name ), avm_remote );
             try
             {
                 Document d = db.parse(istream);
                 if ("alfresco:press-release".equals(d.getDocumentElement().getNodeName()))
                 {
                     Element t = (Element)d.getElementsByTagName("alfresco:title").item(0);
                     Element a = (Element)d.getElementsByTagName("alfresco:abstract").item(0);
                     Element dateEl = (Element)d.getElementsByTagName("alfresco:launch_date").item(0);
                     Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateEl.getFirstChild().getNodeValue());
                     String href = "/media/releases/content/" + entry_name;
                     href = href.replaceAll(".xml$", ".shtml");
                     result.add(new PressRelease(t.getFirstChild().getNodeValue(),
                                                 a.getFirstChild().getNodeValue(),
                                                 date,
                                                 href));
                 }
             }
             catch (Throwable t)
             {
                 t.printStackTrace();
             }
             finally
             {
                 istream.close();
             }
         }
     }
     return result;
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Alfresco Press Releases - Open Source Content Management</title>
<meta name="description" lang="en" content="News and press releases about Alfresco's enterprise content management system and document management software." />
<meta name="keywords" lang="en" content="open source ecms, enterprise content management system, cms, document management system, dms, documentum" />
<link href="/assets/css/screen.css" rel="stylesheet" type="text/css" media="screen" />
<link rel="alternate" type="application/rss+xml" title="RSS" href="http://www.nooked.com/news/feed/alfresco" />
<style type="text/css">
#main_content .headline {
	font-size: 1.2em;
	border-bottom: none;
	margin-bottom: 0.25em;
}
#main_content .date {
	color: #666666;
	font-size: 0.9em;
	margin-top: 0;
	margin-bottom: 0.25em;
}
#main_content .abstract {
	margin-top: 0;
}
</style>
</head>
<body>
<script language="JavaScript" type="text/javascript" src="/assets/js/controls.js"></script>
<script language="JavaScript" type="text/javascript" src="/assets/js/search.js" ></script>
<div id="container">
<div id="masthead">
<!-- Search -->
<div id="top_links">
<ul>
<li><a href="/accessibility/index.html">Accessibility</a> |</li>
<li><a href="/sitemap/index.html">Site Map</a> |</li>
<li><a href="/about/contact/index.html">Contact Us</a> |</li>
<li><a href="/store/index.html">Store</a> |</li>
<li><a href="/rss/?feed=all">RSS</a> <img src="/assets/images/icons/feedicon10.gif" alt="RSS" title="RSS" width="10" height="10" /></li>
</ul>
<form action="http://www.google.com/search" method="get"><br />
<input type="hidden" name="sitesearch" value="www.alfresco.com" />
<input type="hidden" name="ie" value="UTF-8" />
<input type="hidden" name="oe" value="UTF-8" />
<input class="searchbox" type="text" size="20" value="Search..." name="query" onfocus="if (this.value == 'Search...') this.value = ''; " onblur="if (this.value == '') this.value = 'Search...';" />
<input class="searchbutton" type="submit" name="" value="Go" />
</form>
</div>
<!-- End Search -->
</div>
<div id="topnav">
  <ul id="nav">
    <li id="home"><a href="/index.html" accesskey="h"  ><span>Home</span></a></li>
    <li id="products"><a href="/products/index.html" accesskey="p"  ><span>Products</span></a></li>
    <li id="services"><a href="/services/support/index.html" accesskey="s"  ><span>Services</span></a></li>
    <li id="customers"><a href="/customers/index.html" accesskey="c"  ><span>Customers</span></a></li>
 	<li id="partners"><a href="/partners/index.html" accesskey="r"  ><span>Partners</span></a></li>
    <li id="about"><a href="/about/index.html" accesskey="a"  class="selected" ><span>About us</span></a></li>
	<li id="developers"><a href="http://dev.alfresco.com/" accesskey="v"  ><span>Developers</span></a></li>
	<li id="blogs"><a href="http://blogs.alfresco.com/" accesskey="b"  ><span>Blogs</span></a></li>
	<li id="end"></li>
  </ul>
</div>
<div id="outer">
  <div id="inner">
<div id="sabout">
<p><a href="/about/index.html">Company</a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="/about/contact/index.html">Contact</a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="/media/releases/index.jsp">News</a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="/about/events/index.html">Events</a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="/about/people/index.html">People</a></p>
</div>
</div>
</div>
<div id="content"></div>
<!-- Main Content -->
<div id="main_content">

<!-- BEGIN MAIN CONTENT -->

<h1>Alfresco Press Releases</h1>

<%
List<PressRelease> pressReleases = getPressReleases(request, application, out);
for (PressRelease pr : pressReleases)
{
%>
<h2 class="headline"><a href="<%= pr.href %>"><%= pr.title %></a></h2>
<p class="date"><%= DateFormat.getDateInstance(DateFormat.LONG).format(pr.date) %></p><p class="abstract"><%= pr.theAbstract %></p>
<%
}
%>

<!-- END MAIN CONTENT -->

</div>
<!-- Feature Content -->
<div id="right_content">
<div class="box_blue">
<h2>Press Release Archive</h2>
<ul>
<li><a href="/media/releases/archives/index.html">View Archived Releases</a></li>
</ul>
</div>
</div>
<div id="clear"></div>
</div>
<!--All Three End -->
<!-- Footer -->
<div id="footer">
  <div id="site">
    <div id="footer-logos">
    <a href="http://www.mysql.com/"><img src="/assets/images/footer/mysql.gif" alt="MySQL" title="MySQL" border="0" /></a>
    <a href="http://www.jboss.org"><img src="/assets/images/footer/jboss.gif" alt="JBoss Certified Partner" title="JBoss Certified Partner" border="0" width="74" height="34" /></a>
    <a href="http://www.springframework.org/"><img src="/assets/images/footer/spring.gif" alt="Spring Framework" title="Spring Framework" border="0" width="67" height="34" /></a>
    <a href="http://www.hibernate.org/"><img src="/assets/images/footer/hibernate.gif" alt="Hibernate" title="Hibernate" border="0" width="111" height="34" /></a>
    <a href="http://tomcat.apache.org"><img src="/assets/images/footer/tomcat.gif" alt="Tomcat" title="Tomcat" border="0" width="44" height="34" /></a>
    <a href="http://lucene.apache.org/"><img src="/assets/images/footer/lucene.gif" alt="Lucene" title="Lucene" border="0" width="143" height="34" /></a>
    <a href="http://myfaces.apache.org/"><img src="/assets/images/footer/myfaces.gif" alt="My Faces" title="My Faces" border="0" width="37" height="34" /></a></div>
    <div id="footer-links">
    <p>
    <a href="/index.html">Home</a> | 
    <a href="/legal/index.html">Legal</a> | 
    <a href="/privacy/index.html">Privacy</a> | 
	<a href="/accessibility/index.html">Accessibility</a> | 
	<a href="/sitemap/index.html">Site Map</a> | 
	<a href="/rss/?feed=all/">RSS</a>
	<img src="/assets/images/icons/feedicon12.gif" alt="RSS" title="RSS" width="12" height="12" />
	</p>
    <p>
    <a href="/about/index.html">Open Source ECMS</a> | 
    <a href="/products/index.html">CMS Products</a> | 
    <a href="/services/support/index.html">Management Services</a> | 
    <a href="/resources/index.html">EMS Resources</a>
    </p>
	<p>&copy; 2005-2006 Alfresco Software, Inc., All Rights Reserved</p>
	<p><img src="/assets/images/icons/powered_by_alfresco.gif" alt="Powered by Alfresco" width="88" height="32" /></p>
	</div>
  </div>
  <div style="clear:both; padding-bottom: 10px;"></div>
</div>
<div style="clear:both; padding-bottom: 20px;"></div>
<!-- End Footer -->
</body>
</html>
