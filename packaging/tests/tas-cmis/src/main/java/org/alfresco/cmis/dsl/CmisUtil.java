package org.alfresco.cmis.dsl;

import org.alfresco.cmis.CmisWrapper;
import org.alfresco.cmis.exception.InvalidCmisObjectException;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.exception.IORuntimeException;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.UserModel;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.testng.collections.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.alfresco.utility.report.log.Step.STEP;

/**
 * DSL utility for managing CMIS objects
 */
public class CmisUtil
{
    private CmisWrapper cmisAPI;
    private Logger LOG = LogFactory.getLogger();

    public CmisUtil(CmisWrapper cmisAPI)
    {
        this.cmisAPI = cmisAPI;
    }

    /**
     * Get cmis object by object id
     *
     * @param objectId cmis object id
     * @return CmisObject cmis object
     */
    public CmisObject getCmisObjectById(String objectId)
    {
        LOG.debug("Get CMIS object by ID {}", objectId);
        if (cmisAPI.getSession() == null)
        {
            throw new CmisRuntimeException("Please authenticate user, call: cmisAPI.authenticate(..)!");
        }
        if (objectId == null)
        {
            throw new InvalidCmisObjectException("Invalid content id");
        }
        return cmisAPI.getSession().getObject(objectId);
    }

    /**
     * Get cmis object by object id with OperationContext
     *
     * @param objectId cmis object id
     * @param context OperationContext
     * @return CmisObject cmis object
     */
    public CmisObject getCmisObjectById(String objectId, OperationContext context)
    {
        if (cmisAPI.getSession() == null)
        {
            throw new CmisRuntimeException("Please authenticate user, call: cmisAPI.authenticate(..)!");
        }
        if (objectId == null)
        {
            throw new InvalidCmisObjectException("Invalid content id");
        }
        return cmisAPI.getSession().getObject(objectId, context);
    }

    /**
     * Get cmis object by path
     *
     * @param pathToItem String path to item
     * @return CmisObject cmis object
     */
    public CmisObject getCmisObject(String pathToItem)
    {
        if (cmisAPI.getSession() == null)
        {
            throw new CmisRuntimeException("Please authenticate user, call: cmisAPI.authenticate(..)!");
        }
        if (pathToItem == null)
        {
            throw new InvalidCmisObjectException("Invalid path set for content");
        }
        CmisObject cmisObject = cmisAPI.getSession().getObjectByPath(Utility.removeLastSlash(pathToItem));
        if (cmisObject instanceof Document)
        {
            if (!((Document) cmisObject).getVersionLabel().contentEquals("pwc"))
            {
                // get last version of document
                cmisObject = ((Document) cmisObject).getObjectOfLatestVersion(false);
            }
            else
            {
                // get pwc document
                cmisObject = cmisAPI.getSession().getObject(((Document) cmisObject).getObjectOfLatestVersion(false).getVersionSeriesCheckedOutId());
            }
        }
        return cmisObject;
    }

    /**
     * Get cmis object by path with context
     *
     * @param pathToItem String path to item
     * @param context OperationContext
     * @return CmisObject cmis object
     */
    public CmisObject getCmisObject(String pathToItem, OperationContext context)
    {
        if (cmisAPI.getSession() == null)
        {
            throw new CmisRuntimeException("Please authenticate user!");
        }
        if (pathToItem == null)
        {
            throw new InvalidCmisObjectException("Invalid path set for content");
        }
        CmisObject cmisObject = cmisAPI.getSession().getObjectByPath(Utility.removeLastSlash(pathToItem), context);
        if (cmisObject instanceof Document)
        {
            if (!((Document) cmisObject).getVersionLabel().contentEquals("pwc"))
            {
                // get last version of document
                cmisObject = ((Document) cmisObject).getObjectOfLatestVersion(false, context);
            }
            else
            {
                // get pwc document
                cmisObject = cmisAPI.getSession().getObject(((Document) cmisObject).getObjectOfLatestVersion(false, context).getVersionSeriesCheckedOutId());
            }
        }
        return cmisObject;
    }

