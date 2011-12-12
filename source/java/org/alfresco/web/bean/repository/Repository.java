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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.web.bean.repository;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PathUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Helper class for accessing repository objects, convert values, escape values and service utilities.
 * 
 * @author gavinc
 * @author kevinr
 */
public final class Repository
{
   /** I18N error messages */
   public static final String ERROR_NODEREF = "error_noderef";
   public static final String ERROR_GENERIC = "error_generic";
   public static final String ERROR_NOHOME  = "error_homespace";
   public static final String ERROR_SEARCH  = "error_search";
   public static final String ERROR_QUERY   = "error_search_query";
   public static final String ERROR_EXISTS  = "error_exists";
   
   private static final String METADATA_EXTACTER_REGISTRY = "metadataExtracterRegistry";  

   private static Log logger = LogFactory.getLog(Repository.class);
   
   /** cache of client StoreRef */
   private static StoreRef storeRef = null;
   
   /** reference to the NamespaceService */
   private static NamespaceService namespaceService = null;
   
   /** reference to the ServiceRegistry */
   private static ServiceRegistry serviceRegistry = null;
   
   /**
    * Private constructor
    */
   private Repository()
   {
   }
   
   /**
    * Returns a store reference object
    * 
    * @return A StoreRef object
    */
   public static StoreRef getStoreRef()
   {
      return storeRef;
   }
   
   /**
    * Returns a store reference object.
    * This method is used to setup the cached value by the ContextListener initialisation methods
    * 
    * @return The StoreRef object
    */
   public static StoreRef getStoreRef(ServletContext context)
   {
      storeRef = Application.getRepositoryStoreRef(context);
      
      return storeRef;
   }
   
