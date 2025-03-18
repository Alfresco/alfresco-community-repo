/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.view;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

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
    private boolean crawlAssociations = true;
    private boolean crawlContent = true;
    private boolean crawlNullProperties = true;
    private ReferenceType referenceType = ReferenceType.PATHREF;
    private String[] excludeNamespaceURIs = new String[]{NamespaceService.REPOSITORY_VIEW_1_0_URI};
    private QName[] excludeAspects = new QName[]{};
    private QName[] excludeChildAssocs = new QName[]{};
    private String[] includedPaths = null;

    /**
     * Crawl and export child nodes
     * 
     * @return true => crawl child nodes
     */
    public boolean isCrawlChildNodes()
    {
        return crawlChildNodes;
    }

    /**
     * Sets whether to crawl child nodes
     * 
     * @param crawlChildNodes
     *            boolean
     */
    public void setCrawlChildNodes(boolean crawlChildNodes)
    {
        this.crawlChildNodes = crawlChildNodes;
    }

    /**
     * Crawl and export associations
     * 
     * @return true => crawl associations
     */
    public boolean isCrawlAssociations()
    {
        return crawlAssociations;
    }

    /**
     * Sets whether to crawl associations
     * 
     * @param crawlAssociations
     *            boolean
     */
    public void setCrawlAssociations(boolean crawlAssociations)
    {
        this.crawlAssociations = crawlAssociations;
    }

    /**
     * Crawl and export content properties
     * 
     * @return true => crawl content
     */
    public boolean isCrawlContent()
    {
        return crawlContent;
    }

    /**
     * Sets whether to crawl content
     * 
     * @param crawlContent
     *            boolean
     */
    public void setCrawlContent(boolean crawlContent)
    {
        this.crawlContent = crawlContent;
    }

    /**
     * Crawl and export node at export path
     * 
     * @return true => crawl node at export path
     */
    public boolean isCrawlSelf()
    {
        return crawlSelf;
    }

    /**
     * Sets whether to crawl and export node at export path
     * 
     * @param crawlSelf
     *            boolean
     */
    public void setCrawlSelf(boolean crawlSelf)
    {
        this.crawlSelf = crawlSelf;
    }

    /**
     * Crawl and export null properties
     * 
     * @return true => export null properties
     */
    public boolean isCrawlNullProperties()
    {
        return crawlNullProperties;
    }

    /**
     * Sets whether to crawl null properties
     * 
     * @param crawlNullProperties
     *            boolean
     */
    public void setCrawlNullProperties(boolean crawlNullProperties)
    {
        this.crawlNullProperties = crawlNullProperties;
    }

    /**
     * Gets the list of namespace URIs to exlude from the Export
     * 
     * @return the list of namespace URIs
     */
    public String[] getExcludeNamespaceURIs()
    {
        return excludeNamespaceURIs;
    }

    /**
     * Sets the list of namespace URIs to exclude from the Export
     * 
     * @param excludeNamespaceURIs
     *            String[]
     */
    public void setExcludeNamespaceURIs(String[] excludeNamespaceURIs)
    {
        this.excludeNamespaceURIs = excludeNamespaceURIs;
    }

    /**
     * Gets the list of Aspects to exclude from the Export
     * 
     * @return the list of Aspects
     */
    public QName[] getExcludeAspects()
    {
        return excludeAspects;
    }

    /**
     * Sets the list of Aspects to exclude from the Export
     * 
     * @param excludeAspects
     *            QName[]
     */
    public void setExcludeAspects(QName[] excludeAspects)
    {
        this.excludeAspects = excludeAspects;
    }

    /**
     * Gets the list of Child Associations to exclude from the Export
     * 
     * @return the list of child assocs
     */
    public QName[] getExcludeChildAssocs()
    {
        return excludeChildAssocs;
    }

    /**
     * Sets the list of Child Associations to exclude from the Export
     * 
     * @param excludeChildAssocs
     *            QName[]
     */
    public void setExcludeChildAssocs(QName[] excludeChildAssocs)
    {
        this.excludeChildAssocs = excludeChildAssocs;
    }

    /**
     * Gets the list of included paths to explicitly include in the Export
     * 
     * @return the list of included paths
     */
    public String[] getIncludedPaths()
    {
        return includedPaths;
    }

    /**
     * Sets the list of included paths to explicitly include in the Export
     *
     * @param includedPaths
     *            String[]
     */
    public void setIncludedPaths(String[] includedPaths)
    {
        this.includedPaths = includedPaths;
    }

    /**
     * Gets the path to export from
     * 
     * @return the path to export from
     */
    public Location getExportFrom()
    {
        return exportFrom;
    }

    /**
     * Sets the path to export from
     * 
     * @param exportFrom
     *            Location
     */
    public void setExportFrom(Location exportFrom)
    {
        this.exportFrom = exportFrom;
    }

    /**
     * Gets the format of exported references
     * 
     * @return reference type
     */
    public ReferenceType getReferenceType()
    {
        return referenceType;
    }

    /**
     * Sets the format of exported references (child and association references)
     * 
     * @param referenceType
     *            type
     */
    public void setReferenceType(ReferenceType referenceType)
    {
        this.referenceType = referenceType;
    }

}
