/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.template;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.admin.SysAdminParamsImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import freemarker.ext.dom.NodeModel;

/**
 * Node class specific for use by Template pages that support Bean objects as part of the model.
 * The default template engine FreeMarker can use these objects and they are provided to support it.
 * A single method is completely freemarker specific - getXmlNodeModel()
 * <p>
 * The class exposes Node properties, children as dynamically populated maps and lists.
 * <p>
 * Various helper methods are provided to access common and useful node variables such
 * as the content url and type information. 
 * <p>
 * See {@link http://wiki.alfresco.com/wiki/Template_Guide}
 * 
 * @author Kevin Roast
 */
public class TemplateNode extends BasePermissionsNode implements NamespacePrefixResolverProvider
{
    private static final long serialVersionUID = 1234390333739034171L;
    
    private static Log logger = LogFactory.getLog(TemplateNode.class);
    
    /** Target associations from this node */
    private Map<String, List<TemplateNode>> targetAssocs = null;
    
    /** Source associations to this node */
    private Map<String, List<TemplateNode>> sourceAssocs = null;
    
    /** The child associations from this node */
    private Map<String, List<TemplateNode>> childAssocs = null;

    /** Cached values */
    protected NodeRef nodeRef;
    private String name;
    private QName type;
    private String path;
    private String id;
    private QNameMap<String, Serializable> properties;
    private boolean propsRetrieved = false;
    private TemplateNode parent = null;
    private ChildAssociationRef primaryParentAssoc = null;
    private Boolean isCategory = null;
    
    private PropertyConverter propertyConverter = new TemplatePropertyConverter();
    
    
    // ------------------------------------------------------------------------------
    // Construction 
    
    /**
     * Constructor
     * 
     * @param nodeRef       The NodeRef this Node wrapper represents
     * @param services      The ServiceRegistry the TemplateNode can use to access services
     * @param resolver      Image resolver to use to retrieve icons
     */
    public TemplateNode(NodeRef nodeRef, ServiceRegistry services, TemplateImageResolver resolver)
    {
        if (nodeRef == null)
        {
            throw new IllegalArgumentException("NodeRef must be supplied.");
        }
      
        if (services == null)
        {
            throw new IllegalArgumentException("The ServiceRegistry must be supplied.");
        }
        
        this.nodeRef = nodeRef;
        this.id = nodeRef.getId();
        this.services = services;
        this.imageResolver = resolver;
        
        this.properties = new QNameMap<String, Serializable>(this);
    }
    
    
    // ------------------------------------------------------------------------------
    // TemplateNodeRef contract implementation
    
    /**
     * @return The GUID for the node
     */
    public String getId()
    {
        return this.id;
    }
    
    /**
     * @return  the store type for the node
     */
    public String getStoreType()
    {
        return this.nodeRef.getStoreRef().getProtocol();
    }
    
    /**
     * @return  the store id for the node
     */
    public String getStoreId()
    {
        return this.nodeRef.getStoreRef().getIdentifier();
    }
    
