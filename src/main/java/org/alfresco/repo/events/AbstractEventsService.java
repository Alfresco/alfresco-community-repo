/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.events.EventRegistry;
import org.alfresco.events.types.Event;
import org.alfresco.events.types.TransactionCommittedEvent;
import org.alfresco.events.types.TransactionRolledBackEvent;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.AlfrescoCmisServiceCall;
import org.alfresco.repo.Client;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.FileFilterMode;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gytheio.messaging.MessageProducer;
import org.gytheio.messaging.MessagingException;

/**
 * 
 * @author steveglover
 *
 */
public abstract class AbstractEventsService extends TransactionListenerAdapter
{
    private static Log logger = LogFactory.getLog(AbstractEventsService.class);

	private static final String EVENTS_KEY = "camel.events";

    private MessageProducer messageProducer;

    protected CheckOutCheckInService cociService;
    protected SiteService siteService;
    protected NodeService nodeService;
    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;
    protected PermissionService permissionService;
	protected HiddenAspect hiddenAspect;
	protected EventRegistry eventRegistry;
	protected FileFolderService fileFolderService;
	protected TransactionService transactionService;

	protected Set<String> includeEventTypes;
	protected Set<QName> matchingTypes = new HashSet<QName>();

	protected boolean sendEventsBeforeCommit = true;

	public void setCociService(CheckOutCheckInService cociService)
	{
		this.cociService = cociService;
	}

	public void setHiddenAspect(HiddenAspect hiddenAspect)
	{
        this.hiddenAspect = hiddenAspect;
    }

    public void setSendEventsBeforeCommit(boolean sendEventsBeforeCommit)
	{
        this.sendEventsBeforeCommit = sendEventsBeforeCommit;
    }

