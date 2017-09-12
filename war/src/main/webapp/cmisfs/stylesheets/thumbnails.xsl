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

	<xsl:variable name="filter">
		<xsl:text>cmis:baseTypeId,cmis:name,cmis:objectTypeId,cmis:contentStreamMimeType,cmis:contentStreamLength,cmis:createdBy,cmis:creationDate,cmis:versionLabel,cmis:isMajorVersion,cmis:lastModifiedBy,cmis:lastModificationDate,cmis:isLatestVersion</xsl:text>
	</xsl:variable>

	<xsl:template match="*">
		<xsl:variable name="width">
			<xsl:value-of select="cmisra:object/cmis:rendition/cmis:kind[text() = 'cmis:thumbnail']/../cmis:width"></xsl:value-of>
		</xsl:variable>
		<xsl:variable name="height">
			<xsl:value-of select="cmisra:object/cmis:rendition/cmis:kind[text() = 'cmis:thumbnail']/../cmis:height"></xsl:value-of>
		</xsl:variable>

		<table class="feedtable">
			<tr>
				<td class="tdthumb">
				    <xsl:choose>
					    <xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:document')">
                            <a href="{atom:content/@src}">
                                <xsl:choose>
                                    <xsl:when test="atom:link[@rel='alternate' and @cmisra:renditionKind='cmis:thumbnail']/@href">
                                        <img src="{atom:link[@rel='alternate' and @cmisra:renditionKind='cmis:thumbnail']/@href}" width="{$width}px" height="{$height}px"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <img src="{$auxRoot}unknown.png"/>
                                    </xsl:otherwise>
                              	</xsl:choose>
						    </a>
					    </xsl:when>
                        <xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:folder')">
						    <a href="{$browseUrl}{atom:link[@rel='down']/@href}&amp;filter={$filter}">
							    <img src="{$auxRoot}folder.png"/>
						    </a>
						</xsl:when>    
					    <xsl:when test="contains(cmisra:object/cmis:properties/cmis:propertyId[@propertyDefinitionId='cmis:baseTypeId'],'cmis:policy')">
					        <a href="{$browseUrl}{atom:link[@rel='self']/@href}">
						        <img src="{$auxRoot}policy.png"/>
						    </a>    
					    </xsl:when>
					    <xsl:otherwise>
                            <a href="{$browseUrl}{atom:link[@rel='self']/@href}">
						        <img src="{$auxRoot}unknown.png"/>
                            </a>
                        </xsl:otherwise>
                    </xsl:choose>
				</td>
			</tr>
			<tr>
				<td class="tdthumbId">
					<a href="{$browseUrl}{atom:link[@rel='self']/@href}">
						<xsl:value-of select="cmisra:object/cmis:properties/cmis:propertyString[@propertyDefinitionId='cmis:name']" />
					</a>
				</td>
			</tr>
		</table>
	</xsl:template>

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
					<form name="urlform">
						<input type="text" name="url" value="" size="100"/>
						<input type="hidden" name="browseUrl" value="{$browseUrl}" />
						<input type="button" value="Go" onClick="load()" />
						<input type="button" value="Atom" onClick="loadAtom()" />
					</form>
					<script type="text/javascript">
						var x = window.location.search.indexOf("=");
						if(x > -1) {
							document.forms.urlform.url.value = decodeURI(window.location.search.substring(x+1));
						}
						function load() { window.location.href = document.forms.urlform.browseUrl.value + document.forms.urlform.url.value; }
						function loadAtom() { window.location.href = document.forms.urlform.url.value; }
					</script>
					<xsl:if test="atom:feed/atom:link[@rel='service']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='service']/@href}">Service</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='self']">
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
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='first']/@href}{$browseOverrideStylesheet}thumbnails">First</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='previous']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='previous']/@href}{$browseOverrideStylesheet}thumbnails">Previous</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='next']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='next']/@href}{$browseOverrideStylesheet}thumbnails">Next</a> -
					</xsl:if>
					<xsl:if test="atom:feed/atom:link[@rel='last']">
						<a href="{$browseUrl}{atom:feed/atom:link[@rel='last']/@href}{$browseOverrideStylesheet}thumbnails">Last</a> -
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
					<table>
						<tr>
							<td valign="top">
								<xsl:apply-templates select="atom:feed/atom:entry[position() mod 3 = 1]"/>
							</td>
							<td valign="top">
								<xsl:apply-templates select="atom:feed/atom:entry[position() mod 3 = 2]"/>
							</td>
							<td valign="top">
								<xsl:apply-templates select="atom:feed/atom:entry[position() mod 3 = 0]"/>
							</td>
						</tr>
					</table>
				</xsl:if>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>
