/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.bulkimport.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.bulkimport.BulkFSImportEvent;
import org.alfresco.repo.bulkimport.BulkFilesystemImporter;
import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.BulkImportStatus;
import org.alfresco.repo.bulkimport.DirectoryAnalyser;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @since 4.0
 *
 */
public abstract class AbstractBulkFilesystemImporter implements BulkFilesystemImporter, InitializingBean, ApplicationContextAware
{
    private static final QName LOCK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "BatchFilesystemImport");
    protected static final Log logger = LogFactory.getLog(BulkFilesystemImporter.class);

    protected ApplicationContext applicationContext;
    
    protected FileFolderService fileFolderService;
    protected TransactionService transactionService;
    protected PermissionService permissionService;
    protected RetryingTransactionHelper transactionHelper;

    protected BulkImportStatusImpl importStatus;
    protected DirectoryAnalyser directoryAnalyser = null;

    protected JobLockService jobLockService;
    
    protected BehaviourFilter behaviourFilter;
    
	public void setBehaviourFilter(BehaviourFilter behaviourFilter)
	{
		this.behaviourFilter = behaviourFilter;
	}
	
	public void setJobLockService(JobLockService jobLockService)
	{
		this.jobLockService = jobLockService;
	}
    
	public void setImportStatus(BulkImportStatusImpl importStatus)
	{
		this.importStatus = importStatus;
	}
	
    public final void setDirectoryAnalyser(DirectoryAnalyser directoryAnalyser)
    {
        this.directoryAnalyser = directoryAnalyser;
    }

	public void setFileFolderService(FileFolderService fileFolderService)
	{
		this.fileFolderService = fileFolderService;
	}

	public void setTransactionService(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	public void setPermissionService(PermissionService permissionService)
	{
		this.permissionService = permissionService;
	}
    
    /**
     * @see org.alfresco.extension.bulkfilesystemimport.BulkFilesystemImporter#getStatus()
     */
    public final BulkImportStatus getStatus()
    {
        return(importStatus);
    }
	
	public void afterPropertiesSet() throws Exception
	{
        PropertyCheck.mandatory(this, "fileFolderService", fileFolderService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        
        PropertyCheck.mandatory(this, "importStatus", importStatus);
        PropertyCheck.mandatory(this, "directoryAnalyser", directoryAnalyser);
        
        this.transactionHelper = transactionService.getRetryingTransactionHelper();
	}

    protected abstract void bulkImportImpl(BulkImportParameters bulkImportParameters, NodeImporter nodeImporter, String lockToken) throws Throwable;

    /**
     * Attempts to get the lock. If the lock couldn't be taken, then <tt>null</tt> is returned.
     * 
     * @return Returns the lock token or <tt>null</tt>
     */
    protected String getLock(long time)
    {
        try
        {
            return jobLockService.getLock(LOCK, time);
        }
        catch (LockAcquisitionException e)
        {
            return null;
        }
    }

    /**
     * Attempts to get the lock. If it fails, the current transaction is marked for rollback.
     * 
     * @return Returns the lock token
     */
    protected void refreshLock(String lockToken, long time)
    {
        if (lockToken == null)
        {
            throw new IllegalArgumentException("Must provide existing lockToken");
        }
        jobLockService.refreshLock(lockToken, LOCK, time);
    }
    
    protected void releaseLock(String lockToken)
    {
        if (lockToken == null)
        {
            throw new IllegalArgumentException("Must provide existing lockToken");
        }
        jobLockService.releaseLock(lockToken, LOCK);
    }
    
    /*
     * Because commons-lang ToStringBuilder doesn't seem to like unmodifiable Maps
     */
    protected final String mapToString(Map<?, ?> map)
    {
        StringBuffer result = new StringBuffer();
        
        if (map != null)
        {
            result.append('[');

            if (map.size() > 0)
            {
                for (Object key : map.keySet())
                {
                    result.append(String.valueOf(key));
                    result.append(" = ");
                    result.append(String.valueOf(map.get(key)));
                    result.append(",\n");
                }
                
                // Delete final dangling ", " value
                result.delete(result.length() - 2, result.length());
            }
            
            result.append(']');
        }
        else
        {
            result.append("(null)");
        }
        
        return(result.toString());
    }
    
    protected final String getRepositoryPath(NodeRef nodeRef)
    {
        String result = null;
        
        if (nodeRef != null)
        {
            List<FileInfo> pathElements = null;
            
            try
            {
                pathElements = fileFolderService.getNamePath(null, nodeRef);

                if (pathElements != null && pathElements.size() > 0)
                {
                    StringBuilder temp = new StringBuilder();
                    
                    for (FileInfo pathElement : pathElements)
                    {
                        temp.append("/");
                        temp.append(pathElement.getName());
                    }
                    
                    result = temp.toString();
                }
            }
            catch (final FileNotFoundException fnfe)
            {
                // Do nothing
            }
        }
        
        return(result);
    }

    protected final void validateNodeRefIsWritableSpace(NodeRef target)
    {
        if (target == null)
        {
            throw new IllegalArgumentException("target must not be null.");
        }
        
        if (!fileFolderService.exists(target))
        {
            throw new IllegalArgumentException("Target '" + target.toString() + "' doesn't exist.");
        }
        
        if (AccessStatus.DENIED.equals(permissionService.hasPermission(target, PermissionService.ADD_CHILDREN)))
        {
            throw new IllegalArgumentException("Target '" + target.toString() + "' is not writeable.");
        }
        
        if (!fileFolderService.getFileInfo(target).isFolder())
        {
            throw new IllegalArgumentException("Target '" + target.toString() + "' is not a space.");
        }
    }
    
    protected String getFileName(File file)
    {
    	return FileUtils.getFileName(file);
    }
    
    protected String getLockToken()
    {
		// Take out a bulk filesystem import lock
		RetryingTransactionCallback<String> txnWork = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Exception
            {
		        String lockToken = getLock(20000L);
		        return lockToken;
            }
        };

        String lockToken = transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
//        if(lockToken == null)
//        {
//            logger.warn("Can't get lock. Assume multiple bulk filesystem importers ...");
//            return;
//        }
        
        return lockToken;
    }
    
    public void validateSourceIsReadableDirectory(File source)
    {
        try
        {
            if (source == null)
            {
                throw new IllegalArgumentException("source must not be null.");
            }
            
            if (!source.exists())
            {
                throw new IllegalArgumentException("Source '" + source.getCanonicalPath() + "' doesn't exist.");
            }
            
            if (!source.canRead())
            {
                throw new IllegalArgumentException("Source '" + source.getCanonicalPath() + "' is not readable.");
            }
            
            if (!source.isDirectory())
            {
                throw new IllegalArgumentException("Source '" + source.getCanonicalPath() + "' is not a directory.");
            }
        }
        catch (final IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }
    
    public void asyncBulkImport(final BulkImportParameters bulkImportParameters, final NodeImporter nodeImporter)
    {
    	final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        Runnable backgroundLogic = new Runnable()
        {
            public void run()
            {
            	AuthenticationUtil.runAs(new RunAsWork<Object>()
            	{
            		public Object doWork()
            		{
            			bulkImport(bulkImportParameters, nodeImporter);
		            	return null;
					}
				}, currentUser);
            }
        };

        Thread backgroundThread = new Thread(backgroundLogic, "BulkFilesystemImport-BackgroundThread");
        //backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
    
    /**
     * @see org.alfresco.extension.bulkfilesystemimport.BulkFilesystemImporter#bulkImport(java.io.File, org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void bulkImport(final BulkImportParameters bulkImportParameters, final NodeImporter nodeImporter)
    {
    	final File sourceFolder = nodeImporter.getSourceFolder();
    	final BulkFilesystemImporter importer = this;

    	transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
    	{
    		@Override
    		public Void execute() throws Throwable
    		{
    			String sourceDirectory = getFileName(sourceFolder);
    			String targetSpace = getRepositoryPath(bulkImportParameters.getTarget());

				String lockToken = getLockToken();

    			try
    			{
    				importStatus.startImport(sourceDirectory, targetSpace);
    				
    				BulkFSImportEvent bulkImportEvent = new BulkFSImportEvent(importer);
    				applicationContext.publishEvent(bulkImportEvent);
    				
    				validateNodeRefIsWritableSpace(bulkImportParameters.getTarget());
    				validateSourceIsReadableDirectory(sourceFolder);
    				
    				if(logger.isDebugEnabled())
    				{
    					logger.debug("Bulk import started from '" + sourceFolder.getAbsolutePath() + "'...");
    				}
    				
    				bulkImportImpl(bulkImportParameters, nodeImporter, lockToken);
    				
    				importStatus.stopImport();
    				
    				if(logger.isDebugEnabled())
    				{
    					logger.debug("Bulk import from '" + getFileName(sourceFolder) + "' succeeded.");
    				}
    				
    				return null;
    			}
    			catch(Throwable e)
    			{
    				logger.error("Bulk import from '" + getFileName(sourceFolder) + "' failed.", e);
    				importStatus.stopImport(e);
    				throw new AlfrescoRuntimeException("Bulk filesystem import failed", e);
    			}
    			finally
    			{
    				BulkFSImportEvent bulkImportEvent = new BulkFSImportEvent(importer);
    				applicationContext.publishEvent(bulkImportEvent);

    				releaseLock(lockToken);
    			}
            }
    	}, false, true);
    }
        
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
	{
		this.applicationContext = applicationContext;
	}
}
