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
				<title><xsl:value-of select="atom:feed/atom:title" /></title>
				<link rel="stylesheet" type="text/css" href="{$webContentRoot}browser/browser.css" />
			</head>
			<body>
				<h1><xsl:value-of select="atom:feed/atom:title" /></h1>
				<div class="navigationbox">
					<xsl:if test="atom:feed/atom:link[@rel='service']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='service']/@href}">Service</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='seld']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='self']/@href}">Entry</a> -
					</xsl:if>					
					<xsl:if test="atom:feed/atom:link[@rel='up']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='up']/@href}">Up</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='down']">
						<xsl:for-each select="atom:feed/atom:link[@rel='down']">
							<a href="{@href}">Down (<xsl:value-of select="@type"></xsl:value-of>)</a> -
						</xsl:for-each>
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='first']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='first']/@href}">First</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='previous']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='previous']/@href}">Previous</a> -
					</xsl:if>					
					<xsl:if test="atom:feed/atom:link[@rel='next']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='next']/@href}">Next</a> -
					</xsl:if>		
					<xsl:if test="atom:feed/atom:link[@rel='last']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='last']/@href}">Last</a> -
					</xsl:if>						
					<xsl:if test="atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/foldertree']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/foldertree']/@href}">Folder Tree</a> -
					</xsl:if>				
					<xsl:if test="atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/allowableactions']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/allowableactions']/@href}">Allowable Actions</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/acl']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/acl']/@href}">ACL</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/policies']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/policies']/@href}">Policies</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='describedby']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='describedby']/@href}">Type</a>
					</xsl:if>
				</div>
				<xsl:if test="atom:feed/atom:entry/cmisra:object">
					<table class="feedtable">
					<tr>
						<th></th>
						<th>Name</th>
						<th>Type</th>
						<th>MIME Type</th>
						<th>Size</th>
						<th>Created By /<br/> Last Modified By</th>
						<th>Creation Date /<br/> Last Modification Date</th>
						<th>Version Label</th>
						<th>Major /<br/> Latest</th>
					</tr>
					<xsl:for-each select="atom:feed/atom:entry">
						<xsl:sort select="cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId']" />
						<xsl:sort select="cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:Name']" />
						
						<xsl:variable name="odd">
							<xsl:if test="(position() mod 2) != 1"></xsl:if>
							<xsl:if test="(position() mod 2) = 1">-odd</xsl:if>
						</xsl:variable>					
						
						<tr>
							<td class="tdlinks{$odd}" rowspan="2">
								<xsl:choose>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:document')">
										<img src="{$webContentRoot}browser/document.png" />
									</xsl:when>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:folder')">
										<img src="{$webContentRoot}browser/folder.png" />
									</xsl:when>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:relationship')">
										<img src="{$webContentRoot}browser/relationship.png" />
									</xsl:when>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:policy')">
										<img src="{$webContentRoot}browser/policy.png" />
									</xsl:when>
									<xsl:otherwise>
										<img src="{$webContentRoot}browser/unknown.png" />
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="tdinfo{$odd}" style="font-weight: bold;">
								<xsl:choose>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:document')">
										<a href="{atom:content/@src}">
										<xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@pdid='cmis:Name']" />
										</a>
									</xsl:when>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:BaseTypeId'],'cmis:folder')">
										<a href="{$browseUrl}{atom:link[@rel='down']/@href}">
										<xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@pdid='cmis:Name']" />
										</a>
									</xsl:when>
									<xsl:otherwise>
										<a href="{$browseUrl}{atom:link[@rel='self']/@href}">
										<xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@pdid='cmis:Name']" />
										</a>
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="tdinfo{$odd}">
								<a href="{$browseUrl}{atom:link[@rel='describedby']/@href}">
								<xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyId[@pdid='cmis:ObjectTypeId']" />
								</a>
							</td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@pdid='cmis:ContentStreamMimeType']" /></td>							
							<td class="tdinfo{$odd}" align="right"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyInteger[@pdid='cmis:ContentStreamLength']" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@pdid='cmis:CreatedBy']" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyDateTime[@pdid='cmis:CreationDate']" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@pdid='cmis:VersionLabel']" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyBoolean[@pdid='cmis:IsMajorVersion']" /></td>
						</tr>
						<tr>
							<td class="tdlinks{$odd}" colspan="4">
								<a href="{$browseUrl}{atom:link[@rel='self']/@href}" class="actionink">Entry</a> - 
								<a href="{$browseUrl}{atom:link[@rel='describedby']/@href}" class="actionink">Type Info</a> -
								<xsl:if test="atom:link[@rel='down']">
									<a href="{$browseUrl}{atom:link[@rel='down']/@href}" class="actionink">Down</a> -
								</xsl:if>
								<xsl:if test="atom:content">
									<a href="{atom:content/@src}" class="actionink">Download</a> - 
								</xsl:if>
								<xsl:if test="atom:link[@rel='version-history']">
									<a href="{$browseUrl}{atom:link[@rel='version-history']/@href}" class="actionink">All Versions</a> -
								</xsl:if>
								<xsl:if test="atom:link[@rel='alternate']">
									<xsl:for-each select="atom:link[@rel='alternate']">
										<a href="{@href}" class="actionink">Rendition (<xsl:value-of select="@cmisra:renditionType"></xsl:value-of>)</a> -
									</xsl:for-each>
								</xsl:if>
								<xsl:if test="atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/allowableactions']">
									<a href="{$browseUrl}{atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/allowableactions']/@href}" class="actionink">Allowable Actions</a> -
								</xsl:if>
								<xsl:if test="atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/acl']">
									<a href="{$browseUrl}{atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200901/acl']/@href}" class="actionink">ACL</a>
								</xsl:if>
							</td>
							<td class="tdinfo2{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@pdid='cmis:LastModifiedBy']" /></td>
							<td class="tdinfo2{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyDateTime[@pdid='cmis:LastModificationDate']" /></td>
							<td class="tdinfo2{$odd}"> </td>
							<td class="tdinfo2{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyBoolean[@pdid='cmis:IsLatestVersion']" /></td>
					</tr>
					</xsl:for-each>
					</table>
				</xsl:if>
				<xsl:if test="atom:feed/atom:entry/cmisra:type">
				
					<table class="feedtable">
					<tr>
						<th></th>
						<th>Id</th>
						<th>Local Name</th>
						<th>Local Namespace</th>
						<th>Display Name</th>
						<th>Query Name</th>
						<th>Base Type</th>
						<th>Description</th>
					</tr>
					<xsl:for-each select="atom:feed/atom:entry">
					<xsl:variable name="odd">
							<xsl:if test="(position() mod 2) != 1"></xsl:if>
							<xsl:if test="(position() mod 2) = 1">-odd</xsl:if>
						</xsl:variable>	
						
						<tr>
							<td class="tdlinks{$odd}" rowspan="2">
								<img src="{$webContentRoot}browser/type.png" />
							</td>
							<td class="tdinfo{$odd}">
								<a href="{$browseUrl}{atom:link[@rel='self']/@href}" style="font-weight: bold;">
								<xsl:value-of select="cmisra:type/cmis:id" />
								</a>
							</td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:type/cmis:localName" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:type/cmis:localNamespace" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:type/cmis:displayName" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:type/cmis:queryName" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:type/cmis:baseTypeId" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:type/cmis:description" /></td>
						</tr>
						<tr>
							<td class="tdlinks{$odd}" colspan="7">
								<a href="{$browseUrl}{atom:link[@rel='self']/@href}" class="actionink">Entry</a> - 
								<a href="{$browseUrl}{atom:link[@rel='describedby']/@href}" class="actionink">Type Info</a> -
								<xsl:if test="atom:link[@rel='down']">
									<a href="{$browseUrl}{atom:link[@rel='down']/@href}" class="actionink">Down</a>
								</xsl:if>
							</td>
						</tr>
					</xsl:for-each>
					</table>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
	
</xsl:stylesheet>