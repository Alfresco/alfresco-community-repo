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
package org.alfresco.wcm.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.config.JNDIConstants;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.actions.AVMDeployWebsiteAction;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.FileNameValidator;
import org.alfresco.util.VirtServerUtils;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Helper methods and constants related to WCM directories, paths and store name manipulation.
 *
 * @author Ariel Backenroth, Kevin Roast, janv
 */
public class WCMUtil extends AVMUtil
{
    private static Log logger = LogFactory.getLog(WCMUtil.class);
    
   /**
    * Extracts the sandbox store id from the avm path
    *
    * @param avmPath an absolute avm path
    * 
    * @return the sandbox store id
    */
   public static String getSandboxStoreId(final String avmPath)
   {
      return getStoreName(avmPath);
   }
   
   /**
    * Extracts the web project store id from the (sandbox) store name
    * <p>
    * For example, if the (sandbox) store name is: teststore--admin then the web project store id is: teststore
    * <p>
    * Note: Although the staging sandbox store name is currently equivalent to the web project store id, it should
    * be derived using 'buildStagingStoreName'.
    * 
    * @param storeName the sandbox store id
    * 
    * @return the web project store id
    */
   public static String getWebProjectStoreId(final String storeName)
   {
      final int index = storeName.indexOf(WCMUtil.STORE_SEPARATOR);
      return (index == -1
              ? storeName
              : storeName.substring(0, index));
   }
   
   /**
    * Extracts the web project store id from the avm path
    *
    * For example, if the avm path is: teststore--admin:/www/ROOT then the web project id is: teststore
    *
    * @param avmPath an absolute avm path
    * 
    * @return the web project store id.
    */
   public static String getWebProjectStoreIdFromPath(final String avmPath)
   {
       return getWebProjectStoreId(getStoreName(avmPath));
   }

   /**
    * Indicates whether the store name describes a preview store.
    *
    * @param storeName the store name
    * 
    * @return <tt>true</tt> if the store is a preview store, <tt>false</tt> otherwise.
    */
   protected static boolean isPreviewStore(final String storeName)
   {
      return ((storeName != null) && (storeName.endsWith(WCMUtil.STORE_SEPARATOR + WCMUtil.STORE_PREVIEW)));
   }
   
   
   /**
    * http://wiki.alfresco.com/wiki/WCM_Deployment_Features#Debugging_.26_Testing
    *
    * Examples of locally deployed store names for web project "MyWebProj" are:
    *
    *    MyWebProjlive
    *    MyWebProj--adminlive
    *
    * Note: if web project "MyWebProjlive" is pre-created then should be possible to browse staging:
    *
    *    http://wiki.alfresco.com/wiki/WCM_Deployment_Features#Deployed_Runtime
    *
    * @param storeName
    * @return
    */
   protected static boolean isLocalhostDeployedStore(String wpStoreId, String storeName)
   {
      return ((storeName.startsWith(wpStoreId)) &&
              (storeName.endsWith(AVMDeployWebsiteAction.LIVE_SUFFIX)) &&
              (! wpStoreId.endsWith(AVMDeployWebsiteAction.LIVE_SUFFIX)));
   }

   /**
    * Indicates whether the store name describes a workflow store.
    *
    * @param storeName the store name
    * 
    * @return <tt>true</tt> if the store is a workflow store, <tt>false</tt> otherwise.
    */
   protected static boolean isWorkflowStore(String storeName)
   {
      if (WCMUtil.isPreviewStore(storeName))
      {
         storeName = WCMUtil.getCorrespondingMainStoreName(storeName);
      }
      
      return ((storeName != null) && (storeName.indexOf(STORE_SEPARATOR + STORE_WORKFLOW) != -1));
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
      if (WCMUtil.isPreviewStore(storeName))
      {
         storeName = WCMUtil.getCorrespondingMainStoreName(storeName);
      }
      return ((storeName != null) && (storeName.indexOf(WCMUtil.STORE_SEPARATOR) != -1));
   }
   
