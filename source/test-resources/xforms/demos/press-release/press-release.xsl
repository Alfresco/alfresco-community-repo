<!--
Copyright (C) 2005 Alfresco, Inc.

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
-->
<!-- Produces an html rendition of a press release -->
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
		xmlns:xhtml="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:alfresco="http://www.alfresco.org/alfresco"
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
	<title><xsl:value-of select="/alfresco:press-release/alfresco:title"/></title>
	<meta name="description" lang="en" >
	  <xsl:attribute name="content"><xsl:value-of select="/alfresco:press-release/alfresco:title"/></xsl:attribute>
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
	    <h1><xsl:value-of select="/alfresco:press-release/alfresco:title"/></h1>  
	    <p><strong><xsl:value-of select="/alfresco:press-release/alfresco:abstract"/></strong></p>
	    <p></p>
	    <xsl:for-each select="/alfresco:press-release/alfresco:body">
	      <p>
	        <xsl:if test="position()=1"><xsl:value-of select="normalize-space(/alfresco:press-release/alfresco:location)"/>&#8212;<xsl:value-of select="normalize-space(/alfresco:press-release/alfresco:launch_date)"/>&#8212;</xsl:if><xsl:value-of select="normalize-space(.)" disable-output-escaping="yes"/>
	      </p>
	    </xsl:for-each>
	    <xsl:for-each select="/alfresco:press-release/alfresco:include_company_footer">
              <xsl:variable name="cf-id"><xsl:value-of select="."/></xsl:variable>
	      <!-- load the xml document for the company footer using a built in FormDataFunction -->
	      <xsl:variable name="cf" select="alfresco:getXMLDocument($cf-id)"/>
              <h2>About <xsl:value-of select="$cf/alfresco:company-footer/alfresco:name"/></h2>
	      <xsl:for-each select="$cf/alfresco:company-footer/alfresco:body">
		<p><xsl:value-of select="." disable-output-escaping="yes"/></p>
	      </xsl:for-each>
	    </xsl:for-each>
	    <xsl:if test="/alfresco:press-release/alfresco:include_media_contacts='true'">
              <h2>Media Contacts</h2>  
              <div><p>John Newton<br />Alfresco Software Inc.<br />+44 1628 860639<br />press@alfresco.com</p></div>
              <div><p>Chuck Tanowitz<br />Schwartz Communications<br />+1 781 684-0770<br />alfresco@schwartz-pr.com</p></div>
	    </xsl:if>
	    <!-- END MAIN CONTENT -->
	    <xsl:element name="a">
	      <xsl:attribute name="href"><xsl:value-of select="fn:replaceAll($derived_from_file_name, '.xml', '.txt')"/></xsl:attribute>
	      <xsl:text>view plain text version</xsl:text>
	    </xsl:element>
	  </div>
	  <!-- Feature Content -->
	  <div id="right_content">&#160;</div>
	  <div id="clear">&#160;</div>
	</div>
	<!--All Three End -->
	<xsl:comment>#include virtual="/assets/footer.html"</xsl:comment>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
