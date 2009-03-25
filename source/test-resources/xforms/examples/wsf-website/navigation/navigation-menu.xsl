<?xml version="1.0" encoding="UTF-8"?>
<!--
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" 
    xmlns:nav="http://www.alfresco.org/alfresco/navigation" exclude-result-prefixes="nav fn" version="1.0">
    <xsl:output method="html" />
    <xsl:template match="/nav:navigation">
        <div id="nav-menu" class="yuimenubar">
            <div class="bd">
                 <ul class="first-of-type">
            		<xsl:for-each select="nav:main_channel">
            		    <xsl:variable name="main_channel" select="nav:name"/>
              			<xsl:element name="li">
              			<xsl:if test="position()=1">
					<xsl:attribute name="class">yuimenubaritem first-of-type</xsl:attribute>      					
                        	</xsl:if>
              			<xsl:if test="position()!=1">
					<xsl:attribute name="class">yuimenubaritem</xsl:attribute>      					
                        	</xsl:if>
                        	<xsl:element name="a">
      						<xsl:attribute name="href">/views/pages/main-channel.jsp?channel=/<xsl:value-of select="$main_channel"/></xsl:attribute>
      						<xsl:value-of select="$main_channel"/>
    					</xsl:element>  
                 			<xsl:element name="div">
                 				<xsl:attribute name="id"><xsl:value-of select="$main_channel"/></xsl:attribute>
                 				<xsl:attribute name="class">yuimenu</xsl:attribute>
                     				<div class="bd">
                     				    <ul>
		     				           <xsl:for-each select="nav:sub_channel">
		     				           <li class="yuimenuitem">
		     				               <xsl:variable name="sub_channel" select="nav:name"/>
		     				               <xsl:variable name="sub_channel_path" select="concat('/',$main_channel,'/',$sub_channel)"/>
		     			                       <xsl:element name="a">
		      							<xsl:attribute name="href">/views/pages/sub-channel.jsp?channel=<xsl:value-of select="$sub_channel_path"/></xsl:attribute>
		      							<xsl:value-of select="$sub_channel"/>
		    						</xsl:element>
		    						<xsl:element name="div">
								        <xsl:attribute name="id"><xsl:value-of select="$sub_channel"/></xsl:attribute>
								        <xsl:attribute name="class">yuimenu</xsl:attribute>
								        <div class="bd">
                     				    				<ul class="first-of-type">
                     				    				<xsl:for-each select="nav:sub_sub_channel">
                     				    					<xsl:variable name="sub_sub_channel" select="nav:name"/>
											<xsl:variable name="sub_sub_channel_path" select="concat('/',$main_channel,'/',$sub_channel,'/',$sub_sub_channel)"/>		     			                       
		    									<li>
						     			                       <xsl:element name="a">
						      							<xsl:attribute name="href">/views/pages/sub-sub-channel.jsp?channel=<xsl:value-of select="$sub_sub_channel_path"/></xsl:attribute>
						      							<xsl:value-of select="$sub_sub_channel"/>
						    					       </xsl:element>
		    									</li>
		    								</xsl:for-each>
		    								</ul>
		    							</div>
		    						</xsl:element>	
               						   </li>
               						   </xsl:for-each>
               					     </ul>
               					</div>
               				</xsl:element>
               			</xsl:element>	
            		</xsl:for-each>
            		<li class="yuimenubaritem"><a href="/views/pages/my-tools.jsp">My Tools</a></li>
          	</ul>  
         </div>
      </div>    
    </xsl:template>   
</xsl:stylesheet>