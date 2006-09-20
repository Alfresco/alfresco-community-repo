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
      return store + AVMConstants.STORE_STAGING;
   }
   
   public static String buildAVMStagingPreviewStoreName(String store)
   {
      return store + AVMConstants.STORE_PREVIEW;
   }
   
   public static String buildAVMUserMainStoreName(String store, String username)
   {
      return store + '-' + username + AVMConstants.STORE_MAIN;
   }
   
   public static String buildAVMUserPreviewStoreName(String store, String username)
   {
      return store + '-' + username + AVMConstants.STORE_PREVIEW;
   }
   
   public static String buildAVMStoreRootPath(String store)
   {
      return store + ":/" + DIR_APPBASE + '/' + DIR_WEBAPPS;
   }
   
   // names of the stores representing the layers for an AVM website
   public final static String STORE_STAGING = "-staging";
   public final static String STORE_MAIN = "-main";
   public final static String STORE_PREVIEW = "-preview";
   
   // system directories at the top level of an AVM website
   public final static String DIR_APPBASE = "appBase";
   public final static String DIR_WEBAPPS = "avm_webapps";
   
   // system property keys for sandbox identification and DNS virtualisation mapping
   public final static String PROP_SANDBOXID = ".sandbox-id.";
   public final static String PROP_SANDBOX_STAGING_MAIN = ".sandbox.staging.main";
   public final static String PROP_SANDBOX_STAGING_PREVIEW = ".sandbox.staging.preview";
   public final static String PROP_SANDBOX_AUTHOR_MAIN = ".sandbox.author.main";
   public final static String PROP_SANDBOX_AUTHOR_PREVIEW = ".sandbox.author.preview";
   public final static String PROP_DNS = ".dns.";
   public final static String PROP_WEBSITE_NAME = ".website.name";
   public final static String SPACE_ICON_WEBSITE = "space-icon-website";
}
