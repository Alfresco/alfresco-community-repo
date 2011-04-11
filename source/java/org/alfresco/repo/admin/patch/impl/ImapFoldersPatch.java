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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.MessageSource;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.FileCopyUtils;
/**
 * Builds folders tree necessary for IMAP functionality and imports email action scripts.
 * 
 * 1. Company Home > Data Dictionary > Imap Config > Templates
 * 2. Company Home > Data Dictionary > Email Actions > search
 * 3. Company Home > Data Dictionary > Scripts > command-processor.js, command-search.js
 * 
 * @author Arseny Kovalchuk
 */
public class ImapFoldersPatch extends AbstractPatch
{
    // messages' ids
    private static final String MSG_EXISTS = "patch.imapFolders.result.exists";
    private static final String MSG_CREATED = "patch.imapFolders.result.created";

    // folders' names for path building
    private static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    private static final String PROPERTY_DICTIONARY_CHILDNAME = "spaces.dictionary.childname";
    private static final String PROPERTY_SCRIPTS_CHILDNAME = "spaces.scripts.childname";
    private static final String PROPERTY_IMAP_CONFIG_CHILDNAME = "spaces.imapConfig.childname";

    private ImporterBootstrap importerBootstrap;
    private MessageSource messageSource;
    protected Properties configuration;
    private ImporterService importerService;

    private NodeRef companyHomeNodeRef;
    private NodeRef dictionaryNodeRef;
    private NodeRef scriptsNodeRef;
    private NodeRef imapConfigFolderNodeRef;
    private String configFoldersACP;
    private String emailActionsACP;
    private String scriptsACP;

    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    public void setMessageSource(MessageSource messageSource)
    {
        this.messageSource = messageSource;
    }

    public void setImporterService(ImporterService importerService)
    {
        this.importerService = importerService;
    }

    public void setConfigFoldersACP(String configFoldersACP)
    {
        this.configFoldersACP = configFoldersACP;
    }

    public void setEmailActionsACP(String emailActionsACP)
    {
        this.emailActionsACP = emailActionsACP;
    }

    public void setScriptsACP(String scriptsACP)
    {
        this.scriptsACP = scriptsACP;
    }

    protected void checkCommonProperties() throws Exception
    {
        checkPropertyNotNull(importerBootstrap, "importerBootstrap");
        checkPropertyNotNull(messageSource, "messageSource");
        checkPropertyNotNull(importerService, "importerService");
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

        // build the search string to get the dictionary node
        sb.append("/").append(dictionaryChildName);
        xpath = sb.toString();
        // get the dictionary node
        nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("XPath didn't return any results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath + "\n" + "   results: " + nodeRefs);
        }
        this.dictionaryNodeRef = nodeRefs.get(0);
        sb.append("/").append(scriptsChildName);
        xpath = sb.toString();
        nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("XPath didn't return any results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath + "\n" + "   results: " + nodeRefs);
        }
        this.scriptsNodeRef = nodeRefs.get(0);
        // get the ImapConfig node
        sb.delete((sb.length() - (scriptsChildName.length() + 1)), sb.length());
        sb.append("/").append(imapConfigChildName);
        xpath = sb.toString();
        nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath + "\n" + "   results: " + nodeRefs);
        }
        else if (nodeRefs.size() == 0)
        {
            this.imapConfigFolderNodeRef = null;
        }
        else
        {
            this.imapConfigFolderNodeRef = nodeRefs.get(0);
        }

    }

    @Override
    protected String applyInternal() throws Exception
    {
        checkCommonProperties();
        setUp();
        String msg = null;
        if (imapConfigFolderNodeRef == null)
        {
            // import the content
            final RunAsWork<Object> importRunAs = new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    importImapConfig();
                    importScripts();
                    importEmailActions();
                    return null;
                }
            };
            
            RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable 
                {
                    AuthenticationUtil.runAs(importRunAs, authenticationContext.getSystemUserName());
                    return null;
                }
         
            };
            
            transactionHelper.doInTransaction(cb, false, true);
            msg = I18NUtil.getMessage(MSG_CREATED);
        }
        else
        {
            msg = I18NUtil.getMessage(MSG_EXISTS, imapConfigFolderNodeRef);
        }
        return msg;

    }

    private void importImapConfig() throws IOException
    {
        importInternal(this.configFoldersACP, this.dictionaryNodeRef);
    }

    private void importEmailActions() throws IOException
    {
        importInternal(this.emailActionsACP, this.dictionaryNodeRef);
    }

    private void importScripts() throws IOException
    {
        importInternal(this.scriptsACP, this.scriptsNodeRef);
    }

    private void importInternal(String acpName, NodeRef space) throws IOException
    {
        File acpFile = getFile(acpName);
        ACPImportPackageHandler acpHandler = new ACPImportPackageHandler(acpFile, null);
        Location importLocation = new Location(space);
        importerService.importView(acpHandler, importLocation, null, null);
    }

    private File getFile(String acpName)
    {
        // Try as a file location
        File file = new File(acpName);
        if ((file != null) && (file.exists()))
        {
            return file;
        }
        else
        {
            // Try as a classpath location
            
            // Get input stream
            InputStream viewStream = getClass().getClassLoader().getResourceAsStream(acpName);
            if (viewStream == null)
            {
                throw new ImporterException("Could not find view file " + acpName);
            }
            
            // Create output stream
            File tempFile = TempFileProvider.createTempFile("acpImport", ".tmp");
            try
            {
                FileOutputStream os = new FileOutputStream(tempFile);
                FileCopyUtils.copy(viewStream, os);
            }
            catch (FileNotFoundException e)
            {
                throw new ImporterException("Could not import view " + acpName, e);
            }
            catch (IOException e)
            {
                throw new ImporterException("Could not import view " + acpName, e);
            }
            return tempFile;
        }
    }
}
