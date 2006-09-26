<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
		xmlns:xhtml="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:alfresco="http://www.alfresco.org/alfresco"
		exclude-result-prefixes="xhtml">
  <xsl:output method="html"  encoding="UTF-8" indent="yes"
                doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
                doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <xsl:preserve-space elements="*"/>
  <xsl:param name="avm_store_url" select="'not_specified'"/>

  <xsl:template match="/">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<title><xsl:value-of select="/alfresco:press-release/alfresco:title"/></title>
	<meta name="description" lang="en" >
	  <xsl:attribute name="content"><xsl:value-of select="/alfresco:press-release/alfresco:title"/></xsl:attribute>
        </meta>
	<link href="/assets/css/screen.css" rel="stylesheet" type="text/css" media="screen"></link>
	<link rel="alternate" type="application/rss+xml" title="RSS" href="http://www.nooked.com/news/feed/alfresco"></link>
	<style type="text/css">
	  p.leader {
	  font-weight: 700;
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
		<li><a href="/accessibility/">Accessibility</a> |</li>
		<li><a href="/sitemap/">Site Map</a> |</li>
		<li><a href="/about/contact/">Contact Us</a> |</li>
		<li><a href="/store/">Store</a> |</li>
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
	      <li id="home"><a href="/" accesskey="h"  ><span>Home</span></a></li>
	      <li id="products"><a href="/products/" accesskey="p"  ><span>Products</span></a></li>
	      <li id="services"><a href="/services/support/" accesskey="s"  ><span>Services</span></a></li>
	      <li id="customers"><a href="/customers/" accesskey="c"  ><span>Customers</span></a></li>
 	      <li id="partners"><a href="/partners/" accesskey="r"  ><span>Partners</span></a></li>
	      <li id="about"><a href="/about/" accesskey="a"  class="selected" ><span>About us</span></a></li>
	      <li id="developers"><a href="http://dev.alfresco.com/" accesskey="v"  ><span>Developers</span></a></li>
	      <li id="blogs"><a href="http://blogs.alfresco.com/" accesskey="b"  ><span>Blogs</span></a></li>
	      <li id="end"></li>
	    </ul>
	  </div>
	  <div id="outer">
	    <div id="inner">
	      <div id="sabout">
		<p><a href="/about/">Company</a>&#160;&#160;|&#160;&#160;<a href="/about/contact/">Contact</a>&#160;&#160;|&#160;&#160;<a href="/about/news/">News</a>&#160;&#160;|&#160;&#160;<a href="/about/events/">Events</a>&#160;&#160;|&#160;&#160;<a href="/about/people/">People</a></p>
	      </div>
	    </div>
	  </div>
	  <div id="content">&#160;</div>
	  <!-- Main Content -->
	  <div id="main_content">

	    <!-- BEGIN MAIN CONTENT -->
	    <h1><xsl:value-of select="/alfresco:press-release/alfresco:title"/></h1>  
	    <p><strong><xsl:value-of select="/alfresco:press-release/alfresco:abstract"/></strong></p>
	    <p></p>
	    <xsl:for-each select="/alfresco:press-release/alfresco:body">
	      <p>
	        <xsl:if test="position()=1"><xsl:value-of select="normalize-space(/alfresco:press-release/alfresco:location)"/>&#8212;<xsl:value-of select="normalize-space(/alfresco:press-release/alfresco:launch_date)"/>&#8212;</xsl:if><xsl:value-of select="." disable-output-escaping="yes"/>
	      </p>
	    </xsl:for-each>
	    <xsl:for-each select="/alfresco:press-release/alfresco:include_about_blurb">
              <xsl:variable name="blurb-id"><xsl:value-of select="."/></xsl:variable>
	      <xsl:variable name="blurb-url"><xsl:value-of select="concat('/media/releases/content/about_blurbs/', concat($blurb-id, '.xml'))"/></xsl:variable>
              <h2>About <xsl:value-of select="document($blurb-url)/alfresco:about-blurb/alfresco:name"/></h2>
	      <xsl:for-each select="document($blurb-url)/alfresco:about-blurb/alfresco:body">
		<p><xsl:value-of select="." disable-output-escaping="yes"/></p>
	      </xsl:for-each>
	    </xsl:for-each>
	    <xsl:if test="/alfresco:press-release/alfresco:include_media_contacts='true'">
              <h2>Media Contacts</h2>  
              <div><p>John Newton<br />Alfresco Software Inc.<br />+44 1628 860639<br />press@alfresco.com</p></div>
              <div><p>Chuck Tanowitz<br />Schwartz Communications<br />+1 781 684-0770<br />alfresco@schwartz-pr.com</p></div>
	    </xsl:if>
	    <!-- END MAIN CONTENT -->
	  </div>
	  <!-- Feature Content -->
	  <div id="right_content">&#160;</div>
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
		<a href="/">Home</a> | 
		<a href="/legal/">Legal</a> | 
		<a href="/privacy/">Privacy</a> | 
		<a href="/accessibility/">Accessibility</a> | 
		<a href="/sitemap/">Site Map</a> | 
		<a href="/rss/?feed=all/">RSS</a>
		<img src="/assets/images/icons/feedicon12.gif" alt="RSS" title="RSS" width="12" height="12" />
	      </p>
	      <p>
		<a href="/about/">Open Source ECMS</a> | 
		<a href="/products/">CMS Products</a> | 
		<a href="/services/support/">Management Services</a> | 
		<a href="/resources/">EMS Resources</a>
	      </p>
	      <p>&#169; 2005-2006 Alfresco Software, Inc., All Rights Reserved</p>
	      <p><img src="/assets/images/icons/powered_by_alfresco.gif" alt="Powered by Alfresco" width="88" height="32" /></p>
	    </div>
	  </div>
	  <div style="clear:both; padding-bottom: 10px;"></div>
	</div>
	<div style="clear:both; padding-bottom: 20px;"></div>
	<!-- End Footer -->
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
