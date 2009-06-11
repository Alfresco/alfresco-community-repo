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
package org.alfresco.repo.imap;

import static org.alfresco.repo.imap.AlfrescoImapConst.CLASSPATH_TEXT_HTML_TEMPLATE;
import static org.alfresco.repo.imap.AlfrescoImapConst.CLASSPATH_TEXT_PLAIN_TEMPLATE;
import static org.alfresco.repo.imap.AlfrescoImapConst.DICTIONARY_TEMPLATE_PREFIX;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Flags.Flag;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.admin.patch.PatchInfo;
import org.alfresco.repo.admin.patch.PatchService;
import org.alfresco.repo.imap.config.ImapConfigBean;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * Helper class to access repository services by IMAP components. Also contains a common helper methods to search and
 * manage IMAP content and other usefull methods. Configured as {@code <bean id="imapHelper"
 * class="org.alfresco.repo.imap.ImapHelper">} in the {@code imap-server-context.xml} file.
 * 
 * @author Dmitry Vaserin
 */
/*package*/class ImapHelper extends AbstractLifecycleBean
{
    private static Log logger = LogFactory.getLog(ImapHelper.class);
    
    private static String PATCH_ID = "patch.imapFolders";

    private NodeService nodeService;
    private SearchService searchService;
    private FileFolderService fileFolderService;
    private TemplateService templateService;
    private NamespaceService namespaceService;
    private PermissionService permissionService;
    private DictionaryService dictionaryService;
    private PreferenceService preferenceService;
    private SiteService siteService;

    private ServiceRegistry serviceRegistry;
    
    private PatchService patchService;

    private String defaultFromAddress;
    private String webApplicationContextUrl = "http://localhost:8080/alfresco";
    private String repositoryTemplatePath;
    private String imapRoot;
    private NodeRef spacesStoreNodeRef;
    private NodeRef companyHomeNodeRef;
    private NodeRef imapRootNodeRef;
    
    private boolean patchApplied = false;

    private final static Map<QName, Flags.Flag> qNameToFlag;
    private final static Map<Flags.Flag, QName> flagToQname;

    private Map<String, ImapConfigBean> imapConfigBeans = Collections.emptyMap();

    static
    {
        qNameToFlag = new HashMap<QName, Flags.Flag>();
        qNameToFlag.put(ImapModel.PROP_FLAG_ANSWERED, Flags.Flag.ANSWERED);
        qNameToFlag.put(ImapModel.PROP_FLAG_DELETED, Flags.Flag.DELETED);
        qNameToFlag.put(ImapModel.PROP_FLAG_DRAFT, Flags.Flag.DRAFT);
        qNameToFlag.put(ImapModel.PROP_FLAG_SEEN, Flags.Flag.SEEN);
        qNameToFlag.put(ImapModel.PROP_FLAG_RECENT, Flags.Flag.RECENT);
        qNameToFlag.put(ImapModel.PROP_FLAG_FLAGGED, Flags.Flag.FLAGGED);

        flagToQname = new HashMap<Flags.Flag, QName>();
        flagToQname.put(Flags.Flag.ANSWERED, ImapModel.PROP_FLAG_ANSWERED);
        flagToQname.put(Flags.Flag.DELETED, ImapModel.PROP_FLAG_DELETED);
        flagToQname.put(Flags.Flag.DRAFT, ImapModel.PROP_FLAG_DRAFT);
        flagToQname.put(Flags.Flag.SEEN, ImapModel.PROP_FLAG_SEEN);
        flagToQname.put(Flags.Flag.RECENT, ImapModel.PROP_FLAG_RECENT);
        flagToQname.put(Flags.Flag.FLAGGED, ImapModel.PROP_FLAG_FLAGGED);
    }

    public static enum EmailBodyType
    {
        TEXT_PLAIN, TEXT_HTML;

        public String getSubtype()
        {
            return name().toLowerCase().substring(5);
        }

        public String getTypeSubtype()
        {
            return name().toLowerCase().replaceAll("_", "");
        }

        public String getMimeType()
        {
            return name().toLowerCase().replaceAll("_", "/");
        }

    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Do nothing
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                List<PatchInfo> patches = getPatchService().getPatches(null, null);
                for (PatchInfo patch : patches)
                {
                    if (patch.getId().equals(PATCH_ID))
                    {
                        patchApplied = true;
                        break;
                    }
                }
                
                if (!patchApplied)
                {
                    return null;
                }

                int indexOfStoreDelim = imapRoot.indexOf(StoreRef.URI_FILLER);

                if (indexOfStoreDelim == -1)
                {
                    throw new RuntimeException("Bad path format, " + StoreRef.URI_FILLER + " not found");
                }

                indexOfStoreDelim += StoreRef.URI_FILLER.length();

                int indexOfPathDelim = imapRoot.indexOf("/", indexOfStoreDelim);

                if (indexOfPathDelim == -1)
                {
                    throw new java.lang.RuntimeException("Bad path format, / not found");
                }

                String storePath = imapRoot.substring(0, indexOfPathDelim);
                String rootPathInStore = imapRoot.substring(indexOfPathDelim);

                StoreRef storeRef = new StoreRef(storePath);

                if (nodeService.exists(storeRef) == false)
                {
                    throw new RuntimeException("No store for path: " + storeRef);
                }

                NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

                List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, rootPathInStore, null, namespaceService, false);

                if (nodeRefs.size() > 1)
                {
                    throw new RuntimeException("Multiple possible roots for : \n" + "   root path: " + rootPathInStore + "\n" + "   results: " + nodeRefs);
                }
                else if (nodeRefs.size() == 0)
                {
                    throw new RuntimeException("No root found for : \n" + "   root path: " + rootPathInStore);
                }

                imapRootNodeRef = nodeRefs.get(0);

                // Get "Company Home" node reference
                StoreRef store = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
                ResultSet rs = searchService.query(store, SearchService.LANGUAGE_XPATH, "/app:company_home");
                try
                {
                    if (rs.length() == 0)
                    {
                        throw new AlfrescoRuntimeException("'Company Home' space doesn't exists.");
                    }
                    companyHomeNodeRef = rs.getNodeRef(0);
                }
                finally
                {
                    rs.close();
                }

                spacesStoreNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Search for files in specified context
     * 
     * @param contextNodeRef context folder for search
     * @param namePattern name pattern for search
     * @param searchType type for search
     * @param includeSubFolders include SubFolders
     * @return list of files with specifed type
     */
    public List<FileInfo> searchFiles(NodeRef contextNodeRef, String namePattern, QName searchType, boolean includeSubFolders)
    {
        return search(contextNodeRef, namePattern, searchType, true, false, includeSubFolders);
    }

    /**
     * Search for mailboxes in specified context
     * 
     * @param contextNodeRef context folder for search
     * @param namePattern name pattern for search
     * @param includeSubFolders include SubFolders
     * @param isVirtualView is folder in "Virtual" View
     * @return list of mailboxes
     */
    public List<FileInfo> searchFolders(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders, boolean isVirtualView)
    {
        QName searchType = ContentModel.TYPE_FOLDER;
        if (isVirtualView)
        {
            searchType = null;
        }

        List<FileInfo> result = search(contextNodeRef, namePattern, searchType, false, true, includeSubFolders);
        if (isVirtualView)
        {
            List<SiteInfo> nonFavSites = getNonFavouriteSites(getCurrentUser());
            for (SiteInfo siteInfo : nonFavSites)
            {
                FileInfo nonFavSite = fileFolderService.getFileInfo(siteInfo.getNodeRef());
                List<FileInfo> siteChilds = search(nonFavSite.getNodeRef(), namePattern, null, false, true, true);
                result.removeAll(siteChilds);
                result.remove(nonFavSite);
            }

        }
        else
        {
            // Remove folders from Sites
            List<SiteInfo> sites = siteService.listSites(getCurrentUser());
            for (SiteInfo siteInfo : sites)
            {
                List<FileInfo> siteChilds = search(siteInfo.getNodeRef(), namePattern, null, false, true, true);
                result.removeAll(siteChilds);
            }

        }
        return result;
    }

    /**
     * Search for emails in specified folder depend on view mode.
     * 
     * @param contextNodeRef context folder for search
     * @param namePattern name pattern for search
     * @param viewMode context folder view mode
     * @param includeSubFolders includeSubFolders
     * @return list of emails that context folder contains.
     */
    public List<FileInfo> searchMails(NodeRef contextNodeRef, String namePattern, String viewMode, boolean includeSubFolders)
    {

        List<FileInfo> result = new LinkedList<FileInfo>();
        if (viewMode.equals(AlfrescoImapConst.MODE_ARCHIVE))
        {
            result = search(contextNodeRef, namePattern, ImapModel.TYPE_IMAP_CONTENT, false, true, includeSubFolders);
        }
        else
        {
            if (viewMode.equals(AlfrescoImapConst.MODE_VIRTUAL))
            {
                result = search(contextNodeRef, namePattern, null, true, false, includeSubFolders);
            }
        }

        return result;
    }

    private List<FileInfo> search(NodeRef contextNodeRef, String namePattern, QName searchType, boolean fileSearch, boolean folderSearch, boolean includeSubFolders)
    {
        List<FileInfo> result = new LinkedList<FileInfo>();
        List<FileInfo> searchResult = fileFolderService.search(contextNodeRef, namePattern, fileSearch, folderSearch, includeSubFolders);

        if (searchType == null)
        {
            return searchResult;
        }

        for (FileInfo fileInfo : searchResult)
        {
            if (nodeService.getType(fileInfo.getNodeRef()).equals(searchType))
            {
                result.add(fileInfo);
            }
        }

        return result;
    }

    /**
     * Get root reference for the specified mailbox
     * 
     * @param mailboxName mailbox name in IMAP client.
     * @param userName
     * @return
     */
    public NodeRef getMailboxRootRef(String mailboxName, String userName)
    {
        String rootFolder;
        int index = mailboxName.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        if (index > 0)
        {
            rootFolder = mailboxName.substring(0, index);
        }
        else
        {
            rootFolder = mailboxName;
        }

        Map<String, ImapConfigBean> imapConfigs = getImapConfig();
        if (imapConfigs.keySet().contains(rootFolder))
        {
            Map<String, NodeRef> mountPoints = getMountPoints();
            NodeRef mountRef = mountPoints.get(rootFolder);
            return nodeService.getParentAssocs(mountRef).get(0).getParentRef();
        }
        else
        {
            return getUserImapHomeRef(userName);
        }
    }

    /**
     * @param userName user name
     * @return user IMAP home reference and create it if it doesn't exist.
     */
    public NodeRef getUserImapHomeRef(final String userName)
    {
        NodeRef userHome = fileFolderService.searchSimple(imapRootNodeRef, userName);
        if (userHome == null)
        {
            // create user home
            userHome = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    NodeRef result = fileFolderService.create(imapRootNodeRef, userName, ContentModel.TYPE_FOLDER).getNodeRef();
                    nodeService.setProperty(result, ContentModel.PROP_DESCRIPTION, userName);
                    // create inbox
                    fileFolderService.create(result, AlfrescoImapConst.INBOX_NAME, ContentModel.TYPE_FOLDER);
                    return result;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
        return userHome;
    }

    public String getCurrentUser()
    {
        return AuthenticationUtil.getFullyAuthenticatedUser();
    }

    public String getUserImapHomeId(String userName)
    {
        return getUserImapHomeRef(userName).getId();
    }

    public NodeRef getImapRootNodeRef()
    {
        return imapRootNodeRef;
    }

    public NodeRef getCompanyHomeNodeRef()
    {
        return companyHomeNodeRef;
    }

    public NodeRef getSpacesStoreNodeRef()
    {
        return spacesStoreNodeRef;
    }

    public void setImapRoot(String imapRoot)
    {
        this.imapRoot = imapRoot;
    }

    public String getDefaultFromAddress()
    {
        return defaultFromAddress;
    }

    public void setDefaultFromAddress(String defaultFromAddress)
    {
        this.defaultFromAddress = defaultFromAddress;
    }

    public String getWebApplicationContextUrl()
    {
        return this.webApplicationContextUrl;
    }

    public void setWebApplicationContextUrl(String webApplicationContextUrl)
    {
        this.webApplicationContextUrl = webApplicationContextUrl;
    }

    public String getRepositoryTemplatePath()
    {
        return repositoryTemplatePath;
    }

    public void setRepositoryTemplatePath(String repositoryTemplatePath)
    {
        this.repositoryTemplatePath = repositoryTemplatePath;
    }

    /**
     * Return flags that belong to the specified imap folder.
     * 
     * @param messageInfo imap folder info.
     * @return flags.
     */
    public Flags getFlags(FileInfo messageInfo)
    {
        Flags flags = new Flags();
        checkForFlaggableAspect(messageInfo.getNodeRef());
        Map<QName, Serializable> props = nodeService.getProperties(messageInfo.getNodeRef());

        for (QName key : qNameToFlag.keySet())
        {
            Boolean value = (Boolean) props.get(key);
            if (value != null && value)
            {
                flags.add(qNameToFlag.get(key));
            }
        }
        // This is a multiuser flag support. Commented due new requirements
        // for (QName key : qNameToFlag.keySet())
        // {
        // if (key.equals(ImapModel.PROP_FLAG_DELETED))
        // {
        // Boolean value = (Boolean) props.get(key);
        // if (value != null && value)
        // {
        // flags.add(qNameToFlag.get(key));
        // }
        // }
        // else
        // {
        // String users = (String) props.get(key);
        //
        // if (users != null && users.indexOf(formatUserEntry(getCurrentUser())) >= 0)
        // {
        // flags.add(qNameToFlag.get(key));
        // }
        // }
        // }

        return flags;
    }

    /**
     * Set flags to the specified imapFolder.
     * 
     * @param messageInfo FileInfo of imap Folder.
     * @param flags flags to set.
     * @param value value to set.
     */
    public void setFlags(FileInfo messageInfo, Flags flags, boolean value)
    {
        checkForFlaggableAspect(messageInfo.getNodeRef());
        for (Flags.Flag flag : flags.getSystemFlags())
        {
            setFlag(messageInfo, flag, value);
        }
    }

    /**
     * Set flags to the specified imapFolder.
     * 
     * @param messageInfo FileInfo of imap Folder
     * @param flag flag to set.
     * @param value value value to set.
     */
    public void setFlag(final FileInfo messageInfo, final Flag flag, final boolean value)
    {
        checkForFlaggableAspect(messageInfo.getNodeRef());
        nodeService.setProperty(messageInfo.getNodeRef(), flagToQname.get(flag), value);

        // This is a multiuser flag support. Commented due new requirements
        // if (flagToQname.get(flag).equals(ImapModel.PROP_FLAG_DELETED))
        // {
        // nodeService.setProperty(messageInfo.getNodeRef(), flagToQname.get(flag), value);
        // }
        // else
        // {
        // AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        // {
        // public Void doWork() throws Exception
        // {
        //
        // String users = (String) nodeService.getProperty(messageInfo.getNodeRef(), flagToQname.get(flag));
        // if (value)
        // {
        // if (users == null)
        // {
        // users = "";
        // }
        // users += formatUserEntry(getCurrentUser());
        //
        // }
        // else if (users != null)
        // {
        // users = users.replace(formatUserEntry(getCurrentUser()), "");
        //
        // }
        // nodeService.setProperty(messageInfo.getNodeRef(), flagToQname.get(flag), users);
        // return null;
        // }
        // }, AuthenticationUtil.getSystemUserName());
        // }

    }

    /**
     * Check that the given authentication has a particular permission for the given node.
     * 
     * @param nodeRef nodeRef of the node
     * @param permission permission for check
     * @return the access status
     */
    public AccessStatus hasPermission(NodeRef nodeRef, String permission)
    {
        return permissionService.hasPermission(nodeRef, permission);
    }

    /**
     * Change userName into following format ;userName;
     * 
     * @param userName
     * @return
     */
    public String formatUserEntry(String userName)
    {
        return AlfrescoImapConst.USER_SEPARATOR + userName + AlfrescoImapConst.USER_SEPARATOR;
    }

    /**
     * This method should returns a unique identifier of Alfresco server. The possible UID may be calculated based on IP address, Server port, MAC address, Web Application context.
     * This UID should be parseable into initial components. This necessary for the implementation of the following case: If the message being copied (e.g. drag-and-drop) between
     * two different Alfresco accounts in the IMAP client, we must unambiguously identify from which Alfresco server this message being copied. The message itself does not contain
     * content data, so we must download it from the initial server (e.g. using download content servlet) and save it into destination repository.
     * 
     * @return String representation of unique identifier of Alfresco server
     */
    public String getAlfrescoServerUID()
    {
        // TODO Implement as javadoc says.
        return "Not-Implemented";
    }

    /**
     * Map of mount points. Name of mount point == key in the map.
     * 
     * @return Map of mount points.
     */
    public Map<String, NodeRef> getMountPoints()
    {
        Map<String, ImapConfigBean> imapConfigs = getImapConfig();
        Map<String, NodeRef> mountPoints = new HashMap<String, NodeRef>();

        for (ImapConfigBean config : imapConfigs.values())
        {
            // Get node reference
            StoreRef store = new StoreRef(config.getStore());
            ResultSet rs = searchService.query(store, SearchService.LANGUAGE_XPATH, config.getRootPath());
            if (rs.length() == 0)
            {
                logger.warn("Didn't find " + config.getName());
            }
            else
            {
                NodeRef nodeRef = rs.getNodeRef(0);
                mountPoints.put(config.getName(), nodeRef);
            }
            rs.close();
        }
        return mountPoints;
    }

    public void setImapConfigBeans(ImapConfigBean[] imapConfigBeans)
    {
        this.imapConfigBeans = new LinkedHashMap<String, ImapConfigBean>(imapConfigBeans.length * 2);
        for (ImapConfigBean bean : imapConfigBeans)
        {
            this.imapConfigBeans.put(bean.getName(), bean);
        }
    }

    /**
     * Return map of imap configs. Name of config == key in the map
     * 
     * @return map of imap configs.
     */
    public Map<String, ImapConfigBean> getImapConfig()
    {
        return this.imapConfigBeans;
    }

    /**
     * Return view mode ("virtual" or "archive") for specified mailbox.
     * 
     * @param mailboxName name of the mailbox in IMAP client.
     * @return view mode of the specified mailbox.
     */
    public String getViewMode(String mailboxName)
    {
        String rootFolder;
        int index = mailboxName.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        if (index > 0)
        {
            rootFolder = mailboxName.substring(0, index);
        }
        else
        {
            rootFolder = mailboxName;
        }
        Map<String, ImapConfigBean> imapConfigs = getImapConfig();
        if (imapConfigs.keySet().contains(rootFolder))
        {
            return imapConfigs.get(rootFolder).getMode();
        }
        else
        {
            return AlfrescoImapConst.MODE_ARCHIVE;
        }
    }

    /**
     * Return mount point name, which was specified in imap-config.xml for the current mailbox.
     * 
     * @param mailboxName mailbox name in IMAP client.
     * @return mount point name or null.
     */
    public String getMountPointName(String mailboxName)
    {
        String rootFolder;
        int index = mailboxName.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        if (index > 0)
        {
            rootFolder = mailboxName.substring(0, index);
        }
        else
        {
            rootFolder = mailboxName;
        }
        Map<String, ImapConfigBean> imapConfigs = getImapConfig();
        if (imapConfigs.keySet().contains(rootFolder))
        {
            return rootFolder;
        }
        else
        {
            return null;
        }

    }

    /**
     * Convert mailpath from IMAP client representation to the alfresco representation view. (e.g. with default settings "getMailPathInRepo(Repository_virtual.Imap Home)" will
     * return "Company Home.Imap Home")
     * 
     * @param mailPath mailbox path in IMAP client
     * @return mailbox path in alfresco
     */
    public String getMailPathInRepo(String mailPath)
    {
        String rootFolder;
        String remain = "";
        int index = mailPath.indexOf(AlfrescoImapConst.HIERARCHY_DELIMITER);
        if (index > 0)
        {
            rootFolder = mailPath.substring(0, index);
            remain = mailPath.substring(index);
        }
        else
        {
            rootFolder = mailPath;
        }
        Map<String, ImapConfigBean> imapConfigs = getImapConfig();
        if (imapConfigs.keySet().contains(rootFolder))
        {
            Map<String, NodeRef> mountPoints = getMountPoints();
            NodeRef rootRef = mountPoints.get(rootFolder);
            String rootName = nodeService.getProperty(rootRef, ContentModel.PROP_NAME).toString();

            return rootName + remain;
        }
        else
        {
            return mailPath;
        }
    }

    /**
     * Return list of sites, that belong to the specified user and not marked as "Imap favourite"
     * 
     * @param userName name of user
     * @return List of nonFavourite sites.
     */
    public List<SiteInfo> getNonFavouriteSites(String userName)
    {
        List<SiteInfo> nonFavSites = new LinkedList<SiteInfo>();
        Map<String, Serializable> prefs = preferenceService.getPreferences(userName, AlfrescoImapConst.PREF_IMAP_FAVOURITE_SITES);
        List<SiteInfo> sites = siteService.listSites(userName);
        for (SiteInfo siteInfo : sites)
        {
            String key = AlfrescoImapConst.PREF_IMAP_FAVOURITE_SITES + "." + siteInfo.getShortName();
            Boolean isImapFavourite = (Boolean) prefs.get(key);
            if (isImapFavourite == null || !isImapFavourite)
            {
                nonFavSites.add(siteInfo);
            }
        }

        return nonFavSites;
    }

    /**
     * Returns the text representing email body for ContentModel node.
     * 
     * @param nodeRef NodeRef of the target content.
     * @param type The type of the returned body. May be the one of {@link EmailBodyType}.
     * @return Text representing email body for ContentModel node.
     */
    public String getEmailBodyText(NodeRef nodeRef, EmailBodyType type)
    {
        return templateService.processTemplate(getDefaultEmailBodyTemplate(type), createEmailTemplateModel(nodeRef));
    }

    /**
     * Returns default email body template. This method trying to find a template on the path in the repository first e.g. {@code "Data Dictionary > IMAP Templates >"}. This path
     * should be set as the property of the "imapHelper" bean. In this case it returns {@code NodeRef.toString()} of the template. If there are no template in the repository it
     * returns a default template on the classpath.
     * 
     * @param type One of the {@link EmailBodyType}.
     * @return String representing template classpath path or NodeRef.toString().
     */
    public String getDefaultEmailBodyTemplate(EmailBodyType type)
    {
        String result = null;
        switch (type)
        {
        case TEXT_HTML:
            result = CLASSPATH_TEXT_HTML_TEMPLATE;
            break;
        case TEXT_PLAIN:
            result = CLASSPATH_TEXT_PLAIN_TEMPLATE;
            break;
        }
        final StringBuilder templateName = new StringBuilder(DICTIONARY_TEMPLATE_PREFIX).append("-").append(type.getTypeSubtype()).append(".ftl");
        int indexOfStoreDelim = repositoryTemplatePath.indexOf(StoreRef.URI_FILLER);
        if (indexOfStoreDelim == -1)
        {
            logger.error("Bad path format, " + StoreRef.URI_FILLER + " not found");
            return result;
        }
        indexOfStoreDelim += StoreRef.URI_FILLER.length();
        int indexOfPathDelim = repositoryTemplatePath.indexOf("/", indexOfStoreDelim);
        if (indexOfPathDelim == -1)
        {
            logger.error("Bad path format, / not found");
            return result;
        }
        final String storePath = repositoryTemplatePath.substring(0, indexOfPathDelim);
        final String rootPathInStore = repositoryTemplatePath.substring(indexOfPathDelim);
        final String query = String.format("+PATH:\"%1$s/*\" +@cm\\:name:\"%2$s\"", rootPathInStore, templateName.toString());
        if (logger.isDebugEnabled())
        {
            logger.debug("Using template path :" + repositoryTemplatePath + "/" + templateName);
            logger.debug("Query: " + query);
        }
        StoreRef storeRef = new StoreRef(storePath);
        ResultSet resultSet = searchService.query(storeRef, "lucene", query);
        if (resultSet == null || resultSet.length() == 0)
        {
            logger.error(String.format("IMAP message template '%1$s' does not exist in the path '%2$s'.", templateName, repositoryTemplatePath));
            return result;
        }
        result = resultSet.getNodeRef(0).toString();
        return result;
    }

    /**
     * Builds default email template model for TemplateProcessor
     * 
     * @param ref NodeRef of the target content.
     * @return Map that includes template model objects.
     */
    private Map<String, Object> createEmailTemplateModel(NodeRef ref)
    {
        Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
        TemplateNode tn = new TemplateNode(ref, serviceRegistry, null);
        model.put("document", tn);
        NodeRef parent = nodeService.getPrimaryParent(ref).getParentRef();
        model.put("space", new TemplateNode(parent, serviceRegistry, null));
        model.put("date", new Date());
        model.put("contextUrl", new String(getWebApplicationContextUrl()));
        model.put("alfTicket", new String(serviceRegistry.getAuthenticationService().getCurrentTicket()));
        return model;
    }

    private void  checkForFlaggableAspect(NodeRef nodeRef)
    {
        if (!nodeService.hasAspect(nodeRef, ImapModel.ASPECT_FLAGGABLE))
        {
            Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
            nodeService.addAspect(nodeRef, ImapModel.ASPECT_FLAGGABLE, aspectProperties);
        }
    }
    
    public boolean isPatchApplied()
    {
        return patchApplied;
    }

    // ----------------------Getters and Setters----------------------------

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public FileFolderService getFileFolderService()
    {
        return fileFolderService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public TemplateService getTemplateService()
    {
        return templateService;
    }

    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }

    public NamespaceService getNamespaceService()
    {
        return namespaceService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public PreferenceService getPreferenceService()
    {
        return preferenceService;
    }

    public void setPreferenceService(PreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }

    public SiteService getSiteService()
    {
        return siteService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    public PatchService getPatchService()
    {
        return patchService;
    }

    public void setPatchService(PatchService patchService)
    {
	    this.patchService = patchService;
    }

}
