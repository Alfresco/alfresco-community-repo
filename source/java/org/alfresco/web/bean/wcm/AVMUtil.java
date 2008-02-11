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
package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.config.JNDIConstants;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.sandbox.SandboxConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VirtServerUtils;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.ClientConfigElement;


/**
 * Helper methods and constants related to AVM directories, paths and store name manipulation.
 * 
 * @author Ariel Backenroth
 * @author Kevin Roast
 */
public final class AVMUtil
{
   /////////////////////////////////////////////////////////////////////////////
   
   public static enum PathRelation
   {
      SANDBOX_RELATIVE
      {
         @Override
         protected Pattern pattern() 
         { 
            return AVMUtil.SANDBOX_RELATIVE_PATH_PATTERN;
         }
      },
      WEBAPP_RELATIVE
      {
         @Override
         protected Pattern pattern()
         {
            return AVMUtil.WEBAPP_RELATIVE_PATH_PATTERN;
         }
      };

      protected abstract Pattern pattern();
   }

   /////////////////////////////////////////////////////////////////////////////

   /**
    * Private constructor
    */
   private AVMUtil()
   {
   }

   /**
    * Extracts the store name from the avmpath
    *
    * @param avmPath an absolute avm path
    * 
    * @return the store name
    */
   public static String getStoreName(final String avmPath)
   {
      final int i = avmPath.indexOf(':');
      if (i == -1)
      {
         throw new IllegalArgumentException("path " + avmPath + " does not contain a store");
      }
      return avmPath.substring(0, i);
   }

   /**
    * Indicates whether the store name describes a preview store.
    *
    * @param storeName the store name
    * 
    * @return <tt>true</tt> if the store is a preview store, <tt>false</tt> otherwise.
    */
   public static boolean isPreviewStore(final String storeName)
   {
      return storeName.endsWith(AVMUtil.STORE_SEPARATOR + AVMUtil.STORE_PREVIEW);
   }

   /**
    * Indicates whether the store name describes a workflow store.
    *
    * @param storeName the store name
    * 
    * @return <tt>true</tt> if the store is a workflow store, <tt>false</tt> otherwise.
    */
   public static boolean isWorkflowStore(String storeName)
   {
      if (AVMUtil.isPreviewStore(storeName))
      {
         storeName = AVMUtil.getCorrespondingMainStoreName(storeName);
      }
      
      return storeName.indexOf(STORE_SEPARATOR + STORE_WORKFLOW) != -1;
   }

   /**
    * Indicates whether the store name describes a user store.
    *
    * @param storeName the store name
    * 
    * @return <tt>true</tt> if the store is a user store, <tt>false</tt> otherwise.
    */
   public static boolean isUserStore(String storeName)
   {
      if (AVMUtil.isPreviewStore(storeName))
      {
         storeName = AVMUtil.getCorrespondingMainStoreName(storeName);
      }
      return storeName.indexOf(AVMUtil.STORE_SEPARATOR) != -1;
   }
   
   /**
    * Indicates whether the store name describes a main store.
    * 
    * @param storeName the store name
    * 
    * @return <tt>true</tt> if the store is a main store, <tt>false</tt> otherwise.
    */
   public static boolean isMainStore(String storeName)
   {
      return (storeName.indexOf(AVMUtil.STORE_SEPARATOR) == -1);
   }

   /**
    * Extracts the username from the store name.
    *
    * @param storeName the store name
    * 
    * @return the username associated or <tt>null</tt> if this is a staging store.
    */
   public static String getUserName(String storeName)
   {
      if (AVMUtil.isPreviewStore(storeName))
      {
         storeName = AVMUtil.getCorrespondingMainStoreName(storeName);
      }
      final int index = storeName.indexOf(AVMUtil.STORE_SEPARATOR);
      return (index == -1
              ? null
              : storeName.substring(index + AVMUtil.STORE_SEPARATOR.length()));
   }

   /**
    * Extracts the store id from the store name.
    *
    * @param storeName the store name.
    * 
    * @return the store id.
    */
   public static String getStoreId(final String storeName)
   {
      final int index = storeName.indexOf(AVMUtil.STORE_SEPARATOR);
      return (index == -1
              ? storeName
              : storeName.substring(0, index));
   }

