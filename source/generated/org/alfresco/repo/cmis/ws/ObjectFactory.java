
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

    private final static QName _GetDescendantsIncludeRelationships_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "includeRelationships");
    private final static QName _GetDescendantsIncludeAllowableActions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "includeAllowableActions");
    private final static QName _GetDescendantsFilter_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "filter");
    private final static QName _GetDescendantsIncludeRenditions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "includeRenditions");
    private final static QName _GetDescendantsDepth_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "depth");
    private final static QName _GetCheckedOutDocsFolderId_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "folderId");
    private final static QName _GetCheckedOutDocsSkipCount_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "skipCount");
    private final static QName _GetCheckedOutDocsOrderBy_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "orderBy");
    private final static QName _GetCheckedOutDocsMaxItems_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "maxItems");
    private final static QName _GetChildrenIncludeACL_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "includeACL");
    private final static QName _UpdatePropertiesChangeToken_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "changeToken");
    private final static QName _GetTypeChildrenTypeId_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "typeId");
    private final static QName _GetTypeChildrenIncludePropertyDefinitions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "includePropertyDefinitions");
    private final static QName _SetContentStreamOverwriteFlag_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "overwriteFlag");
    private final static QName _CreateRelationshipRemoveACEs_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "removeACEs");
    private final static QName _CreateRelationshipAddACEs_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "addACEs");
    private final static QName _CmisQueryTypeIncludeAllowableActions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/core/200901", "includeAllowableActions");
    private final static QName _CmisQueryTypeIncludeRenditions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/core/200901", "includeRenditions");
    private final static QName _CmisQueryTypeIncludeRelationships_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/core/200901", "includeRelationships");
    private final static QName _DeleteObjectAllVersions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "allVersions");
    private final static QName _CreateDocumentContentStream_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "contentStream");
    private final static QName _CreateDocumentVersioningState_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "versioningState");
    private final static QName _GetRelationshipsIncludeSubRelationshipTypes_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "includeSubRelationshipTypes");
    private final static QName _GetRelationshipsDirection_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "direction");
    private final static QName _GetContentChangesIncludeProperties_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "includeProperties");
    private final static QName _AllowableActions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/core/200901", "allowableActions");
    private final static QName _CmisFault_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "cmisFault");
    private final static QName _Acl_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/core/200901", "acl");
    private final static QName _Query_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/core/200901", "query");
    private final static QName _CheckInCheckinComment_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "checkinComment");
    private final static QName _CheckInProperties_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "properties");
    private final static QName _CheckInMajor_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "major");
    private final static QName _DeleteTreeContinueOnFailure_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "continueOnFailure");
    private final static QName _ApplyACLPropogationType_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200901", "propogationType");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.alfresco.repo.cmis.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeleteContentStreamResponse }
     * 
     */
    public DeleteContentStreamResponse createDeleteContentStreamResponse() {
        return new DeleteContentStreamResponse();
    }

    /**
     * Create an instance of {@link CmisChoiceDecimal }
     * 
     */
    public CmisChoiceDecimal createCmisChoiceDecimal() {
        return new CmisChoiceDecimal();
    }

    /**
     * Create an instance of {@link CmisPropertyDefinitionType }
     * 
     */
    public CmisPropertyDefinitionType createCmisPropertyDefinitionType() {
        return new CmisPropertyDefinitionType();
    }

    /**
     * Create an instance of {@link CmisTypeFolderDefinitionType }
     * 
     */
    public CmisTypeFolderDefinitionType createCmisTypeFolderDefinitionType() {
        return new CmisTypeFolderDefinitionType();
    }

    /**
     * Create an instance of {@link CmisPropertiesType }
     * 
     */
    public CmisPropertiesType createCmisPropertiesType() {
        return new CmisPropertiesType();
    }

    /**
     * Create an instance of {@link CmisPermissionDefinition }
     * 
     */
    public CmisPermissionDefinition createCmisPermissionDefinition() {
        return new CmisPermissionDefinition();
    }

    /**
     * Create an instance of {@link UpdateProperties }
     * 
     */
    public UpdateProperties createUpdateProperties() {
        return new UpdateProperties();
    }

    /**
     * Create an instance of {@link GetChildren }
     * 
     */
    public GetChildren createGetChildren() {
        return new GetChildren();
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
     * Create an instance of {@link DeleteContentStream }
     * 
     */
    public DeleteContentStream createDeleteContentStream() {
        return new DeleteContentStream();
    }

    /**
     * Create an instance of {@link GetChildrenResponse }
     * 
     */
    public GetChildrenResponse createGetChildrenResponse() {
        return new GetChildrenResponse();
    }

    /**
     * Create an instance of {@link RemoveObjectFromFolderResponse }
     * 
     */
    public RemoveObjectFromFolderResponse createRemoveObjectFromFolderResponse() {
        return new RemoveObjectFromFolderResponse();
    }

    /**
     * Create an instance of {@link GetContentChangesResponse }
     * 
     */
    public GetContentChangesResponse createGetContentChangesResponse() {
        return new GetContentChangesResponse();
    }

    /**
     * Create an instance of {@link GetAllVersionsResponse }
     * 
     */
    public GetAllVersionsResponse createGetAllVersionsResponse() {
        return new GetAllVersionsResponse();
    }

    /**
     * Create an instance of {@link CmisACLCapabilityType }
     * 
     */
    public CmisACLCapabilityType createCmisACLCapabilityType() {
        return new CmisACLCapabilityType();
    }

    /**
     * Create an instance of {@link GetCheckedOutDocsResponse }
     * 
     */
    public GetCheckedOutDocsResponse createGetCheckedOutDocsResponse() {
        return new GetCheckedOutDocsResponse();
    }

    /**
     * Create an instance of {@link DeleteObject }
     * 
     */
    public DeleteObject createDeleteObject() {
        return new DeleteObject();
    }

    /**
     * Create an instance of {@link CmisTypeDefinitionType }
     * 
     */
    public CmisTypeDefinitionType createCmisTypeDefinitionType() {
        return new CmisTypeDefinitionType();
    }

    /**
     * Create an instance of {@link GetTypeDescendants }
     * 
     */
    public GetTypeDescendants createGetTypeDescendants() {
        return new GetTypeDescendants();
    }

    /**
     * Create an instance of {@link GetTypeChildrenResponse }
     * 
     */
    public GetTypeChildrenResponse createGetTypeChildrenResponse() {
        return new GetTypeChildrenResponse();
    }

    /**
     * Create an instance of {@link DeleteTreeResponse.FailedToDelete }
     * 
     */
    public DeleteTreeResponse.FailedToDelete createDeleteTreeResponseFailedToDelete() {
        return new DeleteTreeResponse.FailedToDelete();
    }

    /**
     * Create an instance of {@link CmisChangeEventType }
     * 
     */
    public CmisChangeEventType createCmisChangeEventType() {
        return new CmisChangeEventType();
    }

    /**
     * Create an instance of {@link GetAllVersions }
     * 
     */
    public GetAllVersions createGetAllVersions() {
        return new GetAllVersions();
    }

    /**
     * Create an instance of {@link CreatePolicyResponse }
     * 
     */
    public CreatePolicyResponse createCreatePolicyResponse() {
        return new CreatePolicyResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyDecimalDefinitionType }
     * 
     */
    public CmisPropertyDecimalDefinitionType createCmisPropertyDecimalDefinitionType() {
        return new CmisPropertyDecimalDefinitionType();
    }

    /**
     * Create an instance of {@link GetContentChanges }
     * 
     */
    public GetContentChanges createGetContentChanges() {
        return new GetContentChanges();
    }

    /**
     * Create an instance of {@link CmisPropertyXml }
     * 
     */
    public CmisPropertyXml createCmisPropertyXml() {
        return new CmisPropertyXml();
    }

    /**
     * Create an instance of {@link CmisPropertyXhtml }
     * 
     */
    public CmisPropertyXhtml createCmisPropertyXhtml() {
        return new CmisPropertyXhtml();
    }

    /**
     * Create an instance of {@link CmisPropertyInteger }
     * 
     */
    public CmisPropertyInteger createCmisPropertyInteger() {
        return new CmisPropertyInteger();
    }

    /**
     * Create an instance of {@link GetPropertiesOfLatestVersionResponse }
     * 
     */
    public GetPropertiesOfLatestVersionResponse createGetPropertiesOfLatestVersionResponse() {
        return new GetPropertiesOfLatestVersionResponse();
    }

    /**
     * Create an instance of {@link DeleteTreeResponse }
     * 
     */
    public DeleteTreeResponse createDeleteTreeResponse() {
        return new DeleteTreeResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyIdDefinitionType }
     * 
     */
    public CmisPropertyIdDefinitionType createCmisPropertyIdDefinitionType() {
        return new CmisPropertyIdDefinitionType();
    }

    /**
     * Create an instance of {@link GetContentStreamResponse }
     * 
     */
    public GetContentStreamResponse createGetContentStreamResponse() {
        return new GetContentStreamResponse();
    }

    /**
     * Create an instance of {@link RemovePolicy }
     * 
     */
    public RemovePolicy createRemovePolicy() {
        return new RemovePolicy();
    }

    /**
     * Create an instance of {@link CmisChoiceUri }
     * 
     */
    public CmisChoiceUri createCmisChoiceUri() {
        return new CmisChoiceUri();
    }

    /**
     * Create an instance of {@link CmisRepositoryInfoType }
     * 
     */
    public CmisRepositoryInfoType createCmisRepositoryInfoType() {
        return new CmisRepositoryInfoType();
    }

    /**
     * Create an instance of {@link GetDescendants }
     * 
     */
    public GetDescendants createGetDescendants() {
        return new GetDescendants();
    }

    /**
     * Create an instance of {@link CheckInResponse }
     * 
     */
    public CheckInResponse createCheckInResponse() {
        return new CheckInResponse();
    }

    /**
     * Create an instance of {@link CmisChoiceXhtml }
     * 
     */
    public CmisChoiceXhtml createCmisChoiceXhtml() {
        return new CmisChoiceXhtml();
    }

    /**
     * Create an instance of {@link CmisChoiceXml.Value }
     * 
     */
    public CmisChoiceXml.Value createCmisChoiceXmlValue() {
        return new CmisChoiceXml.Value();
    }

    /**
     * Create an instance of {@link CmisPropertyUriDefinitionType }
     * 
     */
    public CmisPropertyUriDefinitionType createCmisPropertyUriDefinitionType() {
        return new CmisPropertyUriDefinitionType();
    }

    /**
     * Create an instance of {@link CmisPropertyXml.Value }
     * 
     */
    public CmisPropertyXml.Value createCmisPropertyXmlValue() {
        return new CmisPropertyXml.Value();
    }

    /**
     * Create an instance of {@link UpdatePropertiesResponse }
     * 
     */
    public UpdatePropertiesResponse createUpdatePropertiesResponse() {
        return new UpdatePropertiesResponse();
    }

    /**
     * Create an instance of {@link MoveObject }
     * 
     */
    public MoveObject createMoveObject() {
        return new MoveObject();
    }

    /**
     * Create an instance of {@link ApplyPolicy }
     * 
     */
    public ApplyPolicy createApplyPolicy() {
        return new ApplyPolicy();
    }

    /**
     * Create an instance of {@link CmisChoiceHtml.Value }
     * 
     */
    public CmisChoiceHtml.Value createCmisChoiceHtmlValue() {
        return new CmisChoiceHtml.Value();
    }

    /**
     * Create an instance of {@link DeleteObjectResponse }
     * 
     */
    public DeleteObjectResponse createDeleteObjectResponse() {
        return new DeleteObjectResponse();
    }

    /**
     * Create an instance of {@link CreateRelationship }
     * 
     */
    public CreateRelationship createCreateRelationship() {
        return new CreateRelationship();
    }

    /**
     * Create an instance of {@link GetRepositoriesResponse }
     * 
     */
    public GetRepositoriesResponse createGetRepositoriesResponse() {
        return new GetRepositoriesResponse();
    }

    /**
     * Create an instance of {@link GetAppliedPolicies }
     * 
     */
    public GetAppliedPolicies createGetAppliedPolicies() {
        return new GetAppliedPolicies();
    }

    /**
     * Create an instance of {@link CmisAccessControlEntryType }
     * 
     */
    public CmisAccessControlEntryType createCmisAccessControlEntryType() {
        return new CmisAccessControlEntryType();
    }

    /**
     * Create an instance of {@link GetRenditionsResponse }
     * 
     */
    public GetRenditionsResponse createGetRenditionsResponse() {
        return new GetRenditionsResponse();
    }

    /**
     * Create an instance of {@link GetRelationshipsResponse }
     * 
     */
    public GetRelationshipsResponse createGetRelationshipsResponse() {
        return new GetRelationshipsResponse();
    }

    /**
     * Create an instance of {@link GetProperties }
     * 
     */
    public GetProperties createGetProperties() {
        return new GetProperties();
    }

    /**
     * Create an instance of {@link CmisContentStreamType }
     * 
     */
    public CmisContentStreamType createCmisContentStreamType() {
        return new CmisContentStreamType();
    }

    /**
     * Create an instance of {@link GetTypeDefinition }
     * 
     */
    public GetTypeDefinition createGetTypeDefinition() {
        return new GetTypeDefinition();
    }

    /**
     * Create an instance of {@link CheckOut }
     * 
     */
    public CheckOut createCheckOut() {
        return new CheckOut();
    }

    /**
     * Create an instance of {@link GetAllowableActions }
     * 
     */
    public GetAllowableActions createGetAllowableActions() {
        return new GetAllowableActions();
    }

    /**
     * Create an instance of {@link CmisAccessControlListType }
     * 
     */
    public CmisAccessControlListType createCmisAccessControlListType() {
        return new CmisAccessControlListType();
    }

    /**
     * Create an instance of {@link CheckOutResponse }
     * 
     */
    public CheckOutResponse createCheckOutResponse() {
        return new CheckOutResponse();
    }

    /**
     * Create an instance of {@link AddObjectToFolder }
     * 
     */
    public AddObjectToFolder createAddObjectToFolder() {
        return new AddObjectToFolder();
    }

    /**
     * Create an instance of {@link GetTypeDescendantsResponse }
     * 
     */
    public GetTypeDescendantsResponse createGetTypeDescendantsResponse() {
        return new GetTypeDescendantsResponse();
    }

    /**
     * Create an instance of {@link CmisTypePolicyDefinitionType }
     * 
     */
    public CmisTypePolicyDefinitionType createCmisTypePolicyDefinitionType() {
        return new CmisTypePolicyDefinitionType();
    }

    /**
     * Create an instance of {@link MoveObjectResponse }
     * 
     */
    public MoveObjectResponse createMoveObjectResponse() {
        return new MoveObjectResponse();
    }

    /**
     * Create an instance of {@link GetPropertiesOfLatestVersion }
     * 
     */
    public GetPropertiesOfLatestVersion createGetPropertiesOfLatestVersion() {
        return new GetPropertiesOfLatestVersion();
    }

    /**
     * Create an instance of {@link CmisRepositoryEntryType }
     * 
     */
    public CmisRepositoryEntryType createCmisRepositoryEntryType() {
        return new CmisRepositoryEntryType();
    }

    /**
     * Create an instance of {@link CheckIn }
     * 
     */
    public CheckIn createCheckIn() {
        return new CheckIn();
    }

    /**
     * Create an instance of {@link GetFolderParentResponse }
     * 
     */
    public GetFolderParentResponse createGetFolderParentResponse() {
        return new GetFolderParentResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyDateTimeDefinitionType }
     * 
     */
    public CmisPropertyDateTimeDefinitionType createCmisPropertyDateTimeDefinitionType() {
        return new CmisPropertyDateTimeDefinitionType();
    }

    /**
     * Create an instance of {@link CmisPropertyBooleanDefinitionType }
     * 
     */
    public CmisPropertyBooleanDefinitionType createCmisPropertyBooleanDefinitionType() {
        return new CmisPropertyBooleanDefinitionType();
    }

    /**
     * Create an instance of {@link CmisPropertyHtml }
     * 
     */
    public CmisPropertyHtml createCmisPropertyHtml() {
        return new CmisPropertyHtml();
    }

    /**
     * Create an instance of {@link CancelCheckOutResponse }
     * 
     */
    public CancelCheckOutResponse createCancelCheckOutResponse() {
        return new CancelCheckOutResponse();
    }

    /**
     * Create an instance of {@link CmisChoiceBoolean }
     * 
     */
    public CmisChoiceBoolean createCmisChoiceBoolean() {
        return new CmisChoiceBoolean();
    }

    /**
     * Create an instance of {@link CmisPropertyId }
     * 
     */
    public CmisPropertyId createCmisPropertyId() {
        return new CmisPropertyId();
    }

    /**
     * Create an instance of {@link CmisFaultType }
     * 
     */
    public CmisFaultType createCmisFaultType() {
        return new CmisFaultType();
    }

    /**
     * Create an instance of {@link GetRepositoryInfoResponse }
     * 
     */
    public GetRepositoryInfoResponse createGetRepositoryInfoResponse() {
        return new GetRepositoryInfoResponse();
    }

    /**
     * Create an instance of {@link GetObjectParents }
     * 
     */
    public GetObjectParents createGetObjectParents() {
        return new GetObjectParents();
    }

    /**
     * Create an instance of {@link CmisAllowableActionsType }
     * 
     */
    public CmisAllowableActionsType createCmisAllowableActionsType() {
        return new CmisAllowableActionsType();
    }

    /**
     * Create an instance of {@link CmisPropertyDecimal }
     * 
     */
    public CmisPropertyDecimal createCmisPropertyDecimal() {
        return new CmisPropertyDecimal();
    }

    /**
     * Create an instance of {@link GetRenditions }
     * 
     */
    public GetRenditions createGetRenditions() {
        return new GetRenditions();
    }

    /**
     * Create an instance of {@link GetFolderTreeResponse }
     * 
     */
    public GetFolderTreeResponse createGetFolderTreeResponse() {
        return new GetFolderTreeResponse();
    }

    /**
     * Create an instance of {@link CmisChoiceString }
     * 
     */
    public CmisChoiceString createCmisChoiceString() {
        return new CmisChoiceString();
    }

    /**
     * Create an instance of {@link CmisChoiceId }
     * 
     */
    public CmisChoiceId createCmisChoiceId() {
        return new CmisChoiceId();
    }

    /**
     * Create an instance of {@link GetFolderByPathResponse }
     * 
     */
    public GetFolderByPathResponse createGetFolderByPathResponse() {
        return new GetFolderByPathResponse();
    }

    /**
     * Create an instance of {@link CreateDocument }
     * 
     */
    public CreateDocument createCreateDocument() {
        return new CreateDocument();
    }

    /**
     * Create an instance of {@link GetFolderTree }
     * 
     */
    public GetFolderTree createGetFolderTree() {
        return new GetFolderTree();
    }

    /**
     * Create an instance of {@link CmisChoiceXhtml.Value }
     * 
     */
    public CmisChoiceXhtml.Value createCmisChoiceXhtmlValue() {
        return new CmisChoiceXhtml.Value();
    }

    /**
     * Create an instance of {@link CmisPropertyDateTime }
     * 
     */
    public CmisPropertyDateTime createCmisPropertyDateTime() {
        return new CmisPropertyDateTime();
    }

    /**
     * Create an instance of {@link CreatePolicy }
     * 
     */
    public CreatePolicy createCreatePolicy() {
        return new CreatePolicy();
    }

    /**
     * Create an instance of {@link SetContentStreamResponse }
     * 
     */
    public SetContentStreamResponse createSetContentStreamResponse() {
        return new SetContentStreamResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyHtmlDefinitionType }
     * 
     */
    public CmisPropertyHtmlDefinitionType createCmisPropertyHtmlDefinitionType() {
        return new CmisPropertyHtmlDefinitionType();
    }

    /**
     * Create an instance of {@link CmisChoiceXml }
     * 
     */
    public CmisChoiceXml createCmisChoiceXml() {
        return new CmisChoiceXml();
    }

    /**
     * Create an instance of {@link GetRelationships }
     * 
     */
    public GetRelationships createGetRelationships() {
        return new GetRelationships();
    }

    /**
     * Create an instance of {@link CmisAccessControlPrincipalType }
     * 
     */
    public CmisAccessControlPrincipalType createCmisAccessControlPrincipalType() {
        return new CmisAccessControlPrincipalType();
    }

    /**
     * Create an instance of {@link ApplyACLResponse }
     * 
     */
    public ApplyACLResponse createApplyACLResponse() {
        return new ApplyACLResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyStringDefinitionType }
     * 
     */
    public CmisPropertyStringDefinitionType createCmisPropertyStringDefinitionType() {
        return new CmisPropertyStringDefinitionType();
    }

    /**
     * Create an instance of {@link GetAllowableActionsResponse }
     * 
     */
    public GetAllowableActionsResponse createGetAllowableActionsResponse() {
        return new GetAllowableActionsResponse();
    }

    /**
     * Create an instance of {@link CmisChoiceDateTime }
     * 
     */
    public CmisChoiceDateTime createCmisChoiceDateTime() {
        return new CmisChoiceDateTime();
    }

    /**
     * Create an instance of {@link CmisProperty }
     * 
     */
    public CmisProperty createCmisProperty() {
        return new CmisProperty();
    }

    /**
     * Create an instance of {@link GetRepositories }
     * 
     */
    public GetRepositories createGetRepositories() {
        return new GetRepositories();
    }

    /**
     * Create an instance of {@link GetObjectParentsResponse }
     * 
     */
    public GetObjectParentsResponse createGetObjectParentsResponse() {
        return new GetObjectParentsResponse();
    }

    /**
     * Create an instance of {@link CmisTypeRelationshipDefinitionType }
     * 
     */
    public CmisTypeRelationshipDefinitionType createCmisTypeRelationshipDefinitionType() {
        return new CmisTypeRelationshipDefinitionType();
    }

    /**
     * Create an instance of {@link CreateFolderResponse }
     * 
     */
    public CreateFolderResponse createCreateFolderResponse() {
        return new CreateFolderResponse();
    }

    /**
     * Create an instance of {@link CreateRelationshipResponse }
     * 
     */
    public CreateRelationshipResponse createCreateRelationshipResponse() {
        return new CreateRelationshipResponse();
    }

    /**
     * Create an instance of {@link GetContentStream }
     * 
     */
    public GetContentStream createGetContentStream() {
        return new GetContentStream();
    }

    /**
     * Create an instance of {@link GetACL }
     * 
     */
    public GetACL createGetACL() {
        return new GetACL();
    }

    /**
     * Create an instance of {@link ApplyACL }
     * 
     */
    public ApplyACL createApplyACL() {
        return new ApplyACL();
    }

    /**
     * Create an instance of {@link GetFolderParent }
     * 
     */
    public GetFolderParent createGetFolderParent() {
        return new GetFolderParent();
    }

    /**
     * Create an instance of {@link GetFolderByPath }
     * 
     */
    public GetFolderByPath createGetFolderByPath() {
        return new GetFolderByPath();
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link CmisPropertyString }
     * 
     */
    public CmisPropertyString createCmisPropertyString() {
        return new CmisPropertyString();
    }

    /**
     * Create an instance of {@link CmisPropertyBoolean }
     * 
     */
    public CmisPropertyBoolean createCmisPropertyBoolean() {
        return new CmisPropertyBoolean();
    }

    /**
     * Create an instance of {@link GetAppliedPoliciesResponse }
     * 
     */
    public GetAppliedPoliciesResponse createGetAppliedPoliciesResponse() {
        return new GetAppliedPoliciesResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyXhtmlDefinitionType }
     * 
     */
    public CmisPropertyXhtmlDefinitionType createCmisPropertyXhtmlDefinitionType() {
        return new CmisPropertyXhtmlDefinitionType();
    }

    /**
     * Create an instance of {@link GetTypeDefinitionResponse }
     * 
     */
    public GetTypeDefinitionResponse createGetTypeDefinitionResponse() {
        return new GetTypeDefinitionResponse();
    }

    /**
     * Create an instance of {@link GetCheckedOutDocs }
     * 
     */
    public GetCheckedOutDocs createGetCheckedOutDocs() {
        return new GetCheckedOutDocs();
    }

    /**
     * Create an instance of {@link ApplyPolicyResponse }
     * 
     */
    public ApplyPolicyResponse createApplyPolicyResponse() {
        return new ApplyPolicyResponse();
    }

    /**
     * Create an instance of {@link CreateDocumentResponse }
     * 
     */
    public CreateDocumentResponse createCreateDocumentResponse() {
        return new CreateDocumentResponse();
    }

    /**
     * Create an instance of {@link GetTypeChildren }
     * 
     */
    public GetTypeChildren createGetTypeChildren() {
        return new GetTypeChildren();
    }

    /**
     * Create an instance of {@link CmisPropertyIntegerDefinitionType }
     * 
     */
    public CmisPropertyIntegerDefinitionType createCmisPropertyIntegerDefinitionType() {
        return new CmisPropertyIntegerDefinitionType();
    }

    /**
     * Create an instance of {@link CmisQueryType }
     * 
     */
    public CmisQueryType createCmisQueryType() {
        return new CmisQueryType();
    }

    /**
     * Create an instance of {@link CmisPropertyXhtml.Value }
     * 
     */
    public CmisPropertyXhtml.Value createCmisPropertyXhtmlValue() {
        return new CmisPropertyXhtml.Value();
    }

    /**
     * Create an instance of {@link RemoveObjectFromFolder }
     * 
     */
    public RemoveObjectFromFolder createRemoveObjectFromFolder() {
        return new RemoveObjectFromFolder();
    }

    /**
     * Create an instance of {@link CmisAnyXml }
     * 
     */
    public CmisAnyXml createCmisAnyXml() {
        return new CmisAnyXml();
    }

    /**
     * Create an instance of {@link GetACLResponse }
     * 
     */
    public GetACLResponse createGetACLResponse() {
        return new GetACLResponse();
    }

    /**
     * Create an instance of {@link GetDescendantsResponse }
     * 
     */
    public GetDescendantsResponse createGetDescendantsResponse() {
        return new GetDescendantsResponse();
    }

    /**
     * Create an instance of {@link CmisPermissionMapping }
     * 
     */
    public CmisPermissionMapping createCmisPermissionMapping() {
        return new CmisPermissionMapping();
    }

    /**
     * Create an instance of {@link CmisRenditionType }
     * 
     */
    public CmisRenditionType createCmisRenditionType() {
        return new CmisRenditionType();
    }

    /**
     * Create an instance of {@link CmisChoiceHtml }
     * 
     */
    public CmisChoiceHtml createCmisChoiceHtml() {
        return new CmisChoiceHtml();
    }

    /**
     * Create an instance of {@link CmisPropertyHtml.Value }
     * 
     */
    public CmisPropertyHtml.Value createCmisPropertyHtmlValue() {
        return new CmisPropertyHtml.Value();
    }

    /**
     * Create an instance of {@link GetPropertiesResponse }
     * 
     */
    public GetPropertiesResponse createGetPropertiesResponse() {
        return new GetPropertiesResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyXmlDefinitionType }
     * 
     */
    public CmisPropertyXmlDefinitionType createCmisPropertyXmlDefinitionType() {
        return new CmisPropertyXmlDefinitionType();
    }

    /**
     * Create an instance of {@link CmisTypeContainer }
     * 
     */
    public CmisTypeContainer createCmisTypeContainer() {
        return new CmisTypeContainer();
    }

    /**
     * Create an instance of {@link CmisChoice }
     * 
     */
    public CmisChoice createCmisChoice() {
        return new CmisChoice();
    }

    /**
     * Create an instance of {@link AddObjectToFolderResponse }
     * 
     */
    public AddObjectToFolderResponse createAddObjectToFolderResponse() {
        return new AddObjectToFolderResponse();
    }

    /**
     * Create an instance of {@link CmisObjectType }
     * 
     */
    public CmisObjectType createCmisObjectType() {
        return new CmisObjectType();
    }

    /**
     * Create an instance of {@link QueryResponse }
     * 
     */
    public QueryResponse createQueryResponse() {
        return new QueryResponse();
    }

    /**
     * Create an instance of {@link CmisRepositoryCapabilitiesType }
     * 
     */
    public CmisRepositoryCapabilitiesType createCmisRepositoryCapabilitiesType() {
        return new CmisRepositoryCapabilitiesType();
    }

    /**
     * Create an instance of {@link CmisPropertyUri }
     * 
     */
    public CmisPropertyUri createCmisPropertyUri() {
        return new CmisPropertyUri();
    }

    /**
     * Create an instance of {@link RemovePolicyResponse }
     * 
     */
    public RemovePolicyResponse createRemovePolicyResponse() {
        return new RemovePolicyResponse();
    }

    /**
     * Create an instance of {@link CmisTypeDocumentDefinitionType }
     * 
     */
    public CmisTypeDocumentDefinitionType createCmisTypeDocumentDefinitionType() {
        return new CmisTypeDocumentDefinitionType();
    }

    /**
     * Create an instance of {@link DeleteTree }
     * 
     */
    public DeleteTree createDeleteTree() {
        return new DeleteTree();
    }

    /**
     * Create an instance of {@link CreateFolder }
     * 
     */
    public CreateFolder createCreateFolder() {
        return new CreateFolder();
    }

    /**
     * Create an instance of {@link CmisChoiceInteger }
     * 
     */
    public CmisChoiceInteger createCmisChoiceInteger() {
        return new CmisChoiceInteger();
    }

    /**
     * Create an instance of {@link GetRepositoryInfo }
     * 
     */
    public GetRepositoryInfo createGetRepositoryInfo() {
        return new GetRepositoryInfo();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRelationships", scope = GetDescendants.class)
    public JAXBElement<EnumIncludeRelationships> createGetDescendantsIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetDescendantsIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeAllowableActions", scope = GetDescendants.class)
    public JAXBElement<Boolean> createGetDescendantsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeAllowableActions_QNAME, Boolean.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetDescendants.class)
    public JAXBElement<String> createGetDescendantsFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRenditions", scope = GetDescendants.class)
    public JAXBElement<Boolean> createGetDescendantsIncludeRenditions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeRenditions_QNAME, Boolean.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "depth", scope = GetDescendants.class)
    public JAXBElement<BigInteger> createGetDescendantsDepth(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetDescendantsDepth_QNAME, BigInteger.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "folderId", scope = GetCheckedOutDocs.class)
    public JAXBElement<String> createGetCheckedOutDocsFolderId(String value) {
        return new JAXBElement<String>(_GetCheckedOutDocsFolderId_QNAME, String.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRelationships", scope = GetCheckedOutDocs.class)
    public JAXBElement<EnumIncludeRelationships> createGetCheckedOutDocsIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetDescendantsIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeAllowableActions", scope = GetCheckedOutDocs.class)
    public JAXBElement<Boolean> createGetCheckedOutDocsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeAllowableActions_QNAME, Boolean.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetCheckedOutDocs.class)
    public JAXBElement<String> createGetCheckedOutDocsFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "skipCount", scope = GetCheckedOutDocs.class)
    public JAXBElement<BigInteger> createGetCheckedOutDocsSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsSkipCount_QNAME, BigInteger.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "orderBy", scope = GetCheckedOutDocs.class)
    public JAXBElement<String> createGetCheckedOutDocsOrderBy(String value) {
        return new JAXBElement<String>(_GetCheckedOutDocsOrderBy_QNAME, String.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "maxItems", scope = GetCheckedOutDocs.class)
    public JAXBElement<BigInteger> createGetCheckedOutDocsMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsMaxItems_QNAME, BigInteger.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "skipCount", scope = GetRenditions.class)
    public JAXBElement<BigInteger> createGetRenditionsSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsSkipCount_QNAME, BigInteger.class, GetRenditions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "maxItems", scope = GetRenditions.class)
    public JAXBElement<BigInteger> createGetRenditionsMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsMaxItems_QNAME, BigInteger.class, GetRenditions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeACL", scope = GetChildren.class)
    public JAXBElement<Boolean> createGetChildrenIncludeACL(Boolean value) {
        return new JAXBElement<Boolean>(_GetChildrenIncludeACL_QNAME, Boolean.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRelationships", scope = GetChildren.class)
    public JAXBElement<EnumIncludeRelationships> createGetChildrenIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetDescendantsIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeAllowableActions", scope = GetChildren.class)
    public JAXBElement<Boolean> createGetChildrenIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeAllowableActions_QNAME, Boolean.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetChildren.class)
    public JAXBElement<String> createGetChildrenFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRenditions", scope = GetChildren.class)
    public JAXBElement<Boolean> createGetChildrenIncludeRenditions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeRenditions_QNAME, Boolean.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "skipCount", scope = GetChildren.class)
    public JAXBElement<BigInteger> createGetChildrenSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsSkipCount_QNAME, BigInteger.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "maxItems", scope = GetChildren.class)
    public JAXBElement<BigInteger> createGetChildrenMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsMaxItems_QNAME, BigInteger.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "changeToken", scope = UpdateProperties.class)
    public JAXBElement<String> createUpdatePropertiesChangeToken(String value) {
        return new JAXBElement<String>(_UpdatePropertiesChangeToken_QNAME, String.class, UpdateProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "typeId", scope = GetTypeChildren.class)
    public JAXBElement<String> createGetTypeChildrenTypeId(String value) {
        return new JAXBElement<String>(_GetTypeChildrenTypeId_QNAME, String.class, GetTypeChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includePropertyDefinitions", scope = GetTypeChildren.class)
    public JAXBElement<Boolean> createGetTypeChildrenIncludePropertyDefinitions(Boolean value) {
        return new JAXBElement<Boolean>(_GetTypeChildrenIncludePropertyDefinitions_QNAME, Boolean.class, GetTypeChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "skipCount", scope = GetTypeChildren.class)
    public JAXBElement<BigInteger> createGetTypeChildrenSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsSkipCount_QNAME, BigInteger.class, GetTypeChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "maxItems", scope = GetTypeChildren.class)
    public JAXBElement<BigInteger> createGetTypeChildrenMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsMaxItems_QNAME, BigInteger.class, GetTypeChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "overwriteFlag", scope = SetContentStream.class)
    public JAXBElement<Boolean> createSetContentStreamOverwriteFlag(Boolean value) {
        return new JAXBElement<Boolean>(_SetContentStreamOverwriteFlag_QNAME, Boolean.class, SetContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "removeACEs", scope = CreateRelationship.class)
    public JAXBElement<CmisAccessControlListType> createCreateRelationshipRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateRelationshipRemoveACEs_QNAME, CmisAccessControlListType.class, CreateRelationship.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "addACEs", scope = CreateRelationship.class)
    public JAXBElement<CmisAccessControlListType> createCreateRelationshipAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateRelationshipAddACEs_QNAME, CmisAccessControlListType.class, CreateRelationship.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", name = "includeAllowableActions", scope = CmisQueryType.class)
    public JAXBElement<Boolean> createCmisQueryTypeIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_CmisQueryTypeIncludeAllowableActions_QNAME, Boolean.class, CmisQueryType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", name = "includeRenditions", scope = CmisQueryType.class)
    public JAXBElement<Boolean> createCmisQueryTypeIncludeRenditions(Boolean value) {
        return new JAXBElement<Boolean>(_CmisQueryTypeIncludeRenditions_QNAME, Boolean.class, CmisQueryType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", name = "includeRelationships", scope = CmisQueryType.class)
    public JAXBElement<EnumIncludeRelationships> createCmisQueryTypeIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_CmisQueryTypeIncludeRelationships_QNAME, EnumIncludeRelationships.class, CmisQueryType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetAppliedPolicies.class)
    public JAXBElement<String> createGetAppliedPoliciesFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetAppliedPolicies.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "folderId", scope = RemoveObjectFromFolder.class)
    public JAXBElement<String> createRemoveObjectFromFolderFolderId(String value) {
        return new JAXBElement<String>(_GetCheckedOutDocsFolderId_QNAME, String.class, RemoveObjectFromFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeACL", scope = GetProperties.class)
    public JAXBElement<Boolean> createGetPropertiesIncludeACL(Boolean value) {
        return new JAXBElement<Boolean>(_GetChildrenIncludeACL_QNAME, Boolean.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRelationships", scope = GetProperties.class)
    public JAXBElement<EnumIncludeRelationships> createGetPropertiesIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetDescendantsIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeAllowableActions", scope = GetProperties.class)
    public JAXBElement<Boolean> createGetPropertiesIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeAllowableActions_QNAME, Boolean.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetProperties.class)
    public JAXBElement<String> createGetPropertiesFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "allVersions", scope = DeleteObject.class)
    public JAXBElement<Boolean> createDeleteObjectAllVersions(Boolean value) {
        return new JAXBElement<Boolean>(_DeleteObjectAllVersions_QNAME, Boolean.class, DeleteObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "typeId", scope = GetTypeDescendants.class)
    public JAXBElement<String> createGetTypeDescendantsTypeId(String value) {
        return new JAXBElement<String>(_GetTypeChildrenTypeId_QNAME, String.class, GetTypeDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includePropertyDefinitions", scope = GetTypeDescendants.class)
    public JAXBElement<Boolean> createGetTypeDescendantsIncludePropertyDefinitions(Boolean value) {
        return new JAXBElement<Boolean>(_GetTypeChildrenIncludePropertyDefinitions_QNAME, Boolean.class, GetTypeDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "depth", scope = GetTypeDescendants.class)
    public JAXBElement<BigInteger> createGetTypeDescendantsDepth(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetDescendantsDepth_QNAME, BigInteger.class, GetTypeDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRelationships", scope = GetFolderTree.class)
    public JAXBElement<EnumIncludeRelationships> createGetFolderTreeIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetDescendantsIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeAllowableActions", scope = GetFolderTree.class)
    public JAXBElement<Boolean> createGetFolderTreeIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeAllowableActions_QNAME, Boolean.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetFolderTree.class)
    public JAXBElement<String> createGetFolderTreeFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "depth", scope = GetFolderTree.class)
    public JAXBElement<BigInteger> createGetFolderTreeDepth(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetDescendantsDepth_QNAME, BigInteger.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "contentStream", scope = CreateDocument.class)
    public JAXBElement<CmisContentStreamType> createCreateDocumentContentStream(CmisContentStreamType value) {
        return new JAXBElement<CmisContentStreamType>(_CreateDocumentContentStream_QNAME, CmisContentStreamType.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "removeACEs", scope = CreateDocument.class)
    public JAXBElement<CmisAccessControlListType> createCreateDocumentRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateRelationshipRemoveACEs_QNAME, CmisAccessControlListType.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "addACEs", scope = CreateDocument.class)
    public JAXBElement<CmisAccessControlListType> createCreateDocumentAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateRelationshipAddACEs_QNAME, CmisAccessControlListType.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumVersioningState }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "versioningState", scope = CreateDocument.class)
    public JAXBElement<EnumVersioningState> createCreateDocumentVersioningState(EnumVersioningState value) {
        return new JAXBElement<EnumVersioningState>(_CreateDocumentVersioningState_QNAME, EnumVersioningState.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRelationships", scope = GetAllVersions.class)
    public JAXBElement<EnumIncludeRelationships> createGetAllVersionsIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetDescendantsIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetAllVersions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeAllowableActions", scope = GetAllVersions.class)
    public JAXBElement<Boolean> createGetAllVersionsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeAllowableActions_QNAME, Boolean.class, GetAllVersions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetAllVersions.class)
    public JAXBElement<String> createGetAllVersionsFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetAllVersions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "folderId", scope = CreatePolicy.class)
    public JAXBElement<String> createCreatePolicyFolderId(String value) {
        return new JAXBElement<String>(_GetCheckedOutDocsFolderId_QNAME, String.class, CreatePolicy.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "typeId", scope = GetRelationships.class)
    public JAXBElement<String> createGetRelationshipsTypeId(String value) {
        return new JAXBElement<String>(_GetTypeChildrenTypeId_QNAME, String.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRelationships", scope = GetRelationships.class)
    public JAXBElement<EnumIncludeRelationships> createGetRelationshipsIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetDescendantsIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeAllowableActions", scope = GetRelationships.class)
    public JAXBElement<Boolean> createGetRelationshipsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeAllowableActions_QNAME, Boolean.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetRelationships.class)
    public JAXBElement<String> createGetRelationshipsFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeSubRelationshipTypes", scope = GetRelationships.class)
    public JAXBElement<Boolean> createGetRelationshipsIncludeSubRelationshipTypes(Boolean value) {
        return new JAXBElement<Boolean>(_GetRelationshipsIncludeSubRelationshipTypes_QNAME, Boolean.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumRelationshipDirection }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "direction", scope = GetRelationships.class)
    public JAXBElement<EnumRelationshipDirection> createGetRelationshipsDirection(EnumRelationshipDirection value) {
        return new JAXBElement<EnumRelationshipDirection>(_GetRelationshipsDirection_QNAME, EnumRelationshipDirection.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "skipCount", scope = GetRelationships.class)
    public JAXBElement<BigInteger> createGetRelationshipsSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsSkipCount_QNAME, BigInteger.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "maxItems", scope = GetRelationships.class)
    public JAXBElement<BigInteger> createGetRelationshipsMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsMaxItems_QNAME, BigInteger.class, GetRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeACL", scope = GetContentChanges.class)
    public JAXBElement<Boolean> createGetContentChangesIncludeACL(Boolean value) {
        return new JAXBElement<Boolean>(_GetChildrenIncludeACL_QNAME, Boolean.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeProperties", scope = GetContentChanges.class)
    public JAXBElement<Boolean> createGetContentChangesIncludeProperties(Boolean value) {
        return new JAXBElement<Boolean>(_GetContentChangesIncludeProperties_QNAME, Boolean.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "maxItems", scope = GetContentChanges.class)
    public JAXBElement<BigInteger> createGetContentChangesMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetCheckedOutDocsMaxItems_QNAME, BigInteger.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAllowableActionsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", name = "allowableActions")
    public JAXBElement<CmisAllowableActionsType> createAllowableActions(CmisAllowableActionsType value) {
        return new JAXBElement<CmisAllowableActionsType>(_AllowableActions_QNAME, CmisAllowableActionsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisFaultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "cmisFault")
    public JAXBElement<CmisFaultType> createCmisFault(CmisFaultType value) {
        return new JAXBElement<CmisFaultType>(_CmisFault_QNAME, CmisFaultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", name = "acl")
    public JAXBElement<CmisAccessControlListType> createAcl(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_Acl_QNAME, CmisAccessControlListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisQueryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/core/200901", name = "query")
    public JAXBElement<CmisQueryType> createQuery(CmisQueryType value) {
        return new JAXBElement<CmisQueryType>(_Query_QNAME, CmisQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeACL", scope = GetPropertiesOfLatestVersion.class)
    public JAXBElement<Boolean> createGetPropertiesOfLatestVersionIncludeACL(Boolean value) {
        return new JAXBElement<Boolean>(_GetChildrenIncludeACL_QNAME, Boolean.class, GetPropertiesOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetPropertiesOfLatestVersion.class)
    public JAXBElement<String> createGetPropertiesOfLatestVersionFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetPropertiesOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "checkinComment", scope = CheckIn.class)
    public JAXBElement<String> createCheckInCheckinComment(String value) {
        return new JAXBElement<String>(_CheckInCheckinComment_QNAME, String.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "contentStream", scope = CheckIn.class)
    public JAXBElement<CmisContentStreamType> createCheckInContentStream(CmisContentStreamType value) {
        return new JAXBElement<CmisContentStreamType>(_CreateDocumentContentStream_QNAME, CmisContentStreamType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "removeACEs", scope = CheckIn.class)
    public JAXBElement<CmisAccessControlListType> createCheckInRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateRelationshipRemoveACEs_QNAME, CmisAccessControlListType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "addACEs", scope = CheckIn.class)
    public JAXBElement<CmisAccessControlListType> createCheckInAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateRelationshipAddACEs_QNAME, CmisAccessControlListType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "properties", scope = CheckIn.class)
    public JAXBElement<CmisPropertiesType> createCheckInProperties(CmisPropertiesType value) {
        return new JAXBElement<CmisPropertiesType>(_CheckInProperties_QNAME, CmisPropertiesType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "major", scope = CheckIn.class)
    public JAXBElement<Boolean> createCheckInMajor(Boolean value) {
        return new JAXBElement<Boolean>(_CheckInMajor_QNAME, Boolean.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "removeACEs", scope = CreateFolder.class)
    public JAXBElement<CmisAccessControlListType> createCreateFolderRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateRelationshipRemoveACEs_QNAME, CmisAccessControlListType.class, CreateFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "addACEs", scope = CreateFolder.class)
    public JAXBElement<CmisAccessControlListType> createCreateFolderAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateRelationshipAddACEs_QNAME, CmisAccessControlListType.class, CreateFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "continueOnFailure", scope = DeleteTree.class)
    public JAXBElement<Boolean> createDeleteTreeContinueOnFailure(Boolean value) {
        return new JAXBElement<Boolean>(_DeleteTreeContinueOnFailure_QNAME, Boolean.class, DeleteTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumACLPropagation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "propogationType", scope = ApplyACL.class)
    public JAXBElement<EnumACLPropagation> createApplyACLPropogationType(EnumACLPropagation value) {
        return new JAXBElement<EnumACLPropagation>(_ApplyACLPropogationType_QNAME, EnumACLPropagation.class, ApplyACL.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeACL", scope = GetFolderByPath.class)
    public JAXBElement<Boolean> createGetFolderByPathIncludeACL(Boolean value) {
        return new JAXBElement<Boolean>(_GetChildrenIncludeACL_QNAME, Boolean.class, GetFolderByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRelationships", scope = GetFolderByPath.class)
    public JAXBElement<EnumIncludeRelationships> createGetFolderByPathIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetDescendantsIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetFolderByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeAllowableActions", scope = GetFolderByPath.class)
    public JAXBElement<Boolean> createGetFolderByPathIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeAllowableActions_QNAME, Boolean.class, GetFolderByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "filter", scope = GetFolderByPath.class)
    public JAXBElement<String> createGetFolderByPathFilter(String value) {
        return new JAXBElement<String>(_GetDescendantsFilter_QNAME, String.class, GetFolderByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRelationships", scope = Query.class)
    public JAXBElement<EnumIncludeRelationships> createQueryIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetDescendantsIncludeRelationships_QNAME, EnumIncludeRelationships.class, Query.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeAllowableActions", scope = Query.class)
    public JAXBElement<Boolean> createQueryIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeAllowableActions_QNAME, Boolean.class, Query.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", name = "includeRenditions", scope = Query.class)
    public JAXBElement<Boolean> createQueryIncludeRenditions(Boolean value) {
        return new JAXBElement<Boolean>(_GetDescendantsIncludeRenditions_QNAME, Boolean.class, Query.class, value);
    }

}
