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
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions" exclude-result-prefixes="xhtml">
	<xsl:output method="html" encoding="UTF-8" indent="yes"
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

	<xsl:preserve-space elements="*"/>

	<xsl:template match="/">
		<xsl:text disable-output-escaping="yes">&lt;%String channel = (String) request.getParameter(&quot;channel&quot;);%&gt;</xsl:text>
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
			<head>
				<title>
					<xsl:value-of select="/article:article/article:title"  disable-output-escaping="yes"/>
				</title>
				<meta name="description" lang="en">
					<xsl:attribute name="content">
						<xsl:value-of select="/article:article/article:title"  disable-output-escaping="yes"/>
					</xsl:attribute>
				</meta>
			</head>
			<body>

				<table>
					<tr>
						<td>
							<b>
								<xsl:element name="a">
								      	<xsl:attribute name="href">/views/pages/details.jsp?channel=&lt;%=channel%&gt;&amp;content=/content/article/<xsl:value-of select="fn:replaceAll(string($alf:form_instance_data_file_name), '.xml', '.html')"/>&amp;title=<xsl:value-of select="java:java.net.URLEncoder.encode(/article:article/article:title,'UTF-8')"  disable-output-escaping="yes"/></xsl:attribute>
								      	<xsl:value-of select="/article:article/article:title"  disable-output-escaping="yes"/>
    								</xsl:element>
							</b>
							<br/>
							<i>
								<xsl:value-of select="/article:article/article:author"  disable-output-escaping="yes"/>,<xsl:value-of select="/article:article/article:launch_date"/>
							</i>
						</td>
					</tr>
					<tr>
						<td>
							<p>
							<xsl:if test="/article:article/article:photo[article:page_number='0']">
								<xsl:element name="img">
									<xsl:attribute name="src">
										<xsl:value-of select="/article:article/article:photo[article:page_number='0']/article:image"/>
									</xsl:attribute>
									<xsl:attribute name="align">
										<xsl:text>left</xsl:text>
									</xsl:attribute>
								</xsl:element>							
							</xsl:if>
								<xsl:value-of select="normalize-space(/article:article/article:teaser)"  disable-output-escaping="yes"/>
							</p>
						</td>
							
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
