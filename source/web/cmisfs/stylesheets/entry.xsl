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
				<title><xsl:value-of select="atom:entry/atom:title" /></title>
				<link rel="stylesheet" type="text/css" href="{$webContentRoot}browser/browser.css" />
			</head>
			<body>
				<h1>
				<xsl:choose>
					<xsl:when test="contains(atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:document')">
						<img src="{$webContentRoot}browser/document.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:when test="contains(atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:folder')">
						<img src="{$webContentRoot}browser/folder.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:when test="contains(atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:relationship')">
						<img src="{$webContentRoot}browser/relationship.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:when test="contains(atom:entry/cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:policy')">
						<img src="{$webContentRoot}browser/policy.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:when test="atom:entry/cmisra:type">
						<img src="{$webContentRoot}browser/type.png" style="vertical-align:middle; padding-right: 10px;" />
					</xsl:when>
					<xsl:otherwise>
						<img src="{$webContentRoot}browser/unknown.png" style="vertical-align:middle; padding-right: 10px;" />
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
							<a href="{@href}">Down (<xsl:value-of select="@type"></xsl:value-of>)</a> -
						</xsl:for-each>
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/foldertree']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/foldertree']/@href}">Folder Tree</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:content">
						<a href="{atom:entry/atom:content/@src}">Download</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='version-history']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='version-history']/@href}">All Versions</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='alternate']">
						<xsl:for-each select="atom:entry/atom:link[@rel='alternate']">
							<a href="{@href}">Rendition (<xsl:value-of select="@cmisra:renditionType"></xsl:value-of>)</a> -
						</xsl:for-each>
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/relationships']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/relationships']/@href}">Relationships</a> -
					</xsl:if>					
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/allowableactions']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/allowableactions']/@href}">Allowable Actions</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/acl']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/acl']/@href}">ACL</a> -
					</xsl:if>
					<xsl:if test="atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/policies']">
						<a href="{$browseUrl}{atom:entry/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/policies']/@href}">Policies</a> -
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
				
				<h2>Properties</h2>
				<table class="entrytable">
				<xsl:for-each select="atom:entry/cmisra:object/cmis:properties/*">
					<tr>
						<td style="font-weight: bold;"><xsl:value-of select="@pdid" /></td>
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