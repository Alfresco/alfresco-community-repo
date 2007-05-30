/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.avm;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.config.JNDIConstants;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.sandbox.SandboxConstants;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.DNSNameMangler;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean that is responsible for locating expired content and routing
 * it for review to the most relevant user.
 * 
 * @author gavinc
 */
public class AVMExpiredContentProcessor
{
    // defaults in case these properties are not configured in Spring
    protected String adminUserName = "admin";
    protected String workflowName = "jbpm$wcmwf:changerequest";
    
    protected List<String> workflowStores;
    protected Map<String, Map<String, List<String>>> expiredContent;
    protected AVMService avmService;
    protected AVMSyncService avmSyncService;
    protected NodeService nodeService;
    protected WorkflowService workflowService;
    protected PersonService personService;
    protected PermissionService permissionService;
    protected TransactionService transactionService;
    protected VirtServerRegistry virtServerRegistry;
    protected SearchService searchService;
    
    private static Log logger = LogFactory.getLog(AVMExpiredContentProcessor.class);

    private static final String STORE_SEPARATOR                = "--";
    private final static Pattern STORE_RELATIVE_PATH_PATTERN   = Pattern.compile("[^:]+:(.+)");
    
    public AVMExpiredContentProcessor()
    {
    }
    
    public void setAdminUserName(String adminUserName)
    {
        this.adminUserName = adminUserName;
    }

