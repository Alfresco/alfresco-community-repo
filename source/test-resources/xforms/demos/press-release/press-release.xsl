<?xml version="1.0" encoding="UTF-8"?>
<!--
 * Copyright (C) 2005-2007 Alfresco Software Limited.

Licensed under the Mozilla Public License version 1.1 
with a permitted attribution clause. You may obtain a
copy of the License at

  http://www.alfresco.org/legal/license.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific
language governing permissions and limitations under the
License.


Produces an html rendition of a press release
-->
<xsl:stylesheet version="1.0"
		xmlns:xhtml="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:pr="http://www.alfresco.org/alfresco/pr"
		xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
		exclude-result-prefixes="xhtml">
  <xsl:output method="html"  encoding="UTF-8" indent="yes"
              doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
              doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <xsl:preserve-space elements="*"/>

  <xsl:template match="/">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
	<!-- include common navigation components using SSIs (see web.xml for more information) -->
	<xsl:comment>#include virtual="/assets/include_in_head.html"</xsl:comment>
	<title><xsl:value-of select="/pr:press_release/pr:title"/></title>
	<meta name="description" lang="en" >
	  <xsl:attribute name="content"><xsl:value-of select="/pr:press_release/pr:title"/></xsl:attribute>
        </meta>
	<style type="text/css">
	  p.leader {
	  font-weight: 700;
	  }
	</style>
      </head>
      <body>
        <div id="container">
	  <xsl:comment>#include virtual="/assets/include_main_navigation.html"</xsl:comment>
	  <xsl:comment>#include virtual="/about/navigation.html"</xsl:comment>
	  <div id="content">&#160;</div>
	  <!-- Main Content -->
	  <div id="main_content">

	    <!-- BEGIN MAIN CONTENT -->
	    <h1><xsl:value-of select="/pr:press_release/pr:title"/></h1>  
	    <p><strong><xsl:value-of select="/pr:press_release/pr:abstract"/></strong></p>
	    <p></p>
	    <xsl:for-each select="/pr:press_release/pr:body">
	      <p>
	        <xsl:if test="position()=1"><xsl:value-of select="normalize-space(/pr:press_release/pr:location)"/>&#8212;<xsl:value-of select="normalize-space(/pr:press_release/pr:launch_date)"/>&#8212;</xsl:if><xsl:value-of select="normalize-space(.)" disable-output-escaping="yes"/>
	      </p>
	    </xsl:for-each>
	    <xsl:for-each select="/pr:press_release/pr:include_company_footer">
              <xsl:variable name="cf-id"><xsl:value-of select="."/></xsl:variable>
	      <!-- load the xml document for the company footer using a built in FormDataFunction -->
	      <xsl:variable name="cf" select="alf:parseXMLDocument($cf-id)"/>
              <h2>About <xsl:value-of select="$cf/pr:name"/></h2>
	      <xsl:for-each select="$cf/pr:body">
		<p><xsl:value-of select="." disable-output-escaping="yes"/></p>
	      </xsl:for-each>
	    </xsl:for-each>
	    <xsl:if test="/pr:press_release/pr:include_media_contacts='true'">
              <h2>Media Contacts</h2>  
              <div><p>John Newton<br />Alfresco Software Inc.<br />+44 1628 860639<br />press@alfresco.com</p></div>
              <div><p>Chuck Tanowitz<br />Schwartz Communications<br />+1 781 684-0770<br />alfresco@schwartz-pr.com</p></div>
	    </xsl:if>
	    <!-- END MAIN CONTENT -->
	    <xsl:element name="a">
	      <xsl:attribute name="href">
		<xsl:value-of select="fn:replaceAll(string($alf:form_instance_data_file_name), '.xml', '.txt')"/>
	      </xsl:attribute>
	      <xsl:text>view plain text version</xsl:text>
	    </xsl:element>
	  </div>
	  <!-- Feature Content -->
	  <div id="right_content">
	    <div class="box_blue">
	      <h2>Press Releases By Category</h2>

	      <!-- store the current category in a variable for later comparison -->
	      <xsl:variable name="my_category" 
			    select="/pr:press_release/pr:category"/>
	      <!-- store the current title in a variable for later comparison -->
	      <xsl:variable name="my_title" 
			    select="/pr:press_release/pr:title"/>
	      <!-- load all press releases into a variable by calling into a form data function -->
	      <xsl:variable name="all_press_releases" 
			    select="alf:parseXMLDocuments('press-release')"/>
	      <ul>
		<!-- select a unique set of categories for the first level navigation -->
		<xsl:for-each select="$all_press_releases[not(pr:category=preceding-sibling::pr:press_release/pr:category)]">
		  <xsl:sort select="pr:category"/>
		  <li> 
		    <xsl:choose>
		      <!-- for the current category, present all press releases in this category -->
		      <xsl:when test="pr:category=$my_category">
			<b><xsl:value-of select="pr:category"/></b>
			<ul>
			  <!-- iterate all press releases which are in my_category -->
			  <xsl:for-each select="$all_press_releases[pr:category=$my_category]">
			    <xsl:sort select="pr:title"/>
			    <li>
			      <xsl:element name="a">
				<xsl:if test="$my_title=pr:title">
				  <xsl:attribute name="style">font-weight:bold;</xsl:attribute>
				</xsl:if>
				<xsl:attribute name="href">
				  <xsl:value-of select="fn:replaceAll(string(@alf:file_name), '.xml', '.html')"/>
				</xsl:attribute>
				<xsl:value-of select="pr:title"/>
			      </xsl:element>
			    </li>
			  </xsl:for-each>
			</ul>
		      </xsl:when>
		      <xsl:otherwise>
			<!-- 
			for other categories present a link to the first document in that category 
			with the category label
			-->
			<xsl:element name="a">
			  <xsl:attribute name="href">
			    <xsl:value-of select="fn:replaceAll(string(@alf:file_name), '.xml', '.html')"/>
			  </xsl:attribute>
			  <xsl:value-of select="pr:category"/>
			</xsl:element>
		      </xsl:otherwise>
		    </xsl:choose>
		  </li>
		</xsl:for-each>
	      </ul>
	      <h2>Press Release Archive</h2>
	      <ul>
		<li><a href="/media/releases/archives/index.html">View Archived Releases</a></li>
	      </ul>
	    </div>
	  </div>
	  <div id="clear">&#160;</div>
	</div>
	<!--All Three End -->
	<xsl:comment>#include virtual="/assets/footer.html"</xsl:comment>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
