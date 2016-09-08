/**
 * 
 */
package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FreezeAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.context.ApplicationContext;

/**
 * @author Roy Wetherall
 */
public class CommonRMTestUtils implements RecordsManagementModel
{
	private DispositionService dispositionService;
	private NodeService nodeService;
	private ContentService contentService;
	private RecordsManagementActionService actionService;
	private ModelSecurityService modelSecurityService;
	private FilePlanRoleService filePlanRoleService;
	private CapabilityService capabilityService;
	
    /** test values */
    public static final String DEFAULT_DISPOSITION_AUTHORITY = "disposition authority";
    public static final String DEFAULT_DISPOSITION_INSTRUCTIONS = "disposition instructions";
    public static final String DEFAULT_DISPOSITION_DESCRIPTION = "disposition action description";
    public static final String DEFAULT_EVENT_NAME = "case_closed";
    public static final String PERIOD_NONE = "none|0";
    public static final String PERIOD_IMMEDIATELY = "immediately|0";
	
	public CommonRMTestUtils(ApplicationContext applicationContext)
	{
		dispositionService = (DispositionService)applicationContext.getBean("DispositionService");
		nodeService = (NodeService)applicationContext.getBean("NodeService");
		contentService = (ContentService)applicationContext.getBean("ContentService");
		actionService = (RecordsManagementActionService)applicationContext.getBean("RecordsManagementActionService");
		modelSecurityService = (ModelSecurityService)applicationContext.getBean("ModelSecurityService");
		filePlanRoleService = (FilePlanRoleService)applicationContext.getBean("FilePlanRoleService");
		capabilityService = (CapabilityService)applicationContext.getBean("CapabilityService");
	}
	
    /**
     * 
     * @param container
     * @return
     */
    public DispositionSchedule createBasicDispositionSchedule(NodeRef container)
    {
        return createBasicDispositionSchedule(container, DEFAULT_DISPOSITION_INSTRUCTIONS, DEFAULT_DISPOSITION_AUTHORITY, false, true);
    }
    
    /**
     * 
     * @param container
     * @param isRecordLevel
     * @param defaultDispositionActions
     * @return
     */
    public DispositionSchedule createBasicDispositionSchedule(
                                    NodeRef container, 
                                    String dispositionInstructions,
                                    String dispositionAuthority,
                                    boolean isRecordLevel, 
                                    boolean defaultDispositionActions)
    {
        Map<QName, Serializable> dsProps = new HashMap<QName, Serializable>(3);
        dsProps.put(PROP_DISPOSITION_AUTHORITY, dispositionAuthority);
        dsProps.put(PROP_DISPOSITION_INSTRUCTIONS, dispositionInstructions);
        dsProps.put(PROP_RECORD_LEVEL_DISPOSITION, isRecordLevel);
        DispositionSchedule dispositionSchedule = dispositionService.createDispositionSchedule(container, dsProps);                
        
        if (defaultDispositionActions == true)
        {
            Map<QName, Serializable> adParams = new HashMap<QName, Serializable>(3);
            adParams.put(PROP_DISPOSITION_ACTION_NAME, "cutoff");
            adParams.put(PROP_DISPOSITION_DESCRIPTION, DEFAULT_DISPOSITION_DESCRIPTION);
            
            List<String> events = new ArrayList<String>(1);
            events.add(DEFAULT_EVENT_NAME);
            adParams.put(PROP_DISPOSITION_EVENT, (Serializable)events);
            
            dispositionService.addDispositionActionDefinition(dispositionSchedule, adParams);
            
            adParams = new HashMap<QName, Serializable>(3);
            adParams.put(PROP_DISPOSITION_ACTION_NAME, "destroy");
            adParams.put(PROP_DISPOSITION_DESCRIPTION, DEFAULT_DISPOSITION_DESCRIPTION);
            adParams.put(PROP_DISPOSITION_PERIOD, "immediately|0");            
            
            dispositionService.addDispositionActionDefinition(dispositionSchedule, adParams);
        }
        
        return dispositionSchedule;
    }
    
    public NodeRef createRecord(NodeRef recordFolder, String name)
    {
        return createRecord(recordFolder, name, null, "Some test content");
    }
    
    public NodeRef createRecord(NodeRef recordFolder, String name, String title)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_TITLE, title);
        return createRecord(recordFolder, name, props, "Some test content");
    }
    
    public NodeRef createRecord(NodeRef recordFolder, String name, Map<QName, Serializable> properties, String content)
	{
    	// Create the document
	    if (properties == null)
	    {
	        properties = new HashMap<QName, Serializable>(1);
	    }
        if (properties.containsKey(ContentModel.PROP_NAME) == false)
        {
            properties.put(ContentModel.PROP_NAME, name);
        }
        NodeRef recordOne = nodeService.createNode(recordFolder, 
                                                        ContentModel.ASSOC_CONTAINS, 
                                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                                                        ContentModel.TYPE_CONTENT,
                                                        properties).getChildRef();       
        
        // Set the content
        ContentWriter writer = contentService.getWriter(recordOne, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(content);
        
        return recordOne;
	}   
      
    public void declareRecord(final NodeRef record)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                modelSecurityService.setEnabled(false);
                try
                {
                    // Declare record
                    nodeService.setProperty(record, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
                    nodeService.setProperty(record, RecordsManagementModel.PROP_MEDIA_TYPE, "mediaTypeValue"); 
                    nodeService.setProperty(record, RecordsManagementModel.PROP_FORMAT, "formatValue"); 
                    nodeService.setProperty(record, RecordsManagementModel.PROP_DATE_RECEIVED, new Date());
                    nodeService.setProperty(record, RecordsManagementModel.PROP_DATE_FILED, new Date());
                    nodeService.setProperty(record, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
                    nodeService.setProperty(record, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
                    nodeService.setProperty(record, ContentModel.PROP_TITLE, "titleValue");
                    actionService.executeRecordsManagementAction(record, "declareRecord");
                }
                finally
                {
                    modelSecurityService.setEnabled(true);
                }
                
                return null;
            }
            
        }, AuthenticationUtil.getAdminUserName());
        
	}
    
    public void closeFolder(final NodeRef recordFolder)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                modelSecurityService.setEnabled(false);
                try
                {
                    actionService.executeRecordsManagementAction(recordFolder, "closeRecordFolder");
                }
                finally
                {
                    modelSecurityService.setEnabled(true);
                }
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }
    
    public void freeze(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FreezeAction.PARAM_REASON, "Freeze reason.");
                actionService.executeRecordsManagementAction(nodeRef, "freeze", params);
                
                return null;
            }
            
        }, AuthenticationUtil.getSystemUserName());
    }
    
    public void unfreeze(final NodeRef nodeRef)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                actionService.executeRecordsManagementAction(nodeRef, "unfreeze");                
                return null;
            }
            
        }, AuthenticationUtil.getSystemUserName());
    }
    
    public Role createRole(NodeRef filePlan, String roleName, String ... capabilityNames)
    {
        Set<Capability> capabilities = new HashSet<Capability>(capabilityNames.length);
        for (String name : capabilityNames)
        {
            Capability capability = capabilityService.getCapability(name);
            if (capability == null)
            {
                throw new AlfrescoRuntimeException("capability " + name + " not found.");
            }
            capabilities.add(capability);
        }
        
        return filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);
    }
}
