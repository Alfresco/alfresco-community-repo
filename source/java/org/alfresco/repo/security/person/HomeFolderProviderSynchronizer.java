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
package org.alfresco.repo.security.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Called on startup to move (synchronise) home folders to the preferred
 * location defined by their {@link HomeFolderProvider2} or extend the
 * now depreciated {@link AbstractHomeFolderProvider}. Only users that
 * use a HomeFolderProvider2 that don't provide a shared home
 * folder (all user are given the same home folder) will be moved. This
 * allows existing home directories to be moved to reflect changes in
 * policy related to the location of home directories. Originally created
 * for ALF-7797 which related to the need to move large numbers of
 * existing home directories created via an LDAP import into a hierarchical
 * folder structure with fewer home folder in each.<p>
 * 
 * By default no action is taken unless the the global property 
 * {@code home_folder_provider_synchronizer.enabled=true}.<p>
 * 
 * The home folders for internal users (such as {@code admin} and {@code
 * guest}) that use {@code guestHomeFolderProvider} or {@code
 * bootstrapHomeFolderProvider} are not moved, nor are any users that use
 * {@link HomeFolderProviders} create shared home folders (all user are
 * given the same home folder).
 * 
 * It is also possible change the HomeFolderProvider used by all other
 * users by setting the global property
 * {@code home_folder_provider_synchronizer.override_provider=<providerBeanName>}.<p>
 * 
 * <b>Warning:</b> The LDAP synchronise process overwrites the home folder
 * provider property. This is not an issue as long as the root path of
 * the overwriting provider is the same as the overwritten provider or is
 * not an ancestor of any of the existing home folders. This is important
 * because the root directory value is used by this class to tidy up empty
 * 'parent' folders under the root when a home folders are moved elsewhere.
 * If you have any concerns that this may not be true, set the global
 * property {@code home_folder_provider_synchronizer.keep_empty_parents=true}
 * and tidy up any empty folders manually. Typically users created by the
 * LDAP sync process are all placed under the same root folder so there
 * will be no parent folders anyway.<p>
 * 
 * @author Alan Davis
 */
public class HomeFolderProviderSynchronizer extends AbstractLifecycleBean
{
    private static final Log logger = LogFactory.getLog(HomeFolderProviderSynchronizer.class);
    
    private static final String ENABLED_PROPERTY_NAME = "home_folder_provider_synchronizer.enabled";
    private static final String OVERRIDE_PROPERTY_NAME = "home_folder_provider_synchronizer.override_provider";
    private static final String KEEP_EMPTY_PARENTS_PROPERTY_NAME = "home_folder_provider_synchronizer.keep_empty_parents";
    
    private static final String GUEST_HOME_FOLDER_PROVIDER = "guestHomeFolderProvider";
    private static final String BOOTSTRAP_HOME_FOLDER_PROVIDER = "bootstrapHomeFolderProvider";

    private final Properties properties;
    private final TransactionService transactionService;
    private final AuthorityService authorityService;
    private final PersonService personService;
    private final FileFolderService fileFolderService;
    private final NodeService nodeService;
    private final HomeFolderManager homeFolderManager;
    private final TenantAdminService tenantAdminService;
    
    public HomeFolderProviderSynchronizer(Properties properties,
            TransactionService transactionService,
            AuthorityService authorityService, PersonService personService,
            FileFolderService fileFolderService, NodeService nodeService,
            HomeFolderManager homeFolderManager,
            TenantAdminService tenantAdminService)
    {
        this.properties = properties;
        this.transactionService = transactionService;
        this.authorityService = authorityService;
        this.personService = personService;
        this.fileFolderService = fileFolderService;
        this.nodeService = nodeService;
        this.homeFolderManager = homeFolderManager;
        this.tenantAdminService = tenantAdminService;
    }
    
    private boolean enabled()
    {
        return "true".equalsIgnoreCase(properties.getProperty(ENABLED_PROPERTY_NAME));
    }
    
    private String getOverrideHomeFolderProviderName()
    {
        return properties.getProperty(OVERRIDE_PROPERTY_NAME);
    }
    
