/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.bulkimport.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
import org.alfresco.service.cmr.rule.RuleService;
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
    protected RuleService ruleService;

    protected BulkImportStatusImpl importStatus;
    protected DirectoryAnalyser directoryAnalyser = null;

    protected JobLockService jobLockService;
    
    protected BehaviourFilter behaviourFilter;
    
	public void setRuleService(RuleService ruleService)
	{
		this.ruleService = ruleService;
	}

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
     * @see org.alfresco.repo.bulkimport.BulkFilesystemImporter#getStatus()
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

    protected abstract void bulkImportImpl(BulkImportParameters bulkImportParameters, NodeImporter nodeImporter, String lockToken);

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
        backgroundThread.start();
    }
    
    /**
     * @see org.alfresco.repo.bulkimport.BulkFilesystemImporter#bulkImport(org.alfresco.repo.bulkimport.BulkImportParameters, org.alfresco.repo.bulkimport.NodeImporter)
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
    		    final String sourceDirectory = getFileName(sourceFolder);
    		    final String targetSpace = getRepositoryPath(bulkImportParameters.getTarget());
    		    final String lockToken = getLockToken();

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
    		        importStatus.stopImport(e);
    		        throw e;
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
