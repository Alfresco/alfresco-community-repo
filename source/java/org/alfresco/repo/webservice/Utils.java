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
package org.alfresco.repo.webservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;

import org.alfresco.repo.webservice.axis.QueryConfigHandler;
import org.alfresco.repo.webservice.types.AssociationDefinition;
import org.alfresco.repo.webservice.types.Cardinality;
import org.alfresco.repo.webservice.types.ClassDefinition;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.ParentReference;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.PropertyDefinition;
import org.alfresco.repo.webservice.types.Query;
import org.alfresco.repo.webservice.types.QueryLanguageEnum;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.RoleDefinition;
import org.alfresco.repo.webservice.types.Store;
import org.alfresco.repo.webservice.types.StoreEnum;
import org.alfresco.repo.webservice.types.Version;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Helper class used by the web services
 * 
 * @author gavinc
 */
public class Utils
{
    public static final String REPOSITORY_SERVICE_NAMESPACE = "http://www.alfresco.org/ws/service/repository/1.0";

    private Utils()
    {
        // don't allow construction
    }

    /**
     * Converts the web service Store type to a StoreRef used by the repository
     * 
     * @param store
     *            The Store to convert
     * @return The converted StoreRef
     */
    public static StoreRef convertToStoreRef(Store store)
    {
        return new StoreRef(store.getScheme().getValue(), store.getAddress());
    }

    /**
     * Converts a store reference ot a Store type
     * 
     * @param ref
     *            the store reference
     * @return the store
     */
    public static Store convertToStore(StoreRef ref)
    {
        return new Store(StoreEnum.fromValue(ref.getProtocol()), ref
                .getIdentifier());
    }

    /**
     * Converts the given Reference web service type into a repository NodeRef
     * 
     * @param ref
     *            The Reference to convert
     * @return The NodeRef representation of the Reference
     */
    public static NodeRef convertToNodeRef(Reference ref,
            NodeService nodeService, SearchService searchService,
            NamespaceService namespaceService)
    {
        return resolveToNodeRef(ref.getStore(), ref.getUuid(), ref.getPath(),
                nodeService, searchService, namespaceService);
    }

    /**
     * Converts the given ParentReference web service type into a repository
     * NodeRef
     * 
     * @param parentRef
     *            The ParentReference to convert
     * @return The NodeRef representation of the ParentReference
     */
    public static NodeRef convertToNodeRef(ParentReference parentRef,
            NodeService nodeService, SearchService searchService,
            NamespaceService namespaceService)
    {
        // TODO: Also take into account any association information passed in
        // the ParentReference

        return resolveToNodeRef(parentRef.getStore(), parentRef.getUuid(),
                parentRef.getPath(), nodeService, searchService,
                namespaceService);
    }

    /**
     * Converts the given repository NodeRef object into a web service Reference
     * type
     * 
     * @param node
     *            The node to create a Reference for
     * @return The Reference
     */
    public static Reference convertToReference(NodeRef node)
    {
        Reference ref = new Reference();
        Store store = new Store(StoreEnum.fromValue(node.getStoreRef()
                .getProtocol()), node.getStoreRef().getIdentifier());
        ref.setStore(store);
        ref.setUuid(node.getId());
        return ref;
    }