    private boolean keepEmptyParents()
    {
        return "true".equalsIgnoreCase(properties.getProperty(KEEP_EMPTY_PARENTS_PROPERTY_NAME));
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // do nothing
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        if (enabled())
        {
            final String overrideProviderName = getOverrideHomeFolderProviderName();
            
            // Scan users in default and each Tenant
            String systemUserName = AuthenticationUtil.getSystemUserName();
            scanPeople(systemUserName, TenantService.DEFAULT_DOMAIN, overrideProviderName);
            
            if (tenantAdminService.isEnabled())
            {
                List<Tenant> tenants = tenantAdminService.getAllTenants();
                for (Tenant tenant : tenants)
                {
                    if (tenant.isEnabled())
                    {
                        systemUserName = tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain());
                        scanPeople(systemUserName, tenant.getTenantDomain(), overrideProviderName);
                    }
                }
           }
        }
    }

    /**
     * Scans all {@code person} people objects and checks their home folder is located according
     * to the person's home folder provider preferred default location.
     * @param systemUserName String the system user name with the tenant-specific ID attached.
     * @param tenantDomain String name of the tenant domain. Used to restrict the which people
     *        are processed.
     * @param overrideProvider String the bean name of a HomeFolderProvider to be used
     *        in place of the all home folders existing providers. If {@code null}
     *        the existing provider is used. 
     */
    private void scanPeople(final String systemUserName, String tenantDomain, final String overrideProvider)
    {
        /*
         * To avoid problems with existing home folders that are located in locations
         * that will be needed by 'parent' folders, we need a 4 phase process.
         * Consider the following user names and required structure. There would be a
         * problem with the username 'ab'.
         * 
         *     abc --> ab/abc
         *     def       /abd
         *     abd       /ab
         *     ab      de/def
         *     
         * 1. Record which parent folders are needed
         * 2. Move any home folders which overlap with parent folders to a temporary folder
         * 3. Create parent folder structure. Done in a single thread before the move of
         *    home folders to avoid race conditions
         * 4. Move home folders if required
         * 
         * Alternative approaches are possible, but the above has the advantage that
         * nodes are not moved if they are already in their preferred location.
         */
        
        // Using authorities rather than Person objects as they are much lighter
        final Set<String> authorities = getAllAuthoritiesInTxn(systemUserName);
        final ParentFolderStructure parentFolderStructure = new ParentFolderStructure();
        final Map<NodeRef,String> tmpFolders = new HashMap<NodeRef,String>();
        
        // Define the phases
        final String createParentFoldersPhaseName = "createParentFolders";
        RunAsWorker[] workers = new RunAsWorker[]
        {
            new RunAsWorker(systemUserName, "calculateParentFolderStructure")
            {
                @Override
                public void doWork(NodeRef person) throws Exception
                {
                    calculateParentFolderStructure(
                            parentFolderStructure, person, overrideProvider);
                }
            },
                
            new RunAsWorker(systemUserName, "moveHomeFolderThatClashesWithParentFolderStructure")
            {
                @Override
                public void doWork(NodeRef person) throws Exception
                {
                    moveHomeFolderThatClashesWithParentFolderStructure(
                            parentFolderStructure, tmpFolders, person, overrideProvider);
                }
            },
                
            new RunAsWorker(systemUserName, createParentFoldersPhaseName)
            {
                @Override
                public void doWork(NodeRef person) throws Exception
                {
                    createParentFolders(person, overrideProvider);
                }
            },
                
            new RunAsWorker(systemUserName, "moveHomeFolderIfRequired")
            {
                @Override
                public void doWork(NodeRef person) throws Exception
                {
                    moveHomeFolderIfRequired(person, overrideProvider);
                }
            }
        };
        
        // Run the phases
        for (RunAsWorker worker: workers)
        {
            String name = worker.getName();
            if (logger.isDebugEnabled())
            {
                logger.debug("  -- "+
                        (TenantService.DEFAULT_DOMAIN.equals(tenantDomain)? "" : tenantDomain+" ")+
                        name+" --");
            }
            
            int threadCount = (name.equals(createParentFoldersPhaseName)) ? 1 : 2;
            int peoplePerTransaction = 20;
            
            // Use 2 threads, 20 person objects per transaction. Log every 100 entries.
            BatchProcessor<NodeRef> processor = new BatchProcessor<NodeRef>(
                    "HomeFolderProviderSynchronizer",
                    transactionService.getRetryingTransactionHelper(),
                    new WorkProvider(authorities),
                    threadCount, peoplePerTransaction,
                    null,
                    logger, 100);
            processor.process(worker, true);
            if (processor.getTotalErrors() > 0)
            {
                logger.debug("  -- Give up after error --");
                break;
            }
        }
    }

    // Can only use authorityService.getAllAuthorities(...) in a transaction.
    private Set<String> getAllAuthoritiesInTxn(final String systemUserName)
    {
        return AuthenticationUtil.runAs(new RunAsWork<Set<String>>()
        {
            public Set<String> doWork() throws Exception
            {
                RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                RetryingTransactionCallback<Set<String>> restoreCallback =
                    new RetryingTransactionCallback<Set<String>>()
                {
                    public Set<String> execute() throws Exception
                    {
                        return authorityService.getAllAuthorities(AuthorityType.USER);
                    }
                };
                return txnHelper.doInTransaction(restoreCallback, false, true);
            }
        }, systemUserName);
    }
    
    /**
     * Work out the preferred parent folder structure so we will be able to work out if any
     * existing home folders clash.
     */
    private ParentFolderStructure calculateParentFolderStructure(
            final ParentFolderStructure parentFolderStructure,
            NodeRef person, String overrideProviderName)
    {
        new HomeFolderHandler(person, overrideProviderName)
        {
            @Override
            protected void handleNotInPreferredLocation()
            {
                recordParentFolder();
            }

            @Override
            protected void handleInPreferredLocation()
            {
                recordParentFolder();
            }

            private void recordParentFolder()
            {
                parentFolderStructure.recordParentFolder(root, preferredPath);
            }
        }.doWork();
        
        return parentFolderStructure;
    }
    
    /**
     * Move any home folders (to a temporary folder) that clash with the
     * new parent folder structure.
     */
    private void moveHomeFolderThatClashesWithParentFolderStructure(
            final ParentFolderStructure parentFolderStructure,
            final Map<NodeRef,String> tmpFolders,
            NodeRef person, String overrideProviderName)
    {
        new HomeFolderHandler(person, overrideProviderName)
        {
            @Override
            protected void handleNotInPreferredLocation()
            {
                moveToTmpIfClash();
            }

            @Override
            protected void handleInPreferredLocation()
            {
                moveToTmpIfClash();
            }
            
            private void moveToTmpIfClash()
            {
                if (parentFolderStructure.clash(root, actualPath))
                {
                    String tmpFolder = getTmpFolderName(root);
                    preferredPath = new ArrayList<String>();
                    preferredPath.add(tmpFolder);
                    preferredPath.addAll(actualPath);
                    
                    // - providerName parameter is set to null as we don't want the
                    //   "homeFolderProvider" reset
                    moveHomeFolder(person, homeFolder, root, preferredPath, originalRoot,
                            null, originalProviderName, actualPath);
                }
            }

            private String getTmpFolderName(NodeRef root)
            {
                synchronized(tmpFolders)
                {
                    String tmpFolder = tmpFolders.get(root);
                    if (tmpFolder == null)
                    {
                        tmpFolder = createTmpFolderName(root);
                        tmpFolders.put(root, tmpFolder);
                    }
                    return tmpFolder;
                }
            }

            private String createTmpFolderName(NodeRef root)
            {
                // Try a few times but then give up.
                for (int i = 1; i <= 100; i++)
                {
                    String tmpFolderName = "Temporary"+i;
                    if (fileFolderService.searchSimple(root, tmpFolderName) == null)
                    {
                        fileFolderService.create(root, tmpFolderName, ContentModel.TYPE_FOLDER);
                        return tmpFolderName;
                    }
                }
                throw new PersonException("Unable to create a temporty " +
                	"folder into which home folders could be moved.");
            }
        }.doWork();
    }
    
    /**
     * Creates the new home folder structure, before we move home folders so that
     * we don't have race conditions that result in unnecessary retries.
     * @param parentFolderStructure
     */
    private void createParentFolders(NodeRef person, String overrideProviderName)
    {
        // We could short cut this process and build all the home folder from the
        // ParentFolderStructure in the calling method, but that would complicate
        // the code a little more and might result in transaction size problems.
        // For now lets loop through all the person objects.
        
        new HomeFolderHandler(person, overrideProviderName)
        {
            @Override
            protected void handleNotInPreferredLocation()
            {
                createNewParentIfRequired(root, preferredPath);
            }

            @Override
            protected void handleInPreferredLocation()
            {
                // do nothing
            }
       }.doWork();
    }

    /**
     * If the home folder has been created but is not in its preferred location, the home folder
     * is moved. Empty parent folder's under the old root are only removed if the old root is
     * known and {@code home_folder_provider_synchronizer.keep_empty_parents=true} has not been
     * set.
     * @param person Person to be checked.
     * @param overrideProviderName String name of a provider to use in place of
     *        the one currently used. Ignored if {@code null}.
     */
    private void moveHomeFolderIfRequired(NodeRef person, String overrideProviderName)
    {
        new HomeFolderHandler(person, overrideProviderName)
        {
            @Override
            protected void handleNotInPreferredLocation()
            {
                moveHomeFolder(person, homeFolder, root, preferredPath, originalRoot,
                        providerName, originalProviderName, actualPath);
            }
            
            @Override
            protected void handleInPreferredLocation()
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("  "+toPath(actualPath)+" is already in preferred location.");
                }
            }
            
            @Override
            protected void handleSharedHomeProvider()
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("  "+userName+" "+providerName+" creates shared home folders - These are not moved.");
                }
            }

            
            @Override
            protected void handleOriginalSharedHomeProvider()
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("  "+userName+" Original "+originalProviderName+" creates shared home folders - These are not moved.");
                }
            }

            @Override
            protected void handleNotAHomeFolderProvider2()
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("  "+userName+" "+providerName+" for is not a HomeFolderProvider2.");
                }
            }

            @Override
            protected void handleSpecialHomeFolderProvider()
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("  "+userName+" Original "+originalProviderName+" is an internal type - These are not moved.");
                }
            }

            @Override
            protected void handleHomeFolderNotSet()
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("  "+userName+" Home folder is not set - ignored");
                }
            }
       }.doWork();
    }
    
    /**
     * @return a String for debug a folder list.
     */
    private String toPath(List<String> folders)
    {
        StringBuilder sb = new StringBuilder("");
        if (folders != null)
        {
            for (String folder : folders)
            {
                if (sb.length() > 0)
                {
                    sb.append('/');
                }
                sb.append(folder);
            }
        }
        return sb.toString();
    }
    
    private String toPath(NodeRef root, NodeRef leaf)
    {
        StringBuilder sb = new StringBuilder("");
        List<String> path = getRelativePath(root, leaf);
        if (path != null)
        {
            for (String folder : path)
            {
                if (sb.length() > 0)
                {
                    sb.append('/');
                }
                sb.append(folder);
            }
        }
        return sb.toString();
    }

    /**
     * @return the relative 'path' (a list of folder names) of the {@code homeFolder}
     * from the {@code root} or {@code null} if the homeFolder is not under the root
     * or is the root.
     */
    private List<String> getRelativePath(NodeRef root, NodeRef homeFolder)
    {
        if (root == null || homeFolder == null)
        {
            return null;
        }
        
        Path rootPath = nodeService.getPath(root);
        Path homeFolderPath = nodeService.getPath(homeFolder);
        int rootSize = rootPath.size();
        int homeFolderSize = homeFolderPath.size();
        if (rootSize >= homeFolderSize)
        {
            return null;
        }
        
        // Check homeFolder is under root
        for (int i=0; i < rootSize; i++)
        {
            if (!rootPath.get(i).equals(homeFolderPath.get(i)))
            {
                return null;
            }
        }
        
        // Build up path of sub folders
        List<String> path = new ArrayList<String>();
        for (int i = rootSize; i < homeFolderSize; i++)
        {
            Path.Element element = homeFolderPath.get(i);
            if (!(element instanceof Path.ChildAssocElement))
            {
                return null;
            }
            QName folderQName = ((Path.ChildAssocElement) element).getRef().getQName();
            path.add(folderQName.getLocalName());
        }
        return path;
    }

    /**
     * Move an existing home folder from one location to another,
     * removing empty parent folders and reseting homeFolder and
     * homeFolderProvider properties.
     */
    private void moveHomeFolder(NodeRef person, NodeRef homeFolder, NodeRef root,
            List<String> preferredPath, NodeRef oldRoot, String providerName,
            String originalProviderName, List<String> actualPath)
    {
        try
        {       
            // Create the new parent folder (if required)
            // Code still here for completeness, but should be okay
            // as the temporary folder will have been created and any 
            // parent folders should have been created.
            NodeRef newParent = createNewParentIfRequired(root, preferredPath);

            String homeFolderName = preferredPath.get(preferredPath.size() - 1);
            
            // Throw our own FileExistsException before we get one that
            // marks the transaction for rollback, as there is no point
            // trying again.
            if (nodeService.getChildByName(newParent, ContentModel.ASSOC_CONTAINS,
                    homeFolderName) != null)
            {
                throw new FileExistsException(newParent, homeFolderName);
            }
       
            // Get the old parent before we move anything.
            NodeRef oldParent = nodeService.getPrimaryParent(homeFolder) .getParentRef();

            // Perform the move
            homeFolder = fileFolderService.move(homeFolder, newParent,
                    homeFolderName).getNodeRef();

            // Reset the homeFolder property
            nodeService.setProperty(person, ContentModel.PROP_HOMEFOLDER, homeFolder);

            // Change provider name
            if (providerName != null && !providerName.equals(originalProviderName))
            {
                nodeService.setProperty(person,
                        ContentModel.PROP_HOME_FOLDER_PROVIDER, providerName);
            }
                
            // Log action
            if (logger.isDebugEnabled())
            {
               logger.debug("  mv "+toPath(actualPath)+
                        " "+ toPath(preferredPath)+
                        ((providerName != null && !providerName.equals(originalProviderName))
                        ? "    AND reset provider to "+providerName
                        : "") + ".");
            }

            // Tidy up
            removeEmptyParentFolders(oldParent, oldRoot);
        }
        catch (FileExistsException e)
        {
            String message = "mv "+toPath(actualPath)+" "+toPath(preferredPath)+
                    " failed as the target already existed.";
            logger.error("  "+message);
            throw new PersonException(message);
        }
        catch (FileNotFoundException e)
        {
            // This should not happen unless there is a coding error
            String message = "mv "+toPath(actualPath)+" "+toPath(preferredPath)+
                    " failed as source did not exist.";
            logger.error("  "+message);
            throw new PersonException(message);
        }
    }
    
    private NodeRef createNewParentIfRequired(NodeRef root, List<String> homeFolderPath)
    {
        NodeRef parent = root;
        int len = homeFolderPath.size() - 1;
        for (int i = 0; i < len; i++)
        {
            String pathElement = homeFolderPath.get(i);
            NodeRef nodeRef = nodeService.getChildByName(parent,
                    ContentModel.ASSOC_CONTAINS, pathElement);
            if (nodeRef == null)
            {
                parent = fileFolderService.create(parent, pathElement,
                        ContentModel.TYPE_FOLDER).getNodeRef();
            }
            else
            {
                // Throw our own FileExistsException before we get an 
                // exception when we cannot create a sub-folder under
                // a non folder that marks the transaction rollback, as
                // there is no point trying again.
                if (!fileFolderService.getFileInfo(nodeRef).isFolder())
                {
                    throw new FileExistsException(parent, null);
                }

                parent = nodeRef;
            }
        }
        return parent;
    }

    /**
     * Removes the parent folder if it is empty and its parents up to but not
     * including the root.
     */
    private void removeEmptyParentFolders(NodeRef parent, NodeRef root)
    {
        // Parent folders we have created don't have an owner, were as
        // home folders do, hence the 3rd test (just in case) as we really
        // don't want to delete empty home folders.
        if (root != null &&
            !keepEmptyParents() &&
            nodeService.getProperty(parent, ContentModel.PROP_OWNER) == null)
        {   
            // Do nothing if root is not an ancestor of parent.
            NodeRef nodeRef = parent;
            while (!root.equals(nodeRef))
            {
                if (nodeRef == null)
                {
                    return;
                }
                nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
            }
           
            // Remove any empty nodes.
            while (!root.equals(parent))
            {
                nodeRef = parent;
                parent = nodeService.getPrimaryParent(parent).getParentRef();

                if (!nodeService.getChildAssocs(nodeRef).isEmpty())
                {
                    return;
                }
                if (logger.isDebugEnabled())
                {
                    logger.debug("    rm "+toPath(root, nodeRef));
                }
                nodeService.deleteNode(nodeRef);
            }
        }
    }
    
    // BatchProcessWorkProvider returns batches of 100 person objects from lightweight authorities.
    private class WorkProvider implements BatchProcessWorkProvider<NodeRef>
    {
        private static final int BATCH_SIZE = 100;
        
        private final VmShutdownListener vmShutdownLister = new VmShutdownListener("getHomeFolderProviderSynchronizerWorkProvider");
        private final Iterator<String> iterator;
        private final int size;
        
        public WorkProvider(Set<String> authorities)
        {
            iterator = authorities.iterator();
            size = authorities.size();
        }

        @Override
        public synchronized int getTotalEstimatedWorkSize()
        {
            return size;
        }

        @Override
        public synchronized Collection<NodeRef> getNextWork()
        {
            if (vmShutdownLister.isVmShuttingDown())
            {
                return Collections.emptyList();
            }

            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            RetryingTransactionCallback<Collection<NodeRef>> restoreCallback = new RetryingTransactionCallback<Collection<NodeRef>>()
            {
                public Collection<NodeRef> execute() throws Exception
                {
                    Collection<NodeRef> results = new ArrayList<NodeRef>(BATCH_SIZE);
                    while (results.size() < BATCH_SIZE && iterator.hasNext())
                    {
                        String userName = iterator.next();
                        try
                        {
                            NodeRef person = personService.getPerson(userName, false);
                            results.add(person);
                        }
                        catch (NoSuchPersonException e)
                        {
                            if (logger.isTraceEnabled())
                            {
                                logger.trace("The user "+userName+" no longer exists - ignored.");
                            }
                        }
                    }
                    return results;
                }
            };
            return txnHelper.doInTransaction(restoreCallback, false, true);
        }
    }
    
    // BatchProcessWorker that runs work as another user.
    private abstract class RunAsWorker extends BatchProcessWorkerAdaptor<NodeRef>
    {
        final String userName;
        final String name;
        
        public RunAsWorker(String userName, String name)
        {
            this.userName = userName;
            this.name = name;
        }
        
        public void process(final NodeRef person) throws Throwable
        {
            RunAsWork<Object> runAsWork = new RunAsWork<Object>()
            {
                @Override
                public Object doWork() throws Exception
                {
                    RunAsWorker.this.doWork(person);
                    return null;
                }
            };
            AuthenticationUtil.runAs(runAsWork, userName);
        }
        
        public abstract void doWork(NodeRef person) throws Exception;
        
        public String getName()
        {
            return name;
        }
    };

    // Obtains home folder provider and path information with call backs.
    private abstract class HomeFolderHandler
    {
        protected final NodeRef person;
        protected final String overrideProviderName;
        
        protected NodeRef homeFolder;
        protected String userName;
        protected String originalProviderName;
        protected String providerName;
        protected HomeFolderProvider2 provider;
        protected NodeRef root;
        protected List<String> preferredPath;
        protected List<String> actualPath;
        protected NodeRef originalRoot;
        
        public HomeFolderHandler(NodeRef person, String overrideProviderName)
        {
            this.person = person;
            this.overrideProviderName = overrideProviderName;
        }
        
        public void doWork()
        {
            homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class,
                    nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
            userName = DefaultTypeConverter.INSTANCE.convert(
                    String.class, nodeService.getProperty(person, ContentModel.PROP_USERNAME));
        
            if (homeFolder != null)
            {
                originalProviderName = DefaultTypeConverter.INSTANCE.convert(String.class, 
                       nodeService.getProperty(person, ContentModel.PROP_HOME_FOLDER_PROVIDER));
                if (!BOOTSTRAP_HOME_FOLDER_PROVIDER.equals(originalProviderName) &&
                    !GUEST_HOME_FOLDER_PROVIDER.equals(originalProviderName))
                {
                    providerName = overrideProviderName != null
                        ? overrideProviderName
                        : originalProviderName;
                    provider = homeFolderManager.getHomeFolderProvider2(providerName);
            
                    if (provider != null)
                    {
                        root = homeFolderManager.getRootPathNodeRef(provider);
                        preferredPath = provider.getHomeFolderPath(person);
                        
                        if (preferredPath == null || preferredPath.isEmpty())
                        {
                            handleSharedHomeProvider();
                        }
                        else
                        {
                            originalRoot = null;
                            HomeFolderProvider2 originalProvider = homeFolderManager.getHomeFolderProvider2(originalProviderName);
                            List<String> originalPreferredPath = null;
                            if (originalProvider != null)
                            {
                                originalRoot = homeFolderManager.getRootPathNodeRef(originalProvider);
                                originalPreferredPath = originalProvider.getHomeFolderPath(person);
                            }
                            
                            if (originalProvider != null &&
                                (originalPreferredPath == null || originalPreferredPath.isEmpty()))
                            {
                                handleOriginalSharedHomeProvider();
                            }
                            else
                            {
                                actualPath = getRelativePath(root, homeFolder);
                                
                                if (preferredPath.equals(actualPath))
                                {
                                    handleInPreferredLocation();
                                }
                                else
                                {
                                    handleNotInPreferredLocation();
                                }
                            }
                        }
                    }
                    else
                    {
                        handleNotAHomeFolderProvider2();
                    }
                }
                else
                {
                    handleSpecialHomeFolderProvider();
                }
            }
            else
            {
                handleHomeFolderNotSet();
            }
        }

        protected abstract void handleInPreferredLocation();

        protected abstract void handleNotInPreferredLocation();
        
        protected void handleSharedHomeProvider()
        {
        }
        
        protected void handleOriginalSharedHomeProvider()
        {
        }
        
        protected void handleNotAHomeFolderProvider2()
        { 
        }

        protected void handleSpecialHomeFolderProvider()
        {
        }

        protected void handleHomeFolderNotSet()
        { 
        }
    }
    
    // Gathers and checks parent folder paths.
    private class ParentFolderStructure
    {
        // Sets of parent folders within each root node 
        private Map<NodeRef, Set<List<String>>> folders = new HashMap<NodeRef, Set<List<String>>>();
        
        public void recordParentFolder(NodeRef root, List<String> path)
        {
            Set<List<String>> rootsFolders = getFolders(root);
            synchronized(rootsFolders)
            {
                // If parent is the root, all home folders clash
                int parentSize = path.size() - 1;
                if (parentSize == 0)
                {
                    // We could optimise the code a little by clearing
                    // all other entries and putting a contains(null)
                    // check just inside the synchronized(rootsFolders)
                    // but it might be useful to have a complete lit of
                    // folders.
                    rootsFolders.add(null);

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("  Recorded root as parent");
                    }
                }
				else
                {
                    while (parentSize-- > 0)
                    {
                        List<String> parentPath = new ArrayList<String>();
                        for (int j = 0; j <= parentSize; j++)
                        {
                            parentPath.add(path.get(j));
                        }

                        if (logger.isDebugEnabled()
                                && !rootsFolders.contains(parentPath))
                        {
                            logger.debug("  Recorded parent: "
                                    + toPath(parentPath));
                        }

                        rootsFolders.add(parentPath);
                    }
                }
            }
        }
        
        /**
         * @return {@code true} if the {@code path} is a parent folder
         *         or the parent folders includes the root itself. In
         *         the latter case all existing folders might clash
         *         so must be moved out of the way.
         */
        public boolean clash(NodeRef root, List<String> path)
        {
            Set<List<String>> rootsFolders = getFolders(root);
            synchronized(rootsFolders)
            {
                return rootsFolders.contains(path) ||
                       rootsFolders.contains(null);
            }
        }

        private Set<List<String>> getFolders(NodeRef root)
        {
            synchronized(folders)
            {
                Set<List<String>> rootsFolders = folders.get(root);
                if (rootsFolders == null)
                {
                    rootsFolders = new HashSet<List<String>>();
                    folders.put(root, rootsFolders);
                }
                return rootsFolders;
            }
        }
    }
}
