<?xml version="1.0" encoding="UTF-8"?>
<!--
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"

-->
<xsl:stylesheet version="1.0" xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:article="http://www.alfresco.org/alfresco/article"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions" exclude-result-prefixes="xhtml">
	<xsl:output method="html" encoding="UTF-8" indent="yes"
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

	<xsl:preserve-space elements="*"/>

	<xsl:template match="/">
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
							<h1>
								<xsl:value-of select="/article:article/article:title"  disable-output-escaping="yes"/>
							</h1>
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
					<tr>
						<td valign="top">
							<xsl:for-each select="/article:article/article:body">
								<xsl:variable name="page_number"  select="1+count(preceding-sibling::*[name()=name(current())])"/>
								<p>
									<xsl:if test="/article:article/article:photo[article:page_number=$page_number]">
									<xsl:element name="table">
										<xsl:attribute name="align">
											<xsl:if test="$page_number mod 2 = 1">
												<xsl:text>left</xsl:text>
											</xsl:if>	
											<xsl:if test="$page_number mod 2 = 0">
												<xsl:text>right</xsl:text>
											</xsl:if>	
										</xsl:attribute>	
									<tr>
										<td>
									<xsl:element name="img">
										<xsl:attribute name="src">
											<xsl:value-of select="/article:article/article:photo[article:page_number=$page_number]/article:image"/>
										</xsl:attribute>
									</xsl:element>
										</td>
									</tr>
									<tr>
										<td width='200'>
											<h6><xsl:value-of select="/article:article/article:photo[article:page_number=$page_number]/article:caption"  disable-output-escaping="yes"/></h6>
										</td>	
									</tr>
									</xsl:element>	
									</xsl:if>
									<xsl:if test="position()=1">
										<b>
											<xsl:value-of select="normalize-space(/article:article/article:location)"  disable-output-escaping="yes"/>
										</b>
										&#8212;
									</xsl:if>
									<xsl:value-of select="normalize-space(.)" disable-output-escaping="yes"/>
								</p>
							</xsl:for-each>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
