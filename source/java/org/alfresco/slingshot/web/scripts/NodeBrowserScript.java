package org.alfresco.slingshot.web.scripts;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO9075;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Node browser web script to handle search results, node details and workspaces
 * 
 * @author dcaruana
 * @author wabson
 */
public class NodeBrowserScript extends DeclarativeWebScript
{
    /** available query languages */
    private static List<String> queryLanguages = new ArrayList<String>();
    static
    {
        queryLanguages.add("noderef");
        queryLanguages.add(SearchService.LANGUAGE_XPATH);
        queryLanguages.add(SearchService.LANGUAGE_LUCENE);
        queryLanguages.add(SearchService.LANGUAGE_FTS_ALFRESCO);
        queryLanguages.add(SearchService.LANGUAGE_CMIS_STRICT);
        queryLanguages.add(SearchService.LANGUAGE_CMIS_ALFRESCO);
        queryLanguages.add(SearchService.LANGUAGE_JCR_XPATH);
    }

    // stores and node
    transient private List<StoreRef> stores = null;

    // supporting repository services
    transient private TransactionService transactionService;
    transient private NodeService nodeService;
    transient private DictionaryService dictionaryService;
    transient private SearchService searchService;
    transient private NamespaceService namespaceService;
    transient private PermissionService permissionService;
    transient private OwnableService ownableService;
    transient private AVMService avmService;

    /**
     * @param transactionService        transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    private TransactionService getTransactionService()
    {
        return transactionService;
    }

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    private NodeService getNodeService()
    {
        return nodeService;
    }

    /**
     * @param searchService search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    private SearchService getSearchService()
    {
        return searchService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    private DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    /**
     * @param namespaceService namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

	private NamespaceService getNamespaceService()
    {
        return namespaceService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    private PermissionService getPermissionService()
    {
        return permissionService;
    }

	public void setOwnableService(OwnableService ownableService)
	{
		this.ownableService = ownableService;
	}

    public OwnableService getOwnableService()
    {
		return ownableService;
	}

	/**
     * @param avmService AVM service
     */
    public void setAVMService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    private AVMService getAVMService()
    {
        return avmService;
    }

    /**
     * Gets the list of repository stores
     * 
     * @return stores
     */
    public List<StoreRef> getStores()
    {
        if (stores == null)
        {
            stores = getNodeService().getStores();
        }
        return stores;
    }

    /**
     * Gets the current node type
     * 
     * @return node type
     */
    public QName getNodeType(NodeRef nodeRef)
    {
        return getNodeService().getType(nodeRef);
    }

    /**
     * Gets the current node primary path
     * 
     * @return primary path
     */
    public String getPrimaryPath(NodeRef nodeRef)
    {
        Path primaryPath = getNodeService().getPath(nodeRef);
        return ISO9075.decode(primaryPath.toString());
    }

    /**
     * Gets the current node primary path
     * 
     * @return primary path
     */
    public String getPrimaryPrefixedPath(NodeRef nodeRef)
    {
        Path primaryPath = getNodeService().getPath(nodeRef);
        return ISO9075.decode(primaryPath.toPrefixString(getNamespaceService()));
    }

    /**
     * Gets the current node primary parent reference
     * 
     * @return primary parent ref
     */
    public NodeRef getPrimaryParent(NodeRef nodeRef)
    {
        Path primaryPath = getNodeService().getPath(nodeRef);
        Path.Element element = primaryPath.last();
        NodeRef parentRef = ((Path.ChildAssocElement) element).getRef().getParentRef();
        return parentRef;
    }

    /**
     * Gets the current node aspects
     * 
     * @return node aspects
     */
    public List<Aspect> getAspects(NodeRef nodeRef)
    {
        Set<QName> qnames = getNodeService().getAspects(nodeRef);
        List<Aspect> aspects = new ArrayList<Aspect>(qnames.size());
        for (QName qname : qnames)
        {
			aspects.add(new Aspect(qname));
		}
        return aspects;
    }

