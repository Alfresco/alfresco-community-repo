
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.alfresco.repo.cmis.ws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _UpdatePropertiesChangeToken_QNAME = new QName("http://www.cmis.org/2008/05", "changeToken");
    private final static QName _CreatePolicyFolderId_QNAME = new QName("http://www.cmis.org/2008/05", "folderId");
    private final static QName _CreateDocumentVersioningState_QNAME = new QName("http://www.cmis.org/2008/05", "versioningState");
    private final static QName _CreateDocumentContentStream_QNAME = new QName("http://www.cmis.org/2008/05", "contentStream");
    private final static QName _GetAllVersionsIncludeAllowableActions_QNAME = new QName("http://www.cmis.org/2008/05", "includeAllowableActions");
    private final static QName _GetAllVersionsFilter_QNAME = new QName("http://www.cmis.org/2008/05", "filter");
    private final static QName _GetAllVersionsIncludeRelationships_QNAME = new QName("http://www.cmis.org/2008/05", "includeRelationships");
    private final static QName _GetRelationshipsSkipCount_QNAME = new QName("http://www.cmis.org/2008/05", "skipCount");
    private final static QName _GetRelationshipsTypeId_QNAME = new QName("http://www.cmis.org/2008/05", "typeId");
    private final static QName _GetRelationshipsIncludeSubRelationshipTypes_QNAME = new QName("http://www.cmis.org/2008/05", "includeSubRelationshipTypes");
    private final static QName _GetRelationshipsDirection_QNAME = new QName("http://www.cmis.org/2008/05", "direction");
    private final static QName _GetRelationshipsMaxItems_QNAME = new QName("http://www.cmis.org/2008/05", "maxItems");
    private final static QName _PropertyStringDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyStringDefinition");
    private final static QName _PropertyXml_QNAME = new QName("http://www.cmis.org/2008/05", "propertyXml");
    private final static QName _ChoiceDateTime_QNAME = new QName("http://www.cmis.org/2008/05", "choiceDateTime");
    private final static QName _Terminator_QNAME = new QName("http://www.cmis.org/2008/05", "terminator");
    private final static QName _Object_QNAME = new QName("http://www.cmis.org/2008/05", "object");
    private final static QName _ObjectNotFoundException_QNAME = new QName("http://www.cmis.org/2008/05", "objectNotFoundException");
    private final static QName _HasMoreItems_QNAME = new QName("http://www.cmis.org/2008/05", "hasMoreItems");
    private final static QName _ChoiceBoolean_QNAME = new QName("http://www.cmis.org/2008/05", "choiceBoolean");
    private final static QName _PropertyDecimal_QNAME = new QName("http://www.cmis.org/2008/05", "propertyDecimal");
    private final static QName _PropertyDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyDefinition");
    private final static QName _ChoiceHtml_QNAME = new QName("http://www.cmis.org/2008/05", "choiceHtml");
    private final static QName _ConstraintViolationException_QNAME = new QName("http://www.cmis.org/2008/05", "constraintViolationException");
    private final static QName _OperationNotSupportedException_QNAME = new QName("http://www.cmis.org/2008/05", "operationNotSupportedException");
    private final static QName _TypeNotFoundException_QNAME = new QName("http://www.cmis.org/2008/05", "typeNotFoundException");
    private final static QName _FolderNotValidException_QNAME = new QName("http://www.cmis.org/2008/05", "folderNotValidException");
    private final static QName _DocumentType_QNAME = new QName("http://www.cmis.org/2008/05", "documentType");
    private final static QName _Type_QNAME = new QName("http://www.cmis.org/2008/05", "type");
    private final static QName _ChoiceInteger_QNAME = new QName("http://www.cmis.org/2008/05", "choiceInteger");
    private final static QName _InvalidArgumentException_QNAME = new QName("http://www.cmis.org/2008/05", "invalidArgumentException");
    private final static QName _OffsetException_QNAME = new QName("http://www.cmis.org/2008/05", "offsetException");
    private final static QName _StorageException_QNAME = new QName("http://www.cmis.org/2008/05", "storageException");
    private final static QName _ChoiceDecimal_QNAME = new QName("http://www.cmis.org/2008/05", "choiceDecimal");
    private final static QName _RelationshipType_QNAME = new QName("http://www.cmis.org/2008/05", "relationshipType");
    private final static QName _AllowableActions_QNAME = new QName("http://www.cmis.org/2008/05", "allowableActions");
    private final static QName _ChoiceString_QNAME = new QName("http://www.cmis.org/2008/05", "choiceString");
    private final static QName _FolderType_QNAME = new QName("http://www.cmis.org/2008/05", "folderType");
    private final static QName _VersioningException_QNAME = new QName("http://www.cmis.org/2008/05", "versioningException");
    private final static QName _ChoiceId_QNAME = new QName("http://www.cmis.org/2008/05", "choiceId");
    private final static QName _Query_QNAME = new QName("http://www.cmis.org/2008/05", "query");
    private final static QName _ChoiceUri_QNAME = new QName("http://www.cmis.org/2008/05", "choiceUri");
    private final static QName _PropertyBoolean_QNAME = new QName("http://www.cmis.org/2008/05", "propertyBoolean");
    private final static QName _PropertyDecimalDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyDecimalDefinition");
    private final static QName _PolicyType_QNAME = new QName("http://www.cmis.org/2008/05", "policyType");
    private final static QName _PropertyInteger_QNAME = new QName("http://www.cmis.org/2008/05", "propertyInteger");
    private final static QName _PropertyBooleanDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyBooleanDefinition");
    private final static QName _PropertyString_QNAME = new QName("http://www.cmis.org/2008/05", "propertyString");
    private final static QName _PropertyIdDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyIdDefinition");
    private final static QName _GetRepositoryInfoResponse_QNAME = new QName("http://www.cmis.org/2008/05", "getRepositoryInfoResponse");
    private final static QName _PropertyUriDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyUriDefinition");
    private final static QName _PropertyDateTime_QNAME = new QName("http://www.cmis.org/2008/05", "propertyDateTime");
    private final static QName _RepositoryInfo_QNAME = new QName("http://www.cmis.org/2008/05", "repositoryInfo");
    private final static QName _ContentAlreadyExistsException_QNAME = new QName("http://www.cmis.org/2008/05", "contentAlreadyExistsException");
    private final static QName _UpdateConflictException_QNAME = new QName("http://www.cmis.org/2008/05", "updateConflictException");
    private final static QName _Choice_QNAME = new QName("http://www.cmis.org/2008/05", "choice");
    private final static QName _PropertyXmlDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyXmlDefinition");
    private final static QName _PermissionDeniedException_QNAME = new QName("http://www.cmis.org/2008/05", "permissionDeniedException");
    private final static QName _ChoiceXml_QNAME = new QName("http://www.cmis.org/2008/05", "choiceXml");
    private final static QName _NotInFolderException_QNAME = new QName("http://www.cmis.org/2008/05", "notInFolderException");
    private final static QName _StreamNotSupportedException_QNAME = new QName("http://www.cmis.org/2008/05", "streamNotSupportedException");
    private final static QName _Property_QNAME = new QName("http://www.cmis.org/2008/05", "property");
    private final static QName _FilterNotValidException_QNAME = new QName("http://www.cmis.org/2008/05", "filterNotValidException");
    private final static QName _PropertyHtmlDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyHtmlDefinition");
    private final static QName _PropertyUri_QNAME = new QName("http://www.cmis.org/2008/05", "propertyUri");
    private final static QName _PropertyDateTimeDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyDateTimeDefinition");
    private final static QName _PropertyHtml_QNAME = new QName("http://www.cmis.org/2008/05", "propertyHtml");
    private final static QName _PropertyId_QNAME = new QName("http://www.cmis.org/2008/05", "propertyId");
    private final static QName _RuntimeException_QNAME = new QName("http://www.cmis.org/2008/05", "runtimeException");
    private final static QName _PropertyIntegerDefinition_QNAME = new QName("http://www.cmis.org/2008/05", "propertyIntegerDefinition");
    private final static QName _DeleteTreeContinueOnFailure_QNAME = new QName("http://www.cmis.org/2008/05", "continueOnFailure");
    private final static QName _CheckInProperties_QNAME = new QName("http://www.cmis.org/2008/05", "properties");
    private final static QName _CheckInMajor_QNAME = new QName("http://www.cmis.org/2008/05", "major");
    private final static QName _CheckInCheckinComment_QNAME = new QName("http://www.cmis.org/2008/05", "checkinComment");
    private final static QName _GetDescendantsDepth_QNAME = new QName("http://www.cmis.org/2008/05", "depth");
    private final static QName _GetTypesReturnPropertyDefinitions_QNAME = new QName("http://www.cmis.org/2008/05", "returnPropertyDefinitions");
    private final static QName _GetCheckedoutDocsFolderID_QNAME = new QName("http://www.cmis.org/2008/05", "folderID");
    private final static QName _GetPropertiesReturnVersion_QNAME = new QName("http://www.cmis.org/2008/05", "returnVersion");
    private final static QName _SetContentStreamOverwriteFlag_QNAME = new QName("http://www.cmis.org/2008/05", "overwriteFlag");
    private final static QName _MoveObjectSourceFolderId_QNAME = new QName("http://www.cmis.org/2008/05", "sourceFolderId");
    private final static QName _GetFolderParentReturnToRoot_QNAME = new QName("http://www.cmis.org/2008/05", "returnToRoot");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.alfresco.repo.cmis.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetAllowableActionsResponse }
     * 
     */
    public GetAllowableActionsResponse createGetAllowableActionsResponse() {
        return new GetAllowableActionsResponse();
    }

    /**
     * Create an instance of {@link GetPropertiesOfLatestVersionResponse }
     * 
     */
    public GetPropertiesOfLatestVersionResponse createGetPropertiesOfLatestVersionResponse() {
        return new GetPropertiesOfLatestVersionResponse();
    }

    /**
     * Create an instance of {@link GetTypesResponse }
     * 
     */
    public GetTypesResponse createGetTypesResponse() {
        return new GetTypesResponse();
    }

    /**
     * Create an instance of {@link DeleteTree }
     * 
     */
    public DeleteTree createDeleteTree() {
        return new DeleteTree();
    }

    /**
     * Create an instance of {@link CmisPropertyStringDefinitionType }
     * 
     */
    public CmisPropertyStringDefinitionType createCmisPropertyStringDefinitionType() {
        return new CmisPropertyStringDefinitionType();
    }

    /**
     * Create an instance of {@link ApplyPolicyResponse }
     * 
     */
    public ApplyPolicyResponse createApplyPolicyResponse() {
        return new ApplyPolicyResponse();
    }

    /**
     * Create an instance of {@link DeleteAllVersions }
     * 
     */
    public DeleteAllVersions createDeleteAllVersions() {
        return new DeleteAllVersions();
    }

    /**
     * Create an instance of {@link CmisPropertyUriDefinitionType }
     * 
     */
    public CmisPropertyUriDefinitionType createCmisPropertyUriDefinitionType() {
        return new CmisPropertyUriDefinitionType();
    }

    /**
     * Create an instance of {@link RemoveObjectFromFolderResponse }
     * 
     */
    public RemoveObjectFromFolderResponse createRemoveObjectFromFolderResponse() {
        return new RemoveObjectFromFolderResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyBoolean }
     * 
     */
    public CmisPropertyBoolean createCmisPropertyBoolean() {
        return new CmisPropertyBoolean();
    }

    /**
     * Create an instance of {@link CreatePolicy }
     * 
     */
    public CreatePolicy createCreatePolicy() {
        return new CreatePolicy();
    }

    /**
     * Create an instance of {@link CmisRepositoryEntryType }
     * 
     */
    public CmisRepositoryEntryType createCmisRepositoryEntryType() {
        return new CmisRepositoryEntryType();
    }

    /**
     * Create an instance of {@link DeleteTreeResponse.FailedToDelete }
     * 
     */
    public DeleteTreeResponse.FailedToDelete createDeleteTreeResponseFailedToDelete() {
        return new DeleteTreeResponse.FailedToDelete();
    }

    /**
     * Create an instance of {@link CreateFolderResponse }
     * 
     */
    public CreateFolderResponse createCreateFolderResponse() {
        return new CreateFolderResponse();
    }

    /**
     * Create an instance of {@link CmisRepositoryInfoType }
     * 
     */
    public CmisRepositoryInfoType createCmisRepositoryInfoType() {
        return new CmisRepositoryInfoType();
    }

    /**
     * Create an instance of {@link CmisPropertyUri }
     * 
     */
    public CmisPropertyUri createCmisPropertyUri() {
        return new CmisPropertyUri();
    }

    /**
     * Create an instance of {@link CmisChoiceDateTimeType }
     * 
     */
    public CmisChoiceDateTimeType createCmisChoiceDateTimeType() {
        return new CmisChoiceDateTimeType();
    }

    /**
     * Create an instance of {@link CmisChoiceUriType }
     * 
     */
    public CmisChoiceUriType createCmisChoiceUriType() {
        return new CmisChoiceUriType();
    }

    /**
     * Create an instance of {@link CmisChoiceIntegerType }
     * 
     */
    public CmisChoiceIntegerType createCmisChoiceIntegerType() {
        return new CmisChoiceIntegerType();
    }

    /**
     * Create an instance of {@link GetPropertiesOfLatestVersion }
     * 
     */
    public GetPropertiesOfLatestVersion createGetPropertiesOfLatestVersion() {
        return new GetPropertiesOfLatestVersion();
    }

    /**
     * Create an instance of {@link NotInFolderExceptionType }
     * 
     */
    public NotInFolderExceptionType createNotInFolderExceptionType() {
        return new NotInFolderExceptionType();
    }

    /**
     * Create an instance of {@link RemovePolicy }
     * 
     */
    public RemovePolicy createRemovePolicy() {
        return new RemovePolicy();
    }

    /**
     * Create an instance of {@link CmisPropertyDateTimeDefinitionType }
     * 
     */
    public CmisPropertyDateTimeDefinitionType createCmisPropertyDateTimeDefinitionType() {
        return new CmisPropertyDateTimeDefinitionType();
    }

    /**
     * Create an instance of {@link CmisObjectType }
     * 
     */
    public CmisObjectType createCmisObjectType() {
        return new CmisObjectType();
    }

    /**
     * Create an instance of {@link CmisChoiceStringType }
     * 
     */
    public CmisChoiceStringType createCmisChoiceStringType() {
        return new CmisChoiceStringType();
    }

    /**
     * Create an instance of {@link SetContentStreamResponse }
     * 
     */
    public SetContentStreamResponse createSetContentStreamResponse() {
        return new SetContentStreamResponse();
    }

    /**
     * Create an instance of {@link GetRelationships }
     * 
     */
    public GetRelationships createGetRelationships() {
        return new GetRelationships();
    }

    /**
     * Create an instance of {@link CmisChoiceIdType }
     * 
     */
    public CmisChoiceIdType createCmisChoiceIdType() {
        return new CmisChoiceIdType();
    }

    /**
     * Create an instance of {@link CmisProperty }
     * 
     */
    public CmisProperty createCmisProperty() {
        return new CmisProperty();
    }

    /**
     * Create an instance of {@link CreateFolder }
     * 
     */
    public CreateFolder createCreateFolder() {
        return new CreateFolder();
    }

    /**
     * Create an instance of {@link DeleteContentStream }
     * 
     */
    public DeleteContentStream createDeleteContentStream() {
        return new DeleteContentStream();
    }

    /**
     * Create an instance of {@link CmisQueryType }
     * 
     */
    public CmisQueryType createCmisQueryType() {
        return new CmisQueryType();
    }

    /**
     * Create an instance of {@link CmisPropertyXmlDefinitionType }
     * 
     */
    public CmisPropertyXmlDefinitionType createCmisPropertyXmlDefinitionType() {
        return new CmisPropertyXmlDefinitionType();
    }

    /**
     * Create an instance of {@link GetAllowableActions }
     * 
     */
    public GetAllowableActions createGetAllowableActions() {
        return new GetAllowableActions();
    }

    /**
     * Create an instance of {@link StreamNotSupportedExceptionType }
     * 
     */
    public StreamNotSupportedExceptionType createStreamNotSupportedExceptionType() {
        return new StreamNotSupportedExceptionType();
    }

    /**
     * Create an instance of {@link CmisChoiceHtmlType }
     * 
     */
    public CmisChoiceHtmlType createCmisChoiceHtmlType() {
        return new CmisChoiceHtmlType();
    }

    /**
     * Create an instance of {@link GetFolderParentResponse }
     * 
     */
    public GetFolderParentResponse createGetFolderParentResponse() {
        return new GetFolderParentResponse();
    }

    /**
     * Create an instance of {@link MoveObject }
     * 
     */
    public MoveObject createMoveObject() {
        return new MoveObject();
    }

    /**
     * Create an instance of {@link CmisPropertyDefinitionType }
     * 
     */
    public CmisPropertyDefinitionType createCmisPropertyDefinitionType() {
        return new CmisPropertyDefinitionType();
    }

    /**
     * Create an instance of {@link CreateRelationship }
     * 
     */
    public CreateRelationship createCreateRelationship() {
        return new CreateRelationship();
    }

    /**
     * Create an instance of {@link QueryResponse }
     * 
     */
    public QueryResponse createQueryResponse() {
        return new QueryResponse();
    }

    /**
     * Create an instance of {@link ApplyPolicy }
     * 
     */
    public ApplyPolicy createApplyPolicy() {
        return new ApplyPolicy();
    }

    /**
     * Create an instance of {@link CheckOut }
     * 
     */
    public CheckOut createCheckOut() {
        return new CheckOut();
    }

    /**
     * Create an instance of {@link CmisAnyXml }
     * 
     */
    public CmisAnyXml createCmisAnyXml() {
        return new CmisAnyXml();
    }

    /**
     * Create an instance of {@link CmisPropertyDateTime }
     * 
     */
    public CmisPropertyDateTime createCmisPropertyDateTime() {
        return new CmisPropertyDateTime();
    }

    /**
     * Create an instance of {@link CmisPropertyIntegerDefinitionType }
     * 
     */
    public CmisPropertyIntegerDefinitionType createCmisPropertyIntegerDefinitionType() {
        return new CmisPropertyIntegerDefinitionType();
    }

    /**
     * Create an instance of {@link GetAllVersionsResponse }
     * 
     */
    public GetAllVersionsResponse createGetAllVersionsResponse() {
        return new GetAllVersionsResponse();
    }

    /**
     * Create an instance of {@link ObjectTreeCollectionType }
     * 
     */
    public ObjectTreeCollectionType createObjectTreeCollectionType() {
        return new ObjectTreeCollectionType();
    }

    /**
     * Create an instance of {@link GetTypes }
     * 
     */
    public GetTypes createGetTypes() {
        return new GetTypes();
    }

    /**
     * Create an instance of {@link GetCheckedoutDocs }
     * 
     */
    public GetCheckedoutDocs createGetCheckedoutDocs() {
        return new GetCheckedoutDocs();
    }

    /**
     * Create an instance of {@link CmisChoiceXmlType }
     * 
     */
    public CmisChoiceXmlType createCmisChoiceXmlType() {
        return new CmisChoiceXmlType();
    }

    /**
     * Create an instance of {@link DeleteObjectResponse }
     * 
     */
    public DeleteObjectResponse createDeleteObjectResponse() {
        return new DeleteObjectResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyHtml }
     * 
     */
    public CmisPropertyHtml createCmisPropertyHtml() {
        return new CmisPropertyHtml();
    }

    /**
     * Create an instance of {@link DeleteAllVersionsResponse }
     * 
     */
    public DeleteAllVersionsResponse createDeleteAllVersionsResponse() {
        return new DeleteAllVersionsResponse();
    }

    /**
     * Create an instance of {@link SetContentStream }
     * 
     */
    public SetContentStream createSetContentStream() {
        return new SetContentStream();
    }

    /**
     * Create an instance of {@link CancelCheckOut }
     * 
     */
    public CancelCheckOut createCancelCheckOut() {
        return new CancelCheckOut();
    }

    /**
     * Create an instance of {@link CmisPropertiesType }
     * 
     */
    public CmisPropertiesType createCmisPropertiesType() {
        return new CmisPropertiesType();
    }

    /**
     * Create an instance of {@link GetRepositoriesResponse }
     * 
     */
    public GetRepositoriesResponse createGetRepositoriesResponse() {
        return new GetRepositoriesResponse();
    }

    /**
     * Create an instance of {@link GetRelationshipsResponse }
     * 
     */
    public GetRelationshipsResponse createGetRelationshipsResponse() {
        return new GetRelationshipsResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyHtmlDefinitionType }
     * 
     */
    public CmisPropertyHtmlDefinitionType createCmisPropertyHtmlDefinitionType() {
        return new CmisPropertyHtmlDefinitionType();
    }

    /**
     * Create an instance of {@link DeleteObject }
     * 
     */
    public DeleteObject createDeleteObject() {
        return new DeleteObject();
    }

    /**
     * Create an instance of {@link UpdateProperties }
     * 
     */
    public UpdateProperties createUpdateProperties() {
        return new UpdateProperties();
    }

    /**
     * Create an instance of {@link OffsetExceptionType }
     * 
     */
    public OffsetExceptionType createOffsetExceptionType() {
        return new OffsetExceptionType();
    }

    /**
     * Create an instance of {@link CmisTypeFolderDefinitionType }
     * 
     */
    public CmisTypeFolderDefinitionType createCmisTypeFolderDefinitionType() {
        return new CmisTypeFolderDefinitionType();
    }

    /**
     * Create an instance of {@link StorageExceptionType }
     * 
     */
    public StorageExceptionType createStorageExceptionType() {
        return new StorageExceptionType();
    }

    /**
     * Create an instance of {@link GetObjectParentsResponse }
     * 
     */
    public GetObjectParentsResponse createGetObjectParentsResponse() {
        return new GetObjectParentsResponse();
    }

    /**
     * Create an instance of {@link GetContentStreamResponse }
     * 
     */
    public GetContentStreamResponse createGetContentStreamResponse() {
        return new GetContentStreamResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyInteger }
     * 
     */
    public CmisPropertyInteger createCmisPropertyInteger() {
        return new CmisPropertyInteger();
    }

    /**
     * Create an instance of {@link CmisTypeDocumentDefinitionType }
     * 
     */
    public CmisTypeDocumentDefinitionType createCmisTypeDocumentDefinitionType() {
        return new CmisTypeDocumentDefinitionType();
    }

    /**
     * Create an instance of {@link CancelCheckOutResponse }
     * 
     */
    public CancelCheckOutResponse createCancelCheckOutResponse() {
        return new CancelCheckOutResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyString }
     * 
     */
    public CmisPropertyString createCmisPropertyString() {
        return new CmisPropertyString();
    }

    /**
     * Create an instance of {@link GetPropertiesResponse }
     * 
     */
    public GetPropertiesResponse createGetPropertiesResponse() {
        return new GetPropertiesResponse();
    }

    /**
     * Create an instance of {@link UpdatePropertiesResponse }
     * 
     */
    public UpdatePropertiesResponse createUpdatePropertiesResponse() {
        return new UpdatePropertiesResponse();
    }

    /**
     * Create an instance of {@link CheckInResponse }
     * 
     */
    public CheckInResponse createCheckInResponse() {
        return new CheckInResponse();
    }

    /**
     * Create an instance of {@link AddObjectToFolderResponse }
     * 
     */
    public AddObjectToFolderResponse createAddObjectToFolderResponse() {
        return new AddObjectToFolderResponse();
    }

    /**
     * Create an instance of {@link CmisTypePolicyDefinitionType }
     * 
     */
    public CmisTypePolicyDefinitionType createCmisTypePolicyDefinitionType() {
        return new CmisTypePolicyDefinitionType();
    }

    /**
     * Create an instance of {@link GetFolderParent }
     * 
     */
    public GetFolderParent createGetFolderParent() {
        return new GetFolderParent();
    }

    /**
     * Create an instance of {@link GetContentStream }
     * 
     */
    public GetContentStream createGetContentStream() {
        return new GetContentStream();
    }

    /**
     * Create an instance of {@link CreateDocument }
     * 
     */
    public CreateDocument createCreateDocument() {
        return new CreateDocument();
    }

    /**
     * Create an instance of {@link UpdateConflictExceptionType }
     * 
     */
    public UpdateConflictExceptionType createUpdateConflictExceptionType() {
        return new UpdateConflictExceptionType();
    }

    /**
     * Create an instance of {@link CreatePolicyResponse }
     * 
     */
    public CreatePolicyResponse createCreatePolicyResponse() {
        return new CreatePolicyResponse();
    }

    /**
     * Create an instance of {@link CmisRepositoryCapabilitiesType }
     * 
     */
    public CmisRepositoryCapabilitiesType createCmisRepositoryCapabilitiesType() {
        return new CmisRepositoryCapabilitiesType();
    }

    /**
     * Create an instance of {@link ObjectNotFoundExceptionType }
     * 
     */
    public ObjectNotFoundExceptionType createObjectNotFoundExceptionType() {
        return new ObjectNotFoundExceptionType();
    }

    /**
     * Create an instance of {@link CmisAllowableActionsType }
     * 
     */
    public CmisAllowableActionsType createCmisAllowableActionsType() {
        return new CmisAllowableActionsType();
    }

    /**
     * Create an instance of {@link GetDescendants }
     * 
     */
    public GetDescendants createGetDescendants() {
        return new GetDescendants();
    }

    /**
     * Create an instance of {@link CheckIn }
     * 
     */
    public CheckIn createCheckIn() {
        return new CheckIn();
    }

    /**
     * Create an instance of {@link GetRepositoryInfo }
     * 
     */
    public GetRepositoryInfo createGetRepositoryInfo() {
        return new GetRepositoryInfo();
    }

    /**
     * Create an instance of {@link GetCheckedoutDocsResponse }
     * 
     */
    public GetCheckedoutDocsResponse createGetCheckedoutDocsResponse() {
        return new GetCheckedoutDocsResponse();
    }

    /**
     * Create an instance of {@link CheckOutResponse }
     * 
     */
    public CheckOutResponse createCheckOutResponse() {
        return new CheckOutResponse();
    }

    /**
     * Create an instance of {@link GetProperties }
     * 
     */
    public GetProperties createGetProperties() {
        return new GetProperties();
    }

    /**
     * Create an instance of {@link CmisChoiceDecimalType }
     * 
     */
    public CmisChoiceDecimalType createCmisChoiceDecimalType() {
        return new CmisChoiceDecimalType();
    }

    /**
     * Create an instance of {@link FilterNotValidExceptionType }
     * 
     */
    public FilterNotValidExceptionType createFilterNotValidExceptionType() {
        return new FilterNotValidExceptionType();
    }

    /**
     * Create an instance of {@link CmisTypeDefinitionType }
     * 
     */
    public CmisTypeDefinitionType createCmisTypeDefinitionType() {
        return new CmisTypeDefinitionType();
    }

    /**
     * Create an instance of {@link CmisPropertyXml }
     * 
     */
    public CmisPropertyXml createCmisPropertyXml() {
        return new CmisPropertyXml();
    }

    /**
     * Create an instance of {@link CmisFaultType }
     * 
     */
    public CmisFaultType createCmisFaultType() {
        return new CmisFaultType();
    }

    /**
     * Create an instance of {@link DeleteContentStreamResponse }
     * 
     */
    public DeleteContentStreamResponse createDeleteContentStreamResponse() {
        return new DeleteContentStreamResponse();
    }

    /**
     * Create an instance of {@link InvalidArgumentExceptionType }
     * 
     */
    public InvalidArgumentExceptionType createInvalidArgumentExceptionType() {
        return new InvalidArgumentExceptionType();
    }

    /**
     * Create an instance of {@link TypeNotFoundExceptionType }
     * 
     */
    public TypeNotFoundExceptionType createTypeNotFoundExceptionType() {
        return new TypeNotFoundExceptionType();
    }

    /**
     * Create an instance of {@link CmisPropertyDecimalDefinitionType }
     * 
     */
    public CmisPropertyDecimalDefinitionType createCmisPropertyDecimalDefinitionType() {
        return new CmisPropertyDecimalDefinitionType();
    }

    /**
     * Create an instance of {@link GetAppliedPoliciesResponse }
     * 
     */
    public GetAppliedPoliciesResponse createGetAppliedPoliciesResponse() {
        return new GetAppliedPoliciesResponse();
    }

    /**
     * Create an instance of {@link CmisContentStreamType }
     * 
     */
    public CmisContentStreamType createCmisContentStreamType() {
        return new CmisContentStreamType();
    }

    /**
     * Create an instance of {@link ContentAlreadyExistsExceptionType }
     * 
     */
    public ContentAlreadyExistsExceptionType createContentAlreadyExistsExceptionType() {
        return new ContentAlreadyExistsExceptionType();
    }

    /**
     * Create an instance of {@link RuntimeExceptionType }
     * 
     */
    public RuntimeExceptionType createRuntimeExceptionType() {
        return new RuntimeExceptionType();
    }

    /**
     * Create an instance of {@link CreateDocumentResponse }
     * 
     */
    public CreateDocumentResponse createCreateDocumentResponse() {
        return new CreateDocumentResponse();
    }

    /**
     * Create an instance of {@link ConstraintViolationExceptionType }
     * 
     */
    public ConstraintViolationExceptionType createConstraintViolationExceptionType() {
        return new ConstraintViolationExceptionType();
    }

    /**
     * Create an instance of {@link CmisPropertyId }
     * 
     */
    public CmisPropertyId createCmisPropertyId() {
        return new CmisPropertyId();
    }

    /**
     * Create an instance of {@link GetTypeDefinition }
     * 
     */
    public GetTypeDefinition createGetTypeDefinition() {
        return new GetTypeDefinition();
    }

    /**
     * Create an instance of {@link CmisPropertyIdDefinitionType }
     * 
     */
    public CmisPropertyIdDefinitionType createCmisPropertyIdDefinitionType() {
        return new CmisPropertyIdDefinitionType();
    }

    /**
     * Create an instance of {@link CmisTypeRelationshipDefinitionType }
     * 
     */
    public CmisTypeRelationshipDefinitionType createCmisTypeRelationshipDefinitionType() {
        return new CmisTypeRelationshipDefinitionType();
    }

    /**
     * Create an instance of {@link CreateRelationshipResponse }
     * 
     */
    public CreateRelationshipResponse createCreateRelationshipResponse() {
        return new CreateRelationshipResponse();
    }

    /**
     * Create an instance of {@link VersioningExceptionType }
     * 
     */
    public VersioningExceptionType createVersioningExceptionType() {
        return new VersioningExceptionType();
    }

    /**
     * Create an instance of {@link PermissionDeniedExceptionType }
     * 
     */
    public PermissionDeniedExceptionType createPermissionDeniedExceptionType() {
        return new PermissionDeniedExceptionType();
    }

    /**
     * Create an instance of {@link OperationNotSupportedExceptionType }
     * 
     */
    public OperationNotSupportedExceptionType createOperationNotSupportedExceptionType() {
        return new OperationNotSupportedExceptionType();
    }

    /**
     * Create an instance of {@link GetAllVersions }
     * 
     */
    public GetAllVersions createGetAllVersions() {
        return new GetAllVersions();
    }

    /**
     * Create an instance of {@link GetDescendantsResponse }
     * 
     */
    public GetDescendantsResponse createGetDescendantsResponse() {
        return new GetDescendantsResponse();
    }

    /**
     * Create an instance of {@link GetChildren }
     * 
     */
    public GetChildren createGetChildren() {
        return new GetChildren();
    }

    /**
     * Create an instance of {@link CmisChoiceBooleanType }
     * 
     */
    public CmisChoiceBooleanType createCmisChoiceBooleanType() {
        return new CmisChoiceBooleanType();
    }

    /**
     * Create an instance of {@link RemoveObjectFromFolder }
     * 
     */
    public RemoveObjectFromFolder createRemoveObjectFromFolder() {
        return new RemoveObjectFromFolder();
    }

    /**
     * Create an instance of {@link AddObjectToFolder }
     * 
     */
    public AddObjectToFolder createAddObjectToFolder() {
        return new AddObjectToFolder();
    }

    /**
     * Create an instance of {@link GetRepositories }
     * 
     */
    public GetRepositories createGetRepositories() {
        return new GetRepositories();
    }

    /**
     * Create an instance of {@link GetObjectParents }
     * 
     */
    public GetObjectParents createGetObjectParents() {
        return new GetObjectParents();
    }

    /**
     * Create an instance of {@link CmisPropertyBooleanDefinitionType }
     * 
     */
    public CmisPropertyBooleanDefinitionType createCmisPropertyBooleanDefinitionType() {
        return new CmisPropertyBooleanDefinitionType();
    }

    /**
     * Create an instance of {@link GetAppliedPolicies }
     * 
     */
    public GetAppliedPolicies createGetAppliedPolicies() {
        return new GetAppliedPolicies();
    }

    /**
     * Create an instance of {@link GetChildrenResponse }
     * 
     */
    public GetChildrenResponse createGetChildrenResponse() {
        return new GetChildrenResponse();
    }

    /**
     * Create an instance of {@link DeleteTreeResponse }
     * 
     */
    public DeleteTreeResponse createDeleteTreeResponse() {
        return new DeleteTreeResponse();
    }

    /**
     * Create an instance of {@link FolderNotValidExceptionType }
     * 
     */
    public FolderNotValidExceptionType createFolderNotValidExceptionType() {
        return new FolderNotValidExceptionType();
    }

    /**
     * Create an instance of {@link GetTypeDefinitionResponse }
     * 
     */
    public GetTypeDefinitionResponse createGetTypeDefinitionResponse() {
        return new GetTypeDefinitionResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyDecimal }
     * 
     */
    public CmisPropertyDecimal createCmisPropertyDecimal() {
        return new CmisPropertyDecimal();
    }

    /**
     * Create an instance of {@link MoveObjectResponse }
     * 
     */
    public MoveObjectResponse createMoveObjectResponse() {
        return new MoveObjectResponse();
    }

    /**
     * Create an instance of {@link RemovePolicyResponse }
     * 
     */
    public RemovePolicyResponse createRemovePolicyResponse() {
        return new RemovePolicyResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "changeToken", scope = UpdateProperties.class)
    public JAXBElement<String> createUpdatePropertiesChangeToken(String value) {
        return new JAXBElement<String>(_UpdatePropertiesChangeToken_QNAME, String.class, UpdateProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "folderId", scope = CreatePolicy.class)
    public JAXBElement<String> createCreatePolicyFolderId(String value) {
        return new JAXBElement<String>(_CreatePolicyFolderId_QNAME, String.class, CreatePolicy.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumVersioningState }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "versioningState", scope = CreateDocument.class)
    public JAXBElement<EnumVersioningState> createCreateDocumentVersioningState(EnumVersioningState value) {
        return new JAXBElement<EnumVersioningState>(_CreateDocumentVersioningState_QNAME, EnumVersioningState.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "contentStream", scope = CreateDocument.class)
    public JAXBElement<CmisContentStreamType> createCreateDocumentContentStream(CmisContentStreamType value) {
        return new JAXBElement<CmisContentStreamType>(_CreateDocumentContentStream_QNAME, CmisContentStreamType.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeAllowableActions", scope = GetAllVersions.class)
    public JAXBElement<Boolean> createGetAllVersionsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeAllowableActions_QNAME, Boolean.class, GetAllVersions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "filter", scope = GetAllVersions.class)
    public JAXBElement<String> createGetAllVersionsFilter(String value) {
        return new JAXBElement<String>(_GetAllVersionsFilter_QNAME, String.class, GetAllVersions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeRelationships", scope = GetAllVersions.class)
    public JAXBElement<Boolean> createGetAllVersionsIncludeRelationships(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeRelationships_QNAME, Boolean.class, GetAllVersions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeAllowableActions", scope = GetRelationships.class)
    public JAXBElement<Boolean> createGetRelationshipsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeAllowableActions_QNAME, Boolean.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "skipCount", scope = GetRelationships.class)
    public JAXBElement<BigInteger> createGetRelationshipsSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetRelationshipsSkipCount_QNAME, BigInteger.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "typeId", scope = GetRelationships.class)
    public JAXBElement<String> createGetRelationshipsTypeId(String value) {
        return new JAXBElement<String>(_GetRelationshipsTypeId_QNAME, String.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeSubRelationshipTypes", scope = GetRelationships.class)
    public JAXBElement<Boolean> createGetRelationshipsIncludeSubRelationshipTypes(Boolean value) {
        return new JAXBElement<Boolean>(_GetRelationshipsIncludeSubRelationshipTypes_QNAME, Boolean.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumRelationshipDirection }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "direction", scope = GetRelationships.class)
    public JAXBElement<EnumRelationshipDirection> createGetRelationshipsDirection(EnumRelationshipDirection value) {
        return new JAXBElement<EnumRelationshipDirection>(_GetRelationshipsDirection_QNAME, EnumRelationshipDirection.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "filter", scope = GetRelationships.class)
    public JAXBElement<String> createGetRelationshipsFilter(String value) {
        return new JAXBElement<String>(_GetAllVersionsFilter_QNAME, String.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "maxItems", scope = GetRelationships.class)
    public JAXBElement<BigInteger> createGetRelationshipsMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetRelationshipsMaxItems_QNAME, BigInteger.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeRelationships", scope = GetRelationships.class)
    public JAXBElement<Boolean> createGetRelationshipsIncludeRelationships(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeRelationships_QNAME, Boolean.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyStringDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyStringDefinition", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "propertyDefinition")
    public JAXBElement<CmisPropertyStringDefinitionType> createPropertyStringDefinition(CmisPropertyStringDefinitionType value) {
        return new JAXBElement<CmisPropertyStringDefinitionType>(_PropertyStringDefinition_QNAME, CmisPropertyStringDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyXml }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyXml", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "property")
    public JAXBElement<CmisPropertyXml> createPropertyXml(CmisPropertyXml value) {
        return new JAXBElement<CmisPropertyXml>(_PropertyXml_QNAME, CmisPropertyXml.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceDateTimeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choiceDateTime", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "choice")
    public JAXBElement<CmisChoiceDateTimeType> createChoiceDateTime(CmisChoiceDateTimeType value) {
        return new JAXBElement<CmisChoiceDateTimeType>(_ChoiceDateTime_QNAME, CmisChoiceDateTimeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "terminator")
    public JAXBElement<String> createTerminator(String value) {
        return new JAXBElement<String>(_Terminator_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisObjectType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "object")
    public JAXBElement<CmisObjectType> createObject(CmisObjectType value) {
        return new JAXBElement<CmisObjectType>(_Object_QNAME, CmisObjectType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObjectNotFoundExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "objectNotFoundException")
    public JAXBElement<ObjectNotFoundExceptionType> createObjectNotFoundException(ObjectNotFoundExceptionType value) {
        return new JAXBElement<ObjectNotFoundExceptionType>(_ObjectNotFoundException_QNAME, ObjectNotFoundExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "hasMoreItems")
    public JAXBElement<Boolean> createHasMoreItems(Boolean value) {
        return new JAXBElement<Boolean>(_HasMoreItems_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceBooleanType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choiceBoolean", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "choice")
    public JAXBElement<CmisChoiceBooleanType> createChoiceBoolean(CmisChoiceBooleanType value) {
        return new JAXBElement<CmisChoiceBooleanType>(_ChoiceBoolean_QNAME, CmisChoiceBooleanType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyDecimal", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "property")
    public JAXBElement<CmisPropertyDecimal> createPropertyDecimal(CmisPropertyDecimal value) {
        return new JAXBElement<CmisPropertyDecimal>(_PropertyDecimal_QNAME, CmisPropertyDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyDefinition")
    public JAXBElement<CmisPropertyDefinitionType> createPropertyDefinition(CmisPropertyDefinitionType value) {
        return new JAXBElement<CmisPropertyDefinitionType>(_PropertyDefinition_QNAME, CmisPropertyDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceHtmlType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choiceHtml", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "choice")
    public JAXBElement<CmisChoiceHtmlType> createChoiceHtml(CmisChoiceHtmlType value) {
        return new JAXBElement<CmisChoiceHtmlType>(_ChoiceHtml_QNAME, CmisChoiceHtmlType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConstraintViolationExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "constraintViolationException")
    public JAXBElement<ConstraintViolationExceptionType> createConstraintViolationException(ConstraintViolationExceptionType value) {
        return new JAXBElement<ConstraintViolationExceptionType>(_ConstraintViolationException_QNAME, ConstraintViolationExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OperationNotSupportedExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "operationNotSupportedException")
    public JAXBElement<OperationNotSupportedExceptionType> createOperationNotSupportedException(OperationNotSupportedExceptionType value) {
        return new JAXBElement<OperationNotSupportedExceptionType>(_OperationNotSupportedException_QNAME, OperationNotSupportedExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TypeNotFoundExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "typeNotFoundException")
    public JAXBElement<TypeNotFoundExceptionType> createTypeNotFoundException(TypeNotFoundExceptionType value) {
        return new JAXBElement<TypeNotFoundExceptionType>(_TypeNotFoundException_QNAME, TypeNotFoundExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FolderNotValidExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "folderNotValidException")
    public JAXBElement<FolderNotValidExceptionType> createFolderNotValidException(FolderNotValidExceptionType value) {
        return new JAXBElement<FolderNotValidExceptionType>(_FolderNotValidException_QNAME, FolderNotValidExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisTypeDocumentDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "documentType", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "type")
    public JAXBElement<CmisTypeDocumentDefinitionType> createDocumentType(CmisTypeDocumentDefinitionType value) {
        return new JAXBElement<CmisTypeDocumentDefinitionType>(_DocumentType_QNAME, CmisTypeDocumentDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisTypeDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "type")
    public JAXBElement<CmisTypeDefinitionType> createType(CmisTypeDefinitionType value) {
        return new JAXBElement<CmisTypeDefinitionType>(_Type_QNAME, CmisTypeDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceIntegerType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choiceInteger", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "choice")
    public JAXBElement<CmisChoiceIntegerType> createChoiceInteger(CmisChoiceIntegerType value) {
        return new JAXBElement<CmisChoiceIntegerType>(_ChoiceInteger_QNAME, CmisChoiceIntegerType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InvalidArgumentExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "invalidArgumentException")
    public JAXBElement<InvalidArgumentExceptionType> createInvalidArgumentException(InvalidArgumentExceptionType value) {
        return new JAXBElement<InvalidArgumentExceptionType>(_InvalidArgumentException_QNAME, InvalidArgumentExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OffsetExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "offsetException")
    public JAXBElement<OffsetExceptionType> createOffsetException(OffsetExceptionType value) {
        return new JAXBElement<OffsetExceptionType>(_OffsetException_QNAME, OffsetExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StorageExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "storageException")
    public JAXBElement<StorageExceptionType> createStorageException(StorageExceptionType value) {
        return new JAXBElement<StorageExceptionType>(_StorageException_QNAME, StorageExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceDecimalType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choiceDecimal", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "choice")
    public JAXBElement<CmisChoiceDecimalType> createChoiceDecimal(CmisChoiceDecimalType value) {
        return new JAXBElement<CmisChoiceDecimalType>(_ChoiceDecimal_QNAME, CmisChoiceDecimalType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisTypeRelationshipDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "relationshipType", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "type")
    public JAXBElement<CmisTypeRelationshipDefinitionType> createRelationshipType(CmisTypeRelationshipDefinitionType value) {
        return new JAXBElement<CmisTypeRelationshipDefinitionType>(_RelationshipType_QNAME, CmisTypeRelationshipDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAllowableActionsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "allowableActions")
    public JAXBElement<CmisAllowableActionsType> createAllowableActions(CmisAllowableActionsType value) {
        return new JAXBElement<CmisAllowableActionsType>(_AllowableActions_QNAME, CmisAllowableActionsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choiceString", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "choice")
    public JAXBElement<CmisChoiceStringType> createChoiceString(CmisChoiceStringType value) {
        return new JAXBElement<CmisChoiceStringType>(_ChoiceString_QNAME, CmisChoiceStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisTypeFolderDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "folderType", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "type")
    public JAXBElement<CmisTypeFolderDefinitionType> createFolderType(CmisTypeFolderDefinitionType value) {
        return new JAXBElement<CmisTypeFolderDefinitionType>(_FolderType_QNAME, CmisTypeFolderDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VersioningExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "versioningException")
    public JAXBElement<VersioningExceptionType> createVersioningException(VersioningExceptionType value) {
        return new JAXBElement<VersioningExceptionType>(_VersioningException_QNAME, VersioningExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceIdType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choiceId", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "choice")
    public JAXBElement<CmisChoiceIdType> createChoiceId(CmisChoiceIdType value) {
        return new JAXBElement<CmisChoiceIdType>(_ChoiceId_QNAME, CmisChoiceIdType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisQueryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "query")
    public JAXBElement<CmisQueryType> createQuery(CmisQueryType value) {
        return new JAXBElement<CmisQueryType>(_Query_QNAME, CmisQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceUriType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choiceUri", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "choice")
    public JAXBElement<CmisChoiceUriType> createChoiceUri(CmisChoiceUriType value) {
        return new JAXBElement<CmisChoiceUriType>(_ChoiceUri_QNAME, CmisChoiceUriType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyBoolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyBoolean", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "property")
    public JAXBElement<CmisPropertyBoolean> createPropertyBoolean(CmisPropertyBoolean value) {
        return new JAXBElement<CmisPropertyBoolean>(_PropertyBoolean_QNAME, CmisPropertyBoolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyDecimalDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyDecimalDefinition", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "propertyDefinition")
    public JAXBElement<CmisPropertyDecimalDefinitionType> createPropertyDecimalDefinition(CmisPropertyDecimalDefinitionType value) {
        return new JAXBElement<CmisPropertyDecimalDefinitionType>(_PropertyDecimalDefinition_QNAME, CmisPropertyDecimalDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisTypePolicyDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "policyType", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "type")
    public JAXBElement<CmisTypePolicyDefinitionType> createPolicyType(CmisTypePolicyDefinitionType value) {
        return new JAXBElement<CmisTypePolicyDefinitionType>(_PolicyType_QNAME, CmisTypePolicyDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyInteger", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "property")
    public JAXBElement<CmisPropertyInteger> createPropertyInteger(CmisPropertyInteger value) {
        return new JAXBElement<CmisPropertyInteger>(_PropertyInteger_QNAME, CmisPropertyInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyBooleanDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyBooleanDefinition", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "propertyDefinition")
    public JAXBElement<CmisPropertyBooleanDefinitionType> createPropertyBooleanDefinition(CmisPropertyBooleanDefinitionType value) {
        return new JAXBElement<CmisPropertyBooleanDefinitionType>(_PropertyBooleanDefinition_QNAME, CmisPropertyBooleanDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyString }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyString", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "property")
    public JAXBElement<CmisPropertyString> createPropertyString(CmisPropertyString value) {
        return new JAXBElement<CmisPropertyString>(_PropertyString_QNAME, CmisPropertyString.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyIdDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyIdDefinition", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "propertyDefinition")
    public JAXBElement<CmisPropertyIdDefinitionType> createPropertyIdDefinition(CmisPropertyIdDefinitionType value) {
        return new JAXBElement<CmisPropertyIdDefinitionType>(_PropertyIdDefinition_QNAME, CmisPropertyIdDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisRepositoryInfoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "getRepositoryInfoResponse")
    public JAXBElement<CmisRepositoryInfoType> createGetRepositoryInfoResponse(CmisRepositoryInfoType value) {
        return new JAXBElement<CmisRepositoryInfoType>(_GetRepositoryInfoResponse_QNAME, CmisRepositoryInfoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyUriDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyUriDefinition", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "propertyDefinition")
    public JAXBElement<CmisPropertyUriDefinitionType> createPropertyUriDefinition(CmisPropertyUriDefinitionType value) {
        return new JAXBElement<CmisPropertyUriDefinitionType>(_PropertyUriDefinition_QNAME, CmisPropertyUriDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyDateTime }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyDateTime", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "property")
    public JAXBElement<CmisPropertyDateTime> createPropertyDateTime(CmisPropertyDateTime value) {
        return new JAXBElement<CmisPropertyDateTime>(_PropertyDateTime_QNAME, CmisPropertyDateTime.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisRepositoryInfoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "repositoryInfo")
    public JAXBElement<CmisRepositoryInfoType> createRepositoryInfo(CmisRepositoryInfoType value) {
        return new JAXBElement<CmisRepositoryInfoType>(_RepositoryInfo_QNAME, CmisRepositoryInfoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ContentAlreadyExistsExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "contentAlreadyExistsException")
    public JAXBElement<ContentAlreadyExistsExceptionType> createContentAlreadyExistsException(ContentAlreadyExistsExceptionType value) {
        return new JAXBElement<ContentAlreadyExistsExceptionType>(_ContentAlreadyExistsException_QNAME, ContentAlreadyExistsExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateConflictExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "updateConflictException")
    public JAXBElement<UpdateConflictExceptionType> createUpdateConflictException(UpdateConflictExceptionType value) {
        return new JAXBElement<UpdateConflictExceptionType>(_UpdateConflictException_QNAME, UpdateConflictExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choice")
    public JAXBElement<CmisChoiceType> createChoice(CmisChoiceType value) {
        return new JAXBElement<CmisChoiceType>(_Choice_QNAME, CmisChoiceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyXmlDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyXmlDefinition", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "propertyDefinition")
    public JAXBElement<CmisPropertyXmlDefinitionType> createPropertyXmlDefinition(CmisPropertyXmlDefinitionType value) {
        return new JAXBElement<CmisPropertyXmlDefinitionType>(_PropertyXmlDefinition_QNAME, CmisPropertyXmlDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PermissionDeniedExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "permissionDeniedException")
    public JAXBElement<PermissionDeniedExceptionType> createPermissionDeniedException(PermissionDeniedExceptionType value) {
        return new JAXBElement<PermissionDeniedExceptionType>(_PermissionDeniedException_QNAME, PermissionDeniedExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisChoiceXmlType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "choiceXml", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "choice")
    public JAXBElement<CmisChoiceXmlType> createChoiceXml(CmisChoiceXmlType value) {
        return new JAXBElement<CmisChoiceXmlType>(_ChoiceXml_QNAME, CmisChoiceXmlType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotInFolderExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "notInFolderException")
    public JAXBElement<NotInFolderExceptionType> createNotInFolderException(NotInFolderExceptionType value) {
        return new JAXBElement<NotInFolderExceptionType>(_NotInFolderException_QNAME, NotInFolderExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StreamNotSupportedExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "streamNotSupportedException")
    public JAXBElement<StreamNotSupportedExceptionType> createStreamNotSupportedException(StreamNotSupportedExceptionType value) {
        return new JAXBElement<StreamNotSupportedExceptionType>(_StreamNotSupportedException_QNAME, StreamNotSupportedExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisProperty }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "property")
    public JAXBElement<CmisProperty> createProperty(CmisProperty value) {
        return new JAXBElement<CmisProperty>(_Property_QNAME, CmisProperty.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FilterNotValidExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "filterNotValidException")
    public JAXBElement<FilterNotValidExceptionType> createFilterNotValidException(FilterNotValidExceptionType value) {
        return new JAXBElement<FilterNotValidExceptionType>(_FilterNotValidException_QNAME, FilterNotValidExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyHtmlDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyHtmlDefinition", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "propertyDefinition")
    public JAXBElement<CmisPropertyHtmlDefinitionType> createPropertyHtmlDefinition(CmisPropertyHtmlDefinitionType value) {
        return new JAXBElement<CmisPropertyHtmlDefinitionType>(_PropertyHtmlDefinition_QNAME, CmisPropertyHtmlDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyUri }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyUri", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "property")
    public JAXBElement<CmisPropertyUri> createPropertyUri(CmisPropertyUri value) {
        return new JAXBElement<CmisPropertyUri>(_PropertyUri_QNAME, CmisPropertyUri.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyDateTimeDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyDateTimeDefinition", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "propertyDefinition")
    public JAXBElement<CmisPropertyDateTimeDefinitionType> createPropertyDateTimeDefinition(CmisPropertyDateTimeDefinitionType value) {
        return new JAXBElement<CmisPropertyDateTimeDefinitionType>(_PropertyDateTimeDefinition_QNAME, CmisPropertyDateTimeDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyHtml }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyHtml", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "property")
    public JAXBElement<CmisPropertyHtml> createPropertyHtml(CmisPropertyHtml value) {
        return new JAXBElement<CmisPropertyHtml>(_PropertyHtml_QNAME, CmisPropertyHtml.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyId", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "property")
    public JAXBElement<CmisPropertyId> createPropertyId(CmisPropertyId value) {
        return new JAXBElement<CmisPropertyId>(_PropertyId_QNAME, CmisPropertyId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RuntimeExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "runtimeException")
    public JAXBElement<RuntimeExceptionType> createRuntimeException(RuntimeExceptionType value) {
        return new JAXBElement<RuntimeExceptionType>(_RuntimeException_QNAME, RuntimeExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertyIntegerDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "propertyIntegerDefinition", substitutionHeadNamespace = "http://www.cmis.org/2008/05", substitutionHeadName = "propertyDefinition")
    public JAXBElement<CmisPropertyIntegerDefinitionType> createPropertyIntegerDefinition(CmisPropertyIntegerDefinitionType value) {
        return new JAXBElement<CmisPropertyIntegerDefinitionType>(_PropertyIntegerDefinition_QNAME, CmisPropertyIntegerDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeAllowableActions", scope = GetChildren.class)
    public JAXBElement<Boolean> createGetChildrenIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeAllowableActions_QNAME, Boolean.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumTypesOfFileableObjects }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "type", scope = GetChildren.class)
    public JAXBElement<EnumTypesOfFileableObjects> createGetChildrenType(EnumTypesOfFileableObjects value) {
        return new JAXBElement<EnumTypesOfFileableObjects>(_Type_QNAME, EnumTypesOfFileableObjects.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "skipCount", scope = GetChildren.class)
    public JAXBElement<BigInteger> createGetChildrenSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetRelationshipsSkipCount_QNAME, BigInteger.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "filter", scope = GetChildren.class)
    public JAXBElement<String> createGetChildrenFilter(String value) {
        return new JAXBElement<String>(_GetAllVersionsFilter_QNAME, String.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "maxItems", scope = GetChildren.class)
    public JAXBElement<BigInteger> createGetChildrenMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetRelationshipsMaxItems_QNAME, BigInteger.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeRelationships", scope = GetChildren.class)
    public JAXBElement<Boolean> createGetChildrenIncludeRelationships(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeRelationships_QNAME, Boolean.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "continueOnFailure", scope = DeleteTree.class)
    public JAXBElement<Boolean> createDeleteTreeContinueOnFailure(Boolean value) {
        return new JAXBElement<Boolean>(_DeleteTreeContinueOnFailure_QNAME, Boolean.class, DeleteTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "properties", scope = CheckIn.class)
    public JAXBElement<CmisPropertiesType> createCheckInProperties(CmisPropertiesType value) {
        return new JAXBElement<CmisPropertiesType>(_CheckInProperties_QNAME, CmisPropertiesType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "major", scope = CheckIn.class)
    public JAXBElement<Boolean> createCheckInMajor(Boolean value) {
        return new JAXBElement<Boolean>(_CheckInMajor_QNAME, Boolean.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "contentStream", scope = CheckIn.class)
    public JAXBElement<CmisContentStreamType> createCheckInContentStream(CmisContentStreamType value) {
        return new JAXBElement<CmisContentStreamType>(_CreateDocumentContentStream_QNAME, CmisContentStreamType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "checkinComment", scope = CheckIn.class)
    public JAXBElement<String> createCheckInCheckinComment(String value) {
        return new JAXBElement<String>(_CheckInCheckinComment_QNAME, String.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeAllowableActions", scope = GetDescendants.class)
    public JAXBElement<Boolean> createGetDescendantsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeAllowableActions_QNAME, Boolean.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "filter", scope = GetDescendants.class)
    public JAXBElement<String> createGetDescendantsFilter(String value) {
        return new JAXBElement<String>(_GetAllVersionsFilter_QNAME, String.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "depth", scope = GetDescendants.class)
    public JAXBElement<BigInteger> createGetDescendantsDepth(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetDescendantsDepth_QNAME, BigInteger.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeRelationships", scope = GetDescendants.class)
    public JAXBElement<Boolean> createGetDescendantsIncludeRelationships(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeRelationships_QNAME, Boolean.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "returnPropertyDefinitions", scope = GetTypes.class)
    public JAXBElement<Boolean> createGetTypesReturnPropertyDefinitions(Boolean value) {
        return new JAXBElement<Boolean>(_GetTypesReturnPropertyDefinitions_QNAME, Boolean.class, GetTypes.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "skipCount", scope = GetTypes.class)
    public JAXBElement<BigInteger> createGetTypesSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetRelationshipsSkipCount_QNAME, BigInteger.class, GetTypes.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "typeId", scope = GetTypes.class)
    public JAXBElement<String> createGetTypesTypeId(String value) {
        return new JAXBElement<String>(_GetRelationshipsTypeId_QNAME, String.class, GetTypes.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "maxItems", scope = GetTypes.class)
    public JAXBElement<BigInteger> createGetTypesMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetRelationshipsMaxItems_QNAME, BigInteger.class, GetTypes.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeAllowableActions", scope = GetCheckedoutDocs.class)
    public JAXBElement<Boolean> createGetCheckedoutDocsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeAllowableActions_QNAME, Boolean.class, GetCheckedoutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "skipCount", scope = GetCheckedoutDocs.class)
    public JAXBElement<BigInteger> createGetCheckedoutDocsSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetRelationshipsSkipCount_QNAME, BigInteger.class, GetCheckedoutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "filter", scope = GetCheckedoutDocs.class)
    public JAXBElement<String> createGetCheckedoutDocsFilter(String value) {
        return new JAXBElement<String>(_GetAllVersionsFilter_QNAME, String.class, GetCheckedoutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "maxItems", scope = GetCheckedoutDocs.class)
    public JAXBElement<BigInteger> createGetCheckedoutDocsMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetRelationshipsMaxItems_QNAME, BigInteger.class, GetCheckedoutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeRelationships", scope = GetCheckedoutDocs.class)
    public JAXBElement<Boolean> createGetCheckedoutDocsIncludeRelationships(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeRelationships_QNAME, Boolean.class, GetCheckedoutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "folderID", scope = GetCheckedoutDocs.class)
    public JAXBElement<String> createGetCheckedoutDocsFolderID(String value) {
        return new JAXBElement<String>(_GetCheckedoutDocsFolderID_QNAME, String.class, GetCheckedoutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "folderId", scope = RemoveObjectFromFolder.class)
    public JAXBElement<String> createRemoveObjectFromFolderFolderId(String value) {
        return new JAXBElement<String>(_CreatePolicyFolderId_QNAME, String.class, RemoveObjectFromFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeAllowableActions", scope = GetProperties.class)
    public JAXBElement<Boolean> createGetPropertiesIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeAllowableActions_QNAME, Boolean.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumReturnVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "returnVersion", scope = GetProperties.class)
    public JAXBElement<EnumReturnVersion> createGetPropertiesReturnVersion(EnumReturnVersion value) {
        return new JAXBElement<EnumReturnVersion>(_GetPropertiesReturnVersion_QNAME, EnumReturnVersion.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "filter", scope = GetProperties.class)
    public JAXBElement<String> createGetPropertiesFilter(String value) {
        return new JAXBElement<String>(_GetAllVersionsFilter_QNAME, String.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeRelationships", scope = GetProperties.class)
    public JAXBElement<Boolean> createGetPropertiesIncludeRelationships(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeRelationships_QNAME, Boolean.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "overwriteFlag", scope = SetContentStream.class)
    public JAXBElement<Boolean> createSetContentStreamOverwriteFlag(Boolean value) {
        return new JAXBElement<Boolean>(_SetContentStreamOverwriteFlag_QNAME, Boolean.class, SetContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeAllowableActions", scope = GetObjectParents.class)
    public JAXBElement<Boolean> createGetObjectParentsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeAllowableActions_QNAME, Boolean.class, GetObjectParents.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeRelationships", scope = GetObjectParents.class)
    public JAXBElement<Boolean> createGetObjectParentsIncludeRelationships(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeRelationships_QNAME, Boolean.class, GetObjectParents.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "filter", scope = GetPropertiesOfLatestVersion.class)
    public JAXBElement<String> createGetPropertiesOfLatestVersionFilter(String value) {
        return new JAXBElement<String>(_GetAllVersionsFilter_QNAME, String.class, GetPropertiesOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "sourceFolderId", scope = MoveObject.class)
    public JAXBElement<String> createMoveObjectSourceFolderId(String value) {
        return new JAXBElement<String>(_MoveObjectSourceFolderId_QNAME, String.class, MoveObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "filter", scope = GetAppliedPolicies.class)
    public JAXBElement<String> createGetAppliedPoliciesFilter(String value) {
        return new JAXBElement<String>(_GetAllVersionsFilter_QNAME, String.class, GetAppliedPolicies.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeAllowableActions", scope = GetFolderParent.class)
    public JAXBElement<Boolean> createGetFolderParentIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeAllowableActions_QNAME, Boolean.class, GetFolderParent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "returnToRoot", scope = GetFolderParent.class)
    public JAXBElement<Boolean> createGetFolderParentReturnToRoot(Boolean value) {
        return new JAXBElement<Boolean>(_GetFolderParentReturnToRoot_QNAME, Boolean.class, GetFolderParent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/2008/05", name = "includeRelationships", scope = GetFolderParent.class)
    public JAXBElement<Boolean> createGetFolderParentIncludeRelationships(Boolean value) {
        return new JAXBElement<Boolean>(_GetAllVersionsIncludeRelationships_QNAME, Boolean.class, GetFolderParent.class, value);
    }

}
