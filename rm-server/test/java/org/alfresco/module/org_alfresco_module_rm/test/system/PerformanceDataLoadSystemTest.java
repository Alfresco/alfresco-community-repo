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
package org.alfresco.module.org_alfresco_module_rm.test.system;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Roy Wetherall
 */
public class PerformanceDataLoadSystemTest extends TestCase implements RecordsManagementModel, DOD5015Model
{
    private ApplicationContext appContext;
    private AuthenticationComponent authenticationComponent;
    private RecordsManagementService rmService;
    private DispositionService dispositionService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private ContentService contentService;
    private IdentifierService identifierService;
    
    UserTransaction userTransaction;
    
    private int SERIES_COUNT = 1;
    private int CATEGORY_COUNT = 1;
    private int RECORD_FOLDER_COUNT = 1;
    private int RECORD_COUNT = 700;    
    
    @Override
    protected void setUp() throws Exception
    {
        appContext = ApplicationContextHelper.getApplicationContext();
        authenticationComponent = (AuthenticationComponent)appContext.getBean("authenticationComponent");
        transactionService = (TransactionService)appContext.getBean("transactionService");
        nodeService = (NodeService)appContext.getBean("nodeService");
        rmService = (RecordsManagementService)appContext.getBean("recordsManagementService");
        contentService = (ContentService)appContext.getBean("contentService");
        identifierService = (IdentifierService)appContext.getBean("identifierService");
        dispositionService = (DispositionService)appContext.getBean("dispositionService");
    
        // Set authentication       
        authenticationComponent.setCurrentUser("admin");    
        
        // Start transaction
        userTransaction = transactionService.getUserTransaction();
        userTransaction.begin();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        userTransaction.commit();
    }
    
    public void testLoadTestData() throws Exception
    {
        // Get the file plan node
        List<NodeRef> roots = rmService.getFilePlans();
        if (roots.size() != 1)
        {
            fail("There is more than one root to load the test data into.");
        }
        NodeRef filePlan = roots.get(0);
        
        for (int i = 0; i < SERIES_COUNT; i++)
        {
            // Create the series
            createSeries(filePlan, i);
        }
    }
    
    private void createSeries(NodeRef filePlan, int index) throws Exception
    {
        String name = genName("series-", index, "-" + System.currentTimeMillis());
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(2);
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(PROP_IDENTIFIER, identifierService.generateIdentifier(TYPE_RECORD_CATEGORY, filePlan));
        NodeRef series = nodeService.createNode(
                                       filePlan, 
                                       ContentModel.ASSOC_CONTAINS, 
                                       QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                                       TYPE_RECORD_CATEGORY,
                                       properties).getChildRef();
        
        System.out.println("Created series '" + name);
        
        // Create the categories
        for (int i = 0; i < CATEGORY_COUNT; i++)
        {
            createCategory(series, i);
        }
    }
    
    private void createCategory(NodeRef series, int index) throws Exception
    {
        String name = genName("category-", index);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(ContentModel.PROP_DESCRIPTION, "Test category");
        NodeRef cat = nodeService.createNode(
                                       series, 
                                       ContentModel.ASSOC_CONTAINS, 
                                       QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                                       TYPE_RECORD_CATEGORY,                                       
                                       properties).getChildRef();

        // Need to close the transaction and reopen to kick off required initialisation behaviour
        userTransaction.commit();
        userTransaction = transactionService.getUserTransaction();
        userTransaction.begin();
        
        properties = nodeService.getProperties(cat);
        //properties.put(PROP_IDENTIFIER, identifierService.generateIdentifier(series));
        properties.put(PROP_VITAL_RECORD_INDICATOR, true);
        properties.put(PROP_REVIEW_PERIOD, new Period("week|1"));
        nodeService.setProperties(cat, properties);
        
        // Get the disposition schedule
        DispositionSchedule ds = dispositionService.getDispositionSchedule(cat);
        properties = nodeService.getProperties(ds.getNodeRef());
        properties.put(PROP_DISPOSITION_AUTHORITY, "Disposition Authority");
        properties.put(PROP_DISPOSITION_INSTRUCTIONS, "Test disposition");
        nodeService.setProperties(ds.getNodeRef(), properties);
        
        // Add cutoff disposition action
        Map<QName, Serializable> actionParams = new HashMap<QName, Serializable>(2);
        actionParams.put(PROP_DISPOSITION_ACTION_NAME, "cutoff");
        actionParams.put(PROP_DISPOSITION_PERIOD, new Period("day|1"));
        dispositionService.addDispositionActionDefinition(ds, actionParams);
        
        // Add delete disposition action
        actionParams = new HashMap<QName, Serializable>(3);
        actionParams.put(PROP_DISPOSITION_ACTION_NAME, "destroy");
        actionParams.put(PROP_DISPOSITION_PERIOD, new Period("immediately|0"));
        actionParams.put(PROP_DISPOSITION_PERIOD_PROPERTY, QName.createQName("{http://www.alfresco.org/model/recordsmanagement/1.0}cutOffDate"));
        dispositionService.addDispositionActionDefinition(ds, actionParams);
        
        System.out.println("Created category '" + name);
        
        // Create the record folders        
        for (int i = 0; i < RECORD_FOLDER_COUNT; i++)
        {
            // Create the series
            createRecordFolder(cat, i);            
        }        
    }
    
    private void createRecordFolder(NodeRef cat, int index) throws Exception
    {
        String name = genName("folder-", index);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(2);
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(PROP_IDENTIFIER, identifierService.generateIdentifier(TYPE_RECORD_FOLDER, cat));
        NodeRef rf = nodeService.createNode(
                                       cat, 
                                       ContentModel.ASSOC_CONTAINS, 
                                       QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                                       TYPE_RECORD_FOLDER,
                                       properties).getChildRef();
        
        // Need to close the transaction and reopen to kick off required initialisation behaviour
        userTransaction.commit();
        userTransaction = transactionService.getUserTransaction();
        userTransaction.begin();
        
        System.out.println("Created record folder '" + name);
        
        // Create the records        
        for (int i = 0; i < RECORD_COUNT; i++)
        {
            createRecord(rf, i);            
        }
        
        userTransaction.commit();
        userTransaction = transactionService.getUserTransaction();
        userTransaction.begin();
    }
    
    private void createRecord(NodeRef recordFolder, int index) throws Exception
    {
        String name = genName("record-", index, ".txt");
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(2);
        properties.put(ContentModel.PROP_NAME, name);
        NodeRef r = nodeService.createNode(
                                       recordFolder, 
                                       ContentModel.ASSOC_CONTAINS, 
                                       QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                                       ContentModel.TYPE_CONTENT,
                                       properties).getChildRef();
        ContentWriter cw = contentService.getWriter(r, ContentModel.PROP_CONTENT, true);
        cw.setEncoding("UTF-8");
        cw.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        cw.putContent("This is my records content");
        
        System.out.println("Created record '" + name);
    }
    
    private String genName(String prefix, int index)
    {
        return genName(prefix, index, "");
    }
    
    private String genName(String prefix, int index, String postfix)
    {
        StringBuffer buff = new StringBuffer(120);
        buff.append(prefix)
            .append(index)
            .append(postfix);
        return buff.toString();
    }
}
