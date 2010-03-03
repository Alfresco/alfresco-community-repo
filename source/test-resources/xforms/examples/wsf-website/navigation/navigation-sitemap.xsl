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
