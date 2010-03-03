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
<xsl:stylesheet version="1.0" xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:article="http://www.alfresco.org/alfresco/article"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions" exclude-result-prefixes="xhtml fn java article xsl">
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<xsl:preserve-space elements="*"/>

	<xsl:template match="/">
		<item>
	    		<title><xsl:value-of select="/article:article/article:title"  disable-output-escaping="yes"/></title>
	    		<link>/views/pages/details.jsp?content=/content/article/<xsl:value-of select="fn:replaceAll(string($alf:form_instance_data_file_name), '.xml', '.html')"/>&amp;title=<xsl:value-of select="java:java.net.URLEncoder.encode(/article:article/article:title,'UTF-8')"  disable-output-escaping="yes"/></link>
	    		<description><xsl:value-of select="normalize-space(/article:article/article:teaser)"/></description>
	    		<pubDate>2000-01-01T12:00:00.000-00:00</pubDate>
	  	</item>
	</xsl:template>
</xsl:stylesheet>
