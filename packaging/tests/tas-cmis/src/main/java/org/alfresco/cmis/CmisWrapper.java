package org.alfresco.cmis;

import org.alfresco.cmis.dsl.BaseObjectType;
import org.alfresco.cmis.dsl.CheckIn;
import org.alfresco.cmis.dsl.CmisAssertion;
import org.alfresco.cmis.dsl.CmisUtil;
import org.alfresco.cmis.dsl.DocumentVersioning;
import org.alfresco.cmis.dsl.JmxUtil;
import org.alfresco.cmis.dsl.QueryExecutor;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.dsl.DSLContentModelAction;
import org.alfresco.utility.dsl.DSLFile;
import org.alfresco.utility.dsl.DSLFolder;
import org.alfresco.utility.dsl.DSLProtocol;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.DataListItemModel;
import org.alfresco.utility.model.DataListModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.alfresco.utility.Utility.checkObjectIsInitialized;
import static org.alfresco.utility.report.log.Step.STEP;

@Service
@Scope(value = "prototype")
public class CmisWrapper extends DSLProtocol<CmisWrapper> implements DSLContentModelAction<CmisWrapper>, DSLFile<CmisWrapper>, DSLFolder<CmisWrapper>
{
    protected Logger LOG = LogFactory.getLogger();
    public static String STEP_PREFIX = "CMIS:";

    private Session session;

    @Autowired
    private AuthParameterProviderFactory authParameterProviderFactory;

    @Autowired
    CmisProperties cmisProperties;

    public List<String> deleteTreeFailedObjects = new ArrayList<String>();

    @Override
    public CmisWrapper authenticateUser(UserModel userModel)
    {
        return authenticateUser(userModel, authParameterProviderFactory.getDefaultProvider());
    }