    public void setWorkflowName(String workflowName)
    {
        this.workflowName = workflowName;
    }

    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }
    
    public void setAvmSyncService(AVMSyncService avmSyncService)
    {
        this.avmSyncService = avmSyncService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setVirtServerRegistry(VirtServerRegistry virtServerRegistry)
    {
        this.virtServerRegistry = virtServerRegistry;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Executes the expired content processor.
     * The work is performed within a transaction running as the system user.
     */
    public void execute()
    {
        // setup a wrapper object to run the processor within a transaction.
        AuthenticationUtil.RunAsWork<String> authorisedWork = new AuthenticationUtil.RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                TransactionWork<String> expiredContentWork = new TransactionWork<String>()
                {
                    public String doWork() throws Exception
                    {
                         processExpiredContent();
                         return null;
                     }
                 };

                 return TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, expiredContentWork);
             }
         };
         
         // perform the work as the system user
         AuthenticationUtil.runAs(authorisedWork, this.adminUserName);

         // now we know everything worked ok, let the virtualisation server
         // know about all the new workflow sandboxes created (just the main stores)
         for (String path : this.workflowStores)
         {
            this.virtServerRegistry.updateAllWebapps(-1, path, true);
         }
    }
    
    /**
     * Entry point.
     */
    private void processExpiredContent()
    {
        // create the maps to hold the expired content for each user in each web project
        this.expiredContent = new HashMap<String, Map<String, List<String>>>(8);
        this.workflowStores = new ArrayList<String>(4);
        
        // iterate through all AVM stores and focus only on staging main stores
        List<AVMStoreDescriptor> stores = avmService.getStores();
        if (logger.isDebugEnabled())
           logger.debug("Checking " + stores.size() + " AVM stores...");
        
        for (AVMStoreDescriptor storeDesc : stores)
        {
            String storeName = storeDesc.getName();
            PropertyValue val = avmService.getStoreProperty(storeName, SandboxConstants.PROP_SANDBOX_STAGING_MAIN);
           
            if (val != null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Searching store '" + storeName + "' for expired content...");
                
                // find ant nodes with an expiration *date* of today or before
                Calendar cal = Calendar.getInstance();
                StringBuilder query = new StringBuilder("@wca\\:expirationDate:[0001\\-01\\-01T00:00:00 TO ");
                query.append(cal.get(Calendar.YEAR));
                query.append("\\-");
                query.append((cal.get(Calendar.MONTH)+1));
                query.append("\\-");
                query.append(cal.get(Calendar.DAY_OF_MONTH));
                query.append("T00:00:00]");
                
                // do the query
                StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_AVM, storeName);
                ResultSet results = this.searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, 
                         query.toString());

                if (logger.isDebugEnabled())
                      logger.debug("Found " + results.length() + " potential expired item(s) in store '" + storeName + "'");
                
                if (results.length() > 0)
                {
                   for (NodeRef resultNode : results.getNodeRefs())
                   {
                      // get the AVMNodeDescriptor object for each node found
                      Pair<Integer, String> path = AVMNodeConverter.ToAVMVersionPath(resultNode);
                      AVMNodeDescriptor node = this.avmService.lookup(path.getFirst(), path.getSecond());
                      
                      // process the node to see whether the date and time has passed
                      processNode(storeName, node);
                   }
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Skipping store '" + storeName + "' as it is not a main staging store");
            }
        }
        
        // show all the expired content if debug is on
        if (logger.isDebugEnabled())
           logger.debug("Expired content to action:\n" + this.expiredContent);
        
        // iterate through each store that has expired content, then iterate through
        // each user that has expired content in that store. For each user start
        // a workflow assigned to them to review the expired content.
        for (String storeName: this.expiredContent.keySet())
        {
           // get the name of the store and create the workflow title
           // using it's name
           NodeRef webProjectNodeRef = (NodeRef)avmService.getStoreProperty(storeName, 
               SandboxConstants.PROP_WEB_PROJECT_NODE_REF).getValue(DataTypeDefinition.NODE_REF);
           String webProjectName = (String)this.nodeService.getProperty(webProjectNodeRef, 
                    ContentModel.PROP_NAME);
           String pattern = I18NUtil.getMessage("expiredcontent.workflow.title");
           String workflowTitle = MessageFormat.format(pattern, new Object[] {webProjectName});
           
           Map<String, List<String>> users = this.expiredContent.get(storeName);
           for (String userName: users.keySet())
           {
              List<String> expiredContent = users.get(userName);
              startWorkflow(userName, storeName, expiredContent, workflowTitle);
           }
        }
    }
    
    /**
     * Processes the given node.
     * <p>
     * This method is called if the node has been identified as being expired, 
     * the date and time is checked to make sure it has actually passed i.e. the
     * date maybe todat but the time set to later in the day. If the item is
     * indeed expired it's added to the expired list and the date reset.
     * </p>
     * 
     * @param storeName The name of the store the folder belongs to
     * @param node The node to examine
     */
    private void processNode(String storeName, AVMNodeDescriptor node)
    {
        // check supplied node is a file
        if (node.isFile())
        {
            // check for existence of expires aspect
            String nodePath = node.getPath();
            PropertyValue expirationDateProp = this.avmService.getNodeProperty(-1, nodePath, 
                     WCMAppModel.PROP_EXPIRATIONDATE);
                
             if (logger.isDebugEnabled())
                 logger.debug("Examining expiration date for '" + nodePath + "': " + 
                          expirationDateProp.getStringValue());
             
             if (expirationDateProp != null)
             {
                 Date now = new Date();
                 Date expirationDate = (Date)expirationDateProp.getValue(DataTypeDefinition.DATETIME);
                
                 if (expirationDate != null && expirationDate.before(now))
                 {
                     // get the map of expired content for the store
                     Map<String, List<String>> storeExpiredContent = this.expiredContent.get(storeName);
                     if (storeExpiredContent == null)
                     {
                         storeExpiredContent = new HashMap<String, List<String>>(4);
                         this.expiredContent.put(storeName, storeExpiredContent);
                     }
                   
                     // get the list of expired content for the last modifier of the node
                     String modifier = node.getLastModifier();
                     List<String> userExpiredContent = storeExpiredContent.get(modifier);
                     if (userExpiredContent == null)
                     {
                         userExpiredContent = new ArrayList<String>(4);
                         storeExpiredContent.put(modifier, userExpiredContent);
                     }
                   
                     // add the content to the user's list for the current store
                     userExpiredContent.add(nodePath);
                   
                     if (logger.isDebugEnabled())
                         logger.debug("Added " + nodePath + " to " + modifier + "'s list of expired content");
                   
                     // reset the expiration date
                     this.avmService.setNodeProperty(nodePath, WCMAppModel.PROP_EXPIRATIONDATE, 
                              new PropertyValue(DataTypeDefinition.DATETIME, null));
                   
                     if (logger.isDebugEnabled())
                         logger.debug("Reset expiration date for: " + nodePath);
                }
            }
        }
    }
    
    /**
     * Starts a workflow for the given user prompting them to review the list of given 
     * expired content in the given store.
     * 
     * @param userName The user the expired content should be sent to
     * @param storeName The store the expired content is in
     * @param expiredContent List of paths to expired content
     * @param workflowTitle The title to apply to the workflow
     */
    private void startWorkflow(String userName, String storeName, List<String> expiredContent,
             String workflowTitle)
    {
        // find the 'Change Request' workflow
        WorkflowDefinition wfDef = workflowService.getDefinitionByName(this.workflowName);
        WorkflowPath path = this.workflowService.startWorkflow(wfDef.id, null);
        if (path != null)
        {
            // extract the start task
            List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1)
            {
                WorkflowTask startTask = tasks.get(0);
      
                if (startTask.state == WorkflowTaskState.IN_PROGRESS)
                {
                    // determine the user to assign the workflow to
                    String userStore = storeName + STORE_SEPARATOR + userName;
                    if (this.avmService.getStore(userStore) == null)
                    {
                        // use the creator of the store (the web project creator) to assign the
                        // workflow to
                        String storeCreator = this.avmService.getStore(storeName).getCreator();
                
                        if (logger.isDebugEnabled())
                            logger.debug("'" + userName + "' is no longer assigned to web project. Using '" + 
                                     storeCreator + "' as they created store '" + storeName + "'");
                         
                        userName = storeCreator;
                        userStore = storeName + STORE_SEPARATOR + userName;
                    }
             
                    // lookup the NodeRef for the user
                    NodeRef assignee = this.personService.getPerson(userName);
                    
                    // create a workflow store layered over the users store
                    String workflowStoreName = createUserWorkflowSandbox(storeName, userStore);

                    // create a workflow package with all the expired items
                    NodeRef workflowPackage = setupWorkflowPackage(workflowStoreName, expiredContent);
                    
                    // create the workflow parameters map
                    Map<QName, Serializable> params = new HashMap<QName, Serializable>(5);
                    params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
                    params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
                    params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowTitle);
                    
                    // transition the workflow to send it to the users inbox
                    this.workflowService.updateTask(startTask.id, params, null, null);
                    this.workflowService.endTask(startTask.id, null);
                    
                    // remember the root path of the workflow sandbox so we can inform
                    // the virtualisation server later
                    this.workflowStores.add(workflowStoreName + ":/" + 
                             JNDIConstants.DIR_DEFAULT_WWW + "/" + 
                             JNDIConstants.DIR_DEFAULT_APPBASE + "/ROOT");
                    
                    if (logger.isDebugEnabled())
                       logger.debug("Started '" + this.workflowName + "' workflow for user '" +
                                userName + "' in store '" + storeName + "'");
                }
            }
        }
    }
        
    /**
     * Creates a workflow sandbox for the given user store. This will create a
     * workflow sandbox layered over the user's main store.
     * 
     * @param stagingStore The name of the staging store the user sandbox is layered over
     * @param userStore The name of the user store to create the workflow for
     * @return The store name of the main store in the workflow sandbox
     */
    private String createUserWorkflowSandbox(String stagingStore, String userStore)
    {
        // create the workflow 'main' store
        String packageName = "workflow-" + GUID.generate();
        String workflowStoreName = userStore + STORE_SEPARATOR + packageName;
      
        this.avmService.createStore(workflowStoreName);
        
        if (logger.isDebugEnabled())
            logger.debug("Created user workflow sandbox store: " + workflowStoreName);
         
        // create a layered directory pointing to 'www' in the users store
        this.avmService.createLayeredDirectory(
                 userStore + ":/" + JNDIConstants.DIR_DEFAULT_WWW, 
                 workflowStoreName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
         
        // tag the store with the store type
        this.avmService.setStoreProperty(workflowStoreName, 
                 SandboxConstants.PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN,
                 new PropertyValue(DataTypeDefinition.TEXT, null));
         
        // tag the store with the name of the author's store this one is layered over
        this.avmService.setStoreProperty(workflowStoreName, 
                 SandboxConstants.PROP_AUTHOR_NAME,
                 new PropertyValue(DataTypeDefinition.TEXT, userStore));
         
        // tag the store, oddly enough, with its own store name for querying.
        this.avmService.setStoreProperty(workflowStoreName,
                 QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + workflowStoreName),
                 new PropertyValue(DataTypeDefinition.TEXT, null));
         
        // tag the store with the DNS name property
        String path = workflowStoreName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + 
                 "/" + JNDIConstants.DIR_DEFAULT_APPBASE;
        // DNS name mangle the property name - can only contain value DNS characters!
        String dnsProp = SandboxConstants.PROP_DNS + DNSNameMangler.MakeDNSName(userStore, packageName);
        this.avmService.setStoreProperty(workflowStoreName, QName.createQName(null, dnsProp),
                 new PropertyValue(DataTypeDefinition.TEXT, path));
         
        // the main workflow store depends on the main user store (dist=1)
        String prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + userStore;
        this.avmService.setStoreProperty(workflowStoreName, QName.createQName(null, prop_key),
                 new PropertyValue(DataTypeDefinition.INT, 1));
        
        // The main workflow store depends on the main staging store (dist=2)
        prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + stagingStore;
        this.avmService.setStoreProperty(workflowStoreName, QName.createQName(null, prop_key),
                 new PropertyValue(DataTypeDefinition.INT, 2));
      
        // snapshot the store
        this.avmService.createSnapshot(workflowStoreName, null, null);
         
        // create the workflow 'preview' store
        String previewStoreName = workflowStoreName + STORE_SEPARATOR + "preview";
        this.avmService.createStore(previewStoreName);
      
        if (logger.isDebugEnabled())
            logger.debug("Created user workflow sandbox preview store: " + previewStoreName);
         
        // create a layered directory pointing to 'www' in the workflow 'main' store
        this.avmService.createLayeredDirectory(
                 workflowStoreName + ":/" + JNDIConstants.DIR_DEFAULT_WWW, 
                 previewStoreName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
         
        // tag the store with the store type
        this.avmService.setStoreProperty(previewStoreName, SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW,
                 new PropertyValue(DataTypeDefinition.TEXT, null));
      
        // tag the store with its own store name for querying.
        avmService.setStoreProperty(previewStoreName,
                 QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + previewStoreName),
                 new PropertyValue(DataTypeDefinition.TEXT, null));
         
        // tag the store with the DNS name property
        path = previewStoreName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + 
                 "/" + JNDIConstants.DIR_DEFAULT_APPBASE;
        // DNS name mangle the property name - can only contain value DNS characters!
        dnsProp = SandboxConstants.PROP_DNS + DNSNameMangler.MakeDNSName(userStore, packageName, "preview");
        this.avmService.setStoreProperty(previewStoreName, QName.createQName(null, dnsProp),
                 new PropertyValue(DataTypeDefinition.TEXT, path));

        // The preview worfkflow store depends on the main workflow store (dist=1)
        prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + workflowStoreName;
        this.avmService.setStoreProperty(previewStoreName, QName.createQName(null, prop_key),
                 new PropertyValue(DataTypeDefinition.INT, 1));

        // The preview workflow store depends on the main user store (dist=2)
        prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + userStore;
        this.avmService.setStoreProperty(previewStoreName, QName.createQName(null, prop_key),
                 new PropertyValue(DataTypeDefinition.INT, 2));
        
        // The preview workflow store depends on the main staging store (dist=3)
        prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + stagingStore;
        this.avmService.setStoreProperty(previewStoreName, QName.createQName(null, prop_key),
                 new PropertyValue(DataTypeDefinition.INT, 3));
      
        // snapshot the store
        this.avmService.createSnapshot(previewStoreName, null, null);
         
        // tag all related stores to indicate that they are part of a single sandbox
        QName sandboxIdProp = QName.createQName(SandboxConstants.PROP_SANDBOXID + GUID.generate());
        this.avmService.setStoreProperty(workflowStoreName, sandboxIdProp,
                 new PropertyValue(DataTypeDefinition.TEXT, null));
        this.avmService.setStoreProperty(previewStoreName, sandboxIdProp,
                 new PropertyValue(DataTypeDefinition.TEXT, null));
      
        // return the main workflow store name
        return workflowStoreName;
    }
    
    /**
     * Sets up a workflow package from the given main workflow store and applies
     * the list of paths as modified items within the main workflow store.
     * 
     * @param workflowStoreName The main workflow store to setup
     * @param expiredContent The expired content
     * @return The NodeRef representing the workflow package
     */
    private NodeRef setupWorkflowPackage(String workflowStoreName, List<String> expiredContent)
    {
        // create package paths (layered to user sandbox area as target)
        String packagesPath = workflowStoreName + ":/" + JNDIConstants.DIR_DEFAULT_WWW;
        
        for (final String srcPath : expiredContent)
        {
            final Matcher m = STORE_RELATIVE_PATH_PATTERN.matcher(srcPath);
            String relPath = m.matches() && m.group(1).length() != 0 ? m.group(1) : null;
            String pathInWorkflowStore = workflowStoreName + ":" + relPath;
            
            // call forceCopy to make sure the path appears modified in the workflow
            // sandbox, if the item is already modified or deleted this call has no
            // effect.
            this.avmService.forceCopy(pathInWorkflowStore);
        }
                    
        // convert package to workflow package
        AVMNodeDescriptor packageDesc = avmService.lookup(-1, packagesPath);
        NodeRef packageNodeRef = workflowService.createPackage(
                 AVMNodeConverter.ToNodeRef(-1, packageDesc.getPath()));
        this.nodeService.setProperty(packageNodeRef, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, true);

        // apply global permission to workflow package
        this.permissionService.setPermission(packageNodeRef, PermissionService.ALL_AUTHORITIES, 
                 PermissionService.ALL_PERMISSIONS, true);
      
        return packageNodeRef;
    }
}
