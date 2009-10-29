<?xml version="1.0" encoding="UTF-8"?>
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
				<title><xsl:value-of select="atom:feed/atom:title" /></title>
				<link rel="stylesheet" type="text/css" href="{$auxRoot}browser.css" />
			</head>
			<body>
				<img src="{$auxRoot}cmis.png" style="float: right;" />
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
							<a href="{$browseUrl}{@href}">Down (<xsl:value-of select="@type" />)</a> -
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
					<xsl:if test="atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/foldertree']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/foldertree']/@href}">Folder Tree</a> -
					</xsl:if>				
					<xsl:if test="atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/allowableactions']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/allowableactions']/@href}">Allowable Actions</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/acl']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/acl']/@href}">ACL</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/policies']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/policies']/@href}">Policies</a> -
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
						<xsl:sort select="cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId']" />
						<xsl:sort select="cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:name']" />
						
						<xsl:variable name="odd">
							<xsl:if test="(position() mod 2) != 1"></xsl:if>
							<xsl:if test="(position() mod 2) = 1">-odd</xsl:if>
						</xsl:variable>					
						
						<tr>
							<td class="tdlinks{$odd}" rowspan="2">
								<xsl:choose>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:document')">
										<img src="{$auxRoot}document.png" />
									</xsl:when>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:folder')">
										<img src="{$auxRoot}folder.png" />
									</xsl:when>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:relationship')">
										<img src="{$auxRoot}relationship.png" />
									</xsl:when>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:policy')">
										<img src="{$auxRoot}policy.png" />
									</xsl:when>
									<xsl:otherwise>
										<img src="{$auxRoot}unknown.png" />
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="tdinfo{$odd}" style="font-weight: bold;">
								<xsl:choose>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:document')">
										<a href="{atom:content/@src}">
										<xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@propertyDefinitionId='cmis:name']" />
										</a>
									</xsl:when>
									<xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:folder')">
										<a href="{$browseUrl}{atom:link[@rel='down']/@href}">
										<xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@propertyDefinitionId='cmis:name']" />
										</a>
									</xsl:when>
									<xsl:otherwise>
										<a href="{$browseUrl}{atom:link[@rel='self']/@href}">
										<xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@propertyDefinitionId='cmis:name']" />
										</a>
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="tdinfo{$odd}">
								<a href="{$browseUrl}{atom:link[@rel='describedby']/@href}">
								<xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:objectTypeId']" />
								</a>
							</td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@propertyDefinitionId='cmis:contentStreamMimeType']" /></td>							
							<td class="tdinfo{$odd}" align="right"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyInteger[@propertyDefinitionId='cmis:contentStreamLength']" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@propertyDefinitionId='cmis:createdBy']" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyDateTime[@propertyDefinitionId='cmis:creationDate']" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@propertyDefinitionId='cmis:versionLabel']" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyBoolean[@propertyDefinitionId='cmis:isMajorVersion']" /></td>
						</tr>
						<tr>
							<td class="tdlinks{$odd}" colspan="4">
								<a href="{$browseUrl}{atom:link[@rel='self']/@href}" class="actionlink">Entry</a> - 
								<a href="{$browseUrl}{atom:link[@rel='describedby']/@href}" class="actionlink">Type Info</a> -
								<xsl:if test="atom:link[@rel='down']">							
									<xsl:for-each select="atom:link[@rel='down']">
										<a href="{$browseUrl}{@href}" class="actionlink">Down (<xsl:value-of select="@type" />)</a> -
									</xsl:for-each>
								</xsl:if>
								<xsl:if test="atom:content">
									<a href="{atom:content/@src}" class="actionlink">Download</a> - 
								</xsl:if>
								<xsl:if test="atom:link[@rel='version-history']">
									<a href="{$browseUrl}{atom:link[@rel='version-history']/@href}" class="actionlink">All Versions</a> -
								</xsl:if>
								<xsl:if test="atom:link[@rel='alternate']">
									<xsl:for-each select="atom:link[@rel='alternate']">
										<a href="{@href}" class="actionlink">Rendition (<xsl:value-of select="@cmisra:renditionType"></xsl:value-of>)</a> -
									</xsl:for-each>
								</xsl:if>
								<xsl:if test="atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/relationships']">
									<a href="{$browseUrl}{atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/relationships']/@href}">Relationships</a> -
								</xsl:if>	
								<xsl:if test="atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/allowableactions']">
									<a href="{$browseUrl}{atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/allowableactions']/@href}" class="actionlink">Allowable Actions</a> -
								</xsl:if>
								<xsl:if test="atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/acl']">
									<a href="{$browseUrl}{atom:link[@rel='http://docs.oasis-open.org/ns/cmis/link/200908/acl']/@href}" class="actionlink">ACL</a>
								</xsl:if>
							</td>
							<td class="tdinfo2{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@propertyDefinitionId='cmis:lastModifiedBy']" /></td>
							<td class="tdinfo2{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyDateTime[@propertyDefinitionId='cmis:lastModificationDate']" /></td>
							<td class="tdinfo2{$odd}"> </td>
							<td class="tdinfo2{$odd}"><xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyBoolean[@propertyDefinitionId='cmis:isLatestVersion']" /></td>
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
						<th>Base Id</th>
						<th>Description</th>
					</tr>
					<xsl:for-each select="atom:feed/atom:entry">
					<xsl:variable name="odd">
							<xsl:if test="(position() mod 2) != 1"></xsl:if>
							<xsl:if test="(position() mod 2) = 1">-odd</xsl:if>
						</xsl:variable>	
						
						<tr>
							<td class="tdlinks{$odd}" rowspan="2">
								<img src="{$auxRoot}type.png" />
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
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:type/cmis:baseId" /></td>
							<td class="tdinfo{$odd}"><xsl:value-of select="cmisra:type/cmis:description" /></td>
						</tr>
						<tr>
							<td class="tdlinks{$odd}" colspan="7">
								<a href="{$browseUrl}{atom:link[@rel='self']/@href}" class="actionlink">Entry</a> - 
								<a href="{$browseUrl}{atom:link[@rel='describedby']/@href}" class="actionlink">Type Info</a> -
								<xsl:if test="atom:link[@rel='down']">
									<a href="{$browseUrl}{atom:link[@rel='down']/@href}" class="actionlink">Down</a>
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