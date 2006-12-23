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
package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;

import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.ClientConfigElement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * @author Kevin Roast
 */
public final class AVMConstants
{
   /////////////////////////////////////////////////////////////////////////////
   
   public static enum PathRelation
   {
      SANDBOX_RELATIVE
      {
         @Override
         protected Pattern pattern() 
         { 
            return AVMConstants.SANDBOX_RELATIVE_PATH_PATTERN;
         }
      },
      WEBAPP_RELATIVE
      {
         @Override
         protected Pattern pattern()
         {
            return AVMConstants.WEBAPP_RELATIVE_PATH_PATTERN;
         }
      };

      protected abstract Pattern pattern();
   }

   /////////////////////////////////////////////////////////////////////////////

   /**
    * Private constructor
    */
   private AVMConstants()
   {
   }

   /**
    * Extracts the store name from the avmpath
    *
    * @param avmPath an absolute avm pth
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
    * @return <tt>true</tt> if the store is a preview store, <tt>false</tt> otherwise.
    */
   public static boolean isPreviewStore(final String storeName)
   {
      return storeName.endsWith(AVMConstants.STORE_SEPARATOR + AVMConstants.STORE_PREVIEW);
   }

   public static boolean isWorkflowStore(String storeName)
   {
      if (AVMConstants.isPreviewStore(storeName))
      {
         storeName = AVMConstants.getCorrespondingMainStoreName(storeName);
      }
      
      return storeName.indexOf(STORE_SEPARATOR + STORE_WORKFLOW) != -1;
   }

   /**
    * Indicates whether the store name describes a user store.
    *
    * @param storeName the store name
    * @return <tt>true</tt> if the store is a user store, <tt>false</tt> otherwise.
    */
   public static boolean isUserStore(String storeName)
   {
      if (AVMConstants.isPreviewStore(storeName))
      {
         storeName = AVMConstants.getCorrespondingMainStoreName(storeName);
      }
      return storeName.indexOf(AVMConstants.STORE_SEPARATOR) != -1;
   }

   /**
    * Extracts the username from the store name.
    *
    * @param storeName the store name
    * @return the username associated or <tt>null</tt> if this is a staging store.
    */
   public static String getUserName(String storeName)
   {
      if (AVMConstants.isPreviewStore(storeName))
      {
         storeName = AVMConstants.getCorrespondingMainStoreName(storeName);
      }
      final int index = storeName.indexOf(AVMConstants.STORE_SEPARATOR);
      return (index == -1
              ? null
              : storeName.substring(index + AVMConstants.STORE_SEPARATOR.length()));
   }

   /**
    * Extracts the store id from the store name.
    *
    * @param storeName the store name.
    * @return the store id.
    */
   public static String getStoreId(final String storeName)
   {
      final int index = storeName.indexOf(AVMConstants.STORE_SEPARATOR);
      return (index == -1
              ? storeName
              : storeName.substring(0, index));
   }

   /**
    * Returns the corresponding main store name if this is a preview store name.
    *
    * @param storeName the preview store name.
    * @return the corresponding main store name.
    * @exception IllegalArgumentException if this is not a preview store name.
    */
   public static String getCorrespondingMainStoreName(final String storeName)
   {
      if (!AVMConstants.isPreviewStore(storeName))
      {
         throw new IllegalArgumentException("store " + storeName + " is not a preview store");
      }
      return storeName.substring(0, 
                                 (storeName.length() - 
                                  (AVMConstants.STORE_SEPARATOR + AVMConstants.STORE_PREVIEW).length()));
   }

   /**
    * Returns the corresponding preview store name if this is a main store name.
    *
    * @param storeName the main store name.
    * @return the corresponding preview store name.
    * @exception IllegalArgumentException if this is not a main store name.
    */
   public static String getCorrespondingPreviewStoreName(final String storeName)
   {
      if (AVMConstants.isPreviewStore(storeName))
      {
         throw new IllegalArgumentException("store " + storeName + " is already a preview store");
      }
      return storeName + AVMConstants.STORE_SEPARATOR + AVMConstants.STORE_PREVIEW;
   }

   /**
    * Returns the corresponding path in the preview store name if this is a path in 
    * a main store.
    *
    * @param avmPath an avm path within the main store.
    * @return the corresponding path within the preview store.
    * @exception IllegalArgumentException if this is not a path within the preview store.
    */
   public static String getCorrespondingPathInMainStore(final String avmPath)
   {
      String storeName = AVMConstants.getStoreName(avmPath);
      storeName = AVMConstants.getCorrespondingMainStoreName(storeName);
      return AVMConstants.getCorrespondingPath(avmPath, storeName);
   }

