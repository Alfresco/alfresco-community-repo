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
package org.alfresco.cmis.mapping;

import java.util.Date;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.cmis.CMISAccessControlService;
import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISRenditionService;
import org.alfresco.cmis.CMISServices;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.NamespaceDAOImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Base CMIS test
 * Basic TX control and authentication
 * 
 * @author andyh
 *
 */
public abstract class BaseCMISTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    protected CMISMapping cmisMapping;
    
    protected CMISServices cmisService;
    
    protected CMISDictionaryService cmisDictionaryService;
    
    protected CMISRenditionService cmisRenditionService;
    
    protected CMISAccessControlService cmisAccessControlService;
    
    protected DictionaryService dictionaryService;

    protected TransactionService transactionService;

    protected AuthenticationComponent authenticationComponent;

    protected UserTransaction testTX;

    protected NodeService nodeService;

    protected NodeRef rootNodeRef;

    protected FileFolderService fileFolderService;

    protected ServiceRegistry serviceRegistry;

    protected NamespaceService namespaceService;
    
    protected CMISQueryService cmisQueryService;

    private MutableAuthenticationService authenticationService;

    private MutableAuthenticationDao authenticationDAO;

    protected SearchService searchService;

    protected ContentService contentService;

    protected PermissionService permissionService;
    
    protected ThumbnailService thumbnailService;
    
    protected ModelDAO permissionModelDao;

    protected DictionaryDAO dictionaryDAO;

    protected NamespaceDAOImpl namespaceDao;

    protected VersionService versionService;

    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        
        cmisDictionaryService = (CMISDictionaryService) ctx.getBean("CMISDictionaryService");
        cmisMapping = (CMISMapping) ctx.getBean("CMISMapping");
        cmisQueryService = (CMISQueryService) ctx.getBean("CMISQueryService");
        cmisService = (CMISServices) ctx.getBean("CMISService");
        cmisRenditionService = (CMISRenditionService) ctx.getBean("CMISRenditionService");
        cmisAccessControlService = (CMISAccessControlService) ctx.getBean("CMISAccessControlService");
        dictionaryService = (DictionaryService) ctx.getBean("dictionaryService");
        nodeService = (NodeService) ctx.getBean("nodeService");
        fileFolderService = (FileFolderService) ctx.getBean("fileFolderService");
        namespaceService = (NamespaceService) ctx.getBean("namespaceService");
        
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        
        searchService = (SearchService) ctx.getBean("searchService");
        
        contentService = (ContentService) ctx.getBean("contentService");
        
        permissionService = (PermissionService) ctx.getBean("permissionService");
        
        versionService = (VersionService) ctx.getBean("versionService");
        
        authenticationService = (MutableAuthenticationService) ctx.getBean("authenticationService");
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
        
        thumbnailService = (ThumbnailService) ctx.getBean("thumbnailService");
        
        permissionModelDao = (ModelDAO) ctx.getBean("permissionsModelDAO");
        
        dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        namespaceDao = (NamespaceDAOImpl) ctx.getBean("namespaceDAO");
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        String storeName = "CMISTest-" + getStoreName() + "-" + (new Date().getTime());
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, storeName);
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        if(authenticationDAO.userExists("cmis"))
        {
            authenticationService.deleteAuthentication("cmis");
        }
        authenticationService.createAuthentication("cmis", "cmis".toCharArray());        
    }
    
    private String getStoreName()
    {
        String testName = getName();
        testName = testName.replace("_", "-");
        testName = testName.replace("%", "-");
        return testName;
        
    }

    protected void runAs(String userName)
    {
        authenticationService.authenticate(userName, userName.toCharArray());
        assertNotNull(authenticationService.getCurrentUserName());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (testTX.getStatus() == Status.STATUS_ACTIVE)
        {
            testTX.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }
}
