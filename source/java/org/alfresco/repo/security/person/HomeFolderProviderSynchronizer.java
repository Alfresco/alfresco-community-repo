/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
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
    private static final Log batchLogger = LogFactory.getLog(HomeFolderProviderSynchronizer.class+".batch");
    
    private static final String GUEST_HOME_FOLDER_PROVIDER = "guestHomeFolderProvider";
    private static final String BOOTSTRAP_HOME_FOLDER_PROVIDER = "bootstrapHomeFolderProvider";

    private final TransactionService transactionService;
    private final AuthorityService authorityService;
    private final PersonService personService;
    private final FileFolderService fileFolderService;
    private final NodeService nodeService;
    private final PortableHomeFolderManager homeFolderManager;
    private final TenantAdminService tenantAdminService;

    private boolean enabled;
    private String overrideHomeFolderProviderName;
    private boolean keepEmptyParents;
    
    public HomeFolderProviderSynchronizer(
            TransactionService transactionService,
            AuthorityService authorityService, PersonService personService,
            FileFolderService fileFolderService, NodeService nodeService,
            PortableHomeFolderManager homeFolderManager,
            TenantAdminService tenantAdminService)
    {
        this.transactionService = transactionService;
        this.authorityService = authorityService;
        this.personService = personService;
        this.fileFolderService = fileFolderService;
        this.nodeService = nodeService;
        this.homeFolderManager = homeFolderManager;
        this.tenantAdminService = tenantAdminService;
    }
    
    public void setEnabled(String enabled)
    {
        this.enabled = "true".equalsIgnoreCase(enabled); 
    }
    
    private boolean enabled()
    {
        return enabled;
    }
    
    public void setOverrideHomeFolderProviderName(String overrideHomeFolderProviderName)
    {
        this.overrideHomeFolderProviderName = overrideHomeFolderProviderName; 
    }
    
    private String getOverrideHomeFolderProviderName()
    {
        return overrideHomeFolderProviderName;
    }
    
    public void setKeepEmptyParents(String keepEmptyParents)
    {
        this.keepEmptyParents = "true".equalsIgnoreCase(keepEmptyParents); 
    }
    
    private boolean keepEmptyParents()
    {
        return keepEmptyParents;
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
            
            scanPeople(AuthenticationUtil.getSystemUserName(), TenantService.DEFAULT_DOMAIN, overrideProviderName);
            
            if (tenantAdminService.isEnabled())
            {
                List<Tenant> tenants = tenantAdminService.getAllTenants();
                for (Tenant tenant : tenants)
                {
                    if (tenant.isEnabled())
                    {
                        final String tenantDomain = tenant.getTenantDomain();
                        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                        {
                            public NodeRef doWork() throws Exception
                            {
                                scanPeople(AuthenticationUtil.getSystemUserName(), tenantDomain, overrideProviderName);
                                return null;
                            }
                        }, tenantDomain);
                        
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
    private void scanPeople(final String systemUserName, final String tenantDomain, final String overrideProvider)
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
         * 
         * Also needed to change the case of parent folders.
         */
        
        // Using authorities rather than Person objects as they are much lighter
        final Set<String> authorities = getAllAuthoritiesInTxn(systemUserName);
        final ParentFolderStructure parentFolderStructure = new ParentFolderStructure();
        final Map<NodeRef,String> tmpFolders = new HashMap<NodeRef,String>();
        
        // Define the phases
        final String createParentFoldersPhaseName = "createParentFolders";
        final String moveFolderThatClashesPhaseName = "moveHomeFolderThatClashesWithParentFolderStructure";
        RunAsWorker[] workers = new RunAsWorker[]
        {
            new RunAsWorker(systemUserName, tenantDomain, "calculateParentFolderStructure")
            {
                @Override
                public void doWork(NodeRef person) throws Exception
                {
                    calculateParentFolderStructure(
                            parentFolderStructure, person, overrideProvider);
                }
            },
            
            new RunAsWorker(systemUserName, tenantDomain, moveFolderThatClashesPhaseName)
            {
                @Override
                public void doWork(NodeRef person) throws Exception
                {
                    moveHomeFolderThatClashesWithParentFolderStructure(
                            parentFolderStructure, tmpFolders, person, overrideProvider);
                }
            },
            
            new RunAsWorker(systemUserName, tenantDomain, createParentFoldersPhaseName)
            {
                @Override
                public void doWork(NodeRef person) throws Exception
                {
                    createParentFolders(person, overrideProvider);
                }
            },
            
            new RunAsWorker(systemUserName, tenantDomain, "moveHomeFolderIfRequired")
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
            
            if (logger.isInfoEnabled())
            {
                logger.info("  -- "+
                        (TenantService.DEFAULT_DOMAIN.equals(tenantDomain)? "" : tenantDomain+" ")+
                        name+" --");
            }
            
            int threadCount = (name.equals(createParentFoldersPhaseName) || name.equals(moveFolderThatClashesPhaseName)) ? 1 : 2;
            int peoplePerTransaction = 20;
            
            // Use 2 threads, 20 person objects per transaction. Log every 100 entries.
            BatchProcessor<NodeRef> processor = new BatchProcessor<NodeRef>(
                    "HomeFolderProviderSynchronizer",
                    transactionService.getRetryingTransactionHelper(),
                    new WorkProvider(authorities),
                    threadCount, peoplePerTransaction,
                    null,
                    batchLogger, 100);
            processor.process(worker, true);
            if (processor.getTotalErrors() > 0)
            {
                logger.info("  -- Give up after error --");
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
                        // Returns a sorted set (using natural ordering) rather than a hashCode
                        // so that it is more obvious what the order is for processing users.
                        Set<String> result = new TreeSet<String>();
                        result.addAll(authorityService.getAllAuthorities(AuthorityType.USER));
                        return result;
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
                String temporary = "Temporary-";
                int from = 1;
                int to = 100;
                for (int i = from; i <= to; i++)
                {
                    String tmpFolderName = temporary+i;
                    if (fileFolderService.searchSimple(root, tmpFolderName) == null)
                    {
                        fileFolderService.create(root, tmpFolderName, ContentModel.TYPE_FOLDER);
                        return tmpFolderName;
                    }
                }
                String msg = "Unable to create a temporary " +
                        "folder into which home folders will be moved. " +
                        "Tried creating " + temporary + from + " .. " + temporary + to +
                        ". Remove these folders and try again.";
                logger.error("     # "+msg);
                throw new PersonException(msg);
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
                if (logger.isInfoEnabled())
                {
                    logger.info("     # "+toPath(actualPath)+" is already in preferred location.");
                }
            }
            
            @Override
            protected void handleSharedHomeProvider()
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("     # "+userName+" "+providerName+" creates shared home folders - These are not moved.");
                }
            }

            
            @Override
            protected void handleOriginalSharedHomeProvider()
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("     # "+userName+" Original "+originalProviderName+" creates shared home folders - These are not moved.");
                }
            }

            @Override
            protected void handleRootOrAbove()
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("     # "+userName+" has a home folder that is the provider's root directory (or is above it). " +
                    		"This is normally for users that origanally had an internal provider or a provider that uses " +
                    		"shared home folders - These are not moved.");
                }
            }
            
            @Override
            protected void handleNotAHomeFolderProvider2()
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("     # "+userName+" "+providerName+" for is not a HomeFolderProvider2.");
                }
            }

            @Override
            protected void handleSpecialHomeFolderProvider()
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("     # "+userName+" Original "+originalProviderName+" is an internal type - These are not moved.");
                }
            }

            @Override
            protected void handleHomeFolderNotSet()
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("     # "+userName+" Home folder is not set - ignored");
                }
            }
       }.doWork();
    }
    
    /**
     * @return a String for debug a folder list.
     */
    private String toPath(List<String> folders)
    {
        return toPath(folders, (folders == null) ? 0 : folders.size()-1);
    }
    
    private String toPath(List<String> folders, int depth)
    {
        StringBuilder sb = new StringBuilder("");
        if (folders != null)
        {
            if (folders.isEmpty())
            {
                sb.append('.');
            }
            else
            {
                for (String folder : folders)
                {
                    if (sb.length() > 0)
                    {
                        sb.append('/');
                    }
                    sb.append(folder);
                    if (depth-- <= 0)
                    {
                        break;
                    }
                }
            }
        }
        else
        {
            sb.append("<notUnderSameRoot>");
        }
        return sb.toString();
    }
    
    private String toPath(NodeRef root, NodeRef leaf)
    {
        StringBuilder sb = new StringBuilder("");
        List<String> path = getRelativePath(root, leaf);
        if (path != null)
        {
            if (path.isEmpty())
            {
                sb.append('.');
            }
            else
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
        }
        else
        {
            sb.append("<notUnderSameRoot>");
        }
        return sb.toString();
    }

    /**
     * @return the relative 'path' (a list of folder names) of the {@code homeFolder}
     * from the {@code root} or {@code null} if the homeFolder is not under the root
     * or is the root. An empty list is returned if the homeFolder is the same as the
     * root or the root is below the homeFolder.
     */
    private List<String> getRelativePath(NodeRef root, NodeRef homeFolder)
    {
        if (root == null || homeFolder == null)
        {
            return null;
        }
        
        if (root.equals(homeFolder))
        {
            return Collections.emptyList();
        }
        
        Path rootPath = nodeService.getPath(root);
        Path homeFolderPath = nodeService.getPath(homeFolder);
        int rootSize = rootPath.size();
        int homeFolderSize = homeFolderPath.size();
        if (rootSize >= homeFolderSize)
        {
            return Collections.emptyList();
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

            // If the preferred home folder already exists, append "-N"
            homeFolderManager.modifyHomeFolderNameIfItExists(root, preferredPath);
            String homeFolderName = preferredPath.get(preferredPath.size() - 1);
            
            // Get the old parent before we move anything.
            NodeRef oldParent = nodeService.getPrimaryParent(homeFolder) .getParentRef();

            // Log action
            if (logger.isInfoEnabled())
            {
               logger.info("     mv "+toPath(actualPath)+
                        " "+ toPath(preferredPath)+
                        ((providerName != null && !providerName.equals(originalProviderName))
                        ? "    # AND reset provider to "+providerName
                        : ""));
            }

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
                
            // Tidy up
            removeEmptyParentFolders(oldParent, oldRoot);
        }
        catch (FileExistsException e)
        {
            String message = "mv "+toPath(actualPath)+" "+toPath(preferredPath)+
                    " failed as the target already existed.";
            logger.error("     # "+message);
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
            String path = toPath(homeFolderPath, i);
            if (nodeRef == null)
            {
                if (logger.isInfoEnabled())
                {
                   logger.info("     mkdir "+path);
                }
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
                    if (logger.isErrorEnabled())
                    {
                       logger.error("     # cannot create folder " + path +
                               " as content with the same name exists. " +
                               "Move the content and try again.");
                    }
                    throw new FileExistsException(parent, path);
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
                if (logger.isInfoEnabled())
                {
                    logger.info("       rm "+toPath(root, nodeRef));
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
        @Override
        public void beforeProcess() throws Throwable
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser(userName);
        }

        @Override
        public void afterProcess() throws Throwable
        {
            AuthenticationUtil.popAuthentication();
        }

        final String userName;
        final String tenantDomain;
        final String name;
        
        public RunAsWorker(String userName, String tenantDomain, String name)
        {
            this.userName = userName;
            this.tenantDomain = tenantDomain;
            this.name = name;
        }
        
        public void process(final NodeRef person) throws Throwable
        {
            // note: runAs before runAsTenant (to avoid clearing tenant context, if no previous auth)
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                @Override
                public Object doWork() throws Exception
                {
                    return TenantUtil.runAsTenant(new TenantRunAsWork<Void>()
                    {
                        public Void doWork() throws Exception
                        {
                           RunAsWorker.this.doWork(person);
                            return null;
                        }
                    }, tenantDomain);
                }
            }, userName);
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
            this.overrideProviderName =
                (overrideProviderName == null || overrideProviderName.trim().isEmpty())
                ? null
                : overrideProviderName;
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

                                if (actualPath != null && actualPath.isEmpty())
                                {
                                    handleRootOrAbove();
                                }
                                else 
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
        
        protected void handleRootOrAbove()
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
    
    // Records the parents of the preferred folder paths (the leaf folder are not recorded)
    // and checks actual paths against these.
    private class ParentFolderStructure
    {
        // Parent folders within each root node 
        private Map<NodeRef, RootFolder> folders = new HashMap<NodeRef, RootFolder>();
        
        public void recordParentFolder(NodeRef root, List<String> path)
        {
            RootFolder rootsFolders = getFolders(root);
            synchronized(rootsFolders)
            {
                rootsFolders.add(path);
            }
        }
        
        /**
         * Checks to see if there is a clash between the preferred paths and the
         * existing folder structure. If there is a clash, the existing home folder
         * (the leaf folder) is moved to a temporary structure. This allows any
         * parent folders to be tidied up (if empty), so that the new preferred
         * structure can be recreated.<p>
         * 
         * 1. There is no clash if the path is null or empty.
         * 
         * 2. There is a clash if there is a parent structure included the root
         *    folder itself.<p>
         * 
         * 3. There is a clash if the existing path exists in the parent structure.
         *    This comparison ignores case as Alfresco does not allow duplicates
         *    regardless of case.<p>
         *
         * 4. There is a clash if any of the folders in the existing path don't
         *    match the case of the parent folders.
         * 
         * 5. There is a clash there are different case versions of the parent
         *    folders themselves or other existing folders.
         *    
         * When 4 takes place, we will end up with the first one we try to recreate
         * being used for all.
         */
        public boolean clash(NodeRef root, List<String> path)
        {
            if (path == null || path.isEmpty())
            {
                return false;
            }
            
            RootFolder rootsFolders = getFolders(root);
            synchronized(rootsFolders)
            {
                return rootsFolders.clash(path);
            }
        }

        private RootFolder getFolders(NodeRef root)
        {
            synchronized(folders)
            {
                RootFolder rootsFolders = folders.get(root);
                if (rootsFolders == null)
                {
                    rootsFolders = new RootFolder();
                    folders.put(root, rootsFolders);
                }
                return rootsFolders;
            }
        }
        
        // Records the parents of the preferred folder paths (the leaf folder are not recorded)
        // and checks actual paths against these BUT only for a single root.
        private class RootFolder extends Folder
        {
            private boolean includesRoot;
            
            public RootFolder()
            {
                super(null);
            }
            
            // Adds a path (but not the leaf folder) if it does not already exist.
            public void add(List<String> path)
            {
                if (!includesRoot)
                {
                    int parentSize = path.size() - 1;
                    if (parentSize == 0)
                    {
                        includesRoot = true;
                        children = null; // can discard children as all home folders now clash.
                        if (logger.isInfoEnabled())
                        {
                            logger.info("   # Recorded root as parent - no need to record other parents as all home folders will clash");
                        }
                    }
                    else
                    {
                        add(path, 0);
                    }
                }
            }

            /**
             * See description of {@link ParentFolderStructure#clash(NodeRef, List)}.<p>
             * 
             * Performs check 2 and then calls {@link Folder#clash(List, int)} to
             * perform 3, 4 and 5.
             */
            public boolean clash(List<String> path)
            {
                // Checks 2.
                return includesRoot ? false : clash(path, 0);
            }
        }
        
        private class Folder
        {
            // Case specific name of first folder added.
            String name;
            
            // Indicates if there is another preferred name that used different case.
            boolean duplicateWithDifferentCase;
            
            List<Folder> children;
            
            public Folder(String name)
            {
                this.name = name;
            }
            
            /**
             * Adds a path (but not the leaf folder) if it does not already exist.
             * @param path the full path to add
             * @param depth the current depth into the path starting with 0.
             */
            protected void add(List<String> path, int depth)
            {
                int parentSize = path.size() - 1;
                String name = path.get(depth);
                Folder child = getChild(name);
                if (child == null)
                {
                    child = new Folder(name);
                    if (children == null)
                    {
                        children = new LinkedList<Folder>();
                    }
                    children.add(child);
                    if (logger.isInfoEnabled())
                    {
                        logger.info("     " + toPath(path, depth));
                    }
                }
                else if (!child.name.equals(name))
                {
                    child.duplicateWithDifferentCase = true;
                }
                
                // Don't add the leaf folder
                if (++depth < parentSize)
                {
                    add(path, depth);
                }
            }

            /**
             * See description of {@link ParentFolderStructure#clash(NodeRef, List)}.<p>
             * 
             * Performs checks 3, 4 and 5 for a single level and then recursively checks
             * lower levels.
             */
            protected boolean clash(List<String> path, int depth)
            {
                String name = path.get(depth);
                Folder child = getChild(name); // Uses equalsIgnoreCase
                if (child == null)
                {
                    // Negation of check 3.
                    return false;
                }
                else if (child.duplicateWithDifferentCase) // if there folders using different case!
                {
                    // Check 5.
                    return true;
                }
                else if (!child.name.equals(name)) // if the case does not match
                {
                    // Check 4.
                    child.duplicateWithDifferentCase = true;
                    return true;
                }
                
                // If a match (including case) has been made to the end of the path
                if (++depth == path.size())
                {
                    // Check 3.
                    return true;
                }
                
                // Check lower levels.
                return clash(path, depth);
            }
            
            /**
             * Returns the child folder with the specified name (ignores case).
             */
            private Folder getChild(String name)
            {
                if (children != null)
                {
                    for (Folder child: children)
                    {
                        if (name.equalsIgnoreCase(child.name))
                        {
                            return child;
                        }
                    }
                }
                return null;
            }
        }
    }
}