    /**
     * Resolves the given parameters to a repository NodeRef
     * 
     * @param store
     *            The Store to search within
     * @param uuid
     *            The id of the node, or the id of the starting node if a path
     *            is also present
     * @param path
     *            The path to the required node, if a uuid is given the search
     *            starts from that node otherwise the search will start from the
     *            root node
     * @param nodeService
     *            NodeService to use
     * @param searchService
     *            SearchService to use
     * @param namespaceService
     *            NamespaceService to use
     * @return A repository NodeRef
     */
    public static NodeRef resolveToNodeRef(Store store, String uuid,
            String path, NodeService nodeService, SearchService searchService,
            NamespaceService namespaceService)
    {
        if (store == null)
        {
            throw new IllegalArgumentException(
                    "A Store must be supplied to resolve to a NodeRef");
        }

        NodeRef nodeRef = null;

        // find out where we are starting from, either the root or the node
        // represented by the uuid
        NodeRef rootNodeRef = null;
        if (uuid == null || uuid.length() == 0)
        {
            rootNodeRef = nodeService.getRootNode(convertToStoreRef(store));
        } else
        {
            rootNodeRef = new NodeRef(convertToStoreRef(store), uuid);
        }

        // see if we have a path to further define the node being requested
        if (path != null && path.length() != 0)
        {
            List<NodeRef> nodes = searchService.selectNodes(rootNodeRef, path,
                    null, namespaceService, false);

            // make sure we only have one result
            if (nodes.size() != 1)
            {
                StringBuilder builder = new StringBuilder(
                        "Failed to resolve to a single NodeRef with parameters (store=");
                builder.append(store.getScheme().getValue()).append(":")
                        .append(store.getAddress());
                builder.append(" uuid=").append(uuid);
                builder.append(" path=").append(path).append("), found ");
                builder.append(nodes.size()).append(" nodes.");
                throw new IllegalStateException(builder.toString());
            }

            nodeRef = nodes.get(0);
        } else
        {
            // if there is no path just use whatever the rootNodeRef currently
            // is
            nodeRef = rootNodeRef;
        }

        return nodeRef;
    }

    /**
     * Resolves the given predicate into a list of NodeRefs that can be acted
     * upon
     * 
     * @param predicate
     *            The predicate passed from the client
     * @param nodeService
     *            NodeService to use
     * @param searchService
     *            SearchService to use
     * @param namespaceService
     *            NamespaceService to use
     * @return A List of NodeRef objects
     */
    public static List<NodeRef> resolvePredicate(Predicate predicate,
            NodeService nodeService, SearchService searchService,
            NamespaceService namespaceService)
    {
        List<NodeRef> nodeRefs = null;

        if (predicate.getNodes() != null)
        {
            Reference[] nodes = predicate.getNodes();
            nodeRefs = new ArrayList<NodeRef>(nodes.length);

            for (int x = 0; x < nodes.length; x++)
            {
                nodeRefs.add(convertToNodeRef(nodes[x], nodeService,
                        searchService, namespaceService));
            }
        } 
        else if (predicate.getQuery() != null)
        {
            // make sure a query is present
            Query query = predicate.getQuery();

            if (query == null)
            {
                throw new IllegalStateException(
                        "Either a set of nodes or a query must be supplied in a Predicate.");
            }

            // make sure a Store has been supplied too
            if (predicate.getStore() == null)
            {
                throw new IllegalStateException(
                        "A Store has to be supplied to in order to execute a query.");
            }

            QueryLanguageEnum langEnum = query.getLanguage();

            if (langEnum.equals(QueryLanguageEnum.cql)
                    || langEnum.equals(QueryLanguageEnum.xpath))
            {
                throw new IllegalArgumentException("Only '"
                        + QueryLanguageEnum.lucene.getValue()
                        + "' queries are currently supported!");
            }

            // execute the query
            ResultSet searchResults = null;
            try
            {
                searchResults = searchService.query(Utils
                    .convertToStoreRef(predicate.getStore()), langEnum
                    .getValue(), query.getStatement());
                // get hold of all the NodeRef's from the results
                nodeRefs = searchResults.getNodeRefs();
            }
            finally
            {
                if (searchResults != null)
                {
                    searchResults.close();
                }
            }
        }
        else if (predicate.getStore() != null)
        {
            // Since only the store was supplied interpret this to mean the predicate should be resolved to the
            // stores root node
            Store store = predicate.getStore();
            NodeRef rootNode = nodeService.getRootNode(Utils.convertToStoreRef(store));
            
            nodeRefs = new ArrayList<NodeRef>();
            nodeRefs.add(rootNode);
        }

        return nodeRefs;
    }

    /**
     * Returns the current Spring WebApplicationContext object
     * 
     * @param msgContext
     *            SOAP message context
     * @return The Spring WebApplicationContext
     */
    public static WebApplicationContext getSpringContext(
            MessageContext msgContext)
    {
        // get hold of the web application context via the message context
        HttpServletRequest req = (HttpServletRequest) msgContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        ServletContext servletCtx = req.getSession().getServletContext();
        return WebApplicationContextUtils
                .getRequiredWebApplicationContext(servletCtx);
    }

