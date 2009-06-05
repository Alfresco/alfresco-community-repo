/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.config.JNDIConstants;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.preview.PreviewURIService;
import org.alfresco.web.bean.wcm.preview.VirtualisationServerPreviewURIService;
import org.alfresco.web.config.ClientConfigElement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Helper methods and constants related to AVM directories, paths and store name manipulation.
 * 
 * TODO refactor ...
 *
 * @author Ariel Backenroth
 * @author Kevin Roast
 */
public final class AVMUtil extends WCMUtil
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

   public static String getStoreName(final String avmPath)
   {
      return WCMUtil.getSandboxStoreId(avmPath);
   }
   
   public static boolean isPreviewStore(final String storeName)
   {
      return WCMUtil.isPreviewStore(storeName);
   }

   public static boolean isWorkflowStore(String storeName)
   {
      return WCMUtil.isWorkflowStore(storeName);
   }

   public static boolean isUserStore(String storeName)
   {
      return WCMUtil.isUserStore(storeName);
   }
   
   public static boolean isMainStore(String storeName)
   {
      return WCMUtil.isStagingStore(storeName);
   }

   public static String getUserName(String storeName)
   {
      return WCMUtil.getUserName(storeName);
   }

   public static String getStoreId(final String storeName)
   {
      return WCMUtil.getWebProjectStoreId(storeName);
   }

   public static String getCorrespondingMainStoreName(final String storeName)
   {
      return WCMUtil.getCorrespondingMainStoreName(storeName);
   }

   public static String getCorrespondingPreviewStoreName(final String storeName)
   {
      return WCMUtil.getCorrespondingPreviewStoreName(storeName);
   }

   public static String getCorrespondingPathInMainStore(final String avmPath)
   {
      return WCMUtil.getCorrespondingPathInMainStore(avmPath);
   }

   public static String getCorrespondingPathInPreviewStore(final String avmPath)
   {
      return WCMUtil.getCorrespondingPathInPreviewStore(avmPath);
   }

   public static String getCorrespondingPath(final String avmPath, final String otherStore)
   {
      return WCMUtil.getCorrespondingPath(avmPath, otherStore);
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
   
   public static String buildStagingStoreName(final String storeId)
   {
      return WCMUtil.buildStagingStoreName(storeId);
   }
   
   public static String buildStagingPreviewStoreName(final String storeId)
   {
      return WCMUtil.buildStagingPreviewStoreName(storeId);
   }
   
   public static String buildUserMainStoreName(final String storeId, 
                                               final String username)
   {
      return WCMUtil.buildUserMainStoreName(storeId, username);
   }
   
   public static String buildUserPreviewStoreName(final String storeId, 
                                                  final String username)
   {
      return WCMUtil.buildUserPreviewStoreName(storeId, username);
   }

   public static String buildWorkflowMainStoreName(final String storeId, 
                                                   final String workflowId)
   {
      return WCMUtil.buildWorkflowMainStoreName(storeId, workflowId);
   }

   public static String buildWorkflowPreviewStoreName(final String storeId, 
                                                      final String workflowId)
   {
      return WCMUtil.buildWorkflowPreviewStoreName(storeId, workflowId);
   }

   public static String buildStoreRootPath(final String storeName)
   {
      return WCMUtil.buildStoreRootPath(storeName);
   }

   public static String buildSandboxRootPath(final String storeName)
   {
      return WCMUtil.buildSandboxRootPath(storeName);
   }
   
   public static String buildStoreWebappPath(final String storeName, String webapp)
   {
      return WCMUtil.buildStoreWebappPath(storeName, webapp);
   }
   
   public static String buildStoreUrl(String store)
   {
      ServiceRegistry serviceRegistry = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      AVMService avmService = serviceRegistry.getAVMService();
      ClientConfigElement config = Application.getClientConfig(FacesContext.getCurrentInstance());
      return WCMUtil.buildStoreUrl(avmService, store, config.getWCMDomain(), config.getWCMPort());
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
   
   /**
    * NOTE: do not call directly from client - use getPreviewURI instead
    */
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
      return WCMUtil.buildAssetUrl(assetPath, domain, port, dns);
   }
   
   public static String getPreviewURI(String storeNameOrAvmPath)
   {
       if (storeNameOrAvmPath == null || storeNameOrAvmPath.length() == 0)
       {
          throw new IllegalArgumentException("AVM store name or absolute path is mandatory.");
       }
       final String[] s = storeNameOrAvmPath.split(AVM_STORE_SEPARATOR);
       if (s.length == 1)
       {
           return AVMUtil.getPreviewURI(s[0], null);
       }
       if (s.length != 2)
       {
          throw new IllegalArgumentException("expected exactly one ':' in " + storeNameOrAvmPath);
       }
       return AVMUtil.getPreviewURI(s[0], s[1]);
   }
   
   public static String getPreviewURI(final String storeId, final String assetPath)
   {
      if (previewURIGenerator == null)
      {
         WebApplicationContext wac = FacesContextUtils.getRequiredWebApplicationContext(
                     FacesContext.getCurrentInstance());
            
         if (wac.containsBean(SPRING_BEAN_NAME_PREVIEW_URI_SERVICE))
         {
            // if the bean is present retrieve it
            previewURIGenerator = (PreviewURIService)wac.getBean(SPRING_BEAN_NAME_PREVIEW_URI_SERVICE, 
                     PreviewURIService.class);
         }
         else
         {
            // Backwards compatibility - if the new Spring bean doesn't exist, default to the 
            // existing behaviour (create a URL to the virtualisation server).
            previewURIGenerator = new VirtualisationServerPreviewURIService();
         }
      }
       
      return previewURIGenerator.getPreviewURI(storeId, assetPath);
   }
   
   public static String lookupStoreDNS(String store)
   {
      final ServiceRegistry serviceRegistry =
         Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      final AVMService avmService = serviceRegistry.getAVMService();
      return WCMUtil.lookupStoreDNS(avmService, store);
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

   public static String getStoreRelativePath(final String absoluteAVMPath)
   {
      return WCMUtil.getStoreRelativePath(absoluteAVMPath);
   }
   
   public static String getWebappRelativePath(final String absoluteAVMPath)
   {
      return WCMUtil.getWebappRelativePath(absoluteAVMPath);
   }
   
   public static String getWebapp(final String absoluteAVMPath)
   {
      return WCMUtil.getWebapp(absoluteAVMPath);
   }

   public static String getWebappPath(final String absoluteAVMPath)
   {
      return WCMUtil.getWebappPath(absoluteAVMPath);
   }

   public static String getSandboxRelativePath(final String absoluteAVMPath)
   {
      return WCMUtil.getSandboxRelativePath(absoluteAVMPath);
   }

   public static String getSandboxPath(final String absoluteAVMPath)
   {
      return WCMUtil.getSandboxPath(absoluteAVMPath);
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
   
   public static void updateVServerWebapp(String path, boolean force)
   {
      WCMUtil.updateVServerWebapp(getVirtServerRegistry(), path, force);
   }
   
   public static void removeAllVServerWebapps(String path, boolean force)
   {
      WCMUtil.removeAllVServerWebapps(getVirtServerRegistry(), path, force);
   }
   
   public static void removeVServerWebapp(String path, boolean force)
   {
      WCMUtil.removeVServerWebapp(getVirtServerRegistry(), path, force);
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
   
   // pattern for absolute AVM Path
   //private final static Pattern STORE_RELATIVE_PATH_PATTERN = 
   //   Pattern.compile("[^:]+:(.+)");
   
   private final static Pattern WEBAPP_RELATIVE_PATH_PATTERN = 
      Pattern.compile("([^:]+:/" + JNDIConstants.DIR_DEFAULT_WWW +
                      "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/([^/]+))(.*)");
   
   private final static Pattern SANDBOX_RELATIVE_PATH_PATTERN = 
      Pattern.compile("([^:]+:/" + JNDIConstants.DIR_DEFAULT_WWW +
                      "/" + JNDIConstants.DIR_DEFAULT_APPBASE + ")(.*)");
   
   private static ConfigElement deploymentConfig = null;
   private static ConfigElement linksManagementConfig = null;
   
   private final static String      SPRING_BEAN_NAME_PREVIEW_URI_SERVICE = "PreviewURIService";
   private static PreviewURIService previewURIGenerator                  = null;
}