    /**
     * Gets the current node parents
     * 
     * @return node parents
     */
    public List<ChildAssociation> getParents(NodeRef nodeRef)
    {
        List<ChildAssociationRef> parents = getNodeService().getParentAssocs(nodeRef);
        List<ChildAssociation> assocs = new ArrayList<ChildAssociation>(parents.size());
    	for (ChildAssociationRef ref : parents)
    	{
    		assocs.add(new ChildAssociation(ref));
		}
        return assocs;
    }

    /**
     * Gets the current node properties
     * 
     * @return properties
     */
    public List<Property> getProperties(NodeRef nodeRef)
    {
        Map<QName, Serializable> propertyValues = getNodeService().getProperties(nodeRef);
        List<Property> properties = new ArrayList<Property>(propertyValues.size());
        for (Map.Entry<QName, Serializable> property : propertyValues.entrySet())
        {
            properties.add(new Property(property.getKey(), property.getValue()));
        }
        return properties;
    }

    /**
     * Gets whether the current node inherits its permissions from a parent node
     * 
     * @return true => inherits permissions
     */
    public boolean getInheritPermissions(NodeRef nodeRef)
    {
        Boolean inheritPermissions = this.getPermissionService().getInheritParentPermissions(nodeRef);
        return inheritPermissions.booleanValue();
    }

    /**
     * Gets the current node permissions
     * 
     * @return the permissions
     */
    public List<Permission> getPermissions(NodeRef nodeRef)
    {
        List<Permission> permissions = null;
        AccessStatus readPermissions = this.getPermissionService().hasPermission(nodeRef, PermissionService.READ_PERMISSIONS);
        if (readPermissions.equals(AccessStatus.ALLOWED))
        {
            List<Permission> nodePermissions = new ArrayList<Permission>();
            for (Iterator<AccessPermission> iterator = getPermissionService().getAllSetPermissions(nodeRef).iterator(); iterator
                    .hasNext();)
            {
            	AccessPermission ap = iterator.next();
                nodePermissions.add(new Permission(ap.getPermission(), ap.getAuthority(), ap.getAccessStatus().toString()));
            }
            permissions = nodePermissions;
        }
        else
        {
            List<Permission> noReadPermissions = new ArrayList<Permission>(1);
            noReadPermissions.add(new NoReadPermissionGranted());
            permissions = noReadPermissions;
        }
        return permissions;
    }

    /**
     * Gets the current node permissions
     * 
     * @return the permissions
     */
    public List<Permission> getStorePermissionMasks(NodeRef nodeRef)
    {
        List<Permission> permissionMasks = null;
        if (nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
        {
        	permissionMasks = new ArrayList<Permission>();
            for (Iterator<AccessPermission> iterator = getPermissionService().getAllSetPermissions(nodeRef.getStoreRef()).iterator(); iterator
                    .hasNext();)
            {
            	AccessPermission ap = iterator.next();
            	permissionMasks.add(new Permission(ap.getPermission(), ap.getAuthority(), ap.getAccessStatus().toString()));
            }
        }
        else
        {
        	permissionMasks = new ArrayList<Permission>(1);
        	permissionMasks.add(new NoStoreMask());
        }
        return permissionMasks;
    }

    /**
     * Gets the current node children
     * 
     * @return node children
     */
    public List<ChildAssociation> getChildren(NodeRef nodeRef)
    {
    	List<ChildAssociationRef> refs = getNodeService().getChildAssocs(nodeRef);
        List<ChildAssociation> assocs = new ArrayList<ChildAssociation>(refs.size());
    	for (ChildAssociationRef ref : refs)
    	{
    		assocs.add(new ChildAssociation(ref));
		}
        return assocs;
    }

    /**
     * Gets the current node associations
     * 
     * @return associations
     */
    public List<PeerAssociation> getAssocs(NodeRef nodeRef)
    {
        List<AssociationRef> refs = null;
        try
        {
            refs = getNodeService().getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        }
        catch (UnsupportedOperationException err)
        {
           // some stores do not support associations
        }
        List<PeerAssociation> assocs = new ArrayList<PeerAssociation>(refs.size());
    	for (AssociationRef ref : refs)
    	{
    		assocs.add(new PeerAssociation(ref.getTypeQName(), ref.getSourceRef(), ref.getTargetRef()));
		}
        return assocs;
    }

    /**
     * Gets the current source associations
     * 
     * @return associations
     */
    public List<PeerAssociation> getSourceAssocs(NodeRef nodeRef)
    {
        List<AssociationRef> refs = null;
        try
        {
            refs = getNodeService().getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
        }
        catch (UnsupportedOperationException err)
        {
           // some stores do not support associations
        }
        List<PeerAssociation> assocs = new ArrayList<PeerAssociation>(refs.size());
    	for (AssociationRef ref : refs)
    	{
    		assocs.add(new PeerAssociation(ref.getTypeQName(), ref.getSourceRef(), ref.getTargetRef()));
		}
        return assocs;
    }

    public boolean getInAVMStore(NodeRef nodeRef)
    {
        return nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM);
    }