   /**
    * Returns a company root node reference object.
    * 
    * @return The NodeRef object
    */
   public static NodeRef getCompanyRoot(final FacesContext context)
   {
	   final String currentUserName = Application.getCurrentUser(context).getUserName();

       // note: run in context of System user using tenant-specific store
       // so that Company Root can be returned, even if the user does not have 
       // permission to access the Company Root (including, for example, the Guest user)
       return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
       {
            public NodeRef doWork() throws Exception
            {
         	   ServiceRegistry sr = getServiceRegistry(context);
         	   
               TenantService tenantService = (TenantService)FacesContextUtils.getRequiredWebApplicationContext(context).getBean("tenantService");

               // get store ref (from config)
               StoreRef storeRef = tenantService.getName(currentUserName, Repository.getStoreRef());
               
               // get root path (from config)
               String rootPath = Application.getRootPath(context);
               
         	   return getCompanyRoot(sr.getNodeService(), sr.getSearchService(), sr.getNamespaceService(), storeRef, rootPath);
            }
       }, AuthenticationUtil.getSystemUserName());
   }
   
   /**
    * Returns a company root node reference object.
    * 
    * @return The NodeRef object
    */
   public static NodeRef getCompanyRoot(NodeService nodeService, SearchService searchService, NamespaceService namespaceService, StoreRef storeRef, String rootPath)
   {
	   // check the repository exists, create if it doesn't
	   if (nodeService.exists(storeRef) == false)
	   {
	      throw new AlfrescoRuntimeException("Store not created prior to application startup: " + storeRef);
	   }
	
	   // get hold of the root node
	   NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
	
	   // see if the company home space is present
	   if (rootPath == null)
	   {
	      throw new AlfrescoRuntimeException("Root path has not been configured");
	   }
	
	   List<NodeRef> nodes = searchService.selectNodes(rootNodeRef, rootPath, null, namespaceService, false);
	   if (nodes.size() == 0)
	   {
	      throw new AlfrescoRuntimeException("Root path not created prior to application startup: " + rootPath);
	   }
	
	   // return company root
	   return nodes.get(0);
   }

   /**
    * Helper to get the display name for a Node.
    * The method will attempt to use the "name" attribute, if not found it will revert to using
    * the QName.getLocalName() retrieved from the primary parent relationship.
    * 
    * @param ref     NodeRef
    * 
    * @return display name string for the specified Node.
    */
   public static String getNameForNode(NodeService nodeService, NodeRef ref)
   {
      String name = null;
      
      // Check that node reference still exists
      if (nodeService.exists(ref) == true)
      {      
          // try to find a display "name" property for this node
          Object nameProp = nodeService.getProperty(ref, ContentModel.PROP_NAME);
          if (nameProp != null)
          {
             name = nameProp.toString();
          }
          else
          {
             // revert to using QName if not found
             QName qname = nodeService.getPrimaryParent(ref).getQName();
             if (qname != null)
             {
                name = qname.getLocalName();
             }
          }
      }
      
      return name;
   }

   /** 
    * Helper to get the display name path for a category node. 
    * 
    * @param nodeService 
    * @param ref the category node ref
    * @return display name string for the specified category node.
    */ 
   public static String getNameForCategoryNode(NodeService nodeService, NodeRef ref) 
   { 
      String name = null; 
       
      // Check that node reference still exists 
      if (nodeService.exists(ref) == true) 
      { 
          Path path = nodeService.getPath(ref); 
          name = Repository.getNamePath(nodeService, path, null, "/", null); 
      } 
       
      return name; 
   } 

   /**
    * Escape a QName value so it can be used in lucene search strings
    * 
    * @param qName      QName to escape
    * 
    * @return escaped value
    */
   public static String escapeQName(QName qName)
   {
       String string = qName.toString();
       StringBuilder buf = new StringBuilder(string.length() + 4);
       for (int i = 0; i < string.length(); i++)
       {
           char c = string.charAt(i);
           if ((c == '{') || (c == '}') || (c == ':') || (c == '-'))
           {
              buf.append('\\');
           }
   
           buf.append(c);
       }
       return buf.toString();
   }

   /**
    * Return whether a Node is currently locked
    * 
    * @param node             The Node wrapper to test against
    * @param lockService      The LockService to use
    * 
    * @return whether a Node is currently locked
    */
   public static Boolean isNodeLocked(Node node, LockService lockService)
   {
      Boolean locked = Boolean.FALSE;
      
      if (node.hasAspect(ContentModel.ASPECT_LOCKABLE))
      {
         LockStatus lockStatus = lockService.getLockStatus(node.getNodeRef());
         if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER)
         {
            locked = Boolean.TRUE;
         }
      }
      
      return locked;
   }
   
   /**
    * Return whether a Node is currently locked by the current user
    * 
    * @param node             The Node wrapper to test against
    * @param lockService      The LockService to use
    * 
    * @return whether a Node is currently locked by the current user
    */
   public static Boolean isNodeOwnerLocked(Node node, LockService lockService)
   {
      Boolean locked = Boolean.FALSE;
      
      if (node.hasAspect(ContentModel.ASPECT_LOCKABLE) &&
          lockService.getLockStatus(node.getNodeRef()) == LockStatus.LOCK_OWNER)
      {
         locked = Boolean.TRUE;
      }
      
      return locked;
   }
   
   /**
    * Return the human readable form of the specified node Path. Fast version of the method that
    * simply converts QName localname components to Strings.
    * 
    * @param path    Path to extract readable form from, excluding the final element
    * 
    * @return human readable form of the Path excluding the final element
    */
   public static String getDisplayPath(Path path)
   {
      return getDisplayPath(path, false);
   }
   
   /**
    * Return the human readable form of the specified node Path. Fast version of the method that
    * simply converts QName localname components to Strings.
    * 
    * @param path       Path to extract readable form from
    * @param showLeaf   Whether to process the final leaf element of the path
    * 
    * @return human readable form of the Path excluding the final element
    */
   public static String getDisplayPath(Path path, boolean showLeaf)
   {
      return PathUtil.getDisplayPath(path, showLeaf);
   }
   
   /**
    * Resolve a Path by converting each element into its display NAME attribute
    * 
    * @param path       Path to convert
    * @param separator  Separator to user between path elements
    * @param prefix     To prepend to the path
    * 
    * @return Path converted using NAME attribute on each element
    */
   public static String getNamePath(NodeService nodeService, Path path, NodeRef rootNode, String separator, String prefix)
   {
      StringBuilder buf = new StringBuilder(128);
      
      // ignore root node check if not passed in
      boolean foundRoot = (rootNode == null);
      
      if (prefix != null)
      {
         buf.append(prefix);
      }
      
      // skip first element as it represents repo root '/'
      for (int i=1; i<path.size(); i++)
      {
         Path.Element element = path.get(i);
         String elementString = null;
         if (element instanceof Path.ChildAssocElement)
         {
            ChildAssociationRef elementRef = ((Path.ChildAssocElement)element).getRef();
            if (elementRef.getParentRef() != null)
            {
               // only append if we've found the root already
               if (foundRoot == true)
               {
                  Object nameProp = nodeService.getProperty(elementRef.getChildRef(), ContentModel.PROP_NAME);
                  if (nameProp != null)
                  {
                     elementString = nameProp.toString();
                  }
                  else
                  {
                     elementString = element.getElementString();
                  }
               }
               
               // either we've found root already or may have now
               // check after as we want to skip the root as it represents the CIFS share name
               foundRoot = (foundRoot || elementRef.getChildRef().equals(rootNode));
            }
         }
         else
         {
            elementString = element.getElementString();
         }
         
         if (elementString != null)
         {
            buf.append(separator);
            buf.append(elementString);
         }
      }
      
      return buf.toString();
   }
   
   /**
    * Sets up the breadcrumb location representation for the given node in
    * the given list.
    * 
    * @param context FacesContext
    * @param navBean NavigationBean instance
    * @param location The location list to setup
    * @param node The Node being navigated to
    */
   public static void setupBreadcrumbLocation(FacesContext context, 
            NavigationBean navBean, List<IBreadcrumbHandler> location, NodeRef node)
   {
      // make the sure the given list is empty
      location.clear();
      
      // get required services
      NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
      DictionaryService dictionaryService = Repository.getServiceRegistry(context).getDictionaryService();
      PermissionService permsService = Repository.getServiceRegistry(context).getPermissionService();
      
      // add the given node to start
      String nodeName = Repository.getNameForNode(nodeService, node);
      location.add(navBean.new NavigationBreadcrumbHandler(node, nodeName));
      
      // get the given node's parent node
      NodeRef parent = nodeService.getPrimaryParent(node).getParentRef();
      while (parent != null)
      {
         // check the user can read the parent node
         if (permsService.hasPermission(parent, PermissionService.READ) == AccessStatus.ALLOWED)
         {
            // get the grand parent so we can check for the root node
            NodeRef grandParent = nodeService.getPrimaryParent(parent).getParentRef();
            
            if (grandParent != null)
            {
               // check that the node is actually a folder type, content can have children!
               QName parentType = nodeService.getType(parent);
               if (dictionaryService.isSubClass(parentType, ContentModel.TYPE_FOLDER))
               {
                  // if it's a folder add the location to the breadcrumb
                  String parentName = Repository.getNameForNode(nodeService, parent);
                  location.add(0, navBean.new NavigationBreadcrumbHandler(parent, parentName));
               }
            }
            
            parent = grandParent;
         }
         else
         {
            // the user does not have Read permission above this point so stop!
            break;
         }
      }
   }

   /**
    * Resolve a Path by converting each element into its display NAME attribute.
    * Note: This method resolves path regardless access permissions.
    * Fixes the UI part of the ETWOONE-214 and ETWOONE-238 issues
    * 
    * @param path       Path to convert
    * @param separator  Separator to user between path elements
    * @param prefix     To prepend to the path
    * 
    * @return Path converted using NAME attribute on each element
    */
   public static String getNamePathEx(FacesContext context, final Path path, final NodeRef rootNode, final String separator, final String prefix)
   {
       String result = null;
       
       RetryingTransactionHelper transactionHelper = getRetryingTransactionHelper(context);
       transactionHelper.setMaxRetries(1);
       final NodeService runtimeNodeService = (NodeService)FacesContextUtils.getRequiredWebApplicationContext(context).getBean("nodeService");
       result = transactionHelper.doInTransaction(
            new RetryingTransactionCallback<String>()
            {
                 public String execute() throws Throwable
                 {
                     return getNamePath(runtimeNodeService, path, rootNode, separator, prefix);
                 }
            });
       
       return result;
   }

   /**
    * Return the mimetype for the specified file name.
    * <p>
    * The file extension will be extracted from the filename and used to lookup the mimetype.
    * 
    * @param context       FacesContext
    * @param filename      Non-null filename to process
    * 
    * @return mimetype for the specified filename - falls back to 'application/octet-stream' if not found.
    */
   public static String getMimeTypeForFileName(FacesContext context, String filename)
   {
      return getMimeTypeForFile(context, filename, null);
   }

   /**
    * Return the mimetype for the specified file, based on both the
    *  file name and the file's contents.
    * <p>
    * The file extension will be extracted from the filename and used
    *  along with the file contents to identify the mimetype.
    * 
    * @param context       FacesContext
    * @param filename      Non-null filename to process
    * @param file          The File object (used to read the contents)
    * 
    * @return mimetype for the specified filename - falls back to 'application/octet-stream' if not found.
    */
   public static String getMimeTypeForFile(FacesContext context, String filename, File file)
   {
      String mimetype = MimetypeMap.MIMETYPE_BINARY;
      MimetypeService mimetypeService = (MimetypeService)getServiceRegistry(context).getMimetypeService();

      // Use the file contents if available
      if (file != null)
      {
         FileContentReader reader;
         try
         {
            reader = new FileContentReader(file);
            mimetype = mimetypeService.guessMimetype(filename, reader);
            return mimetype;
         }
         catch (Throwable t)
         {
            // Not terminal
            logger.warn("Error identifying mimetype from file contents ", t);
         }
      }
      
      // If the contents aren't available, go with the filename,
      //  falling back to the Binary Mimetype if needed
      mimetype = mimetypeService.guessMimetype(filename);
      return mimetype;
   }

   /**
    * Return a UserTransaction instance
    * 
    * @param context    FacesContext
    * 
    * @return UserTransaction
    * 
    * @deprecated
    * @see        #getRetryingTransactionHelper(FacesContext)
    */
   public static UserTransaction getUserTransaction(FacesContext context)
   {
      TransactionService transactionService = getServiceRegistry(context).getTransactionService(); 
      return transactionService.getUserTransaction();
   }
   
   /**
    * Returns the transaction helper that executes a unit of work.
    * 
    * @param context    FacesContext
    * @return           Returns the transaction helper
    */
   public static RetryingTransactionHelper getRetryingTransactionHelper(FacesContext context)
   {
      TransactionService transactionService = getServiceRegistry(context).getTransactionService();
      return transactionService.getRetryingTransactionHelper();
   }
   
   /**
    * Return a UserTransaction instance
    * 
    * @param context    FacesContext
    * @param readonly   Transaction readonly state
    * 
    * @return UserTransaction
    */
   public static UserTransaction getUserTransaction(FacesContext context, boolean readonly)
   {
      TransactionService transactionService = getServiceRegistry(context).getTransactionService(); 
      return transactionService.getUserTransaction(readonly);
   }

   /**
    * Return the Repository Service Registry
    * 
    * @param context Faces Context
    * @return the Service Registry
    */
   public static ServiceRegistry getServiceRegistry(FacesContext context)
   {
      if (serviceRegistry == null)
      {
         serviceRegistry = (ServiceRegistry)FacesContextUtils.getRequiredWebApplicationContext(
               context).getBean(ServiceRegistry.SERVICE_REGISTRY);
      }
      return serviceRegistry;
   }
   
   /**
    * Return the Repository Service Registry
    * 
    * @param context Servlet Context
    * @return the Service Registry
    */
   public static ServiceRegistry getServiceRegistry(ServletContext context)
   {
      if (serviceRegistry == null)
      {
         serviceRegistry = (ServiceRegistry)WebApplicationContextUtils.getRequiredWebApplicationContext(
               context).getBean(ServiceRegistry.SERVICE_REGISTRY);
      }
      return serviceRegistry;
   }
   
   /**
    * Return the Configurable Service
    * 
    * @return the configurable service
    */
   public static ConfigurableService getConfigurableService(FacesContext context)
   {
      return (ConfigurableService)FacesContextUtils.getRequiredWebApplicationContext(context).getBean("ConfigurableService");
   }
   
   /**
    * Return the Metadata Extracter Registry
    * 
    * @param context Faces Context
    * @return the MetadataExtracterRegistry
    */
   public static MetadataExtracterRegistry getMetadataExtracterRegistry(FacesContext context)
   {
      return (MetadataExtracterRegistry)FacesContextUtils.getRequiredWebApplicationContext(
            context).getBean(METADATA_EXTACTER_REGISTRY);
   }
   
   /**
    * Extracts the metadata of a "raw" piece of content into a map. 
    * 
    * @param context Faces Context
    * @param reader Content reader for the source content to extract from 
    * @param destination Map of metadata to set metadata values into
    * @return True if an extracter was found
    */
   public static boolean extractMetadata(FacesContext context, ContentReader reader, Map<QName, Serializable> destination)
   {
      // check that source mimetype is available
      String mimetype = reader.getMimetype();
      if (mimetype == null)
      {
         throw new AlfrescoRuntimeException("The content reader mimetype must be set: " + reader);
      }

      // look for a transformer
      MetadataExtracter extracter = getMetadataExtracterRegistry(context).getExtracter(mimetype);
      if (extracter == null)
      {
         // No metadata extracter is not a failure, but we flag it 
         return false;
      }
      
      try
      {
          // we have a transformer, so do it
          extracter.extract(reader, destination);
          return true;
      }
      catch (Throwable e)
      {
          // it failed
          logger.warn("Metadata extraction failed: \n" +
                  "   reader: " + reader + "\n" +
                  "   extracter: " + extracter);
          return false;
      }
   }
   
   /**
    * Extract the characterset from the stream
    * 
    * @param context       the Faces Context
    * @param is            the stream of characters or data
    * @param mimetype      the stream's mimetype, or <tt>null</tt> if unknown
    * @return              Returns the guessed characterset and never <tt>null</tt>
    */
   public static String guessEncoding(FacesContext context, InputStream is, String mimetype)
   {
      ContentCharsetFinder charsetFinder = getServiceRegistry(context).getMimetypeService().getContentCharsetFinder();
      Charset charset = charsetFinder.getCharset(is, mimetype);
      return charset.name();
   }

   /**
    * Query a list of Person type nodes from the repo
    * It is currently assumed that all Person nodes exist below the Repository root node
    * 
    * @param context Faces Context
    * @param nodeService The node service
    * @param searchService The search service, which is ignored
    * @return List of Person node objects
    * @deprecated Use {@link #getUsers(FacesContext, NodeService, PersonService)} instead
    */
   public static List<Node> getUsers(FacesContext context, NodeService nodeService, SearchService searchService)
   {
       PersonService personService = (PersonService)FacesContextUtils.getRequiredWebApplicationContext(context).getBean("personService");
       return getUsers(context, nodeService, personService);
   }
   
   /**
    * Query a list of Person type nodes from the repo
    * It is currently assumed that all Person nodes exist below the Repository root node
    * 
    * @param context Faces Context
    * @param nodeService The node service
    * @param searchService used to perform the search
    * @return List of Person node objects
    */
   public static List<Node> getUsers(FacesContext context, NodeService nodeService, PersonService personService)
   {
      List<Node> personNodes = null;
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         NodeRef peopleRef = personService.getPeopleContainer();
         
         // TODO: better to perform an XPath search or a get for a specific child type here?
         List<ChildAssociationRef> childRefs = nodeService.getChildAssocs(peopleRef);
         personNodes = new ArrayList<Node>(childRefs.size());
         for (ChildAssociationRef ref: childRefs)
         {
            // create our Node representation from the NodeRef
            NodeRef nodeRef = ref.getChildRef();
            
            if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_PERSON))
            {
               // create our Node representation
               MapNode node = new MapNode(nodeRef);
               
               // set data binding properties
               // this will also force initialisation of the props now during the UserTransaction
               // it is much better for performance to do this now rather than during page bind
               Map<String, Object> props = node.getProperties(); 
               String firstName = (String)props.get("firstName");
               String lastName = (String)props.get("lastName");
               props.put("fullName", (firstName != null ? firstName : "") + ' ' + (lastName != null ? lastName : ""));
               NodeRef homeFolderNodeRef = (NodeRef)props.get("homeFolder");
               if (homeFolderNodeRef != null)
               {
                  props.put("homeSpace", homeFolderNodeRef);
               }
               
               personNodes.add(node);
            }
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_NODEREF), new Object[] {"root"}) );
         personNodes = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), err.getMessage()), err );
         personNodes = Collections.<Node>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return personNodes;
   }
   
   /**
    * @return true if we are currently the special Guest user
    */
   public static boolean getIsGuest(FacesContext context)
   {
	   TenantService tenantService = (TenantService)FacesContextUtils.getRequiredWebApplicationContext(context).getBean("tenantService");
	   String userName = Application.getCurrentUser(context).getUserName();
	   return tenantService.getBaseNameUser(userName).equalsIgnoreCase(AuthenticationUtil.getGuestUserName());
   }
   
   /**
    * Convert a property of unknown type to a String value. A native String value will be
    * returned directly, else toString() will be executed, null is returned as null. 
    * 
    * @param value      Property value
    * 
    * @return value to String or null
    */
   public static String safePropertyToString(Serializable value)
   {
      if (value == null)
      {
         return null;
      }
      else if (value instanceof String)
      {
         return (String)value;
      }
      else
      {
         return value.toString();
      }
   }
   
   /**
    * Creates a QName representation for the given String.
    * If the String has no namespace the Alfresco namespace is added.
    * If the String has a prefix an attempt to resolve the prefix to the
    * full URI will be made. 
    * 
    * @param str The string to convert
    * @return A QName representation of the given string 
    */
   public static QName resolveToQName(String str)
   {
      return QName.resolveToQName(getNamespaceService(), str);
   }
   
   /**
    * Creates a string representation of a QName for the given string.
    * If the given string already has a namespace, either a URL or a prefix,
    * nothing the given string is returned. If it does not have a namespace
    * the Alfresco namespace is added.
    * 
    * @param str The string to convert
    * @return A QName String representation of the given string 
    */
   public static String resolveToQNameString(String str)
   {
      return QName.resolveToQNameString(getNamespaceService(), str);
   }
   
   /**
    * Returns an instance of the namespace service
    * 
    * @return The NamespaceService
    */
   private static NamespaceService getNamespaceService()
   {
      if (namespaceService == null)
      {
         ServiceRegistry svcReg = getServiceRegistry(FacesContext.getCurrentInstance());
         namespaceService = svcReg.getNamespaceService();
      }
      
      return namespaceService;
   }
}
