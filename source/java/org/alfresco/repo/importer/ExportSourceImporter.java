/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
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
import org.alfresco.util.TempFileProvider;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ExportSourceImporter implements ImporterJobSPI
{
    private ImporterService importerService;

    private ExportSource exportSource;

    private AuthenticationComponent authenticationComponent;
    
    private StoreRef storeRef;

    private String path;
    
    private boolean clearAllChildren;
    
    private NodeService nodeService;
    
    private SearchService searchService;
    
    private NamespacePrefixResolver namespacePrefixResolver;
    
    private TransactionService transactionService;

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
    
    

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void doImport()
    {
        UserTransaction userTransaction = null;
        try
        {
            userTransaction = transactionService.getUserTransaction();
            userTransaction.begin();
            authenticationComponent.setSystemUserAsCurrentUser();
            if(clearAllChildren)
            {
                List<NodeRef> refs = searchService.selectNodes(nodeService.getRootNode(storeRef), path, null, namespacePrefixResolver, false);
                for(NodeRef ref: refs)
                {
                    for(ChildAssociationRef car: nodeService.getChildAssocs(ref))
                    {
                        nodeService.deleteNode(car.getChildRef());
                    }
                }
            }
            
            File tempFile = TempFileProvider.createTempFile("ExportSourceImporter-", ".xml");
            Writer writer = new BufferedWriter(new FileWriter(tempFile));
            XMLWriter xmlWriter = createXMLExporter(writer);
            exportSource.generateExport(xmlWriter);
            xmlWriter.close();

            Reader reader = new BufferedReader(new FileReader(tempFile));

            Location location = new Location(storeRef);
            location.setPath(path);

            importerService.importView(reader, location, REPLACE_BINDING, null);
            reader.close();
            userTransaction.commit();
        }
        catch(Throwable t)
        {
            try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
            try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
            throw new ExportSourceImporterException("Failed to import", t);
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
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