	public void setTransactionService(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

	public void setIncludeEventTypes(String includeEventTypesStr)
	{
		StringTokenizer st = new StringTokenizer(includeEventTypesStr, ",");
		this.includeEventTypes = new HashSet<String>();
		while(st.hasMoreTokens())
		{
			String eventType = st.nextToken().trim();
			this.includeEventTypes.add(eventType);
		}
	}

	public void setFileFolderService(FileFolderService fileFolderService)
	{
		this.fileFolderService = fileFolderService;
	}

	public void setEventRegistry(EventRegistry eventRegistry)
	{
		this.eventRegistry = eventRegistry;
	}

	public void setNamespaceService(NamespaceService namespaceService)
	{
		this.namespaceService = namespaceService;
	}

	public void setPermissionService(PermissionService permissionService)
	{
		this.permissionService = permissionService;
	}

	public void setMessageProducer(MessageProducer messageProducer)
	{
		this.messageProducer = messageProducer;
	}

	public void setDictionaryService(DictionaryService dictionaryService)
	{
		this.dictionaryService = dictionaryService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void init()
	{
	}

	protected long nextSequenceNumber()
	{
    	TxnEvents events = getTxnEvents();
    	long seqNumber = events.nextSeqNumber();
    	return seqNumber;
	}

	@Override
    public void beforeCommit(boolean readOnly)
    {
	    if(sendEventsBeforeCommit)
	    {
	        // send all events
    		final TxnEvents events = (TxnEvents)AlfrescoTransactionSupport.getResource(EVENTS_KEY);
    		if(events != null)
    		{
    			events.sendEvents();
    		}
	    }
    }

	protected Client getAlfrescoClient(Client knownClientType)
	{
		String alfrescoClientId = null;

		CallContext context = AlfrescoCmisServiceCall.get();
		if(context != null)
		{
			HttpServletRequest request = (HttpServletRequest)context.get(CallContext.HTTP_SERVLET_REQUEST);
			if(request != null)
			{
				alfrescoClientId = (String)request.getHeader("alfrescoClientId");
				return new Client(Client.ClientType.cmis, alfrescoClientId);
			}
		}

		return knownClientType;
	}

    @Override
    public void afterCommit()
    {
	    if(sendEventsBeforeCommit)
	    {
	        // send txn committed event
    		String txnId = AlfrescoTransactionSupport.getTransactionId();
        	long timestamp = System.currentTimeMillis();
            String networkId = TenantUtil.getCurrentDomain();
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            Client alfrescoClient = getAlfrescoClient(null);

    		final Event event = new TransactionCommittedEvent(nextSequenceNumber(), txnId, networkId, timestamp, username,
    				alfrescoClient);

    		if (logger.isDebugEnabled())
    		{
    			logger.debug("sendEvent "+event);
    		}

    		// Need to execute this in another read txn because Camel/JMS expects it (the config now seems to
    		// require a txn)
    		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
			{
				@Override
				public Void execute() throws Throwable
				{
					messageProducer.send(event);

	                return null;
                }
			}, true, false);
	    }
	    else
	    {
	        // send all events
            final TxnEvents events = (TxnEvents)AlfrescoTransactionSupport.getResource(EVENTS_KEY);
            if(events != null)
            {
            	events.sendEvents();
            }
	    }
    }

	@Override
	public void afterRollback()
	{
		String txnId = AlfrescoTransactionSupport.getTransactionId();
		long timestamp = System.currentTimeMillis();
		String networkId = TenantUtil.getCurrentDomain();
		String username = AuthenticationUtil.getFullyAuthenticatedUser();
		Client alfrescoClient = getAlfrescoClient(null);

		Event event = new TransactionRolledBackEvent(nextSequenceNumber(), txnId, networkId, timestamp, username,
				alfrescoClient);

		if (logger.isDebugEnabled())
		{
			logger.debug("sendEvent "+event);
		}

		try
		{
			messageProducer.send(event);
		}
		catch (MessagingException e)
		{
//			throw new AlfrescoRuntimeException("Failed to send event", e);
			// TODO just log for now. How to deal with no running ActiveMQ?
			logger.error("Failed to send event " + event, e);
		}
		finally
		{
			TxnEvents events = (TxnEvents)AlfrescoTransactionSupport.getResource(EVENTS_KEY);
			if(events != null)
			{
				events.clear();
			}
		}
    }

	protected boolean includeEventType(String eventType)
	{
		return includeEventTypes.contains(eventType);
	}

	protected class NodeInfo
	{
	    private String txnId;
	    private String name;
		private NodeRef nodeRef;
		private List<Path> nodePaths;
		private Long modificationTimestamp;
		private QName type;
		private String siteId;
		private Status status;
		private Client client;
		private Boolean nodeExists;
		private boolean include;
		private Boolean isVisible;
		private Boolean typeMatches;
		private String eventType;
		private List<String> stringPaths;
		private List<List<String>> parentNodeIds;
		private Set<QName> aspects;

		public NodeInfo(String eventType, String txnId, String name, NodeRef nodeRef, Status status, List<Path> nodePaths,
				Long modificationTimestamp, QName type, Set<QName> aspects, String siteId, Client client, Boolean nodeExists, boolean include,
				Boolean isVisible, Boolean typeMatches)
		{
			super();
			this.eventType = eventType;
			this.txnId = txnId;
			this.name = name;
			this.nodeRef = nodeRef;
			this.status = status;
			this.nodePaths = nodePaths;
			this.modificationTimestamp = modificationTimestamp;
			this.type = type;
			this.aspects = aspects;
			this.siteId = siteId;
			this.client = client;
			this.nodeExists = nodeExists;
			this.include = include;
			this.isVisible = isVisible;
			this.typeMatches = typeMatches;
		}

		public Map<String, Serializable> getProperties()
		{
	    	Map<String, Serializable> properties = new HashMap<>();

	    	String workingCopyOwner = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_WORKING_COPY_OWNER);
	    	if(workingCopyOwner != null)
	    	{
	    		 properties.put(ContentModel.PROP_WORKING_COPY_OWNER.toPrefixString(namespaceService),
	    				 workingCopyOwner);
	    	}

	    	return properties;
		}

	    public boolean checkNodeInfo()
	    {
	    	boolean ret = true;

	    	if(!nodeExists)
	    	{
				logger.warn("Unable to send event of type " + eventType + ", node not found: " + this.toString());
				ret = false;
	    	}
	    	else if(!include)
	    	{
				logger.warn("Unable to send event of type " + eventType + ", node not included: " + this.toString());
				ret = false;
	    	}
	    	else if(!typeMatches)
	    	{
				logger.warn("Unable to send event of type " + eventType + ", node type filtered: " + this.toString());
				ret = false;
	    	}
	    	else if(!isVisible)
	    	{
				logger.warn("Unable to send event of type " + eventType + ", node is not visible: " + this.toString());
				ret = false;
	    	}

	    	return ret;
	    }

		public Set<QName> getAspects()
		{
			return aspects;
		}

		public Set<String> getAspectsAsStrings()
		{
			Set<String> strAspects = new HashSet<>();
			for(QName aspect : aspects)
			{
				strAspects.add(aspect.toPrefixString(namespaceService));
			}
			return strAspects;
		}

		public Boolean getTypeMatches()
		{
			return typeMatches;
		}

		public String getEventType()
		{
			return eventType;
		}

		public boolean isInclude()
		{
			return include;
		}

		public Boolean getIsVisible()
		{
			return isVisible;
		}

		public Boolean isNodeExists()
		{
			return nodeExists;
		}

		public String getTxnId()
		{
            return txnId;
        }

        public String getName()
		{
            return name;
        }

        public String getNodeId()
		{
        	String nodeId = nodeRef.getId();
        	return nodeId;
		}
		
		public Status getStatus()
		{
			return status;
		}

		List<String> getToAppend()
		{
			List<String> ret = null;

			if(name != null)
			{
				ret = Arrays.asList(name);
			}
			else
			{
				ret = Collections.emptyList();
			}

			return ret;
		}

		public List<String> getPaths()
		{
		    if(stringPaths == null && nodePaths != null)
		    {
		        stringPaths = AbstractEventsService.this.getPaths(nodePaths, getToAppend());
		    }

	        return stringPaths;
		}

		public List<List<String>> getParentNodeIds()
		{
		    if(parentNodeIds == null && nodePaths != null)
		    {
		        parentNodeIds = AbstractEventsService.this.getNodeIdsFromParent(nodePaths);
		    }

		    return parentNodeIds;
		}

		public Long getModificationTimestamp()
		{
			return modificationTimestamp;
		}
		
		public QName getType()
		{
			return type;
		}

		public String getSiteId()
		{
			return siteId;
		}

        public Client getClient()
        {
            return client;
        }

        void updateName(String name)
        {
        	this.name = name;
			stringPaths = null;
        }

		@Override
		public String toString()
		{
			return "NodeInfo [txnId=" + txnId + ", name=" + name + ", nodeRef="
					+ nodeRef+ ", nodePaths=" + nodePaths
					+ ", modificationTimestamp=" + modificationTimestamp
					+ ", type=" + type + ", siteId=" + siteId + ", status="
					+ status + ", client=" + client + ", nodeExists="
					+ nodeExists + ", include=" + include + ", isVisible="
					+ isVisible + ", typeMatches=" + typeMatches
					+ ", eventType=" + eventType + ", stringPaths="
					+ stringPaths + ", parentNodeIds=" + parentNodeIds + "]";
		}
	}