   /**
    * Returns the corresponding main store name if this is a preview store name.
    *
    * @param storeName the preview store name.
    * 
    * @return the corresponding main store name.
    * 
    * @exception IllegalArgumentException if this is not a preview store name.
    */
   public static String getCorrespondingMainStoreName(final String storeName)
   {
      if (!AVMUtil.isPreviewStore(storeName))
      {
         throw new IllegalArgumentException("store " + storeName + " is not a preview store");
      }
      return storeName.substring(0, 
                                 (storeName.length() - 
                                  (AVMUtil.STORE_SEPARATOR + AVMUtil.STORE_PREVIEW).length()));
   }

   /**
    * Returns the corresponding preview store name if this is a main store name.
    *
    * @param storeName the main store name.
    * 
    * @return the corresponding preview store name.
    * 
    * @exception IllegalArgumentException if this is not a main store name.
    */
   public static String getCorrespondingPreviewStoreName(final String storeName)
   {
      if (AVMUtil.isPreviewStore(storeName))
      {
         throw new IllegalArgumentException("store " + storeName + " is already a preview store");
      }
      return storeName + AVMUtil.STORE_SEPARATOR + AVMUtil.STORE_PREVIEW;
   }

   /**
    * Returns the corresponding path in the main store name if this is a path in 
    * a preview store.
    *
    * @param avmPath an avm path within the main store.
    * 
    * @return the corresponding path within the preview store.
    * 
    * @exception IllegalArgumentException if this is not a path within the preview store.
    */
   public static String getCorrespondingPathInMainStore(final String avmPath)
   {
      String storeName = AVMUtil.getStoreName(avmPath);
      storeName = AVMUtil.getCorrespondingMainStoreName(storeName);
      return AVMUtil.getCorrespondingPath(avmPath, storeName);
   }

   /**
    * Returns the corresponding path in the preview store name if this is a path in 
    * a main store.
    *
    * @param avmPath an avm path within the main store.
    * 
    * @return the corresponding path within the preview store.
    * 
    * @exception IllegalArgumentException if this is not a path within the preview store.
    */
   public static String getCorrespondingPathInPreviewStore(final String avmPath)
   {
      String storeName = AVMUtil.getStoreName(avmPath);
      storeName = AVMUtil.getCorrespondingPreviewStoreName(storeName);
      return AVMUtil.getCorrespondingPath(avmPath, storeName);
   }

   /**
    * Returns the corresponding path in the store provided.
    * 
    * @param avmPath an avm path
    * @param otherStore the other store to return the corresponding path for
    * 
    * @return the corresponding path within the supplied store
    */
   public static String getCorrespondingPath(final String avmPath, final String otherStore)
   {
      return (otherStore + ':' + AVMUtil.getStoreRelativePath(avmPath));
   }
   
   /**
    * Returns the number of seconds between each call back to the server to
    * obtain the latest status of an in progress deployment.
    * <p>
    * This value is read from the &lt;wcm&gt; config section in
    * web-client-config-wcm.xml
    * </p>
    * 
    * @return Number of seconds between each call to the server (in seconds).
    *         The default is 2.
    */
   public static int getRemoteDeploymentPollingFrequency()
   {
      int pollFreq = 2;
      
      ConfigElement deploymentConfig = getDeploymentConfig();
      if (deploymentConfig != null)
      {
         ConfigElement elem = deploymentConfig.getChild("progress-polling-frequency");
         if (elem != null)
         {
            try
            {
               int value = Integer.parseInt(elem.getValue());
               if (value > 0)
               {
                  pollFreq = value;
               }
            }
            catch (NumberFormatException nfe)
            {
               // do nothing, just use the default
            }
         }
      }
      
      return pollFreq;
   }
   
   /**
    * Returns the number of seconds between each call back to the server to
    * obtain the latest status of a link validation check.
    * <p>
    * This value is read from the &lt;wcm&gt; config section in
    * web-client-config-wcm.xml
    * </p>
    * 
    * @return Number of seconds between each call to the server (in seconds).
    *         The default is 2.
    */
   public static int getLinkValidationPollingFrequency()
   {
      int pollFreq = 2;
      
      ConfigElement linkMngmtConfig = getLinksManagementConfig();
      if (linkMngmtConfig != null)
      {
         ConfigElement elem = linkMngmtConfig.getChild("progress-polling-frequency");
         if (elem != null)
         {
            try
            {
               int value = Integer.parseInt(elem.getValue());
               if (value > 0)
               {
                  pollFreq = value;
               }
            }
            catch (NumberFormatException nfe)
            {
               // do nothing, just use the default
            }
         }
      }
      
      return pollFreq;
   }
   