    public CmisWrapper authenticateUser(UserModel userModel, Function<UserModel, Map<String, String>> authParameterProvider)
    {
        disconnect();
        STEP(String.format("%s Connect with %s/%s", STEP_PREFIX, userModel.getUsername(), userModel.getPassword()));
        SessionFactory factory = SessionFactoryImpl.newInstance();

        // Initialise a new session parameter map with session authentication parameters for userModel
        Map<String, String> parameter = new HashMap<>(authParameterProvider.apply(userModel));

        String binding = cmisProperties.getCmisBinding().toLowerCase();
        String cmisURLPath = cmisProperties.envProperty().getFullServerUrl() + cmisProperties.getBasePath();
        if (binding.equals(BindingType.BROWSER.value()))
        {
            parameter.put(SessionParameter.BROWSER_URL, cmisURLPath);
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
            LOG.info("Using binding type [{}] to [{}] and credentials: {}", BindingType.BROWSER.value(), cmisURLPath, userModel.toString());
        }
        else if (binding.equals(BindingType.ATOMPUB.value().replace("pub", "")))
        {
            parameter.put(SessionParameter.ATOMPUB_URL, cmisURLPath);
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            LOG.info("Using binding type [{}] to [{}] and credentials: {}", BindingType.ATOMPUB.value(), cmisURLPath, userModel.toString());
        }
            else if (binding.equals(BindingType.WEBSERVICES.value()))
            {
                parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, cmisURLPath);
                parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, cmisURLPath);
                parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, cmisURLPath);
                parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, cmisURLPath);
                parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, cmisURLPath);
                parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, cmisURLPath);
                parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, cmisURLPath);
                parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, cmisURLPath);
                parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, cmisURLPath);
                parameter.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
                LOG.info("Using binding type [{}] to [{}] and credentials: {}", BindingType.WEBSERVICES.value(), cmisURLPath, userModel.toString());
            }

        parameter.put(SessionParameter.CONNECT_TIMEOUT, "20000");
        parameter.put(SessionParameter.READ_TIMEOUT, "60000");
        List<Repository> repositories = factory.getRepositories(parameter);
        parameter.put(SessionParameter.REPOSITORY_ID, repositories.get(0).getId());
        session = repositories.get(0).createSession();
        setTestUser(userModel);
        return this;
    }

    public CmisWrapper authUserUsingBrowserUrlAndBindingType(UserModel userModel, String urlPath, String bindingType)
    {
        STEP(String.format("%s Setting binding type %s to %s", STEP_PREFIX, bindingType, urlPath));
        STEP(String.format("%s Connect with %s/%s", STEP_PREFIX, userModel.getUsername(), userModel.getPassword()));
        SessionFactory factory = SessionFactoryImpl.newInstance();
        // Initialise a new session parameter map with session authentication parameters for userModel
        Map<String, String> parameter = new HashMap<>(authParameterProviderFactory.getDefaultProvider().apply(userModel));

        parameter.put(SessionParameter.BROWSER_URL, urlPath);
        parameter.put(SessionParameter.BINDING_TYPE, bindingType);
        LOG.info("Using binding type [{}] to [{}] and credentials: {}", bindingType, urlPath, userModel.toString());
        List<Repository> repositories = factory.getRepositories(parameter);
        parameter.put(SessionParameter.REPOSITORY_ID, repositories.get(0).getId());
        session = repositories.get(0).createSession();
        setTestUser(userModel);
        return this;
    }

    public AuthParameterProviderFactory getAuthParameterProviderFactory()
    {
        return this.authParameterProviderFactory;
    }

    @Override
    public CmisWrapper disconnect()
    {
        if (session != null)
        {
            getSession().clear();
        }
        return this;
    }

    @Override
    public String buildPath(String parent, String... paths)
    {
        return Utility.convertBackslashToSlash(super.buildPath(parent, paths)).replace("//", "/");
    }

    /**
     * Get the current session
     * 
     * @return Session
     */
    public synchronized Session getSession()
    {
        return session;
    }

    @Override
    public CmisWrapper createFile(FileModel fileModel)
    {
        return createFile(fileModel, BaseTypeId.CMIS_DOCUMENT.value(), VersioningState.MAJOR);
    }

    /**
     * Create a new file
     * 
     * @param fileModel {@link FileModel} file model to be created
     * @param versioningState {@link VersioningState}
     * @return CmisWrapper
     */
    public CmisWrapper createFile(FileModel fileModel, VersioningState versioningState)
    {
        return createFile(fileModel, BaseTypeId.CMIS_DOCUMENT.value(), versioningState);
    }

    /**
     * Create a new file
     * 
     * @param fileModel {@link FileModel} file model to be created
     * @param cmisBaseTypeId base type id (e.g. 'cmis:document')
     * @param versioningState {@link VersioningState}
     * @return CmisWrapper
     */
    public CmisWrapper createFile(FileModel fileModel, String cmisBaseTypeId, VersioningState versioningState)
    {
        return createFile(fileModel, withCMISUtil().getProperties(fileModel, cmisBaseTypeId), versioningState);
    }

    public CmisWrapper createFile(FileModel fileModel, Map<String, Object> properties, VersioningState versioningState)
    {
        ContentStream contentStream = dataContent.getContentStream(fileModel.getName(), fileModel.getContent());
        STEP(String.format("%s Create file '%s' in '%s'", STEP_PREFIX, fileModel.getName(), getCurrentSpace()));
        Document doc = null;
        try
        {
            doc = withCMISUtil().getCmisFolder(getCurrentSpace()).createDocument(properties, contentStream, versioningState);
        }
        catch (CmisStorageException se)
        {
            doc = withCMISUtil().getCmisFolder(getCurrentSpace()).createDocument(properties, contentStream, versioningState);
        }
        fileModel.setNodeRef(doc.getId());
        String location = buildPath(getCurrentSpace(), fileModel.getName());
        setLastResource(location);
        fileModel.setProtocolLocation(location);
        fileModel.setCmisLocation(location);
        dataContent.closeContentStream(contentStream);
        return this;
    }

    /**
     * Create new file from existing one (that was set in last resource)
     * 
     * @param newfileModel {@link FileModel} file model to be created
     * @param sourceFileModel {@link ContentModel} source file model
     * @return CmisWrapper
     */
    public CmisWrapper createFileFromSource(FileModel newfileModel, ContentModel sourceFileModel)
    {
        return createFileFromSource(newfileModel, sourceFileModel, BaseTypeId.CMIS_DOCUMENT.value());
    }

    /**
     * Create new file from existing one with versioning state set to Major(that was set in last resource)
     * 
     * @param newfileModel {@link FileModel} file model to be created
     * @param sourceFileModel {@link ContentModel} source file model
     * @param cmisBaseTypeId base type id (e.g. 'cmis:document')
     * @return CmisWrapper
     */
    public CmisWrapper createFileFromSource(FileModel newfileModel, ContentModel sourceFileModel, String cmisBaseTypeId)
    {
        return createFileFromSource(newfileModel, sourceFileModel, cmisBaseTypeId, VersioningState.MAJOR);
    }

    /**
     * Create new file from existing one (that was set in last resource)
     *
     * @param newfileModel {@link FileModel} file model to be created
     * @param sourceFileModel {@link ContentModel} source file model
     * @param versioningState version(e.g. 'VersioningState.MAJOR')
     * @return CmisWrapper
     */
    public CmisWrapper createFileFromSource(FileModel newfileModel, ContentModel sourceFileModel, VersioningState versioningState)
    {
        return createFileFromSource(newfileModel, sourceFileModel, BaseTypeId.CMIS_DOCUMENT.value(), versioningState);
    }

    /**
     * Create new file from existing one (that was set in last resource)
     *
     * @param newfileModel {@link FileModel} file model to be created
     * @param sourceFileModel {@link ContentModel} source file model
     * @param cmisBaseTypeId base type id (e.g. 'cmis:document')
     * @param versioningState (e.g. 'VersioningState.MAJOR')
     * @return CmisWrapper
     */
    public CmisWrapper createFileFromSource(FileModel newfileModel, ContentModel sourceFileModel, String cmisBaseTypeId, VersioningState versioningState)
    {
        String resourcePath = getLastResource();
        STEP(String.format("%s Create new file '%s' from source '%s' in '%s'", STEP_PREFIX, newfileModel.getName(), sourceFileModel.getName(), resourcePath));
        Document source = withCMISUtil().getCmisDocument(sourceFileModel.getCmisLocation());
        Map<String, Object> properties = withCMISUtil().getProperties(newfileModel, cmisBaseTypeId);
        Document doc = withCMISUtil().getCmisFolder(resourcePath).createDocumentFromSource(source, properties, versioningState);
        doc.refresh();
        newfileModel.setNodeRef(doc.getId());
        String location = buildPath(resourcePath, doc.getName());
        setLastResource(location);
        newfileModel.setProtocolLocation(location);
        newfileModel.setCmisLocation(location);
        return this;
    }

    @Override
    public CmisWrapper createFolder(FolderModel folderModel)
    {
        return createFolder(folderModel, BaseTypeId.CMIS_FOLDER.value());
    }

    public CmisWrapper createFolder(FolderModel folderModel, String cmisBaseTypeId)
    {
        Map<String, Object> properties = withCMISUtil().getProperties(folderModel, cmisBaseTypeId);
        createFolder(folderModel, properties);
        return this;
    }

    public CmisWrapper createFolder(FolderModel folderModel, Map<String, Object> properties)
    {
        STEP(String.format("%s Create folder '%s' in '%s'", STEP_PREFIX, folderModel.getName(), getCurrentSpace()));
        Folder folder = withCMISUtil().getCmisFolder(getCurrentSpace()).createFolder(properties);
        String location = buildPath(getCurrentSpace(), folderModel.getName());
        setLastResource(location);
        folderModel.setProtocolLocation(location);
        folderModel.setCmisLocation(location);
        folderModel.setNodeRef(folder.getId());
        return this;
    }

    /**
     * Deletes this folder and all subfolders with all versions and continue on failure
     * 
     * @return current wrapper
     */
    public CmisWrapper deleteFolderTree()
    {
        return deleteFolderTree(true, UnfileObject.DELETE, true);
    }

    /**
     * Deletes this folder and all subfolders with specific parameters
     * 
     * @param allVersions
     * @param unfile {@link UnfileObject}
     * @param continueOnFailure
     * @return current wrapper
     */
    public CmisWrapper deleteFolderTree(boolean allVersions, UnfileObject unfile, boolean continueOnFailure)
    {
        String path = getLastResource();
        Folder parent = withCMISUtil().getCmisFolder(Utility.convertBackslashToSlash(new File(path).getParent()));
        STEP(String.format("%s Delete parent folder from '%s'", STEP_PREFIX, path));
        Folder folder = withCMISUtil().getCmisFolder(path);
        folder.refresh();
        deleteTreeFailedObjects.clear();
        deleteTreeFailedObjects = folder.deleteTree(allVersions, unfile, continueOnFailure);
        for (String failedObj : deleteTreeFailedObjects)
        {
            LOG.error(String.format("Failed to delete object %s", failedObj));
        }
        if (!deleteTreeFailedObjects.isEmpty())
        {
            LOG.info(String.format("Retry: delete parent folder from %s", path));
            Utility.waitToLoopTime(2);
            folder.refresh();
            folder.deleteTree(allVersions, unfile, continueOnFailure);
        }
        else
        {
            parent.refresh();
            dataContent.waitUntilContentIsDeleted(path);
        }
        return this;
    }

    @Override
    public String getRootPath() throws TestConfigurationException
    {
        return "/";
    }

    @Override
    public String getSitesPath() throws TestConfigurationException
    {
        return String.format("%s/%s", getPrefixSpace(), "Sites");
    }

    @Override
    public String getUserHomesPath() throws TestConfigurationException
    {
        return String.format("%s/%s", getPrefixSpace(), "User Homes");
    }

    @Override
    public String getDataDictionaryPath() throws TestConfigurationException
    {
        return String.format("%s/%s", getPrefixSpace(), "Data Dictionary");
    }

    public String getSharedPath() throws TestConfigurationException
    {
        return String.format("%s/%s", getPrefixSpace(), "Shared");
    }

    @Override
    public CmisWrapper usingSite(String siteId)
    {
        STEP(String.format("%s Navigate to site '%s/%s'", STEP_PREFIX, siteId, "documentLibrary"));
        checkObjectIsInitialized(siteId, "SiteID");
        setCurrentSpace(buildSiteDocumentLibraryPath(siteId, ""));
        return this;
    }

    @Override
    public CmisWrapper usingSite(SiteModel siteModel)
    {
        STEP(String.format("%s Navigate to site '%s/%s'", STEP_PREFIX, siteModel.getId(), "documentLibrary"));
        checkObjectIsInitialized(siteModel, "SiteModel");
        String path = buildSiteDocumentLibraryPath(siteModel.getId(), "");
        setCurrentSpace(path);
        return this;
    }

    @Override
    public CmisWrapper usingUserHome(String username)
    {
        STEP(String.format("%s Navigate to 'User Home' folder", STEP_PREFIX));
        checkObjectIsInitialized(username, "username");
        setCurrentSpace(buildUserHomePath(username, ""));
        return this;
    }

    @Override
    public CmisWrapper usingUserHome()
    {
        STEP(String.format("%s Navigate to 'User Home' folder", STEP_PREFIX));
        checkObjectIsInitialized(getTestUser().getUsername(), "username");
        setCurrentSpace(buildUserHomePath(getTestUser().getUsername(), ""));
        return this;
    }

    public CmisWrapper usingShared()
    {
        STEP(String.format("%s Navigate to 'Shared' folder", STEP_PREFIX));
        setCurrentSpace(getSharedPath());
        return this;
    }

    @Override
    public CmisWrapper usingResource(ContentModel model)
    {
        STEP(String.format("%s Navigate to '%s'", STEP_PREFIX, model.getName()));
        checkObjectIsInitialized(model, "contentName");
        setCurrentSpace(model.getCmisLocation());
        return this;
    }

    @Override
    protected String getProtocolJMXConfigurationStatus()
    {
        return "";
    }

    @Override
    public String getPrefixSpace()
    {
        return "";
    }

    @Override
    public CmisWrapper rename(String newName)
    {
        String resourcePath = getLastResource();
        CmisObject objToRename = withCMISUtil().getCmisObject(resourcePath);
        STEP(String.format("%s Rename '%s' to '%s'", STEP_PREFIX, objToRename.getName(), newName));
        objToRename.rename(newName);
        setLastResource(buildPath(new File(resourcePath).getParent(), newName));
        return this;
    }

    @Override
    public CmisWrapper update(String content)
    {
        return update(content, true);
    }

    public CmisWrapper update(String content, boolean isLastChunk)
    {
        Document doc = withCMISUtil().getCmisDocument(getLastResource());
        doc.refresh();
        Utility.waitToLoopTime(2);
        STEP(String.format("%s Update content from '%s' by appending '%s'", STEP_PREFIX, doc.getName(), content));
        ContentStream contentStream = dataContent.getContentStream(doc.getName(), content);
        doc.appendContentStream(contentStream, isLastChunk);
        dataContent.closeContentStream(contentStream);
        return this;
    }

    /**
     * Update the properties of the last resource {@link ContentModel}.
     * Example updateProperty("cmis:name", "test1234")
     * 
     * @param property
     * @param value
     * @return
     */
    public CmisWrapper updateProperty(String property, Object value)
    {
        String lastResource = getLastResource();
        CmisObject objSource = withCMISUtil().getCmisObject(lastResource);
        STEP(String.format("%s Update '%s' property for '%s'", STEP_PREFIX, property, objSource.getName()));
        Map<String, Object> properties = new HashMap<>();
        properties.put(property, value);
        objSource.updateProperties(properties, true);

        if (property.equals("cmis:name"))
        {
            if (objSource instanceof Document)
            {
                setLastResource(buildPath(new File(getLastResource()).getParent(), objSource.getName()));
            }
            else if (objSource instanceof Folder)
            {
                setLastResource(buildPath(((Folder) objSource).getFolderParent().getPath(), value.toString()));
            }
        }
        return this;
    }

    @Override
    public CmisWrapper delete()
    {
        String resourcePath = getLastResource();
        STEP(String.format("%s Delete content from '%s'", STEP_PREFIX, resourcePath));
        withCMISUtil().getCmisObject(resourcePath).delete();
        return this;
    }

    /**
     * Deletes all versions if parameter is set to true, otherwise deletes only last version
     * 
     * @param allVersions
     * @return
     */
    public CmisWrapper deleteAllVersions(boolean allVersions)
    {
        String resourcePath = getLastResource();
        if (allVersions)
            STEP(String.format("%s Delete all content '%s' versions", STEP_PREFIX, resourcePath));
        else
            STEP(String.format("%s Delete only the last content '%s' version", STEP_PREFIX, resourcePath));
        withCMISUtil().getCmisObject(getLastResource()).delete(allVersions);
        return this;
    }

    /**
     * Delete content stream
     * 
     * @return
     */
    public CmisWrapper deleteContent()
    {
        String resourcePath = getLastResource();
        STEP(String.format("%s Delete document content from '%s'", STEP_PREFIX, resourcePath));
        withCMISUtil().getCmisDocument(getLastResource()).deleteContentStream();
        return this;
    }

    /**
     * Delete content stream and refresh document
     * 
     * @param refresh boolean refresh resource
     * @return
     */
    public CmisWrapper deleteContent(boolean refresh)
    {
        String resourcePath = getLastResource();
        STEP(String.format("%s Delete document content from '%s'", STEP_PREFIX, resourcePath));
        withCMISUtil().getCmisDocument(getLastResource()).deleteContentStream(refresh);
        return this;
    }

    /**
     * Set the content stream for a document
     * 
     * @param content String content to set
     * @param overwrite
     * @return
     */
    public CmisWrapper setContent(String content, boolean overwrite)
    {
        Utility.waitToLoopTime(1);
        Document doc = withCMISUtil().getCmisDocument(getLastResource());
        doc.refresh();
        STEP(String.format("%s Set '%s' content to '%s' - node: %s", STEP_PREFIX, content, doc.getName(), doc.getId()));
        ContentStream contentStream = dataContent.getContentStream(doc.getName(), content);
        try
        {
            doc.setContentStream(contentStream, overwrite, true);
        }
        catch (CmisStorageException cs)
        {
            doc.setContentStream(contentStream, overwrite, true);
        }
        dataContent.closeContentStream(contentStream);
        return this;
    }

    /**
     * Set the content stream for a document with overwrite set to TRUE
     * 
     * @param content
     * @return
     */
    public CmisWrapper setContent(String content)
    {
        return setContent(content, true);
    }

    /**
     * Create a 'R:cm:basis' relationship between a source document and a target document
     *
     * @param targetContent
     * @return
     */
    public CmisWrapper createRelationshipWith(ContentModel targetContent)
    {
        return createRelationshipWith(targetContent, "R:cm:basis");
    }

    /**
     * Create relationship between a source document and a target document
     *
     * @param targetContent {@link ContentModel}
     * @param relationType
     * @return
     */
    public CmisWrapper createRelationshipWith(ContentModel targetContent, String relationType)
    {
        STEP(String.format("%s Set %s relationship between source from '%s' and target '%s'", STEP_PREFIX, relationType, getLastResource(),
                targetContent.getName()));
        Map<String, String> properties = new HashMap<>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, relationType);
        properties.put(PropertyIds.SOURCE_ID, withCMISUtil().getCmisObject(getLastResource()).getId());
        properties.put(PropertyIds.TARGET_ID, targetContent.getNodeRef());
        getSession().createRelationship(properties);
        return this;
    }

    /**
     * Method allows you to file a document object in more than one folder.
     *
     * @param destination - the destination folder to which this document will be added
     * @param allVersions - if this parameter is true, then all versions of the document will be added to the destination folder
     * @return
     */
    public CmisWrapper addDocumentToFolder(FolderModel destination, boolean allVersions)
    {
        CmisObject objSource = withCMISUtil().getCmisObject(getLastResource());
        Folder objDestination = withCMISUtil().getCmisFolder(destination.getCmisLocation());
        STEP(String.format("%s Add object '%s' to '%s'", STEP_PREFIX, objSource.getName(), destination.getCmisLocation()));
        ((FileableCmisObject) objSource).addToFolder(objDestination, allVersions);
        setLastResource(buildPath(destination.getCmisLocation(), objSource.getName()));
        return this;
    }

    /**
     * Method allows you to remove a document object from the given folder.
     *
     * @param parentFolder - the folder from which this object should be removed
     * @return
     */
    public CmisWrapper removeDocumentFromFolder(FolderModel parentFolder)
    {
        CmisObject objSource = withCMISUtil().getCmisObject(getLastResource());
        Folder parentObj = withCMISUtil().getCmisFolder(parentFolder.getCmisLocation());
        STEP(String.format("%s Remove object '%s' from '%s'", STEP_PREFIX, objSource.getName(), parentFolder.getCmisLocation()));
        ((FileableCmisObject) objSource).removeFromFolder(parentObj);
        return this;
    }

    /**
     * Get child folders from a parent folder
     *
     * @return List<FolderModel>
     */
    @Override
    public List<FolderModel> getFolders()
    {
        STEP(String.format("%s Get the folder children from '%s'", STEP_PREFIX, getLastResource()));
        return withCMISUtil().getFolders();
    }

    /**
     * Get child documents from a parent folder
     *
     * @return List<FolderModel>
     */
    @Override
    public List<FileModel> getFiles()
    {
        STEP(String.format("%s Get the file children from '%s'", STEP_PREFIX, getLastResource()));
        return withCMISUtil().getFiles();
    }

    @Override
    public CmisWrapper copyTo(ContentModel destination)
    {
        String source = getLastResource();
        String sourceName = new File(source).getName();
        STEP(String.format("%s Copy '%s' to '%s'", STEP_PREFIX, sourceName, destination.getCmisLocation()));
        CmisObject objSource = withCMISUtil().getCmisObject(source);

        CmisObject objDestination = withCMISUtil().getCmisObject(destination.getCmisLocation());
        if (objSource instanceof Document)
        {
            Document d = (Document) objSource;
            d.copy(objDestination);
        }
        else if (objSource instanceof Folder)
        {
            Folder fFrom = (Folder) objSource;
            Folder toFolder = (Folder) objDestination;
            withCMISUtil().copyFolder(fFrom, toFolder);
        }
        setLastResource(buildPath(destination.getCmisLocation(), sourceName));
        return this;
    }

    @Override
    public CmisWrapper moveTo(ContentModel destination)
    {
        String source = getLastResource();
        String sourceName = new File(source).getName();
        STEP(String.format("%s Move '%s' to '%s'", STEP_PREFIX, sourceName, destination.getCmisLocation()));
        CmisObject objSource = withCMISUtil().getCmisObject(source);
        CmisObject objDestination = withCMISUtil().getCmisObject(destination.getCmisLocation());
        if (objSource instanceof Document)
        {
            Document d = (Document) objSource;
            List<Folder> parents = d.getParents();
            CmisObject parent = getSession().getObject(parents.get(0).getId());
            d.move(parent, objDestination);
        }
        else if (objSource instanceof Folder)
        {
            Folder f = (Folder) objSource;
            List<Folder> parents = f.getParents();
            CmisObject parent = getSession().getObject(parents.get(0).getId());
            f.move(parent, objDestination);
        }
        setLastResource(buildPath(destination.getCmisLocation(), sourceName));
        return this;
    }

    public RepositoryInfo getRepositoryInfo()
    {
        STEP(String.format("Get repository information for user %s", getCurrentUser().getUsername()));
        return getSession().getRepositoryInfo();
    }

    public AclCapabilities getAclCapabilities()
    {
        return getRepositoryInfo().getAclCapabilities();
    }

    /**
     * Checks out the document
     */
    public CmisWrapper checkOut()
    {
        Document document = withCMISUtil().getCmisDocument(getLastResource());
        STEP(String.format("%s Check out document '%s'", STEP_PREFIX, document.getName()));
        try
        {
            document.checkOut();
        }
        catch (CmisRuntimeException e)
        {
            document.checkOut();
        }
        return this;
    }

    /**
     * If this is a PWC (private working copy) the check out will be reversed.
     */
    public CmisWrapper cancelCheckOut()
    {
        Document document = withCMISUtil().getCmisDocument(getLastResource());
        STEP(String.format("%s Cancel document '%s' check out", STEP_PREFIX, document.getName()));
        document.cancelCheckOut();
        return this;
    }

    /**
     * Starts the process to check in a document
     */
    public CheckIn prepareDocumentForCheckIn()
    {
        return new CheckIn(this);
    }

    /**
     * Reloads the resource from the repository
     */
    public CmisWrapper refreshResource()
    {
        CmisObject cmisObject = withCMISUtil().getCmisObject(getLastResource());
        STEP(String.format("%s Reload '%s'", STEP_PREFIX, cmisObject.getName()));
        cmisObject.refresh();
        return this;
    }

    /**
     * @return utilities that are used by CMIS
     */
    public CmisUtil withCMISUtil()
    {
        return new CmisUtil(this);
    }

    /**
     * @return JMX DSL for this wrapper
     */
    public JmxUtil withJMX()
    {
        return new JmxUtil(this, jmxBuilder.getJmxClient());
    }

    @Override
    public CmisAssertion assertThat()
    {
        return new CmisAssertion(this);
    }

    /**
     * Starts the process to work with a version of a document
     */
    public DocumentVersioning usingVersion()
    {
        return new DocumentVersioning(this, withCMISUtil().getCmisObject(getLastResource()));
    }

    /**
     * Add new permission for user
     * 
     * @param user UserModel user
     * @param role UserRole role to add
     * @param aclPropagation AclPropagation propagation
     * @return
     */
    public CmisWrapper addAcl(UserModel user, UserRole role, AclPropagation aclPropagation)
    {
        STEP(String.format("%s Add permission '%s' for user %s ", STEP_PREFIX, role.name(), user.getUsername()));
        withCMISUtil().getCmisObject(getLastResource(), withCMISUtil().setIncludeAclContext()).addAcl(withCMISUtil().createAce(user, role), aclPropagation);
        return this;
    }

    /**
     * Add new permission for a group
     * 
     * @param group GroupModel group
     * @param role UserRole role to add
     * @param aclPropagation AclPropagation propagation
     * @return
     */
    public CmisWrapper addAcl(GroupModel group, UserRole role, AclPropagation aclPropagation)
    {
        STEP(String.format("%s Add permission '%s' for user %s ", STEP_PREFIX, role.name(), group.getDisplayName()));
        withCMISUtil().getCmisObject(getLastResource(), withCMISUtil().setIncludeAclContext()).addAcl(withCMISUtil().createAce(group, role), aclPropagation);
        return this;
    }

    /**
     * Add new permission for a group
     * 
     * @param group GroupModel group
     * @param role UserRole role to add
     * @return
     */
    public CmisWrapper addAcl(GroupModel group, UserRole role)
    {
        return addAcl(group, role, null);
    }

    /**
     * Add new permissions to user
     * 
     * @param user {@link UserModel}
     * @param permissions to add ({@link PermissionMapping} can be used)
     * @return
     */
    public CmisWrapper addAcl(UserModel user, String... permissions)
    {
        withCMISUtil().getCmisObject(getLastResource(), withCMISUtil().setIncludeAclContext()).addAcl(withCMISUtil().createAce(user, permissions), null);
        return this;
    }

    /**
     * Add new permission for user
     * 
     * @param user UserModel user
     * @param role UserRole role to add
     * @return
     */
    public CmisWrapper addAcl(UserModel user, UserRole role)
    {
        return addAcl(user, role, null);
    }

    /**
     * Update permission for user.
     * If the role to remove is invalid a {@link CmisConstraintException} is thrown.
     * 
     * @param user UserModel user
     * @param newRole UserRole new role to add
     * @param removeRole UserRole remove already added role
     * @param aclPropagation AclPropagation
     * @return
     */
    public CmisWrapper applyAcl(UserModel user, UserRole newRole, UserRole removeRole, AclPropagation aclPropagation)
    {
        STEP(String.format("%s Edit permission for user %s from %s to %s ", STEP_PREFIX, user.getUsername(), removeRole.name(), newRole.name()));
        withCMISUtil().getCmisObject(getLastResource(), withCMISUtil().setIncludeAclContext()).applyAcl(withCMISUtil().createAce(user, newRole),
                withCMISUtil().createAce(user, removeRole), aclPropagation);
        return this;
    }

    /**
     * Update permission for user.
     * If the role to remove is invalid a {@link CmisConstraintException} is thrown.
     * 
     * @param user UserModel user
     * @param newRole UserRole new role to add
     * @param removeRole UserRole remove already added role
     * @return
     */
    public CmisWrapper applyAcl(UserModel user, UserRole newRole, UserRole removeRole)
    {
        return applyAcl(user, newRole, removeRole, null);
    }

    /**
     * Update permission for user.
     * If the permission to remove is invalid a {@link CmisConstraintException} is thrown.
     * 
     * @param user {@link UserModel }
     * @param newPermission permissions to add ({@link PermissionMapping} can be used)
     * @param removePermission permissions to remove ({@link PermissionMapping} can be used)
     * @return
     */
    public CmisWrapper applyAcl(UserModel user, String newPermission, String removePermission)
    {
        STEP(String.format("%s Edit permission for user %s from %s to %s ", STEP_PREFIX, user.getUsername(), removePermission, newPermission));
        withCMISUtil().getCmisObject(getLastResource(), withCMISUtil().setIncludeAclContext()).applyAcl(withCMISUtil().createAce(user, newPermission),
                withCMISUtil().createAce(user, removePermission), null);
        return this;
    }

    /**
     * Remove permission from user
     * 
     * @param user UserModel user
     * @param removeRole UserRole role to remove
     * @param aclPropagation AclPropagation
     * @return
     */
    public CmisWrapper removeAcl(UserModel user, UserRole removeRole, AclPropagation aclPropagation)
    {
        STEP(String.format("%s Remove permission '%s' from user %s ", STEP_PREFIX, removeRole.name(), user.getUsername()));
        withCMISUtil().getCmisObject(getLastResource(), withCMISUtil().setIncludeAclContext()).removeAcl(withCMISUtil().createAce(user, removeRole),
                aclPropagation);
        return this;
    }

    /**
     * Remove permission from user
     * 
     * @param user UserModel user
     * @param removeRole UserRole role to remove
     * @return
     */
    public CmisWrapper removeAcl(UserModel user, UserRole removeRole)
    {
        return removeAcl(user, removeRole, null);
    }

    public CmisWrapper removeAcl(UserModel user, String permissionToRemove)
    {
        STEP(String.format("%s Remove permission '%s' from user %s ", STEP_PREFIX, permissionToRemove, user.getUsername()));
        withCMISUtil().getCmisObject(getLastResource(), withCMISUtil().setIncludeAclContext()).removeAcl(withCMISUtil().createAce(user, permissionToRemove),
                null);
        return this;
    }

    /**
     * Pass a string CMIS query, that will be handled by {@link QueryExecutor} using {@link org.apache.chemistry.opencmis.client.api.Session#query(String, boolean)}
     * 
     * @param query
     * @return {@link QueryExecutor} will all DSL assertions on returned restult
     */
    public QueryExecutor withQuery(String query)
    {
        return new QueryExecutor(this, query);
    }

    /**
     * Use this method if the document is checked out. If not {@link CmisVersioningException} will be thrown.
     * 
     * @return
     */
    public CmisWrapper usingPWCDocument()
    {
        STEP(String.format("%s Navigate to private working copy of content '%s'", STEP_PREFIX, withCMISUtil().getPWCFileModel().getName()));
        setCurrentSpace(withCMISUtil().getPWCFileModel().getCmisLocation());
        return this;
    }

    /**
     * @param baseType
     * @return the DSL of asserting BaseObject type children for example.
     */
    public BaseObjectType usingObjectType(String baseType)
    {
        return new BaseObjectType(this, baseType);
    }

    /**
     * Create a new data list type
     * 
     * @param dataListModel {@link DataListModel}
     * @return
     */
    public CmisWrapper createDataList(DataListModel dataListModel)
    {
        Map<String, Object> properties = withCMISUtil().getProperties(dataListModel, "F:dl:dataList");
        properties.put("dl:dataListItemType", dataListModel.getDataListItemType());
        Folder folder = withCMISUtil().getCmisFolder(getCurrentSpace()).createFolder(properties);
        String location = buildPath(getCurrentSpace(), dataListModel.getName());
        setLastResource(location);
        dataListModel.setProtocolLocation(location);
        dataListModel.setCmisLocation(location);
        dataListModel.setNodeRef(folder.getId());
        return this;
    }

    /**
     * Create new data list item
     * 
     * @param itemModel {@link DataListItemModel}
     * @return
     */
    public CmisWrapper createDataListItem(DataListItemModel itemModel)
    {
        Map<String, Object> propertyMap = itemModel.getItemProperties();
        String name = (String) propertyMap.get(PropertyIds.NAME);
        STEP(String.format("%s Create new data list item %s (type: %s)", STEP_PREFIX, name, propertyMap.get(PropertyIds.OBJECT_TYPE_ID)));
        ObjectId itemId = getSession().createDocument(propertyMap, withCMISUtil().getCmisObject(getLastResource()), null, null);
        String path = buildPath(getCurrentSpace(), name);
        itemModel.setName(name);
        itemModel.setCmisLocation(path);
        itemModel.setProtocolLocation(path);
        itemModel.setNodeRef(itemId.getId());
        setLastResource(path);
        return this;
    }

    /**
     * Attach documents to existent item set in last resource
     * 
     * @param documents {@link ContentModel} list of content to attach
     * @return
     */
    public CmisWrapper attachDocument(ContentModel... contents)
    {
        String itemId = withCMISUtil().getCmisObject(getLastResource()).getId();
        for (ContentModel content : contents)
        {
            STEP(String.format("Attach document %s to item %s", content.getName(), itemId));
            Map<String, Object> relProps = new HashMap<String, Object>();
            relProps.put(PropertyIds.OBJECT_TYPE_ID, "R:cm:attachments");
            relProps.put(PropertyIds.SOURCE_ID, itemId);
            relProps.put(PropertyIds.TARGET_ID, content.getNodeRef());
            getSession().createRelationship(relProps);
        }
        return this;
    }

    /**
     * Assign user to existent item set in last resource
     * 
     * @param user {@link UserModel}
     * @param relationType e.g. R:dl:issueAssignedTo, R:dl:assignee, R:dl:taskAssignee
     * @return
     */
    public CmisWrapper assignToUser(UserModel user, String relationType)
    {
        Map<String, Object> relProps = new HashMap<String, Object>();
        relProps.put(PropertyIds.OBJECT_TYPE_ID, relationType);
        relProps.put(PropertyIds.SOURCE_ID, withCMISUtil().getCmisObject(getLastResource()).getId());
        relProps.put(PropertyIds.TARGET_ID, withCMISUtil().getUserNodeRef(user));
        getSession().createRelationship(relProps);
        return this;
    }
    
    /**
     * Add new secondary types
     * 
     * @param secondaryTypes e.g. P:cm:effectivity, P:audio:audio, P:cm:dublincore
     * @return
     */
    public CmisWrapper addSecondaryTypes(String...secondaryTypes)
    {
        CmisObject object = withCMISUtil().getCmisObject(getLastResource());
        List<String> secondaryTypesNew = new ArrayList<String>();
        for(SecondaryType oldType : object.getSecondaryTypes())
        {
            secondaryTypesNew.add(oldType.getId());
        }
        for(String newType : secondaryTypes)
        {
            secondaryTypesNew.add(newType);
        }
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondaryTypesNew);
        object.updateProperties(properties);
        return this;
    }
}
