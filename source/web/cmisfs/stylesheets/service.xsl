<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:app="http://www.w3.org/2007/app"
	xmlns:atom="http://www.w3.org/2005/Atom" xmlns:cmis="http://docs.oasis-open.org/ns/cmis/core/200901"
	xmlns:cmisra="http://docs.oasis-open.org/ns/cmis/restatom/200901">

	<xsl:output method="html" />

	<xsl:param name="browseUrl"/>
    <xsl:param name="webContentRoot"/>

	<xsl:template match="/">
		<html>
			<head>
				<title>Service</title>
				<link rel="stylesheet" type="text/css" href="{$webContentRoot}browser/browser.css" />
			</head>
			<body>
				<xsl:for-each select="app:service/app:workspace">
					<div class="servicebox">
						<h2>Repository <xsl:value-of select="@cmis:id" /></h2>
						<h3>Collections:</h3><ul><xsl:apply-templates select="app:collection" /></ul>
						<h3>Links:</h3><ul><xsl:apply-templates select="atom:link" /></ul>
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
		Type: <xsl:value-of select="cmis:setType" />
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
		<li><a href="{$browseUrl}{@href}"><xsl:value-of select="atom:title" /> (<xsl:value-of select="@cmisra:collectionType" />)</a></li>
	</xsl:template>

	<xsl:template match="atom:link">
		<li><a href="{$browseUrl}{@href}"><xsl:value-of select="@rel" /></a> (<xsl:value-of select="@type" />)</li>
	</xsl:template>

</xsl:stylesheet>