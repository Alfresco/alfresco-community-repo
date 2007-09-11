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
package org.alfresco.repo.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.ldap.LDAPGroupExportSource;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.view.ImporterBinding;
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

    private AuthenticationComponent authenticationComponent;

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

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
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
            authenticationComponent.setSystemUserAsCurrentUser();
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
                authenticationComponent.clearCurrentSecurityContext();
            }
            catch (Exception ex)
            {
            }
            throw new ExportSourceImporterException("Failed to import", t);
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
    }

    private static ImporterBinding REPLACE_BINDING = new ImporterBinding()
    {

        public UUID_BINDING getUUIDBinding()
        {
            return UUID_BINDING.UPDATE_EXISTING;
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

}