    /**
     * @return Returns the NodeRef this Node object represents
     */
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }
    
    /**
     * @return <code>true</code> if this node still exists
     */
    public boolean getExists()
    {
        return this.services.getNodeService().exists(this.nodeRef);
    }
    
    /**
     * @return <code>true</code> if this node is a working copy
     */
    public boolean getIsWorkingCopy()
    {
        return this.services.getNodeService().hasAspect(this.nodeRef, ContentModel.ASPECT_WORKING_COPY);
    }

    /**
     * @return Returns the type.
     */
    public QName getType()
    {
        if (this.type == null)
        {
            this.type = this.services.getNodeService().getType(this.nodeRef);
        }
        
        return type;
    }

    /**
     * @return Returns the type in short format.
     */
    public String getTypeShort()
    {
        return this.getType().toPrefixString(this.services.getNamespaceService());
    }

    /**
     * @return The display name for the node
     */
    public String getName()
    {
        if (this.name == null)
        {
            // try and get the name from the properties first
            this.name = (String)getProperties().get("cm:name");
            
            // if we didn't find it as a property get the name from the association name
            if (this.name == null)
            {
                ChildAssociationRef parentRef = this.services.getNodeService().getPrimaryParent(this.nodeRef);
                if (parentRef != null && parentRef.getQName() != null)
                {
                    this.name = parentRef.getQName().getLocalName();
                }
                else
                {
                    this.name = "";
                }
            }
        }
        
        return this.name;
    }
    
    
    // ------------------------------------------------------------------------------
    // TemplateProperties contract implementation
    
    /**
     * @return All the properties known about this node.
     */
    public Map<String, Serializable> getProperties()
    {
        if (this.propsRetrieved == false)
        {
            Map<QName, Serializable> props = this.services.getNodeService().getProperties(this.nodeRef);
            
            for (QName qname : props.keySet())
            {
                Serializable value = this.propertyConverter.convertProperty(
                        props.get(qname), qname, this.services, getImageResolver());
                this.properties.put(qname.toString(), value);
            }
            
            this.propsRetrieved = true;
        }
        
        return this.properties;
    }
    
    
    // ------------------------------------------------------------------------------
    // Repository Node API
    
    /**
     * @return Target associations for this Node. As a Map of assoc name to a List of TemplateNodes. 
     */
    public Map<String, List<TemplateNode>> getAssocs()
    {
        if (this.targetAssocs == null)
        {
            List<AssociationRef> refs = this.services.getNodeService().getTargetAssocs(this.nodeRef, RegexQNamePattern.MATCH_ALL);
            this.targetAssocs = new QNameMap<String, List<TemplateNode>>(this);
            for (AssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<TemplateNode> nodes = this.targetAssocs.get(qname);
                if (nodes == null)
                {
                    // first access for the list for this qname
                    nodes = new ArrayList<TemplateNode>(4);
                    this.targetAssocs.put(ref.getTypeQName().toString(), nodes);
                }
                nodes.add( new TemplateNode(ref.getTargetRef(), this.services, this.imageResolver) );
            }
        }
        
        return this.targetAssocs;
    }
    
    public Map<String, List<TemplateNode>> getAssociations()
    {
        return getAssocs();
    }
    
    /**
     * @return Source associations for this Node. As a Map of assoc name to a List of TemplateNodes. 
     */
    public Map<String, List<TemplateNode>> getSourceAssocs()
    {
        if (this.sourceAssocs == null)
        {
            List<AssociationRef> refs = this.services.getNodeService().getSourceAssocs(this.nodeRef, RegexQNamePattern.MATCH_ALL);
            this.sourceAssocs = new QNameMap<String, List<TemplateNode>>(this);
            for (AssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<TemplateNode> nodes = this.sourceAssocs.get(qname);
                if (nodes == null)
                {
                    // first access for the list for this qname
                    nodes = new ArrayList<TemplateNode>(4);
                    this.sourceAssocs.put(ref.getTypeQName().toString(), nodes);
                }
                nodes.add( new TemplateNode(ref.getSourceRef(), this.services, this.imageResolver) );
            }
        }
        
        return this.sourceAssocs;
    }
    
    public Map<String, List<TemplateNode>> getSourceAssociations()
    {
        return getSourceAssocs();
    }
    
    /**
     * @return The child associations for this Node. As a Map of assoc name to a List of TemplateNodes. 
     */
    public Map<String, List<TemplateNode>> getChildAssocs()
    {
        if (this.childAssocs == null)
        {
            List<ChildAssociationRef> refs = this.services.getNodeService().getChildAssocs(this.nodeRef);
            this.childAssocs = new QNameMap<String, List<TemplateNode>>(this);
            for (ChildAssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<TemplateNode> nodes = this.childAssocs.get(qname);
                if (nodes == null)
                {
                    // first access for the list for this qname
                    nodes = new ArrayList<TemplateNode>(4);
                    this.childAssocs.put(ref.getTypeQName().toString(), nodes);
                }
                nodes.add( new TemplateNode(ref.getChildRef(), this.services, this.imageResolver) );
            }
        }
        
        return this.childAssocs;
    }
    
    public Map<String, List<TemplateNode>> getChildAssociations()
    {
        return getChildAssocs();
    }
    
    /**
     * @return The list of children of this Node that match a specific object type.
     */
    public List<TemplateNode> getChildAssocsByType(String type)
    {
        Set<QName> types = new HashSet<QName>(1, 1.0f);
        types.add(createQName(type));
        List<ChildAssociationRef> refs = this.services.getNodeService().getChildAssocs(this.nodeRef, types);
        List<TemplateNode> nodes = new ArrayList<TemplateNode>(refs.size());
        for (ChildAssociationRef ref : refs)
        {
            String qname = ref.getTypeQName().toString();
            nodes.add( new TemplateNode(ref.getChildRef(), this.services, this.imageResolver) );
        }
        return nodes;
    }
    
    /**
     * @return true if the node is currently locked
     */
    public boolean getIsLocked()
    {
        boolean locked = false;
        
        if (getAspects().contains(ContentModel.ASPECT_LOCKABLE))
        {
            LockStatus lockStatus = this.services.getLockService().getLockStatus(this.nodeRef);
            if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER)
            {
                locked = true;
            }
        }
        
        return locked;
    }
    
    /**
     * @return true if the node is a Category instance
     */
    public boolean getIsCategory()
    {
        if (isCategory == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isCategory = Boolean.valueOf(dd.isSubClass(getType(), ContentModel.TYPE_CATEGORY));
        }

        return isCategory.booleanValue();
    }
    
    /**
     * @return the primary parent node
     */
    public TemplateProperties getParent()
    {
        if (parent == null)
        {
            NodeRef parentRef = this.services.getNodeService().getPrimaryParent(nodeRef).getParentRef();
            // handle root node (no parent!)
            if (parentRef != null)
            {
                parent = new TemplateNode(parentRef, this.services, this.imageResolver);
            }
        }
        
        return parent;
    }
    
    /**
     * @return the primary parent association so we can access the association QName and association type QName.
     */
    public ChildAssociationRef getPrimaryParentAssoc()
    {
        if (primaryParentAssoc == null)
        {
            primaryParentAssoc = this.services.getNodeService().getPrimaryParent(nodeRef);
        }
        return primaryParentAssoc;
    }
    
    /**
     * @return a list of objects representing the version history of this node.
     *         @see VersionHistoryNode
     */
    public List<VersionHistoryNode> getVersionHistory()
    {
        List<VersionHistoryNode> records = Collections.<VersionHistoryNode>emptyList();
        
        if (this.getAspects().contains(ContentModel.ASPECT_VERSIONABLE))
        {
            VersionHistory history = this.services.getVersionService().getVersionHistory(this.nodeRef);
            if (history != null)
            {
                records = new ArrayList<VersionHistoryNode>(8);
                for (Version version : history.getAllVersions())
                {
                    // create a wrapper for the version information
                    VersionHistoryNode record = new VersionHistoryNode(version, this, this.services);
                    
                    // add the client side version to the list
                    records.add(record);
                }
            }
        }
        
        return records;
    }
    
    
    // ------------------------------------------------------------------------------
    // Node Helper API 
    
    /**
     * @return FreeMarker NodeModel for the XML content of this node, or null if no parsable XML found
     */
    public NodeModel getXmlNodeModel()
    {
        try
        {
            return NodeModel.parse(new InputSource(new StringReader(getContent())));
        }
        catch (Throwable err)
        {
            if (logger.isDebugEnabled())
                logger.debug(err.getMessage(), err);
            
            return null;
        }
    }
    
    
    // ------------------------------------------------------------------------------
    // Search API
    
    /**
     * @return A map capable of returning the TemplateNode at the specified Path as a child of this node.
     */
    public Map getChildByNamePath()
    {
        return new NamePathResultsMap(this, this.services);
    }
    
    /**
     * @return A map capable of returning a List of TemplateNode objects from an XPath query
     *         as children of this node.
     */
    public Map getChildrenByXPath()
    {
        return new XPathResultsMap(this, this.services);
    }
    
    /**
     * @return A map capable of returning a List of TemplateNode objects from an NodeRef to a Saved Search
     *         object. The Saved Search is executed and the resulting nodes supplied as a sequence.
     */
    public Map getChildrenBySavedSearch()
    {
        return new SavedSearchResultsMap(this, this.services);
    }
    
    /**
     * @return A map capable of returning a List of TemplateNode objects from an NodeRef to a Lucene search
     *         string. The Saved Search is executed and the resulting nodes supplied as a sequence.
     */
    public Map getChildrenByLuceneSearch()
    {
        return new LuceneSearchResultsMap(this, this.services);
    }
    
    /**
     * @return A map capable of returning a TemplateNode for a single specified NodeRef reference.
     */
    public Map getNodeByReference()
    {
        return new NodeSearchResultsMap(this, this.services);
    }
    
    // ------------------------------------------------------------------------------
    // Audit API
    
    /**
     * @return a list of AuditInfo objects describing the Audit Trail for this node instance
     */
    public List<TemplateAuditInfo> getAuditTrail()
    {
        final List<TemplateAuditInfo> result = new ArrayList<TemplateAuditInfo>();
        
        // create the callback for auditQuery method
        final AuditQueryCallback callback = new AuditQueryCallback()
        {
            public boolean valuesRequired()
            {
                return true;
            }

            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve audit data.", error);
            }

            public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time,
                    Map<String, Serializable> values)
            {
                TemplateAuditInfo auditInfo = new TemplateAuditInfo(applicationName, user, time, values);
                result.add(auditInfo);
                return true;
            }
        };

        // resolve the path of the node 
        final String nodePath = services.getNodeService().getPath(this.nodeRef).toPrefixString(services.getNamespaceService());

        // run as admin user to allow everyone to see audit information
        // (new 3.4 API doesn't allow this by default)
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                String applicationName = "alfresco-access";
                AuditQueryParameters pathParams = new AuditQueryParameters();
                pathParams.setApplicationName(applicationName);
                pathParams.addSearchKey("/alfresco-access/transaction/path", nodePath);
                services.getAuditService().auditQuery(callback, pathParams, -1);
                
                AuditQueryParameters copyFromPathParams = new AuditQueryParameters();
                copyFromPathParams.setApplicationName(applicationName);
                copyFromPathParams.addSearchKey("/alfresco-access/transaction/copy/from/path", nodePath);
                services.getAuditService().auditQuery(callback, copyFromPathParams, -1);
                
                AuditQueryParameters moveFromPathParams = new AuditQueryParameters();
                moveFromPathParams.setApplicationName(applicationName);
                moveFromPathParams.addSearchKey("/alfresco-access/transaction/move/from/path", nodePath);
                services.getAuditService().auditQuery(callback, moveFromPathParams, -1);
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
        
        // sort audit entries by time of generation
        Collections.sort(result, new Comparator<TemplateAuditInfo>()
        {
            public int compare(TemplateAuditInfo o1, TemplateAuditInfo o2)
            {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        return result;
    }
    
    // ------------------------------------------------------------------------------
    // Misc helpers 
    
    /**
     * @return the image resolver instance used by this node
     */
    public TemplateImageResolver getImageResolver()
    {
        return this.imageResolver;
    }
    
    /**
     * Helper to create a QName from either a fully qualified or short-name QName string
     * 
     * @param s    Fully qualified or short-name QName string
     * 
     * @return QName
     */
    private QName createQName(String s)
    {
        QName qname;
        if (s.indexOf(NAMESPACE_BEGIN) != -1)
        {
            qname = QName.createQName(s);
        }
        else
        {
            qname = QName.createQName(s, this.services.getNamespaceService());
        }
        return qname;
    }
    
    /**
     * This method returns a URL string which resolves to an Alfresco Share view of this node.
     * Note that in order for this method to return meaningful data, the {@link SysAdminParams sysAdminParams}
     * bean must have been configured.
     * <p/>
     * Currently this method only produces valid URls for documents and not for folders.
     * @see SysAdminParamsImpl#setAlfrescoHost(String)
     * @see SysAdminParamsImpl#setShareHost(String)
     */
    public String getShareUrl()
    {
        // TODO URLs for the repo server.
        // TODO URLs for folders
        
        SiteInfo siteInfo = services.getSiteService().getSite(getNodeRef());
        String siteShortName = siteInfo == null ? null : siteInfo.getShortName();
        
        String baseUrl = UrlUtil.getShareUrl(services.getSysAdminParams());
        
        StringBuilder result = new StringBuilder();
        result.append(baseUrl)
              .append("/page/");
        if (siteShortName != null)
        {
            result.append("site/").append(siteShortName).append("/");
        }
        
        result.append("document-details?nodeRef=")
              .append(getNodeRef());
        
        return result.toString();
    }
    
    
    // ------------------------------------------------------------------------------
    // Inner classes
    
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return this.services.getNamespaceService();
    }

    /**
     * Class to convert properties into template accessable objects
     */
    class TemplatePropertyConverter extends PropertyConverter
    {
        @Override
        public Serializable convertProperty(
                Serializable value, QName name, ServiceRegistry services, TemplateImageResolver resolver)
        {
            if (value instanceof ContentData)
            {
                // ContentData object properties are converted to TemplateContentData objects
                // so the content and other properties of those objects can be accessed
                return new TemplateContentData((ContentData)value, name);
            }
            else
            {
                return super.convertProperty(value, name, services, resolver);
            }
        }
    }
    
    public class TemplateAuditInfo
    {
        private String applicationName;
        private String userName;
        private long time;
        private Map<String, Serializable> values;

        public TemplateAuditInfo(String applicationName, String userName, long time, Map<String, Serializable> values)
        {
            this.applicationName = applicationName;
            this.userName = userName;
            this.time = time;
            this.values = values;
        }

        public String getAuditApplication()
        {
            return this.applicationName;
        }

        public String getUserIdentifier()
        {
            return this.userName;
        }

        public Date getDate()
        {
            return new Date(time);
        }

        public String getAuditMethod()
        {
            return this.values.get("/alfresco-access/transaction/action").toString();
        }
        
        public Map<String, Serializable> getValues()
        {
            return this.values;
        }
    }
}