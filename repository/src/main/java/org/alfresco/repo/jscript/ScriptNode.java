/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.jscript;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.Wrapper;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.extensions.surf.util.URLEncoder;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.UnimportantTransformException;
import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl.InvalidTypeException;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.rendition2.RenditionDefinition2;
import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2;
import org.alfresco.repo.rendition2.RenditionService2;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tagging.script.TagScope;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailHelper;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.thumbnail.script.ScriptThumbnail;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.workflow.jscript.JscriptWorkflowInstance;
import org.alfresco.scripts.ScriptException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;

/**
 * Script Node class implementation, specific for use by ScriptService as part of the object model.
 * <p>
 * The class exposes Node properties, children and assocs as dynamically populated maps and lists. The various collection classes are mirrored as JavaScript properties. So can be accessed using standard JavaScript property syntax, such as <code>node.children[0].properties.name</code>.
 * <p>
 * Various helper methods are provided to access common and useful node variables such as the content url and type information.
 * 
 * @author Kevin Roast
 */
public class ScriptNode implements Scopeable, NamespacePrefixResolverProvider
{
    private static final long serialVersionUID = -3378946227712939601L;

    private static Log logger = LogFactory.getLog(ScriptNode.class);

    private final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;

    private final static String CONTENT_DEFAULT_URL = "/d/d/{0}/{1}/{2}/{3}";
    private final static String CONTENT_DOWNLOAD_URL = "/d/a/{0}/{1}/{2}/{3}";
    private final static String CONTENT_PROP_URL = "/d/d/{0}/{1}/{2}/{3}?property={4}";
    private final static String CONTENT_DOWNLOAD_PROP_URL = "/d/a/{0}/{1}/{2}/{3}?property={4}";
    private final static String FOLDER_BROWSE_URL = "/n/browse/{0}/{1}/{2}";

    /** Root scope for this object */
    protected Scriptable scope;

    /** Node Value Converter */
    protected NodeValueConverter converter = null;

    /** Cached values */
    protected NodeRef nodeRef;

    private FileInfo nodeInfo;

    private String name;
    private QName type;
    protected String id;
    protected String siteName;
    protected boolean siteNameResolved = false;

    /** The aspects applied to this node */
    protected Set<QName> aspects = null;

    /** The target associations from this node */
    private ScriptableQNameMap<String, Object> targetAssocs = null;

    /** The source associations to this node */
    private ScriptableQNameMap<String, Object> sourceAssocs = null;

    /** The child associations for this node */
    private ScriptableQNameMap<String, Object> childAssocs = null;

    /** The children of this node */
    private Scriptable children = null;

    /** The properties of this node */
    private ScriptableQNameMap<String, Serializable> properties = null;

    /** The versions of this node */
    private Scriptable versions = null;

    /** The active workflows acting on this node */
    private Scriptable activeWorkflows = null;

    protected ServiceRegistry services = null;
    private NodeService nodeService = null;
    private FileFolderService fileFolderService = null;
    private RenditionService2 renditionService2;
    private RenditionDefinitionRegistry2 renditionDefinitionRegistry2;
    private SynchronousTransformClient synchronousTransformClient;
    private RetryingTransactionHelper retryingTransactionHelper = null;
    private Boolean isDocument = null;
    private Boolean isContainer = null;
    private Boolean isLinkToDocument = null;
    private Boolean isLinkToContainer = null;
    private Boolean hasChildren = null;
    private String displayPath = null;
    private String qnamePath = null;
    protected TemplateImageResolver imageResolver = null;
    protected ScriptNode parent = null;
    private ChildAssociationRef primaryParentAssoc = null;
    private ScriptableQNameMap<String, Object> parentAssocs = null;
    // NOTE: see the reset() method when adding new cached members!

    // ------------------------------------------------------------------------------
    // Construction

    /**
     * Constructor
     * 
     * @param nodeRef
     *            The NodeRef this Node wrapper represents
     * @param services
     *            The ServiceRegistry the Node can use to access services
     */
    public ScriptNode(NodeRef nodeRef, ServiceRegistry services)
    {
        this(nodeRef, services, null);
    }

    /**
     * Constructor
     * 
     * @param nodeInfo
     *            The FileInfo this Node wrapper represents
     * @param services
     *            The ServiceRegistry the Node can use to access services
     * @param scope
     *            Root scope for this Node
     */
    public ScriptNode(FileInfo nodeInfo, ServiceRegistry services, Scriptable scope)
    {
        this(nodeInfo.getNodeRef(), services, scope);

        this.nodeInfo = nodeInfo;
    }

