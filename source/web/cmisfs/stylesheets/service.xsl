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
				<title>Service</title>
				<link rel="stylesheet" type="text/css" href="{$auxRoot}browser.css" />
			</head>
			<body>
				<img src="{$auxRoot}cmis.png" style="float: right;" />
				<H1>Repositories</H1>
				<xsl:for-each select="app:service/app:workspace">
					<div class="servicebox">
						<h2>Repository <xsl:value-of select="cmisra:repositoryInfo/cmis:repositoryId" /></h2>
						<h3>Collections:</h3><ul><xsl:apply-templates select="app:collection" /></ul>
						<h3>Links:</h3><ul><xsl:apply-templates select="atom:link" /></ul>
						<h3>URI Templates:</h3><ul><xsl:apply-templates select="cmisra:uritemplate" /></ul>
						<table style="border-spacing:5px">
							<tr>
								<td style="vertical-align:top;"><xsl:apply-templates select="cmisra:repositoryInfo" /></td>
								<td style="vertical-align:top;"><xsl:apply-templates select="cmisra:repositoryInfo/cmis:capabilities" /></td>
								<td style="vertical-align:top;"><xsl:apply-templates select="cmisra:repositoryInfo/cmis:aclCapability" /></td>
							</tr>
						</table>
					</div>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="cmisra:repositoryInfo">
		<div>
		<h3>Info</h3>
		<table class="servicetable">
			<xsl:for-each select="*[not(*)]">
				<tr>
					<td><xsl:value-of select="local-name()" /></td>
					<td><xsl:value-of select="current()" /></td>
				</tr>
			</xsl:for-each>
		</table>
		</div>
	</xsl:template>

	<xsl:template match="cmis:capabilities">
		<div>
		<h3>Capabilities</h3>
		<table class="servicetable">
			<xsl:for-each select="*[not(*)]">
				<tr>
					<td><xsl:value-of select="substring-after(local-name(), 'capability')" /></td>
					<td><xsl:value-of select="current()" /></td>
				</tr>
			</xsl:for-each>
		</table>
		</div>
	</xsl:template>

	<xsl:template match="cmis:aclCapability">
		<div>
		<h3>ACL Capabilities</h3>
		<table class="servicetable">
			<tr>
				<td>Supported Permissions</td><td><xsl:value-of select="cmis:supportedPermissions" /></td>
			</tr>
			<tr>
				<td>Propagation</td><td><xsl:value-of select="cmis:propagation" /></td>
			</tr>
		</table>
		<h4>Permissions</h4>
		<table class="servicetable">
			<tr><th>Permission</th><th>Description</th></tr>
			<xsl:for-each select="cmis:permissions">
				<tr>
					<td><xsl:value-of select="cmis:permission" /></td>
					<td><xsl:value-of select="cmis:description" /></td>
				</tr>
			</xsl:for-each>
		</table>
		<h4>Mappings</h4>
		<table class="servicetable">
			<tr><th>Key</th><th>Permission</th></tr>
			<xsl:for-each select="cmis:mapping">
				<tr>
					<td><xsl:value-of select="cmis:key" /></td>
					<td><xsl:value-of select="cmis:permission" /></td>
				</tr>
			</xsl:for-each>
		</table>
		</div>
	</xsl:template>

	<xsl:template match="app:collection">
		<li><a href="{$browseUrl}{@href}"><xsl:value-of select="atom:title" /> (<xsl:value-of select="cmisra:collectionType" />)</a></li>
	</xsl:template>

	<xsl:template match="atom:link">
		<li><a href="{$browseUrl}{@href}"><xsl:value-of select="@rel" /></a> (<xsl:value-of select="@type" />)</li>
	</xsl:template>
	
	<xsl:template match="cmisra:uritemplate">
		<li><xsl:value-of select="cmisra:type" /> (<xsl:value-of select="cmisra:mediatype" />): 
		<a href="{$browseUrl}{cmisra:template}"><xsl:value-of select="cmisra:template" /></a></li>
	</xsl:template>

</xsl:stylesheet>