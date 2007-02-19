/*
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
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.alfresco.i18n.I18NUtil;
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
    private static final String MSG_EXISTS = "patch.genericBootstrap.result.exists";
    private static final String MSG_CREATED = "patch.genericBootstrap.result.created";
    private static final String ERR_MULTIPLE_FOUND = "patch.genericBootstrap.err.multiple_found";
    
    private ImporterBootstrap importerBootstrap;
    private String checkPath;
    private Properties bootstrapView;

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
        List<Properties> bootstrapViews = Collections.singletonList(bootstrapView);
        // modify the bootstrapper
        importerBootstrap.setBootstrapViews(bootstrapViews);
        importerBootstrap.setUseExistingStore(true);              // allow import into existing store

        importerBootstrap.bootstrap();
        // done
        return I18NUtil.getMessage(MSG_CREATED, path, rootNodeRef);
    }
}
