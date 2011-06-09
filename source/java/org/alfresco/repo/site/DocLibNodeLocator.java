/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 */

package org.alfresco.repo.site;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.nodelocator.AbstractNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocator;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;

/**
 * This {@link NodeLocator} identifies the site in which the source node resides and returns the Document Library container for that site.
 * If no site can be found or the site does not have a Document Library then the Company Home is returned.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class DocLibNodeLocator extends AbstractNodeLocator
{
    public static final String NAME = "doclib";

    private SiteService siteService;
    private Repository repositoryHelper;
    
    /**
     * Finds the site in which the source {@link NodeRef} resides and returns the Document Library container for that site.
     * If no site can be found or the site does not have a Document Library then the Company Home is returned.
     * 
     * @param sourceNode the starting point for locating the site Document Library.
     * @param params Not used.
     * @return the Document Library or the Company Home.
     */
    @Override
    public NodeRef getNode(NodeRef source, Map<String, Serializable> params)
    {
        NodeRef docLib = null;
        if (source != null)
        {
            SiteInfo siteInfo = siteService.getSite(source);
            if (siteInfo != null)
            {
                String siteName = siteInfo.getShortName();
                String containerId = SiteService.DOCUMENT_LIBRARY;
                docLib = siteService.getContainer(siteName, containerId);
            }
        }
        if (docLib == null)
        {
            docLib = repositoryHelper.getCompanyHome();
        }
        return docLib;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getName()
    {
        return NAME;
    }

    /**
     * @param siteService the siteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * @param repositoryHelper the repositoryHelper to set
     */
    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }
}
