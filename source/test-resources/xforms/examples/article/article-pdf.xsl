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
	xmlns:article="http://www.alfresco.org/alfresco/article"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:html="http://www.w3.org/1999/xhtml"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions" exclude-result-prefixes="xhtml">

	<xsl:output method="xml"/>

	<xsl:template match="/">
	    <fo:root>
			<fo:layout-master-set>
				<fo:simple-page-master master-name="only">
        			<fo:region-body 
        			   region-name="xsl-region-body" 
        			   margin="0.7in"/>
        			<fo:region-before 
        			   region-name="xsl-region-before" 
        			   extent="0.7in" 
        			    display-align="before" />

        		        <fo:region-after 
        			   region-name="xsl-region-after" 
           			   display-align="after"
           			   extent="0.7in" />
      			       </fo:simple-page-master>
				</fo:layout-master-set>

      	        <fo:page-sequence master-reference="only">
      				<fo:flow flow-name="xsl-region-body">
      			  	 	<fo:block  
				  		    font-family="Times"
				  		    font-size="18pt"
				  		    font-weight="bold"
				  		    space-before="18pt"
				  		    space-after="12pt"
     							 text-align="left">
     						<xsl:value-of select="/article:article/article:title"  disable-output-escaping="yes"/>
     					</fo:block>	 

						<fo:table text-align="justify">
							<fo:table-column column-width="1.3in"/>
							<fo:table-column column-width="1.3in" number-columns-repeated="4"/>
							<fo:table-body>
    								<fo:table-row>
      									<xsl:element name="fo:table-cell">
											<xsl:attribute name="padding">
												<xsl:value-of select="'6pt'"/>
											</xsl:attribute>
											<xsl:attribute name="number-columns-spanned">											
													<xsl:value-of select="'5'"/>
											</xsl:attribute>
      										<fo:block  
									  		    font-family="Times"
									  		    font-size="12pt"
									  		    space-before="18pt"
									  		    space-after="12pt">
									  		    	<xsl:value-of select="normalize-space(/article:article/article:teaser)"  disable-output-escaping="yes"/>
									  		</fo:block>
      									</xsl:element>
    								</fo:table-row>
								<xsl:for-each select="/article:article/article:page">
									<xsl:variable name="page_number"  select="1+count(preceding-sibling::*[name()=name(current())])"/>
    								<fo:table-row>
      									<xsl:element name="fo:table-cell">
											<xsl:attribute name="padding">
												<xsl:value-of select="'6pt'"/>
											</xsl:attribute>
											<xsl:attribute name="number-columns-spanned">											
													<xsl:value-of select="'5'"/>
											</xsl:attribute>
      										<fo:block  
									  		    font-family="Times"
									  		    font-size="12pt"
									  		    space-before="18pt"
									  		    space-after="12pt">
												<xsl:if test="position()=1">
													<fo:inline  font-weight="bold">      												
														<xsl:value-of select="normalize-space(/article:article/article:location)"  disable-output-escaping="yes"/>&#8212;
													</fo:inline>
												</xsl:if>											
									  		    <xsl:value-of select="normalize-space(.)" disable-output-escaping="yes"/>
									  		</fo:block>
      									</xsl:element>
    								</fo:table-row>
    							</xsl:for-each>
  							</fo:table-body>
						</fo:table>
	      			</fo:flow>
      		 	</fo:page-sequence>
    		</fo:root>
    	</xsl:template>	

</xsl:stylesheet>
