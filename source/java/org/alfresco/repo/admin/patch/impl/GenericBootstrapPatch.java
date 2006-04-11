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
        checkPropertyNotNull(checkPath, "checkPath");
        checkPropertyNotNull(bootstrapView, "bootstrapView");
        // fulfil contract of override
        super.checkProperties();
    }

    @Override
    protected String applyInternal() throws Exception
    {
        StoreRef storeRef = importerBootstrap.getStoreRef();
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
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
