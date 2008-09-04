package org.alfresco.repo.cmis.ws;


import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Base class for all CMIS content tests
 *
 * @author Dmitry Lazurkin
 *
 */
public class BaseServicePortContentTest extends BaseServicePortTest
{
    protected CheckOutCheckInService checkOutCheckInService;

    private static final String IMPORT = "import-for-cmis-ws-test.acp";

    protected static final String L0_FILE_0 = "L0-File-0";
    protected static final String L0_FILE_1 = "L0-File-1";
    protected static final String L0_FILE_2 = "L0-File-2";
    protected static final String L1_FILE_0 = "L1-File-0";
    protected static final String L1_FILE_1 = "L1-File-1";
    protected static final String L1_FILE_VERSIONABLE = "L1-File-Versionable";
    protected static final String L0_FOLDER_0 = "L0-Folder-0";
    protected static final String L0_FOLDER_1 = "L0-Folder-1";
    protected static final String L0_FOLDER_2 = "L0-Folder-2";
    protected static final String L1_FOLDER_0 = "L1-Folder-0";
    protected static final String L1_FOLDER_1 = "L1-Folder-1";

    protected NodeRef L0_FILE_0_NODEREF;
    protected NodeRef L0_FILE_1_NODEREF;
    protected NodeRef L0_FILE_2_NODEREF;
    protected NodeRef L1_FILE_0_NODEREF;
    protected NodeRef L1_FILE_1_NODEREF;
    protected NodeRef L0_FOLDER_0_NODEREF;
    protected NodeRef L0_FOLDER_1_NODEREF;
    protected NodeRef L0_FOLDER_2_NODEREF;
    protected NodeRef L1_FOLDER_0_NODEREF;
    protected NodeRef L1_FOLDER_1_NODEREF;
    protected NodeRef L0_NONEXISTENT_NODEREF;

    protected NodeRef L1_FILE_VERSION_2_1_NODEREF;
    protected NodeRef L1_FILE_VERSION_2_0_NODEREF;
    protected NodeRef L1_FILE_VERSION_1_0_NODEREF;

    protected AssociationRef L0_FILE_1_TO_L0_FILE_0_ASSOCREF;
    protected AssociationRef NONEXISTENT_ASSOCREF;

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();

        checkOutCheckInService = serviceRegistry.getCheckOutCheckInService();

        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        InputStream is = getClass().getClassLoader().getResourceAsStream(IMPORT);
        if (is == null)
        {
            throw new NullPointerException("Test resource not found: " + IMPORT);
        }

        NodeRef importForTest = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "import-for-test.acp"), ContentModel.TYPE_CONTENT).getChildRef();
        ContentService contentService = serviceRegistry.getContentService();
        ContentWriter writer = contentService.getWriter(importForTest, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_ACP);
        writer.setEncoding("UTF-8");
        writer.putContent(is);

        Map<String, Serializable> params = new HashMap<String, Serializable>(2, 1.0f);
        params.put(ImporterActionExecuter.PARAM_DESTINATION_FOLDER, rootNodeRef);
        params.put(ImporterActionExecuter.PARAM_ENCODING, "UTF-8");

        ActionService actionService = serviceRegistry.getActionService();
        Action action = actionService.createAction(ImporterActionExecuter.NAME, params);
        action.setExecuteAsynchronously(false);

        actionService.executeAction(action, importForTest);

        nodeService.deleteNode(importForTest);

        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        VersionService versionService = serviceRegistry.getVersionService();

        L0_FILE_0_NODEREF = fileFolderService.resolveNamePath(rootNodeRef, Collections.singletonList(L0_FILE_0)).getNodeRef();
        L0_FILE_1_NODEREF = fileFolderService.resolveNamePath(rootNodeRef, Collections.singletonList(L0_FILE_1)).getNodeRef();
        L0_FILE_2_NODEREF = fileFolderService.resolveNamePath(rootNodeRef, Collections.singletonList(L0_FILE_2)).getNodeRef();
        L0_FOLDER_0_NODEREF = fileFolderService.resolveNamePath(rootNodeRef, Collections.singletonList(L0_FOLDER_0)).getNodeRef();
        L0_FOLDER_1_NODEREF = fileFolderService.resolveNamePath(rootNodeRef, Collections.singletonList(L0_FOLDER_1)).getNodeRef();
        L0_FOLDER_2_NODEREF = fileFolderService.resolveNamePath(rootNodeRef, Collections.singletonList(L0_FOLDER_2)).getNodeRef();
        L1_FILE_0_NODEREF = fileFolderService.resolveNamePath(L0_FOLDER_0_NODEREF, Collections.singletonList(L1_FILE_0)).getNodeRef();
        L1_FILE_1_NODEREF = fileFolderService.resolveNamePath(L0_FOLDER_1_NODEREF, Collections.singletonList(L1_FILE_1)).getNodeRef();

        L1_FILE_VERSION_2_1_NODEREF = fileFolderService.resolveNamePath(L0_FOLDER_2_NODEREF, Collections.singletonList(L1_FILE_VERSIONABLE)).getNodeRef();
        nodeService.addAspect(L1_FILE_VERSION_2_1_NODEREF, ContentModel.ASPECT_VERSIONABLE, Collections.singletonMap(ContentModel.PROP_AUTO_VERSION, (Serializable) Boolean.FALSE));
        contentService.getWriter(L1_FILE_VERSION_2_1_NODEREF, ContentModel.PROP_CONTENT, true).putContent("1.0");
        L1_FILE_VERSION_1_0_NODEREF = versionService.createVersion(L1_FILE_VERSION_2_1_NODEREF, createVersionProps("1.0", VersionType.MAJOR)).getFrozenStateNodeRef();
        contentService.getWriter(L1_FILE_VERSION_2_1_NODEREF, ContentModel.PROP_CONTENT, true).putContent("2.0");
        L1_FILE_VERSION_2_0_NODEREF = versionService.createVersion(L1_FILE_VERSION_2_1_NODEREF, createVersionProps("2.0", VersionType.MAJOR)).getFrozenStateNodeRef();
        contentService.getWriter(L1_FILE_VERSION_2_1_NODEREF, ContentModel.PROP_CONTENT, true).putContent("2.1");
        versionService.createVersion(L1_FILE_VERSION_2_1_NODEREF, createVersionProps("2.1", VersionType.MINOR));

        L1_FOLDER_0_NODEREF = fileFolderService.resolveNamePath(L0_FOLDER_0_NODEREF, Collections.singletonList(L1_FOLDER_0)).getNodeRef();
        L1_FOLDER_1_NODEREF = fileFolderService.resolveNamePath(L0_FOLDER_0_NODEREF, Collections.singletonList(L1_FOLDER_1)).getNodeRef();
        L0_NONEXISTENT_NODEREF = importForTest;

        L0_FILE_1_TO_L0_FILE_0_ASSOCREF = nodeService.createAssociation(L0_FILE_1_NODEREF, L0_FILE_0_NODEREF, ContentModel.ASSOC_REFERENCES);
        NONEXISTENT_ASSOCREF = new AssociationRef(L1_FILE_0_NODEREF, ContentModel.ASSOC_REFERENCES, L1_FILE_1_NODEREF);

        authenticationComponent.clearCurrentSecurityContext();
    }

    private Map<String, Serializable> createVersionProps(String comment, VersionType versionType)
    {
        Map<String, Serializable> props = new HashMap<String, Serializable>(1, 1.0f);
        props.put(Version.PROP_DESCRIPTION, comment);
        props.put(VersionModel.PROP_VERSION_TYPE, versionType);
        return props;
    }

}