    /**
     * Get Document object for a file
     *
     * @param path String path to document
     * @return {@link Document} object
     */
    public Document getCmisDocument(final String path)
    {
        LOG.debug("Get CMIS Document by path {}", path);
        Document d = null;
        CmisObject docObj = getCmisObject(path);
        if (docObj instanceof Document)
        {
            d = (Document) docObj;
        }
        else if (docObj instanceof Folder)
        {
            throw new InvalidCmisObjectException("Content at " + path + " is not a file");
        }
        return d;
    }

    /**
     * Get Folder object for a folder
     *
     * @param path String path to folder
     * @return {@link Folder} object
     */
    public Folder getCmisFolder(final String path)
    {
        Folder f = null;
        CmisObject folderObj = getCmisObject(path);
        if (folderObj instanceof Folder)
        {
            f = (Folder) folderObj;
        }
        else if (folderObj instanceof Document)
        {
            throw new InvalidCmisObjectException("Content at " + path + " is not a folder");
        }
        return f;
    }

    /**
     * Helper method to get the contents of a stream
     *
     * @param stream
     * @return
     * @throws IORuntimeException
     */
    protected String getContentAsString(ContentStream stream)
    {
        LOG.debug("Get Content as String {}", stream);
        InputStream inputStream = stream.getStream();
        String result;
        try
        {
            result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new IORuntimeException(e);
        }
        IOUtils.closeQuietly(inputStream);
        return result;
    }

    /**
     * Copy all the children of the source folder to the target folder
     *
     * @param sourceFolder
     * @param targetFolder
     */
    protected void copyChildrenFromFolder(Folder sourceFolder, Folder targetFolder)
    {
        for (Tree<FileableCmisObject> t : sourceFolder.getDescendants(-1))
        {
            CmisObject obj = t.getItem();
            if (obj instanceof Document)
            {
                Document d = (Document) obj;
                d.copy(targetFolder);
            }
            else if (obj instanceof Folder)
            {
                copyFolder((Folder) obj, targetFolder);
            }
        }
    }

    /**
     * Copy folder with all children
     *
     * @param sourceFolder source folder
     * @param targetFolder target folder
     * @return CmisObject of new created folder
     */
    public CmisObject copyFolder(Folder sourceFolder, Folder targetFolder)
    {
        Map<String, Object> folderProperties = new HashMap<String, Object>(2);
        folderProperties.put(PropertyIds.NAME, sourceFolder.getName());
        folderProperties.put(PropertyIds.OBJECT_TYPE_ID, sourceFolder.getBaseTypeId().value());
        Folder newFolder = targetFolder.createFolder(folderProperties);
        copyChildrenFromFolder(sourceFolder, newFolder);
        return newFolder;
    }

    protected boolean isPrivateWorkingCopy()
    {
        boolean result;
        try
        {
            result = getPWCDocument().isPrivateWorkingCopy();
        }
        catch (CmisVersioningException cmisVersioningException)
        {
            result = false;
        }
        return result;
    }

    /**
     * Returns the PWC (private working copy) ID of the document version series
     */
    public Document getPWCDocument()
    {
        Document document = getCmisDocument(cmisAPI.getLastResource());
        String pwcId = document.getVersionSeriesCheckedOutId();
        if (pwcId != null)
        {
            return (Document) cmisAPI.getSession().getObject(pwcId);
        }
        else
        {
            throw new CmisVersioningException(String.format("Document %s is not checked out", document.getName()));
        }
    }

    public FileModel getPWCFileModel()
    {
        Document document = getPWCDocument();
        String[] pathTokens = cmisAPI.getLastResource().split("/");
        String path = "";
        for (int i = 0; i < pathTokens.length - 1; i++)
            path = Utility.buildPath(path, pathTokens[i]);
        path = Utility.buildPath(path, document.getName());

        FileModel fileModel = new FileModel();
        fileModel.setName(document.getName());
        fileModel.setCmisLocation(path);
        return fileModel;
    }

