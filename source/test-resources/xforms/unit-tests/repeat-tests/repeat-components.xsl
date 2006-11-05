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
	<title>repeat-components</title>
      </head>
      <body>
	<table border="1">
          <tr><th>zero-to-one</th><td colspan="2"><xsl:value-of select="/repeat-components/zero-to-one"/></td></tr>
	  <tr><th>one-to-one</th><td colspan="2"><xsl:value-of select="/repeat-components/one-to-one"/></td></tr>
	  <tr>
	    <th>
	      <!--
		  <xsl:attribute name="rowspan">
                    <xsl:value-of select="count(/repeat-components/one-to-inf)"/>
		  </xsl:attribute>
		  -->
	      one-to-inf
            </th>
	    <td>
	      <ul>
		<xsl:for-each select="/repeat-components/one-to-inf">
		  <li><xsl:value-of select="."/></li>
		</xsl:for-each>
	      </ul>
	    </td>
	  </tr>
	  <tr>
	    <th>
	      <!--	      
			      <xsl:attribute name="rowspan">
				<xsl:value-of select="count(/repeat-components/zero-to-inf)"/>
			      </xsl:attribute>
			      -->
	      zero-to-inf
            </th>
	    <td>
	      <ul>
		<xsl:for-each select="/repeat-components/zero-to-inf">
		  <li><xsl:value-of select="."/></li>
		</xsl:for-each>
	      </ul>
	    </td>
	  </tr>
	  <tr>
	    <th>
	      <!--
		  <xsl:attribute name="rowspan">
                    <xsl:value-of select="count(/repeat-components/one-to-five)"/>
		  </xsl:attribute>
		  -->
	      one-to-five
	    </th>
	    <td>
	      <ul>
		<xsl:for-each select="/repeat-components/one-to-five">
		  <li colspan="2"><xsl:value-of select="."/></li>
		</xsl:for-each>
	      </ul>
	    </td>
	  </tr>
	  <tr>
	    <th>
	      <!--
	      <xsl:attribute name="rowspan">
                <xsl:value-of select="count(/repeat-components/zero-to-five)"/>
	      </xsl:attribute>
-->
	      zero-to-five
            </th>
	    <td>
	      <ul>
		<xsl:for-each select="/repeat-components/zero-to-five">
		  <li colspan="2"><xsl:value-of select="."/></li>
		</xsl:for-each>
	      </ul>
	    </td>
	  </tr>
	  <tr>
	    <th>
	      <!--
		  <xsl:attribute name="rowspan">
                    <xsl:value-of select="count(/repeat-components/one-to-five-multi)"/>
		  </xsl:attribute>
		  -->
	      one-to-five-multi
            </th>
	    <td>
	      <ul>
		<xsl:for-each select="/repeat-components/one-to-five-multi">
		  <ul>
		    <li><xsl:value-of select="string"/></li>
		    <li><xsl:value-of select="int"/></li>
		  </ul>
		</xsl:for-each>
	      </ul>
	    </td>
	  </tr>
	  <tr>
	    <th>
	      <!--
		  <xsl:attribute name="rowspan">
		    <xsl:value-of select="count(/repeat-components/zero-to-five-multi)"/>
		  </xsl:attribute>
		  -->
	      zero-to-five-multi
	    </th>
	    <td>
	      <ul>
		<xsl:for-each select="/repeat-components/zero-to-five-multi">
		  <li>
		    <ul>
		      <li><xsl:value-of select="string"/></li>
		      <li><xsl:value-of select="int"/></li>
		    </ul>
		  </li>
		</xsl:for-each>
	      </ul>
	    </td>
	  </tr>
	</table>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
