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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ApplicationModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;

/**
 * Removes the <b>uifacets</b> aspect incorrectly applied to the default set of Presentation
 * Templates loaded during bootstrap. For new installs the bootstrap XML file has been modified
 * to no longer apply the aspect.
 * <p>
 * This uses the bootstrap importer to get the paths to look for. 
 * 
 * @author Kevin Roast
 */
public class UIFacetsAspectRemovalPatch extends AbstractPatch
{
    private static final String MSG_UPDATED = "patch.uifacetsAspectRemovalPatch.updated";
    
    public static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    public static final String PROPERTY_DICTIONARY_CHILDNAME = "spaces.dictionary.childname";
    public static final String PROPERTY_TEMPLATES_CHILDNAME = "spaces.templates.content.childname";
    
    private ImporterBootstrap importerBootstrap;
    private MessageSource messageSource;
    
    protected Properties configuration;
    protected NodeRef templatesNodeRef;
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    public void setMessageSource(MessageSource messageSource)
    {
        this.messageSource = messageSource;
    }
    
    /**
     * Ensure that required properties have been set
     */
    protected void checkRequiredProperties() throws Exception
    {
        checkPropertyNotNull(importerBootstrap, "importerBootstrap");
        checkPropertyNotNull(messageSource, "messageSource");
    }
    
    /**
     * Extracts pertinent references and properties that are common to execution
     * of this and derived patches.
     * 
     * @return the number of updated template files
     */
    protected int removeAspectFromTemplates() throws Exception
    {
        // get the node store that we must work against
        StoreRef storeRef = importerBootstrap.getStoreRef();
        if (storeRef == null)
        {
            throw new PatchException("Bootstrap store has not been set");
        }
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
        
        this.configuration = importerBootstrap.getConfiguration();
        
        // get the association names that form the path
        String companyHomeChildName = configuration.getProperty(PROPERTY_COMPANY_HOME_CHILDNAME);
        if (companyHomeChildName == null || companyHomeChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_COMPANY_HOME_CHILDNAME + "' is not present");
        }
        String dictionaryChildName = configuration.getProperty(PROPERTY_DICTIONARY_CHILDNAME);
        if (dictionaryChildName == null || dictionaryChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_DICTIONARY_CHILDNAME + "' is not present");
        }
        String templatesChildName = configuration.getProperty(PROPERTY_TEMPLATES_CHILDNAME);
        if (templatesChildName == null || templatesChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_TEMPLATES_CHILDNAME + "' is not present");
        }
        
        // build the search string to get the email templates node
        StringBuilder sb = new StringBuilder(128);
        sb.append("/").append(companyHomeChildName)
          .append("/").append(dictionaryChildName)
          .append("/").append(templatesChildName)
          .append("//*[subtypeOf('cm:content')]");
        String xpath = sb.toString();
        
        // get the template content nodes
        int updated = 0;
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        for (NodeRef ref : nodeRefs)
        {
            // if the content has the uifacets aspect, then remove it and meaningless icon reference
            if (nodeService.hasAspect(ref, ApplicationModel.ASPECT_UIFACETS))
            {
                nodeService.removeAspect(ref, ApplicationModel.ASPECT_UIFACETS);
                nodeService.setProperty(ref, ApplicationModel.PROP_ICON, null);
                updated++;
            }
        }
        return updated;
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        // common properties must be set before we can continue
        checkRequiredProperties();
        
        int updated = removeAspectFromTemplates();
        
        // output a message to describe the result
        return I18NUtil.getMessage(MSG_UPDATED, updated);
    }
}
