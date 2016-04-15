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
package org.alfresco.repo.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterContentCache;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSourceImporter implements ImporterJobSPI
{
    private static Log s_logger = LogFactory.getLog(FileSourceImporter.class);
    
    private ImporterService importerService;

    private String fileLocation;

    private AuthenticationContext authenticationContext;

    private StoreRef storeRef;

    private String path;

    private boolean clearAllChildren;

    private NodeService nodeService;

    private SearchService searchService;

    private NamespacePrefixResolver namespacePrefixResolver;

    private TransactionService transactionService;

    private Set<SimpleCache> caches;

    public FileSourceImporter()
    {
        super();
    }

    public void setImporterService(ImporterService importerService)
    {
        this.importerService = importerService;
    }

    public void setFileLocation(String fileLocation)
    {
        this.fileLocation = fileLocation;
    }

    public void setClearAllChildren(boolean clearAllChildren)
    {
        this.clearAllChildren = clearAllChildren;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setStoreRef(String storeRef)
    {
        this.storeRef = new StoreRef(storeRef);
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setCaches(Set<SimpleCache> caches)
    {
        this.caches = caches;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    @SuppressWarnings("unchecked")
    public void doImport()
    {
        UserTransaction userTransaction = null;
        try
        {
            long start = System.nanoTime();
            userTransaction = transactionService.getUserTransaction();
            userTransaction.begin();
            authenticationContext.setSystemUserAsCurrentUser();
            if (clearAllChildren)
            {
                List<NodeRef> refs = searchService.selectNodes(nodeService.getRootNode(storeRef), path, null,
                        namespacePrefixResolver, false);
                for (NodeRef ref : refs)
                {
                    for (ChildAssociationRef car : nodeService.getChildAssocs(ref))
                    {
                        nodeService.deleteNode(car.getChildRef());
                    }
                }
            }

            if (caches != null)
            {
                for (SimpleCache cache : caches)
                {

                    cache.clear();
                }
            }

            Reader reader = new BufferedReader(new FileReader(fileLocation));

            Location location = new Location(storeRef);
            location.setPath(path);

            importerService.importView(reader, location, REPLACE_BINDING, null);
            reader.close();

            if (caches != null)
            {
                for (SimpleCache cache : caches)
                {
                    cache.clear();
                }
            }

            userTransaction.commit();
            long end = System.nanoTime();
            s_logger.info("Imported "+fileLocation+ " in "+((end-start)/1e9f) + " seconds");
        }
        catch (Throwable t)
        {
            try
            {
                if (userTransaction != null)
                {
                    userTransaction.rollback();
                }
            }
            catch (Exception ex)
            {
            }
            try
            {
                authenticationContext.clearCurrentSecurityContext();
            }
            catch (Exception ex)
            {
            }
            throw new ExportSourceImporterException("Failed to import", t);
        }
        finally
        {
            authenticationContext.clearCurrentSecurityContext();
        }
    }

    private static ImporterBinding REPLACE_BINDING = new ImporterBinding()
    {
        @Override
        public UUID_BINDING getUUIDBinding()
        {
            return UUID_BINDING.UPDATE_EXISTING;
        }

        @Override
        public String getValue(String key)
        {
            return null;
        }

        @Override
        public boolean allowReferenceWithinTransaction()
        {
            return false;
        }

        @Override
        public QName[] getExcludedClasses()
        {
            return null;
        }
        
        @Override
        public ImporterContentCache getImportConentCache()
        {
            return null;
        }
    };

}
