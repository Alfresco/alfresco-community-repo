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
package org.alfresco.repo.googledocs;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.IEntry;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclFeed;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.docs.PdfEntry;
import com.google.gdata.data.docs.PresentationEntry;
import com.google.gdata.data.docs.SpreadsheetEntry;
import com.google.gdata.data.docs.DocumentListEntry.MediaType;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

/**
 * Google docs integration service implementation
 */
public class GoogleDocsServiceImpl extends TransactionListenerAdapter
                                   implements GoogleDocsService, GoogleDocsModel
{    
    /** Log */
    private static Log logger = LogFactory.getLog(GoogleDocsServiceImpl.class);

    /** Google document types */
    public static final String TYPE_DOCUMENT = "document";
    public static final String TYPE_SPREADSHEET = "spreadsheet";
    public static final String TYPE_PRESENTATION = "presentation";
    public static final String TYPE_PDF = "pdf";
    
    /** Transaction resource keys */
    private final static String KEY_MARKED_CREATE = "google_doc_service.marked_resources";
    private final static String KEY_MARKED_DELETE = "google_doc_service.marked_delete";

    /** Services */
    private DocsService googleDocumentService;
    private GoogleService spreadsheetsService;
    private NodeService nodeService;
    private ContentService contentService;
    private PersonService personService;
    private MimetypeService mimetypeService;
    private PermissionService permissionService;
    private OwnableService ownableService;
    private AuthorityService authorityService;
    private DictionaryService dictionaryService;
    
    /** Indicates whether the GDoc integration is enabled or not */
    private boolean enabled = false;
    
    /** GoogleDoc base feed url */
    private String url = "http://docs.google.com/feeds/default/private/full";
    private String downloadUrl = "https://docs.google.com/feeds/download";
    
    /** Authentication credentials */
    private boolean initialised = false;    
    private String username;
    private String password;
    
    /** Permission map */
    private Map<String, String> permissionMap;
    
    // TODO: need a way of indicating if a customer is a premium user or not 
    
    /** 
     * List of supported GoogleDoc supported mimetypes.
     * Taken from list found at http://code.google.com/apis/documents/faq.html#WhatKindOfFilesCanIUpload
     * NOTE: this restriction only applies to non-premium users.
     */
    // TODO make this list configurable
    private List<String> supportedMimetypes = Arrays.asList(
    	"text/csv",
    	"text/tab-separated-values",
    	"text/html",
    	"application/msword",
    	"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    	"application/x-vnd.oasis.opendocument.spreadsheet",
    	"application/vnd.oasis.opendocument.text",
    	"application/rtf",
    	"application/vnd.sun.xml.writer",
    	"text/plain",
    	"application/vnd.ms-excel",
    	"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    	"application/pdf",
    	"application/vnd.ms-powerpoint",
    	"image/x-wmf"
    );

    /**
     * @param googleDocumentService google document service
     */
    public void setGoogleDocumentService(DocsService googleDocumentService)
    {
        this.googleDocumentService = googleDocumentService;
    }

    /**
     * @param spreadsheetsService   spread sheets service
     */
    public void setSpreadsheetsService(GoogleService spreadsheetsService)
    {
        this.spreadsheetsService = spreadsheetsService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param personService     person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * @param mimetypeService   mime type service
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    /**
     * @param permissionService     permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * @param ownableService    ownable service
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }
    
    /**
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService) 
    {
		this.dictionaryService = dictionaryService;
	}
    
    /**
     * @param url  root googleDoc URL
     */
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    /**
     * @param downloadUrl   root download URL
     */
    public void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    /**
     * @param username  google service user name
     */
    public void setUsername(String username)
    {
        this.username = username;
        this.initialised = false;
    }

    /**
     * @param password  google service password
     */
    public void setPassword(String password)
    {
        this.password = password;
        this.initialised = false;
    }
    
    /**
     * @param permissionMap permission map
     */
    public void setPermissionMap(Map<String, String> permissionMap)
    {
        this.permissionMap = permissionMap;
    }
    
    /**
     * Set whether the service is enabled or not.
     * @param enabled   true if enabled, false otherwise
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        this.initialised = false;
    }
    
    /**
     * @see org.alfresco.repo.googledocs.GoogleDocsService#isEnabled()
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Initialise google docs services
     */
    public void initialise() throws GoogleDocsServiceInitException
    {
        if (initialised == false)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Trying to initialise google docs service for user " + username);
            }
            
            if (username == null ||username.length() == 0 || password == null)
            {
                throw new GoogleDocsServiceInitException("No Google Docs credentials found. Please set the Google Docs authentication configuration.");
            }
            
            try
            {
                googleDocumentService.setUserCredentials(username, password);
                spreadsheetsService.setUserCredentials(username, password);
                googleDocumentService.setChunkedMediaUpload(-1);
            }
            catch (AuthenticationException e)
            {
                throw new GoogleDocsServiceInitException("Unable to connect to Google Docs.  Please check the Google Docs authentication configuration.", e);
            }
            
            initialised = true;
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Successfully initialised google docs service for user " + username);
            }
        }
    }

    /**
     * @see org.alfresco.repo.googledocs.GoogleDocsService#isSupportedMimetype(java.lang.String)
     */
    @Override
    public boolean isSupportedMimetype(String mimetype) 
    {
    	return supportedMimetypes.contains(mimetype);
    }
    
    /**
     * @throws GoogleDocsUnsupportedMimetypeException 
     * @see org.alfresco.google.docs.GoogleDocsService#upload(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void createGoogleDoc(NodeRef nodeRef, GoogleDocsPermissionContext permissionContext)
    {
        // Check for mandatory parameters
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        // Initialise google doc services
        try
        {
        	initialise();
        }
        catch (GoogleDocsServiceInitException e)
        {
        	throw new AlfrescoRuntimeException("Unable to create google doc, because service could not be initialised.", e);
        }

        // Get property values
        String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      
        // TODO should be checking to make sure this doesn't already have an associated google doc
        
        // Get content reader
        String mimetype = null;
        InputStream is = null;
        ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (contentReader == null)
        {
            // Determine the mimetype from the file extension only
            // (We've no content so we can't include that in our check)
            mimetype = mimetypeService.guessMimetype(name);
        }
        else
        {
            // Get the mime type and input stream from the content reader
            mimetype = contentReader.getMimetype();
            if (contentReader.getSize() != 0)
            {
                is = contentReader.getContentInputStream();
            }
        }
        
        // Hack to modify the mimetype of ods file so GDoc upload works
        if ("application/vnd.oasis.opendocument.spreadsheet".equals(mimetype) == true)
        {
        	mimetype = "application/x-vnd.oasis.opendocument.spreadsheet";        	            
        }
        
        // Check that we support the mimetype
        if (isSupportedMimetype(mimetype) == false)
        {
        	throw new GoolgeDocsUnsupportedMimetypeException(nodeRef, ContentModel.PROP_CONTENT, mimetype);
        }
        
        // Get the parent folder id
        DocumentListEntry parentFolder = getParentFolder(nodeRef);
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Creating google document (" + name + "," + mimetype + ")");
        }
        
        // Create the new google document
        DocumentListEntry document = createGoogleDocument(name, mimetype, parentFolder, is);
        
        // Set permissions
        setGoogleResourcePermissions(nodeRef, document, permissionContext);
        
        // Set the google document details
        setResourceDetails(nodeRef, document);
    }
    
    /**
     * @see org.alfresco.google.docs.GoogleDocsService#deleteGoogleResource(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void deleteGoogleResource(NodeRef nodeRef)
    {        
        // Check for mandatory parameters
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        // Initialise google doc services
        try
        {
        	initialise();
        }
        catch (GoogleDocsServiceInitException e)
        {
        	throw new AlfrescoRuntimeException("Unable to create google doc, because service could not be initialised.", e);
        }

        if (nodeService.hasAspect(nodeRef, ASPECT_GOOGLERESOURCE) == true)
        {
            // Get the entry
            DocumentListEntry entry = getDocumentListEntry(nodeRef);
            if (entry != null)
            {
                // Mark the resource for deletion upon completion of the transaction
                markResource(KEY_MARKED_DELETE, entry.getResourceId());
            }
            
            // Remove the aspect from the node
            nodeService.removeAspect(nodeRef, ASPECT_GOOGLERESOURCE);
        }        
    }
    
    /**
     * Set a google permission on a specified resource 
     * 
     * @param nodeRef				node reference
     * @param resource				document resource			
     * @param permissionContext		permission context
     */
    private void setGoogleResourcePermissions(NodeRef nodeRef, DocumentListEntry resource, GoogleDocsPermissionContext permissionContext)
    { 
        if (GoogleDocsPermissionContext.PRIVATE.equals(permissionContext) == false)
        {
            Set<AccessPermission> accessPermissions = permissionService.getAllSetPermissions(nodeRef);
            for (AccessPermission accessPermission : accessPermissions)
            {
                String authorityName = accessPermission.getAuthority();
                AuthorityType authorityType = accessPermission.getAuthorityType();
                String permission = accessPermission.getPermission();
                if (permissionMap.containsKey(permission) == true)
                {
                    String aclRole = permissionMap.get(permission);
                    if (GoogleDocsPermissionContext.SHARE_READ.equals(permissionContext) == true && 
                        ("reader".equals(aclRole) == true || "writer".equals(aclRole) == true))
                    {
                        // Set the permission to read
                        setGoogleResourcePermission(resource, authorityType, authorityName, "reader");
                    }
                    else if (GoogleDocsPermissionContext.SHARE_WRITE.equals(permissionContext) == true &&
                            "writer".equals(aclRole) == true)
                    {
                        // Set the permission to write
                        setGoogleResourcePermission(resource, authorityType, authorityName, "writer");
                    }
                    else if (GoogleDocsPermissionContext.SHARE_READWRITE.equals(permissionContext) == true && 
                            ("reader".equals(aclRole) == true || "writer".equals(aclRole) == true))
                    {
                        // Set the permission to the current acl
                        setGoogleResourcePermission(resource, authorityType, authorityName, aclRole);
                    }                    
                }
            }
        }
        
        // Always make sure the owner has write permissions on the document 
        String owner = ownableService.getOwner(nodeRef);
        setGoogleResourcePermission(resource, AuthorityType.USER, owner, "writer");
    }
    
    /**
     * Set a google permission on a specified resource
     * 
     * @param resource			document resource
     * @param authorityType		authority type
     * @param authorityName		authority name
     * @param role				role
     */
    private void setGoogleResourcePermission(DocumentListEntry resource, AuthorityType authorityType, String authorityName, String role)
    {
        if (AuthorityType.USER.equals(authorityType) == true)
        {
            // Set the user permissions on the resource
            String userEMail = getUserEMail(authorityName);
            if (userEMail != null && userEMail.length() != 0)
            {
                setGoogleResourcePermission(resource, userEMail, role);
            }
        }
        else if (AuthorityType.GROUP.equals(authorityType) == true)
        {
            Set<String> childAuthorities = authorityService.getContainedAuthorities(AuthorityType.USER, authorityName, false);
            for (String childAuthority : childAuthorities)
            {
                setGoogleResourcePermission(resource, AuthorityType.USER, childAuthority, role);
            }
        }
    }
    
    /**
     * Gets the users email used to identify their google account.
     * 
     * @param userName		user name
     * @return String		google account email, null if none
     */
    private String getUserEMail(String userName)
    {
        String email = null;
        NodeRef personNodeRef = personService.getPerson(userName);
        if (personNodeRef != null)
        {
        	// First see if the google user information has been set
        	email = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_GOOGLEUSERNAME);
        	
        	// If no google user information then default back to the user's email
        	if (email == null || email.length() == 0)
        	{
        		email = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL);
        	}
        }
        return email;
    }
    
    /**
     * Gets the nodes parent folder google resource.
     * 
     * @param nodeRef					node reference
     * @return DocumentList Entry		folder resource
     */
    private DocumentListEntry getParentFolder(final NodeRef nodeRef)
    {
        DocumentListEntry folder = null;
        
        NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (parentNodeRef != null)
        {
            if (nodeService.hasAspect(parentNodeRef, ASPECT_GOOGLERESOURCE) == true)
            {
                String resourceType = (String)nodeService.getProperty(parentNodeRef, PROP_RESOURCE_TYPE);
                String resourceId = (String)nodeService.getProperty(parentNodeRef, PROP_RESOURCE_ID);
                folder = getDocumentListEntry(resourceType + ":" + resourceId);
                
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Found existing google folder + " + resourceId);
                }
            }
            else
            {
            	// Get the parent folder
                DocumentListEntry parentFolder = getParentFolder(parentNodeRef);

                // Determine the name of the new google folder
                String name = null;
                QName parentNodeType = nodeService.getType(parentNodeRef);
                if (dictionaryService.isSubClass(parentNodeType, ContentModel.TYPE_STOREROOT) == true)
                {
                	name = parentNodeRef.getStoreRef().getIdentifier();
                }
                else
            	{
                	name = (String)nodeService.getProperty(parentNodeRef, ContentModel.PROP_NAME);
            	}
                
                // Create the folder and set the meta data in Alfresco
                folder = createGoogleFolder(name, parentFolder);               
                setResourceDetails(parentNodeRef, folder);                                  
            }
        }
        
        return folder;
    }
    
    /**
     * Sets the resource details on the node reference
     * 
     * @param nodeRef				node reference
     * @param documentListEntry		document list entry
     */
    private void setResourceDetails(final NodeRef nodeRef, final DocumentListEntry documentListEntry)
    {
        AuthenticationUtil.RunAsWork<Object> runAsWork = new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // Create a map of the property values
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(GoogleDocsModel.PROP_RESOURCE_ID, documentListEntry.getDocId());
                props.put(GoogleDocsModel.PROP_RESOURCE_TYPE, documentListEntry.getType());
                props.put(GoogleDocsModel.PROP_URL, documentListEntry.getDocumentLink().getHref());        
                
                // Add the google resource aspect
                nodeService.addAspect(nodeRef, GoogleDocsModel.ASPECT_GOOGLERESOURCE, props);                
                return null;
            }            
        };
        
        // Run as admin
        AuthenticationUtil.runAs(runAsWork, AuthenticationUtil.getAdminUserName());  
    }

    /**
     * @see org.alfresco.google.docs.GoogleDocsService#getGoogleDocContent(org.alfresco.service.cmr.repository.NodeRef)
     */
    public InputStream getGoogleDocContent(NodeRef nodeRef)
    {          
        InputStream result = null;
                
        // Check for mandatory parameters
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        // Initialise google doc services
        try
        {
        	initialise();
        }
        catch (GoogleDocsServiceInitException e)
        {
        	throw new AlfrescoRuntimeException("Unable to create google doc, because service could not be initialised.", e);
        }
        
        try
        {
            if (nodeService.hasAspect(nodeRef, ASPECT_GOOGLERESOURCE) == true)
            {
                String downloadUrl = null;
                DocumentListEntry document = getDocumentListEntry(nodeRef);
                if (document != null)
                {
                    String docType = document.getType();
                    
                    ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                    String fileExtension = mimetypeService.getExtension(contentData.getMimetype());
                    if (fileExtension.equals("docx"))
                    {
                        fileExtension = "doc";
                    }
                    else if (fileExtension.equals("xlsx"))
                    {
                    	fileExtension = "xls";
                    }
                
                    if (docType.equals(TYPE_DOCUMENT) || docType.equals(TYPE_PRESENTATION))
                    {
                        downloadUrl = this.downloadUrl + "/" + docType + "s/Export?docId=" + document.getDocId() + 
                                                                           "&exportFormat=" + fileExtension;
                    }
                    else if (docType.equals(TYPE_SPREADSHEET))
                    {
                        downloadUrl = ((MediaContent)document.getContent()).getUri() + "&exportFormat=" + fileExtension;
    
                        // If exporting to .csv or .tsv, add the gid parameter to specify which sheet to export
                        if (fileExtension.equals("csv") || fileExtension.equals("tsv")) 
                        {
                            downloadUrl += "&gid=0";  // gid=0 will download only the first sheet
                        }
                    }
                    else if (docType.equals(TYPE_PDF))
                    {            
                        MediaContent mc = (MediaContent)document.getContent();
                        downloadUrl = mc.getUri();
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException("Unsuported document type: " + docType);
                    }
            
                    // Log the download URI
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Download URL for " + docType + " is " + downloadUrl);
                    }
                    
                    // TODO need to verify that download of a spreadsheet works before we delete this historical code ...
                    
                    UserToken docsToken = null;
                    if (docType.equals(TYPE_SPREADSHEET) == true)
                    {
                        docsToken = (UserToken) googleDocumentService.getAuthTokenFactory().getAuthToken();
                        UserToken spreadsheetsToken = (UserToken) spreadsheetsService.getAuthTokenFactory().getAuthToken();
                        googleDocumentService.setUserToken(spreadsheetsToken.getValue());
            
                    }
            
                    MediaContent mc = new MediaContent();
                    mc.setUri(downloadUrl);            
                    MediaSource ms = googleDocumentService.getMedia(mc);
            
                    if (docType.equals(TYPE_SPREADSHEET) == true)
                    {
                        googleDocumentService.setUserToken(docsToken.getValue());
                    }
            
                    result = ms.getInputStream(); 
                }
                else
                {
                    throw new AlfrescoRuntimeException("Can not download google doc content since no corresponsing google resource could be found");
                }
            }
            else
            {
                // error since we are trying to download a non-google resource
                throw new AlfrescoRuntimeException("Can not download google doc content since no corresponsing google resource could be found");
            }
        }
        catch (ServiceException e)
        {
            throw new AlfrescoRuntimeException("Unable to get google document stream.", e);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to get google document stream.", e);   
        }
        
        return result;
    }
    
    /**
     * Gets the document list entry that corresponds to the google resource
     * related to the node reference provided.
     * 
     * @param docNodeRef			node reference
     * @return DocumentListEntry	document list entry	
     */
    private DocumentListEntry getDocumentListEntry(NodeRef docNodeRef)
    {
        DocumentListEntry result = null;
        String docType = (String)nodeService.getProperty(docNodeRef, PROP_RESOURCE_TYPE);
        String docId = (String)nodeService.getProperty(docNodeRef, PROP_RESOURCE_ID);        
        if (docType != null && docId != null)
        {
            result = getDocumentListEntry(docType + ":" + docId);
        }
        return result;
    }
    
    /**
     * Gets the document resource entry for a document resource id
     * 
     * @param docResourceId			document resource id
     * @return DocumentListEntry	document list entry
     */
    private DocumentListEntry getDocumentListEntry(String docResourceId)
    {
        return getEntry(docResourceId, DocumentListEntry.class);
    }
    
    /**
     * Gets the entry for a given resource id.
     * 
     * @param <E>			Entry class
     * @param resourceId	resource id
     * @param entryClass	entry class
     * @return E			entry instance
     */
    private <E extends IEntry> E getEntry(String resourceId, Class<E> entryClass)
    {
        E result = null;
        try
        {
            URL docEntryURL = new URL(url + "/" + resourceId);
            result = googleDocumentService.getEntry(docEntryURL, entryClass);
        }
        catch (ServiceException e)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Unable to get document list entry for resource " + resourceId + " because " + e.getMessage());
            }
            result = null;
        }
        catch (IOException e)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Unable to get document list entry for resource " + resourceId + " because " + e.getMessage());
            }
            result = null;            
        }
        return result;
    }
    
    /**
     * Create a google document
     * 
     * @param name					document name 
     * @param mimetype				mime type
     * @param parentFolder      	parent folder resource
     * @param is					input stream for content
     * @return DocumentListEntry	resource for created document
     */
    private DocumentListEntry createGoogleDocument(String name, String mimetype, DocumentListEntry parentFolder, InputStream is)
    {
        DocumentListEntry document = null;
        
        // Log details 
        if (logger.isDebugEnabled() == true)
        {
        	logger.debug("Creating google document with name " + name);
        }
        
        try
        { 
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Creating media content object for mimetype " + mimetype);
            }
            
            // Create the media content object
            MediaContent mediaContent = new MediaContent();            
            mediaContent.setMimeType(new ContentType(mimetype));
            
            if (is != null)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug(" ... input stream has been set");
                }
                mediaContent.setMediaSource(new MediaStreamSource(is, mimetype));
            }
            
            // Parent folder url
            String parentFolderUrl = url;
            if (parentFolder != null)
            {
                parentFolderUrl = ((MediaContent)parentFolder.getContent()).getUri();
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug(" ... parent folder URL is " + parentFolderUrl);
                }
            }
            
            // Create the document entry object
            DocumentListEntry docEntry = null;
            if (MediaType.XLS.getMimeType().equals(mimetype) == true ||
                MediaType.XLSX.getMimeType().equals(mimetype) == true ||
                MediaType.ODS.getMimeType().equals(mimetype) == true)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Creating SpreadsheetEntry for mimetype " + mimetype);
                }
                docEntry = new SpreadsheetEntry();
            }
            else if (MediaType.PPS.getMimeType().equals(mimetype) == true ||
                     MediaType.PPT.getMimeType().equals(mimetype) == true)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Creating PresentationEntry for mimetype " + mimetype);
                }
                docEntry = new PresentationEntry();
            }
            else if (MediaType.PDF.getMimeType().equals(mimetype) == true)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Creating PdfEntry for mimetype " + mimetype);
                }
                docEntry = new PdfEntry();
            }
            else
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Creating DocumentEntry for mimetype " + mimetype);
                }
                docEntry = new DocumentEntry();
            }
            
            // Set the content and the title of the document
            docEntry.setContent(mediaContent);
            docEntry.setTitle(new PlainTextConstruct(name));  
            
            // Upload the document into the parent folder
            document = googleDocumentService.insert(
                        new URL(parentFolderUrl), 
                        docEntry);

            // Mark create entry
            markResource(KEY_MARKED_CREATE, document.getResourceId());
        }
        catch (IOException e)
        {
        	// Log details of exception
            if (logger.isDebugEnabled() == true)
            {
            	logger.debug("Unable to create google document with name " + name + ", because " + e.getMessage());
            }
            
            // Rethrow as runtime exception
            throw new AlfrescoRuntimeException("Unable to create google document", e);
        }
        catch (ServiceException e)
        {
        	// Log details of exception
            if (logger.isDebugEnabled() == true)
            {
            	logger.debug("Unable to create google document with name " + name + ", because " + e.getMessage());
            }
            
            // Rethrow as runtime exception
            throw new AlfrescoRuntimeException("Unable to create google document", e);
        }
        
        return document;
    }
    
    /**
     * Updates the content of a google document
     * 
     * @param document		document resource
     * @param mimeType		mimetype
     * @param is			input stream
     */
    @SuppressWarnings("unused")
    private void updateGoogleDocContent(DocumentListEntry document, String mimeType, InputStream is)
    {     
    	// Log details
    	if (logger.isDebugEnabled() == true)
    	{
    		logger.debug("Updating content of document " + document.getResourceId());
    	}
    	
        try
        {
            // Update the existing content
            googleDocumentService.getRequestFactory().setHeader("If-Match", "*");
            document.setMediaSource(new MediaStreamSource(is, mimeType));
            document.updateMedia(false);                
            googleDocumentService.getRequestFactory().setHeader("If-Match", null);
        }
        catch (ServiceException e)
        {
        	// Log details of the error
        	if (logger.isDebugEnabled() == true)
        	{
        		logger.debug("Unable to update the content of document " + document.getResourceId() + ", because " + e.getMessage());
        	}
        	
        	// Rethrow as runtime exception
            throw new AlfrescoRuntimeException("Unable to update documents content in google docs", e);
        }
        catch (IOException e)
        {
        	// Log details of the error
        	if (logger.isDebugEnabled() == true)
        	{
        		logger.debug("Unable to update the content of document " + document.getResourceId() + ", because " + e.getMessage());
        	}
        	
        	// Rethrow as runtime exception
            throw new AlfrescoRuntimeException("Unable to update documents content in google docs", e);
        }
    }
    
    /**
     * Creates a google folder, returning the folder resource. 
     * 
     * @param folderName			folder name
     * @param parentFolder  		parent folder resource
     * @return DocumentListEntry	created folder resource
     */
    private DocumentListEntry createGoogleFolder(String folderName, DocumentListEntry parentFolder)
    {
        DocumentListEntry folderEntry = null;
        
        try
        {
        	// Log details
        	if (logger.isDebugEnabled() == true)
        	{
        		logger.debug("Creating folder " + folderName);
        	}
        	
            // Parent folder url
            String parentFolderUrl = url;
            if (parentFolder != null)
            {
                parentFolderUrl = ((MediaContent)parentFolder.getContent()).getUri();
            }
            
            // Create the folder entry
            FolderEntry folder = new FolderEntry();
            folder.setTitle(new PlainTextConstruct(folderName));           
            
            // Create the folder
            folderEntry = googleDocumentService.insert(
                    new URL(parentFolderUrl), 
                    folder);
            
            // Mark create entry
            markResource(KEY_MARKED_CREATE, folderEntry.getResourceId());
        }
        catch (IOException e)
        {
        	// Log details of the failure
        	if (logger.isDebugEnabled() == true)
        	{
        		logger.debug("Unable to create folder " + folderName + ", because " + e.getMessage());
        	}
        	
        	// Rethrow as runtime exception
            throw new AlfrescoRuntimeException("Unable to create Google Folder", e);
        }
        catch (ServiceException e)
        {
        	// Log details of the failure
        	if (logger.isDebugEnabled() == true)
        	{
        		logger.debug("Unable to create folder " + folderName + ", because " + e.getMessage());
        	}
        	
        	// Rethrow as runtime exception
            throw new AlfrescoRuntimeException("Unable to create Google Folder", e);
        }

        return folderEntry;
    }
    
    /**
     * Set permissions on a googleDoc resource
     * 
     * @param resourceId
     * @param email
     * @param role
     */
    private void setGoogleResourcePermission(DocumentListEntry resource, String email, String role)
    {
    	// Check mandatory parameters have been set
        ParameterCheck.mandatory("resource", resource);
        ParameterCheck.mandatory("email", email);
        ParameterCheck.mandatory("role", role);
        
        // Log details
    	if (logger.isDebugEnabled() == true)
    	{
    		logger.debug("Setting the role " + role + " on the google resource " + resource.getResourceId() + " for email " + email + ".");
    	}

        try
        {   
            AclRole aclRole = new AclRole(role);
            AclScope scope = new AclScope(AclScope.Type.USER, email);
            
            // Get the URL
            URL aclFeedLinkURL = new URL(resource.getAclFeedLink().getHref());
            
            // See if we have already set this permission or not
            AclEntry aclEntry = null;
            AclFeed aclFeed = googleDocumentService.getFeed(aclFeedLinkURL, AclFeed.class);
            if (aclFeed != null)
            {
                List<AclEntry> aclEntries = aclFeed.getEntries();
                for (AclEntry tempAclEntry : aclEntries)
                {
                    AclScope tempScope = tempAclEntry.getScope();
                    if (tempScope.equals(scope) == true)
                    {
                        // Existing ACL entry found
                        aclEntry = tempAclEntry;
                        break;
                    }
                }
            }
            
            // Set the permission details
            if (aclEntry == null)
            {
                aclEntry = new AclEntry();
                aclEntry.setRole(aclRole);
                aclEntry.setScope(scope);
                googleDocumentService.insert(aclFeedLinkURL, aclEntry);
            }
            else
            {
            	// Log details of failure
            	if (logger.isDebugEnabled() == true)
            	{
            		logger.debug("Unable to the role " + role + " on the google resource " + resource.getResourceId() + " for email " + email + "." +
            				     "  This user already has a role on this document.");
            	}
            }
            
            // TODO for now we will not 'update' the permissions if they have already been set ....
            //
            //else
            //{
            //    AclRole currentAclRole = aclEntry.getRole(); 
            //    if (currentAclRole.toString().equals(aclRole.toString()) == false)
            //    {
            //        aclEntry.setRole(aclRole);
            //        googleDocumentService.update(new URL(aclEntry.getEditLink().getHref()), aclEntry);                   
            //    }
            //}    
        }
        catch (ServiceException e)
        {
            // Ignore this exception since we don't want to roll back the entire transaction because
        	// a single users permissions can not be set.
        	// It seems the google API will return a server exception if the email does not correspond to 
        	// a google account, so catching this exception in this indiscriminate way is the best thing to 
        	// do for now.
        	
        	// Log details of failure
        	if (logger.isDebugEnabled() == true)
        	{
        		logger.debug("Unable to the role " + role + " on the google resource " + resource.getResourceId() + " for email " + email + "." +
        				     "  Check that this is a valid google account.");
        	}
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to set premissions on google document", e);  
        }
    }    
    
    /**
     * Marks a resource as created in this transaction
     * 
     * @param resourceId    resource id of created resource
     */
    @SuppressWarnings("unchecked")
    private void markResource(String key, String resourceId)
    {
        List<String> resources = (List<String>)AlfrescoTransactionSupport.getResource(key);
        if (resources == null)
        {
            // bind pending rules to the current transaction
            resources = new ArrayList<String>();
            AlfrescoTransactionSupport.bindResource(key, resources);
            // bind the rule transaction listener
            AlfrescoTransactionSupport.bindListener(this);
        }
        
        if (resources.contains(resourceId) == false)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Marking resource " + resourceId + " with key " + key);
            }
            
            resources.add(resourceId);
        }
    }
    
    /**
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterCommit()
    {
        List<String> resources = (List<String>)AlfrescoTransactionSupport.getResource(KEY_MARKED_DELETE);
        if (resources != null)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Transaction commited, deleting Google resources");
            }
            
            for (String resourceId : resources)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Deleting resource " + resourceId);
                }
                
                // Delete resource
                try
                {
                    DocumentListEntry entry = getDocumentListEntry(resourceId);  
                    if (entry != null)
                    {
                        googleDocumentService.delete(new URL(entry.getEditLink().getHref() + "?delete=true"), entry.getEtag());
                    }
                    else
                    {
                        if (logger.isDebugEnabled() == true)
                        {
                            logger.debug("Unable to delete resource " + resourceId + " during commit.");
                        }
                    }
                } 
                catch (Throwable e)
                {
                    // Ignore, but log
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Unable to delete resource " + resourceId + " during commit.", e);
                    }
                } 
            }
        }
    }
    
    /**
     * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterRollback()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void afterRollback()
    {   
        List<String> resources = (List<String>)AlfrescoTransactionSupport.getResource(KEY_MARKED_CREATE);
        if (resources != null)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Transaction rolled back, manually deleting created Google Resources");
            }
            
            for (String resourceId : resources)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Deleting created resource " + resourceId);
                }
                
                // Delete resource
                try
                {
                    DocumentListEntry entry = getDocumentListEntry(resourceId);   
                    if (entry != null)
                    {
                        googleDocumentService.delete(new URL(entry.getEditLink().getHref() + "?delete=true"), entry.getEtag());
                    }
                    else
                    {
                        if (logger.isDebugEnabled() == true)
                        {
                            logger.debug("Unable to delete resource " + resourceId + " during rollback.");
                        }                        
                    }
                } 
                catch (Throwable e)
                {
                    // Ignore, but log
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Unable to delete resource " + resourceId + " during rollback.", e);
                    }
                } 
            }
        }
    }    
}
