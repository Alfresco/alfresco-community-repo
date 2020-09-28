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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ExportSourceImporter implements ImporterJobSPI
{
    private static Log logger = LogFactory.getLog(ExportSourceImporter.class);
    
    private ImporterService importerService;

    private ExportSource exportSource;

    private StoreRef storeRef;

    private String path;

    private boolean clearAllChildren;

    private NodeService nodeService;

    private SearchService searchService;

    private NamespacePrefixResolver namespacePrefixResolver;

    private TransactionService transactionService;

    private Set<SimpleCache> caches;

    public ExportSourceImporter()
    {
        super();
    }

    public void setImporterService(ImporterService importerService)
    {
        this.importerService = importerService;
    }

    public void setExportSource(ExportSource exportSource)
    {
        this.exportSource = exportSource;
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
            AuthenticationUtil.pushAuthentication();
            userTransaction = transactionService.getUserTransaction();
            userTransaction.begin();
            AuthenticationUtil.setRunAsUserSystem();
            if (clearAllChildren)
            {
                logger.debug("clear all children");
                List<NodeRef> refs = searchService.selectNodes(nodeService.getRootNode(storeRef), path, null,
                        namespacePrefixResolver, false);
                
                for (NodeRef ref : refs)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("clear node ref" + ref);
                    }
                    for (ChildAssociationRef car : nodeService.getChildAssocs(ref))
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("delete child" + car.getChildRef());
                        }
                        nodeService.deleteNode(car.getChildRef());
                    }
                }
            }

            if (caches != null)
            {
                logger.debug("clearing caches");
                for (SimpleCache cache : caches)
                {

                    cache.clear();
                }
            }

            File tempFile = TempFileProvider.createTempFile("ExportSourceImporter-", ".xml");
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
            XMLWriter xmlWriter = createXMLExporter(writer);
            exportSource.generateExport(xmlWriter);
            xmlWriter.close();

            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile), "UTF-8"));

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
            logger.debug("about to commit");
            userTransaction.commit();
        }
        catch (Throwable t)
        {
            try
            {
                if (userTransaction != null)
                {
                    logger.debug("rolling back due to exception", t);
                    userTransaction.rollback();
                }
            }
            catch (Exception ex)
            {
                logger.debug("exception during rollback", ex);
            }
            throw new ExportSourceImporterException("Failed to import", t);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    private XMLWriter createXMLExporter(Writer writer)
    {
        // Define output format
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(3);
        format.setEncoding("UTF-8");

        // Construct an XML Exporter

        XMLWriter xmlWriter = new XMLWriter(writer, format);
        return xmlWriter;
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