	// TODO cache parents?
    private boolean parentMatches(QName type)
    {
    	boolean matches = false;

    	QName t = type;
    	while(t != null)
    	{
	    	TypeDefinition typeDef = dictionaryService.getType(t);
	    	t = typeDef.getParentName();
	    	if(t != null)
	    	{
	    		if(matchingTypes.contains(t))
	    		{
	    			matches = true;
	    			matchingTypes.add(type);
	    			break;
	    		}
	    	}
    	}

    	return matches;
    }

    private boolean typeMatches(QName type)
    {
    	boolean matches = false;

    	if(type != null)
    	{
	    	if(matchingTypes == null || matchingTypes.size() == 0)
	    	{
	    		matches = true;
	    	}
	    	else
	    	{
		    	if(matchingTypes.contains(type))
		    	{
		    		matches = true;
		    	}
		    	else
		    	{
		    		matches = parentMatches(type);
		    	}
	    	}
    	}

    	return matches;
    }

    protected List<String> getPaths(List<Path> nodePaths, List<String> toAppend)
    {
        // TODO use fileFolderService.getNamePath instead?
        List<String> stringPaths = new ArrayList<String>(nodePaths.size());
        for(final Path path : nodePaths)
        {
            // run as system because the events system is a system service
            String displayPath = AuthenticationUtil.runAsSystem(new RunAsWork<String>()
            {
                @Override
                public String doWork() throws Exception
                {
                    String displayPath = path.toDisplayPath(nodeService, permissionService);
                    return displayPath;
                }
                
            });

            StringBuilder pathStr = new StringBuilder(displayPath);
            if(toAppend != null && toAppend.size() > 0)
            {
                for(String elem : toAppend)
                {
                    pathStr.append("/");
                    pathStr.append(elem);
                }
            }

            stringPaths.add(pathStr.toString());
        }

        return stringPaths;
    }

    /**
     * For each path, construct a list of ancestor node ids starting with the parent node id first.
     * 
     * @param nodePaths
     * @return
     */
    protected List<List<String>> getNodeIdsFromParent(List<Path> nodePaths)
    {
        // TODO use fileFolderService.getNamePath instead?
        List<List<String>> pathNodeIds = new ArrayList<List<String>>(nodePaths.size());
        for(Path path : nodePaths)
        {
            List<String> nodeIds = new ArrayList<String>(path.size());

            // don't include the leaf node id
            // add in reverse order (so the first element is the immediate parent)
            for(int i = path.size() - 2; i >= 0; i--)
            {
                Path.Element element = path.get(i);
                if(element instanceof ChildAssocElement)
                {
                    ChildAssocElement childAssocElem = (ChildAssocElement)element;
                    NodeRef childNodeRef = childAssocElem.getRef().getChildRef();
                    nodeIds.add(childNodeRef.getId());
                }
            }

            pathNodeIds.add(nodeIds);
        }

        return pathNodeIds;
    }