   /**
    * Returns the corresponding path in the preview store name if this is a path in 
    * a main store.
    *
    * @param avmPath an avm path within the main store.
    * @return the corresponding path within the preview store.
    * @exception IllegalArgumentException if this is not a path within the preview store.
    */
   public static String getCorrespondingPathInPreviewStore(final String avmPath)
   {
      String storeName = AVMConstants.getStoreName(avmPath);
      storeName = AVMConstants.getCorrespondingPreviewStoreName(storeName);
      return AVMConstants.getCorrespondingPath(avmPath, storeName);
   }

   public static String getCorrespondingPath(final String avmPath, final String otherStore)
   {
      return (otherStore + ':' + AVMConstants.getStoreRelativePath(avmPath));
   }
   
   public static String buildStagingStoreName(final String storeId)
   {
      if (storeId == null || storeId.length() == 0)
      {
         throw new IllegalArgumentException("Store id is mandatory.");
      }
      return storeId;
   }
   
   public static String buildStagingPreviewStoreName(final String storeId)
   {
      return (AVMConstants.buildStagingStoreName(storeId) + 
              AVMConstants.STORE_SEPARATOR + AVMConstants.STORE_PREVIEW);
   }
   
   public static String buildUserMainStoreName(final String storeId, 
                                                  final String username)
   {
      if (username == null || username.length() == 0)
      {
         throw new IllegalArgumentException("Username is mandatory.");
      }
      return (AVMConstants.buildStagingStoreName(storeId) + AVMConstants.STORE_SEPARATOR + 
              username);
   }
   
   public static String buildUserPreviewStoreName(final String storeId, 
                                                     final String username)
   {
      return (AVMConstants.buildUserMainStoreName(storeId, username) +
              AVMConstants.STORE_SEPARATOR + AVMConstants.STORE_PREVIEW);
   }

   public static String buildWorkflowMainStoreName(final String storeId, 
                                                      final String workflowId)
   {
      if (workflowId == null || workflowId.length() == 0)
      {
         throw new IllegalArgumentException("workflowId is mandatory.");
      }
      return (AVMConstants.buildStagingStoreName(storeId) + AVMConstants.STORE_SEPARATOR +
              workflowId);
   }

   public static String buildWorkflowPreviewStoreName(final String storeId, 
                                                         final String workflowId)
   {
      return (AVMConstants.buildWorkflowMainStoreName(storeId, workflowId) +
              AVMConstants.STORE_SEPARATOR + AVMConstants.STORE_PREVIEW);
   }