    protected Folder getFolderParent()
    {
        return getCmisFolder(cmisAPI.getLastResource()).getFolderParent();
    }

    /**
     * @return List<Action> of allowable actions for the current object
     */
    protected List<Action> getAllowableActions()
    {
        return Lists.newArrayList(getCmisObject(cmisAPI.getLastResource()).getAllowableActions().getAllowableActions());
    }

    /**
     * Returns the requested property. If the property is not available, null is returned
     * 
     * @param propertyId
     * @return CMIS Property
     */
    protected <T> Property<T> getProperty(String propertyId)
    {
        CmisObject cmisObject = getCmisObject(cmisAPI.getLastResource());
        return cmisObject.getProperty(propertyId);
    }

    protected List<Rendition> getRenditions()
    {
        OperationContext operationContext = cmisAPI.getSession().createOperationContext();
        operationContext.setRenditionFilterString("*");
        CmisObject obj = cmisAPI.getSession().getObjectByPath(cmisAPI.getLastResource(), operationContext);
        obj.refresh();
        List<Rendition> renditions = obj.getRenditions();
        int retry = 0;
        while ((renditions == null || renditions.isEmpty()) && retry < Utility.retryCountSeconds)
        {
            Utility.waitToLoopTime(1);
            obj.refresh();
            renditions = obj.getRenditions();
            retry++;
        }
        return obj.getRenditions();
    }

    protected List<SecondaryType> getSecondaryTypes()
    {
        CmisObject obj = getCmisObject(cmisAPI.getLastResource());
        obj.refresh();
        return obj.getSecondaryTypes();
    }

    /**
     * Get the children from a parent folder
     *
     * @return Map<ContentModel, ObjectType>
     */
    public Map<ContentModel, ObjectType> getChildren()
    {
        String folderParent = cmisAPI.getLastResource();
        ItemIterable<CmisObject> children = cmisAPI.withCMISUtil().getCmisFolder(folderParent).getChildren();
        Map<ContentModel, ObjectType> contents = new HashMap<ContentModel, ObjectType>();
        for (CmisObject o : children)
        {
            ContentModel content = new ContentModel(o.getName());
            content.setNodeRef(o.getId());
            content.setDescription(o.getDescription());
            content.setCmisLocation(Utility.buildPath(folderParent, o.getName()));
            contents.put(content, o.getType());
        }
        return contents;
    }

    /**
     * Gets the folder descendants starting with the current folder
     *
     * @param depth level of the tree that you want to go to
     *            - currentFolder
     *            -- file1.txt
     *            -- file2.txt
     *            -- folderB
     *            --- file3.txt
     *            --- file4.txt
     *            e.g. A depth of 1 will give you just the current folder descendants (file1.txt, file2.txt, folder1)
     *            e.g. A depth of -1 will return all the descendants (file1.txt, file2.txt, folder1, file3.txt and file4.txt)
     */
    public List<CmisObject> getFolderDescendants(int depth)
    {
        return getFolderTreeCmisObjects(getCmisFolder(cmisAPI.getLastResource()).getDescendants(depth));
    }

    /**
     * Returns a list of Cmis objects for the provided Content Models
     *
     * @param contentModels
     */
    public List<CmisObject> getCmisObjectsFromContentModels(ContentModel... contentModels)
    {
        List<CmisObject> cmisObjects = new ArrayList<>();
        for (ContentModel contentModel : contentModels)
            cmisObjects.add(getCmisObject(contentModel.getCmisLocation()));
        return cmisObjects;
    }

    public ContentStream getContentStream(String content)
    {
        String fileName = getCmisDocument(cmisAPI.getLastResource()).getName();

        return cmisAPI.getDataContentService().getContentStream(fileName, content);
    }

