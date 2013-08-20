/*
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
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Generic patch that uses existing {@link org.alfresco.repo.importer.ImporterBootstrap importers}
 * to import snippets into the system.  These snippets would otherwise have been bootstrapped by
 * a clean install.
 * <p>
 * By providing this class with a bootstrap view and an importer, it can check whether the path
 * exists and perform the import if it doesn't.
 * 
 * @author Derek Hulley
 */
public class GenericBootstrapPatch extends AbstractPatch
{
    protected static final String MSG_EXISTS = "patch.genericBootstrap.result.exists";
    protected static final String MSG_CREATED = "patch.genericBootstrap.result.created";
    protected static final String MSG_DEFERRED = "patch.genericBootstrap.result.deferred";
    protected static final String ERR_MULTIPLE_FOUND = "patch.genericBootstrap.err.multiple_found";
    
    protected ImporterBootstrap importerBootstrap;
    protected String checkPath;
    protected Properties bootstrapView;

    /**
     * @param importerBootstrap the bootstrap bean that performs the user store bootstrap
     */
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    /**
     * Set the XPath statement that must be executed to check whether the import data is
     * already present or not.
     * 
     * @param checkPath an XPath statement
     */
    public void setCheckPath(String checkPath)
    {
        this.checkPath = checkPath;
    }

    /**
     * @see ImporterBootstrap#setBootstrapViews(List)
     * 
     * @param bootstrapView the bootstrap location
     */
    public void setBootstrapView(Properties bootstrapView)
    {
        this.bootstrapView = bootstrapView;
    }

    @Override
    protected void checkProperties()
    {
        checkPropertyNotNull(importerBootstrap, "importerBootstrap");
        checkPropertyNotNull(bootstrapView, "bootstrapView");
        // fulfil contract of override
        super.checkProperties();
    }


    @Override
    protected String applyInternal() throws Exception
    {
        StoreRef storeRef = importerBootstrap.getStoreRef();
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        if (checkPath != null)
        {
            List<NodeRef> results = searchService.selectNodes(
                    rootNodeRef,
                    checkPath,
                    null,
                    namespaceService,
                    false);
            if (results.size() > 1)
            {
                throw new PatchException(ERR_MULTIPLE_FOUND, checkPath);
            }
            else if (results.size() == 1)
            {
                // nothing to do - it exsists
                return I18NUtil.getMessage(MSG_EXISTS, checkPath);
                
            }
        }
        String path = bootstrapView.getProperty("path");
        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        // modify the bootstrapper
        importerBootstrap.setBootstrapViews(bootstrapViews);
        importerBootstrap.setUseExistingStore(true);              // allow import into existing store

        importerBootstrap.bootstrap();
        // done
        return I18NUtil.getMessage(MSG_CREATED, path, rootNodeRef);
    }


}
