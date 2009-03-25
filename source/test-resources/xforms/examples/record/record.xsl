<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="xhtml">

    <xsl:output method="html" version="4.01" encoding="UTF-8" indent="yes"
                doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
                doctype-system="http://www.w3.org/TR/html4/loose.dtd"/>
    <xsl:preserve-space elements="*"/>

    <xsl:template match="/">
      <html>
	<head>
	  <style type="text/css">
body
{
  font-family: Tahoma, Arial, Helvetica, sans-serif;
  background-color: white;
  font-size: 11px;
}

.name {
  color: #003366;
  font-weight: bold;
  margin-right: 10px;
}

	  </style>
	  <title><xsl:value-of select="/record/@title"/></title>
	</head>
	<body>
          <div><span class="name">Name:</span> <xsl:value-of select="/record/@name"/></div>
          <div><span class="name">Artist:</span> <xsl:value-of select="/record/@artist"/></div>
          <div><span class="name">Genre:</span> <xsl:value-of select="/record/@genre"/></div>
          <div><span class="name">Year:</span> <xsl:value-of select="/record/@year"/></div>
        </body>
      </html>
    </xsl:template>
</xsl:stylesheet>
