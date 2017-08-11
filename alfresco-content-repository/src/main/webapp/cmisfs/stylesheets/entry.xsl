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
	<xsl:param name="browseOverrideStylesheet"/>

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
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/relationships']/@href}&amp;relationshipDirection=either{$browseOverrideStylesheet}relationships">Relationships</a> -
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

				<xsl:if test="contains(atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:relationship')">
				<h2>Relationship</h2>
				<table class="entrytable">
					<tr>
						<th>Source Id</th>
						<th>Target Id</th>
					</tr>
					<tr>
						<xsl:variable name="objectId">
							<xsl:value-of select="atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:objectId']/cmis:value"></xsl:value-of>
						</xsl:variable>
						<xsl:variable name="entryUrl">
							<xsl:value-of select="atom:entry/atom:link[@rel='self']/@href"></xsl:value-of>
						</xsl:variable>
						<xsl:variable name="sourceId">
							<xsl:value-of select="atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:sourceId']/cmis:value"></xsl:value-of>
						</xsl:variable>
						<xsl:variable name="targetId">
							<xsl:value-of select="atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:targetId']/cmis:value"></xsl:value-of>
						</xsl:variable>
						<xsl:variable name="sourceEntryUrl">
							<xsl:value-of select="substring-before($entryUrl, $objectId)"></xsl:value-of>
							<xsl:value-of select="$sourceId"></xsl:value-of>
						</xsl:variable>
						<xsl:variable name="targetEntryUrl">
							<xsl:value-of select="substring-before($entryUrl, $objectId)"></xsl:value-of>
							<xsl:value-of select="$targetId"></xsl:value-of>
						</xsl:variable>

						<td style="font-weight: bold;"><a href="{$browseUrl}{$sourceEntryUrl}"><xsl:value-of select="$sourceId"></xsl:value-of></a></td>
						<td style="font-weight: bold;"><a href="{$browseUrl}{$targetEntryUrl}"><xsl:value-of select="$targetId"></xsl:value-of></a></td>
					</tr>
				</table>
				</xsl:if>

				<h2>Properties</h2>
				<table class="entrytable">
				<xsl:for-each select="atom:entry/cmisra:object/cmis:properties/*">
					<tr>
						<td style="font-weight: bold;"><xsl:value-of select="@propertyDefinitionId" /></td>
						<td>
						<xsl:for-each select="cmis:value">
							<xsl:value-of select="current()" /><br/>
						</xsl:for-each>
						</td>
					</tr>
				</xsl:for-each>
				<xsl:for-each select="atom:entry/cmisra:type/*[not(*)]">
					<tr>
						<td style="font-weight: bold;"><xsl:value-of select="local-name()" /></td>
						<td><xsl:value-of select="current()" /></td>
					</tr>
				</xsl:for-each>
				<xsl:if test="atom:entry/cmisra:pathSegment">
					<tr>
						<td style="font-weight: bold; font-style:italic;">Path Segment</td>
						<td style="font-style:italic;"><xsl:value-of select="atom:entry/cmisra:pathSegment" /></td>
					</tr>
				</xsl:if>
				</table>

				<xsl:if test="atom:entry/cmisra:type">
					<h2>Property Definitions</h2>
					<xsl:for-each select="atom:entry/cmisra:type/*[*]">
						<h3><xsl:value-of select="cmis:id"/></h3>
						<table class="entrytable">
						<xsl:for-each select="*">
							<tr>
								<td><xsl:value-of select="local-name()" /></td>
								<td><xsl:value-of select="current()" /></td>
							</tr>
						</xsl:for-each>
						</table>
					</xsl:for-each>
				</xsl:if>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>
