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
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.ClientConfigElement;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * @author Kevin Roast
 */
public final class AVMConstants
{
   /**
    * Private constructor
    */
   private AVMConstants()
   {
   }
   
   public static String buildAVMStagingStoreName(String store)
   {
      if (store == null || store.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      return store + AVMConstants.STORE_STAGING;
   }
   
   public static String buildAVMStagingPreviewStoreName(String store)
   {
      if (store == null || store.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      return store + AVMConstants.STORE_PREVIEW;
   }
   
   public static String buildAVMUserMainStoreName(String store, String username)
   {
      if (store == null || store.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      if (username == null || username.length() == 0)
      {
         throw new IllegalArgumentException("Username is mandatory.");
      }
      return store + '-' + username + AVMConstants.STORE_MAIN;
   }
   
   public static String buildAVMUserPreviewStoreName(String store, String username)
   {
      if (store == null || store.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      if (username == null || username.length() == 0)
      {
         throw new IllegalArgumentException("Username is mandatory.");
      }
      return store + '-' + username + AVMConstants.STORE_PREVIEW;
   }
   
   public static String buildAVMStoreRootPath(String store)
   {
      if (store == null || store.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      return store + ":/" + DIR_APPBASE + '/' + DIR_WEBAPPS;
   }
   
   public static String buildAVMStoreWebappPath(String store, String webapp)
   {
      if (store == null || store.length() == 0)
      {
         throw new IllegalArgumentException("Store name is mandatory.");
      }
      if (webapp == null || webapp.length() == 0)
      {
         throw new IllegalArgumentException("Webapp name is mandatory.");
      }
      return store + ":/" + DIR_APPBASE + '/' + DIR_WEBAPPS + '/' + webapp;
   }
   
   public static String buildAVMStoreUrl(String store)
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
      return MessageFormat.format(PREVIEW_SANDBOX_URL, lookupStoreDNS(store), config.getWCMDomain(), config.getWCMPort());
   }
   
   public static String buildAVMWebappUrl(String store, String webapp)
   {
      if (webapp == null || webapp.length() == 0)
      {
         throw new IllegalArgumentException("Webapp name is mandatory.");
      }
      if (!webapp.equals(DIR_ROOT))
      {
         return buildAVMStoreUrl(store) + '/' + webapp;
      }
      else
      {
         return buildAVMStoreUrl(store);
      }
   }
   
   public static String buildAVMAssetUrl(final String avmPath)
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
      return AVMConstants.buildAVMAssetUrl(s[0], s[1]);
   }
   
   public static String buildAVMAssetUrl(String store, String assetPath)
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
      return buildAVMAssetUrl(assetPath, config.getWCMDomain(), config.getWCMPort(), lookupStoreDNS(store));
   }
   
   public static String buildAVMAssetUrl(String assetPath, String domain, String port, String dns)
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
      
      String dns = null;
      
      AVMService avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      Map<QName, PropertyValue> props = avmService.queryStorePropertyKey(store, QName.createQName(null, PROP_DNS + '%'));
      if (props.size() == 1)
      {
         dns = props.entrySet().iterator().next().getKey().getLocalName().substring(PROP_DNS.length());
      }
      
      return dns;
   }

   /**
    * Converts the provided path to an absolute path within the avm.
    *
    * @param parentAVMPath used as the parent path if the provided path
    * is relative, otherwise used to extract the parent path portion up until
    * the webapp directory.
    * @param path a path relative to the parentAVMPath path, or if it is
    * absolute, it is relative to the webapp used in the parentAVMPath.
    *
    * @return an absolute path within the avm using the paths provided.
    */
   public static String buildAbsoluteAVMPath(final String parentAVMPath, final String path)
   {
      String parent = parentAVMPath;
      if (path == null || path.length() == 0 || 
          ".".equals(path) || "./".equals(path))
      {
         return parent;
      }
      
      if (path.charAt(0) == '/')
      {
         final Matcher m = absoluteAVMPath.matcher(parent);
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
    * Returns a path relative to the webapp portion of the avm path.
    *
    * @param absoluteAVMPath an absolute path within the avm
    * @return a relative path within the webapp.
    */
   public static String getWebappRelativePath(final String absoluteAVMPath)
   {
      final Matcher m = webappRelativePath.matcher(absoluteAVMPath);
      return m.matches() && m.group(1).length() != 0 ? m.group(1) : "/";
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
      
      return webinfPathPattern.matcher(path).matches();
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
         VirtServerRegistry vServerRegistry = (VirtServerRegistry)FacesContextUtils.getRequiredWebApplicationContext(
               FacesContext.getCurrentInstance()).getBean(BEAN_VIRT_SERVER_REGISTRY);
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
         VirtServerRegistry vServerRegistry = (VirtServerRegistry)FacesContextUtils.getRequiredWebApplicationContext(
               FacesContext.getCurrentInstance()).getBean(BEAN_VIRT_SERVER_REGISTRY);
         int webappIndex = path.indexOf('/', path.indexOf(DIR_WEBAPPS) + DIR_WEBAPPS.length() + 1);
         if (webappIndex != -1)
         {
            path = path.substring(0, webappIndex);
         }
         vServerRegistry.webappRemoved(-1, path, true);
      }
   }
   
   
   // names of the stores representing the layers for an AVM website
   public final static String STORE_STAGING = "-staging";
   public final static String STORE_MAIN = "-main";
   public final static String STORE_PREVIEW = "-preview";
   
   // system directories at the top level of an AVM website
   public final static String DIR_APPBASE = "appBase";
   public final static String DIR_WEBAPPS = "avm_webapps";
   
   // servlet implicit root directory
   public final static String DIR_ROOT = "ROOT";
   
   // system property keys for sandbox identification and DNS virtualisation mapping
   public final static String PROP_SANDBOXID = ".sandbox-id.";
   public final static String PROP_SANDBOX_STAGING_MAIN = ".sandbox.staging.main";
   public final static String PROP_SANDBOX_STAGING_PREVIEW = ".sandbox.staging.preview";
   public final static String PROP_SANDBOX_AUTHOR_MAIN = ".sandbox.author.main";
   public final static String PROP_SANDBOX_AUTHOR_PREVIEW = ".sandbox.author.preview";
   public final static String PROP_DNS = ".dns.";
   public final static String PROP_WEBSITE_NAME = ".website.name";
   public final static String PROP_SANDBOX_STORE_PREFIX = ".sandbox.store.";
   public final static String SPACE_ICON_WEBSITE = "space-icon-website";
   
   // virtualisation server MBean registry
   private static final String BEAN_VIRT_SERVER_REGISTRY = "VirtServerRegistry";
   
   // URLs for preview of sandboxes and assets
   private final static String PREVIEW_SANDBOX_URL = "http://www-{0}.{1}:{2}";
   private final static String PREVIEW_ASSET_URL = "http://www-{0}.{1}:{2}{3}";
   
   // pattern for absolute AVM Path
   private final static Pattern absoluteAVMPath = Pattern.compile(
         "([^:]+:/" + AVMConstants.DIR_APPBASE + "/[^/]+/[^/]+).*");
   private final static Pattern webappRelativePath = Pattern.compile(
         "[^:]+:/" + AVMConstants.DIR_APPBASE +
         "/" + AVMConstants.DIR_WEBAPPS + "/[^/]+(.*)");
   
   // patterns for WEB-INF files that require virtualisation server reload
   private final static Pattern webinfPathPattern = Pattern.compile(
         ".*:/" + AVMConstants.DIR_APPBASE + "/" + AVMConstants.DIR_WEBAPPS +
         "/.*/WEB-INF/((classes/.*)|(lib/.*)|(web.xml))",
         Pattern.CASE_INSENSITIVE);
}
