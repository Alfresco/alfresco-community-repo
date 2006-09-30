<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
	  xmlns:pr="http://www.alfresco.org/pr"
          xmlns:fmt="http://java.sun.com/jsp/jstl/fmt">

 <jsp:output doctype-root-element="html"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
    doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <jsp:directive.page language="java" contentType="text/html; charset=UTF-8"/>
    <jsp:directive.page isELIgnored="false"/>
  
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
  <p><a href="/about/index.html">Company</a>&amp;nbsp;&amp;nbsp;|&amp;nbsp;&amp;nbsp;<a href="/about/contact/index.html">Contact</a>&amp;nbsp;&amp;nbsp;|&amp;nbsp;&amp;nbsp;<a href="/media/releases/index.jsp">News</a>&amp;nbsp;&amp;nbsp;|&amp;nbsp;&amp;nbsp;<a href="/about/events/index.html">Events</a>&amp;nbsp;&amp;nbsp;|&amp;nbsp;&amp;nbsp;<a href="/about/people/index.html">People</a></p>
  </div>
  </div>
  </div>
  <div id="content"></div>
  <!-- Main Content -->
  <div id="main_content">
  
  <!-- BEGIN MAIN CONTENT -->
  
  <h1>Alfresco Press Releases</h1>
  
  <c:forEach items="${pr:getPressReleases(pageContext)}" var="pressRelease">
    <h2 class="headline">
      <jsp:element name="a">
        <jsp:attribute name="href"><c:out value="${pressRelease.href}"/></jsp:attribute>
	<jsp:body><c:out value="${pressRelease.title}"/></jsp:body>
      </jsp:element>
    </h2>
    <p class="date"><fmt:formatDate value="${pressRelease.launchDate}" dateStyle="long"/></p>
    <p class="abstract"><c:out value="${pressRelease.abstract}"/></p>
  </c:forEach>

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
  <div id="clear">&#160;</div>
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
  	<p>&amp;copy; 2005-2006 Alfresco Software, Inc., All Rights Reserved</p>
  	<p><img src="/assets/images/icons/powered_by_alfresco.gif" alt="Powered by Alfresco" width="88" height="32" /></p>
  	</div>
    </div>
    <div style="clear:both; padding-bottom: 10px;">&#160;</div>
  </div>
  <div style="clear:both; padding-bottom: 20px;">&#160;</div>
  <!-- End Footer -->
  </body>
  </html>
</jsp:root>
