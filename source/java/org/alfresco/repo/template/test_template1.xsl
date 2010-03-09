<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/02/xpath-functions">
	<xsl:output method="text" />
	<xsl:preserve-space elements="*" />
	<xsl:template match="/">
		<xsl:for-each select="/nutrition/food">
			<xsl:value-of select="name" />
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