   /**
    * Indicates whether the store name describes a staging store.
    * 
    * @param storeName the store name
    * 
    * @return <tt>true</tt> if the store is a main store, <tt>false</tt> otherwise.
    */
   public static boolean isStagingStore(String storeName)
   {
      return ((storeName != null) && (storeName.indexOf(WCMUtil.STORE_SEPARATOR) == -1));
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
      if (WCMUtil.isPreviewStore(storeName))
      {
         storeName = WCMUtil.getCorrespondingMainStoreName(storeName);
      }
      final int index = storeName.indexOf(WCMUtil.STORE_SEPARATOR);
      return (index == -1 ? null : unescapeStoreNameComponent(storeName.substring(index
            + WCMUtil.STORE_SEPARATOR.length())));
   }
   
   /**
    * Extracts the workflow id
    * 
    * @param storeName
    * @return
    */
   public static String getWorkflowId(String storeName)
   {
      if (WCMUtil.isPreviewStore(storeName))
      {
         storeName = WCMUtil.getCorrespondingMainStoreName(storeName);
      }
      final int index = storeName.indexOf(STORE_SEPARATOR + STORE_WORKFLOW);
      return (index == -1
              ? null
              : storeName.substring(index + WCMUtil.STORE_SEPARATOR.length()));
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
   protected static String getCorrespondingMainStoreName(final String storeName)
   {
      if (!WCMUtil.isPreviewStore(storeName))
      {
         throw new IllegalArgumentException("store " + storeName + " is not a preview store");
      }
      return storeName.substring(0, 
                                 (storeName.length() - 
                                  (WCMUtil.STORE_SEPARATOR + WCMUtil.STORE_PREVIEW).length()));
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
   protected static String getCorrespondingPreviewStoreName(final String storeName)
   {
      if (WCMUtil.isPreviewStore(storeName))
      {
         throw new IllegalArgumentException("store " + storeName + " is already a preview store");
      }
      return storeName + WCMUtil.STORE_SEPARATOR + WCMUtil.STORE_PREVIEW;
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
   protected static String getCorrespondingPathInMainStore(final String avmPath)
   {
      String storeName = getStoreName(avmPath);
      storeName = WCMUtil.getCorrespondingMainStoreName(storeName);
      return WCMUtil.getCorrespondingPath(avmPath, storeName);
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
   protected static String getCorrespondingPathInPreviewStore(final String avmPath)
   {
      String storeName = getStoreName(avmPath);
      storeName = WCMUtil.getCorrespondingPreviewStoreName(storeName);
      return WCMUtil.getCorrespondingPath(avmPath, storeName);
   }

   /**
    * Returns the corresponding path in the store provided.
    * 
    * @param avmPath an avm path
    * @param otherStore the other store name to return the corresponding path for
    * 
    * @return the corresponding path within the supplied store
    */
   public static String getCorrespondingPath(final String avmPath, final String otherStoreName)
   {
      return (buildAVMPath(otherStoreName, WCMUtil.getStoreRelativePath(avmPath)));
   }
   
   /**
    * Utility function for escaping part of a compound store name (delimited by STORE_SEPARATOR sequences). Uses ISO
    * 9075 style encoding to escape otherwise problematic character sequences.
    * 
    * @param component
    *           the component
    * @return the escaped string
    */
   public static final String escapeStoreNameComponent(String component)
   {
      StringBuilder builder = null;
      int length = component.length();
      // If the component matches one of the common suffixes, encode it
      if (component.equals(STORE_PREVIEW) || component.equals(STORE_WORKFLOW))
      {
         builder = new StringBuilder(length + 5);
         appendEncoded(builder, component);
         return builder.toString();
      }

      // Look for problematic character sequences
      Matcher matcher = PATTERN_ILLEGAL_SEQUENCE.matcher(component);
      int lastAppendPosition = 0;
      while (matcher.find())
      {
         if (builder == null)
         {
            builder = new StringBuilder(length + 5);
         }
         if (matcher.start() != lastAppendPosition)
         {
            builder.append(component, lastAppendPosition, matcher.start());
         }
         lastAppendPosition = matcher.end();
         appendEncoded(builder, matcher.group());
      }
      if (builder == null)
      {
         return component;
      }
      if (lastAppendPosition < length)
      {
         builder.append(component, lastAppendPosition, length);
      }

      return builder.toString();
   }

   /**
    * Utility function for decoding from Strings produced by the above method.
    * 
    * @param component
    *           the encoded component
    * @return the decoded component
    */
   private static String unescapeStoreNameComponent(String component)
   {
      StringBuilder builder = null;
      int length = component.length();
      int lastAppendPosition = 0;
      int escapeIndex;
      while ((escapeIndex = component.indexOf("-x", lastAppendPosition)) != -1)
      {
         if (builder == null)
         {
            builder = new StringBuilder(length + 5);
         }
         if (escapeIndex != lastAppendPosition)
         {
            builder.append(component, lastAppendPosition, escapeIndex);
         }
         lastAppendPosition = component.indexOf('-', escapeIndex + 2);
         builder.appendCodePoint(Integer.parseInt(component.substring(escapeIndex + 2, lastAppendPosition), 16));
         lastAppendPosition++;
      }
      if (builder == null)
      {
         return component;
      }
      if (lastAppendPosition < length)
      {
         builder.append(component, lastAppendPosition, length);
      }

      return builder.toString();
   }

   private static final void appendEncoded(StringBuilder builder, String sequence)
   {
      builder.append("-x").append(Integer.toString(sequence.codePointAt(0), 16).toUpperCase()).append("-");
      int length = sequence.length();
      int next = sequence.offsetByCodePoints(0, 1);
      if (next < length)
      {
         builder.append(sequence, next, length);
      }
   }

   /**
    * Returns the main staging store name for the specified web project
    * 
    * @param wpStoreId   web project store id to build staging store name for
    * @return String     main staging store name for the specified web project store id
    */
   public static String buildStagingStoreName(final String wpStoreId)
   {
       ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
       return wpStoreId;
   }
   
   /**
    * Returns the preview store name for the specified store id.
    * 
    * @param storeId store id to build preview store name for
    * 
    * @return preview store name for the specified store id
    */
   protected static String buildStagingPreviewStoreName(final String storeId)
   {
      return (WCMUtil.buildStagingStoreName(storeId) + WCMUtil.STORE_SEPARATOR + 
              WCMUtil.STORE_PREVIEW);
   }
   
   /**
    * Returns the user's main store name for a specific username
    * 
    * @param storeId store id to build user store name for
    * @param username of the user to build store name for
    * 
    * @return the main store for the specified user and store id
    */
   public static String buildUserMainStoreName(final String storeId, 
                                               final String userName)
   {
       ParameterCheck.mandatoryString("userName", userName);
       String fixedUserName = escapeStoreNameComponent(userName);
       return (WCMUtil.buildStagingStoreName(storeId) + WCMUtil.STORE_SEPARATOR + 
               fixedUserName);
   }
   
   /**
    * Returns the preview store name for a specific username.
    * 
    * @param storeId store id to build user preview store name for
    * @param username of the user to build preview store name for
    * 
    * @return the preview store for the specified user and store id
    */
   protected static String buildUserPreviewStoreName(final String storeId, 
                                                  final String username)
   {
      return (WCMUtil.buildUserMainStoreName(storeId, username) + WCMUtil.STORE_SEPARATOR + 
              WCMUtil.STORE_PREVIEW);
   }

   /**
    * Returns the store name for a specific workflow Id.
    * 
    * @param storeId store id to build workflow store name for
    * @param workflowId of the user to build workflow store name for
    * 
    * @return the store for the specified workflow and store ids
    */
   protected static String buildWorkflowMainStoreName(final String storeId, 
                                                   final String workflowId)
   {
       ParameterCheck.mandatoryString("workflowId", workflowId);
       return (WCMUtil.buildStagingStoreName(storeId) + WCMUtil.STORE_SEPARATOR +
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
   protected static String buildWorkflowPreviewStoreName(final String storeId, 
                                                      final String workflowId)
   {
      return (WCMUtil.buildWorkflowMainStoreName(storeId, workflowId) +
              WCMUtil.STORE_SEPARATOR + WCMUtil.STORE_PREVIEW);
   }

   /**
    * Returns the root path for the specified store name
    * 
    * eg. mystore -> mystore:/www
    * 
    * @param storeName store to build root path for
    * 
    * @return root path for the specified store name
    */
   public static String buildStoreRootPath(final String storeName)
   {
       ParameterCheck.mandatoryString("storeName", storeName);
       return buildAVMPath(storeName, AVM_PATH_SEPARATOR_CHAR + JNDIConstants.DIR_DEFAULT_WWW);
   }

   /**
    * Returns the root path for the specified sandbox name
    * 
    * * eg. mystore -> mystore:/www/avm_webapps
    * 
    * @param storeName store to build root sandbox path for
    * 
    * @return root sandbox path for the specified store name
    */
   public static String buildSandboxRootPath(final String storeName)
   {
       ParameterCheck.mandatoryString("storeName", storeName);
       return buildAVMPath(storeName, JNDIConstants.DIR_DEFAULT_WWW_APPBASE);
   }
   
   /**
    * Returns the root webapp path for the specified store and webapp name
    * 
    * @param storeName store to build root webapp path for
    * @param webapp webapp folder name
    * 
    * @return the root webapp path for the specified store and webapp name
    */
   public static String buildStoreWebappPath(final String storeName, String webApp)
   {
       ParameterCheck.mandatoryString("webApp", webApp);
       return WCMUtil.buildSandboxRootPath(storeName) + AVM_PATH_SEPARATOR_CHAR + webApp;
   }
   
   public static String lookupStoreDNS(AVMService avmService, String store)
   {
       ParameterCheck.mandatoryString("store", store);
       
       final Map<QName, PropertyValue> props = 
         avmService.queryStorePropertyKey(store, QName.createQName(null, SandboxConstants.PROP_DNS + '%'));
       
       return (props.size() == 1
              ? props.keySet().iterator().next().getLocalName().substring(SandboxConstants.PROP_DNS.length())
              : null);
   }
   
   public static NodeRef getWebProjectNodeFromWebProjectStore(AVMService avmService, String wpStoreId)
   {
       NodeRef wpNodeRef = null;
       
       String stagingStoreId = wpStoreId; // note: equivalent to WCMUtil.buildStagingStoreName(wpStoreId)
       
       try
       {
           PropertyValue pValue = avmService.getStoreProperty(stagingStoreId, SandboxConstants.PROP_WEB_PROJECT_NODE_REF);
           
           if (pValue != null)
           {
               wpNodeRef = (NodeRef)pValue.getValue(DataTypeDefinition.NODE_REF);
           }
       }
       catch (AVMNotFoundException nfe)
       {
           logger.warn(wpStoreId + " is not a web project: " + nfe);
       }
       
       return wpNodeRef;
   }
   
   /** 
    * Returns web project store id for an AVM store name (or null for vanilla AVM store)
    */
   public static String getWebProject(AVMService avmService, String avmStoreName)
   {   
       String wpStoreId = WCMUtil.getWebProjectStoreId(avmStoreName);
       if (WCMUtil.getWebProjectNodeFromWebProjectStore(avmService, wpStoreId) != null)
       {
           return wpStoreId;
       }
       return null;
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
   /*
   protected static String buildPath(final String parentAVMPath,
                                  final String path,
                                  final PathRelation relation)
   {
      String parent = parentAVMPath;
      if (path == null || path.length() == 0 || ".".equals(path) || "./".equals(path))
      {
         return parent;
      }
      
      if (path.charAt(0) == AVM_PATH_SEPARATOR_CHAR)
      {
         final Matcher m = relation.pattern().matcher(parent);
         if (m.matches())
         {
            parent = m.group(1);
         }
      } 
      else if (parent.charAt(parent.length() - 1) != AVM_PATH_SEPARATOR_CHAR)
      {
         parent = parent + AVM_PATH_SEPARATOR_CHAR;
      }

      return parent + path;
   }
   */

   /**
    * Returns a path relative to the store portion of the avm path.
    *
    * @param absoluteAVMPath an absolute path within the avm
    * @return the path without the store prefix.
    */
   public static String getStoreRelativePath(final String absoluteAVMPath)
   {
       ParameterCheck.mandatoryString("absoluteAVMPath", absoluteAVMPath);
       return AVMUtil.splitPath(absoluteAVMPath)[1];
   }

   /**
    * Returns a path relative to the webapp portion of the avm path.
    *
    * @param absoluteAVMPath an absolute path within the avm
    * @return a relative path within the webapp.
    */
   protected static String getWebappRelativePath(final String absoluteAVMPath)
   {
      final Matcher m = WEBAPP_RELATIVE_PATH_PATTERN.matcher(absoluteAVMPath);
      return m.matches() && m.group(3).length() != 0 ? m.group(3) : AVM_PATH_SEPARATOR;
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
   protected static String getWebappPath(final String absoluteAVMPath)
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
   protected static String getSandboxRelativePath(final String absoluteAVMPath)
   {
      final Matcher m = SANDBOX_RELATIVE_PATH_PATTERN.matcher(absoluteAVMPath);
      return m.matches() && m.group(2).length() != 0 ? m.group(2) : AVM_PATH_SEPARATOR;
   }

   /**
    * Returns the path portion up the sandbox
    *
    * @param absoluteAVMPath the path from which to extract the sandbox path
    *
    * @return an absolute avm path to the sandbox contained within
    * the path or <tt>null</tt>.
    */
   protected static String getSandboxPath(final String absoluteAVMPath)
   {
      final Matcher m = SANDBOX_RELATIVE_PATH_PATTERN.matcher(absoluteAVMPath);
      return m.matches() && m.group(1).length() != 0 ? m.group(1) : null;
   }

   protected static Map<String, String> listWebUsers(NodeService nodeService, NodeRef wpNodeRef)
   {
       List<ChildAssociationRef> userInfoRefs = listWebUserRefs(nodeService, wpNodeRef, true);
       
       Map<String, String> webUsers = new HashMap<String, String>(23);
       
       for (ChildAssociationRef ref : userInfoRefs)
       {
           NodeRef userInfoRef = ref.getChildRef();
           String userName = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
           String userRole = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
           
           webUsers.put(userName, userRole);
        }
       
       return webUsers;
   }
   
   protected static List<ChildAssociationRef> listWebUserRefs(NodeService nodeService, NodeRef wpNodeRef, boolean preLoad)
   {
       return nodeService.getChildAssocs(wpNodeRef, WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL, preLoad);
   }
   
   /**
    * Creates all directories for a path if they do not already exist.
    */
   /*
   protected static void makeAllDirectories(AVMService avmService, final String avmDirectoryPath)
   {
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
   */

    /**
     * Update notification on the virtualisation server webapp as required for the specified path
     * 
     * @param path    Path to match against
     * @param force   True to force update of server even if path does not match
     */
    public static void updateVServerWebapp(VirtServerRegistry vServerRegistry, String path, boolean force)
    {
        if (force || VirtServerUtils.requiresUpdateNotification(path))
        {
            final int webappIndex = path.indexOf(AVM_PATH_SEPARATOR_CHAR, 
                                                 path.indexOf(JNDIConstants.DIR_DEFAULT_APPBASE) + 
                                                 JNDIConstants.DIR_DEFAULT_APPBASE.length() + 1);

            if (webappIndex != -1)
            {
                path = path.substring(0, webappIndex);
            }
            vServerRegistry.updateWebapp(-1, path, true);
        }
    }
   
    /**
     * Removal notification on all the virtualisation server webapp as required by the specified path
     * 
     * @param path    Path to match against
     * @param force   True to force update of server even if path does not match
     */
    protected static void removeAllVServerWebapps(VirtServerRegistry vServerRegistry, String path, boolean force)
    {
        if (force || VirtServerUtils.requiresUpdateNotification(path))
        {
            final int webappIndex = path.indexOf(AVM_PATH_SEPARATOR_CHAR, 
                                                 path.indexOf(JNDIConstants.DIR_DEFAULT_APPBASE) + 
                                                 JNDIConstants.DIR_DEFAULT_APPBASE.length() + 1);

            if (webappIndex != -1)
            {
                path = path.substring(0, webappIndex);
            }
            vServerRegistry.removeAllWebapps(-1, path, true);
        }
    }

   /**
    * Removal notification on the virtualisation server webapp as required for the specified path
    * 
    * @param path    Path to match against
    * @param force   True to force update of server even if path does not match
    */
   protected static void removeVServerWebapp(VirtServerRegistry vServerRegistry, String path, boolean force)
   {
      if (force || VirtServerUtils.requiresUpdateNotification(path))
      {
         final int webappIndex = path.indexOf(AVM_PATH_SEPARATOR_CHAR, 
                                              path.indexOf(JNDIConstants.DIR_DEFAULT_APPBASE) + 
                                              JNDIConstants.DIR_DEFAULT_APPBASE.length() + 1);
         
         if (webappIndex != -1)
         {
            path = path.substring(0, webappIndex);
         }
         vServerRegistry.removeWebapp(-1, path, true);
      }
   }
   
   // return common web app or null if paths span multiple web apps (or no web app)
   public static String getCommonWebApp(String sbStoreId, List<String> storeRelativePaths)
   {
       String derivedWebApp = null;
       boolean multiWebAppsFound = false;
       
       for (String storeRelativePath : storeRelativePaths)
       {
           // Example srcPath:
           //     mysite--alice:/www/avm_webapps/ROOT/foo.txt
           String srcPath = WCMUtil.buildAVMPath(sbStoreId, storeRelativePath);
           
           // TODO - don't really need the sbStoreId 
           // derive webapp for now
           String srcWebApp = WCMUtil.getWebapp(srcPath);
           if (srcWebApp != null)
           {
               if (derivedWebApp == null)
               {
                   derivedWebApp = srcWebApp;
               }
               else if (! derivedWebApp.equals(srcWebApp))
               {
                   multiWebAppsFound = true;
               }
           }
       }
       
       return (multiWebAppsFound == false ? derivedWebApp : null);
   }
   
   // Component Separator.
   protected static final String STORE_SEPARATOR = "--";

   /**
    * Matches character sequences that must be escaped in a compound store name. We disallow non-ASCII characters due to
    * Tomcat 5.5's insistence on URL decoding paths with the JVM default charset (not necessarily UTF-8)
    */
   protected static final Pattern PATTERN_ILLEGAL_SEQUENCE = Pattern.compile(FileNameValidator.FILENAME_ILLEGAL_REGEX
         + "|[^\\p{ASCII}]|-x|" + STORE_SEPARATOR);
   
   // names of the stores representing the layers for an AVM website
   //XXXarielb this should be private
   protected final static String STORE_WORKFLOW = "workflow";
   protected final static String STORE_PREVIEW = "preview";
   
   // servlet default webapp
   //    Note: this webapp is mapped to the URL path ""
   public final static String DIR_ROOT = "ROOT";
   
   protected final static String SPACE_ICON_WEBSITE       = "space-icon-website";
   
   // Locking constants
   public static final String LOCK_KEY_STORE_NAME = "avm-store-name";
   
   // web user role permissions
   public static final String ROLE_CONTENT_MANAGER     = PermissionService.WCM_CONTENT_MANAGER;
   public static final String ROLE_CONTENT_PUBLISHER   = PermissionService.WCM_CONTENT_PUBLISHER;
   public static final String ROLE_CONTENT_CONTRIBUTOR = PermissionService.WCM_CONTENT_CONTRIBUTOR;
   public static final String ROLE_CONTENT_REVIEWER    = PermissionService.WCM_CONTENT_REVIEWER;
   
   private final static Pattern WEBAPP_RELATIVE_PATH_PATTERN = 
      Pattern.compile("([^:]+:/" + JNDIConstants.DIR_DEFAULT_WWW +
                      AVM_PATH_SEPARATOR + JNDIConstants.DIR_DEFAULT_APPBASE + "/([^/]+))(.*)");
   
   private final static Pattern SANDBOX_RELATIVE_PATH_PATTERN = 
      Pattern.compile("([^:]+:/" + JNDIConstants.DIR_DEFAULT_WWW +
                      AVM_PATH_SEPARATOR + JNDIConstants.DIR_DEFAULT_APPBASE + ")(.*)");
   
   public static final String WORKFLOW_SUBMITDIRECT_NAME = "wcmwf:submitdirect";
}
