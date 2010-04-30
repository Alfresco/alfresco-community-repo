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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import com.google.gdata.data.docs.PresentationEntry;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

/**
 * Google docs integration service implementation
 */
public class GoogleDocsServiceImpl implements GoogleDocsService, GoogleDocsModel
{    
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(GoogleDocsServiceImpl.class);

    /** Google document types */
    public static final String TYPE_DOCUMENT = "document";
    public static final String TYPE_SPREADSHEET = "spreadsheet";
    public static final String TYPE_PRESENTATION = "presentation";
    public static final String TYPE_PDF = "pdf";

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

    /** GoogleDoc base feed url */
    private String url = "http://docs.google.com/feeds/default/private/full";
    
    /** Authentication credentials */
    private boolean initialised = false;
    private String username;
    private String password;
    
    /** Permission map */
    private Map<String, String> permissionMap;

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
     * @param url  root googleDoc URL
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @param username  google service user name
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * @param password  google service password
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    /**
     * @param permissionMap permission map
     */
    public void setPermissionMap(Map<String, String> permissionMap)
    {
        this.permissionMap = permissionMap;
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
                throw new GoogleDocsServiceInitException("No Goolge Docs credentials found. Please set the Google Docs authentication configuration.");
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
            // Determine the mimetype from the file extension
            mimetype = mimetypeService.guessMimetype(name);
        }
        else
        {
            // Get the mime type and input stream from the content reader
            mimetype = contentReader.getMimetype();
            is = contentReader.getContentInputStream();
        }
        
        // Get the parent folder id
        DocumentListEntry parentFolder = getParentFolder(nodeRef);
        
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
        
        try
        {
            if (nodeService.hasAspect(nodeRef, ASPECT_GOOGLERESOURCE) == true)
            {
                // Get the entry
                DocumentListEntry entry = getDocumentListEntry(nodeRef);
                if (entry == null)
                {
                    throw new AlfrescoRuntimeException("Unable to find google resource to delete for node " + nodeRef.toString());
                }
                
                // Perminantly delete the entry
                googleDocumentService.delete(new URL(entry.getEditLink().getHref() + "?delete=true"), entry.getEtag());
                
                // Remove the aspect from the node
                nodeService.removeAspect(nodeRef, ASPECT_GOOGLERESOURCE);
            }
        }
        catch (ServiceException e)
        {
            throw new AlfrescoRuntimeException("Unable to delete google resource for the node "+ nodeRef.toString());
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to delete google resource for the node "+ nodeRef.toString());
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
        // Set the owner of the document
        String owner = ownableService.getOwner(nodeRef);
        setGoogleResourcePermission(resource, AuthorityType.USER, owner, "owner");
        
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
    private DocumentListEntry getParentFolder(NodeRef nodeRef)
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
            }
            else
            {
                DocumentListEntry parentFolder = getParentFolder(parentNodeRef);
                String name = (String)nodeService.getProperty(parentNodeRef, ContentModel.PROP_NAME);
                folder = createGoogleFolder(name, parentFolder);
               
                setResourceDetails(parentNodeRef, folder);
            }
        }
        
        return folder;
    }
    
    /**
     * 
     * @param nodeRef
     * @param folderId
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
                String docType = document.getType();
                
                ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                String fileExtension = mimetypeService.getExtension(contentData.getMimetype());
                if (fileExtension.equals("docx"))
                {
                    fileExtension = "doc";
                }
            
                if (docType.equals(TYPE_DOCUMENT) || docType.equals(TYPE_PRESENTATION))
                {
                    downloadUrl = ((MediaContent)document.getContent()).getUri() + "&exportFormat=" + fileExtension;
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
        
                // TODO need to verify that download of a spreadsheet works before we delete this historical code ...
                
//                UserToken docsToken = null;
//                if (isSpreadSheet)
//                {
//                    docsToken = (UserToken) googleDocumentService.getAuthTokenFactory().getAuthToken();
//                    UserToken spreadsheetsToken = (UserToken) spreadsheetsService.getAuthTokenFactory().getAuthToken();
//                    googleDocumentService.setUserToken(spreadsheetsToken.getValue());
//        
//                }
        
                MediaContent mc = new MediaContent();
                mc.setUri(downloadUrl);            
                MediaSource ms = googleDocumentService.getMedia(mc);
        
             //   if (isSpreadSheet)
             //   {
             //       googleDocumentService.setUserToken(docsToken.getValue());
             //   }
        
                result = ms.getInputStream();                
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
     * 
     * @param docNodeRef
     * @return
     */
    private DocumentListEntry getDocumentListEntry(NodeRef docNodeRef)
    {
        String docType = (String)nodeService.getProperty(docNodeRef, PROP_RESOURCE_TYPE);
        String docId = (String)nodeService.getProperty(docNodeRef, PROP_RESOURCE_ID);        
        return getDocumentListEntry(docType + ":" + docId);
    }
    
    /**
     * 
     * @param docResourceId
     * @return
     */
    private DocumentListEntry getDocumentListEntry(String docResourceId)
    {
        return getEntry(docResourceId, DocumentListEntry.class);
    }
    
    /**
     * 
     * @param <E>
     * @param resourceId
     * @param entryClass
     * @return
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
            throw new AlfrescoRuntimeException("Unable to get document list entry for resource " + resourceId, e);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to get document list entry for resource " + resourceId, e);            
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
        
        try
        { 
            // Create the media content object
            MediaContent mediaContent = new MediaContent();            
            mediaContent.setMimeType(new ContentType(mimetype));
            
            if (is != null)
            {
                mediaContent.setMediaSource(new MediaStreamSource(is, mimetype));
            }
            
            // Parent folder url
            String parentFolderUrl = url;
            if (parentFolder != null)
            {
                parentFolderUrl = ((MediaContent)parentFolder.getContent()).getUri();
            }
            
            // Create the document entry object
            DocumentListEntry docEntry = null;
            if (MimetypeMap.MIMETYPE_EXCEL.equals(mimetype) == true)
            {
                docEntry = new PresentationEntry();
            }
            else
            {
                docEntry = new DocumentEntry();
            }
            
            docEntry.setContent(mediaContent);
            docEntry.setTitle(new PlainTextConstruct(name));  
            
            // Upload the document into the parent folder
            document = googleDocumentService.insert(
                        new URL(parentFolderUrl), 
                        docEntry);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to create google document", e);
        }
        catch (ServiceException e)
        {
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
    private void updateGoogleDocContent(DocumentListEntry document, String mimeType, InputStream is)
    {        
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
            throw new AlfrescoRuntimeException("Unable to update documents content in google docs", e);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to update documents content in google docs", e);
        }
    }
    
    /**
     * 
     * @param folderName
     * @param parentFolderId
     * @return
     */
    private DocumentListEntry createGoogleFolder(String folderName, DocumentListEntry parentFolder)
    {
        DocumentListEntry folderEntry = null;
        
        try
        {
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
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to create Google Folder", e);
        }
        catch (ServiceException e)
        {
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
        ParameterCheck.mandatory("resource", resource);
        ParameterCheck.mandatory("email", email);
        ParameterCheck.mandatory("role", role);
        
        // Log details of failure
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
    
    
}