    /**
     * Constructor
     * 
     * @param nodeRef
     *            The NodeRef this Node wrapper represents
     * @param services
     *            The ServiceRegistry the Node can use to access services
     * @param scope
     *            Root scope for this Node
     */
    public ScriptNode(NodeRef nodeRef, ServiceRegistry services, Scriptable scope)
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
        this.nodeService = services.getNodeService();
        this.fileFolderService = services.getFileFolderService();
        this.retryingTransactionHelper = services.getTransactionService().getRetryingTransactionHelper();
        this.scope = scope;
        renditionService2 = services.getRenditionService2();
        renditionDefinitionRegistry2 = renditionService2.getRenditionDefinitionRegistry2();
        synchronousTransformClient = services.getSynchronousTransformClient();
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (!nodeRef.equals(((ScriptNode) obj).nodeRef))
            return false;
        return true;
    }

    /**
     * Factory method
     */
    public ScriptNode newInstance(NodeRef nodeRef, ServiceRegistry services, Scriptable scope)
    {
        return new ScriptNode(nodeRef, services, scope);
    }

    public ScriptNode newInstance(FileInfo nodeInfo, ServiceRegistry services, Scriptable scope)
    {
        return new ScriptNode(nodeInfo, services, scope);
    }

    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }

    // ------------------------------------------------------------------------------
    // Node Wrapper API

    /**
     * @return The GUID for the node
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return the store type for the node
     */
    public String getStoreType()
    {
        return this.nodeRef.getStoreRef().getProtocol();
    }

    /**
     * @return the store id for the node
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
     * @return Returns the QName type.
     */
    public QName getQNameType()
    {
        if (this.type == null)
        {
            this.type = this.nodeService.getType(this.nodeRef);
        }

        return type;
    }

    /**
     * @return Returns the type.
     */
    public String getType()
    {
        return getQNameType().toString();
    }

    /**
     * @return Returns the type in short format.
     */
    public String getTypeShort()
    {
        return this.getShortQName(getQNameType());
    }

    /**
     * @return Helper to return the 'name' property for the node
     */
    public String getName()
    {
        if (this.name == null)
        {
            // try and get the name from the properties first
            this.name = (String) getProperties().get("cm:name");

            // if we didn't find it as a property get the name from the association name
            if (this.name == null)
            {
                ChildAssociationRef parentRef = this.nodeService.getPrimaryParent(this.nodeRef);
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

    /**
     * Helper to set the 'name' property for the node.
     * 
     * @param name
     *            Name to set
     */
    public void setName(String name)
    {
        if (name != null)
        {
            QName typeQName = getQNameType();
            if ((services.getDictionaryService().isSubClass(typeQName, ContentModel.TYPE_FOLDER) &&
                    !services.getDictionaryService().isSubClass(typeQName, ContentModel.TYPE_SYSTEM_FOLDER)) ||
                    services.getDictionaryService().isSubClass(typeQName, ContentModel.TYPE_CONTENT))
            {
                try
                {
                    this.services.getFileFolderService().rename(this.nodeRef, name);
                }
                catch (FileNotFoundException e)
                {
                    throw new AlfrescoRuntimeException("Failed to rename node " + nodeRef + " to " + name, e);
                }
            }
            this.getProperties().put(ContentModel.PROP_NAME.toString(), name.toString());
        }
    }

    /**
     * @return The children of this Node as JavaScript array of Node object wrappers
     */
    public Scriptable getChildren()
    {
        if (this.children == null)
        {
            List<ChildAssociationRef> childRefs = this.nodeService.getChildAssocs(this.nodeRef);
            Object[] children = new Object[childRefs.size()];
            for (int i = 0; i < childRefs.size(); i++)
            {
                // create our Node representation from the NodeRef
                children[i] = newInstance(childRefs.get(i).getChildRef(), this.services, this.scope);
            }

            // Do a locale-sensitive sort by name
            sort(children);

            this.children = Context.getCurrentContext().newArray(this.scope, children);
            this.hasChildren = (children.length != 0);
        }

        return this.children;
    }

    /**
     * Performs a locale-sensitive sort by name of a node array
     * 
     * @param nodes
     *            the node array
     */
    private static void sort(Object[] nodes)
    {
        final Collator col = Collator.getInstance(I18NUtil.getLocale());
        Arrays.sort(nodes, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2)
            {
                return col.compare(((ScriptNode) o1).getName(), ((ScriptNode) o2).getName());
            }
        });
    }

    /**
     * @return true if the Node has children
     */
    public boolean getHasChildren()
    {
        if (this.hasChildren == null)
        {
            this.hasChildren = !this.services.getNodeService().getChildAssocs(
                    getNodeRef(), RegexQNamePattern.MATCH_ALL, RegexQNamePattern.MATCH_ALL, false).isEmpty();
        }
        return hasChildren;
    }

    /**
     * childByNamePath returns the Node at the specified 'cm:name' based Path walking the children of this Node. So a valid call might be: <code>mynode.childByNamePath("/QA/Testing/Docs");</code>
     * 
     * @param path
     *            the relative path of the descendant node to find e.g. {@code "/QA/Testing/Docs"}
     * @return The ScriptNode or {@code null} if the node is not found. {@code null} if the specified path is {@code ""}.
     * @throws NullPointerException
     *             if the provided path is {@code null}.
     */
    public ScriptNode childByNamePath(String path)
    {
        // Ensure that paths that do not represent descendants are not needlessly tokenised. See ALF-20896.
        if (path == null)
        {
            throw new NullPointerException("Illegal null path");
        }
        else if (path.isEmpty())
        {
            return null;
        }

        // We have a path worth looking at...
        ScriptNode child = null;

        if (this.services.getDictionaryService().isSubClass(getQNameType(), ContentModel.TYPE_FOLDER))
        {
            // The current node is a folder e.g. Company Home and standard child folders.
            // optimized code path for cm:folder and sub-types supporting getChildrenByName() method
            final StringTokenizer t = new StringTokenizer(path, "/");
            // allow traversal of a cm:name based path even if user cannot retrieve the node directly
            NodeRef result = AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
                @Override
                public NodeRef doWork() throws Exception
                {
                    NodeRef child = ScriptNode.this.nodeRef;
                    while (t.hasMoreTokens() && child != null)
                    {
                        String name = t.nextToken();
                        child = nodeService.getChildByName(child, ContentModel.ASSOC_CONTAINS, name);
                    }
                    return child;
                }
            }, AuthenticationUtil.getSystemUserName());

            // final node must be accessible to the user via the usual ACL permission checks
            if (result != null
                    && services.getPublicServiceAccessService().hasAccess("NodeService", "getProperties", result) != AccessStatus.ALLOWED)
            {
                result = null;
            }

            child = (result != null ? newInstance(result, this.services, this.scope) : null);
        }
        else
        {
            // The current node is not a folder. It does not support the cm:contains association.
            // Convert the name based path to a valid XPath query
            StringBuilder xpath = new StringBuilder(path.length() << 1);
            StringTokenizer t = new StringTokenizer(path, "/");
            int count = 0;
            QueryParameterDefinition[] params = new QueryParameterDefinition[t.countTokens()];
            DataTypeDefinition ddText = this.services.getDictionaryService().getDataType(DataTypeDefinition.TEXT);
            NamespaceService ns = this.services.getNamespaceService();
            while (t.hasMoreTokens())
            {
                if (xpath.length() != 0)
                {
                    xpath.append('/');
                }
                String strCount = Integer.toString(count);
                xpath.append("*[@cm:name=$cm:name")
                        .append(strCount)
                        .append(']');
                params[count++] = new QueryParameterDefImpl(
                        QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "name" + strCount, ns),
                        ddText,
                        true,
                        t.nextToken());
            }

            Object[] nodes = getChildrenByXPath(xpath.toString(), params, true);

            child = (nodes.length != 0) ? (ScriptNode) nodes[0] : null;
        }

        return child;
    }

    /**
     * @return Returns a JavaScript array of Nodes at the specified XPath starting at this Node. So a valid call might be <code>mynode.childrenByXPath("*[@cm:name='Testing']/*");</code>
     */
    public Scriptable childrenByXPath(String xpath)
    {
        return Context.getCurrentContext().newArray(this.scope, getChildrenByXPath(xpath, null, false));
    }

    /**
     * @return Returns a JavaScript array of child file/folder nodes for this nodes. Automatically retrieves all sub-types of cm:content and cm:folder, also removes system folder types from the results. This is equivalent to @see FileFolderService.list()
     */
    public Scriptable childFileFolders()
    {
        return childFileFolders(true, true, null);
    }

    /**
     * @param files
     *            Return files extending from cm:content
     * @param folders
     *            Return folders extending from cm:folder - ignoring sub-types of cm:systemfolder
     * 
     * @return Returns a JavaScript array of child file/folder nodes for this nodes. Automatically retrieves all sub-types of cm:content and cm:folder, also removes system folder types from the results. This is equivalent to @see FileFolderService.listFiles() and @see FileFolderService.listFolders()
     */
    public Scriptable childFileFolders(boolean files, boolean folders)
    {
        return childFileFolders(files, folders, null);
    }

    /**
     * @param files
     *            Return files extending from cm:content
     * @param folders
     *            Return folders extending from cm:folder - ignoring sub-types of cm:systemfolder
     * @param ignoreTypes
     *            Also optionally removes additional type qnames. The additional type can be specified in short or long qname string form as a single string or an Array e.g. "fm:forum".
     * 
     * @return Returns a JavaScript array of child file/folder nodes for this nodes. Automatically retrieves all sub-types of cm:content and cm:folder, also removes system folder types from the results. This is equivalent to @see FileFolderService.listFiles() and @see FileFolderService.listFolders()
     */
    public Scriptable childFileFolders(boolean files, boolean folders, Object ignoreTypes)
    {
        return childFileFolders(files, folders, ignoreTypes, -1, -1, 0, null, null, null).getPage();
    }

    /**
     * @param files
     *            Return files extending from cm:content
     * @param folders
     *            Return folders extending from cm:folder - ignoring sub-types of cm:systemfolder
     * @param ignoreTypes
     *            Also optionally removes additional type qnames. The additional type can be specified in short or long qname string form as a single string or an Array e.g. "fm:forum".
     * @param maxItems
     *            Max number of items
     * 
     * @return Returns ScriptPagingNodes which includes a JavaScript array of child file/folder nodes for this nodes. Automatically retrieves all sub-types of cm:content and cm:folder, also removes system folder types from the results. This is equivalent to @see FileFolderService.listFiles() and @see FileFolderService.listFolders()
     * 
     * @deprecated API for review (subject to change prior to release)
     *
     *             <br>
     *             author janv
     * @since 4.0
     */
    public ScriptPagingNodes childFileFolders(boolean files, boolean folders, Object ignoreTypes, int maxItems)
    {
        return childFileFolders(files, folders, ignoreTypes, 0, maxItems, 0, null, null, null);
    }

    @SuppressWarnings("unchecked")
    /**
     * @param files
     *            Return files extending from cm:content
     * @param folders
     *            Return folders extending from cm:folder - ignoring sub-types of cm:systemfolder
     * @param ignoreTypes
     *            Also optionally removes additional type qnames. The additional type can be specified in short or long qname string form as a single string or an Array e.g. "fm:forum".
     * @param skipOffset
     *            Items to skip (e.g. 0 or (num pages to skip * size of page)
     * @param maxItems
     *            Max number of items (eg. size of page)
     * @param requestTotalCountMax
     *            Request total count (upto a given max total count) Note: if 0 then total count is not requested and the query may be able to optimise/cutoff for max items)
     * @param sortProp
     *            Optional sort property as a prefix qname string (e.g. "cm:name"). Also supports special content case (i.e. "cm:content.size" and "cm:content.mimetype")
     * @param sortAsc
     *            Given a sort property, true => ascending, false => descending
     * @param queryExecutionId
     *            If paging then can pass back the previous query execution (as a hint for possible query optimisation)
     * 
     * @return Returns ScriptPagingNodes which includes a JavaScript array of child file/folder nodes for this nodes. Automatically retrieves all sub-types of cm:content and cm:folder, also removes system folder types from the results. This is equivalent to @see FileFolderService.listFiles() and @see FileFolderService.listFolders()
     * 
     *         <br/>
     *         <br/>
     *         author janv
     * @since 4.0
     */
    public ScriptPagingNodes childFileFolders(boolean files, boolean folders, Object ignoreTypes, int skipOffset, int maxItems, int requestTotalCountMax, String sortProp, Boolean sortAsc, String queryExecutionId)
    {
        Object[] results;

        Set<QName> ignoreTypeQNames = new HashSet<QName>(5);

        // Add user defined types to ignore
        if (ignoreTypes instanceof ScriptableObject)
        {
            Serializable types = getValueConverter().convertValueForRepo((ScriptableObject) ignoreTypes);
            if (types instanceof List)
            {
                for (Serializable typeObj : (List<Serializable>) types)
                {
                    ignoreTypeQNames.add(createQName(typeObj.toString()));
                }
            }
            else if (types instanceof String)
            {
                ignoreTypeQNames.add(createQName(types.toString()));
            }
        }
        else if (ignoreTypes instanceof String)
        {
            ignoreTypeQNames.add(createQName(ignoreTypes.toString()));
        }

        // ALF-13968 - sort folders before files (for Share) - TODO should be optional sort param
        List<Pair<QName, Boolean>> sortProps = new ArrayList<Pair<QName, Boolean>>(2);
        if ((sortProp == null) || (!sortProp.equals(GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE.getLocalName())))
        {
            sortProps.add(new Pair<QName, Boolean>(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, false));
        }
        if (sortProp != null)
        {
            sortProps.add(new Pair<QName, Boolean>(createQName(sortProp), sortAsc));
        }

        PagingRequest pageRequest = new PagingRequest(skipOffset, maxItems, queryExecutionId);
        pageRequest.setRequestTotalCountMax(requestTotalCountMax);

        PagingResults<FileInfo> pageOfNodeInfos = null;
        FileFilterMode.setClient(Client.script);
        try
        {
            pageOfNodeInfos = this.fileFolderService.list(this.nodeRef, files, folders, null, ignoreTypeQNames, sortProps, pageRequest);
        }
        finally
        {
            FileFilterMode.clearClient();
        }

        List<FileInfo> nodeInfos = pageOfNodeInfos.getPage();
        int size = nodeInfos.size();
        results = new Object[size];
        for (int i = 0; i < size; i++)
        {
            FileInfo nodeInfo = nodeInfos.get(i);
            results[i] = newInstance(nodeInfo, this.services, this.scope);
        }

        int totalResultCountLower = -1;
        int totalResultCountUpper = -1;

        Pair<Integer, Integer> totalResultCount = pageOfNodeInfos.getTotalResultCount();
        if (totalResultCount != null)
        {
            totalResultCountLower = (totalResultCount.getFirst() != null ? totalResultCount.getFirst() : -1);
            totalResultCountUpper = (totalResultCount.getSecond() != null ? totalResultCount.getSecond() : -1);
        }

        return new ScriptPagingNodes(Context.getCurrentContext().newArray(this.scope, results), pageOfNodeInfos.hasMoreItems(), totalResultCountLower, totalResultCountUpper);
    }

    /**
     * Return the target associations from this Node. As a Map of assoc type to a JavaScript array of Nodes. The Map returned implements the Scriptable interface to allow access to the assoc arrays via JavaScript associative array access. This means associations of this node can be access thus: <code>node.assocs["translations"][0]</code>
     * 
     * @return target associations as a Map of assoc name to a JavaScript array of Nodes.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAssocs()
    {
        if (this.targetAssocs == null)
        {
            // this Map implements the Scriptable interface for native JS syntax property access
            this.targetAssocs = new ScriptableQNameMap<String, Object>(this);

            // get the list of target nodes for each association type
            List<AssociationRef> refs = this.nodeService.getTargetAssocs(this.nodeRef, RegexQNamePattern.MATCH_ALL);
            for (AssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<ScriptNode> nodes = (List<ScriptNode>) this.targetAssocs.get(qname);
                if (nodes == null)
                {
                    // first access of the list for this qname
                    nodes = new ArrayList<ScriptNode>(4);
                }
                this.targetAssocs.put(ref.getTypeQName().toString(), nodes);
                nodes.add(newInstance(ref.getTargetRef(), this.services, this.scope));
            }

            // convert each Node list into a JavaScript array object
            for (String qname : this.targetAssocs.keySet())
            {
                List<ScriptNode> nodes = (List<ScriptNode>) this.targetAssocs.get(qname);
                Object[] objs = nodes.toArray(new Object[nodes.size()]);
                this.targetAssocs.put(qname, Context.getCurrentContext().newArray(this.scope, objs));
            }
        }

        return this.targetAssocs;
    }

    public Map<String, Object> getAssociations()
    {
        return getAssocs();
    }

    /**
     * Return the source associations to this Node. As a Map of assoc name to a JavaScript array of Nodes. The Map returned implements the Scriptable interface to allow access to the assoc arrays via JavaScript associative array access. This means source associations to this node can be access thus: <code>node.sourceAssocs["translations"][0]</code>
     * 
     * @return source associations as a Map of assoc name to a JavaScript array of Nodes.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSourceAssocs()
    {
        if (this.sourceAssocs == null)
        {
            // this Map implements the Scriptable interface for native JS syntax property access
            this.sourceAssocs = new ScriptableQNameMap<String, Object>(this);

            // get the list of source nodes for each association type
            List<AssociationRef> refs = this.nodeService.getSourceAssocs(this.nodeRef, RegexQNamePattern.MATCH_ALL);
            for (AssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<ScriptNode> nodes = (List<ScriptNode>) this.sourceAssocs.get(qname);
                if (nodes == null)
                {
                    // first access of the list for this qname
                    nodes = new ArrayList<ScriptNode>(4);
                    this.sourceAssocs.put(ref.getTypeQName().toString(), nodes);
                }
                nodes.add(newInstance(ref.getSourceRef(), this.services, this.scope));
            }

            // convert each Node list into a JavaScript array object
            for (String qname : this.sourceAssocs.keySet())
            {
                List<ScriptNode> nodes = (List<ScriptNode>) this.sourceAssocs.get(qname);
                Object[] objs = nodes.toArray(new Object[nodes.size()]);
                this.sourceAssocs.put(qname, Context.getCurrentContext().newArray(this.scope, objs));
            }
        }

        return this.sourceAssocs;
    }

    public Map<String, Object> getSourceAssociations()
    {
        return getSourceAssocs();
    }

    /**
     * Return the child associations from this Node. As a Map of assoc name to a JavaScript array of Nodes. The Map returned implements the Scriptable interface to allow access to the assoc arrays via JavaScript associative array access. This means associations of this node can be access thus: <code>node.childAssocs["contains"][0]</code>
     * 
     * @return child associations as a Map of assoc name to a JavaScript array of Nodes.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getChildAssocs()
    {
        if (this.childAssocs == null)
        {
            // this Map implements the Scriptable interface for native JS syntax property access
            this.childAssocs = new ScriptableQNameMap<String, Object>(this);

            // get the list of child assoc nodes for each association type
            List<ChildAssociationRef> refs = this.nodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<ScriptNode> nodes = (List<ScriptNode>) this.childAssocs.get(qname);
                if (nodes == null)
                {
                    // first access of the list for this qname
                    nodes = new ArrayList<ScriptNode>(4);
                    this.childAssocs.put(ref.getTypeQName().toString(), nodes);
                }
                nodes.add(newInstance(ref.getChildRef(), this.services, this.scope));
            }

            // convert each Node list into a JavaScript array object
            for (String qname : this.childAssocs.keySet())
            {
                List<ScriptNode> nodes = (List<ScriptNode>) this.childAssocs.get(qname);
                Object[] objs = nodes.toArray(new Object[nodes.size()]);
                this.childAssocs.put(qname, Context.getCurrentContext().newArray(this.scope, objs));
            }
        }

        return this.childAssocs;
    }

    public Map<String, Object> getChildAssociations()
    {
        return getChildAssocs();
    }

    /**
     * Return an Array of the associations from this Node that match a specific object type. <code>node.getChildAssocsByType("cm:folder")[0]</code>
     * 
     * @return Array of child associations from this Node that match a specific object type.
     */
    public Scriptable getChildAssocsByType(String type)
    {
        // get the list of child assoc nodes for each association type
        Set<QName> types = new HashSet<QName>(1, 1.0f);
        types.add(createQName(type));
        List<ChildAssociationRef> refs = this.nodeService.getChildAssocs(this.nodeRef, types);
        Object[] nodes = new Object[refs.size()];
        for (int i = 0; i < nodes.length; i++)
        {
            ChildAssociationRef ref = refs.get(i);
            nodes[i] = newInstance(ref.getChildRef(), this.services, this.scope);
        }
        return Context.getCurrentContext().newArray(this.scope, nodes);
    }

    /**
     * Return the parent associations to this Node. As a Map of assoc name to a JavaScript array of Nodes. The Map returned implements the Scriptable interface to allow access to the assoc arrays via JavaScript associative array access. This means associations of this node can be access thus: <code>node.parentAssocs["contains"][0]</code>
     * 
     * @return parent associations as a Map of assoc name to a JavaScript array of Nodes.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParentAssocs()
    {
        if (this.parentAssocs == null)
        {
            // this Map implements the Scriptable interface for native JS syntax property access
            this.parentAssocs = new ScriptableQNameMap<String, Object>(this);

            // get the list of child assoc nodes for each association type
            List<ChildAssociationRef> refs = this.nodeService.getParentAssocs(nodeRef);
            for (ChildAssociationRef ref : refs)
            {
                String qname = ref.getTypeQName().toString();
                List<ScriptNode> nodes = (List<ScriptNode>) this.parentAssocs.get(qname);
                if (nodes == null)
                {
                    // first access of the list for this qname
                    nodes = new ArrayList<ScriptNode>(4);
                    this.parentAssocs.put(ref.getTypeQName().toString(), nodes);
                }
                nodes.add(newInstance(ref.getParentRef(), this.services, this.scope));
            }

            // convert each Node list into a JavaScript array object
            for (String qname : this.parentAssocs.keySet())
            {
                List<ScriptNode> nodes = (List<ScriptNode>) this.parentAssocs.get(qname);
                Object[] objs = nodes.toArray(new Object[nodes.size()]);
                this.parentAssocs.put(qname, Context.getCurrentContext().newArray(this.scope, objs));
            }
        }

        return this.parentAssocs;
    }

    public Map<String, Object> getParentAssociations()
    {
        return getParentAssocs();
    }

    /**
     * Checks whether the {@link ScriptNode} exists in the repository.
     * 
     * @return boolean
     */
    public boolean exists()
    {
        return nodeService.exists(nodeRef);
    }

    /**
     * Return all the properties known about this node. The Map returned implements the Scriptable interface to allow access to the properties via JavaScript associative array access. This means properties of a node can be access thus: <code>node.properties["name"]</code>
     * 
     * @return Map of properties for this Node.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getProperties()
    {
        if (this.properties == null)
        {
            // this Map implements the Scriptable interface for native JS syntax property access
            // this impl of the QNameMap is capable of creating ScriptContentData on demand for 'cm:content'
            // properties that have not been initialised - see AR-1673.
            this.properties = new ContentAwareScriptableQNameMap<String, Serializable>(this, this.services);

            Map<QName, Serializable> props = null;
            if (this.nodeInfo != null)
            {
                props = this.nodeInfo.getProperties();
            }
            else
            {
                props = this.nodeService.getProperties(this.nodeRef);
            }

            for (QName qname : props.keySet())
            {
                Serializable propValue = props.get(qname);

                // perform the conversion to a script safe value and store
                this.properties.put(qname.toString(), getValueConverter().convertValueForScript(qname, propValue));
            }
        }

        return this.properties;
    }

    /**
     * Return all the property names defined for this node's type as an array of short QNames.
     * 
     * @return Array of property names for this node's type.
     */
    public Scriptable getTypePropertyNames()
    {
        return getTypePropertyNames(true);
    }

    /**
     * Return all the property names defined for this node's type as an array.
     * 
     * @param useShortQNames
     *            if true short-form qnames will be returned, else long-form.
     * @return Array of property names for this node's type.
     */
    public Scriptable getTypePropertyNames(boolean useShortQNames)
    {
        Set<QName> props = this.services.getDictionaryService().getClass(this.getQNameType()).getProperties().keySet();
        Object[] result = new Object[props.size()];
        int count = 0;
        for (QName qname : props)
        {
            result[count++] = useShortQNames ? getShortQName(qname).toString() : qname.toString();
        }
        return Context.getCurrentContext().newArray(this.scope, result);
    }

    /**
     * Return all the property names defined for this node as an array.
     * 
     * @param useShortQNames
     *            if true short-form qnames will be returned, else long-form.
     * @return Array of property names for this node type and optionally parent properties.
     */
    public Scriptable getPropertyNames(boolean useShortQNames)
    {
        Set<QName> props = this.nodeService.getProperties(this.nodeRef).keySet();
        Object[] result = new Object[props.size()];
        int count = 0;
        for (QName qname : props)
        {
            result[count++] = useShortQNames ? getShortQName(qname).toString() : qname.toString();
        }
        return Context.getCurrentContext().newArray(this.scope, result);
    }

    /**
     * @return true if this Node is a container (i.e. a folder)
     */
    public boolean getIsContainer()
    {
        if (isContainer == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isContainer = Boolean.valueOf((dd.isSubClass(getQNameType(), ContentModel.TYPE_FOLDER) == true &&
                    dd.isSubClass(getQNameType(), ContentModel.TYPE_SYSTEM_FOLDER) == false));
        }

        return isContainer.booleanValue();
    }

    /**
     * @return true if this Node is a Document (i.e. with content)
     */
    public boolean getIsDocument()
    {
        if (isDocument == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isDocument = Boolean.valueOf(dd.isSubClass(getQNameType(), ContentModel.TYPE_CONTENT));
        }

        return isDocument.booleanValue();
    }

    /**
     * @return true if this Node is a Link to a Container (i.e. a folderlink)
     */
    public boolean getIsLinkToContainer()
    {
        if (isLinkToContainer == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isLinkToContainer = Boolean.valueOf(dd.isSubClass(getQNameType(), ApplicationModel.TYPE_FOLDERLINK));
        }

        return isLinkToContainer.booleanValue();
    }

    /**
     * @return true if this Node is a Link to a Document (i.e. a filelink)
     */
    public boolean getIsLinkToDocument()
    {
        if (isLinkToDocument == null)
        {
            DictionaryService dd = this.services.getDictionaryService();
            isLinkToDocument = Boolean.valueOf(dd.isSubClass(getQNameType(), ApplicationModel.TYPE_FILELINK));
        }

        return isLinkToDocument.booleanValue();
    }

    /**
     * @return true if the Node is a Category
     */
    public boolean getIsCategory()
    {
        // this valid is overriden by the CategoryNode sub-class
        return false;
    }

    /**
     * @return The list of aspects applied to this node
     */
    public Set<QName> getAspectsSet()
    {
        if (this.aspects == null)
        {
            this.aspects = this.nodeService.getAspects(this.nodeRef);
        }

        return this.aspects;
    }

    /**
     * @return The array of aspects applied to this node as fully qualified qname strings
     */
    public Scriptable getAspects()
    {
        Set<QName> aspects = getAspectsSet();
        Object[] result = new Object[aspects.size()];
        int count = 0;
        for (QName qname : aspects)
        {
            result[count++] = qname.toString();
        }
        return Context.getCurrentContext().newArray(this.scope, result);
    }

    /**
     * @return The array of aspects applied to this node as short prefix qname strings
     */
    public Scriptable getAspectsShort()
    {
        final NamespaceService ns = this.services.getNamespaceService();
        final Map<String, String> cache = new HashMap<String, String>();
        final Set<QName> aspects = getAspectsSet();
        final Object[] result = new Object[aspects.size()];
        int count = 0;
        for (final QName qname : aspects)
        {
            String prefix = cache.get(qname.getNamespaceURI());
            if (prefix == null)
            {
                // first request for this namespace prefix, get and cache result
                Collection<String> prefixes = ns.getPrefixes(qname.getNamespaceURI());
                prefix = prefixes.size() != 0 ? prefixes.iterator().next() : "";
                cache.put(qname.getNamespaceURI(), prefix);
            }
            result[count++] = prefix + QName.NAMESPACE_PREFIX + qname.getLocalName();
        }
        return Context.getCurrentContext().newArray(this.scope, result);
    }

    /**
     * @param aspect
     *            The aspect name to test for (fully qualified or short-name form)
     * @return true if the node has the aspect false otherwise
     */
    public boolean hasAspect(String aspect)
    {
        return getAspectsSet().contains(createQName(aspect));
    }

    /**
     * @param type
     *            The qname type to test this object against (fully qualified or short-name form)
     * @return true if this Node is a sub-type of the specified class (or itself of that class)
     */
    public boolean isSubType(String type)
    {
        ParameterCheck.mandatoryString("Type", type);

        QName qnameType = createQName(type);

        return this.services.getDictionaryService().isSubClass(getQNameType(), qnameType);
    }

    /**
     * @return QName path to this node. This can be used for Lucene PATH: style queries
     */
    public String getQnamePath()
    {
        if (this.qnamePath == null)
        {
            final NamespaceService ns = this.services.getNamespaceService();
            final Map<String, String> cache = new HashMap<String, String>();
            final StringBuilder buf = new StringBuilder(128);
            final Path path = this.services.getNodeService().getPath(getNodeRef());
            for (final Path.Element e : path)
            {
                if (e instanceof Path.ChildAssocElement)
                {
                    final QName qname = ((Path.ChildAssocElement) e).getRef().getQName();
                    if (qname != null)
                    {
                        String prefix = cache.get(qname.getNamespaceURI());
                        if (prefix == null)
                        {
                            // first request for this namespace prefix, get and cache result
                            Collection<String> prefixes = ns.getPrefixes(qname.getNamespaceURI());
                            prefix = prefixes.size() != 0 ? prefixes.iterator().next() : "";
                            cache.put(qname.getNamespaceURI(), prefix);
                        }
                        buf.append('/');
                        if (prefix.length() > 0)
                        {
                            buf.append(prefix).append(':');
                        }
                        buf.append(ISO9075.encode(qname.getLocalName()));
                    }
                }
                else
                {
                    buf.append('/').append(e.toString());
                }
            }
            this.qnamePath = buf.toString();
        }

        return this.qnamePath;
    }

    /**
     * @return Display path to this node
     */
    public String getDisplayPath()
    {
        if (this.displayPath == null)
        {
            this.displayPath = this.nodeService.getPath(this.nodeRef).toDisplayPath(
                    this.nodeService, this.services.getPermissionService());
        }

        return this.displayPath;
    }

    /**
     * @return the small icon image for this node
     */
    public String getIcon16()
    {
        return "/images/filetypes/_default.gif";
    }

    /**
     * @return the large icon image for this node
     */
    public String getIcon32()
    {
        return "/images/filetypes32/_default.gif";
    }

    /**
     * @return true if the node is currently locked
     */
    public boolean getIsLocked()
    {
        boolean locked = false;

        if (getAspectsSet().contains(ContentModel.ASPECT_LOCKABLE))
        {
            locked = this.services.getLockService().isLocked(this.nodeRef);
        }

        return locked;
    }

    /**
     * @return the primary parent node
     */
    public ScriptNode getParent()
    {
        if (parent == null)
        {
            NodeRef parentRef = getPrimaryParentAssoc().getParentRef();
            // handle root node (no parent!)
            if (parentRef != null)
            {
                parent = newInstance(parentRef, this.services, this.scope);
            }
        }

        return parent;
    }

    /**
     * @return all parent nodes
     */
    public Scriptable getParents()
    {
        List<ChildAssociationRef> parentRefs = this.nodeService.getParentAssocs(this.nodeRef);
        Object[] parents = new Object[parentRefs.size()];
        for (int i = 0; i < parentRefs.size(); i++)
        {
            NodeRef ref = parentRefs.get(i).getParentRef();
            parents[i] = newInstance(ref, this.services, this.scope);
        }
        return Context.getCurrentContext().newArray(this.scope, parents);
    }

    /**
     * @return the primary parent association so we can get at the association QName and the association type QName.
     */
    public ChildAssociationRef getPrimaryParentAssoc()
    {
        if (primaryParentAssoc == null)
        {
            primaryParentAssoc = this.nodeService.getPrimaryParent(nodeRef);
        }
        return primaryParentAssoc;
    }

    // ------------------------------------------------------------------------------
    // Content API

    /**
     * @return the content String for this node from the default content property (@see ContentModel.PROP_CONTENT)
     */
    public String getContent()
    {
        String content = "";

        ScriptContentData contentData = (ScriptContentData) getProperties().get(ContentModel.PROP_CONTENT);
        if (contentData != null)
        {
            content = contentData.getContent();
        }

        return content;
    }

    /**
     * Set the content for this node
     * 
     * @param content
     *            Content string to set
     */
    public void setContent(String content)
    {
        ScriptContentData contentData = (ScriptContentData) getProperties().get(ContentModel.PROP_CONTENT);
        if (contentData != null)
        {
            contentData.setContent(content);
        }
    }

    /**
     * @return For a content document, this method returns the URL to the content stream for the default content property (@see ContentModel.PROP_CONTENT)
     *         <p>
     *         For a container node, this method return the URL to browse to the folder in the web-client
     */
    public String getUrl()
    {
        if (getIsDocument() == true)
        {
            return MessageFormat.format(CONTENT_DEFAULT_URL, new Object[]{nodeRef.getStoreRef().getProtocol(),
                    nodeRef.getStoreRef().getIdentifier(), nodeRef.getId(),
                    URLEncoder.encode(getName())});
        }
        else
        {
            return MessageFormat.format(FOLDER_BROWSE_URL, new Object[]{nodeRef.getStoreRef().getProtocol(),
                    nodeRef.getStoreRef().getIdentifier(), nodeRef.getId()});
        }
    }

    /**
     * @return For a content document, this method returns the download URL to the content for the default content property (@see ContentModel.PROP_CONTENT)
     *         <p>
     *         For a container node, this method returns an empty string
     */
    public String getDownloadUrl()
    {
        if (getIsDocument() == true)
        {
            return MessageFormat.format(CONTENT_DOWNLOAD_URL, new Object[]{
                    nodeRef.getStoreRef().getProtocol(),
                    nodeRef.getStoreRef().getIdentifier(),
                    nodeRef.getId(),
                    URLEncoder.encode(getName())});
        }
        else
        {
            return "";
        }
    }

    public String jsGet_downloadUrl()
    {
        return getDownloadUrl();
    }

    /**
     * @return The WebDav cm:name based path to the content for the default content property (@see ContentModel.PROP_CONTENT)
     */
    public String getWebdavUrl()
    {
        String url = "";
        try
        {
            if (getIsContainer() || getIsDocument())
            {
                List<String> paths = this.services.getFileFolderService().getNameOnlyPath(null, getNodeRef());

                // build up the webdav url
                StringBuilder path = new StringBuilder(128);
                path.append("/webdav");

                // build up the path skipping the first path as it is the root folder
                for (int i = 1; i < paths.size(); i++)
                {
                    path.append("/")
                            .append(URLEncoder.encode(paths.get(i)));
                }
                url = path.toString();
            }
        }
        catch (InvalidTypeException typeErr)
        {
            // cannot build path if file is a type such as a rendition
        }
        catch (FileNotFoundException nodeErr)
        {
            // cannot build path if file no longer exists
        }
        return url;
    }

    /**
     * @return The mimetype encoding for content attached to the node from the default content property (@see ContentModel.PROP_CONTENT)
     */
    public String getMimetype()
    {
        String mimetype = null;
        ScriptContentData content = (ScriptContentData) this.getProperties().get(ContentModel.PROP_CONTENT);
        if (content != null)
        {
            mimetype = content.getMimetype();
        }

        return mimetype;
    }

    /**
     * Set the mimetype encoding for the content attached to the node from the default content property (@see ContentModel.PROP_CONTENT)
     * 
     * @param mimetype
     *            Mimetype to set
     */
    public void setMimetype(String mimetype)
    {
        ScriptContentData content = (ScriptContentData) this.getProperties().get(ContentModel.PROP_CONTENT);
        if (content != null)
        {
            content.setMimetype(mimetype);
        }
    }

    /**
     * @return The size in bytes of the content attached to the node from the default content property (@see ContentModel.PROP_CONTENT)
     */
    public long getSize()
    {
        long size = 0;
        ScriptContentData content = (ScriptContentData) this.getProperties().get(ContentModel.PROP_CONTENT);
        if (content != null)
        {
            size = content.getSize();
        }

        return size;
    }

    // ------------------------------------------------------------------------------
    // Security API

    /**
     * Return true if the user has the specified permission on the node.
     * <p>
     * The default permissions are found in <code>org.alfresco.service.cmr.security.PermissionService</code>. Most commonly used are "Write", "Delete" and "AddChildren".
     * 
     * @param permission
     *            as found in <code>org.alfresco.service.cmr.security.PermissionService</code>
     * @return true if the user has the specified permission on the node.
     */
    public boolean hasPermission(String permission)
    {
        ParameterCheck.mandatory("Permission Name", permission);

        boolean allowed = false;

        if (permission != null && permission.length() != 0)
        {
            AccessStatus status = this.services.getPermissionService().hasPermission(this.nodeRef, permission);
            allowed = (AccessStatus.ALLOWED == status);
        }

        return allowed;
    }

    /**
     * @return Array of permissions applied to this Node, including inherited. Strings returned are of the format [ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION for example ALLOWED;kevinr;Consumer so can be easily tokenized on the ';' character.
     */
    public Scriptable getPermissions()
    {
        return Context.getCurrentContext().newArray(this.scope, retrieveAllSetPermissions(false, false));
    }

    /**
     * @return Array of permissions applied directly to this Node (does not include inherited). Strings returned are of the format [ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION for example ALLOWED;kevinr;Consumer so can be easily tokenized on the ';' character.
     */
    public Scriptable getDirectPermissions()
    {
        return Context.getCurrentContext().newArray(this.scope, retrieveAllSetPermissions(true, false));
    }

    /**
     * @return Array of all permissions applied to this Node, including inherited. Strings returned are of the format [ALLOWED|DENIED];[USERNAME|GROUPNAME];PERMISSION;[INHERITED|DIRECT] for example: ALLOWED;kevinr;Consumer;DIRECT so can be easily tokenized on the ';' character.
     */
    public Scriptable getFullPermissions()
    {
        return Context.getCurrentContext().newArray(this.scope, retrieveAllSetPermissions(false, true));
    }

    /**
     * @return Sorted list of <code>AccessPermission</code> based on <code>CMISConnector.AccessPermissionComparator</code> and <code>AccessStatus</code> of the permission for an authority.
     */
    public static List<AccessPermission> getSortedACLs(Set<AccessPermission> acls)
    {
        ArrayList<AccessPermission> ordered = new ArrayList<AccessPermission>(acls);
        Map<String, AccessPermission> deDuplicatedPermissions = new HashMap<String, AccessPermission>(acls.size());
        Collections.sort(ordered, new CMISConnector.AccessPermissionComparator());
        for (AccessPermission current : ordered)
        {
            String composedKey = current.getAuthority() + current.getPermission();
            if (current.getAccessStatus() == AccessStatus.ALLOWED)
            {
                deDuplicatedPermissions.put(composedKey, current);
            }
            else if (current.getAccessStatus() == AccessStatus.DENIED)
            {
                deDuplicatedPermissions.remove(composedKey);
            }
        }

        return new ArrayList<AccessPermission>(deDuplicatedPermissions.values());
    }

    /**
     * Helper to construct the response object for the various getPermissions() calls.
     * 
     * @param direct
     *            True to only retrieve direct permissions, false to get inherited also
     * @param full
     *            True to retrieve full data string with [INHERITED|DIRECT] element This exists to maintain backward compatibility with existing permission APIs.
     * 
     * @return Object[] of packed permission strings.
     */
    protected Object[] retrieveAllSetPermissions(boolean direct, boolean full)
    {
        Set<AccessPermission> acls = this.services.getPermissionService().getAllSetPermissions(getNodeRef());
        List<Object> permissions = new ArrayList<Object>(acls.size());
        List<AccessPermission> ordered = getSortedACLs(acls);
        for (AccessPermission permission : ordered)
        {
            if (!direct || permission.isSetDirectly())
            {
                StringBuilder buf = new StringBuilder(64);
                buf.append(permission.getAccessStatus())
                        .append(';')
                        .append(permission.getAuthority())
                        .append(';')
                        .append(permission.getPermission());
                if (full)
                {
                    buf.append(';').append(permission.isSetDirectly() ? "DIRECT" : "INHERITED");
                }
                permissions.add(buf.toString());
            }
        }
        return (Object[]) permissions.toArray(new Object[permissions.size()]);
    }

    /**
     * @return Array of settable permissions for this Node
     */
    public Scriptable getSettablePermissions()
    {
        Set<String> permissions = this.services.getPermissionService().getSettablePermissions(getNodeRef());
        Object[] result = permissions.toArray(new Object[0]);
        return Context.getCurrentContext().newArray(this.scope, result);
    }

    /**
     * @return true if the node inherits permissions from the parent node, false otherwise
     */
    public boolean inheritsPermissions()
    {
        return this.services.getPermissionService().getInheritParentPermissions(this.nodeRef);
    }

    /**
     * Set whether this node should inherit permissions from the parent node.
     * 
     * @param inherit
     *            True to inherit parent permissions, false otherwise.
     */
    public void setInheritsPermissions(boolean inherit)
    {
        this.services.getPermissionService().setInheritParentPermissions(this.nodeRef, inherit);
    }

    /**
     * Set whether this node should inherit permissions from the parent node. If the operation takes too long and asyncCall parameter set accordingly, fixed ACLs method will be asynchronously called.
     * 
     * @param inherit
     *            True to inherit parent permissions, false otherwise.
     * @param asyncCall
     *            True if fixed ACLs should be asynchronously set when operation execution takes too long, false otherwise.
     */
    public void setInheritsPermissions(boolean inherit, boolean asyncCall)
    {
        this.services.getPermissionService().setInheritParentPermissions(this.nodeRef, inherit, asyncCall);
    }

    /**
     * Apply a permission for ALL users to the node.
     * 
     * @param permission
     *            Permission to apply
     * @see org.alfresco.service.cmr.security.PermissionService
     */
    public void setPermission(String permission)
    {
        ParameterCheck.mandatoryString("Permission Name", permission);
        this.services.getPermissionService().setPermission(
                this.nodeRef, PermissionService.ALL_AUTHORITIES, permission, true);
    }

    /**
     * Apply a permission for the specified authority (e.g. username or group) to the node.
     * 
     * @param permission
     *            Permission to apply @see org.alfresco.service.cmr.security.PermissionService
     * @param authority
     *            Authority (generally a username or group name) to apply the permission for
     */
    public void setPermission(String permission, String authority)
    {
        ParameterCheck.mandatoryString("Permission Name", permission);
        ParameterCheck.mandatoryString("Authority", authority);
        this.services.getPermissionService().setPermission(
                this.nodeRef, authority, permission, true);
    }

    /**
     * Remove a permission for ALL user from the node.
     * 
     * @param permission
     *            Permission to remove @see org.alfresco.service.cmr.security.PermissionService
     */
    public void removePermission(String permission)
    {
        ParameterCheck.mandatoryString("Permission Name", permission);
        this.services.getPermissionService().deletePermission(
                this.nodeRef, PermissionService.ALL_AUTHORITIES, permission);
    }

    /**
     * Remove a permission for the specified authority (e.g. username or group) from the node.
     * 
     * @param permission
     *            Permission to remove @see org.alfresco.service.cmr.security.PermissionService
     * @param authority
     *            Authority (generally a username or group name) to apply the permission for
     */
    public void removePermission(String permission, String authority)
    {
        ParameterCheck.mandatoryString("Permission Name", permission);
        ParameterCheck.mandatoryString("Authority", authority);
        this.services.getPermissionService().deletePermission(
                this.nodeRef, authority, permission);
    }

    // ------------------------------------------------------------------------------
    // Ownership API

    /**
     * Set the owner of the node
     */
    public void setOwner(String userId)
    {
        this.services.getOwnableService().setOwner(this.nodeRef, userId);
    }

    /**
     * Take ownership of the node.
     */
    public void takeOwnership()
    {
        this.services.getOwnableService().takeOwnership(this.nodeRef);
    }

    /**
     * Get the owner of the node.
     * 
     * @return String
     */
    public String getOwner()
    {
        return this.services.getOwnableService().getOwner(this.nodeRef);
    }

    // ------------------------------------------------------------------------------
    // Create and Modify API

    /**
     * Persist the modified properties of this Node.
     */
    public void save()
    {
        // persist properties back to the node in the DB
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(getProperties().size());
        for (String key : this.properties.keySet())
        {
            Serializable value = (Serializable) this.properties.get(key);

            QName qname = createQName(key);

            // MNT-15798
            if (ContentModel.PROP_CONTENT.equals(qname) && isScriptContent(value))
            {
                ScriptContentData contentData = (ScriptContentData) value;
                // Do not persist the contentData if it was not touched
                if (!contentData.isDirty())
                {
                    continue;
                }
            }

            // perform the conversion from script wrapper object to repo serializable values
            value = getValueConverter().convertValueForRepo(value);

            props.put(qname, value);
        }
        this.nodeService.setProperties(this.nodeRef, props);
    }

    /**
     * Re-sets the type of the node. Can be called in order specialise a node to a sub-type. This should be used with caution since calling it changes the type of the node and thus* implies a different set of aspects, properties and associations. It is the responsibility of the caller to ensure that the node is in a approriate state after changing the type.
     * 
     * @param type
     *            Type to specialize the node
     * 
     * @return true if successful, false otherwise
     */
    public boolean specializeType(String type)
    {
        ParameterCheck.mandatoryString("Type", type);

        QName qnameType = createQName(type);

        // Ensure that we are performing a specialise
        if (getQNameType().equals(qnameType) == false &&
                this.services.getDictionaryService().isSubClass(qnameType, getQNameType()) == true)
        {
            // Specialise the type of the node
            this.nodeService.setType(this.nodeRef, qnameType);
            this.type = qnameType;

            return true;
        }
        return false;
    }

    /**
     * Create a new File (cm:content) node as a child of this node.
     * <p>
     * Once created the file should have content set using the <code>content</code> property.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param name
     *            Name of the file to create
     * 
     * @return Newly created Node or null if failed to create.
     */
    public ScriptNode createFile(String name)
    {
        return createFile(name, null);
    }

    /**
     * Create a new File (cm:content) node as a child of this node.
     * <p>
     * Once created the file should have content set using the <code>content</code> property.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param name
     *            Name of the file to create
     * @param type
     *            Type of the file to create (if null, defaults to ContentModel.TYPE_CONTENT)
     * 
     * @return Newly created Node or null if failed to create.
     */
    public ScriptNode createFile(String name, String type)
    {
        ParameterCheck.mandatoryString("Node Name", name);

        FileInfo fileInfo = this.services.getFileFolderService().create(
                this.nodeRef, name, type == null ? ContentModel.TYPE_CONTENT : createQName(type));

        reset();

        ScriptNode file = newInstance(fileInfo.getNodeRef(), this.services, this.scope);
        file.setMimetype(this.services.getMimetypeService().guessMimetype(name));

        return file;
    }

    /**
     * Create a new folder (cm:folder) node as a child of this node.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param name
     *            Name of the folder to create
     * 
     * @return Newly created Node or null if failed to create.
     */
    public ScriptNode createFolder(String name)
    {
        return createFolder(name, null);
    }

    /**
     * Create a new folder (cm:folder) node as a child of this node.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param name
     *            Name of the folder to create
     * @param type
     *            Type of the folder to create (if null, defaults to ContentModel.TYPE_FOLDER)
     * 
     * @return Newly created Node or null if failed to create.
     */
    public ScriptNode createFolder(String name, String type)
    {
        ParameterCheck.mandatoryString("Node Name", name);

        FileInfo fileInfo = this.services.getFileFolderService().create(
                this.nodeRef, name, type == null ? ContentModel.TYPE_FOLDER : createQName(type));

        reset();

        return newInstance(fileInfo.getNodeRef(), this.services, this.scope);
    }

    /**
     * Create a path of folder (cm:folder) nodes as a child of this node.
     * <p>
     * This method operates like a unix 'mkdir -p' no error if existing, make parent directories as needed.
     * <p>
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param path
     *            Folder path to create - of the form "One/Two/Three". Leading and trailing slashes are not expected to be present in the supplied path.
     * 
     * @return reference to the last child of the newly created folder node(s) or null if failed to create.
     */
    public ScriptNode createFolderPath(String path)
    {
        ParameterCheck.mandatoryString("Folder path", path);

        List<String> pathElements = Arrays.asList(path.split("/"));

        NodeRef currentParentRef = this.nodeRef;
        // just loop and create if necessary
        for (final String element : pathElements)
        {
            final NodeRef contextNodeRef = currentParentRef;
            // does it exist?
            // Navigation should not check permissions
            NodeRef nodeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
                @Override
                public NodeRef doWork() throws Exception
                {
                    return nodeService.getChildByName(contextNodeRef, ContentModel.ASSOC_CONTAINS, element);
                }
            }, AuthenticationUtil.getSystemUserName());

            if (nodeRef == null)
            {
                // Checks for create permissions as the fileFolderService is a public service.
                FileInfo createdFileInfo = services.getFileFolderService().create(
                        currentParentRef, element, ContentModel.TYPE_FOLDER);
                currentParentRef = createdFileInfo.getNodeRef();
            }
            else if (!services.getDictionaryService().isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_FOLDER))
            {
                String parentName = (String) nodeService.getProperty(contextNodeRef, ContentModel.PROP_NAME);
                throw new ScriptException("Name [" + element + "] already exists in the target parent: " + parentName);
            }
            else
            {
                // it exists
                currentParentRef = nodeRef;
            }
        }

        reset();

        return newInstance(currentParentRef, this.services, this.scope);
    }

    /**
     * Create a new Node of the specified type as a child of this node.
     * 
     * @param name
     *            Name of the node to create (can be null for a node without a 'cm:name' property)
     * @param type
     *            QName type (fully qualified or short form such as 'cm:content')
     * 
     * @return Newly created Node or null if failed to create.
     */
    public ScriptNode createNode(String name, String type)
    {
        return createNode(name, type, null, ContentModel.ASSOC_CONTAINS.toString());
    }

    /**
     * Create a new Node of the specified type as a child of this node.
     * 
     * @param name
     *            Name of the node to create (can be null for a node without a 'cm:name' property)
     * @param type
     *            QName type (fully qualified or short form such as 'cm:content')
     * @param assocType
     *            QName of the child association type (fully qualified or short form e.g. 'cm:contains')
     * 
     * @return Newly created Node or null if failed to create.
     */
    public ScriptNode createNode(String name, String type, String assocType)
    {
        return createNode(name, type, null, assocType);
    }

    /**
     * Create a new Node of the specified type as a child of this node.
     * 
     * @param name
     *            Name of the node to create (can be null for a node without a 'cm:name' property)
     * @param type
     *            QName type (fully qualified or short form such as 'cm:content')
     * @param properties
     *            Associative array of the default properties for the node.
     * 
     * @return Newly created Node or null if failed to create.
     */
    public ScriptNode createNode(String name, String type, Object properties)
    {
        return createNode(name, type, properties, ContentModel.ASSOC_CONTAINS.toString());
    }

    /**
     * Create a new Node of the specified type as a child of this node.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param name
     *            Name of the node to create (can be null for a node without a 'cm:name' property)
     * @param type
     *            QName type (fully qualified or short form such as 'cm:content')
     * @param properties
     *            Associative array of the default properties for the node.
     * @param assocType
     *            QName of the child association type (fully qualified or short form e.g. 'cm:contains')
     * 
     * @return Newly created Node or null if failed to create.
     */
    public ScriptNode createNode(String name, String type, Object properties, String assocType)
    {
        return createNode(name, type, properties, assocType, null);
    }

    /**
     * Create a new Node of the specified type as a child of this node.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param name
     *            Name of the node to create (can be null for a node without a 'cm:name' property)
     * @param type
     *            QName type (fully qualified or short form such as 'cm:content')
     * @param properties
     *            Associative array of the default properties for the node.
     * @param assocType
     *            QName of the child association type (fully qualified or short form e.g. 'cm:contains')
     * @param assocName
     *            QName of the child association name (fully qualified or short form e.g. 'fm:discussion')
     * 
     * @return Newly created Node or null if failed to create.
     */
    public ScriptNode createNode(String name, String type, Object properties, String assocType, String assocName)
    {
        ParameterCheck.mandatoryString("Node Type", type);
        ParameterCheck.mandatoryString("Association Type", assocType);

        Map<QName, Serializable> props = null;

        if (properties instanceof ScriptableObject)
        {
            props = new HashMap<QName, Serializable>(4, 1.0f);
            extractScriptableProperties((ScriptableObject) properties, props);
        }

        if (name != null)
        {
            if (props == null)
                props = new HashMap<QName, Serializable>(1, 1.0f);
            props.put(ContentModel.PROP_NAME, name);
        }
        else
        {
            // set name for the assoc local name
            name = GUID.generate();
        }

        ChildAssociationRef childAssocRef = this.nodeService.createNode(
                this.nodeRef,
                createQName(assocType),
                assocName == null ? QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)) : createQName(assocName),
                createQName(type),
                props);

        reset();

        return newInstance(childAssocRef.getChildRef(), this.services, this.scope);
    }

    /**
     * Creates a new secondary association between the current node and the specified child node. The association is given the same name as the child node's primary association.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param node
     *            node to add as a child of this node
     */
    public void addNode(ScriptNode node)
    {
        ParameterCheck.mandatory("node", node);
        ChildAssociationRef childAssocRef = this.nodeService.getPrimaryParent(node.nodeRef);
        nodeService.addChild(this.nodeRef, node.nodeRef, ContentModel.ASSOC_CONTAINS, childAssocRef.getQName());
        reset();
    }

    /**
     * Remove an existing child node of this node.
     *
     * Severs all parent-child relationships between two nodes.
     * <p>
     * The child node will be cascade deleted if one of the associations was the primary association, i.e. the one with which the child node was created.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param node
     *            child node to remove
     */
    public void removeNode(ScriptNode node)
    {
        ParameterCheck.mandatory("node", node);
        nodeService.removeChild(this.nodeRef, node.nodeRef);
        reset();
    }

    /**
     * Create an association between this node and the specified target node.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param target
     *            Destination node for the association
     * @param assocType
     *            Association type qname (short form or fully qualified)
     */
    public Association createAssociation(ScriptNode target, String assocType)
    {
        ParameterCheck.mandatory("Target", target);
        ParameterCheck.mandatoryString("Association Type Name", assocType);

        AssociationRef assocRef = this.nodeService.createAssociation(this.nodeRef, target.nodeRef, createQName(assocType));
        reset();
        return new Association(this.services, assocRef);
    }

    /**
     * Remove an association between this node and the specified target node.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param target
     *            Destination node on the end of the association
     * @param assocType
     *            Association type qname (short form or fully qualified)
     */
    public void removeAssociation(ScriptNode target, String assocType)
    {
        ParameterCheck.mandatory("Target", target);
        ParameterCheck.mandatoryString("Association Type Name", assocType);

        this.nodeService.removeAssociation(this.nodeRef, target.nodeRef, createQName(assocType));
        reset();
    }

    /**
     * Remove this node. Any references to this Node or its NodeRef should be discarded!
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link save()} first.
     * 
     */
    public boolean remove()
    {
        return remove(false);
    }

    /**
     * Remove this node in a new transaction or not as specified. Any references to this Node or its NodeRef should be discarded!
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link save()} first.
     * 
     */
    public boolean remove(boolean newTransaction)
    {
        boolean success = false;

        if (nodeService.exists(this.nodeRef))
        {
            retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                @Override
                public Void execute() throws Throwable
                {
                    nodeService.deleteNode(nodeRef);
                    return null;
                }
            }, false, newTransaction);
            success = true;
        }

        reset();

        return success;
    }

    /**
     * Copy this Node to a new parent destination. Note that children of the source Node are not copied.
     * 
     * @param destination
     *            Node
     * 
     * @return The newly copied Node instance or null if failed to copy.
     */
    public ScriptNode copy(ScriptNode destination)
    {
        ScriptNode copy = copy(destination, false);

        // ALF-9517 fix
        if (copy != null && copy.hasAspect(ContentModel.ASPECT_VERSIONABLE.toString()))
        {
            copy.ensureVersioningEnabled(true, true);
        }
        return copy;
    }

    /**
     * Copy this Node and potentially all child nodes to a new parent destination.
     * 
     * @param destination
     *            Node
     * @param deepCopy
     *            True for a deep copy, false otherwise.
     * 
     * @return The newly copied Node instance or null if failed to copy.
     */
    public ScriptNode copy(ScriptNode destination, boolean deepCopy)
    {
        ParameterCheck.mandatory("Destination Node", destination);

        NodeRef copyRef = this.services.getCopyService().copyAndRename(this.nodeRef, destination.getNodeRef(),
                ContentModel.ASSOC_CONTAINS, null, deepCopy);
        ScriptNode copy = newInstance(copyRef, this.services, this.scope);

        return copy;
    }

    /**
     * Revert this Node to the specified version. Note this is not a deep revert of associations. This node must have the cm:versionable aspect. It will be checked out if required but will be checked in after the call.
     * 
     * @param versionLabel
     *            to revert from
     * 
     * @return the original Node that was checked out if reverted, {@code null} otherwise (if the version does not exist).
     */
    public ScriptNode revert(String history, boolean majorVersion, String versionLabel)
    {
        return revert(history, majorVersion, versionLabel, false);
    }

    /**
     * Revert this Node to the specified version and potentially all child nodes. This node must have the cm:versionable aspect. It will be checked out if required but will be checked in after the call.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param history
     *            Version history note
     * @param majorVersion
     *            True to save as a major version increment, false for minor version.
     * @param versionLabel
     *            to revert from
     * @param deep
     *            {@code true} for a deep revert, {@code false} otherwise.
     * 
     * @return the original Node that was checked out if reverted, {@code null} otherwise (if the version does not exist).
     */
    public ScriptNode revert(String history, boolean majorVersion, String versionLabel, boolean deep)
    {
        if (!getIsVersioned())
        {
            return null;
        }

        // Get the Version - needed to do the revert
        Version version = services.getVersionService().getVersionHistory(nodeRef).getVersion(versionLabel);
        if (version == null)
        {
            return null;
        }

        ScriptNode originalNode = this;
        // cancel editing if we want to revert
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            originalNode = cancelCheckout();
        }

        // Revert the new (current) version of the node
        services.getVersionService().revert(originalNode.getNodeRef(), version, deep);

        // Checkout/Checkin the node - to store the new version in version history
        ScriptNode workingCopy = originalNode.checkout();
        originalNode = workingCopy.checkin(history, majorVersion);

        return originalNode;
    }

    /**
     * Move this Node to a new parent destination.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param destination
     *            Node
     * 
     * @return true on successful move, false on failure to move.
     */
    public boolean move(ScriptNode destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);

        this.primaryParentAssoc = this.nodeService.moveNode(this.nodeRef, destination.getNodeRef(),
                ContentModel.ASSOC_CONTAINS, getPrimaryParentAssoc().getQName());

        // reset cached values
        reset();

        return true;
    }

    /**
     * Move this Node from specified parent to a new parent destination.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param source
     *            Node
     * @param destination
     *            Node
     * @return true on successful move, false on failure to move.
     */
    public boolean move(ScriptNode source, ScriptNode destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);

        if (source == null)
        {
            return move(destination);
        }
        else
        {
            try
            {
                this.services.getFileFolderService().moveFrom(this.nodeRef, source.getNodeRef(), destination.getNodeRef(), null);
            }
            // MNT-7514 Uninformational error message on move when file name conflicts
            catch (FileExistsException ex)
            {
                throw ex;
            }
            catch (Exception e)
            {
                throw new ScriptException("Can't move node", e);
            }
        }

        // reset cached values
        reset();

        return true;
    }

    /**
     * Add an aspect to the Node. As no properties are provided in this call, it can only be used to add aspects that do not require any mandatory properties.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param type
     *            Type name of the aspect to add
     * 
     * @return true if the aspect was added successfully, false if an error occured.
     */
    public boolean addAspect(String type)
    {
        return addAspect(type, null);
    }

    /**
     * Add an aspect to the Node.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param type
     *            Type name of the aspect to add
     * @param props
     *            ScriptableObject (generally an assocative array) providing the named properties for the aspect - any mandatory properties for the aspect must be provided!
     * 
     * @return true if the aspect was added successfully, false if an error occured.
     */
    public boolean addAspect(String type, Object props)
    {
        ParameterCheck.mandatoryString("Aspect Type", type);

        Map<QName, Serializable> aspectProps = null;
        if (props instanceof ScriptableObject)
        {
            aspectProps = new HashMap<QName, Serializable>(4, 1.0f);
            extractScriptableProperties((ScriptableObject) props, aspectProps);
        }
        QName aspectQName = createQName(type);
        if (aspectQName.equals(ContentModel.ASPECT_VERSIONABLE))
        {
            // ALF-13719 need to taking into account script properties for versionable aspect
            if (aspectProps != null)
            {
                Serializable autoVersionObj, autoVersionPropsObj;
                autoVersionObj = aspectProps.get(ContentModel.PROP_AUTO_VERSION);
                autoVersionPropsObj = aspectProps.get(ContentModel.PROP_AUTO_VERSION_PROPS);
                ensureVersioningEnabled(autoVersionObj instanceof Boolean ? ((Boolean) autoVersionObj) : true,
                        autoVersionPropsObj instanceof Boolean ? ((Boolean) autoVersionPropsObj) : true);
            }
            else
            {
                // MNT-9369, read props from contentModel.xml, sets to false, false if there is no defaults.
                Map<QName, PropertyDefinition> versionableProps = services.getDictionaryService().getAspect(ContentModel.ASPECT_VERSIONABLE).getProperties();
                boolean autoVersion = Boolean.parseBoolean(versionableProps.get(ContentModel.PROP_AUTO_VERSION).getDefaultValue());
                boolean autoVersionProps = Boolean.parseBoolean(versionableProps.get(ContentModel.PROP_AUTO_VERSION_PROPS).getDefaultValue());
                ensureVersioningEnabled(autoVersion, autoVersionProps);
            }
        }
        else
        {
            this.nodeService.addAspect(this.nodeRef, aspectQName, aspectProps);
        }

        // reset the relevant cached node members
        reset();

        return true;
    }

    /**
     * Extract a map of properties from a scriptable object (generally an associative array)
     * 
     * @param scriptable
     *            The scriptable object to extract name/value pairs from.
     * @param map
     *            The map to add the converted name/value pairs to.
     */
    private void extractScriptableProperties(ScriptableObject scriptable, Map<QName, Serializable> map)
    {
        // we need to get all the keys to the properties provided
        // and convert them to a Map of QName to Serializable objects
        Object[] propIds = scriptable.getIds();
        for (int i = 0; i < propIds.length; i++)
        {
            // work on each key in turn
            Object propId = propIds[i];

            // we are only interested in keys that are formed of Strings i.e. QName.toString()
            if (propId instanceof String)
            {
                // get the value out for the specified key - it must be Serializable
                String key = (String) propId;
                Object value = scriptable.get(key, scriptable);
                if (value instanceof Serializable)
                {
                    value = getValueConverter().convertValueForRepo((Serializable) value);
                    map.put(createQName(key), (Serializable) value);
                }
            }
        }
    }

    /**
     * Remove aspect from the node.
     * 
     * Beware: Any unsaved property changes will be lost when this is called. To preserve property changes call {@link #save()} first.
     * 
     * @param type
     *            the aspect type
     * 
     * @return true if successful, false otherwise
     */
    public boolean removeAspect(String type)
    {
        ParameterCheck.mandatoryString("Aspect Type", type);

        QName aspectQName = createQName(type);
        this.nodeService.removeAspect(this.nodeRef, aspectQName);

        // reset the relevant cached node members
        reset();

        return true;
    }

    // ------------------------------------------------------------------------------
    // Checkout/Checkin Services

    /**
     * Ensures that this document has the cm:versionable aspect applied to it, and that it has the initial version in the version store. Calling this on a versioned node with a version store entry will have no effect. Calling this on a newly uploaded share node will have versioning enabled for it (Share currently does lazy versioning to improve performance of documents that are uploaded but never edited, and multi upload performance).
     * 
     * @param autoVersion
     *            If the cm:versionable aspect is applied, should auto versioning be requested?
     * @param autoVersionProps
     *            If the cm:versionable aspect is applied, should auto versioning of properties be requested?
     */
    public void ensureVersioningEnabled(boolean autoVersion, boolean autoVersionProps)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
        props.put(ContentModel.PROP_AUTO_VERSION, autoVersion);
        props.put(ContentModel.PROP_AUTO_VERSION_PROPS, autoVersionProps);

        this.services.getVersionService().ensureVersioningEnabled(nodeRef, props);
    }

    /**
     * Ensures that this document has the cm:versionable aspect applied to it, and that it has the initial version in the version store. Calling this on a versioned node with a version store entry will have no effect. Calling this on a newly uploaded share node will have versioning enabled for it (Share currently does lazy versioning to improve performance of documents that are uploaded but never edited, and multi upload performance).
     * 
     */
    public void ensureVersioningEnabled()
    {
        this.services.getVersionService().ensureVersioningEnabled(nodeRef, null);
    }

    /**
     * Create a version of this document. Note: this will add the cm:versionable aspect.
     * 
     * @param history
     *            Version history note
     * @param majorVersion
     *            True to save as a major version increment, false for minor version.
     * 
     * @return ScriptVersion object representing the newly added version node
     */
    public ScriptVersion createVersion(String history, boolean majorVersion)
    {
        Map<String, Serializable> props = new HashMap<String, Serializable>(2, 1.0f);
        props.put(Version.PROP_DESCRIPTION, history);
        props.put(VersionModel.PROP_VERSION_TYPE, majorVersion ? VersionType.MAJOR : VersionType.MINOR);
        ScriptVersion version = new ScriptVersion(this.services.getVersionService().createVersion(this.nodeRef, props), this.services, this.scope);
        this.versions = null;
        return version;
    }

    /**
     * Determines if this node is versioned
     * 
     * @return true => is versioned
     */
    public boolean getIsVersioned()
    {
        return this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE);
    }

    /**
     * Gets the version history
     * 
     * @return version history
     */
    public Scriptable getVersionHistory()
    {
        if (this.versions == null && getIsVersioned())
        {
            VersionHistory history = this.services.getVersionService().getVersionHistory(this.nodeRef);
            if (history != null)
            {
                Collection<Version> allVersions = history.getAllVersions();
                Object[] versions = new Object[allVersions.size()];
                int i = 0;
                for (Version version : allVersions)
                {
                    versions[i++] = new ScriptVersion(version, this.services, this.scope);
                }
                this.versions = Context.getCurrentContext().newArray(this.scope, versions);
            }
        }
        return this.versions;
    }

    /**
     * Gets the version of this node specified by version label
     * 
     * @param versionLabel
     *            version label
     * @return version of node, or null if node is not versioned, or label does not exist
     */
    public ScriptVersion getVersion(String versionLabel)
    {
        if (!getIsVersioned())
        {
            return null;
        }
        VersionHistory history = this.services.getVersionService().getVersionHistory(this.nodeRef);
        Version version = history.getVersion(versionLabel);
        if (version == null)
        {
            return null;
        }
        return new ScriptVersion(version, this.services, this.scope);
    }

    /**
     * Perform a check-out of this document into the current parent space.
     * 
     * @return the working copy Node for the checked out document
     */
    public ScriptNode checkout()
    {
        NodeRef workingCopyRef = this.services.getCheckOutCheckInService().checkout(this.nodeRef);
        ScriptNode workingCopy = newInstance(workingCopyRef, this.services, this.scope);

        // reset the aspect and properties as checking out a document causes changes
        this.properties = null;
        this.aspects = null;

        return workingCopy;
    }

    /**
     * Performs a check-out of this document for the purposes of an upload
     * 
     * @return ScriptNode
     */
    public ScriptNode checkoutForUpload()
    {
        AlfrescoTransactionSupport.bindResource("checkoutforupload", Boolean.TRUE.toString());
        services.getRuleService().disableRules();
        try
        {
            return checkout();
        }
        finally
        {
            services.getRuleService().enableRules();
        }
    }

    /**
     * Perform a check-out of this document into the specified destination space.
     * 
     * @param destination
     *            Destination for the checked out document working copy Node.
     * @return the working copy Node for the checked out document
     */
    public ScriptNode checkout(ScriptNode destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);

        ChildAssociationRef childAssocRef = this.nodeService.getPrimaryParent(destination.getNodeRef());
        NodeRef workingCopyRef = this.services.getCheckOutCheckInService().checkout(this.nodeRef,
                destination.getNodeRef(), ContentModel.ASSOC_CONTAINS, childAssocRef.getQName());
        ScriptNode workingCopy = newInstance(workingCopyRef, this.services, this.scope);

        // reset the aspect and properties as checking out a document causes changes
        this.properties = null;
        this.aspects = null;

        return workingCopy;
    }

    /**
     * Check-in a working copy document. The current state of the working copy is copied to the original node, this will include any content updated in the working node. Note that this method can only be called on a working copy Node.
     * 
     * @return the original Node that was checked out.
     */
    public ScriptNode checkin()
    {
        return checkin("", false);
    }

    /**
     * Check-in a working copy document. The current state of the working copy is copied to the original node, this will include any content updated in the working node. Note that this method can only be called on a working copy Node.
     * 
     * @param history
     *            Version history note
     * 
     * @return the original Node that was checked out.
     */
    public ScriptNode checkin(String history)
    {
        return checkin(history, false);
    }

    /**
     * Check-in a working copy document. The current state of the working copy is copied to the original node, this will include any content updated in the working node. Note that this method can only be called on a working copy Node.
     * 
     * @param history
     *            Version history note
     * @param majorVersion
     *            True to save as a major version increment, false for minor version.
     * 
     * @return the original Node that was checked out.
     */
    public ScriptNode checkin(String history, boolean majorVersion)
    {
        Map<String, Serializable> props = new HashMap<String, Serializable>(2, 1.0f);
        props.put(Version.PROP_DESCRIPTION, history);
        props.put(VersionModel.PROP_VERSION_TYPE, majorVersion ? VersionType.MAJOR : VersionType.MINOR);
        NodeRef original = this.services.getCheckOutCheckInService().checkin(this.nodeRef, props);
        this.versions = null;
        return newInstance(original, this.services, this.scope);
    }

    /**
     * Removes the lock on a node.
     * 
     */
    public void unlock()
    {
        this.services.getLockService().unlock(this.nodeRef);
    }

    /**
     * Gets the check-out of a working copy document
     * 
     * @return the original Node that was checked out or null if it's not a working copy
     */
    public ScriptNode getCheckedOut()
    {
        NodeRef original = this.services.getCheckOutCheckInService().getCheckedOut(this.nodeRef);

        if (original != null)
        {
            return newInstance(original, this.services, this.scope);
        }
        else
        {
            return null;
        }
    }

    /**
     * Cancel the check-out of a working copy document. The working copy will be deleted and any changes made to it are lost. Note that this method can only be called on a working copy Node. The reference to this working copy Node should be discarded.
     * 
     * @return the original Node that was checked out.
     */
    public ScriptNode cancelCheckout()
    {
        NodeRef original = this.services.getCheckOutCheckInService().cancelCheckout(this.nodeRef);
        return newInstance(original, this.services, this.scope);
    }

    // ------------------------------------------------------------------------------
    // Transformation and Rendering API

    /**
     * Transform a document to a new document mimetype format. A copy of the document is made and the extension changed to match the new mimetype, then the transformation isapplied.
     * 
     * @param mimetype
     *            Mimetype destination for the transformation
     * 
     * @return Node representing the newly transformed document.
     */
    public ScriptNode transformDocument(String mimetype)
    {
        return transformDocument(mimetype, getPrimaryParentAssoc().getParentRef());
    }

    /**
     * Transform a document to a new document mimetype format. A copy of the document is made in the specified destination folder and the extension changed to match the new mimetype, then then transformation is applied.
     * 
     * @param mimetype
     *            Mimetype destination for the transformation
     * @param destination
     *            Destination folder location
     * 
     * @return Node representing the newly transformed document.
     */
    public ScriptNode transformDocument(String mimetype, ScriptNode destination)
    {
        return transformDocument(mimetype, destination.getNodeRef());
    }

    private ScriptNode transformDocument(String mimetype, NodeRef destination)
    {
        ParameterCheck.mandatoryString("Mimetype", mimetype);
        ParameterCheck.mandatory("Destination Node", destination);
        final NodeRef sourceNodeRef = nodeRef;

        // the delegate definition for transforming a document
        Transformer transformer = new AbstractTransformer() {
            protected void doTransform(SynchronousTransformClient synchronousTransformClient, ContentReader reader, ContentWriter writer)
            {
                transformNodeRef(synchronousTransformClient, reader, writer, Collections.emptyMap(), sourceNodeRef);
            }
        };

        return transformNode(transformer, mimetype, destination);
    }

    private void transformNodeRef(SynchronousTransformClient synchronousTransformClient,
            ContentReader reader, ContentWriter writer,
            Map<String, String> actualOptions, NodeRef sourceNodeRef)
    {
        try
        {
            synchronousTransformClient.transform(reader, writer, actualOptions, null, sourceNodeRef);
        }
        catch (Exception e)
        {
            throw new ContentIOException("Content conversion failed: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer + "\n", e);
        }
    }

    /**
     * Generic method to transform Node content from one mimetype to another.
     * 
     * @param transformer
     *            The Transformer delegate supplying the transformation logic
     * @param mimetype
     *            Mimetype of the destination content
     * @param destination
     *            Destination folder location for the resulting document
     * 
     * @return Node representing the transformed content - or null if the transform failed
     */
    private ScriptNode transformNode(Transformer transformer, String mimetype, NodeRef destination)
    {
        ScriptNode transformedNode = null;

        // get the content reader
        ContentService contentService = this.services.getContentService();
        ContentReader reader = contentService.getReader(this.nodeRef, ContentModel.PROP_CONTENT);

        // only perform the transformation if some content is available
        if (reader != null)
        {
            // Copy the content node to a new node
            String copyName = TransformActionExecuter.transformName(this.services.getMimetypeService(), getName(),
                    mimetype, true);
            NodeRef copyNodeRef = this.services.getCopyService().copy(this.nodeRef, destination,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(ContentModel.PROP_CONTENT.getNamespaceURI(), QName.createValidLocalName(copyName)),
                    false);

            // modify the name of the copy to reflect the new mimetype
            this.nodeService.setProperty(copyNodeRef, ContentModel.PROP_NAME, copyName);

            // get the writer and set it up
            ContentWriter writer = contentService.getWriter(copyNodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(mimetype); // new mimetype
            writer.setEncoding(reader.getEncoding()); // original encoding

            // Try and transform the content using the supplied delegate
            transformedNode = transformer.transform(synchronousTransformClient, copyNodeRef, reader, writer);
        }

        return transformedNode;
    }

    /**
     * Transform an image to a new image format. A copy of the image document is made and the extension changed to match the new mimetype, then the transformation is applied.
     * 
     * @param mimetype
     *            Mimetype destination for the transformation
     * 
     * @return Node representing the newly transformed image.
     */
    public ScriptNode transformImage(String mimetype)
    {
        return transformImage(mimetype, null, getPrimaryParentAssoc().getParentRef());
    }

    /**
     * Transform an image to a new image format. A copy of the image document is made and the extension changed to match the new mimetype, then the transformation is applied.
     * 
     * @param mimetype
     *            Mimetype destination for the transformation
     * @param options
     *            Image convert command options
     * 
     * @return Node representing the newly transformed image.
     */
    public ScriptNode transformImage(String mimetype, String options)
    {
        return transformImage(mimetype, options, getPrimaryParentAssoc().getParentRef());
    }

    /**
     * Transform an image to a new image mimetype format. A copy of the image document is made in the specified destination folder and the extension changed to match the newmimetype, then then transformation is applied.
     * 
     * @param mimetype
     *            Mimetype destination for the transformation
     * @param destination
     *            Destination folder location
     * 
     * @return Node representing the newly transformed image.
     */
    public ScriptNode transformImage(String mimetype, ScriptNode destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);
        return transformImage(mimetype, null, destination.getNodeRef());
    }

    /**
     * Transform an image to a new image mimetype format. A copy of the image document is made in the specified destination folder and the extension changed to match the new mimetype, then then transformation is applied.
     * 
     * @param mimetype
     *            Mimetype destination for the transformation
     * @param options
     *            Image convert command options
     * @param destination
     *            Destination folder location
     * 
     * @return Node representing the newly transformed image.
     */
    public ScriptNode transformImage(String mimetype, String options, ScriptNode destination)
    {
        ParameterCheck.mandatory("Destination Node", destination);
        return transformImage(mimetype, options, destination.getNodeRef());
    }

    private ScriptNode transformImage(String mimetype, final String options, NodeRef destination)
    {
        ParameterCheck.mandatoryString("Mimetype", mimetype);
        final NodeRef sourceNodeRef = nodeRef;

        // the delegate definition for transforming an image
        Transformer transformer = new AbstractTransformer() {
            protected void doTransform(SynchronousTransformClient synchronousTransformClient, ContentReader reader, ContentWriter writer)
            {
                Map<String, String> actualOptions = new HashMap<>(1);
                if (options != null || !options.trim().isEmpty())
                {
                    actualOptions.put(ImageTransformationOptions.OPT_COMMAND_OPTIONS, options);
                }
                transformNodeRef(synchronousTransformClient, reader, writer, actualOptions, sourceNodeRef);
            }
        };

        return transformNode(transformer, mimetype, destination);
    }

    /**
     * Process a FreeMarker Template against the current node.
     * 
     * @param template
     *            Node of the template to execute
     * 
     * @return output of the template execution
     */
    public String processTemplate(ScriptNode template)
    {
        ParameterCheck.mandatory("Template Node", template);
        return processTemplate(template.getContent(), null, null);
    }

    /**
     * Process a FreeMarker Template against the current node.
     * 
     * @param template
     *            Node of the template to execute
     * @param args
     *            Scriptable object (generally an associative array) containing the name/value pairs of arguments to be passed to the template
     * 
     * @return output of the template execution
     */
    public String processTemplate(ScriptNode template, Object args)
    {
        ParameterCheck.mandatory("Template Node", template);
        return processTemplate(template.getContent(), null, (ScriptableObject) args);
    }

    /**
     * Process a FreeMarker Template against the current node.
     * 
     * @param template
     *            The template to execute
     * 
     * @return output of the template execution
     */
    public String processTemplate(String template)
    {
        ParameterCheck.mandatoryString("Template", template);
        return processTemplate(template, null, null);
    }

    /**
     * Process a FreeMarker Template against the current node.
     * 
     * @param template
     *            The template to execute
     * @param args
     *            Scriptable object (generally an associative array) containing the name/value pairs of arguments to be passed to the template
     * 
     * @return output of the template execution
     */
    public String processTemplate(String template, Object args)
    {
        ParameterCheck.mandatoryString("Template", template);
        return processTemplate(template, null, (ScriptableObject) args);
    }

    private String processTemplate(String template, NodeRef templateRef, ScriptableObject args)
    {
        Object person = (Object) scope.get("person", scope);
        Object companyhome = (Object) scope.get("companyhome", scope);
        Object userhome = (Object) scope.get("userhome", scope);

        // build default model for the template processing
        Map<String, Object> model = this.services.getTemplateService().buildDefaultModel(
                (person.equals(UniqueTag.NOT_FOUND)) ? null : ((ScriptNode) ((Wrapper) person).unwrap()).getNodeRef(),
                (companyhome.equals(UniqueTag.NOT_FOUND)) ? null : ((ScriptNode) ((Wrapper) companyhome).unwrap()).getNodeRef(),
                (userhome.equals(UniqueTag.NOT_FOUND)) ? null : ((ScriptNode) ((Wrapper) userhome).unwrap()).getNodeRef(),
                templateRef,
                null);

        // add the current node as either the document/space as appropriate
        if (this.getIsDocument())
        {
            model.put("document", this.nodeRef);
            model.put("space", getPrimaryParentAssoc().getParentRef());
        }
        else
        {
            model.put("space", this.nodeRef);
        }

        // add the supplied args to the 'args' root object
        if (args != null)
        {
            // we need to get all the keys to the properties provided
            // and convert them to a Map of QName to Serializable objects
            Object[] propIds = args.getIds();
            Map<String, String> templateArgs = new HashMap<String, String>(propIds.length);
            for (int i = 0; i < propIds.length; i++)
            {
                // work on each key in turn
                Object propId = propIds[i];

                // we are only interested in keys that are formed of Strings i.e. QName.toString()
                if (propId instanceof String)
                {
                    // get the value out for the specified key - make sure it is Serializable
                    Object value = args.get((String) propId, args);
                    value = getValueConverter().convertValueForRepo((Serializable) value);
                    if (value != null)
                    {
                        templateArgs.put((String) propId, value.toString());
                    }
                }
            }
            // add the args to the model as the 'args' root object
            model.put("args", templateArgs);
        }

        // execute template!
        // TODO: check that script modified nodes are reflected...
        return this.services.getTemplateService().processTemplateString(null, template, model);
    }

    // ------------------------------------------------------------------------------
    // Thumbnail Methods

    /**
     * Creates a thumbnail for the content property of the node.
     * 
     * The thumbnail name correspionds to pre-set thumbnail details stored in the repository.
     * 
     * @param thumbnailName
     *            the name of the thumbnail
     * @return ScriptThumbnail the newly create thumbnail node
     */
    public ScriptThumbnail createThumbnail(String thumbnailName)
    {
        return createThumbnail(thumbnailName, false);
    }

    /**
     * Creates a thumbnail for the content property of the node.
     * 
     * The thumbnail name corresponds to pre-set thumbnail details stored in the repository.
     * 
     * If the thumbnail is created asynchronously then the result will be null and creation of the thumbnail will occure at some point in the background.
     * 
     * @param thumbnailName
     *            the name of the thumbnail
     * @param async
     *            indicates whether the thumbnail is create asynchronously or not
     * @return ScriptThumbnail the newly create thumbnail node or null if async creation occures
     *
     * @deprecated The async flag in the method signature will not be applicable as all of the future transformations will be asynchronous
     */
    @Deprecated
    public ScriptThumbnail createThumbnail(String thumbnailName, boolean async)
    {
        return createThumbnail(thumbnailName, async, false);
    }

    /**
     * Creates a thumbnail for the content property of the node.
     * 
     * The thumbnail name corresponds to pre-set thumbnail details stored in the repository.
     * 
     * If the thumbnail is created asynchronously then the result will be null and creation of the thumbnail will occure at some point in the background.
     * 
     * If foce param specified system.thumbnail.generate is ignoring. Could be used for preview creation
     * 
     * @param thumbnailName
     *            the name of the thumbnail
     * @param async
     *            indicates whether the thumbnail is create asynchronously or not
     * @param force
     *            ignore system.thumbnail.generate=false
     * @return ScriptThumbnail the newly create thumbnail node or null if async creation occures
     *
     * @deprecated The async flag in the method signature will not be applicable as all of the future transformations will be asynchronous
     */
    @Deprecated
    public ScriptThumbnail createThumbnail(String thumbnailName, boolean async, boolean force)
    {
        final ThumbnailService thumbnailService = services.getThumbnailService();

        ScriptThumbnail result = null;

        // If thumbnail generation has been configured off, then don't bother with any of this.
        // We need to create preview for node even if system.thumbnail.generate=false
        if (force || thumbnailService.getThumbnailsEnabled())
        {
            // Use the thumbnail registy to get the details of the thumbail
            ThumbnailRegistry registry = thumbnailService.getThumbnailRegistry();
            ThumbnailDefinition details = registry.getThumbnailDefinition(thumbnailName);
            if (details == null)
            {
                // Throw exception
                throw new ScriptException("The thumbnail name '" + thumbnailName + "' is not registered");
            }

            // If there's nothing currently registered to generate thumbnails for the
            // specified mimetype, then log a message and bail out
            String nodeMimeType = getMimetype();
            Serializable value = this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
            if (!ContentData.hasContent(contentData))
            {
                if (logger.isDebugEnabled())
                    logger.debug("Unable to create thumbnail '" + details.getName() + "' as there is no content");
                return null;
            }
            if (!registry.isThumbnailDefinitionAvailable(contentData.getContentUrl(), nodeMimeType, getSize(), nodeRef, details))
            {
                logger.info("Unable to create thumbnail '" + details.getName() + "' for " +
                        nodeMimeType + " as no transformer is currently available.");
                return null;
            }

            // Have the thumbnail created
            if (async == false)
            {
                try
                {
                    // Create the thumbnail
                    NodeRef thumbnailNodeRef = thumbnailService.createThumbnail(
                            this.nodeRef,
                            ContentModel.PROP_CONTENT,
                            details.getMimetype(),
                            details.getTransformationOptions(),
                            details.getName());

                    // Create the thumbnail script object
                    result = new ScriptThumbnail(thumbnailNodeRef, this.services, this.scope);
                }
                catch (AlfrescoRuntimeException e)
                {
                    Throwable rootCause = e.getRootCause();
                    if (rootCause instanceof UnimportantTransformException)
                    {
                        logger.debug("Unable to create thumbnail '" + details.getName() + "' as " + rootCause.getMessage());
                        return null;
                    }
                    throw e;
                }
            }
            else
            {
                RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(thumbnailName);
                if (renditionDefinition != null)
                {
                    renditionService2.render(nodeRef, thumbnailName);
                }
                else
                {
                    Action action = ThumbnailHelper.createCreateThumbnailAction(details, services);

                    // Queue async creation of thumbnail
                    this.services.getActionService().executeAction(action, this.nodeRef, true, true);
                }
            }
        }
        return result;
    }

    /**
     * Get the given thumbnail for the content property
     * 
     * @param thumbnailName
     *            the thumbnail name
     * @return ScriptThumbnail the thumbnail
     */
    public ScriptThumbnail getThumbnail(String thumbnailName)
    {
        ScriptThumbnail result = null;
        NodeRef thumbnailNodeRef = this.services.getThumbnailService().getThumbnailByName(
                this.nodeRef,
                ContentModel.PROP_CONTENT,
                thumbnailName);
        if (thumbnailNodeRef != null)
        {
            result = new ScriptThumbnail(thumbnailNodeRef, this.services, this.scope);
        }
        return result;
    }

    /**
     * Get the all the thumbnails for a given node's content property.
     * 
     * @return Scriptable list of thumbnails, empty if none available
     */
    public ScriptThumbnail[] getThumbnails()
    {
        List<NodeRef> thumbnails = this.services.getThumbnailService().getThumbnails(
                this.nodeRef,
                ContentModel.PROP_CONTENT,
                null,
                null);

        List<ScriptThumbnail> result = new ArrayList<ScriptThumbnail>(thumbnails.size());
        for (NodeRef thumbnail : thumbnails)
        {
            ScriptThumbnail scriptThumbnail = new ScriptThumbnail(thumbnail, this.services, this.scope);
            result.add(scriptThumbnail);
        }
        return (ScriptThumbnail[]) result.toArray(new ScriptThumbnail[result.size()]);
    }

    /**
     * Returns the names of the thumbnail defintions that can be applied to the content property of this node.
     * <p>
     * Thumbanil defintions only appear in this list if they can produce a thumbnail for the content found in the content property. This will be determined by looking at the mimetype of the content and the destinatino mimetype of the thumbnail.
     * 
     * @return String[] array of thumbnail names that are valid for the current content type
     */
    public String[] getThumbnailDefinitions()
    {
        ThumbnailService thumbnailService = this.services.getThumbnailService();

        List<String> result = new ArrayList<String>(7);

        Serializable value = this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);

        if (ContentData.hasContent(contentData))
        {
            String mimetype = contentData.getMimetype();
            List<ThumbnailDefinition> thumbnailDefinitions = thumbnailService.getThumbnailRegistry().getThumbnailDefinitions(mimetype, contentData.getSize());
            for (ThumbnailDefinition thumbnailDefinition : thumbnailDefinitions)
            {
                result.add(thumbnailDefinition.getName());
            }
        }

        return (String[]) result.toArray(new String[result.size()]);
    }

    /**
     * This version of the method name spelling is retained (for now) for backwards compatibility
     * 
     * @see #getThumbnailDefinitions()
     */
    @Deprecated
    public String[] getThumbnailDefintions()
    {
        return getThumbnailDefinitions();
    }

    // ------------------------------------------------------------------------------
    // Tag methods

    /**
     * Clear the node's tags
     */
    public void clearTags()
    {
        this.services.getTaggingService().clearTags(this.nodeRef);
        updateTagProperty();
    }

    /**
     * Adds a tag to the node
     * 
     * @param tag
     *            tag name
     */
    public void addTag(String tag)
    {
        this.services.getTaggingService().addTag(this.nodeRef, tag);
        updateTagProperty();
    }

    /**
     * Adds all the tags to the node
     * 
     * @param tags
     *            array of tag names
     */
    public void addTags(String[] tags)
    {
        this.services.getTaggingService().addTags(this.nodeRef, Arrays.asList(tags));
        updateTagProperty();
    }

    /**
     * Removes a tag from the node
     * 
     * @param tag
     *            tag name
     */
    public void removeTag(String tag)
    {
        this.services.getTaggingService().removeTag(this.nodeRef, tag);
        updateTagProperty();
    }

    /**
     * Removes all the tags from the node
     * 
     * @param tags
     *            array of tag names
     */
    public void removeTags(String[] tags)
    {
        this.services.getTaggingService().removeTags(this.nodeRef, Arrays.asList(tags));
        updateTagProperty();
    }

    /**
     * Get all the tags applied to this node
     * 
     * @return String[] array containing all the tag applied to this node
     */
    public String[] getTags()
    {
        String[] result = null;
        List<String> tags = this.services.getTaggingService().getTags(this.nodeRef);
        if (tags.isEmpty() == true)
        {
            result = new String[0];
        }
        else
        {
            result = (String[]) tags.toArray(new String[tags.size()]);
        }
        return result;
    }

    /**
     * Set the tags applied to this node. This overwirtes the list of tags currently applied to the node.
     * 
     * @param tags
     *            array of tags
     */
    public void setTags(String[] tags)
    {
        this.services.getTaggingService().setTags(this.nodeRef, Arrays.asList(tags));
        updateTagProperty();
    }

    private void updateTagProperty()
    {
        Serializable tags = this.services.getNodeService().getProperty(this.nodeRef, ContentModel.PROP_TAGS);
        if (this.properties != null)
        {
            this.properties.put(ContentModel.PROP_TAGS.toString(), getValueConverter().convertValueForScript(ContentModel.PROP_TAGS, tags));
        }
    }

    /**
     * Sets whether this node is a tag scope or not
     * 
     * @param value
     *            true if this node is a tag scope, false otherwise
     */
    public void setIsTagScope(boolean value)
    {
        boolean currentValue = this.services.getTaggingService().isTagScope(this.nodeRef);
        if (currentValue != value)
        {
            if (value == true)
            {
                // Add the tag scope
                this.services.getTaggingService().addTagScope(this.nodeRef);
            }
            else
            {
                // Remove the tag scope
                this.services.getTaggingService().removeTagScope(this.nodeRef);
            }
        }
    }

    /**
     * Gets whether this node is a tag scope
     * 
     * @return boolean true if this node is a tag scope, false otherwise
     */
    public boolean getIsTagScope()
    {
        return this.services.getTaggingService().isTagScope(this.nodeRef);
    }

    /**
     * Gets the 'nearest' tag scope to this node by travesing up the parent hierarchy untill one is found.
     * <p>
     * If none is found, null is returned.
     *
     * @return TagScope the 'nearest' tag scope
     */
    public TagScope getTagScope()
    {
        TagScope tagScope = null;
        org.alfresco.service.cmr.tagging.TagScope tagScopeImpl = this.services.getTaggingService().findTagScope(this.nodeRef);
        if (tagScopeImpl != null)
        {
            tagScope = new TagScope(this.services.getTaggingService(), tagScopeImpl);
        }
        return tagScope;
    }

    /**
     * Gets all (deep) children of this node that have the tag specified.
     * 
     * @param tag
     *            tag name
     * @return ScriptNode[] nodes that are deep children of the node with the tag
     */
    public ScriptNode[] childrenByTags(String tag)
    {
        List<NodeRef> nodeRefs = this.services.getTaggingService().findTaggedNodes(this.nodeRef.getStoreRef(), tag, this.nodeRef);
        ScriptNode[] nodes = new ScriptNode[nodeRefs.size()];
        int index = 0;
        for (NodeRef node : nodeRefs)
        {
            nodes[index] = new ScriptNode(node, this.services, this.scope);
            index++;
        }
        return nodes;
    }

    // ------------------------------------------------------------------------------
    // Workflow methods

    /**
     * Get active workflow instances this node belongs to
     * 
     * @return the active workflow instances this node belongs to
     */
    public Scriptable getActiveWorkflows()
    {
        if (this.activeWorkflows == null)
        {
            WorkflowService workflowService = this.services.getWorkflowService();

            List<WorkflowInstance> workflowInstances = workflowService.getWorkflowsForContent(this.nodeRef, true);
            Object[] jsWorkflowInstances = new Object[workflowInstances.size()];
            int index = 0;
            for (WorkflowInstance workflowInstance : workflowInstances)
            {
                jsWorkflowInstances[index++] = new JscriptWorkflowInstance(workflowInstance, this.services, this.scope);
            }
            this.activeWorkflows = Context.getCurrentContext().newArray(this.scope, jsWorkflowInstances);
        }

        return this.activeWorkflows;
    }

    // ------------------------------------------------------------------------------
    // Site methods

    /**
     * Returns the short name of the site this node is located within. If the node is not located within a site null is returned.
     * 
     * @return The short name of the site this node is located within, null if the node is not located within a site.
     */
    public String getSiteShortName()
    {
        if (!this.siteNameResolved)
        {
            this.siteNameResolved = true;

            Path path = this.services.getNodeService().getPath(getNodeRef());

            if (logger.isDebugEnabled())
                logger.debug("Determing if node is within a site using path: " + path);

            for (int i = 0; i < path.size(); i++)
            {
                if ("st:sites".equals(path.get(i).getPrefixedString(this.services.getNamespaceService())))
                {
                    // we now know the node is in a site, find the next element in the array (if there is one)
                    if ((i + 1) < path.size())
                    {
                        // get the site name
                        Path.Element siteName = path.get(i + 1);

                        // remove the "cm:" prefix and add to result object
                        this.siteName = ISO9075.decode(siteName.getPrefixedString(
                                this.services.getNamespaceService()).substring(3));
                    }

                    break;
                }
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug(this.siteName != null ? "Node is in the site named \"" + this.siteName + "\"" : "Node is not in a site");
        }

        return this.siteName;
    }

    // ------------------------------------------------------------------------------
    // Helper methods

    /**
     * Override Object.toString() to provide useful debug output
     */
    public String toString()
    {
        if (this.nodeService.exists(nodeRef))
        {
            if (this.services.getPermissionService().hasPermission(nodeRef, PermissionService.READ_PROPERTIES) == AccessStatus.ALLOWED)
            {
                // TODO: DC: Allow debug output of property values - for now it's disabled as this could potentially
                // follow a large network of nodes.
                return "Node Type: " + getType() + ", Node Aspects: " + getAspectsSet().toString();
            }
            else
            {
                return "Access denied to node " + nodeRef;
            }

        }
        else
        {
            return "Node no longer exists: " + nodeRef;
        }
    }

    /**
     * Returns the JSON representation of this node.
     * 
     * @param useShortQNames
     *            if true short-form qnames will be returned, else long-form.
     * @return The JSON representation of this node
     */
    public String toJSON(boolean useShortQNames)
    {
        // This method is used by the /api/metadata web script
        String jsonStr = "{}";

        if (this.nodeService.exists(nodeRef))
        {
            if (this.services.getPublicServiceAccessService().hasAccess(ServiceRegistry.NODE_SERVICE.getLocalName(), "getProperties", this.nodeRef) == AccessStatus.ALLOWED)
            {
                JSONObject json = new JSONObject();

                try
                {
                    // add type info
                    json.put("nodeRef", this.getNodeRef().toString());

                    String typeString = useShortQNames ? getShortQName(this.getQNameType())
                            : this.getType();
                    json.put("type", typeString);
                    json.put("mimetype", this.getMimetype());

                    // Fetch all properties
                    Map<QName, Serializable> nodeProperties = this.nodeService.getProperties(this.nodeRef);

                    // Do any special conversion steps that are needed
                    for (QName longQName : nodeProperties.keySet())
                    {
                        Serializable value = nodeProperties.get(longQName);

                        if (value instanceof Date)
                        {
                            value = ISO8601DateFormat.format((Date) value);
                            nodeProperties.put(longQName, value);
                        }
                        if (value instanceof NodeRef)
                        {
                            value = ((NodeRef) value).toString();
                            nodeProperties.put(longQName, value);
                        }
                    }

                    if (useShortQNames)
                    {
                        Map<String, Serializable> nodePropertiesShortQNames = new LinkedHashMap<String, Serializable>(nodeProperties.size());
                        for (QName nextLongQName : nodeProperties.keySet())
                        {
                            try
                            {
                                String nextShortQName = getShortQName(nextLongQName);
                                nodePropertiesShortQNames.put(nextShortQName, nodeProperties.get(nextLongQName));
                            }
                            catch (NamespaceException ne)
                            {
                                // ignore properties that do not have a registered namespace

                                if (logger.isDebugEnabled())
                                    logger.debug("Ignoring property '" + nextLongQName + "' as it's namespace is not registered");
                            }
                        }
                        json.put("properties", nodePropertiesShortQNames);
                    }
                    else
                    {
                        json.put("properties", nodeProperties);
                    }

                    // add aspects as an array
                    Set<QName> nodeAspects = this.nodeService.getAspects(this.nodeRef);
                    if (useShortQNames)
                    {
                        Set<String> nodeAspectsShortQNames = new LinkedHashSet<String>(nodeAspects.size());
                        for (QName nextLongQName : nodeAspects)
                        {
                            String nextShortQName = getShortQName(nextLongQName);
                            nodeAspectsShortQNames.add(nextShortQName);
                        }
                        json.put("aspects", nodeAspectsShortQNames);
                    }
                    else
                    {
                        json.put("aspects", nodeAspects);
                    }
                }
                catch (JSONException error)
                {
                    error.printStackTrace();
                }

                jsonStr = json.toString();
            }
        }

        return jsonStr;
    }

    /**
     * Returns the JSON representation of this node. Long-form QNames are used in the result.
     * 
     * @return The JSON representation of this node
     */
    public String toJSON()
    {
        return this.toJSON(false);
    }

    /**
     * Given a long-form QName, this method uses the namespace service to create a short-form QName string.
     * 
     * @param longQName
     *            QName
     * @return the short form of the QName string, e.g. "cm:content"
     */
    protected String getShortQName(QName longQName)
    {
        return longQName.toPrefixString(services.getNamespaceService());
    }

    /**
     * Helper to create a QName from either a fully qualified or short-name QName string
     * 
     * @param s
     *            Fully qualified or short-name QName string
     * 
     * @return QName
     */
    protected QName createQName(String s)
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
     * Reset the Node cached state
     */
    public void reset()
    {
        this.name = null;
        this.type = null;
        this.properties = null;
        this.aspects = null;
        this.targetAssocs = null;
        this.sourceAssocs = null;
        this.childAssocs = null;
        this.children = null;
        this.hasChildren = null;
        this.parentAssocs = null;
        this.displayPath = null;
        this.qnamePath = null;
        this.isDocument = null;
        this.isContainer = null;
        this.parent = null;
        this.primaryParentAssoc = null;
        this.activeWorkflows = null;
        this.siteName = null;
        this.siteNameResolved = false;
    }

    /**
     * Return a list or a single Node from executing an xpath against the parent Node.
     * 
     * @param xpath
     *            XPath to execute
     * @param firstOnly
     *            True to return the first result only
     * 
     * @return Object[] can be empty but never null
     */
    private Object[] getChildrenByXPath(String xpath, QueryParameterDefinition[] params, boolean firstOnly)
    {
        Object[] result = null;

        if (xpath.length() != 0)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Executing xpath: " + xpath);
                if (params != null)
                {
                    for (int i = 0; i < params.length; i++)
                    {
                        logger.debug(" [" + params[i].getQName() + "]=" + params[i].getDefault());
                    }
                }
            }

            List<NodeRef> nodes = this.services.getSearchService().selectNodes(this.nodeRef, xpath, params,
                    this.services.getNamespaceService(), false);

            // see if we only want the first result
            if (firstOnly == true)
            {
                if (nodes.size() != 0)
                {
                    result = new Object[1];
                    result[0] = newInstance(nodes.get(0), this.services, this.scope);
                }
            }
            // or all the results
            else
            {
                result = new Object[nodes.size()];
                for (int i = 0; i < nodes.size(); i++)
                {
                    NodeRef ref = nodes.get(i);
                    result[i] = newInstance(ref, this.services, this.scope);
                }
            }
        }

        return result != null ? result : new Object[0];
    }

    /**
     * Helper to return true if the supplied property value is a ScriptContentData object
     * 
     * @param o
     *            Object to test
     * 
     * @return true if instanceof ScriptContentData, false otherwise
     */
    public boolean isScriptContent(Object o)
    {
        return (o instanceof ScriptContentData);
    }

    // ------------------------------------------------------------------------------
    // Value Conversion

    /**
     * Gets the node value converter
     * 
     * @return the node value converter
     */
    protected NodeValueConverter getValueConverter()
    {
        if (converter == null)
        {
            converter = createValueConverter();
        }
        return converter;
    }

    /**
     * Constructs the node value converter
     * 
     * @return the node value converter
     */
    protected NodeValueConverter createValueConverter()
    {
        return new NodeValueConverter();
    }

    // Set support

    /**
     * Value converter with knowledge of Node specific value types
     */
    public class NodeValueConverter extends ValueConverter
    {
        /**
         * Convert an object from any repository serialized value to a valid script object. This includes converting Collection multi-value properties into JavaScript Array objects.
         * 
         * @param qname
         *            QName of the property value for conversion
         * @param value
         *            Property value
         * 
         * @return Value safe for scripting usage
         */
        public Serializable convertValueForScript(QName qname, Serializable value)
        {
            return convertValueForScript(services, scope, qname, value);
        }

        /* (non-Javadoc)
         * 
         * @see org.alfresco.repo.jscript.ValueConverter#convertValueForScript(org.alfresco.service.ServiceRegistry, org.mozilla.javascript.Scriptable, org.alfresco.service.namespace.QName, java.io.Serializable) */
        @Override
        public Serializable convertValueForScript(ServiceRegistry services, Scriptable scope, QName qname,
                Serializable value)
        {
            if (value instanceof ContentData)
            {
                // ContentData object properties are converted to ScriptContentData objects
                // so the content and other properties of those objects can be accessed
                value = new ScriptContentData((ContentData) value, qname);
            }
            else
            {
                value = super.convertValueForScript(services, scope, qname, value);
            }
            return value;
        }

        /* (non-Javadoc)
         * 
         * @see org.alfresco.repo.jscript.ValueConverter#convertValueForRepo(java.io.Serializable) */
        @Override
        public Serializable convertValueForRepo(Serializable value)
        {
            if (value instanceof ScriptContentData)
            {
                // convert back to ContentData
                value = ((ScriptContentData) value).contentData;
            }
            else
            {
                value = super.convertValueForRepo(value);
            }
            return value;
        }
    }

    // ------------------------------------------------------------------------------
    // Inner Classes

    /**
     * Inner class wrapping and providing access to a ContentData property
     */
    public class ScriptContentData implements Content, Serializable
    {
        private static final long serialVersionUID = -7819328543933312278L;

        /**
         * Constructor
         * 
         * @param contentData
         *            The ContentData object this object wraps
         * @param property
         *            The property the ContentData is attached too
         */
        public ScriptContentData(ContentData contentData, QName property)
        {
            this.contentData = contentData;
            this.property = property;
            this.isDirty = ContentData.hasContent(contentData);
        }

        /* (non-Javadoc)
         * 
         * @see org.alfresco.repo.jscript.ScriptNode.ScriptContent#getContent() */
        public String getContent()
        {
            ContentService contentService = services.getContentService();
            ContentReader reader = contentService.getReader(nodeRef, property);
            return (reader != null && reader.exists()) ? reader.getContentString() : "";
        }

        /* (non-Javadoc)
         * 
         * @see org.springframework.extensions.surf.util.Content#getInputStream() */
        public InputStream getInputStream()
        {
            ContentService contentService = services.getContentService();
            ContentReader reader = contentService.getReader(nodeRef, property);
            return (reader != null && reader.exists()) ? reader.getContentInputStream() : null;
        }

        /* (non-Javadoc)
         * 
         * @see org.springframework.extensions.surf.util.Content#getReader() */
        public Reader getReader()
        {
            ContentService contentService = services.getContentService();
            ContentReader reader = contentService.getReader(nodeRef, property);

            if (reader != null && reader.exists())
            {
                try
                {
                    return (contentData.getEncoding() == null) ? new InputStreamReader(reader.getContentInputStream()) : new InputStreamReader(reader.getContentInputStream(), contentData.getEncoding());
                }
                catch (IOException e)
                {
                    // NOTE: fall-through
                }
            }
            return null;
        }

        /**
         * @return <code>true</code> if the contentData has a binary (content URL) associated and the updates on contentData and related properties should be saved. <code>false</code> if the contentData has a temporary value and no actual binary to be persisted.
         */
        public boolean isDirty()
        {
            return this.isDirty;
        }

        /**
         * Set the content stream
         * 
         * @param content
         *            Content string to set
         */
        public void setContent(String content)
        {
            ContentService contentService = services.getContentService();
            ContentWriter writer = contentService.getWriter(nodeRef, this.property, true);
            writer.setMimetype(getMimetype()); // use existing mimetype value
            writer.putContent(content);

            // update cached variables after putContent()
            updateContentData(true);
        }

        /**
         * Set the content stream from another content object.
         * 
         * @param content
         *            ScriptContent to set
         */
        public void write(Content content)
        {
            ContentService contentService = services.getContentService();
            ContentWriter writer = contentService.getWriter(nodeRef, this.property, true);
            writer.setMimetype(content.getMimetype());
            writer.setEncoding(content.getEncoding());
            writer.putContent(content.getInputStream());

            // update cached variables after putContent()
            updateContentData(true);
        }

        /**
         * Set the content stream from another content object.
         *
         * @param content
         *            ScriptContent to set
         * @param applyMimetype
         *            If true, apply the mimetype from the Content object, else leave the original mimetype
         * @param guessEncoding
         *            If true, guess the encoding from the underlying input stream, else use encoding set in the Content object as supplied.
         */
        @Deprecated
        public void write(Content content, boolean applyMimetype, boolean guessEncoding)
        {
            write(content, applyMimetype, guessEncoding, null);
        }

        /**
         * Set the content stream from another content object.
         * 
         * @param content
         *            ScriptContent to set
         * @param applyMimetype
         *            If true, apply the mimetype from the Content object, else leave the original mimetype
         * @param guessEncoding
         *            If true, guess the encoding from the underlying input stream, else use encoding set in the Content object as supplied.
         * @param fileName
         *            The filename for the attachment.
         */
        public void write(Content content, boolean applyMimetype, boolean guessEncoding, String fileName)
        {
            ContentService contentService = services.getContentService();
            ContentWriter writer = contentService.getWriter(nodeRef, this.property, true);
            InputStream is = null;
            if (applyMimetype)
            {
                if (fileName != null && !fileName.isEmpty())
                {
                    writer.setMimetype(services.getMimetypeService().guessMimetype(fileName));
                }
                else
                {
                    writer.setMimetype(content.getMimetype().toLowerCase());
                }
            }
            if (guessEncoding)
            {
                is = new BufferedInputStream(content.getInputStream());
                is.mark(1024);
                writer.setEncoding(guessEncoding(is, false));
                try
                {
                    is.reset();
                }
                catch (IOException e)
                {}
            }
            else
            {
                writer.setEncoding(content.getEncoding());
                is = content.getInputStream();
            }
            writer.putContent(is);

            // update cached variables after putContent()
            updateContentData(true);
        }

        /**
         * Set the content stream from another input stream.
         * 
         * @param inputStream
         *            InputStream
         */
        public void write(InputStream inputStream)
        {
            ContentService contentService = services.getContentService();
            ContentWriter writer = contentService.getWriter(nodeRef, this.property, true);
            writer.putContent(inputStream);

            // update cached variables after putContent()
            updateContentData(true);
        }

        /**
         * Delete the content stream
         */
        public void delete()
        {
            ContentService contentService = services.getContentService();
            ContentWriter writer = contentService.getWriter(nodeRef, this.property, true);
            OutputStream output = writer.getContentOutputStream();
            try
            {
                output.close();
            }
            catch (IOException e)
            {
                // NOTE: fall-through
            }
            writer.setMimetype(null);
            writer.setEncoding(null);

            // update cached variables after putContent()
            updateContentData(true);
        }

        /**
         * @return download URL to the content
         */
        public String getUrl()
        {
            return MessageFormat.format(CONTENT_PROP_URL, new Object[]{nodeRef.getStoreRef().getProtocol(),
                    nodeRef.getStoreRef().getIdentifier(), nodeRef.getId(),
                    URLEncoder.encode(getName()),
                    URLEncoder.encode(property.toString())});
        }

        /**
         * @return download URL to the content for a document item only
         */
        public String getDownloadUrl()
        {
            if (getIsDocument() == true)
            {
                return MessageFormat.format(CONTENT_DOWNLOAD_PROP_URL, new Object[]{
                        nodeRef.getStoreRef().getProtocol(),
                        nodeRef.getStoreRef().getIdentifier(),
                        nodeRef.getId(),
                        URLEncoder.encode(getName()),
                        URLEncoder.encode(property.toString())});
            }
            else
            {
                return "";
            }
        }

        public long getSize()
        {
            return contentData.getSize();
        }

        public String getMimetype()
        {
            return contentData.getMimetype();
        }

        public String getEncoding()
        {
            return contentData.getEncoding();
        }

        public void setEncoding(String encoding)
        {
            this.contentData = ContentData.setEncoding(this.contentData, encoding);
            services.getNodeService().setProperty(nodeRef, this.property, this.contentData);
            updateContentData(false);
        }

        public void setMimetype(String mimetype)
        {
            mimetype = mimetype.toLowerCase();
            this.contentData = ContentData.setMimetype(this.contentData, mimetype);
            services.getNodeService().setProperty(nodeRef, this.property, this.contentData);
            updateContentData(false);
        }

        /**
         * Guess the mimetype for the given filename - uses the extension to match on system mimetype map
         */
        public void guessMimetype(String filename)
        {
            ContentService contentService = services.getContentService();
            ContentReader reader = contentService.getReader(nodeRef, property);
            // MNT-12265 Browser sets a mimetype based on extension of file. But mimeType from browser can be
            // different as mapped in Alfresco for current extension. Therefore we need to guess a mimetype based on
            // map in Alfresco
            String typeByExt = services.getMimetypeService().guessMimetype(filename);
            if (reader != null && reader.getMimetype() != null && !typeByExt.equals(MimetypeMap.MIMETYPE_BINARY))
            {
                setMimetype(typeByExt);
            }
            else
            {
                setMimetype(services.getMimetypeService().guessMimetype(filename, reader));
            }
        }

        /**
         * Guess the character encoding of a file. For non-text files UTF-8 default is applied, otherwise the appropriate encoding (such as UTF-16 or similar) will be appiled if detected.
         */
        public void guessEncoding()
        {
            setEncoding(guessEncoding(getInputStream(), true));
        }

        private String guessEncoding(InputStream in, boolean close)
        {
            String encoding = "UTF-8";
            try
            {
                if (in != null)
                {
                    Charset charset = services.getMimetypeService().getContentCharsetFinder().getCharset(in, getMimetype());
                    encoding = charset.name();
                }
            }
            finally
            {
                try
                {
                    if (close && in != null)
                    {
                        in.close();
                    }
                }
                catch (IOException ioErr)
                {}
            }
            return encoding;
        }

        /**
         * Update cached contentData and the isDirty flag
         */
        private void updateContentData(boolean touchContent)
        {
            this.contentData = (ContentData) services.getNodeService().getProperty(nodeRef, this.property);
            this.isDirty = touchContent ? true : this.isDirty;
        }

        private ContentData contentData;
        private QName property;
        private boolean isDirty;
    }

    /**
     * Interface contract for simple anonymous classes that implement document transformations
     */
    private interface Transformer
    {
        /**
         * Transform the reader to the specified writer
         * 
         * @param synchronousTransformClient
         * @param noderef
         *            NodeRef of the destination for the transform
         * @param reader
         *            Source reader
         * @param writer
         *            Destination writer
         *
         * @return Node representing the transformed entity
         */
        ScriptNode transform(SynchronousTransformClient synchronousTransformClient, NodeRef noderef,
                ContentReader reader, ContentWriter writer);
    }

    private abstract class AbstractTransformer implements Transformer
    {
        public ScriptNode transform(SynchronousTransformClient synchronousTransformClient, NodeRef nodeRef,
                ContentReader reader, ContentWriter writer)
        {
            ScriptNode transformedNode = null;

            try
            {
                doTransform(synchronousTransformClient, reader, writer);
                transformedNode = newInstance(nodeRef, services, scope);
            }
            catch (NoTransformerException e)
            {
                // ignore
            }
            catch (AlfrescoRuntimeException e)
            {
                Throwable rootCause = ((AlfrescoRuntimeException) e).getRootCause();
                String message = rootCause.getMessage();
                message = message == null ? "" : message;
                if (rootCause instanceof UnimportantTransformException)
                {
                    logger.debug(message);
                    // ignore
                }
                else if (rootCause instanceof UnsupportedTransformationException)
                {
                    logger.error(message);
                    // ignore
                }
                else
                {
                    throw e;
                }
            }
            return transformedNode;
        }

        protected abstract void doTransform(SynchronousTransformClient synchronousTransformClient,
                ContentReader reader, ContentWriter writer);
    };

    /**
     * NamespacePrefixResolverProvider getter implementation
     */
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return this.services.getNamespaceService();
    }
}