   /**
    * Returns the main staging store name for the specified store id.
    * 
    * @param storeId store id to build staging store name for
    * 
    * @return main staging store name for the specified store id
    */
   public static String buildStagingStoreName(final String storeId)
   {
      if (storeId == null || storeId.length() == 0)
      {
         throw new IllegalArgumentException("Store id is mandatory.");
      }
      return storeId;
   }
   
   /**
    * Returns the preview store name for the specified store id.
    * 
    * @param storeId store id to build preview store name for
    * 
    * @return preview store name for the specified store id
    */
   public static String buildStagingPreviewStoreName(final String storeId)
   {
      return (AVMUtil.buildStagingStoreName(storeId) + 
              AVMUtil.STORE_SEPARATOR + AVMUtil.STORE_PREVIEW);
   }
   
   /**
    * Returns the main store name for a specific username.
    * 
    * @param storeId store id to build user store name for
    * @param username of the user to build store name for
    * 
    * @return the main store for the specified user and store id
    */
   public static String buildUserMainStoreName(final String storeId, 
                                               final String username)
   {
      if (username == null || username.length() == 0)
      {
         throw new IllegalArgumentException("Username is mandatory.");
      }
      return (AVMUtil.buildStagingStoreName(storeId) + AVMUtil.STORE_SEPARATOR + 
              username);
   }
   
   /**
    * Returns the preview store name for a specific username.
    * 
    * @param storeId store id to build user preview store name for
    * @param username of the user to build preview store name for
    * 
    * @return the preview store for the specified user and store id
    */
   public static String buildUserPreviewStoreName(final String storeId, 
                                                  final String username)
   {
      return (AVMUtil.buildUserMainStoreName(storeId, username) +
              AVMUtil.STORE_SEPARATOR + AVMUtil.STORE_PREVIEW);
   }

   /**
    * Returns the store name for a specific workflow Id.
    * 
    * @param storeId store id to build workflow store name for
    * @param workflowId of the user to build workflow store name for
    * 
    * @return the store for the specified workflow and store ids
    */
   public static String buildWorkflowMainStoreName(final String storeId, 
                                                   final String workflowId)
   {
      if (workflowId == null || workflowId.length() == 0)
      {
         throw new IllegalArgumentException("workflowId is mandatory.");
      }
      return (AVMUtil.buildStagingStoreName(storeId) + AVMUtil.STORE_SEPARATOR +
              workflowId);
   }

   /**
    * Returns the preview store name for a specific workflow Id.
    * 
    * @param storeId store id to build preview workflow store name for
    * @param workflowId of the user to build preview workflow store name for
    * 
    * @return the store for the specified preview workflow and store ids
    */
   public static String buildWorkflowPreviewStoreName(final String storeId, 
                                                      final String workflowId)
   {
      return (AVMUtil.buildWorkflowMainStoreName(storeId, workflowId) +
              AVMUtil.STORE_SEPARATOR + AVMUtil.STORE_PREVIEW);
   }