    /**
     * For each path, construct a list of ancestor node ids starting with the leaf path element node id first.
     * 
     * @param nodePaths
     * @return
     */
    protected List<List<String>> getNodeIds(List<Path> nodePaths)
    {
        // TODO use fileFolderService.getNamePath instead?
        List<List<String>> pathNodeIds = new ArrayList<List<String>>(nodePaths.size());
        for(Path path : nodePaths)
        {
            List<String> nodeIds = new ArrayList<String>(path.size());

            // don't include the leaf node id
            // add in reverse order (so the first element is the immediate parent)
            for(int i = path.size() - 1; i >= 0; i--)
            {
                Path.Element element = path.get(i);
                if(element instanceof ChildAssocElement)
                {
                    ChildAssocElement childAssocElem = (ChildAssocElement)element;
                    NodeRef childNodeRef = childAssocElem.getRef().getChildRef();
                    nodeIds.add(childNodeRef.getId());
                }
            }

            pathNodeIds.add(nodeIds);
        }

        return pathNodeIds;
    }

    class TxnEvents
    {
    	private long seqNumber;
    	private List<Event> events;

    	TxnEvents()
    	{
    		this.seqNumber = 0;
    		this.events = new LinkedList<>();
    	}

    	void addEvent(Event event)
    	{
    		events.add(event);
    	}
    	
    	void clear()
    	{
    		events.clear();
    		seqNumber = 0;
    	}

		long nextSeqNumber()
		{
			return seqNumber++;
		}

		List<Event> getEvents()
		{
			return events;
		}

		void sendEvents()
		{
    		if(events != null && events.size() > 0)
    		{
    			try
    			{
    				for(Event event : events)
    				{
    					messageProducer.send(event);
    				}
    			}
    			finally
    			{
    				events.clear();
    			}
    		}
		}
    }

    protected TxnEvents getTxnEvents()
    {
		TxnEvents events = (TxnEvents)AlfrescoTransactionSupport.getResource(EVENTS_KEY);
		if(events == null)
		{
			events = new TxnEvents();
			AlfrescoTransactionSupport.bindResource(EVENTS_KEY, events);
			AlfrescoTransactionSupport.bindListener(this);
		}
		return events;
    }

	protected void sendEvent(Event event)
	{
		String eventType = event.getType();
		if(!eventRegistry.isEventTypeRegistered(eventType))
		{
			eventRegistry.addEventType(eventType);
		}

		if (logger.isDebugEnabled())
		{
			logger.debug("sendEvent "+event);
		}

		TxnEvents events = getTxnEvents();
		events.addEvent(event);
	}

	protected NodeInfo getNodeInfo(final NodeRef nodeRef, final String eventType)
	{
		NodeInfo nodeInfo = AuthenticationUtil.runAsSystem(new RunAsWork<NodeInfo>()
		{
			public NodeInfo doWork() throws Exception
			{
				NodeInfo nodeInfo = null;

				String txnId = AlfrescoTransactionSupport.getTransactionId();

				if(!includeEventType(eventType))
				{
					nodeInfo = new NodeInfo(eventType, null, null, nodeRef, null, null, null, null, null, null, null, null,
							false, null, null);
				}
				else if(nodeRef == null || !nodeService.exists(nodeRef))
				{
					nodeInfo = new NodeInfo(eventType, txnId, null, nodeRef, null, null, null, null, null, null, null, false,
							true, false, null);
				}
				else
				{
				    FileFilterMode.Client filterclient = FileFilterMode.getClient();
					Visibility visibility = hiddenAspect.getVisibility(filterclient, nodeRef);
					QName type = nodeService.getType(nodeRef);

					if(!typeMatches(type))
					{
						nodeInfo = new NodeInfo(eventType, txnId, null, nodeRef, null, null, null, null, null, null,
								null, true, true, false, false);
					}
					else if(!visibility.equals(Visibility.Visible))
					{
						nodeInfo = new NodeInfo(eventType, txnId, null, nodeRef, null, null, null, null, null, null,
								null, true, true, true, true);
					}
					else
					{
						SiteInfo siteInfo = siteService.getSite(nodeRef);
				    	String siteId = (siteInfo != null ? siteInfo.getShortName() : null);

				    	Set<QName> aspects = nodeService.getAspects(nodeRef);
				    	
						final String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
						List<Path> nodePaths = Collections.singletonList(nodeService.getPath(nodeRef));
	
						Date modifiedTime = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
						Long modificationTimestamp = ( modifiedTime != null ? modifiedTime.getTime() : null);
	
						Status status = nodeService.getNodeStatus(nodeRef);
						Client client = ClientUtil.from(filterclient);
				    	nodeInfo = new NodeInfo(eventType, txnId, name, nodeRef, status, nodePaths, modificationTimestamp,
				    			type, aspects, siteId, client, true, true, true, true);
					}
				}

 				return nodeInfo;
			}
		});

		return nodeInfo;
	}
}
