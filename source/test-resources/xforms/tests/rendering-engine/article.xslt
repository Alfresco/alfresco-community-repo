<?xml version="1.0" encoding="UTF-8"?>
<!--
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
-->
<xsl:stylesheet version="1.0"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ar="http://www.alfresco.org/alfresco/article"
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
                exclude-result-prefixes="xhtml">

  <xsl:output method="html"
              encoding="UTF-8"
              indent="yes"
              doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
              doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" />

  <xsl:preserve-space elements="*" />

  <xsl:template match="/">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
        <title>
          <xsl:value-of select="/ar:Article/ar:Title" disable-output-escaping="yes" />
        </title>
        <xsl:if test="/ar:Article/ar:Summary">
          <meta name="description" lang="en">
            <xsl:attribute name="content">
              <xsl:value-of select="/ar:Article/ar:Summary" />
            </xsl:attribute>
          </meta>
        </xsl:if>
        <xsl:if test="/ar:Article/ar:Keyword">
          <meta name="keyword" lang="en">
            <xsl:attribute name="content">
              <xsl:for-each select="/ar:Article/ar:Keyword">
                <xsl:value-of select="normalize-space(.)" />
                <xsl:choose>
                  <xsl:when test="position() != last()">,</xsl:when>
                </xsl:choose>              
              </xsl:for-each>
            </xsl:attribute>
          </meta>
         </xsl:if>
      </head>
      <body>
        <h1><xsl:value-of select="/ar:Article/ar:Title" disable-output-escaping="yes" /></h1>
        <p><xsl:value-of select="/ar:Article/ar:Body" disable-output-escaping="yes" /></p>
        <xsl:if test="/ar:Article/ar:Image">
          <img>
            <xsl:attribute name="src">
              <xsl:value-of select="/ar:Article/ar:Image" disable-output-escaping="yes" />
            </xsl:attribute>
          </img>
        </xsl:if>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>
