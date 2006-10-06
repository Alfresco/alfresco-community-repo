<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
		xmlns:xhtml="http://www.w3.org/1999/xhtml"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:alfresco="http://www.alfresco.org/alfresco">
  <xsl:output method="text"  encoding="UTF-8" indent="no" omit-xml-declaration="no" media-type="text/plain"/>

  <xsl:preserve-space elements="*"/>
  <xsl:param name="avm_store_url" select="'not_specified'"/>

  <xsl:template match="/">
--- <xsl:value-of select="/alfresco:press-release/alfresco:title"/> ---

-- <xsl:value-of select="/alfresco:press-release/alfresco:abstract"/> --
    <xsl:for-each select="/alfresco:press-release/alfresco:body">
      <xsl:if test="position()=1">
<xsl:value-of select="normalize-space(/alfresco:press-release/alfresco:location)"/>--<xsl:value-of select="normalize-space(/alfresco:press-release/alfresco:launch_date)"/>--
      </xsl:if>
<xsl:value-of select="normalize-space(.)" disable-output-escaping="yes"/>
    </xsl:for-each>
    <xsl:for-each select="/alfresco:press-release/alfresco:include_company_footer">
      <xsl:variable name="cf-id"><xsl:value-of select="."/></xsl:variable>
-- About <xsl:value-of select="document($cf-id)/alfresco:company-footer/alfresco:name"/> --
      <xsl:for-each select="document($cf-id)/alfresco:company-footer/alfresco:body">
<xsl:value-of select="." disable-output-escaping="yes"/>
      </xsl:for-each>
    </xsl:for-each>
    <xsl:if test="/alfresco:press-release/alfresco:include_media_contacts='true'">
-- Media Contacts --
John Newton
Alfresco Software Inc.
+44 1628 860639
press@alfresco.com

Chuck Tanowitz
Schwartz Communications
+1 781 684-0770
alfresco@schwartz-pr.com
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
