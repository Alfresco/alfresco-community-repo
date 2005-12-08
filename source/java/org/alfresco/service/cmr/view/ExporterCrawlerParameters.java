/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.view;

import org.alfresco.service.namespace.NamespaceService;


/**
 * Exporter Crawler Configuration.
 * 
 * This class is used to specify which Repository items are exported. 
 * 
 * @author David Caruana
 */
public class ExporterCrawlerParameters
{

    private Location exportFrom = null;
    private boolean crawlSelf = false;
    private boolean crawlChildNodes = true;
    private boolean crawlContent = true;
    private boolean crawlNullProperties = true;
    private String[] excludeNamespaceURIs = new String[] { NamespaceService.REPOSITORY_VIEW_1_0_URI };

    
    /**
     * Crawl and export child nodes
     * 
     * @return  true => crawl child nodes
     */
    public boolean isCrawlChildNodes()
    {
        return crawlChildNodes;
    }

    /**
     * Sets whether to crawl child nodes
     * 
     * @param crawlChildNodes
     */
    public void setCrawlChildNodes(boolean crawlChildNodes)
    {
        this.crawlChildNodes = crawlChildNodes;
    }

    /**
     * Crawl and export content properties
     * 
     * @return  true => crawl content
     */
    public boolean isCrawlContent()
    {
        return crawlContent;
    }

    /**
     * Sets whether to crawl content
     * 
     * @param crawlContent
     */
    public void setCrawlContent(boolean crawlContent)
    {
        this.crawlContent = crawlContent;
    }

    /**
     * Crawl and export node at export path
     * 
     * @return  true => crawl node at export path
     */
    public boolean isCrawlSelf()
    {
        return crawlSelf;
    }

    /**
     * Sets whether to crawl and export node at export path
     * 
     * @param crawlSelf
     */
    public void setCrawlSelf(boolean crawlSelf)
    {
        this.crawlSelf = crawlSelf;
    }

    /**
     * Crawl and export null properties
     * 
     * @return  true => export null properties
     */
    public boolean isCrawlNullProperties()
    {
        return crawlNullProperties;
    }

    /**
     * Sets whether to crawl null properties
     * 
     * @param crawlNullProperties
     */
    public void setCrawlNullProperties(boolean crawlNullProperties)
    {
        this.crawlNullProperties = crawlNullProperties;
    }

    /**
     * Gets the list of namespace URIs to exlude from the Export
     * 
     * @return  the list of namespace URIs
     */
    public String[] getExcludeNamespaceURIs()
    {
        return excludeNamespaceURIs;
    }

    /**
     * Sets the list of namespace URIs to exclude from the Export
     * 
     * @param excludeNamespaceURIs
     */
    public void setExcludeNamespaceURIs(String[] excludeNamespaceURIs)
    {
        this.excludeNamespaceURIs = excludeNamespaceURIs;
    }

    /**
     * Gets the path to export from
     * 
     * @return  the path to export from
     */
    public Location getExportFrom()
    {
        return exportFrom;
    }

    /**
     * Sets the path to export from
     * 
     * @param exportFrom
     */
    public void setExportFrom(Location exportFrom)
    {
        this.exportFrom = exportFrom;
    }

}