   public static String buildStoreRootPath(final String storeName)
   {
      if (storeName == null || storeName.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      return storeName + ":/" + DIR_APPBASE;
   }

   public static String buildSandboxRootPath(final String storeName)
   {
      return AVMConstants.buildStoreRootPath(storeName) + '/' + DIR_WEBAPPS;
   }
   
   public static String buildStoreWebappPath(final String storeName, String webapp)
   {
      if (webapp == null || webapp.length() == 0)
      {
         throw new IllegalArgumentException("Webapp name is mandatory.");
      }
      return AVMConstants.buildSandboxRootPath(storeName) + '/' + webapp;
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
      return MessageFormat.format(PREVIEW_SANDBOX_URL, 
                                  lookupStoreDNS(store), 
                                  config.getWCMDomain(), 
                                  config.getWCMPort());
   }
   
   public static String buildWebappUrl(final String store, 
                                          final String webapp)
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
      return AVMConstants.buildAssetUrl(s[0], s[1]);
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
      if (assetPath.startsWith('/' + DIR_APPBASE + '/' + DIR_WEBAPPS))
      {
         assetPath = assetPath.substring(('/' + DIR_APPBASE + '/' + DIR_WEBAPPS).length());
      }
      if (assetPath.startsWith('/' + DIR_ROOT))
      {
         assetPath = assetPath.substring(('/' + DIR_ROOT).length());
      }
      if (assetPath.length() == 0 || assetPath.charAt(0) != '/')
      {
         assetPath = '/' + assetPath;
      }
      
      return MessageFormat.format(PREVIEW_ASSET_URL, dns, domain, port, assetPath);
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
         avmService.queryStorePropertyKey(store, QName.createQName(null, PROP_DNS + '%'));
      return (props.size() == 1
              ? props.keySet().iterator().next().getLocalName().substring(PROP_DNS.length())
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
      if (path == null || path.length() == 0 || 
          ".".equals(path) || "./".equals(path))
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
    * @param path    Path to match against
    * 
    * @return true if the path should require a virtualisation server reload, false otherwise
    */
   public static boolean requiresVServerUpdate(String path)
   {
      if (path == null || path.length() == 0)
      {
         throw new IllegalArgumentException("Path value is mandatory.");
      }
      
      return WEB_INF_PATH_PATTERN.matcher(path).matches();
   }
   
   /**
    * Update notification on the virtualisation server webapp as required for the specified path
    * 
    * @param path    Path to match against
    * @param force   True to force update of server even if path does not match
    */
   public static void updateVServerWebapp(String path, boolean force)
   {
      if (force || requiresVServerUpdate(path))
      {
         VirtServerRegistry vServerRegistry = AVMConstants.getVirtServerRegistry();
         int webappIndex = path.indexOf('/', path.indexOf(DIR_WEBAPPS) + DIR_WEBAPPS.length() + 1);
         if (webappIndex != -1)
         {
            path = path.substring(0, webappIndex);
         }
         vServerRegistry.webappUpdated(-1, path, true);
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
      if (force || requiresVServerUpdate(path))
      {
         VirtServerRegistry vServerRegistry = AVMConstants.getVirtServerRegistry();
            
         int webappIndex = path.indexOf('/', path.indexOf(DIR_WEBAPPS) + DIR_WEBAPPS.length() + 1);
         if (webappIndex != -1)
         {
            path = path.substring(0, webappIndex);
         }
         vServerRegistry.webappRemoved(-1, path, true);
      }
   }
   
   private static VirtServerRegistry getVirtServerRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      final WebApplicationContext ac = FacesContextUtils.getRequiredWebApplicationContext(fc);
      return (VirtServerRegistry)ac.getBean(BEAN_VIRT_SERVER_REGISTRY);
   }
   
   // Component Separator.
   private static final String STORE_SEPARATOR = "--";
   
   // names of the stores representing the layers for an AVM website
   //XXXarielb this should be private
   public final static String STORE_WORKFLOW = "workflow";
   private final static String STORE_PREVIEW = "preview";
   
   // system directories at the top level of an AVM website
   //
   // TODO:  The virtualization server should get these two parameters
   //        from the Alfresco webapp at registration time.
   //
   public final static String DIR_APPBASE = "appBase";
   public final static String DIR_WEBAPPS = "avm_webapps";

   // servlet default webapp
   //    Note: this webapp is mapped to the URL path ""
   public final static String DIR_ROOT = "ROOT";
   
   // system property keys for sandbox identification and DNS virtualisation mapping
   public final static String PROP_SANDBOXID = ".sandbox-id.";
   public final static String PROP_SANDBOX_STAGING_MAIN = ".sandbox.staging.main";
   public final static String PROP_SANDBOX_STAGING_PREVIEW = ".sandbox.staging.preview";
   public final static String PROP_SANDBOX_AUTHOR_MAIN = ".sandbox.author.main";
   public final static String PROP_SANDBOX_AUTHOR_PREVIEW = ".sandbox.author.preview";
   public final static String PROP_SANDBOX_WORKFLOW_MAIN = ".sandbox.workflow.main";
   public final static String PROP_SANDBOX_WORKFLOW_PREVIEW = ".sandbox.workflow.preview";
   public final static String PROP_DNS = ".dns.";
   public final static String PROP_WEBSITE_NAME = ".website.name";
   public final static String PROP_SANDBOX_STORE_PREFIX = ".sandbox.store.";
   public final static String SPACE_ICON_WEBSITE = "space-icon-website";
   
   // virtualisation server MBean registry
   private static final String BEAN_VIRT_SERVER_REGISTRY = "VirtServerRegistry";
   
   // URLs for preview of sandboxes and assets
   private final static String PREVIEW_SANDBOX_URL = "http://{0}.www--sandbox.{1}:{2}";
   private final static String PREVIEW_ASSET_URL = "http://{0}.www--sandbox.{1}:{2}{3}";
   
   // pattern for absolute AVM Path
   private final static Pattern STORE_RELATIVE_PATH_PATTERN = 
      Pattern.compile("[^:]+:(.+)");
   private final static Pattern WEBAPP_RELATIVE_PATH_PATTERN = 
      Pattern.compile("([^:]+:/" + AVMConstants.DIR_APPBASE +
                      "/" + AVMConstants.DIR_WEBAPPS + "/([^/]+))(.*)");
   private final static Pattern SANDBOX_RELATIVE_PATH_PATTERN = 
      Pattern.compile("([^:]+:/" + AVMConstants.DIR_APPBASE +
                      "/" + AVMConstants.DIR_WEBAPPS + ")(.*)");
   
   // patterns for WEB-INF files that require virtualisation server reload
   private final static Pattern WEB_INF_PATH_PATTERN = Pattern.compile(
         ".*:/" + AVMConstants.DIR_APPBASE + "/" + AVMConstants.DIR_WEBAPPS +
         "/.*/WEB-INF/((classes/.*)|(lib/.*)|(web.xml))",
         Pattern.CASE_INSENSITIVE);
}
