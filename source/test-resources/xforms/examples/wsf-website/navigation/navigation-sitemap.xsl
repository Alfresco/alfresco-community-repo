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
    xmlns:nav="http://www.alfresco.org/alfresco/navigation" version="1.0">
    <xsl:output method="xml" />
    <xsl:template match="/nav:navigation">
        <xsl:variable name="urlPrefix" select="string($alf:avm_sandbox_url)"/>
        <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
            <url>
                <loc>
                    <xsl:value-of select="$urlPrefix"/>/views/pages/index.jsp
                </loc>
            </url>
            <xsl:for-each select="nav:main_channel">
                <xsl:variable name="main_channel" select="nav:name"/>
                <url>
                    <loc>
                        <xsl:value-of select="$urlPrefix"/>/views/pages/main-channel.jsp?channel=/<xsl:value-of select="$main_channel"/>
                    </loc>
                </url>
                <xsl:for-each select="nav:content_link">
                    <xsl:variable name="content_link" select="nav:path"/>
                    <url>
                        <loc>
                            <xsl:value-of select="$urlPrefix"/>/views/pages/details.jsp?channel=/<xsl:value-of select="$main_channel"/>&amp;content=<xsl:value-of select="$content_link"/>
                        </loc>
                    </url>
                </xsl:for-each>    
                <xsl:for-each select="nav:sub_channel">
                    <xsl:variable name="sub_channel" select="nav:name"/>
                    <xsl:variable name="sub_channel_path" select="concat('/',$main_channel,'/',$sub_channel)"/>
                    <url>
                        <loc>
                            <xsl:value-of select="$urlPrefix"/>/views/pages/sub-channel.jsp?channel=<xsl:value-of select="$sub_channel_path"/>
                        </loc>
                    </url>
                <xsl:for-each select="nav:content_link">
                    <xsl:variable name="content_link" select="nav:path"/>
                    <url>
                        <loc>
                            <xsl:value-of select="$urlPrefix"/>/views/pages/details.jsp?channel=<xsl:value-of select="$sub_channel_path"/>&amp;content=<xsl:value-of select="$content_link"/>
                        </loc>
                    </url>
                </xsl:for-each>    
                    <xsl:for-each select="nav:sub_sub_channel">
                        <xsl:variable name="sub_sub_channel" select="nav:name"/>
                        <xsl:variable name="sub_sub_channel_path" select="concat($sub_channel_path,'/',$sub_sub_channel)"/>
                        <url>
                            <loc>
                                <xsl:value-of select="$urlPrefix"/>/views/pages/sub-sub-channel.jsp?channel=<xsl:value-of select="$sub_sub_channel_path"/>
                            </loc>
                        </url>
                        <xsl:for-each select="nav:content_link">
                            <xsl:variable name="content_link" select="nav:path"/>
                            <url>
                                <loc>
                                    <xsl:value-of select="$urlPrefix"/>/views/pages/details.jsp?channel=<xsl:value-of select="$sub_sub_channel_path"/>&amp;content=<xsl:value-of select="$content_link"/>
                                </loc>
                            </url>
                        </xsl:for-each>    
                    </xsl:for-each>    
                </xsl:for-each>    
            </xsl:for-each>
            <xsl:for-each select="nav:content_link">
                <xsl:variable name="content_link" select="nav:path"/>
                <url>
                    <loc>
                        <xsl:value-of select="$urlPrefix"/>/views/pages/details.jsp?channel=/&amp;content=<xsl:value-of select="$content_link"/>
                    </loc>
                </url>
            </xsl:for-each>    
        </urlset>    
    </xsl:template>   
</xsl:stylesheet>