    public List<Map<String, String>> getAVMStoreProperties(NodeRef nodeRef)
    {
        List<Map<String, String>> avmStoreProps = null;
        // work out the store name from current nodeRef
        String store = nodeRef.getStoreRef().getIdentifier();
        Map<QName, PropertyValue> props = getAVMService().getStoreProperties(store);
        List<Map<String, String>> storeProperties = new ArrayList<Map<String, String>>();

        for (Map.Entry<QName, PropertyValue> property : props.entrySet())
        {
            Map<String, String> map = new HashMap<String, String>(2);
            map.put("name", property.getKey().toString());
            map.put("type", property.getValue().getActualTypeString());
            String val = property.getValue().getStringValue();
            if (val == null)
            {
                val = "null";
            }
            map.put("value", val);

            storeProperties.add(map);
        }

        avmStoreProps = storeProperties;

        return avmStoreProps;
    }

    /**
     * Action to submit search
     * 
     * @return next action
     */
    public List<Node> submitSearch(final String store, final String query, final String queryLanguage) throws IOException
    {
    	final StoreRef storeRef = new StoreRef(store);
        RetryingTransactionCallback<List<Node>> searchCallback = new RetryingTransactionCallback<List<Node>>()
        {
            public List<Node> execute() throws Throwable
            {
            	List<Node> searchResults = null;
                if (queryLanguage.equals("noderef"))
                {
                    // ensure node exists
                    NodeRef nodeRef = new NodeRef(query);
                    boolean exists = getNodeService().exists(nodeRef);
                    if (!exists)
                    {
                        throw new WebScriptException(500, "Node " + nodeRef + " does not exist.");
                    }
                    searchResults = new ArrayList<Node>(1);
                    searchResults.add(new Node(nodeRef));
                    return searchResults;
                }

                // perform search
                List<NodeRef> nodeRefs = getSearchService().query(storeRef, queryLanguage, query).getNodeRefs();
                searchResults = new ArrayList<Node>(nodeRefs.size());
                for (NodeRef nodeRef : nodeRefs) {
                	searchResults.add(new Node(nodeRef));
				}
                return searchResults;
            }
        };

        try
        {
            return getTransactionService().getRetryingTransactionHelper().doInTransaction(searchCallback, true);
        }
        catch (Throwable e)
        {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
    	if (req.getPathInfo().equals("/slingshot/node/search"))
    	{
    		List<Node> nodes;
    		Map<String, Object> tmplMap = new HashMap<String, Object>(1);
			try
			{
				if (req.getParameter("store") == null || req.getParameter("store").length() == 0)
				{
					status.setCode(HttpServletResponse.SC_BAD_REQUEST);
					status.setMessage("Store name not provided");
					status.setRedirect(true);
					return null;
				}
				if (req.getParameter("q") == null || req.getParameter("q").length() == 0)
				{
					status.setCode(HttpServletResponse.SC_BAD_REQUEST);
					status.setMessage("Search query not provided");
					status.setRedirect(true);
					return null;
				}
				if (req.getParameter("lang") == null || req.getParameter("lang").length() == 0)
				{
					status.setCode(HttpServletResponse.SC_BAD_REQUEST);
					status.setMessage("Search language not provided");
					status.setRedirect(true);
					return null;
				}
				nodes = submitSearch(req.getParameter("store"), req.getParameter("q"), req.getParameter("lang"));
	    		tmplMap.put("results", nodes);
			}
			catch (IOException e)
			{
				status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				status.setMessage(e.getMessage());
				status.setException(e);
				status.setRedirect(true);
			}
    		return tmplMap;
    	}
    	else if (req.getPathInfo().equals("/slingshot/node/stores"))
    	{
    		Map<String, Object> model = new HashMap<String, Object>();
    		model.put("stores", getStores());
    		return model;
    	}
    	else // Assume we are looking for a node
    	{
     		Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
			if (templateVars.get("protocol") == null || templateVars.get("protocol").length() == 0 || 
					templateVars.get("store") == null || templateVars.get("store").length() == 0 ||
					templateVars.get("id") == null || templateVars.get("id").length() == 0)
			{
				status.setCode(HttpServletResponse.SC_BAD_REQUEST);
				status.setMessage("Node not provided");
				status.setRedirect(true);
				return null;
			}
        	NodeRef nodeRef = new NodeRef(templateVars.get("protocol"), templateVars.get("store"), templateVars.get("id"));
        	
    		Map<String, Object> permissionInfo = new HashMap<String, Object>(3);
    		permissionInfo.put("entries", getPermissions(nodeRef));
    		permissionInfo.put("owner", this.getOwnableService().getOwner(nodeRef));
    		permissionInfo.put("inherit", this.getInheritPermissions(nodeRef));
    		permissionInfo.put("entries", getPermissions(nodeRef));
    		permissionInfo.put("storePermissions", getStorePermissionMasks(nodeRef));

    		Map<String, Object> model = new HashMap<String, Object>();
    		model.put("node", new Node(nodeRef));
    		model.put("properties", getProperties(nodeRef));
    		model.put("aspects", getAspects(nodeRef));
    		model.put("children", getChildren(nodeRef));
    		model.put("parents", getParents(nodeRef));
    		model.put("assocs", getAssocs(nodeRef));
    		model.put("sourceAssocs", getSourceAssocs(nodeRef));
    		model.put("permissions", permissionInfo);
    		return model;
    	}
    }

    /**
     * Node wrapper class
     */
    public class Node
    {
        private String qnamePath;
        
        private String prefixedQNamePath;
        
        private NodeRef nodeRef;
        
        private NodeRef parentNodeRef;
        
        private QNameBean childAssoc;
        
        private QNameBean type;
        
        public Node(NodeRef nodeRef)
        {
        	this.nodeRef = nodeRef;
        	Path path = getNodeService().getPath(nodeRef);
        	this.qnamePath = path.toString();
        	this.prefixedQNamePath = path.toPrefixString(getNamespaceService());
        	this.parentNodeRef = getPrimaryParent(nodeRef);
        	ChildAssociationRef ref = getNodeService().getPrimaryParent(nodeRef);
        	this.childAssoc = ref.getQName() != null ? new QNameBean(ref.getQName()) : null;
        	this.type = new QNameBean(getNodeService().getType(nodeRef));
        }

		public String getQnamePath()
		{
			return qnamePath;
		}

		public String getPrefixedQNamePath()
		{
			return prefixedQNamePath;
		}

		public NodeRef getNodeRef()
		{
			return nodeRef;
		}

		public String getId()
		{
			return nodeRef.getId();
		}

		public String getName()
		{
			return childAssoc != null ? childAssoc.getName() : "";
		}

		public String getPrefixedName()
		{
			return childAssoc != null ? childAssoc.getPrefixedName() : "";
		}

		public QNameBean getType()
		{
			return type;
		}

		public void setNodeRef(NodeRef nodeRef)
		{
			this.nodeRef = nodeRef;
		}

		public NodeRef getParentNodeRef()
		{
			return parentNodeRef;
		}

		public void setParentNodeRef(NodeRef parentNodeRef)
		{
			this.parentNodeRef = parentNodeRef;
		}
    }

    /**
     * Qname wrapper class
     */
    public class QNameBean implements Serializable
    {
		private static final long serialVersionUID = 6982292337846270774L;
		
		protected QName name;

		public QNameBean(QName name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name.toString();
		}
		
		public String getPrefixedName()
		{
			return name.toPrefixString(getNamespaceService());
		}
		
		public String toString()
		{
			return getName();
		}
    }

    /**
     * Aspect wrapper class
     */
    public class Aspect extends QNameBean
    {
		private static final long serialVersionUID = -6448182941386934326L;

		public Aspect(QName name)
		{
			super(name);
		}
    }

    /**
     * Association wrapper class
     */
    public class Association
    {
    	protected QNameBean name;
    	protected QNameBean typeName;
		
		public Association(QName name, QName typeName)
		{
			this.name = name != null ? new QNameBean(name) : null;
			this.typeName = new QNameBean(typeName);
		}

		public QNameBean getName()
		{
			return name;
		}

		public QNameBean getTypeName()
		{
			return typeName;
		}
    }

    /**
     * Child assoc wrapper class
     */
    public class ChildAssociation extends Association implements Serializable
    {
    	/**
		 * 
		 */
		private static final long serialVersionUID = -52439282250891063L;
		
		protected NodeRef childRef;
		protected NodeRef parentRef;
		protected QNameBean childType;
		protected QNameBean parentType;
		protected boolean primary;
    	
    	// from Association
    	protected QNameBean name;
    	protected QNameBean typeName;

		public ChildAssociation(ChildAssociationRef ref)
		{
			super(ref.getQName() != null ? ref.getQName() : null,
					ref.getTypeQName() != null ? ref.getTypeQName() : null);
			
			this.childRef = ref.getChildRef();
			this.parentRef = ref.getParentRef(); // could be null
			if (childRef != null)
				this.childType = new QNameBean(getNodeType(childRef));
			if (parentRef != null)
				this.parentType = new QNameBean(getNodeType(parentRef));
			this.primary = ref.isPrimary();
		}

		public NodeRef getChildRef()
		{
			return childRef;
		}

		public QNameBean getChildTypeName()
		{
			return childType;
		}

		public NodeRef getParentRef()
		{
			return parentRef;
		}

		public QNameBean getParentTypeName()
		{
			return parentType;
		}

		public boolean isPrimary()
		{
			return primary;
		}

		public boolean getPrimary()
		{
			return this.isPrimary();
		}
    }

    /**
     * Peer assoc wrapper class
     */
    public class PeerAssociation extends Association
    {
    	protected NodeRef sourceRef;
    	protected NodeRef targetRef;
    	protected QNameBean sourceType;
    	protected QNameBean targetType;
    	
    	// from Association
    	protected QNameBean name;
    	protected QNameBean typeName;
    	
		public PeerAssociation(QName typeName, NodeRef sourceRef, NodeRef targetRef)
		{
			super(null, typeName);
			
			this.sourceRef = sourceRef;
			this.targetRef = targetRef;
			if (sourceRef != null)
				this.sourceType = new QNameBean(getNodeType(sourceRef));
			if (targetRef != null)
				this.targetType = new QNameBean(getNodeType(targetRef));
		}

		public NodeRef getSourceRef()
		{
			return sourceRef;
		}

		public QNameBean getSourceTypeName()
		{
			return sourceType;
		}

		public NodeRef getTargetRef()
		{
			return targetRef;
		}

		public QNameBean getTargetTypeName()
		{
			return targetType;
		}
    }

    /**
     * Property wrapper class
     */
    public class Property
    {
        private QNameBean name;

        private boolean isCollection = false;

        private List<Value> values;

        private boolean residual;
        
        private QNameBean typeName;

        /**
         * Construct
         * 
         * @param name property name
         * @param value property values
         */
        @SuppressWarnings("unchecked")
        public Property(QName qname, Serializable value)
        {
            this.name = new QNameBean(qname);

            PropertyDefinition propDef = getDictionaryService().getProperty(qname);
            if (propDef != null)
            {
            	QName qn = propDef.getDataType().getName();
                typeName = qn != null ? new QNameBean(propDef.getDataType().getName()) : null;
                residual = false;
            }
            else
            {
                residual = true;
            }

            // handle multi/single values
            final List<Value> values;
            if (value instanceof Collection)
            {
                Collection<Serializable> oldValues = (Collection<Serializable>) value;
                values = new ArrayList<Value>(oldValues.size());
                isCollection = true;
                for (Serializable multiValue : oldValues)
                {
                    values.add(new Value(multiValue instanceof QName ? new QNameBean((QName) multiValue) : multiValue));
                }
            }
            else
            {
                values = Collections.singletonList(new Value(value instanceof QName ? new QNameBean((QName) value) : value));
            }
            this.values = values;
        }

		/**
         * Gets the property name
         * 
         * @return name
         */
        public QNameBean getName()
        {
			return name;
        }

        public QNameBean getTypeName()
        {
			return typeName;
		}

        /**
         * Gets the prefixed property name
         * 
         * @return prefixed name
         */
        public String getPrefixedName()
        {
			return name.getPrefixedName();
        }

        /**
         * Gets the property value
         * 
         * @return value
         */
        public List<Value> getValues()
        {
            return values;
        }

        /**
         * Determines whether the property is residual
         * 
         * @return true => property is not defined in dictionary
         */
        public boolean getResidual()
        {
            return residual;
        }

        /**
         * Determines whether the property is of ANY type
         * 
         * @return true => is any
         */
        public boolean isAny()
        {
            return (getTypeName() == null) ? false : getTypeName().getName().equals(DataTypeDefinition.ANY.toString());
        }

        /**
         * Determines whether the property is a collection
         * 
         * @return true => is collection
         */
        public boolean isCollection()
        {
            return isCollection;
        }

        /**
         * Value wrapper
         */
        public class Value
        {
            private Serializable value;

            /**
             * Construct
             * 
             * @param value value
             */
            public Value(Serializable value)
            {
                this.value = value;
            }

            /**
             * Gets the value
             * 
             * @return the value
             */
            public Serializable getValue()
            {
                return value;
            }

            /**
             * Gets the value datatype
             * 
             * @return the value datatype
             */
            public String getDataType()
            {
                String datatype = Property.this.getTypeName().getName();
                if (datatype == null || datatype.equals(DataTypeDefinition.ANY.toString()))
                {
                    if (value != null)
                    {
                        DataTypeDefinition dataTypeDefinition = getDictionaryService().getDataType(value.getClass());
                        if (dataTypeDefinition != null)
                        {
                            datatype = getDictionaryService().getDataType(value.getClass()).getName().toString();
                        }
                    }
                }
                return datatype;
            }

            /**
             * Determines whether the value is content
             * 
             * @return true => is content
             */
            public boolean isContent()
            {
                String datatype = getDataType();
                return (datatype == null) ? false : datatype.equals(DataTypeDefinition.CONTENT.toString());
            }

            /**
             * Determines whether the value is a node ref
             * 
             * @return true => is node ref
             */
            public boolean isNodeRef()
            {
                String datatype = getDataType();
                return (datatype == null) ? false : datatype.equals(DataTypeDefinition.NODE_REF.toString()) || datatype.equals(DataTypeDefinition.CATEGORY.toString());
            }

            /**
             * Determines whether the value is null
             * 
             * @return true => value is null
             */
            public boolean isNullValue()
            {
                return value == null;
            }
        }
    }

    /**
     * Permission bean
     */
    public static class Permission
    {
    	private String permission;
    	private String authority;
    	private String accessStatus;
    	
		public Permission(String permission, String authority, String accessStatus)
		{
			this.permission = permission;
			this.authority = authority;
			this.accessStatus = accessStatus;
		}

		public String getPermission()
		{
			return permission;
		}
		
		public void setPermission(String permission)
		{
			this.permission = permission;
		}
		
		public String getAuthority()
		{
			return authority;
		}
		
		public void setAuthority(String authority)
		{
			this.authority = authority;
		}
		
		public String getAccessStatus()
		{
			return accessStatus;
		}
		
		public void setAccessStatus(String accessStatus)
		{
			this.accessStatus = accessStatus;
		}
    }

    /**
     * Permission representing the fact that "Read Permissions" has not been granted
     */
    public static class NoReadPermissionGranted extends Permission
    {
        public NoReadPermissionGranted()
        {
            super(PermissionService.READ_PERMISSIONS, "[Current Authority]", "Not Granted");
        }
    }

    public static class NoStoreMask extends Permission
    {
        public NoStoreMask()
        {
            super("All <No Mask>", "All", "Allowed");
        }
    }

}
