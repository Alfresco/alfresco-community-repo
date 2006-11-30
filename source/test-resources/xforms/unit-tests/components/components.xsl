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
	  <title>Component Test</title>
	</head>
	<body>
        <xsl:apply-templates/>
        </body>
      </html>
    </xsl:template>
    <xsl:template match="/components/required_textfield">
      <div style="line-height: 25px;"><span class="name">Required Textfield:</span> <xsl:value-of select="/components/required_textfield"/></div>
    </xsl:template>
    <xsl:template match="/components/optional_textfield">
      <div style="line-height: 25px;"><span class="name">Optional Textfield:</span> <xsl:value-of select="/components/optional_textfield"/></div>
    </xsl:template>
    <xsl:template match="/components/integer">
      <div style="line-height: 25px;"><span class="name">Integer:</span> <xsl:value-of select="/components/integer"/></div>
    </xsl:template>
    <xsl:template match="/components/positive_integer">
      <div style="line-height: 25px;"><span class="name">Positive Integer:</span> <xsl:value-of select="/components/positiveInteger"/></div>
    </xsl:template>
    <xsl:template match="/components/double">
      <div style="line-height: 25px;"><span class="name">Double:</span> <xsl:value-of select="/components/double"/></div>
    </xsl:template>
    <xsl:template match="/components/date">
      <div style="line-height: 25px;"><span class="name">Date:</span> <xsl:value-of select="/components/date"/></div>
    </xsl:template>
    <xsl:template match="/components/radio">
      <div style="line-height: 25px;"><span class="name">Radio:</span> <xsl:value-of select="/components/radio"/></div>
    </xsl:template>
    <xsl:template match="/components/combobox">
      <div style="line-height: 25px;"><span class="name">ComboBox:</span> <xsl:value-of select="/components/combobox"/></div>
    </xsl:template>
    <xsl:template match="/components/textarea">
      <div style="line-height: 25px;"><span class="name">TextArea:</span> <xsl:value-of select="/components/textarea"/></div>
    </xsl:template>
    <xsl:template match="/components/checkbox">
      <div style="line-height: 25px;"><span class="name">CheckBox:</span> <xsl:value-of select="/components/checkbox"/></div>
    </xsl:template>
</xsl:stylesheet>
