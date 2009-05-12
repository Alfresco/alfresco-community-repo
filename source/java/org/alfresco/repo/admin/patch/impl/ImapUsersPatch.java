/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * As a special exception to the terms and conditions of version 2.0 of.
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.context.MessageSource;

/**
 * @author Dmitry Vaserin
 */
public class ImapUsersPatch extends AbstractPatch
{
    // messages' ids
    private static final String MSG_EXISTS = "patch.imapUserFolders.result.exists";
    private static final String MSG_CREATED = "patch.imapUserFolders.result.created";

    // folders' names for path building
    private static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    private static final String PROPERTY_IMAP_HOME_FOLDER_CHILDNAME = "spaces.imap_home.childname";

    private static final String PROPERTY_ICON = "space-icon-default";
    private static final String MSG_IMAP_HOME_FOLDER_NAME = "spaces.imap_home.name";
    private static final String MSG_IMAP_HOME_FOLDER_DESCRIPTION = "spaces.imap_home.description";

    private static final String INBOX_NAME = "INBOX";
    private static final String INBOX_DECSRIPTION = "INBOX mail box";

    private ImporterBootstrap importerBootstrap;
    private MessageSource messageSource;

    protected NodeRef companyHomeNodeRef;
    protected Properties configuration;
    protected NodeRef imapHomeNodeRef;

    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    public void setMessageSource(MessageSource messageSource)
    {
        this.messageSource = messageSource;
    }

    /**
     * Ensure that required common properties have been set
     */
    protected void checkCommonProperties() throws Exception
    {
        checkPropertyNotNull(importerBootstrap, "importerBootstrap");
        checkPropertyNotNull(namespaceService, "namespaceService");
        checkPropertyNotNull(searchService, "searchService");
        checkPropertyNotNull(nodeService, "nodeService");
        checkPropertyNotNull(messageSource, "messageSource");
    }

    /**
     * Extracts pertinent references and properties that are common to execution of this and derived patches.
     */
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
        String imapHomeChildName = configuration.getProperty(PROPERTY_IMAP_HOME_FOLDER_CHILDNAME);
        if (imapHomeChildName == null || imapHomeChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_IMAP_HOME_FOLDER_CHILDNAME + "' is not present");
        }

        // build the search string to get the company home node
        StringBuilder sb = new StringBuilder(256);
        sb.append("/").append(companyHomeChildName);
        String xpath = sb.toString();
        // get the company home
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("XPath didn't return any results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath + "\n" + "   results: " + nodeRefs);
        }
        this.companyHomeNodeRef = nodeRefs.get(0);

        xpath = imapHomeChildName;
        nodeRefs = searchService.selectNodes(companyHomeNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" + "   dictionary node: " + companyHomeNodeRef + "\n" + "   xpath: " + xpath + "\n" + "   results: "
                    + nodeRefs);
        }
        else if (nodeRefs.size() == 0)
        {
            // the node does not exist
            this.imapHomeNodeRef = null;
        }
        else
        {
            this.imapHomeNodeRef = nodeRefs.get(0);
        }
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // properties must be set
        checkCommonProperties();
        // get useful values
        setUp();

        String msg = null;

        if (imapHomeNodeRef == null)
        {
            // create it
            createImapHomeFolders();
            msg = I18NUtil.getMessage(MSG_CREATED, imapHomeNodeRef);
        }
        else
        {
            // it already exists
            msg = I18NUtil.getMessage(MSG_EXISTS, imapHomeNodeRef);
        }

        // done
        return msg;
    }

    private void createImapHomeFolders()
    {
        // get required properties
        String imapHomeChildName = configuration.getProperty(PROPERTY_IMAP_HOME_FOLDER_CHILDNAME);
        if (imapHomeChildName == null)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_IMAP_HOME_FOLDER_CHILDNAME + "' is not present");
        }

        String name = messageSource.getMessage(MSG_IMAP_HOME_FOLDER_NAME, null, I18NUtil.getLocale());
        if (name == null || name.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + MSG_IMAP_HOME_FOLDER_NAME + "' is not present");
        }

        String description = messageSource.getMessage(MSG_IMAP_HOME_FOLDER_DESCRIPTION, null, I18NUtil.getLocale());
        if (description == null || description.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + MSG_IMAP_HOME_FOLDER_DESCRIPTION + "' is not present");
        }

        imapHomeNodeRef = createSpace(companyHomeNodeRef, name, description, imapHomeChildName);

        // Create IMAP Home and "INBOX" for each user
        createImapUserHomes();

    }

    private void createImapUserHomes()
    {
        StringBuilder query = new StringBuilder(128);
        query.append("@").append(NamespaceService.CONTENT_MODEL_PREFIX).append("\\:userName:*");

        SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        params.addStore(importerBootstrap.getStoreRef());
        params.setQuery(query.toString());

        ResultSet results = searchService.query(params);
        List<NodeRef> people;
        try
        {
            people = results.getNodeRefs();
        }
        finally
        {
            results.close();
        }

        for (NodeRef nodeRef : people)
        {
            String userName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
            String desc = userName + " " + messageSource.getMessage(MSG_IMAP_HOME_FOLDER_NAME, null, I18NUtil.getLocale());
            NodeRef userHome = createSpace(imapHomeNodeRef, userName, desc, userName);
            // Create Inbox
            createSpace(userHome, INBOX_NAME, INBOX_DECSRIPTION, INBOX_NAME);

        }

    }

    private NodeRef createSpace(NodeRef parent, String name, String desc, String childName)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_TITLE, name);
        properties.put(ContentModel.PROP_DESCRIPTION, desc);
        properties.put(ApplicationModel.PROP_ICON, PROPERTY_ICON);
        // create the node
        ChildAssociationRef childAssocRef = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.resolveToQName(namespaceService, childName),
                ContentModel.TYPE_FOLDER, properties);
        NodeRef result = childAssocRef.getChildRef();

        // add the required aspects
        nodeService.addAspect(result, ApplicationModel.ASPECT_UIFACETS, null);
        return result;
    }
}
