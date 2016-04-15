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
package org.alfresco.repo.dictionary;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterContentCache;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * 
 * @author sglover
 *
 */
public class DynamicCreateRepositoryLocation extends RepositoryLocation
{
    private static final Log logger = LogFactory.getLog(DynamicCreateRepositoryLocation.class);

    private ImporterService importerService;
	private String contentViewLocation;
    private ResourceBundle bundle;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private TransactionService transactionService;

	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	public void setNamespaceService(NamespaceService namespaceService)
	{
		this.namespaceService = namespaceService;
	}

	public void setContentViewLocation(String contentViewLocation)
	{
		this.contentViewLocation = contentViewLocation;
	}

	public void setImporterService(ImporterService importerService)
    {
		this.importerService = importerService;
	}

	public void setBundleName(String bundleName)
	{
        Locale bindingLocale = I18NUtil.getLocale();
        this.bundle = ResourceBundle.getBundle(bundleName, bindingLocale);
	}

    public void checkAndCreate(NodeRef rootNodeRef)
    {
		List<NodeRef> nodes = searchService.selectNodes(rootNodeRef, getPath(), null, namespaceService, false);
		if(nodes.size() == 0)
		{
    		logger.info("Repository location " + getPath() + " does not exist for tenant "
    				+ TenantUtil.getCurrentDomain() + ", creating");
    		create();
		}
    }

    protected String getParentPath()
    {
    	String parentPath = null;

    	String path = getPath();
    	int idx = path.lastIndexOf("/");
    	if(idx != -1)
    	{
    		parentPath = path.substring(0, idx);
    	}
    	else
    	{
    		parentPath = "/";
    	}

    	return parentPath;
    }

    protected void create()
    {
        RetryingTransactionCallback<Void> initCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                onCreateInTxn();
                return null;
            }
        };
        getTransactionService().getRetryingTransactionHelper().doInTransaction(initCallback, false, true);

    }
    private void onCreateInTxn()
    {       
        final File viewFile = ImporterBootstrap.getFile(contentViewLocation);
        ImportPackageHandler acpHandler = new ACPImportPackageHandler(viewFile, null);
        Location location = new Location(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        location.setPath(getParentPath());

        final ImporterBinding binding = new ImporterBinding()
        {
            @Override
            public String getValue(String key)
            {
                return bundle.getString(key);
            }

            @Override
            public UUID_BINDING getUUIDBinding()
            {
                return UUID_BINDING.CREATE_NEW;
            }

            @Override
            public QName[] getExcludedClasses()
            {
                return null;
            }

            @Override
            public boolean allowReferenceWithinTransaction()
            {
                return false;
            }
            
            @Override
            public ImporterContentCache getImportConentCache()
            {
                return null;
            }
        };

        importerService.importView(acpHandler, location, binding, (ImporterProgress) null);
    }

	public TransactionService getTransactionService() 
	{
		return transactionService;
	}

	public void setTransactionService(TransactionService transactionService) 
	{
		this.transactionService = transactionService;
	}
}
