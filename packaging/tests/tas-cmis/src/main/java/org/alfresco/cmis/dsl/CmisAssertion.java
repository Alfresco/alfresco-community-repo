package org.alfresco.cmis.dsl;

import static org.alfresco.utility.report.log.Step.STEP;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.CmisWrapper;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.dsl.DSLAssertion;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.UserModel;
import org.apache.chemistry.opencmis.client.api.ChangeEvent;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

/**
 * DSL with all assertion available for {@link CmisWrapper}
 */
public class CmisAssertion extends DSLAssertion<CmisWrapper>
{
    public static String STEP_PREFIX = "CMIS:";

    public CmisAssertion(CmisWrapper cmisAPI)
    {
        super(cmisAPI);
    }

    public CmisWrapper cmisAPI()
    {
        return getProtocol();
    }
    
    @Override
    public CmisWrapper existsInRepo()
    {
        STEP(String.format("CMIS: Assert that content '%s' exists in repository", cmisAPI().getLastResource()));
        Assert.assertTrue(!cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource()).getId().isEmpty(),
                    String.format("Content {%s} was found in repository", cmisAPI().getLastResource()));
        return cmisAPI();
    }
    
    @Override
    public CmisWrapper doesNotExistInRepo()
    {
        STEP(String.format("CMIS: Assert that content '%s' does not exist in repository", cmisAPI().getLastResource()));
        boolean notFound = false;
        try
        {
            cmisAPI().getSession().clear();
            cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource());
        }
        catch (CmisObjectNotFoundException | CmisRuntimeException e)
        {
            notFound = true;
        }
        Assert.assertTrue(notFound, String.format("Content {%s} was NOT found in repository", cmisAPI().getLastResource()));
        return cmisAPI();
    }

    /**
     * Verify changes for a specific object from cmis log
     * 
     * @param model {@link ContentModel}
     * @param changeTypes {@link ChangeType}
     * @return
     * @throws Exception
     */
    public CmisWrapper contentModelHasChanges(ContentModel model, ChangeType... changeTypes) throws Exception
    {
        String token = cmisAPI().getRepositoryInfo().getLatestChangeLogToken();
        if (StringUtils.isEmpty(token))
        {
            throw new TestConfigurationException("Please enable CMIS audit");
        }
        ItemIterable<ChangeEvent> events = cmisAPI().getSession().getContentChanges(token, true);
        String lastObjectId = model.getNodeRef();
        boolean isChange = false;
        for (ChangeType changeType : changeTypes)
        {
            STEP(String.format("%s Verify action %s for content: %s", CmisWrapper.STEP_PREFIX, changeType, model.getName()));
            isChange = false;
            for (ChangeEvent event : events)
            {
                if (event.getObjectId().equals(lastObjectId))
                {
                    if (changeType == event.getChangeType())
                    {
                        isChange = true;
                        break;
                    }
                }
            }
            Assert.assertTrue(isChange, String.format("Action %s for content: '%s' was found", changeType, model.getName()));
        }
        return cmisAPI();
    }

    /**
     * Verify that a specific object does not have changes from cmis log
     * 
     * @param model {@link ContentModel}
     * @param changeTypes {@link ChangeType}
     * @return
     * @throws Exception
     */
    public CmisWrapper contentModelDoesnotHaveChangesWithWrongToken(ContentModel model, ChangeType... changeTypes) throws Exception
    {
        String token = cmisAPI().getRepositoryInfo().getLatestChangeLogToken();
        if (StringUtils.isEmpty(token))
        {
            throw new TestConfigurationException("Please enable CMIS audit");
        }
        ItemIterable<ChangeEvent> events = cmisAPI().getSession().getContentChanges(token + 1, true);
        String lastObjectId = model.getNodeRef();
        boolean isChange = false;
        for (ChangeType changeType : changeTypes)
        {
            STEP(String.format("%s Verify action %s for content: %s", CmisWrapper.STEP_PREFIX, changeType, model.getName()));
            isChange = false;
            for (ChangeEvent event : events)
            {
                if (event.getObjectId().equals(lastObjectId))
                {
                    if (changeType == event.getChangeType())
                    {
                        isChange = true;
                        break;
                    }
                }
            }
            Assert.assertFalse(isChange, String.format("Action %s for content: '%s' was found", changeType, model.getName()));
        }
        return cmisAPI();
    }

    /**
     * Check if the {@link #getLastResource()} has the list of {@link Action} Example:
     * {code}
     * .hasAllowableActions(Action.CAN_CREATE_FOLDER);
     * {code}
     * 
     * @param actions
     * @return
     */
    public CmisWrapper hasAllowableActions(Action... actions)
    {
        CmisObject cmisObject = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource());
        for (Action action : actions)
        {
            STEP(String.format("%s Verify if object %s has allowable action %s", CmisWrapper.STEP_PREFIX, cmisObject.getName(), action.name()));
            Assert.assertTrue(cmisObject.hasAllowableAction(action), String.format("Object %s does not have action %s", cmisObject.getName(), action.name()));
        }
        return cmisAPI();
    }

    /**
     * Check if {@link #getLastResource()} object has actions returned
     * from {@link org.apache.chemistry.opencmis.client.api.CmisObject#getAllowableActions()}
     * 
     * @param actions
     * @return
     */
    public CmisWrapper isAllowableActionInList(Action... actions)
    {
        List<Action> currentActions = cmisAPI().withCMISUtil().getAllowableActions();
        for (Action action : actions)
        {
            STEP(String.format("%s Verify that action '%s' exists", CmisWrapper.STEP_PREFIX, action.name()));
            Assert.assertTrue(currentActions.contains(action), String.format("Action %s was found", action.name()));
        }
        return cmisAPI();
    }

    /**
     * Check if the {@link #getLastResource()) does not have the list of {@link Action} Example:
     * {code}
     * .doesNotHaveAllowableActions(Action.CAN_CREATE_FOLDER);
     * {code}
     * 
     * @param actions
     * @return
     */
    public CmisWrapper doesNotHaveAllowableActions(Action... actions)
    {
        CmisObject cmisObject = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource());
        for (Action action : actions)
        {
            STEP(String.format("%s Verify if object %s does not have allowable action %s", CmisWrapper.STEP_PREFIX, cmisObject.getName(), action.name()));
            Assert.assertFalse(cmisObject.hasAllowableAction(action), String.format("Object %s does not have action %s", cmisObject.getName(), action.name()));
        }
        return cmisAPI();
    }

    /**
     * Verify document content
     *
     * @param content String expected content
     * @return
     * @throws Exception
     */
    public CmisWrapper contentIs(String content) throws Exception
    {
        STEP(String.format("%s Verify if content '%s' is the expected one", CmisWrapper.STEP_PREFIX, content));
        Assert.assertEquals(cmisAPI().withCMISUtil().getDocumentContent(), content,
                String.format("The content of file %s - is the expected one", cmisAPI().getLastResource()));
        return cmisAPI();
    }

    /**
     * Verify document content contains specific details
     *
     * @param content String expected content
     * @return
     * @throws Exception
     */
    public CmisWrapper contentContains(String content) throws Exception
    {
        STEP(String.format("%s Verify if content '%s' is the expected one", CmisWrapper.STEP_PREFIX, content));
        Assert.assertTrue(cmisAPI().withCMISUtil().getDocumentContent().contains(content),
                String.format("The content of file %s - is the expected one", cmisAPI().getLastResource()));
        return cmisAPI();
    }
    
    /**
     * Verify if current resource has the id given
     *
     * @param id - expected object id
     * @return
     */
    public CmisWrapper objectIdIs(String id)
    {
        CmisObject objSource = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource());
        STEP(String.format("%s Verify if '%s' object has '%s' id", CmisWrapper.STEP_PREFIX, objSource.getName(), id));
        Assert.assertEquals(objSource.getId(), id, "Object has id.");
        return cmisAPI();
    }

    /**
     * Verify the value of the given property
     *
     * @param property - the property id
     * @param value - expected property value
     * @return
     */
    public CmisWrapper contentPropertyHasValue(String property, String value)
    {
        CmisObject objSource = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource());
        STEP(String.format("%s Verify if '%s' property for '%s' content has '%s' value", CmisWrapper.STEP_PREFIX, property, objSource.getName(), value));
        Object propertyValue = objSource.getPropertyValue(property);
        if (propertyValue instanceof ArrayList)
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            ArrayList<String> values = (ArrayList) propertyValue;
            Assert.assertEquals(values.get(0).toString(), value, "Property has value.");
        }
        else
        {
            Assert.assertEquals(propertyValue.toString(), value, "Property has value.");
        }
        return cmisAPI();
    }

    /**
     * Verify if {@link Document} is checked out
     * 
     * @return
     */
    public CmisWrapper documentIsCheckedOut()
    {
        Document document = cmisAPI().withCMISUtil().getCmisDocument(cmisAPI().getLastResource());
        STEP(String.format("%s Verify if document '%s' is checked out", CmisWrapper.STEP_PREFIX, document.getName()));
        Assert.assertTrue(document.isVersionSeriesCheckedOut(), "Document is checkedout");
        return cmisAPI();
    }

    /**
     * Verify if {@link Document} is private working copy (pwc)
     * 
     * @return
     */
    public CmisWrapper isPrivateWorkingCopy()
    {
        Document document = cmisAPI().withCMISUtil().getCmisDocument(cmisAPI().getLastResource());
        STEP(String.format("%s Verify if document '%s' is private working copy", CmisWrapper.STEP_PREFIX, document.getName()));

        // Alfresco supports BindingType.WEBSERVICES for CMIS 1.0
        // (BindingType.ATOMPUB and BindingType.BROWSER for CMIS 1.1)
        // and "cmis:isPrivateWorkingCopy" was introduced with CMIS 1.1.
        //
        // Checking if the document is a pwc through
        // https://chemistry.apache.org/java/javadoc/org/apache/chemistry/opencmis/client/api/DocumentProperties.html#isPrivateWorkingCopy--
        // won't work for BindingType.WEBSERVICES
        //
        // Thus using
        // https://chemistry.apache.org/java/javadoc/org/apache/chemistry/opencmis/client/api/Document.html#isVersionSeriesPrivateWorkingCopy--
        // which is supported in all CMIS versions.
        Assert.assertTrue(cmisAPI().withCMISUtil().isPrivateWorkingCopy());

        return cmisAPI();
    }

    /**
     * Verify that {@link Document} is not private working copy (pwc)
     * 
     * @return
     */
    public CmisWrapper isNotPrivateWorkingCopy()
    {
        Document document = cmisAPI().withCMISUtil().getCmisDocument(cmisAPI().getLastResource());
        STEP(String.format("%s Verify if document '%s' PWC is not private working copy", CmisWrapper.STEP_PREFIX, document.getName()));
        Assert.assertFalse(cmisAPI().withCMISUtil().isPrivateWorkingCopy());
        return cmisAPI();
    }

    /**
     * Verify that {@link Document} is not checked out
     * 
     * @return
     */
    public CmisWrapper documentIsNotCheckedOut()
    {
        Document document = cmisAPI().withCMISUtil().getCmisDocument(cmisAPI().getLastResource());
        STEP(String.format("%s Verify if document '%s' is not checked out", CmisWrapper.STEP_PREFIX, document.getName()));
        Assert.assertFalse(document.isVersionSeriesCheckedOut(), "Document is not checked out");
        return cmisAPI();
    }

    /**
     * Verify if there is a relationship between current resource and the given target
     *
     * @param targetContent
     * @return
     */
    public CmisWrapper objectHasRelationshipWith(ContentModel targetContent)
    {
        OperationContext oc = new OperationContextImpl();
        oc.setIncludeRelationships(IncludeRelationships.SOURCE);

        CmisObject source = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource(), oc);
        CmisObject target = cmisAPI().withCMISUtil().getCmisObject(targetContent.getCmisLocation());

        STEP(String.format("%s Verify if source '%s' has relationship with '%s'", CmisWrapper.STEP_PREFIX, source.getName(), target.getName()));
        List<String> relTargetIds = new ArrayList<>();
        for (Relationship rel : source.getRelationships())
        {
            relTargetIds.add(rel.getTarget().getId());
        }
        Assert.assertTrue(relTargetIds.contains(target.getId()),
                String.format("Relationship is created between source '%s' and target '%s'.", source.getName(), target.getName()));
        return cmisAPI();
    }

    /**
     * Verify document has version
     *
     * @param version String expected version
     * @return
     * @throws Exception
     */
    public CmisWrapper documentHasVersion(double version) throws Exception
    {
        Document document = cmisAPI().withCMISUtil().getCmisDocument(cmisAPI().getLastResource());
        document.refresh();
        STEP(String.format("%s Verify if document '%s' has version '%s'", CmisWrapper.STEP_PREFIX, document.getName(), version));
        Assert.assertEquals(Double.parseDouble(document.getVersionLabel()), version, "File has version");
        return cmisAPI();
    }

    /**
     * Verify parent from the {@link Folder} set as last resource
     * 
     * @param contentModel
     * @return
     */
    public CmisWrapper folderHasParent(ContentModel contentModel)
    {
        STEP(String.format("%s Verify folder %s has parent %s", CmisWrapper.STEP_PREFIX, cmisAPI().getLastResource(), contentModel.getName()));
        Assert.assertEquals(cmisAPI().withCMISUtil().getFolderParent().getName(), contentModel.getName(), "Folder name is not the expected one");
        return cmisAPI();
    }

    /**
     * Verify base type id
     *
     * @param baseTypeId String expected object type value
     * @return
     * @throws Exception
     */
    public CmisWrapper baseTypeIdIs(String baseTypeId) throws Exception
    {
        STEP(String.format("%s Verify if base object type '%s' is the expected one", CmisWrapper.STEP_PREFIX, baseTypeId));
        String actualBaseTypeIdValue = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource()).getType().getBaseTypeId().value();
        Assert.assertEquals(actualBaseTypeIdValue, baseTypeId, "Object type is the expected one");
        return cmisAPI();
    }

    /**
     * Verify object type id
     *
     * @param objectTypeId String expected object type value
     * @return
     * @throws Exception
     */
    public CmisWrapper objectTypeIdIs(String objectTypeId) throws Exception
    {
        STEP(String.format("%s Verify if object type id '%s' is the expected one", CmisWrapper.STEP_PREFIX, objectTypeId));
        String typeId = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource()).getType().getId();
        if (StringUtils.isEmpty(typeId))
        {
            typeId = "";
        }
        Assert.assertEquals(cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource()).getType().getId(), objectTypeId,
                "Object type id is the expected one");
        return cmisAPI();
    }

    /**
     * Verify a specific object property
     * 
     * @param propertyId
     * @param value
     * @return
     */
    public CmisWrapper objectHasProperty(String propertyId, Object value)
    {
        CmisObject cmisObject = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource());
        STEP(String.format("%s Verify if object %s has property %s ", CmisWrapper.STEP_PREFIX, cmisObject.getName(), propertyId));
        Property<?> property = cmisAPI().withCMISUtil().getProperty(propertyId);
        Object propValue = property.getValue();
        if(propValue instanceof GregorianCalendar)
        {
            Date date = (Date) value;
            long longDate = date.getTime();
            long actualDate = ((GregorianCalendar) propValue).getTimeInMillis();
            Assert.assertEquals(actualDate, longDate);
        }
        else
        {
            if (propValue == null)
            {
                propValue = "";
            }
            Assert.assertEquals(property.getValue().toString(), value.toString(), String.format("Found property value %s", value));
        }
        return cmisAPI();
    }

    /**
     * Check if CMIS object contains a property.
     * Example:
     * ...assertObjectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids","secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds",
     * "P:cm:titled", "P:sys:localized");
     * 
     * @param propertyId
     * @param displayName
     * @param localName
     * @param queryName
     * @param values
     * @return
     */
    public CmisWrapper objectHasProperty(String propertyId, String displayName, String localName, String queryName, String... values)
    {
        CmisObject cmisObject = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource());
        STEP(String.format("%s Verify if object %s has property %s ", CmisWrapper.STEP_PREFIX, cmisObject.getName(), propertyId));
        Property<?> property = cmisAPI().withCMISUtil().getProperty(propertyId);
        if (property != null)
        {
            Assert.assertEquals(property.getDisplayName(), displayName, "Property displayName");
            Assert.assertEquals(property.getLocalName(), localName, "Property localName");
            Assert.assertEquals(property.getQueryName(), queryName, "Property queryName");
            for (String value : values)
            {
                Assert.assertTrue(property.getValues().contains(value), "Property value");
            }
        }
        else
        {
            throw new AssertionError(String.format("Object %s does not have property %s", cmisObject.getName(), propertyId));
        }
        return cmisAPI();
    }

    /**
     * Assert if the {@link #getLastResource()) has the latest major version set
     */
    public CmisWrapper isLatestMajorVersion()
    {
        String path = cmisAPI().getLastResource();
        STEP(String.format("%s Verify that document from '%s' is latest major version", CmisWrapper.STEP_PREFIX, path));
        Assert.assertTrue(cmisAPI().withCMISUtil().getCmisDocument(path).isLatestMajorVersion(), String.format("Document from %s is last major version", path));
        return cmisAPI();
    }

    /**
     * Verify that {@link Document} is not latest major version.
     * 
     * @return
     */
    public CmisWrapper isNotLatestMajorVersion()
    {
        String path = cmisAPI().getLastResource();
        STEP(String.format("%s Verify that document from '%s' is not latest major version", CmisWrapper.STEP_PREFIX, path));
        Assert.assertFalse(cmisAPI().withCMISUtil().getCmisDocument(path).isLatestMajorVersion(),
                String.format("Document from %s is last major version", path));
        return cmisAPI();
    }

    /**
     * Verify that renditions are available
     */
    public CmisWrapper renditionIsAvailable()
    {
        STEP(String.format("%s Verify if renditions are available for %s", CmisWrapper.STEP_PREFIX, cmisAPI().getLastResource()));
        List<Rendition> renditions = cmisAPI().withCMISUtil().getRenditions();
        Assert.assertTrue(renditions != null && !renditions.isEmpty());
        return cmisAPI();
    }

    /**
     * Verify that thumbnail rendition is available
     * 
     * @return
     */
    public CmisWrapper thumbnailRenditionIsAvailable()
    {
        boolean found = false;
        STEP(String.format("%s Verify if thumbnail rendition is available for %s", CmisWrapper.STEP_PREFIX, cmisAPI().getLastResource()));
        List<Rendition> renditions = cmisAPI().withCMISUtil().getRenditions();
        for (Rendition rendition : renditions)
        {
            if (rendition.getKind().equals("cmis:thumbnail"))
            {
                found = true;
            }
        }
        Assert.assertTrue(found, String.format("Thumbnail rendition found for", cmisAPI().getLastResource()));
        return cmisAPI();
    }

    private boolean isSecondaryTypeAvailable(String secondaryTypeId)
    {
        boolean found = false;
        List<SecondaryType> secondaryTypes = cmisAPI().withCMISUtil().getSecondaryTypes();
        for (SecondaryType type : secondaryTypes)
        {
            if (type.getId().equals(secondaryTypeId))
            {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Verify secondary type for specific {@link CmisObject}
     * 
     * @param secondaryTypeId
     * @return
     */
    public CmisWrapper secondaryTypeIsAvailable(String secondaryTypeId)
    {
        STEP(String.format("%s Verify if '%s' secondary type is available for '%s'", CmisWrapper.STEP_PREFIX, secondaryTypeId, 
                    new File(cmisAPI().getLastResource()).getName()));
        Assert.assertTrue(isSecondaryTypeAvailable(secondaryTypeId), String.format("%s is available for %s", secondaryTypeId, cmisAPI().getLastResource()));
        return cmisAPI();
    }

    /**
     * Verify secondary type is not available for specific {@link CmisObject}
     * 
     * @param secondaryTypeId
     * @return
     */
    public CmisWrapper secondaryTypeIsNotAvailable(String secondaryTypeId)
    {
        STEP(String.format("%s Verify if '%s' aspect is NOT available for %s", CmisWrapper.STEP_PREFIX, secondaryTypeId, cmisAPI().getLastResource()));
        Assert.assertFalse(isSecondaryTypeAvailable(secondaryTypeId),
                String.format("%s is NOT available for %s", secondaryTypeId, cmisAPI().getLastResource()));
        return cmisAPI();
    }

    /**
     * Verify document content length
     * 
     * @param contentLength String expected content length
     * @return
     * @throws Exception
     */
    public CmisWrapper contentLengthIs(long contentLength) throws Exception
    {
        STEP(String.format("%s Verify if content length '%s' is the expected one", CmisWrapper.STEP_PREFIX, contentLength));
        Document lastVersion = cmisAPI().withCMISUtil().getCmisDocument(cmisAPI().getLastResource());
        lastVersion.refresh();
        Assert.assertEquals(lastVersion.getContentStreamLength(), contentLength, "File content is the expected one");
        return cmisAPI();
    }

    /**
     * e.g. assertFolderHasDescendant(1, file1) will verify if file1 is a direct descendant of {@link #getLastResource()}
     *
     * @param depth {@link #getFolderDescendants(int)}
     * @param contentModels {@link #getCmisObjectsFromContentModels(ContentModel...)}
     */
    public void hasDescendants(int depth, ContentModel... contentModels)
    {
        STEP(String.format("%s Assert that folder %s has descendants in depth %d:", STEP_PREFIX, getProtocol().getLastResource(), depth));
        CmisObject currentCmisObject = getProtocol().withCMISUtil().getCmisObject(getProtocol().getLastResource());
        List<CmisObject> cmisObjects = getProtocol().withCMISUtil().getCmisObjectsFromContentModels(contentModels);
        List<CmisObject> folderDescendants = getProtocol().withCMISUtil().getFolderDescendants(depth);
        for (CmisObject cmisObject : cmisObjects)
        {
            boolean found = false;
            STEP(String.format("%s Verify that folder '%s' has descendant %s", CmisWrapper.STEP_PREFIX, currentCmisObject.getName(), cmisObject.getName()));
            for (CmisObject folderDescendant : folderDescendants)
                if (folderDescendant.getId().equals(cmisObject.getId()))
                {
                    found = true;
                    break;
                }
            Assert.assertTrue(found, String.format("Folder %s does not have descendant %s", currentCmisObject.getName(), cmisObject));
        }
    }

    public void doesNotHaveDescendants(int depth)
    {
        STEP(String.format("%s Assert that folder %s does not have descendants in depth %d:", STEP_PREFIX, getProtocol().getLastResource(), depth));
        CmisObject currentCmisObject = getProtocol().withCMISUtil().getCmisObject(getProtocol().getLastResource());
        List<CmisObject> folderDescendants = getProtocol().withCMISUtil().getFolderDescendants(depth);
        Assert.assertTrue(folderDescendants.isEmpty(), String.format("Folder %s should not have descendants", currentCmisObject.getName()));
    }

    /**
     * Verify that {@link CmisObject} has ACLs (Access Control Lists)
     * 
     * @return
     */
    public CmisWrapper hasAcls()
    {
        STEP(String.format("%s Verify that %s has acls", CmisWrapper.STEP_PREFIX, cmisAPI().getLastResource()));
        String path = cmisAPI().getLastResource();
        STEP(String.format("%s Get Acls for %s", CmisWrapper.STEP_PREFIX, path));
        Assert.assertNotNull(cmisAPI().withCMISUtil().getAcls(), String.format("Acls found for %s", path));
        return cmisAPI();
    }

    /**
     * Depending on the specified depth, checks that all the contents from contentModels list are present in the current folder tree structure
     * 
     * @param depth the depth of the tree to check, must be -1 or >= 1
     * @param contentModels expected list of contents to be found in the tree
     * @return
     */
    public CmisWrapper hasFolderTree(int depth, ContentModel... contentModels)
    {
        CmisObject currentCmisObject = getProtocol().withCMISUtil().getCmisObject(getProtocol().getLastResource());
        List<CmisObject> cmisObjects = getProtocol().withCMISUtil().getCmisObjectsFromContentModels(contentModels);
        List<CmisObject> folderDescendants = getProtocol().withCMISUtil().getFolderTree(depth);
        for (CmisObject cmisObject : cmisObjects)
        {
            boolean found = false;
            STEP(String.format("%s Verify that folder '%s' has folder tree %s", CmisWrapper.STEP_PREFIX, currentCmisObject.getName(), cmisObject.getName()));
            for (CmisObject folderDescendant : folderDescendants)
                if (folderDescendant.getId().equals(cmisObject.getId()))
                    found = true;
            Assert.assertTrue(found, String.format("Folder %s does not have folder tree %s", currentCmisObject.getName(), cmisObject));
        }
        return cmisAPI();
    }

    /**
     * Verify the permission for a specific user from the last resource object
     * 
     * @param userModel {@link UserModel} user to verify
     * @param role {@link UserRole} user role to verify
     * @return
     */
    public CmisWrapper permissionIsSetForUser(UserModel userModel, UserRole role)
    {
        STEP(String.format("%s Verify that user %s has role %s set to content %s", CmisWrapper.STEP_PREFIX, userModel.getUsername(), role.name(),
                cmisAPI().getLastResource()));
        Assert.assertTrue(checkPermission(userModel.getUsername(), role.getRoleId()),
                String.format("User %s has permission %s", userModel.getUsername(), role.name()));
        return cmisAPI();
    }

    /**
     * Verify the permission for a specific group of users from the last resource object
     * 
     * @param groupModel {@link GroupModel} group to verify
     * @param role {@link UserRole} user role to verify
     * @return
     */
    public CmisWrapper permissionIsSetForGrup(GroupModel groupModel, UserRole role)
    {
        STEP(String.format("%s Verify that user %s has role %s set to content %s", CmisWrapper.STEP_PREFIX, groupModel.getDisplayName(), role.name(),
                cmisAPI().getLastResource()));
        Assert.assertTrue(checkPermission(groupModel.getDisplayName(), role.getRoleId()),
                String.format("User %s has permission %s", groupModel.getDisplayName(), role.name()));
        return cmisAPI();
    }

    private boolean checkPermission(String user, String permission)
    {
        Acl acl = cmisAPI().withCMISUtil().getAcls();
        if (acl == null)
        {
            throw new CmisRuntimeException(String.format("No acls returned for '%s'", cmisAPI().getLastResource()));
        }
        List<Ace> aces = acl.getAces();
        boolean found = false;
        for (Ace ace : aces)
        {
            if (ace.getPrincipalId().equals(user) && ace.getPermissions().get(0).equals(permission))
            {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Verify the permission for a specific user from the last resource object
     * 
     * @param userModel {@link UserModel}
     * @param permission to verify
     * @return
     */
    public CmisWrapper permissionIsSetForUser(UserModel userModel, String permission)
    {
        STEP(String.format("%s Verify that user %s has role %s set to content %s", CmisWrapper.STEP_PREFIX, userModel.getUsername(), permission,
                cmisAPI().getLastResource()));
        Assert.assertTrue(checkPermission(userModel.getUsername(), permission),
                String.format("User %s has permission %s", userModel.getUsername(), permission));
        return cmisAPI();
    }

    /**
     * Verify that permission is not set for a specific user from the last resource object
     * 
     * @param userModel {@link UserModel} user to verify
     * @param role {@link UserRole} user role to verify
     * @return
     */
    public CmisWrapper permissionIsNotSetForUser(UserModel userModel, UserRole role)
    {
        STEP(String.format("%s Verify that user %s doesn't have role %s set to content %s", CmisWrapper.STEP_PREFIX, userModel.getUsername(), role.name(),
                cmisAPI().getLastResource()));
        Assert.assertFalse(checkPermission(userModel.getUsername(), role.getRoleId()),
                String.format("User %s has permission %s", userModel.getUsername(), role.name()));
        return cmisAPI();
    }

    /**
     * Verify that permission is not set for a specific user from the last resource object
     * 
     * @param userModel {@link UserModel} user to verify
     * @param permission to verify
     * @return
     */
    public CmisWrapper permissionIsNotSetForUser(UserModel userModel, String permission)
    {
        STEP(String.format("%s Verify that user %s doesn't have permission %s set to content %s", CmisWrapper.STEP_PREFIX, userModel.getUsername(), permission,
                cmisAPI().getLastResource()));
        Assert.assertFalse(checkPermission(userModel.getUsername(), permission),
                String.format("User %s has permission %s", userModel.getUsername(), permission));
        return cmisAPI();
    }

    public CmisWrapper typeDefinitionIs(ContentModel contentModel)
    {
        CmisObject cmisObject = cmisAPI().withCMISUtil().getCmisObject(contentModel.getCmisLocation());
        STEP(String.format("%s Verify that object '%s' type definition matches '%s' type definition", CmisWrapper.STEP_PREFIX, cmisObject.getName(),
                cmisAPI().withCMISUtil().getTypeDefinition().getId()));
        Assert.assertTrue(cmisAPI().withCMISUtil().getTypeDefinition().equals(cmisObject.getType()), String.format(
                "Object '%s' type definition does not match '%s' type definition", cmisObject.getName(), cmisAPI().withCMISUtil().getTypeDefinition().getId()));
        return cmisAPI();
    }

    /**
     * Verify that a specific folder(set by calling {@link org.alfresco.cmis.CmisWrapper#usingResource(ContentModel)})
     * contains checked out documents
     * 
     * @param contentModels checked out documents to verify
     * @return
     */
    public CmisWrapper folderHasCheckedOutDocument(ContentModel... contentModels)
    {
        List<CmisObject> cmisObjectList = cmisAPI().withCMISUtil().getCmisObjectsFromContentModels(contentModels);
        List<Document> cmisCheckedOutDocuments = cmisAPI().withCMISUtil().getCheckedOutDocumentsFromFolder();
        for (CmisObject cmisObject : cmisObjectList)
        {
            Assert.assertTrue(cmisAPI().withCMISUtil().isCmisObjectContainedInCmisCheckedOutDocumentsList(cmisObject, cmisCheckedOutDocuments),
                    String.format("Folder %s does not contain checked out document %s", cmisAPI().getLastResource(), cmisObject));
        }
        return cmisAPI();
    }

    /**
     * Verify that a specific folder(set by calling {@link org.alfresco.cmis.CmisWrapper#usingResource(ContentModel)})
     * contains checked out documents in a specific order.
     * 
     * @param context {@link OperationContext}
     * @param contentModels documents to verify in the order returned by the {@link OperationContext}
     * @return
     */
    public CmisWrapper folderHasCheckedOutDocument(OperationContext context, ContentModel... contentModels)
    {
        List<CmisObject> cmisObjectList = cmisAPI().withCMISUtil().getCmisObjectsFromContentModels(contentModels);
        List<Document> cmisCheckedOutDocuments = cmisAPI().withCMISUtil().getCheckedOutDocumentsFromFolder(context);
        for (int i = 0; i < cmisObjectList.size(); i++)
        {
            Assert.assertEquals(cmisObjectList.get(i).getId().split(";")[0], cmisCheckedOutDocuments.get(i).getId().split(";")[0],
                    String.format("Folder %s does not contain checked out document %s", cmisAPI().getLastResource(), cmisObjectList.get(i).getName()));
        }
        return cmisAPI();
    }

    /**
     * Verify checked out documents from {@link Session}
     * 
     * @param contentModels documents to verify
     * @return
     */
    public CmisWrapper sessionHasCheckedOutDocument(ContentModel... contentModels)
    {
        List<CmisObject> cmisObjectList = cmisAPI().withCMISUtil().getCmisObjectsFromContentModels(contentModels);
        List<Document> cmisCheckedOutDocuments = cmisAPI().withCMISUtil().getCheckedOutDocumentsFromSession();
        for (CmisObject cmisObject : cmisObjectList)
        {
            Assert.assertTrue(cmisAPI().withCMISUtil().isCmisObjectContainedInCmisCheckedOutDocumentsList(cmisObject, cmisCheckedOutDocuments),
                    String.format("Session does not contain checked out document %s", cmisObject));
        }
        return cmisAPI();
    }

    /**
     * Verify checked out documents from {@link Session} in a specific order set in {@link OperationContext}
     * 
     * @param context {@link OperationContext}
     * @param contentModels documents to verify
     * @return
     */
    public CmisWrapper sessionHasCheckedOutDocument(OperationContext context, ContentModel... contentModels)
    {
        List<CmisObject> cmisObjectList = cmisAPI().withCMISUtil().getCmisObjectsFromContentModels(contentModels);
        List<Document> cmisCheckedOutDocuments = cmisAPI().withCMISUtil().getCheckedOutDocumentsFromSession(context);
        for (int i = 0; i < cmisObjectList.size(); i++)
        {
            Assert.assertEquals(cmisObjectList.get(i).getId().split(";")[0], cmisCheckedOutDocuments.get(i).getId().split(";")[0],
                    String.format("Session does not contain checked out document %s", cmisObjectList.get(i).getName()));
        }
        return cmisAPI();
    }

    /**
     * Verify that checked out documents are not found in {@link Session}
     * 
     * @param contentModels documents to verify
     * @return
     */
    public CmisWrapper sessioDoesNotHaveCheckedOutDocument(ContentModel... contentModels)
    {
        List<CmisObject> cmisObjectList = cmisAPI().withCMISUtil().getCmisObjectsFromContentModels(contentModels);
        List<Document> cmisCheckedOutDocuments = cmisAPI().withCMISUtil().getCheckedOutDocumentsFromSession();
        for (CmisObject cmisObject : cmisObjectList)
        {
            Assert.assertFalse(cmisAPI().withCMISUtil().isCmisObjectContainedInCmisCheckedOutDocumentsList(cmisObject, cmisCheckedOutDocuments),
                    String.format("Session does contain checked out document %s", cmisObject));
        }
        return cmisAPI();
    }

    /**
     * Verify that {@link CmisObject} has a specific aspect extension
     * 
     * @param aspectId
     * @return
     */
    public CmisWrapper hasAspectExtension(String aspectId)
    {
        STEP(String.format("Verify that aspect %s is applied to %s", aspectId, cmisAPI().getLastResource()));
        boolean found = false;
        List<CmisExtensionElement> extensions = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource()).getExtensions(ExtensionLevel.PROPERTIES);
        for (CmisExtensionElement extElement : extensions)
        {
            if (extElement.getName().equals("aspects"))
            {
                List<CmisExtensionElement> aspects = extElement.getChildren();
                for (CmisExtensionElement aspect : aspects)
                {
                    if (aspect.getValue() != null)
                    {
                        if (aspect.getValue().equals(aspectId))
                        {
                            found = true;
                        }
                    }
                }
            }
        }
        Assert.assertTrue(found, String.format("Aspect extension %s for %s was found", aspectId, cmisAPI().getLastResource()));
        return cmisAPI();
    }

    /**
     * Verify the parents for a {@link CmisObject}
     * 
     * @param parentsList
     * @return
     */
    public CmisWrapper hasParents(String... parentsList)
    {
        List<String> folderNames = new ArrayList<>();
        List<Folder> parents = new ArrayList<>();
        String source = cmisAPI().getLastResource();
        CmisObject objSource = cmisAPI().withCMISUtil().getCmisObject(source);
        STEP(String.format("%s Verify the parents for '%s'.", CmisWrapper.STEP_PREFIX, objSource.getName()));
        if (objSource instanceof Document)
        {
            Document d = (Document) objSource;
            parents = d.getParents();
        }
        else if (objSource instanceof Folder)
        {
            Folder f = (Folder) objSource;
            parents = f.getParents();
        }
        for (Folder folder : parents)
        {
            folderNames.add(folder.getName());
        }
        Assert.assertEqualsNoOrder(folderNames.toArray(), parentsList, "Parents list is the expected one.");
        return cmisAPI();
    }

    public CmisWrapper descriptionIs(String description)
    {
        String source = cmisAPI().getLastResource();
        STEP(String.format("%s Verify object '%s' description is '%s'", CmisWrapper.STEP_PREFIX, source, description));
        CmisObject cmisObject = cmisAPI().withCMISUtil().getCmisObject(source);
        Assert.assertEquals(description, cmisObject.getDescription());
        return cmisAPI();
    }

    /**
     * Verify if folder children exist in parent folder
     * 
     * @param fileModel children files
     * @return
     * @throws Exception
     */
    public CmisWrapper hasFolders(FolderModel... folderModel) throws Exception
    {
        String currentSpace = cmisAPI().getCurrentSpace();
        List<FolderModel> folders = cmisAPI().getFolders();
        for (FolderModel folder : folderModel)
        {
            STEP(String.format("%s Verify that folder %s is in %s", CmisWrapper.STEP_PREFIX, folder.getName(), currentSpace));
            Assert.assertTrue(cmisAPI().withCMISUtil().isFolderInList(folder, folders), String.format("Folder %s is in %s", folder.getName(), currentSpace));
        }
        return cmisAPI();
    }

    /**
     * Verify if file children exist in parent folder
     * 
     * @param fileModel children files
     * @return
     * @throws Exception
     */
    public CmisWrapper hasFiles(FileModel... fileModel) throws Exception
    {
        String currentSpace = cmisAPI().getLastResource();
        List<FileModel> files = cmisAPI().getFiles();
        for (FileModel file : fileModel)
        {
            STEP(String.format("%s Verify that file '%s' is in '%s'", CmisWrapper.STEP_PREFIX, file.getName(), currentSpace));
            Assert.assertTrue(cmisAPI().withCMISUtil().isFileInList(file, files), String.format("File %s is in %s", file.getName(), currentSpace));
        }
        return cmisAPI();
    }

    /**
     * Verify if file(s) children exist in parent folder
     *
     * @param fileModels children files
     * @return
     * @throws Exception
     */
    public CmisWrapper doesNotHaveFile(FileModel... fileModels) throws Exception
    {
        String currentSpace = cmisAPI().getLastResource();
        List<FileModel> files = cmisAPI().getFiles();
        for (FileModel fileModel : fileModels)
        {
            STEP(String.format("%s Verify that file '%s' is not in '%s'", CmisWrapper.STEP_PREFIX, fileModel.getName(), currentSpace));
            Assert.assertFalse(cmisAPI().withCMISUtil().isFileInList(fileModel, files), String.format("File %s is in %s", fileModel.getName(), currentSpace));
        }
        return cmisAPI();
    }

    /**
     * Verify if folder(s) children exist in parent folder
     *
     * @param folderModels children files
     * @return
     * @throws Exception
     */
    public CmisWrapper doesNotHaveFolder(FolderModel... folderModels) throws Exception
    {
        String currentSpace = cmisAPI().getLastResource();
        List<FolderModel> folders = cmisAPI().getFolders();
        for (FolderModel folderModel : folderModels)
        {
            STEP(String.format("%s Verify that folder '%s' is not in '%s'", CmisWrapper.STEP_PREFIX, folderModel.getName(), currentSpace));
            Assert.assertFalse(cmisAPI().withCMISUtil().isFolderInList(folderModel, folders),
                    String.format("File %s is in %s", folderModel.getName(), currentSpace));
        }
        return cmisAPI();
    }

    /**
     * Verify the children(files and folders) from a parent folder
     * 
     * @param contentModel children
     * @return
     * @throws Exception
     */
    public CmisWrapper hasChildren(ContentModel... contentModel) throws Exception
    {
        String currentSpace = cmisAPI().getCurrentSpace();
        Map<ContentModel, ObjectType> mapContents = cmisAPI().withCMISUtil().getChildren();
        List<ContentModel> contents = new ArrayList<ContentModel>();
        for (Map.Entry<ContentModel, ObjectType> entry : mapContents.entrySet())
        {
            contents.add(entry.getKey());
        }
        for (ContentModel content : contentModel)
        {
            STEP(String.format("%s Verify that file %s is in %s", CmisWrapper.STEP_PREFIX, content.getName(), currentSpace));
            Assert.assertTrue(cmisAPI().withCMISUtil().isContentInList(content, contents),
                    String.format("Content %s is in %s", content.getName(), currentSpace));
        }
        return cmisAPI();
    }
    
    
    public CmisWrapper hasUniqueChildren(int numberOfChildren) throws Exception
    {
        STEP(String.format("%s Verify that current folder has %d unique children", CmisWrapper.STEP_PREFIX, numberOfChildren));
        Map<ContentModel, ObjectType> mapContents = cmisAPI().withCMISUtil().getChildren();

        Set<String> documentIds = new HashSet<String>();
        for (ContentModel key : mapContents.keySet())
        {
         documentIds.add(key.getName());
        }
        Assert.assertTrue(numberOfChildren==documentIds.size(), String.format("Current folder contains %d unique children, but expected is %d", documentIds.size(), numberOfChildren));
        return cmisAPI();
    }

    /**
     * Get check in comment for last document version
     * 
     * @param comment to verify
     * @return
     */
    public CmisWrapper hasCheckInCommentLastVersion(String comment)
    {
        String source = cmisAPI().getLastResource();
        STEP(String.format("%s Verify check in comment for last version of %s", CmisWrapper.STEP_PREFIX, source));
        Document document = cmisAPI().withCMISUtil().getCmisDocument(source);
        Assert.assertEquals(comment, document.getCheckinComment(), String.format("Document %s has check in comment %s", document.getName(), comment));
        return cmisAPI();
    }

    /**
     * Get check in comment for a specific document version
     * 
     * @param documentVersion version of document
     * @param comment to verify
     * @return
     */
    public CmisWrapper hasCheckInCommentForVersion(double documentVersion, String comment)
    {
        String source = cmisAPI().getLastResource();
        STEP(String.format("%s Verify check in comment for version %s of %s", CmisWrapper.STEP_PREFIX, documentVersion, source));
        String documentId = cmisAPI().withCMISUtil().getObjectId(source).split(";")[0];
        documentId = documentId + ";" + documentVersion;
        Document document = (Document) cmisAPI().withCMISUtil().getCmisObjectById(documentId);
        Assert.assertEquals(comment, document.getCheckinComment(), String.format("Document %s has check in comment %s", document.getName(), comment));
        return cmisAPI();
    }

    /**
     * Verify failed deleted objects after delete tree action
     * 
     * @param nodeRef objects to verify
     * @return
     */
    public CmisWrapper hasFailedDeletedObject(String nodeRef)
    {
        STEP(String.format("%s Verify failed deleted object from %s", CmisWrapper.STEP_PREFIX, cmisAPI().getLastResource()));
        Assert.assertTrue(cmisAPI().deleteTreeFailedObjects.contains(nodeRef), String.format("Object %s found after delete", nodeRef));
        return cmisAPI();
    }

    /**
     * Verify if there is a relationship between current resource and the given target
     *
     * @param user
     * @return
     */
    public CmisWrapper userIsAssigned(UserModel user)
    {
        OperationContext oc = new OperationContextImpl();
        oc.setIncludeRelationships(IncludeRelationships.SOURCE);
        CmisObject source = cmisAPI().withCMISUtil().getCmisObject(cmisAPI().getLastResource(), oc);
        String userNodeRef = cmisAPI().withCMISUtil().getUserNodeRef(user);

        STEP(String.format("%s Verify if user '%s' has relationship with '%s'", CmisWrapper.STEP_PREFIX, user.getUsername(), source.getName()));
        List<String> relTargetIds = new ArrayList<>();
        for (Relationship rel : source.getRelationships())
        {
            relTargetIds.add(rel.getTarget().getId());
        }
        Assert.assertTrue(relTargetIds.contains(userNodeRef),
                String.format("Relationship is created between source '%s' and target '%s'.", source.getName(), user.getUsername()));
        return cmisAPI();
    }
}