    public Acl getAcls()
    {
        OperationContext context = cmisAPI.getSession().createOperationContext();
        context.setIncludeAcls(true);
        return getCmisObject(cmisAPI.getLastResource(), context).getAcl();
    }

    /**
     * Gets only the folder descendants for the {@link #getLastResource()} folder
     *
     * @param depth level of the tree that you want to go to
     *            - currentFolder
     *            -- folderB
     *            -- folderC
     *            --- folderD
     *            e.g. A depth of 1 will give you just the current folder descendants (folderB, folderC)
     *            e.g. A depth of -1 will return all the descendants (folderB, folderC, folderD)
     */
    public List<CmisObject> getFolderTree(int depth)
    {
        return getFolderTreeCmisObjects(getCmisFolder(cmisAPI.getLastResource()).getFolderTree(depth));
    }

    /**
     * Helper method for getFolderTree and getFolderDescendants that cycles threw the folder descendants and returns List<CmisObject>
     */
    private List<CmisObject> getFolderTreeCmisObjects(List<Tree<FileableCmisObject>> descendants)
    {
        List<CmisObject> cmisObjectList = new ArrayList<>();
        for (Tree<FileableCmisObject> descendant : descendants)
        {
            cmisObjectList.add(descendant.getItem());
            cmisObjectList.addAll(descendant.getChildren().stream().map(Tree::getItem).collect(Collectors.toList()));
        }
        return cmisObjectList;
    }

    protected List<Document> getAllDocumentVersions()
    {
        return getCmisDocument(cmisAPI.getLastResource()).getAllVersions();
    }

    public List<Document> getAllDocumentVersionsBy(OperationContext context)
    {
        return getCmisDocument(cmisAPI.getLastResource()).getAllVersions(context);
    }

    public List<Document> getCheckedOutDocumentsFromSession()
    {
        return com.google.common.collect.Lists.newArrayList(cmisAPI.getSession().getCheckedOutDocs());
    }

    public List<Document> getCheckedOutDocumentsFromSession(OperationContext context)
    {
        return com.google.common.collect.Lists.newArrayList(cmisAPI.getSession().getCheckedOutDocs(context));
    }

    public List<Document> getCheckedOutDocumentsFromFolder()
    {
        Folder folder = cmisAPI.withCMISUtil().getCmisFolder(cmisAPI.getLastResource());
        return com.google.common.collect.Lists.newArrayList(folder.getCheckedOutDocs());
    }

    public List<Document> getCheckedOutDocumentsFromFolder(OperationContext context)
    {
        Folder folder = cmisAPI.withCMISUtil().getCmisFolder(cmisAPI.getLastResource());
        return com.google.common.collect.Lists.newArrayList(folder.getCheckedOutDocs(context));
    }

    protected boolean isCmisObjectContainedInCmisCheckedOutDocumentsList(CmisObject cmisObject, List<Document> cmisCheckedOutDocuments)
    {
        for (Document cmisCheckedOutDocument : cmisCheckedOutDocuments)
            if (cmisObject.getId().split(";")[0].equals(cmisCheckedOutDocument.getId().split(";")[0]))
                return true;
        return false;
    }

    public Map<String, Object> getProperties(ContentModel contentModel, String baseTypeId)
    {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, baseTypeId);
        properties.put(PropertyIds.NAME, contentModel.getName());

        // WebServices binding does not have SecondaryTypes and cannot be added to Object.
        // cm:title and cm:description Policies
        if (cmisAPI.getSession().getBinding().getBindingType().value().equals(BindingType.WEBSERVICES.value()))
        {
            return properties;
        }

