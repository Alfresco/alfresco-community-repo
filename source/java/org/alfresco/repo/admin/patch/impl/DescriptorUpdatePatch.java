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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Apply Version Edition to Repository Descriptor
 *
 * @author David Caruana
 */
public class DescriptorUpdatePatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.descriptorUpdate.result";

    private ImporterBootstrap systemBootstrap;
    
    public void setSystemBootstrap(ImporterBootstrap systemBootstrap)
    {
        this.systemBootstrap = systemBootstrap;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        checkPropertyNotNull(systemBootstrap, "systemBootstrap");
        
        // retrieve system descriptor location
        StoreRef storeRef = systemBootstrap.getStoreRef();
        Properties systemProperties = systemBootstrap.getConfiguration();

        // check for the store
        if (nodeService.exists(storeRef))
        {
            // get the current descriptor
            String path = systemProperties.getProperty("system.descriptor.current.childname");
            String searchPath = "/" + path;
            NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, searchPath, null, namespaceService, false);
            if (nodeRefs.size() > 0)
            {
                NodeRef descriptorNodeRef = nodeRefs.get(0);

                // set version edition
                Serializable value = nodeService.getProperty(descriptorNodeRef, ContentModel.PROP_SYS_VERSION_EDITION);
                if (value == null)
                {
                    String edition = systemProperties.getProperty("version.edition");
                    Collection<String> editions = new ArrayList<String>();
                    editions.add(edition);
                    nodeService.setProperty(descriptorNodeRef, ContentModel.PROP_SYS_VERSION_EDITION, (Serializable)editions);
                }
            }
        }
        
        // done
        String msg = I18NUtil.getMessage(MSG_SUCCESS);
        return msg;
    }
    
}
