<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:alfresco="http://www.alfresco.org/alfresco"
    exclude-result-prefixes="xhtml">
<!--
    <xsl:character-map name="htmlentities">
     <xsl:output-character character="&#160;" string="&amp;nbsp;" />
     <xsl:output-character character="&#8212;" string="&amp;mdash;" />
     <xsl:output-character character="&#169;" string="&amp;copy;" />
    </xsl:character-map> 
-->
    <xsl:output method="html" version="4.01" encoding="UTF-8" indent="yes"
		use-character-maps="htmlentities"
                doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
                doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
    <xsl:preserve-space elements="*"/>

    <xsl:template match="/">
</xsl:stylesheet>
