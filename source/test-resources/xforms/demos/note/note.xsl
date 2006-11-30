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
	  <title><xsl:value-of select="/note/title"/></title>
	</head>
	<body>
        <xsl:apply-templates/>
        </body>
      </html>
    </xsl:template>
    <xsl:template match="/note/to">
      <div style="line-height: 25px;"><span class="name">To:</span> <xsl:value-of select="/note/to"/></div>
    </xsl:template>
    <xsl:template match="/note/from">
      <div><span class="name">From:</span> <xsl:value-of select="/note/from"/></div>
    </xsl:template>
    <xsl:template match="/note/subject">
      <div style="position:relative;">
	<span class="name">Subject:</span>
	<xsl:value-of select="/note/subject"/>
	<xsl:choose>
        <xsl:when test="/note/@important='true'">
	  <span style="margin-left:5px;font-size:14px;font-weigh:bold;color:red;">!!!!!</span>
        </xsl:when>
	</xsl:choose>
      </div>
    </xsl:template>
    <xsl:template match="/note/body">
      <div style="width: 50%; height: 250px; border: solid 1px black; margin-top: 15px;">
        <tt><xsl:value-of select="/note/body"/></tt>
      </div>
    </xsl:template>
    <xsl:template match="/note/important">
    </xsl:template>
</xsl:stylesheet>
