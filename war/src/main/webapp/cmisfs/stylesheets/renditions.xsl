<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License. 
-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:app="http://www.w3.org/2007/app"
	xmlns:atom="http://www.w3.org/2005/Atom" xmlns:cmis="http://docs.oasis-open.org/ns/cmis/core/200908/"
	xmlns:cmisra="http://docs.oasis-open.org/ns/cmis/restatom/200908/">

	<xsl:output method="html" />

	<xsl:param name="browseUrl"/>
	<xsl:param name="auxRoot"/>

	<xsl:template match="/">
		<html>
			<head>
				<title><xsl:value-of select="atom:entry/atom:title" /></title>
				<link rel="stylesheet" type="text/css" href="{$auxRoot}browser.css" />
			</head>
			<body>
				<img src="{$auxRoot}cmis.png" style="float: right;" />
				<h1>
				<xsl:choose>
					<xsl:when test="contains(atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:document')">
						<img src="{$auxRoot}document.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:when test="contains(atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:folder')">
						<img src="{$auxRoot}folder.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:when test="contains(atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:relationship')">
						<img src="{$auxRoot}relationship.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:when test="contains(atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:policy')">
						<img src="{$auxRoot}policy.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:when test="atom:entry/cmisra:type">
						<img src="{$auxRoot}type.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:otherwise>
						<img src="{$auxRoot}unknown.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:otherwise>
				</xsl:choose>
				<xsl:value-of select="atom:entry/atom:title" /></h1>

				<div class="navigationbox">
					<xsl:if test="atom:entry/atom:link[@rel='service']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='service']/@href}">Service</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='up']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='up']/@href}">Up</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='down']">
						<xsl:for-each select="atom:entry/atom:link[@rel='down']">
							<a href="{$browseUrl}{@href}">Down (<xsl:value-of select="@type" />)</a> -
						</xsl:for-each>
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/foldertree']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/foldertree']/@href}">Folder Tree</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:content">
						<a href="{atom:entry/atom:content/@src}">Download</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='version-history']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='version-history']/@href}">All Versions</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='alternate']">
						<xsl:for-each select="atom:entry/atom:link[@rel='alternate']">
							<a href="{@href}">Rendition (<xsl:value-of select="@cmisra:renditionKind"></xsl:value-of>)</a> -
						</xsl:for-each>
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/relationships']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/relationships']/@href}">Relationships</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/allowableactions']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/allowableactions']/@href}">Allowable Actions</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/acl']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/acl']/@href}">ACL</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/policies']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/policies']/@href}">Policies</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='describedby']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='describedby']/@href}">Type</a>
					</xsl:if>
				</div>

				<xsl:if test="atom:entry/atom:summary">
				<h2>Summary</h2>
				<div class="entrysummary">
					<xsl:value-of select="atom:entry/atom:summary" disable-output-escaping="yes"/>
				</div>
				</xsl:if>


				<xsl:if test="atom:entry/atom:link[@rel='alternate']">
				<h2>Renditions</h2>
				<table>
					<tr>
						<td>
						<xsl:if test="atom:entry/atom:link[@rel='alternate' and @cmisra:renditionKind='cmis:thumbnail']">
							<img src="{atom:entry/atom:link[@rel='alternate' and @cmisra:renditionKind='cmis:thumbnail']/@href}" />
						</xsl:if>
						</td>
						<td>
							<table class="entrytable">
								<tr>
									<th>Kind</th>
									<th>MIME Type</th>
									<th>Size</th>
									<th>Height</th>
									<th>Width</th>
								</tr>
							<xsl:for-each select="atom:entry/cmisra:object/cmis:rendition">
								<xsl:variable name="odd">
									<xsl:if test="(position() mod 2) != 1"></xsl:if>
									<xsl:if test="(position() mod 2) = 1">-odd</xsl:if>
								</xsl:variable>
								<xsl:variable name="renditionKind">
									<xsl:value-of select="cmis:kind"></xsl:value-of>
								</xsl:variable>
								<xsl:variable name="renditionURL">
									<xsl:for-each select="//atom:entry/atom:link[@rel='alternate']">
										<xsl:if test="@cmisra:renditionKind = $renditionKind">
											<xsl:value-of select="@href"></xsl:value-of>
										</xsl:if>
									</xsl:for-each>
								</xsl:variable>

								<tr>
									<td class="tdlinks{$odd}"><a href="{$renditionURL}"><xsl:value-of select="$renditionKind"></xsl:value-of></a></td>
									<td class="tdlinks{$odd}"><xsl:value-of select="cmis:mimetype"></xsl:value-of></td>
									<td class="tdlinks{$odd}"><xsl:value-of select="cmis:length"></xsl:value-of></td>
									<td class="tdlinks{$odd}"><xsl:value-of select="cmis:height"></xsl:value-of></td>
									<td class="tdlinks{$odd}"><xsl:value-of select="cmis:width"></xsl:value-of></td>
								</tr>
							</xsl:for-each>
							</table>
						</td>
					</tr>
				</table>
				</xsl:if>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>