        List<Object> aspects = new ArrayList<Object>();
        aspects.add("P:cm:titled");
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
        properties.put("cm:title", contentModel.getTitle());
        properties.put("cm:description", contentModel.getDescription());
        return properties;
    }

    public OperationContext setIncludeAclContext()
    {
        OperationContext context = cmisAPI.getSession().createOperationContext();
        context.setIncludeAcls(true);
        return context;
    }

    public List<Ace> createAce(UserModel user, UserRole role)
    {
        List<String> addPermission = new ArrayList<String>();
        addPermission.add(role.getRoleId());
        Ace addAce = cmisAPI.getSession().getObjectFactory().createAce(user.getUsername(), addPermission);
        List<Ace> addAces = new ArrayList<Ace>();
        addAces.add(addAce);
        return addAces;
    }

    public List<Ace> createAce(GroupModel group, UserRole role)
    {
        List<String> addPermission = new ArrayList<String>();
        addPermission.add(role.getRoleId());
        Ace addAce = cmisAPI.getSession().getObjectFactory().createAce(group.getDisplayName(), addPermission);
        List<Ace> addAces = new ArrayList<Ace>();
        addAces.add(addAce);
        return addAces;
    }

    public List<Ace> createAce(UserModel user, String... permissions)
    {
        List<Ace> addAces = new ArrayList<Ace>();
        RepositoryInfo repositoryInfo = cmisAPI.getSession().getRepositoryInfo();
        AclCapabilities aclCapabilities = repositoryInfo.getAclCapabilities();
        Map<String, PermissionMapping> permissionMappings = aclCapabilities.getPermissionMapping();
        for (String perm : permissions)
        {
            STEP(String.format("%s Add permission '%s' for user %s ", CmisWrapper.STEP_PREFIX, perm, user.getUsername()));
            PermissionMapping permissionMapping = permissionMappings.get(perm);
            List<String> permissionsList = permissionMapping.getPermissions();
            Ace addAce = cmisAPI.getSession().getObjectFactory().createAce(user.getUsername(), permissionsList);
            addAces.add(addAce);
        }
        return addAces;
    }

    public ObjectType getTypeDefinition()
    {
        CmisObject cmisObject = cmisAPI.withCMISUtil().getCmisObject(cmisAPI.getLastResource());
        return cmisAPI.getSession().getTypeDefinition(cmisObject.getBaseTypeId().value());
    }

    public ItemIterable<ObjectType> getTypeChildren(String baseType, boolean includePropertyDefinitions)
    {
        STEP(String.format("%s Get type children for '%s' and includePropertyDefinitions set to '%s'", CmisWrapper.STEP_PREFIX, baseType,
                includePropertyDefinitions));
        return cmisAPI.getSession().getTypeChildren(baseType, includePropertyDefinitions);
    }

    public List<Tree<ObjectType>> getTypeDescendants(String baseTypeId, int depth, boolean includePropertyDefinitions)
    {
        STEP(String.format("%s Get type descendants for '%s' with depth set to %d and includePropertyDefinitions set to '%s'", CmisWrapper.STEP_PREFIX,
                baseTypeId, depth, includePropertyDefinitions));
        return cmisAPI.getSession().getTypeDescendants(baseTypeId, depth, includePropertyDefinitions);
    }

    public String getObjectId(String pathToObject)
    {
        return getCmisObject(pathToObject).getId();
    }

    /**
     * Update property for last resource cmis object
     * 
     * @param propertyName String property name (e.g. cmis:name)
     * @param propertyValue Object property value
     */
    public void updateProperties(String propertyName, Object propertyValue)
    {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(propertyName, propertyValue);
        getCmisObject(cmisAPI.getLastResource()).updateProperties(props);
    }

    protected boolean isFolderInList(FolderModel folderModel, List<FolderModel> folders)
    {
        for (FolderModel folder : folders)
        {
            if (folderModel.getName().equals(folder.getName()))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isFileInList(FileModel fileModel, List<FileModel> files)
    {
        for (FileModel file : files)
        {
            if (fileModel.getName().equals(file.getName()))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isContentInList(ContentModel contentModel, List<ContentModel> contents)
    {
        for (ContentModel content : contents)
        {
            if (content.getName().equals(content.getName()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Get children folders from a parent folder
     *
     * @return List<FolderModel>
     */
    public List<FolderModel> getFolders()
    {
        STEP(String.format("%s Get the folder children from '%s'", CmisWrapper.STEP_PREFIX, cmisAPI.getLastResource()));
        Map<ContentModel, ObjectType> children = getChildren();
        List<FolderModel> folders = new ArrayList<FolderModel>();
        for (Map.Entry<ContentModel, ObjectType> entry : children.entrySet())
        {
            if (entry.getValue().getId().equals(BaseTypeId.CMIS_FOLDER.value()))
            {
                FolderModel folder = new FolderModel(entry.getKey().getName());
                folder.setNodeRef(entry.getKey().getNodeRef());
                folder.setDescription(entry.getKey().getDescription());
                folder.setCmisLocation(entry.getKey().getCmisLocation());
                folder.setProtocolLocation(entry.getKey().getCmisLocation());
                folders.add(folder);
            }
        }
        return folders;
    }

    /**
     * Get children documents from a parent folder
     *
     * @return List<FolderModel>
     */
    public List<FileModel> getFiles()
    {
        STEP(String.format("%s Get the file children from '%s'", CmisWrapper.STEP_PREFIX, cmisAPI.getLastResource()));
        Map<ContentModel, ObjectType> children = getChildren();
        List<FileModel> files = new ArrayList<FileModel>();
        for (Map.Entry<ContentModel, ObjectType> entry : children.entrySet())
        {
            if (entry.getValue().getId().equals(BaseTypeId.CMIS_DOCUMENT.value()))
            {
                FileModel file = new FileModel(entry.getKey().getName());
                file.setNodeRef(entry.getKey().getNodeRef());
                file.setDescription(entry.getKey().getDescription());
                file.setCmisLocation(entry.getKey().getCmisLocation());
                file.setProtocolLocation(entry.getKey().getCmisLocation());
                files.add(file);
            }
        }
        return files;
    }

    /*
     * Get document(set as last resource) content
     */
    public String getDocumentContent()
    {
        Utility.waitToLoopTime(2);
        Document lastVersion = getCmisDocument(cmisAPI.getLastResource());
        lastVersion.refresh();
        LOG.info(String.format("Get the content from %s - node: %s", lastVersion.getName(), lastVersion.getId()));
        ContentStream contentStream = lastVersion.getContentStream();
        String actualContent = "";
        if (contentStream != null)
        {
            actualContent = getContentAsString(contentStream);
        }
        else
        {
            lastVersion = getCmisDocument(cmisAPI.getLastResource());
            lastVersion.refresh();
            LOG.info(String.format("Retry get content stream for %s node: %s", lastVersion.getName(), lastVersion.getId()));
            contentStream = lastVersion.getContentStream();
            if (contentStream != null)
            {
                actualContent = getContentAsString(contentStream);
            }
        }
        if(actualContent.isEmpty())
        {
            Utility.waitToLoopTime(2);
            Document retryDoc = getCmisDocument(cmisAPI.getLastResource());
            retryDoc.refresh();
            LOG.info(String.format("Retry get content stream for %s node: %s", retryDoc.getName(), retryDoc.getId()));
            contentStream = retryDoc.getContentStream();
            if (contentStream != null)
            {
                actualContent = getContentAsString(contentStream);
            }
        }
        return actualContent;
    }

    /**
     * Get user noderef
     * 
     * @param user {@link UserModel}
     */
    public String getUserNodeRef(UserModel user)
    {
        String objectId = "";
        ItemIterable<QueryResult> results = cmisAPI.getSession().query("select cmis:objectId from cm:person where cm:userName = '" + user.getUsername() + "'",
                false);
        for (QueryResult qResult : results)
        {
            PropertyData<?> propData = qResult.getPropertyById("cmis:objectId");
            objectId = (String) propData.getFirstValue();
        }
        return objectId;
    }
}
