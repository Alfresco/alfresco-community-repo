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

import java.util.List;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.surf.util.I18NUtil;

public class ClearOldImapMessgesPatch extends AbstractPatch
{
    private static final String MSG_REMOVED = "patch.imap.clear.old.messages.description.cleared";
    
    private static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    private static final String PROPERTY_DICTIONARY_CHILDNAME = "spaces.dictionary.childname";
    private static final String PROPERTY_SCRIPTS_CHILDNAME = "spaces.scripts.childname";
    private static final String PROPERTY_IMAP_CONFIG_CHILDNAME = "spaces.imapConfig.childname";
    private static final String PROPERTY_IMAP_TEMPLATES_CHILDNAME = "spaces.imap_templates.childname";

    private ImporterBootstrap importerBootstrap;
    protected Properties configuration;
    private NodeRef imapTemplatesFolderNodeRef;

    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    protected void setUp() throws Exception
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
        String scriptsChildName = configuration.getProperty(PROPERTY_SCRIPTS_CHILDNAME);
        if (scriptsChildName == null || scriptsChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SCRIPTS_CHILDNAME + "' is not present");
        }

        String imapConfigChildName = configuration.getProperty(PROPERTY_IMAP_CONFIG_CHILDNAME);
        if (imapConfigChildName == null || imapConfigChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_IMAP_CONFIG_CHILDNAME + "' is not present");
        }

        String imapTemplatesChildName = configuration.getProperty(PROPERTY_IMAP_TEMPLATES_CHILDNAME);
        if (imapConfigChildName == null || imapConfigChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_IMAP_CONFIG_CHILDNAME + "' is not present");
        }

        // build the search string to get the company home node
        StringBuilder sb = new StringBuilder(256);
        sb.append("/").append(companyHomeChildName);
        sb.append("/").append(dictionaryChildName);
        sb.append("/").append(imapConfigChildName);
        sb.append("/").append(imapTemplatesChildName);

        String xpath = sb.toString();
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath + "\n" + "   results: " + nodeRefs);
        }
        else if (nodeRefs.size() == 0)
        {
            this.imapTemplatesFolderNodeRef = null;
        }
        else
        {
            this.imapTemplatesFolderNodeRef = nodeRefs.get(0);
        }

    }

    @Override
    protected String applyInternal() throws Exception
    {
        setUp();
        if (imapTemplatesFolderNodeRef != null)
        {
            NodeRef oldTextPlain = nodeService.getChildByName(imapTemplatesFolderNodeRef, ContentModel.ASSOC_CONTAINS, "emailbody-textplain.ftl");
            NodeRef oldTextHTML = nodeService.getChildByName(imapTemplatesFolderNodeRef, ContentModel.ASSOC_CONTAINS, "emailbody-texthtml.ftl");

            if (oldTextPlain != null)
            {
                nodeService.deleteNode(oldTextPlain);
            }
            if (oldTextHTML != null)
            {
                nodeService.deleteNode(oldTextHTML);
            }
        }

        return I18NUtil.getMessage(MSG_REMOVED);
    }

}