    /**
     * Returns a UserTransaction that can be used within a service call
     * 
     * @param msgContext
     *            SOAP message context
     * @return a UserTransaction
     */
    public static UserTransaction getUserTransaction(MessageContext msgContext)
    {
        // get the service regsistry
        ServiceRegistry svcReg = (ServiceRegistry) getSpringContext(msgContext)
                .getBean(ServiceRegistry.SERVICE_REGISTRY);

        TransactionService transactionService = svcReg.getTransactionService();
        return transactionService.getUserTransaction();
    }

    /**
     * Returns the value of the <code>fetchSize</code> from the
     * QueryConfiguration SOAP header (if present)
     * 
     * @param msgContext
     *            The SOAP MessageContext
     * @return The current batch size or -1 if the header is not present
     */
    public static int getBatchSize(MessageContext msgContext)
    {
        int batchSize = -1;

        Integer batchConfigSize = (Integer) MessageContext.getCurrentContext()
                .getProperty(QueryConfigHandler.ALF_FETCH_SIZE);
        if (batchConfigSize != null)
        {
            batchSize = batchConfigSize.intValue();
        }

        return batchSize;
    }

    /**
     * Converts a repository version object into a web service version object.
     * 
     * @param version
     *            the repository version object
     * @return the web service version object
     */
    public static Version convertToVersion(
            org.alfresco.service.cmr.version.Version version)
    {
        Version webServiceVersion = new Version();

        // Set the basic properties
        webServiceVersion.setId(Utils.convertToReference(version
                .getFrozenStateNodeRef()));
        webServiceVersion.setCreator(version.getCreator());
        webServiceVersion.setLabel(version.getVersionLabel());

        // Set the created date
        Date createdDate = version.getCreatedDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdDate);
        webServiceVersion.setCreated(calendar);

        // Set the falg to indicate whether the version was mojor or minor
        boolean isMajor = false;
        VersionType versionType = version.getVersionType();
        if (versionType != null
                && versionType.equals(VersionType.MAJOR) == true)
        {
            isMajor = true;
        }
        webServiceVersion.setMajor(isMajor);

        // Set the commetary values
        Map<String, Serializable> versionProps = version.getVersionProperties();
        NamedValue[] namedValues = new NamedValue[versionProps.size()];
        int iIndex = 0;
        for (Map.Entry<String, Serializable> entry : versionProps.entrySet())
        {
            String value = null;
            try
            {
                value = DefaultTypeConverter.INSTANCE.convert(String.class, entry.getValue());
            } 
            catch (Throwable exception)
            {
                value = entry.getValue().toString();
            }
            namedValues[iIndex] = new NamedValue(entry.getKey(), value);
            iIndex++;
        }
        webServiceVersion.setCommentaries(namedValues);

