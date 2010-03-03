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
	xmlns:mm="http://www.alfresco.org/alfresco/mm"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions" exclude-result-prefixes="xhtml mm fn">
	<xsl:output method="html"/>

	<xsl:preserve-space elements="*"/>

	<xsl:template match="/">
	<xsl:variable name="urlPrefix" select="string($alf:avm_sandbox_url)"/>
		<track>
			<title><xsl:value-of select="/mm:multi-media/mm:title"/></title>
			<creator><xsl:value-of select="/mm:multi-media/mm:author"/></creator>
			<location><xsl:value-of select="$urlPrefix"/><xsl:value-of select="/mm:multi-media/mm:file"/></location>
		</track>
	</xsl:template>
</xsl:stylesheet>
