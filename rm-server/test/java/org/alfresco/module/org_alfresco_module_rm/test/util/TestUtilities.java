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
package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.script.BootstrapTestDataGet;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.springframework.context.ApplicationContext;

/**
 * This class is an initial placeholder for miscellaneous helper methods used in
 * the testing or test initialisation of the DOD5015 module.
 * 
 * @author neilm
 */
public class TestUtilities implements RecordsManagementModel
{
    public static NodeRef loadFilePlanData(ApplicationContext applicationContext)
    {
        return TestUtilities.loadFilePlanData(applicationContext, true, false);
    }
    
    public static final String TEST_FILE_PLAN_NAME = "testUtilities.filePlan";
    
    private static NodeRef getFilePlan(NodeService nodeService, NodeRef rootNode)
    {
    	NodeRef filePlan = null;
    	
        // Try and find a file plan hanging from the root node
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(rootNode, ContentModel.ASSOC_CHILDREN, TYPE_FILE_PLAN);
        if (assocs.size() != 0)
        {
            filePlan = assocs.get(0).getChildRef();            
        }     	
        
        return filePlan;
    }
    
    public static NodeRef loadFilePlanData(ApplicationContext applicationContext, boolean patchData, boolean alwaysLoad)
    {
        NodeService nodeService = (NodeService)applicationContext.getBean("NodeService"); 
        AuthorityService authorityService = (AuthorityService)applicationContext.getBean("AuthorityService");
        PermissionService permissionService = (PermissionService)applicationContext.getBean("PermissionService");       
        SearchService searchService = (SearchService)applicationContext.getBean("SearchService"); 
        ImporterService importerService = (ImporterService)applicationContext.getBean("importerComponent");
        RecordsManagementService recordsManagementService = (RecordsManagementService)applicationContext.getBean("RecordsManagementService");
        RecordsManagementActionService recordsManagementActionService = (RecordsManagementActionService)applicationContext.getBean("RecordsManagementActionService");
        RecordsManagementSecurityService recordsManagementSecurityService = (RecordsManagementSecurityService)applicationContext.getBean("RecordsManagementSecurityService");
        RecordsManagementSearchBehaviour recordsManagementSearchBehaviour = (RecordsManagementSearchBehaviour)applicationContext.getBean("recordsManagementSearchBehaviour");
        DispositionService dispositionService = (DispositionService)applicationContext.getBean("DispositionService");
        
        NodeRef filePlan = null;
        NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        
        if (alwaysLoad == false && getFilePlan(nodeService, rootNode) != null)
        {
            return filePlan;                           
        }
        
        // For now creating the filePlan beneath the
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, TEST_FILE_PLAN_NAME);
        filePlan = nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN,
                TYPE_FILE_PLAN,
                TYPE_FILE_PLAN,
                props).getChildRef();        

        // Do the data load into the the provided filePlan node reference
        // TODO ...
        InputStream is = TestUtilities.class.getClassLoader().getResourceAsStream(
                "alfresco/module/org_alfresco_module_rm/dod5015/DODExampleFilePlan.xml");
        //"alfresco/module/org_alfresco_module_rm/bootstrap/temp.xml");
        Assert.assertNotNull("The DODExampleFilePlan.xml import file could not be found", is);
        Reader viewReader = new InputStreamReader(is);
        Location location = new Location(filePlan);
        importerService.importView(viewReader, location, REPLACE_BINDING, null);
          
        if (patchData == true)
        {
            // Tempory call out to patch data after AMP
            BootstrapTestDataGet.patchLoadedData(searchService, nodeService, recordsManagementService, 
                    recordsManagementActionService, permissionService, 
                    authorityService, recordsManagementSecurityService,
                    recordsManagementSearchBehaviour,
                    dispositionService);
        }

        return filePlan;
    }
    
    public static NodeRef getRecordSeries(RecordsManagementService rmService, NodeService nodeService, String seriesName)
    {
    	NodeRef result = null;
        NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    	NodeRef filePlan = getFilePlan(nodeService, rootNode);
    	
    	if (filePlan != null) 
    	{
    		result = nodeService.getChildByName(filePlan, ContentModel.ASSOC_CONTAINS, seriesName);
    	}
    	return result;
    }
    
    public static NodeRef getRecordCategory(RecordsManagementService rmService, NodeService nodeService, String seriesName, String categoryName)
    {
    	NodeRef seriesNodeRef = getRecordSeries(rmService, nodeService, seriesName);
    	
    	NodeRef result = null;
    	if (seriesNodeRef != null)
    	{
    		result = nodeService.getChildByName(seriesNodeRef, ContentModel.ASSOC_CONTAINS, categoryName);
    	}
    	return result;
    }
    
    public static NodeRef getRecordFolder(RecordsManagementService rmService, NodeService nodeService, String seriesName, String categoryName, String folderName)
    {
    	NodeRef categoryNodeRef = getRecordCategory(rmService, nodeService, seriesName, categoryName);
    	
    	NodeRef result = null;
    	if (categoryNodeRef != null)
    	{
    		result = nodeService.getChildByName(categoryNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
    	}
    	return result;

    }

    
    // TODO .. do we need to redeclare this here ??
    private static ImporterBinding REPLACE_BINDING = new ImporterBinding()
    {

        public UUID_BINDING getUUIDBinding()
        {
            return UUID_BINDING.REPLACE_EXISTING;
        }

        public String getValue(String key)
        {
            return null;
        }

        public boolean allowReferenceWithinTransaction()
        {
            return false;
        }

        public QName[] getExcludedClasses()
        {
            return null;
        }

    };

    public static void declareRecord(NodeRef recordToDeclare, NodeService nodeService,
            RecordsManagementActionService rmActionService)
    {
        // Declare record
        Map<QName, Serializable> propValues = nodeService.getProperties(recordToDeclare);        
        propValues.put(RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());       
        List<String> smList = new ArrayList<String>(2);
//        smList.add(DOD5015Test.FOUO);
//        smList.add(DOD5015Test.NOFORN);
        propValues.put(RecordsManagementModel.PROP_SUPPLEMENTAL_MARKING_LIST, (Serializable)smList);        
        propValues.put(RecordsManagementModel.PROP_MEDIA_TYPE, "mediaTypeValue"); 
        propValues.put(RecordsManagementModel.PROP_FORMAT, "formatValue"); 
        propValues.put(RecordsManagementModel.PROP_DATE_RECEIVED, new Date());       
        propValues.put(RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        propValues.put(RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        propValues.put(ContentModel.PROP_TITLE, "titleValue");
        nodeService.setProperties(recordToDeclare, propValues);
        rmActionService.executeRecordsManagementAction(recordToDeclare, "declareRecord");        
    }
}