        return webServiceVersion;
    }
    
    /**
     * Creates a ClassDefinition web service type object for the given
     * repository ClassDefinition
     * 
     * @param ddClassDef The repository ClassDefinition to generate
     * @return The web service ClassDefinition representation
     */
    public static ClassDefinition setupClassDefObject(org.alfresco.service.cmr.dictionary.ClassDefinition ddClassDef)
    {
       ClassDefinition classDef = new ClassDefinition();
       classDef.setName(ddClassDef.getName().toString());
       classDef.setIsAspect(ddClassDef.isAspect());
       
       if (ddClassDef.getTitle() != null)
       {
          classDef.setTitle(ddClassDef.getTitle());
       }
       if (ddClassDef.getDescription() != null)
       {
          classDef.setDescription(ddClassDef.getDescription());
       }
       if (ddClassDef.getParentName() != null)
       {
          classDef.setSuperClass(ddClassDef.getParentName().toString());
       }
       
       // represent the properties
       Map<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> props = ddClassDef.getProperties();
       if (props != null)
       {
          PropertyDefinition[] propDefs = new PropertyDefinition[props.size()];
          int pos = 0;
          for (org.alfresco.service.cmr.dictionary.PropertyDefinition ddPropDef : props.values())
          {
             PropertyDefinition propDef = new PropertyDefinition();
             propDef.setName(ddPropDef.getName().toString());
             propDef.setDataType(ddPropDef.getDataType().getName().toString());
             propDef.setMandatory(ddPropDef.isMandatory()); 
             propDef.setReadOnly(ddPropDef.isProtected());
             if (ddPropDef.getDefaultValue() != null)
             {
                propDef.setDefaultValue(ddPropDef.getDefaultValue());
             }
             if (ddPropDef.getTitle() != null)
             {
                propDef.setTitle(ddPropDef.getTitle());
             }
             if (ddPropDef.getDescription() != null)
             {
                propDef.setDescription(ddPropDef.getDescription());
             }
             
             // add it to the array
             propDefs[pos] = propDef;
             pos++;
          }
          
          // add properties to the overall ClassDefinition
          classDef.setProperties(propDefs);
       }
       
       // TODO need to get the child associations as well !!
       
       // represent the associations
       Map<QName, org.alfresco.service.cmr.dictionary.AssociationDefinition> assocs = ddClassDef.getAssociations();
       if (assocs != null)
       {
          AssociationDefinition[] assocDefs = new AssociationDefinition[assocs.size()];
          int pos = 0;
          for (org.alfresco.service.cmr.dictionary.AssociationDefinition ddAssocDef : assocs.values())
          {
             AssociationDefinition assocDef = new AssociationDefinition();
             assocDef.setName(ddAssocDef.getName().toString());
             assocDef.setIsChild(ddAssocDef.isChild());
             if (ddAssocDef.getTitle() != null)
             {
                assocDef.setTitle(ddAssocDef.getTitle());
             }
             if (ddAssocDef.getDescription() != null)
             {
                assocDef.setDescription(ddAssocDef.getDescription());
             }
             
             RoleDefinition sourceRole = new RoleDefinition();
             if (ddAssocDef.getSourceRoleName() != null)
             {
                sourceRole.setName(ddAssocDef.getSourceRoleName().toString());
             }
             sourceRole.setCardinality(setupSourceCardinalityObject(ddAssocDef));
             assocDef.setSourceRole(sourceRole);
             
             RoleDefinition targetRole = new RoleDefinition();
             if (ddAssocDef.getTargetRoleName() != null)
             {
                targetRole.setName(ddAssocDef.getTargetRoleName().toString());
             }
             targetRole.setCardinality(setupTargetCardinalityObject(ddAssocDef));;
             assocDef.setTargetRole(targetRole);
             assocDef.setTargetClass(ddAssocDef.getTargetClass().getName().toString());
             
             assocDefs[pos] = assocDef;
             pos++;
          }
          
          classDef.setAssociations(assocDefs);
       }
       
       return classDef;
    }
    
    /**
     * Creates a web service Cardinality type for the source from the given repository AssociationDefinition
     * 
     * @param ddAssocDef The AssociationDefinition to get the cardinality from 
     * @return The Cardinality
     */
    private static Cardinality setupSourceCardinalityObject(org.alfresco.service.cmr.dictionary.AssociationDefinition ddAssocDef)
    {
       if (ddAssocDef.isSourceMandatory() == false && ddAssocDef.isSourceMany() == false)
       {
          // 0..1
          return Cardinality.value1;
       }
       else if (ddAssocDef.isSourceMandatory() && ddAssocDef.isSourceMany() == false)
       {
          // 1
          return Cardinality.value2;
       }
       else if (ddAssocDef.isSourceMandatory() && ddAssocDef.isSourceMany())
       {
          // 1..*
          return Cardinality.value4;
       }
       else
       {
          // *
          return Cardinality.value3;
       }
    }
    
    /**
     * Creates a web service Cardinality type for the target from the given repository AssociationDefinition
     * 
     * @param ddAssocDef The AssociationDefinition to get the cardinality from 
     * @return The Cardinality
     */
    private static Cardinality setupTargetCardinalityObject(org.alfresco.service.cmr.dictionary.AssociationDefinition ddAssocDef)
    {
       if (ddAssocDef.isTargetMandatory() == false && ddAssocDef.isTargetMany() == false)
       {
          // 0..1
          return Cardinality.value1;
       }
       else if (ddAssocDef.isTargetMandatory() && ddAssocDef.isTargetMany() == false)
       {
          // 1
          return Cardinality.value2;
       }
       else if (ddAssocDef.isTargetMandatory() && ddAssocDef.isTargetMany())
       {
          // 1..*
          return Cardinality.value4;
       }
       else
       {
          // *
          return Cardinality.value3;
       }
    }
}