   /**
    * Returns the root path for the specified store name
    * 
    * @param storeName store to build root path for
    * 
    * @return root path for the specified store name
    */
   public static String buildStoreRootPath(final String storeName)
   {
      if (storeName == null || storeName.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      return storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW;
   }

   /**
    * Returns the root path for the specified sandbox name
    * 
    * @param storeName store to build root sandbox path for
    * 
    * @return root sandbox path for the specified store name
    */
   public static String buildSandboxRootPath(final String storeName)
   {
      return AVMUtil.buildStoreRootPath(storeName) + '/' +  JNDIConstants.DIR_DEFAULT_APPBASE;
   }
   
   /**
    * Returns the root webapp path for the specified store and webapp name
    * 
    * @param storeName store to build root webapp path for
    * @param webapp webapp folder name
    * 
    * @return the root webapp path for the specified store and webapp name
    */
   public static String buildStoreWebappPath(final String storeName, String webapp)
   {
      if (webapp == null || webapp.length() == 0)
      {
         throw new IllegalArgumentException("Webapp name is mandatory.");
      }
      return AVMUtil.buildSandboxRootPath(storeName) + '/' + webapp;
   }
   
   public static String buildStoreUrl(String store)
   {
      if (store == null || store.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      if (store.indexOf(':') != -1)
      {
         store = store.substring(0, store.indexOf(':'));
      }
      ClientConfigElement config = Application.getClientConfig(FacesContext.getCurrentInstance());
      return MessageFormat.format(JNDIConstants.PREVIEW_SANDBOX_URL, 
                                  lookupStoreDNS(store), 
                                  config.getWCMDomain(), 
                                  config.getWCMPort());
   }
   
   public static String buildWebappUrl(final String avmPath)
   {
      if (avmPath == null || avmPath.length() == 0)
      {
         throw new IllegalArgumentException("AVM path is mandatory.");
      }
      return AVMUtil.buildWebappUrl(AVMUtil.getStoreName(avmPath),
                                         AVMUtil.getWebapp(avmPath));
   }

   public static String buildWebappUrl(final String store, final String webapp)
   {
      if (webapp == null || webapp.length() == 0)
      {
         throw new IllegalArgumentException("Webapp name is mandatory.");
      }
      return (webapp.equals(DIR_ROOT)
              ? buildStoreUrl(store)
              : buildStoreUrl(store) + '/' + webapp);
   }
   
   public static String buildAssetUrl(final String avmPath)
   {
      if (avmPath == null || avmPath.length() == 0)
      {
         throw new IllegalArgumentException("AVM path is mandatory.");
      }
      final String[] s = avmPath.split(":");
      if (s.length != 2)
      {
         throw new IllegalArgumentException("expected exactly one ':' in " + avmPath);
      }
      return AVMUtil.buildAssetUrl(s[0], s[1]);
   }
   
   public static String buildAssetUrl(String store, String assetPath)
   {
      if (store == null || store.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      if (assetPath == null || assetPath.length() == 0)
      {
         throw new IllegalArgumentException("Asset path is mandatory.");
      }
      ClientConfigElement config = Application.getClientConfig(FacesContext.getCurrentInstance());
      return buildAssetUrl(assetPath, config.getWCMDomain(), config.getWCMPort(), lookupStoreDNS(store));
   }
   
   public static String buildAssetUrl(String assetPath, String domain, String port, String dns)
   {
      if (domain == null || port == null || dns == null)
      {
         throw new IllegalArgumentException("Domain, port and dns name are mandatory.");
      }
      if (assetPath == null || assetPath.length() == 0)
      {
         throw new IllegalArgumentException("Asset path is mandatory.");
      }
      if (assetPath.startsWith('/' + JNDIConstants.DIR_DEFAULT_WWW + 
                               '/' + JNDIConstants.DIR_DEFAULT_APPBASE))
      {
         assetPath = assetPath.substring(('/' + JNDIConstants.DIR_DEFAULT_WWW + 
                                          '/' + JNDIConstants.DIR_DEFAULT_APPBASE).length());
      }
      if (assetPath.startsWith('/' + DIR_ROOT))
      {
         assetPath = assetPath.substring(('/' + DIR_ROOT).length());
      }
      if (assetPath.length() == 0 || assetPath.charAt(0) != '/')
      {
         assetPath = '/' + assetPath;
      }
      
      return MessageFormat.format(JNDIConstants.PREVIEW_ASSET_URL, dns, domain, port, assetPath);
   }
   
   public static String lookupStoreDNS(String store)
   {
      if (store == null || store.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      
      final ServiceRegistry serviceRegistry =
         Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      final AVMService avmService = serviceRegistry.getAVMService();
      final Map<QName, PropertyValue> props = 
         avmService.queryStorePropertyKey(store, QName.createQName(null, SandboxConstants.PROP_DNS + '%'));
      return (props.size() == 1
              ? props.keySet().iterator().next().getLocalName().substring(SandboxConstants.PROP_DNS.length())
              : null);
   }

   /**
    * Converts the provided path to an absolute path within the avm.
    *
    * @param parentAVMPath used as the parent path if the provided path
    * is relative, otherwise used to extract the parent path portion up until
    * the webapp directory.
    * @param path a path relative to the parentAVMPath path, or if it is
    * absolute, it is relative to the sandbox used in the parentAVMPath.
    *
    * @return an absolute path within the avm using the paths provided.
    */
   public static String buildPath(final String parentAVMPath,
                                  final String path,
                                  final PathRelation relation)
   {
      String parent = parentAVMPath;
      if (path == null || path.length() == 0 || ".".equals(path) || "./".equals(path))
      {
         return parent;
      }
      
      if (path.charAt(0) == '/')
      {
         final Matcher m = relation.pattern().matcher(parent);
         if (m.matches())
         {
            parent = m.group(1);
         }
      } 
      else if (parent.charAt(parent.length() - 1) != '/')
      {
         parent = parent + '/';
      }

      return parent + path;
   }

   /**
    * Returns a path relative to the store portion of the avm path.
    *
    * @param absoluteAVMPath an absolute path within the avm
    * @return the path without the store prefix.
    */
   public static String getStoreRelativePath(final String absoluteAVMPath)
   {
      final Matcher m = STORE_RELATIVE_PATH_PATTERN.matcher(absoluteAVMPath);
      return m.matches() && m.group(1).length() != 0 ? m.group(1) : null;
   }

   /**
    * Returns a path relative to the webapp portion of the avm path.
    *
    * @param absoluteAVMPath an absolute path within the avm
    * @return a relative path within the webapp.
    */
   public static String getWebappRelativePath(final String absoluteAVMPath)
   {
      final Matcher m = WEBAPP_RELATIVE_PATH_PATTERN.matcher(absoluteAVMPath);
      return m.matches() && m.group(3).length() != 0 ? m.group(3) : "/";
   }
   
   /**
    * Returns the webapp within the path
    *
    * @param absoluteAVMPath the path from which to extract the webapp name
    *
    * @return an the webapp name contained within the path or <tt>null</tt>.
    */
   public static String getWebapp(final String absoluteAVMPath)
   {
      final Matcher m = WEBAPP_RELATIVE_PATH_PATTERN.matcher(absoluteAVMPath);
      return m.matches() && m.group(2).length() != 0 ? m.group(2) : null;
   }

   /**
    * Returns the path portion up the webapp
    *
    * @param absoluteAVMPath the path from which to extract the webapp path
    *
    * @return an absolute avm path to the webapp contained within
    * the path or <tt>null</tt>.
    */
   public static String getWebappPath(final String absoluteAVMPath)
   {
      final Matcher m = WEBAPP_RELATIVE_PATH_PATTERN.matcher(absoluteAVMPath);
      return m.matches() && m.group(1).length() != 0 ? m.group(1) : null;
   }

   /**
    * Returns a path relative to the sandbox porition of the avm path.
    *
    * @param absoluteAVMPath an absolute path within the avm
    * @return a relative path within the sandbox.
    */
   public static String getSandboxRelativePath(final String absoluteAVMPath)
   {
      final Matcher m = SANDBOX_RELATIVE_PATH_PATTERN.matcher(absoluteAVMPath);
      return m.matches() && m.group(2).length() != 0 ? m.group(2) : "/";
   }

   /**
    * Returns the path portion up the sandbox
    *
    * @param absoluteAVMPath the path from which to extract the sandbox path
    *
    * @return an absolute avm path to the sandbox contained within
    * the path or <tt>null</tt>.
    */
   public static String getSandboxPath(final String absoluteAVMPath)
   {
      final Matcher m = SANDBOX_RELATIVE_PATH_PATTERN.matcher(absoluteAVMPath);
      return m.matches() && m.group(1).length() != 0 ? m.group(1) : null;
   }
   
   /**
    * Returns the NodeRef that represents the given avm path
    * 
    * @param absoluteAVMPath The path from which to determine the Web Project
    * @return The NodeRef representing the Web Project the path is from or null
    *         if it could not be determined
    */
   public static NodeRef getWebProjectNodeFromPath(final String absoluteAVMPath)
   {
      String storeName = AVMUtil.getStoreName(absoluteAVMPath);
      String storeId = AVMUtil.getStoreId(storeName);
      return getWebProjectNodeFromStore(storeId);
   }
   
   /**
    * Returns the NodeRef that represents the given avm store
    * 
    * @param storeName The store name from which to determine the Web Project
    * @return The NodeRef representing the Web Project the store is from or null
    *         if it could not be determined
    */
   public static NodeRef getWebProjectNodeFromStore(final String storeName)
   {
      // get services
      FacesContext fc = FacesContext.getCurrentInstance();
      SearchService searchService = Repository.getServiceRegistry(fc).getSearchService();
      NodeService nodeService = Repository.getServiceRegistry(fc).getNodeService();
      
      // construct the query
      String path = Application.getRootPath(fc) + "/" + Application.getWebsitesFolderName(fc) + "/*";
      String query = "PATH:\"/" + path + "\" AND @wca\\:avmstore:\"" + storeName + "\"";
      
      NodeRef webProjectNode = null;
      ResultSet results = null;
      try
      {
         // execute the query
         results = searchService.query(Repository.getStoreRef(), 
               SearchService.LANGUAGE_LUCENE, query);
         
         // WCM-810:
         // the 'avmstore' property was not defined as an identifier in the model (before 2.2)
         // which means it may get tokenised which in turn means that 'test' and 'test-site' 
         // would get returned by the query above even though it's an exact match query,
         // we therefore need to go through the results and check names if there is more 
         // than one result although this shouldn't happen anymore as the 
         // AVMStorePropertyTokenisationPatch will have reindexed the wca:avmstore property
         if (results.length() == 1)
         {
            webProjectNode = results.getNodeRef(0);
         }
         else if (results.length() > 1)
         {
            for (NodeRef node : results.getNodeRefs())
            {
               String nodeStoreName = (String)nodeService.getProperty(node, 
                        WCMAppModel.PROP_AVMSTORE);
               if (nodeStoreName.equals(storeName))
               {
                  webProjectNode = node;
                  break;
               }
            }
         }
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      return webProjectNode;
   }
   
   /**
    * Retrieves the NodeRef of the deploymentattempt node with the given id
    * 
    * @param attemptId The deployattemptid of the node to be found
    * @return The NodeRef of the deploymentattempt node or null if not found
    */
   public static NodeRef findDeploymentAttempt(String attemptId)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      SearchService searchService = Repository.getServiceRegistry(fc).getSearchService();
      
      // construct the query
      String query = "@wca\\:deployattemptid:\"" + attemptId + "\"";
      
      NodeRef attempt = null;
      ResultSet results = null;
      try
      {
         // execute the query
         results = searchService.query(Repository.getStoreRef(), 
               SearchService.LANGUAGE_LUCENE, query);
         
         if (results.length() == 1)
         {
            attempt = results.getNodeRef(0);
         }
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      return attempt;
   }

   /**
    * Creates all directories for a path if they do not already exist.
    */
   public static void makeAllDirectories(final String avmDirectoryPath)
   {
      final ServiceRegistry serviceRegistry =
         Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      final AVMService avmService = serviceRegistry.getAVMService();
      // LOGGER.debug("mkdir -p " + avmDirectoryPath);
      String s = avmDirectoryPath;
      final Stack<String[]> dirNames = new Stack<String[]>();
      while (s != null)
      {
         try
         {
            if (avmService.lookup(-1, s) != null)
            {
               // LOGGER.debug("path " + s + " exists");
               break;
            }
         }
         catch (AVMNotFoundException avmfe)
         {
         }
         final String[] sb = AVMNodeConverter.SplitBase(s);
         s = sb[0];
         // LOGGER.debug("pushing " + sb[1]);
         dirNames.push(sb);
      }

      while (!dirNames.isEmpty())
      {
         final String[] sb = dirNames.pop();
         // LOGGER.debug("creating " + sb[1] + " in " + sb[0]);
         avmService.createDirectory(sb[0], sb[1]);
      }
   }

   /**
    * Update notification on the virtualisation server webapp as required for the specified path
    * 
    * @param path    Path to match against
    * @param force   True to force update of server even if path does not match
    */
   public static void updateVServerWebapp(String path, boolean force)
   {
      if (force || VirtServerUtils.requiresUpdateNotification(path))
      {
         final int webappIndex = path.indexOf('/', 
                                              path.indexOf(JNDIConstants.DIR_DEFAULT_APPBASE) + 
                                              JNDIConstants.DIR_DEFAULT_APPBASE.length() + 1);

         if (webappIndex != -1)
         {
            path = path.substring(0, webappIndex);
         }
         final VirtServerRegistry vServerRegistry = AVMUtil.getVirtServerRegistry();
         vServerRegistry.updateWebapp(-1, path, true);
      }
   }
   

   /**
    * Removal notification on all the virtualisation server webapp as required by the specified path
    * 
    * @param path    Path to match against
    * @param force   True to force update of server even if path does not match
    */
   public static void removeAllVServerWebapps(String path, boolean force)
   {
      if (force || VirtServerUtils.requiresUpdateNotification(path))
      {
         final int webappIndex = path.indexOf('/', 
                                              path.indexOf(JNDIConstants.DIR_DEFAULT_APPBASE) + 
                                              JNDIConstants.DIR_DEFAULT_APPBASE.length() + 1);

         if (webappIndex != -1)
         {
            path = path.substring(0, webappIndex);
         }
         final VirtServerRegistry vServerRegistry = AVMUtil.getVirtServerRegistry();
         vServerRegistry.removeAllWebapps(-1, path, true);
      }
   }
   


   /**
    * Removal notification on the virtualisation server webapp as required for the specified path
    * 
    * @param path    Path to match against
    * @param force   True to force update of server even if path does not match
    */
   public static void removeVServerWebapp(String path, boolean force)
   {
      if (force || VirtServerUtils.requiresUpdateNotification(path))
      {
         final int webappIndex = path.indexOf('/', 
                                              path.indexOf(JNDIConstants.DIR_DEFAULT_APPBASE) + 
                                              JNDIConstants.DIR_DEFAULT_APPBASE.length() + 1);

         if (webappIndex != -1)
         {
            path = path.substring(0, webappIndex);
         }
         final VirtServerRegistry vServerRegistry = AVMUtil.getVirtServerRegistry();
         vServerRegistry.removeWebapp(-1, path, true);
      }
   }
   
   private static VirtServerRegistry getVirtServerRegistry()
   {
      return Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getVirtServerRegistry();
   }
   
   private static ConfigElement getDeploymentConfig()
   {
      if ((deploymentConfig == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         ConfigService cfgService = Application.getConfigService(FacesContext.getCurrentInstance());
         ConfigElement wcmCfg = cfgService.getGlobalConfig().getConfigElement("wcm");
         if (wcmCfg != null)
         {
            deploymentConfig = wcmCfg.getChild("deployment");
         }
      }
      
      return deploymentConfig;
   }
   
   private static ConfigElement getLinksManagementConfig()
   {
      if ((linksManagementConfig == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         ConfigService cfgService = Application.getConfigService(FacesContext.getCurrentInstance());
         ConfigElement wcmCfg = cfgService.getGlobalConfig().getConfigElement("wcm");
         if (wcmCfg != null)
         {
            linksManagementConfig = wcmCfg.getChild("links-management");
         }
      }
      
      return linksManagementConfig;
   }
   
   // Component Separator.
   /*package*/ static final String STORE_SEPARATOR = "--";
   
   // names of the stores representing the layers for an AVM website
   //XXXarielb this should be private
   /*package*/ final static String STORE_WORKFLOW = "workflow";
   private final static String STORE_PREVIEW = "preview";
   
   // servlet default webapp
   //    Note: this webapp is mapped to the URL path ""
   public final static String DIR_ROOT = "ROOT";
   
   public final static String SPACE_ICON_WEBSITE       = "space-icon-website";
   
   // web user role permissions
   public final static String ROLE_CONTENT_MANAGER    = "ContentManager";
   public final static String ROLE_CONTENT_PUBLISHER  = "ContentPublisher";
   
   // pattern for absolute AVM Path
   private final static Pattern STORE_RELATIVE_PATH_PATTERN = 
      Pattern.compile("[^:]+:(.+)");
   
   private final static Pattern WEBAPP_RELATIVE_PATH_PATTERN = 
      Pattern.compile("([^:]+:/" + JNDIConstants.DIR_DEFAULT_WWW +
                      "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/([^/]+))(.*)");
   
   private final static Pattern SANDBOX_RELATIVE_PATH_PATTERN = 
      Pattern.compile("([^:]+:/" + JNDIConstants.DIR_DEFAULT_WWW +
                      "/" + JNDIConstants.DIR_DEFAULT_APPBASE + ")(.*)");
   
   private static ConfigElement deploymentConfig = null;
   private static ConfigElement linksManagementConfig = null;
}
