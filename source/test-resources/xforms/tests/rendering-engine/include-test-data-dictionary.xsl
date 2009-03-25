<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xhtml">
  
    <xsl:template match="in-data-dictionary">
      <div>Value from data dictonary is <b><xsl:value-of select="."/></b></div>
    </xsl:template>
</xsl:stylesheet>
