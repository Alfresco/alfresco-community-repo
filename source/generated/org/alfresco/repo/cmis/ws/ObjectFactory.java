
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

    private final static QName _Query_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/core/200908/", "query");
    private final static QName _CmisFault_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "cmisFault");
    private final static QName _Acl_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/core/200908/", "acl");
    private final static QName _AllowableActions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/core/200908/", "allowableActions");
    private final static QName _CreateDocumentFromSourceRemoveACEs_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "removeACEs");
    private final static QName _CreateDocumentFromSourceFolderId_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "folderId");
    private final static QName _CreateDocumentFromSourceExtension_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "extension");
    private final static QName _CreateDocumentFromSourceAddACEs_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "addACEs");
    private final static QName _CreateDocumentFromSourceVersioningState_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "versioningState");
    private final static QName _ApplyACLACLPropagation_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "ACLPropagation");
    private final static QName _GetObjectIncludeRelationships_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "includeRelationships");
    private final static QName _GetObjectIncludeAllowableActions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "includeAllowableActions");
    private final static QName _GetObjectIncludeACL_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "includeACL");
    private final static QName _GetObjectRenditionFilter_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "renditionFilter");
    private final static QName _GetObjectFilter_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "filter");
    private final static QName _GetObjectIncludePolicyIds_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "includePolicyIds");
    private final static QName _DeleteObjectAllVersions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "allVersions");
    private final static QName _QuerySkipCount_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "skipCount");
    private final static QName _QueryMaxItems_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "maxItems");
    private final static QName _QuerySearchAllVersions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "searchAllVersions");
    private final static QName _CheckInContentStream_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "contentStream");
    private final static QName _CheckInMajor_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "major");
    private final static QName _CheckInProperties_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "properties");
    private final static QName _CheckInCheckinComment_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "checkinComment");
    private final static QName _GetContentChangesIncludeProperties_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "includeProperties");
    private final static QName _GetContentChangesChangeLogToken_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "changeLogToken");
    private final static QName _GetFolderTreeDepth_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "depth");
    private final static QName _GetFolderTreeIncludePathSegment_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "includePathSegment");
    private final static QName _GetObjectParentsIncludeRelativePathSegment_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "includeRelativePathSegment");
    private final static QName _SetContentStreamChangeToken_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "changeToken");
    private final static QName _SetContentStreamOverwriteFlag_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "overwriteFlag");
    private final static QName _GetTypeChildrenTypeId_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "typeId");
    private final static QName _GetTypeChildrenIncludePropertyDefinitions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "includePropertyDefinitions");
    private final static QName _GetCheckedOutDocsOrderBy_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "orderBy");
    private final static QName _GetContentStreamOffset_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "offset");
    private final static QName _GetContentStreamLength_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "length");
    private final static QName _GetContentStreamStreamId_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "streamId");
    private final static QName _GetObjectRelationshipsRelationshipDirection_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "relationshipDirection");
    private final static QName _DeleteTreeContinueOnFailure_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "continueOnFailure");
    private final static QName _DeleteTreeUnfileObjects_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "unfileObjects");
    private final static QName _GetACLOnlyBasicPermissions_QNAME = new QName("http://docs.oasis-open.org/ns/cmis/messaging/200908/", "onlyBasicPermissions");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.alfresco.repo.cmis.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ApplyACL }
     * 
     */
    public ApplyACL createApplyACL() {
        return new ApplyACL();
    }

    /**
     * Create an instance of {@link GetACLResponse }
     * 
     */
    public GetACLResponse createGetACLResponse() {
        return new GetACLResponse();
    }

    /**
     * Create an instance of {@link CmisTypeDefinitionListType }
     * 
     */
    public CmisTypeDefinitionListType createCmisTypeDefinitionListType() {
        return new CmisTypeDefinitionListType();
    }

    /**
     * Create an instance of {@link GetAllowableActionsResponse }
     * 
     */
    public GetAllowableActionsResponse createGetAllowableActionsResponse() {
        return new GetAllowableActionsResponse();
    }

    /**
     * Create an instance of {@link GetRepositoryInfo }
     * 
     */
    public GetRepositoryInfo createGetRepositoryInfo() {
        return new GetRepositoryInfo();
    }

    /**
     * Create an instance of {@link ApplyPolicyResponse }
     * 
     */
    public ApplyPolicyResponse createApplyPolicyResponse() {
        return new ApplyPolicyResponse();
    }

    /**
     * Create an instance of {@link GetObjectParentsResponse }
     * 
     */
    public GetObjectParentsResponse createGetObjectParentsResponse() {
        return new GetObjectParentsResponse();
    }

    /**
     * Create an instance of {@link DeleteObject }
     * 
     */
    public DeleteObject createDeleteObject() {
        return new DeleteObject();
    }

    /**
     * Create an instance of {@link GetRepositoryInfoResponse }
     * 
     */
    public GetRepositoryInfoResponse createGetRepositoryInfoResponse() {
        return new GetRepositoryInfoResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyDecimalDefinitionType }
     * 
     */
    public CmisPropertyDecimalDefinitionType createCmisPropertyDecimalDefinitionType() {
        return new CmisPropertyDecimalDefinitionType();
    }

    /**
     * Create an instance of {@link CmisTypeContainer }
     * 
     */
    public CmisTypeContainer createCmisTypeContainer() {
        return new CmisTypeContainer();
    }

    /**
     * Create an instance of {@link CmisRepositoryInfoType }
     * 
     */
    public CmisRepositoryInfoType createCmisRepositoryInfoType() {
        return new CmisRepositoryInfoType();
    }

    /**
     * Create an instance of {@link GetPropertiesOfLatestVersionResponse }
     * 
     */
    public GetPropertiesOfLatestVersionResponse createGetPropertiesOfLatestVersionResponse() {
        return new GetPropertiesOfLatestVersionResponse();
    }

    /**
     * Create an instance of {@link CmisTypeRelationshipDefinitionType }
     * 
     */
    public CmisTypeRelationshipDefinitionType createCmisTypeRelationshipDefinitionType() {
        return new CmisTypeRelationshipDefinitionType();
    }

    /**
     * Create an instance of {@link CmisTypeDefinitionType }
     * 
     */
    public CmisTypeDefinitionType createCmisTypeDefinitionType() {
        return new CmisTypeDefinitionType();
    }

    /**
     * Create an instance of {@link GetAllVersionsResponse }
     * 
     */
    public GetAllVersionsResponse createGetAllVersionsResponse() {
        return new GetAllVersionsResponse();
    }

    /**
     * Create an instance of {@link GetContentChanges }
     * 
     */
    public GetContentChanges createGetContentChanges() {
        return new GetContentChanges();
    }

    /**
     * Create an instance of {@link CmisAccessControlPrincipalType }
     * 
     */
    public CmisAccessControlPrincipalType createCmisAccessControlPrincipalType() {
        return new CmisAccessControlPrincipalType();
    }

    /**
     * Create an instance of {@link CmisPropertyDecimal }
     * 
     */
    public CmisPropertyDecimal createCmisPropertyDecimal() {
        return new CmisPropertyDecimal();
    }

    /**
     * Create an instance of {@link CmisPropertyHtml }
     * 
     */
    public CmisPropertyHtml createCmisPropertyHtml() {
        return new CmisPropertyHtml();
    }

    /**
     * Create an instance of {@link CmisProperty }
     * 
     */
    public CmisProperty createCmisProperty() {
        return new CmisProperty();
    }

    /**
     * Create an instance of {@link GetDescendantsResponse }
     * 
     */
    public GetDescendantsResponse createGetDescendantsResponse() {
        return new GetDescendantsResponse();
    }

    /**
     * Create an instance of {@link GetContentStreamResponse }
     * 
     */
    public GetContentStreamResponse createGetContentStreamResponse() {
        return new GetContentStreamResponse();
    }

    /**
     * Create an instance of {@link GetFolderParentResponse }
     * 
     */
    public GetFolderParentResponse createGetFolderParentResponse() {
        return new GetFolderParentResponse();
    }

    /**
     * Create an instance of {@link CmisRenditionType }
     * 
     */
    public CmisRenditionType createCmisRenditionType() {
        return new CmisRenditionType();
    }

    /**
     * Create an instance of {@link CmisPropertyDateTime }
     * 
     */
    public CmisPropertyDateTime createCmisPropertyDateTime() {
        return new CmisPropertyDateTime();
    }

    /**
     * Create an instance of {@link GetFolderTree }
     * 
     */
    public GetFolderTree createGetFolderTree() {
        return new GetFolderTree();
    }

    /**
     * Create an instance of {@link DeleteContentStream }
     * 
     */
    public DeleteContentStream createDeleteContentStream() {
        return new DeleteContentStream();
    }

    /**
     * Create an instance of {@link GetObjectParents }
     * 
     */
    public GetObjectParents createGetObjectParents() {
        return new GetObjectParents();
    }

    /**
     * Create an instance of {@link CmisACLCapabilityType }
     * 
     */
    public CmisACLCapabilityType createCmisACLCapabilityType() {
        return new CmisACLCapabilityType();
    }

    /**
     * Create an instance of {@link CmisChoiceUri }
     * 
     */
    public CmisChoiceUri createCmisChoiceUri() {
        return new CmisChoiceUri();
    }

    /**
     * Create an instance of {@link AddObjectToFolderResponse }
     * 
     */
    public AddObjectToFolderResponse createAddObjectToFolderResponse() {
        return new AddObjectToFolderResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyHtmlDefinitionType }
     * 
     */
    public CmisPropertyHtmlDefinitionType createCmisPropertyHtmlDefinitionType() {
        return new CmisPropertyHtmlDefinitionType();
    }

    /**
     * Create an instance of {@link GetFolderParent }
     * 
     */
    public GetFolderParent createGetFolderParent() {
        return new GetFolderParent();
    }

    /**
     * Create an instance of {@link GetObjectResponse }
     * 
     */
    public GetObjectResponse createGetObjectResponse() {
        return new GetObjectResponse();
    }

    /**
     * Create an instance of {@link CmisACLType }
     * 
     */
    public CmisACLType createCmisACLType() {
        return new CmisACLType();
    }

    /**
     * Create an instance of {@link GetTypeChildren }
     * 
     */
    public GetTypeChildren createGetTypeChildren() {
        return new GetTypeChildren();
    }

    /**
     * Create an instance of {@link CmisPropertyBoolean }
     * 
     */
    public CmisPropertyBoolean createCmisPropertyBoolean() {
        return new CmisPropertyBoolean();
    }

    /**
     * Create an instance of {@link ApplyACLResponse }
     * 
     */
    public ApplyACLResponse createApplyACLResponse() {
        return new ApplyACLResponse();
    }

    /**
     * Create an instance of {@link SetAspects }
     * 
     */
    public SetAspects createSetAspects() {
        return new SetAspects();
    }

    /**
     * Create an instance of {@link CheckOutResponse }
     * 
     */
    public CheckOutResponse createCheckOutResponse() {
        return new CheckOutResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyIntegerDefinitionType }
     * 
     */
    public CmisPropertyIntegerDefinitionType createCmisPropertyIntegerDefinitionType() {
        return new CmisPropertyIntegerDefinitionType();
    }

    /**
     * Create an instance of {@link CmisChoiceDecimal }
     * 
     */
    public CmisChoiceDecimal createCmisChoiceDecimal() {
        return new CmisChoiceDecimal();
    }

    /**
     * Create an instance of {@link GetRepositoriesResponse }
     * 
     */
    public GetRepositoriesResponse createGetRepositoriesResponse() {
        return new GetRepositoriesResponse();
    }

    /**
     * Create an instance of {@link CmisRepositoryCapabilitiesType }
     * 
     */
    public CmisRepositoryCapabilitiesType createCmisRepositoryCapabilitiesType() {
        return new CmisRepositoryCapabilitiesType();
    }

    /**
     * Create an instance of {@link SetContentStreamResponse }
     * 
     */
    public SetContentStreamResponse createSetContentStreamResponse() {
        return new SetContentStreamResponse();
    }

    /**
     * Create an instance of {@link CmisPermissionDefinition }
     * 
     */
    public CmisPermissionDefinition createCmisPermissionDefinition() {
        return new CmisPermissionDefinition();
    }

    /**
     * Create an instance of {@link CmisQueryType }
     * 
     */
    public CmisQueryType createCmisQueryType() {
        return new CmisQueryType();
    }

    /**
     * Create an instance of {@link GetTypeDescendantsResponse }
     * 
     */
    public GetTypeDescendantsResponse createGetTypeDescendantsResponse() {
        return new GetTypeDescendantsResponse();
    }

    /**
     * Create an instance of {@link GetTypeDefinition }
     * 
     */
    public GetTypeDefinition createGetTypeDefinition() {
        return new GetTypeDefinition();
    }

    /**
     * Create an instance of {@link CmisContentStreamType }
     * 
     */
    public CmisContentStreamType createCmisContentStreamType() {
        return new CmisContentStreamType();
    }

    /**
     * Create an instance of {@link RemovePolicy }
     * 
     */
    public RemovePolicy createRemovePolicy() {
        return new RemovePolicy();
    }

    /**
     * Create an instance of {@link CmisTypePolicyDefinitionType }
     * 
     */
    public CmisTypePolicyDefinitionType createCmisTypePolicyDefinitionType() {
        return new CmisTypePolicyDefinitionType();
    }

    /**
     * Create an instance of {@link GetContentStream }
     * 
     */
    public GetContentStream createGetContentStream() {
        return new GetContentStream();
    }

    /**
     * Create an instance of {@link MoveObjectResponse }
     * 
     */
    public MoveObjectResponse createMoveObjectResponse() {
        return new MoveObjectResponse();
    }

    /**
     * Create an instance of {@link GetObjectByPath }
     * 
     */
    public GetObjectByPath createGetObjectByPath() {
        return new GetObjectByPath();
    }

    /**
     * Create an instance of {@link CmisPropertiesType }
     * 
     */
    public CmisPropertiesType createCmisPropertiesType() {
        return new CmisPropertiesType();
    }

    /**
     * Create an instance of {@link CmisChoice }
     * 
     */
    public CmisChoice createCmisChoice() {
        return new CmisChoice();
    }

    /**
     * Create an instance of {@link UpdatePropertiesResponse }
     * 
     */
    public UpdatePropertiesResponse createUpdatePropertiesResponse() {
        return new UpdatePropertiesResponse();
    }

    /**
     * Create an instance of {@link DeleteTree }
     * 
     */
    public DeleteTree createDeleteTree() {
        return new DeleteTree();
    }

    /**
     * Create an instance of {@link CancelCheckOutResponse }
     * 
     */
    public CancelCheckOutResponse createCancelCheckOutResponse() {
        return new CancelCheckOutResponse();
    }

    /**
     * Create an instance of {@link CmisRepositoryEntryType }
     * 
     */
    public CmisRepositoryEntryType createCmisRepositoryEntryType() {
        return new CmisRepositoryEntryType();
    }

    /**
     * Create an instance of {@link CmisPropertyInteger }
     * 
     */
    public CmisPropertyInteger createCmisPropertyInteger() {
        return new CmisPropertyInteger();
    }

    /**
     * Create an instance of {@link CmisPropertyUriDefinitionType }
     * 
     */
    public CmisPropertyUriDefinitionType createCmisPropertyUriDefinitionType() {
        return new CmisPropertyUriDefinitionType();
    }

    /**
     * Create an instance of {@link GetAppliedPolicies }
     * 
     */
    public GetAppliedPolicies createGetAppliedPolicies() {
        return new GetAppliedPolicies();
    }

    /**
     * Create an instance of {@link GetACL }
     * 
     */
    public GetACL createGetACL() {
        return new GetACL();
    }

    /**
     * Create an instance of {@link UpdateProperties }
     * 
     */
    public UpdateProperties createUpdateProperties() {
        return new UpdateProperties();
    }

    /**
     * Create an instance of {@link GetAllVersions }
     * 
     */
    public GetAllVersions createGetAllVersions() {
        return new GetAllVersions();
    }

    /**
     * Create an instance of {@link CmisPropertyBooleanDefinitionType }
     * 
     */
    public CmisPropertyBooleanDefinitionType createCmisPropertyBooleanDefinitionType() {
        return new CmisPropertyBooleanDefinitionType();
    }

    /**
     * Create an instance of {@link CmisAllowableActionsType }
     * 
     */
    public CmisAllowableActionsType createCmisAllowableActionsType() {
        return new CmisAllowableActionsType();
    }

    /**
     * Create an instance of {@link CreateDocument }
     * 
     */
    public CreateDocument createCreateDocument() {
        return new CreateDocument();
    }

    /**
     * Create an instance of {@link CreateRelationship }
     * 
     */
    public CreateRelationship createCreateRelationship() {
        return new CreateRelationship();
    }

    /**
     * Create an instance of {@link CmisChoiceString }
     * 
     */
    public CmisChoiceString createCmisChoiceString() {
        return new CmisChoiceString();
    }

    /**
     * Create an instance of {@link GetPropertiesOfLatestVersion }
     * 
     */
    public GetPropertiesOfLatestVersion createGetPropertiesOfLatestVersion() {
        return new GetPropertiesOfLatestVersion();
    }

    /**
     * Create an instance of {@link GetRenditionsResponse }
     * 
     */
    public GetRenditionsResponse createGetRenditionsResponse() {
        return new GetRenditionsResponse();
    }

    /**
     * Create an instance of {@link CmisAccessControlListType }
     * 
     */
    public CmisAccessControlListType createCmisAccessControlListType() {
        return new CmisAccessControlListType();
    }

    /**
     * Create an instance of {@link CmisPropertyId }
     * 
     */
    public CmisPropertyId createCmisPropertyId() {
        return new CmisPropertyId();
    }

    /**
     * Create an instance of {@link GetRepositories }
     * 
     */
    public GetRepositories createGetRepositories() {
        return new GetRepositories();
    }

    /**
     * Create an instance of {@link CmisTypeFolderDefinitionType }
     * 
     */
    public CmisTypeFolderDefinitionType createCmisTypeFolderDefinitionType() {
        return new CmisTypeFolderDefinitionType();
    }

    /**
     * Create an instance of {@link CmisFaultType }
     * 
     */
    public CmisFaultType createCmisFaultType() {
        return new CmisFaultType();
    }

    /**
     * Create an instance of {@link CmisPropertyString }
     * 
     */
    public CmisPropertyString createCmisPropertyString() {
        return new CmisPropertyString();
    }

    /**
     * Create an instance of {@link GetAppliedPoliciesResponse }
     * 
     */
    public GetAppliedPoliciesResponse createGetAppliedPoliciesResponse() {
        return new GetAppliedPoliciesResponse();
    }

    /**
     * Create an instance of {@link CmisChoiceBoolean }
     * 
     */
    public CmisChoiceBoolean createCmisChoiceBoolean() {
        return new CmisChoiceBoolean();
    }

    /**
     * Create an instance of {@link CreateDocumentFromSource }
     * 
     */
    public CreateDocumentFromSource createCreateDocumentFromSource() {
        return new CreateDocumentFromSource();
    }

    /**
     * Create an instance of {@link GetFolderTreeResponse }
     * 
     */
    public GetFolderTreeResponse createGetFolderTreeResponse() {
        return new GetFolderTreeResponse();
    }

    /**
     * Create an instance of {@link GetObject }
     * 
     */
    public GetObject createGetObject() {
        return new GetObject();
    }

    /**
     * Create an instance of {@link GetContentChangesResponse }
     * 
     */
    public GetContentChangesResponse createGetContentChangesResponse() {
        return new GetContentChangesResponse();
    }

    /**
     * Create an instance of {@link CreateDocumentFromSourceResponse }
     * 
     */
    public CreateDocumentFromSourceResponse createCreateDocumentFromSourceResponse() {
        return new CreateDocumentFromSourceResponse();
    }

    /**
     * Create an instance of {@link DeleteObjectResponse }
     * 
     */
    public DeleteObjectResponse createDeleteObjectResponse() {
        return new DeleteObjectResponse();
    }

    /**
     * Create an instance of {@link GetCheckedOutDocsResponse }
     * 
     */
    public GetCheckedOutDocsResponse createGetCheckedOutDocsResponse() {
        return new GetCheckedOutDocsResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyDefinitionType }
     * 
     */
    public CmisPropertyDefinitionType createCmisPropertyDefinitionType() {
        return new CmisPropertyDefinitionType();
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link CmisObjectInFolderContainerType }
     * 
     */
    public CmisObjectInFolderContainerType createCmisObjectInFolderContainerType() {
        return new CmisObjectInFolderContainerType();
    }

    /**
     * Create an instance of {@link RemovePolicyResponse }
     * 
     */
    public RemovePolicyResponse createRemovePolicyResponse() {
        return new RemovePolicyResponse();
    }

    /**
     * Create an instance of {@link GetObjectOfLatestVersionResponse }
     * 
     */
    public GetObjectOfLatestVersionResponse createGetObjectOfLatestVersionResponse() {
        return new GetObjectOfLatestVersionResponse();
    }

    /**
     * Create an instance of {@link CmisPropertyDateTimeDefinitionType }
     * 
     */
    public CmisPropertyDateTimeDefinitionType createCmisPropertyDateTimeDefinitionType() {
        return new CmisPropertyDateTimeDefinitionType();
    }

    /**
     * Create an instance of {@link CheckIn }
     * 
     */
    public CheckIn createCheckIn() {
        return new CheckIn();
    }

    /**
     * Create an instance of {@link CmisChoiceId }
     * 
     */
    public CmisChoiceId createCmisChoiceId() {
        return new CmisChoiceId();
    }

    /**
     * Create an instance of {@link CreateFolder }
     * 
     */
    public CreateFolder createCreateFolder() {
        return new CreateFolder();
    }

    /**
     * Create an instance of {@link DeleteTreeResponse.FailedToDelete }
     * 
     */
    public DeleteTreeResponse.FailedToDelete createDeleteTreeResponseFailedToDelete() {
        return new DeleteTreeResponse.FailedToDelete();
    }

    /**
     * Create an instance of {@link CreatePolicy }
     * 
     */
    public CreatePolicy createCreatePolicy() {
        return new CreatePolicy();
    }

    /**
     * Create an instance of {@link CmisTypeDocumentDefinitionType }
     * 
     */
    public CmisTypeDocumentDefinitionType createCmisTypeDocumentDefinitionType() {
        return new CmisTypeDocumentDefinitionType();
    }

    /**
     * Create an instance of {@link CmisObjectParentsType }
     * 
     */
    public CmisObjectParentsType createCmisObjectParentsType() {
        return new CmisObjectParentsType();
    }

    /**
     * Create an instance of {@link CmisPropertyStringDefinitionType }
     * 
     */
    public CmisPropertyStringDefinitionType createCmisPropertyStringDefinitionType() {
        return new CmisPropertyStringDefinitionType();
    }

    /**
     * Create an instance of {@link DeleteTreeResponse }
     * 
     */
    public DeleteTreeResponse createDeleteTreeResponse() {
        return new DeleteTreeResponse();
    }

    /**
     * Create an instance of {@link RemoveObjectFromFolder }
     * 
     */
    public RemoveObjectFromFolder createRemoveObjectFromFolder() {
        return new RemoveObjectFromFolder();
    }

    /**
     * Create an instance of {@link CreateRelationshipResponse }
     * 
     */
    public CreateRelationshipResponse createCreateRelationshipResponse() {
        return new CreateRelationshipResponse();
    }

    /**
     * Create an instance of {@link CheckOut }
     * 
     */
    public CheckOut createCheckOut() {
        return new CheckOut();
    }

    /**
     * Create an instance of {@link CmisPropertyIdDefinitionType }
     * 
     */
    public CmisPropertyIdDefinitionType createCmisPropertyIdDefinitionType() {
        return new CmisPropertyIdDefinitionType();
    }

    /**
     * Create an instance of {@link GetTypeChildrenResponse }
     * 
     */
    public GetTypeChildrenResponse createGetTypeChildrenResponse() {
        return new GetTypeChildrenResponse();
    }

    /**
     * Create an instance of {@link SetContentStream }
     * 
     */
    public SetContentStream createSetContentStream() {
        return new SetContentStream();
    }

    /**
     * Create an instance of {@link CmisPermissionMapping }
     * 
     */
    public CmisPermissionMapping createCmisPermissionMapping() {
        return new CmisPermissionMapping();
    }

    /**
     * Create an instance of {@link Aspects }
     * 
     */
    public Aspects createAspects() {
        return new Aspects();
    }

    /**
     * Create an instance of {@link GetObjectOfLatestVersion }
     * 
     */
    public GetObjectOfLatestVersion createGetObjectOfLatestVersion() {
        return new GetObjectOfLatestVersion();
    }

    /**
     * Create an instance of {@link CmisObjectType }
     * 
     */
    public CmisObjectType createCmisObjectType() {
        return new CmisObjectType();
    }

    /**
     * Create an instance of {@link CmisListOfIdsType }
     * 
     */
    public CmisListOfIdsType createCmisListOfIdsType() {
        return new CmisListOfIdsType();
    }

    /**
     * Create an instance of {@link GetProperties }
     * 
     */
    public GetProperties createGetProperties() {
        return new GetProperties();
    }

    /**
     * Create an instance of {@link CmisObjectListType }
     * 
     */
    public CmisObjectListType createCmisObjectListType() {
        return new CmisObjectListType();
    }

    /**
     * Create an instance of {@link GetCheckedOutDocs }
     * 
     */
    public GetCheckedOutDocs createGetCheckedOutDocs() {
        return new GetCheckedOutDocs();
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
     * Create an instance of {@link GetTypeDefinitionResponse }
     * 
     */
    public GetTypeDefinitionResponse createGetTypeDefinitionResponse() {
        return new GetTypeDefinitionResponse();
    }

    /**
     * Create an instance of {@link CmisAccessControlEntryType }
     * 
     */
    public CmisAccessControlEntryType createCmisAccessControlEntryType() {
        return new CmisAccessControlEntryType();
    }

    /**
     * Create an instance of {@link CmisChoiceDateTime }
     * 
     */
    public CmisChoiceDateTime createCmisChoiceDateTime() {
        return new CmisChoiceDateTime();
    }

    /**
     * Create an instance of {@link CmisObjectInFolderType }
     * 
     */
    public CmisObjectInFolderType createCmisObjectInFolderType() {
        return new CmisObjectInFolderType();
    }

    /**
     * Create an instance of {@link DeleteContentStreamResponse }
     * 
     */
    public DeleteContentStreamResponse createDeleteContentStreamResponse() {
        return new DeleteContentStreamResponse();
    }

    /**
     * Create an instance of {@link CmisExtensionType }
     * 
     */
    public CmisExtensionType createCmisExtensionType() {
        return new CmisExtensionType();
    }

    /**
     * Create an instance of {@link CmisPropertyUri }
     * 
     */
    public CmisPropertyUri createCmisPropertyUri() {
        return new CmisPropertyUri();
    }

    /**
     * Create an instance of {@link GetObjectRelationships }
     * 
     */
    public GetObjectRelationships createGetObjectRelationships() {
        return new GetObjectRelationships();
    }

    /**
     * Create an instance of {@link MoveObject }
     * 
     */
    public MoveObject createMoveObject() {
        return new MoveObject();
    }

    /**
     * Create an instance of {@link GetAllowableActions }
     * 
     */
    public GetAllowableActions createGetAllowableActions() {
        return new GetAllowableActions();
    }

    /**
     * Create an instance of {@link GetChildrenResponse }
     * 
     */
    public GetChildrenResponse createGetChildrenResponse() {
        return new GetChildrenResponse();
    }

    /**
     * Create an instance of {@link GetRenditions }
     * 
     */
    public GetRenditions createGetRenditions() {
        return new GetRenditions();
    }

    /**
     * Create an instance of {@link CmisObjectInFolderListType }
     * 
     */
    public CmisObjectInFolderListType createCmisObjectInFolderListType() {
        return new CmisObjectInFolderListType();
    }

    /**
     * Create an instance of {@link CreateFolderResponse }
     * 
     */
    public CreateFolderResponse createCreateFolderResponse() {
        return new CreateFolderResponse();
    }

    /**
     * Create an instance of {@link GetChildren }
     * 
     */
    public GetChildren createGetChildren() {
        return new GetChildren();
    }

    /**
     * Create an instance of {@link CreatePolicyResponse }
     * 
     */
    public CreatePolicyResponse createCreatePolicyResponse() {
        return new CreatePolicyResponse();
    }

    /**
     * Create an instance of {@link CmisChoiceHtml }
     * 
     */
    public CmisChoiceHtml createCmisChoiceHtml() {
        return new CmisChoiceHtml();
    }

    /**
     * Create an instance of {@link GetObjectRelationshipsResponse }
     * 
     */
    public GetObjectRelationshipsResponse createGetObjectRelationshipsResponse() {
        return new GetObjectRelationshipsResponse();
    }

    /**
     * Create an instance of {@link GetTypeDescendants }
     * 
     */
    public GetTypeDescendants createGetTypeDescendants() {
        return new GetTypeDescendants();
    }

    /**
     * Create an instance of {@link CreateDocumentResponse }
     * 
     */
    public CreateDocumentResponse createCreateDocumentResponse() {
        return new CreateDocumentResponse();
    }

    /**
     * Create an instance of {@link AddObjectToFolder }
     * 
     */
    public AddObjectToFolder createAddObjectToFolder() {
        return new AddObjectToFolder();
    }

    /**
     * Create an instance of {@link GetPropertiesResponse }
     * 
     */
    public GetPropertiesResponse createGetPropertiesResponse() {
        return new GetPropertiesResponse();
    }

    /**
     * Create an instance of {@link CheckInResponse }
     * 
     */
    public CheckInResponse createCheckInResponse() {
        return new CheckInResponse();
    }

    /**
     * Create an instance of {@link CmisChangeEventType }
     * 
     */
    public CmisChangeEventType createCmisChangeEventType() {
        return new CmisChangeEventType();
    }

    /**
     * Create an instance of {@link CancelCheckOut }
     * 
     */
    public CancelCheckOut createCancelCheckOut() {
        return new CancelCheckOut();
    }

    /**
     * Create an instance of {@link GetObjectByPathResponse }
     * 
     */
    public GetObjectByPathResponse createGetObjectByPathResponse() {
        return new GetObjectByPathResponse();
    }

    /**
     * Create an instance of {@link CmisChoiceInteger }
     * 
     */
    public CmisChoiceInteger createCmisChoiceInteger() {
        return new CmisChoiceInteger();
    }

    /**
     * Create an instance of {@link RemoveObjectFromFolderResponse }
     * 
     */
    public RemoveObjectFromFolderResponse createRemoveObjectFromFolderResponse() {
        return new RemoveObjectFromFolderResponse();
    }

    /**
     * Create an instance of {@link GetDescendants }
     * 
     */
    public GetDescendants createGetDescendants() {
        return new GetDescendants();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisQueryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/", name = "query")
    public JAXBElement<CmisQueryType> createQuery(CmisQueryType value) {
        return new JAXBElement<CmisQueryType>(_Query_QNAME, CmisQueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisFaultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "cmisFault")
    public JAXBElement<CmisFaultType> createCmisFault(CmisFaultType value) {
        return new JAXBElement<CmisFaultType>(_CmisFault_QNAME, CmisFaultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/", name = "acl")
    public JAXBElement<CmisAccessControlListType> createAcl(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_Acl_QNAME, CmisAccessControlListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAllowableActionsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/core/200908/", name = "allowableActions")
    public JAXBElement<CmisAllowableActionsType> createAllowableActions(CmisAllowableActionsType value) {
        return new JAXBElement<CmisAllowableActionsType>(_AllowableActions_QNAME, CmisAllowableActionsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "removeACEs", scope = CreateDocumentFromSource.class)
    public JAXBElement<CmisAccessControlListType> createCreateDocumentFromSourceRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceRemoveACEs_QNAME, CmisAccessControlListType.class, CreateDocumentFromSource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "folderId", scope = CreateDocumentFromSource.class)
    public JAXBElement<String> createCreateDocumentFromSourceFolderId(String value) {
        return new JAXBElement<String>(_CreateDocumentFromSourceFolderId_QNAME, String.class, CreateDocumentFromSource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreateDocumentFromSource.class)
    public JAXBElement<CmisExtensionType> createCreateDocumentFromSourceExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreateDocumentFromSource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "addACEs", scope = CreateDocumentFromSource.class)
    public JAXBElement<CmisAccessControlListType> createCreateDocumentFromSourceAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceAddACEs_QNAME, CmisAccessControlListType.class, CreateDocumentFromSource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumVersioningState }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "versioningState", scope = CreateDocumentFromSource.class)
    public JAXBElement<EnumVersioningState> createCreateDocumentFromSourceVersioningState(EnumVersioningState value) {
        return new JAXBElement<EnumVersioningState>(_CreateDocumentFromSourceVersioningState_QNAME, EnumVersioningState.class, CreateDocumentFromSource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumACLPropagation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "ACLPropagation", scope = ApplyACL.class)
    public JAXBElement<EnumACLPropagation> createApplyACLACLPropagation(EnumACLPropagation value) {
        return new JAXBElement<EnumACLPropagation>(_ApplyACLACLPropagation_QNAME, EnumACLPropagation.class, ApplyACL.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = ApplyACL.class)
    public JAXBElement<CmisExtensionType> createApplyACLExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, ApplyACL.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetRepositoryInfo.class)
    public JAXBElement<CmisExtensionType> createGetRepositoryInfoExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetRepositoryInfo.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = ApplyPolicyResponse.class)
    public JAXBElement<CmisExtensionType> createApplyPolicyResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, ApplyPolicyResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelationships", scope = GetObject.class)
    public JAXBElement<EnumIncludeRelationships> createGetObjectIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetObjectIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetObject.class)
    public JAXBElement<Boolean> createGetObjectIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeACL", scope = GetObject.class)
    public JAXBElement<Boolean> createGetObjectIncludeACL(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeACL_QNAME, Boolean.class, GetObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = GetObject.class)
    public JAXBElement<String> createGetObjectRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, GetObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetObject.class)
    public JAXBElement<String> createGetObjectFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetObject.class)
    public JAXBElement<CmisExtensionType> createGetObjectExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includePolicyIds", scope = GetObject.class)
    public JAXBElement<Boolean> createGetObjectIncludePolicyIds(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludePolicyIds_QNAME, Boolean.class, GetObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "allVersions", scope = DeleteObject.class)
    public JAXBElement<Boolean> createDeleteObjectAllVersions(Boolean value) {
        return new JAXBElement<Boolean>(_DeleteObjectAllVersions_QNAME, Boolean.class, DeleteObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = DeleteObject.class)
    public JAXBElement<CmisExtensionType> createDeleteObjectExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, DeleteObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreateDocumentFromSourceResponse.class)
    public JAXBElement<CmisExtensionType> createCreateDocumentFromSourceResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreateDocumentFromSourceResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = DeleteObjectResponse.class)
    public JAXBElement<CmisExtensionType> createDeleteObjectResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, DeleteObjectResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = RemovePolicyResponse.class)
    public JAXBElement<CmisExtensionType> createRemovePolicyResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, RemovePolicyResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "skipCount", scope = Query.class)
    public JAXBElement<BigInteger> createQuerySkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_QuerySkipCount_QNAME, BigInteger.class, Query.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "maxItems", scope = Query.class)
    public JAXBElement<BigInteger> createQueryMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_QueryMaxItems_QNAME, BigInteger.class, Query.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelationships", scope = Query.class)
    public JAXBElement<EnumIncludeRelationships> createQueryIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetObjectIncludeRelationships_QNAME, EnumIncludeRelationships.class, Query.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = Query.class)
    public JAXBElement<Boolean> createQueryIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, Query.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = Query.class)
    public JAXBElement<String> createQueryRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, Query.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = Query.class)
    public JAXBElement<CmisExtensionType> createQueryExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, Query.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "searchAllVersions", scope = Query.class)
    public JAXBElement<Boolean> createQuerySearchAllVersions(Boolean value) {
        return new JAXBElement<Boolean>(_QuerySearchAllVersions_QNAME, Boolean.class, Query.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "removeACEs", scope = CheckIn.class)
    public JAXBElement<CmisAccessControlListType> createCheckInRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceRemoveACEs_QNAME, CmisAccessControlListType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "contentStream", scope = CheckIn.class)
    public JAXBElement<CmisContentStreamType> createCheckInContentStream(CmisContentStreamType value) {
        return new JAXBElement<CmisContentStreamType>(_CheckInContentStream_QNAME, CmisContentStreamType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "major", scope = CheckIn.class)
    public JAXBElement<Boolean> createCheckInMajor(Boolean value) {
        return new JAXBElement<Boolean>(_CheckInMajor_QNAME, Boolean.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisPropertiesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "properties", scope = CheckIn.class)
    public JAXBElement<CmisPropertiesType> createCheckInProperties(CmisPropertiesType value) {
        return new JAXBElement<CmisPropertiesType>(_CheckInProperties_QNAME, CmisPropertiesType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CheckIn.class)
    public JAXBElement<CmisExtensionType> createCheckInExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "addACEs", scope = CheckIn.class)
    public JAXBElement<CmisAccessControlListType> createCheckInAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceAddACEs_QNAME, CmisAccessControlListType.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "checkinComment", scope = CheckIn.class)
    public JAXBElement<String> createCheckInCheckinComment(String value) {
        return new JAXBElement<String>(_CheckInCheckinComment_QNAME, String.class, CheckIn.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "maxItems", scope = GetContentChanges.class)
    public JAXBElement<BigInteger> createGetContentChangesMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_QueryMaxItems_QNAME, BigInteger.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeACL", scope = GetContentChanges.class)
    public JAXBElement<Boolean> createGetContentChangesIncludeACL(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeACL_QNAME, Boolean.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetContentChanges.class)
    public JAXBElement<String> createGetContentChangesFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeProperties", scope = GetContentChanges.class)
    public JAXBElement<Boolean> createGetContentChangesIncludeProperties(Boolean value) {
        return new JAXBElement<Boolean>(_GetContentChangesIncludeProperties_QNAME, Boolean.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "changeLogToken", scope = GetContentChanges.class)
    public JAXBElement<String> createGetContentChangesChangeLogToken(String value) {
        return new JAXBElement<String>(_GetContentChangesChangeLogToken_QNAME, String.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetContentChanges.class)
    public JAXBElement<CmisExtensionType> createGetContentChangesExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includePolicyIds", scope = GetContentChanges.class)
    public JAXBElement<Boolean> createGetContentChangesIncludePolicyIds(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludePolicyIds_QNAME, Boolean.class, GetContentChanges.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "removeACEs", scope = CreateFolder.class)
    public JAXBElement<CmisAccessControlListType> createCreateFolderRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceRemoveACEs_QNAME, CmisAccessControlListType.class, CreateFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreateFolder.class)
    public JAXBElement<CmisExtensionType> createCreateFolderExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreateFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "addACEs", scope = CreateFolder.class)
    public JAXBElement<CmisAccessControlListType> createCreateFolderAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceAddACEs_QNAME, CmisAccessControlListType.class, CreateFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "removeACEs", scope = CreatePolicy.class)
    public JAXBElement<CmisAccessControlListType> createCreatePolicyRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceRemoveACEs_QNAME, CmisAccessControlListType.class, CreatePolicy.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "folderId", scope = CreatePolicy.class)
    public JAXBElement<String> createCreatePolicyFolderId(String value) {
        return new JAXBElement<String>(_CreateDocumentFromSourceFolderId_QNAME, String.class, CreatePolicy.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreatePolicy.class)
    public JAXBElement<CmisExtensionType> createCreatePolicyExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreatePolicy.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "addACEs", scope = CreatePolicy.class)
    public JAXBElement<CmisAccessControlListType> createCreatePolicyAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceAddACEs_QNAME, CmisAccessControlListType.class, CreatePolicy.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "folderId", scope = RemoveObjectFromFolder.class)
    public JAXBElement<String> createRemoveObjectFromFolderFolderId(String value) {
        return new JAXBElement<String>(_CreateDocumentFromSourceFolderId_QNAME, String.class, RemoveObjectFromFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = RemoveObjectFromFolder.class)
    public JAXBElement<CmisExtensionType> createRemoveObjectFromFolderExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, RemoveObjectFromFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreateRelationshipResponse.class)
    public JAXBElement<CmisExtensionType> createCreateRelationshipResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreateRelationshipResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelationships", scope = GetFolderTree.class)
    public JAXBElement<EnumIncludeRelationships> createGetFolderTreeIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetObjectIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetFolderTree.class)
    public JAXBElement<Boolean> createGetFolderTreeIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = GetFolderTree.class)
    public JAXBElement<String> createGetFolderTreeRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetFolderTree.class)
    public JAXBElement<String> createGetFolderTreeFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "depth", scope = GetFolderTree.class)
    public JAXBElement<BigInteger> createGetFolderTreeDepth(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetFolderTreeDepth_QNAME, BigInteger.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetFolderTree.class)
    public JAXBElement<CmisExtensionType> createGetFolderTreeExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includePathSegment", scope = GetFolderTree.class)
    public JAXBElement<Boolean> createGetFolderTreeIncludePathSegment(Boolean value) {
        return new JAXBElement<Boolean>(_GetFolderTreeIncludePathSegment_QNAME, Boolean.class, GetFolderTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CheckOut.class)
    public JAXBElement<CmisExtensionType> createCheckOutExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CheckOut.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelationships", scope = GetObjectParents.class)
    public JAXBElement<EnumIncludeRelationships> createGetObjectParentsIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetObjectIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetObjectParents.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetObjectParents.class)
    public JAXBElement<Boolean> createGetObjectParentsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetObjectParents.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelativePathSegment", scope = GetObjectParents.class)
    public JAXBElement<Boolean> createGetObjectParentsIncludeRelativePathSegment(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectParentsIncludeRelativePathSegment_QNAME, Boolean.class, GetObjectParents.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = GetObjectParents.class)
    public JAXBElement<String> createGetObjectParentsRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, GetObjectParents.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetObjectParents.class)
    public JAXBElement<String> createGetObjectParentsFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetObjectParents.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetObjectParents.class)
    public JAXBElement<CmisExtensionType> createGetObjectParentsExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetObjectParents.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = DeleteContentStream.class)
    public JAXBElement<CmisExtensionType> createDeleteContentStreamExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, DeleteContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "changeToken", scope = SetContentStream.class)
    public JAXBElement<String> createSetContentStreamChangeToken(String value) {
        return new JAXBElement<String>(_SetContentStreamChangeToken_QNAME, String.class, SetContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "overwriteFlag", scope = SetContentStream.class)
    public JAXBElement<Boolean> createSetContentStreamOverwriteFlag(Boolean value) {
        return new JAXBElement<Boolean>(_SetContentStreamOverwriteFlag_QNAME, Boolean.class, SetContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = SetContentStream.class)
    public JAXBElement<CmisExtensionType> createSetContentStreamExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, SetContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelationships", scope = GetObjectOfLatestVersion.class)
    public JAXBElement<EnumIncludeRelationships> createGetObjectOfLatestVersionIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetObjectIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetObjectOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetObjectOfLatestVersion.class)
    public JAXBElement<Boolean> createGetObjectOfLatestVersionIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetObjectOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeACL", scope = GetObjectOfLatestVersion.class)
    public JAXBElement<Boolean> createGetObjectOfLatestVersionIncludeACL(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeACL_QNAME, Boolean.class, GetObjectOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = GetObjectOfLatestVersion.class)
    public JAXBElement<String> createGetObjectOfLatestVersionRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, GetObjectOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetObjectOfLatestVersion.class)
    public JAXBElement<String> createGetObjectOfLatestVersionFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetObjectOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetObjectOfLatestVersion.class)
    public JAXBElement<CmisExtensionType> createGetObjectOfLatestVersionExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetObjectOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includePolicyIds", scope = GetObjectOfLatestVersion.class)
    public JAXBElement<Boolean> createGetObjectOfLatestVersionIncludePolicyIds(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludePolicyIds_QNAME, Boolean.class, GetObjectOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = AddObjectToFolderResponse.class)
    public JAXBElement<CmisExtensionType> createAddObjectToFolderResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, AddObjectToFolderResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetFolderParent.class)
    public JAXBElement<CmisExtensionType> createGetFolderParentExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetFolderParent.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "skipCount", scope = GetTypeChildren.class)
    public JAXBElement<BigInteger> createGetTypeChildrenSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_QuerySkipCount_QNAME, BigInteger.class, GetTypeChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "maxItems", scope = GetTypeChildren.class)
    public JAXBElement<BigInteger> createGetTypeChildrenMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_QueryMaxItems_QNAME, BigInteger.class, GetTypeChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "typeId", scope = GetTypeChildren.class)
    public JAXBElement<String> createGetTypeChildrenTypeId(String value) {
        return new JAXBElement<String>(_GetTypeChildrenTypeId_QNAME, String.class, GetTypeChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includePropertyDefinitions", scope = GetTypeChildren.class)
    public JAXBElement<Boolean> createGetTypeChildrenIncludePropertyDefinitions(Boolean value) {
        return new JAXBElement<Boolean>(_GetTypeChildrenIncludePropertyDefinitions_QNAME, Boolean.class, GetTypeChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetTypeChildren.class)
    public JAXBElement<CmisExtensionType> createGetTypeChildrenExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetTypeChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetProperties.class)
    public JAXBElement<String> createGetPropertiesFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetProperties.class)
    public JAXBElement<CmisExtensionType> createGetPropertiesExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "skipCount", scope = GetCheckedOutDocs.class)
    public JAXBElement<BigInteger> createGetCheckedOutDocsSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_QuerySkipCount_QNAME, BigInteger.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "maxItems", scope = GetCheckedOutDocs.class)
    public JAXBElement<BigInteger> createGetCheckedOutDocsMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_QueryMaxItems_QNAME, BigInteger.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelationships", scope = GetCheckedOutDocs.class)
    public JAXBElement<EnumIncludeRelationships> createGetCheckedOutDocsIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetObjectIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetCheckedOutDocs.class)
    public JAXBElement<Boolean> createGetCheckedOutDocsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "folderId", scope = GetCheckedOutDocs.class)
    public JAXBElement<String> createGetCheckedOutDocsFolderId(String value) {
        return new JAXBElement<String>(_CreateDocumentFromSourceFolderId_QNAME, String.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = GetCheckedOutDocs.class)
    public JAXBElement<String> createGetCheckedOutDocsRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetCheckedOutDocs.class)
    public JAXBElement<String> createGetCheckedOutDocsFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "orderBy", scope = GetCheckedOutDocs.class)
    public JAXBElement<String> createGetCheckedOutDocsOrderBy(String value) {
        return new JAXBElement<String>(_GetCheckedOutDocsOrderBy_QNAME, String.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetCheckedOutDocs.class)
    public JAXBElement<CmisExtensionType> createGetCheckedOutDocsExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetCheckedOutDocs.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CheckOutResponse.class)
    public JAXBElement<CmisExtensionType> createCheckOutResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CheckOutResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = ApplyPolicy.class)
    public JAXBElement<CmisExtensionType> createApplyPolicyExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, ApplyPolicy.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = SetContentStreamResponse.class)
    public JAXBElement<CmisExtensionType> createSetContentStreamResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, SetContentStreamResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = DeleteContentStreamResponse.class)
    public JAXBElement<CmisExtensionType> createDeleteContentStreamResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, DeleteContentStreamResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetTypeDefinition.class)
    public JAXBElement<CmisExtensionType> createGetTypeDefinitionExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetTypeDefinition.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = RemovePolicy.class)
    public JAXBElement<CmisExtensionType> createRemovePolicyExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, RemovePolicy.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = MoveObjectResponse.class)
    public JAXBElement<CmisExtensionType> createMoveObjectResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, MoveObjectResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "offset", scope = GetContentStream.class)
    public JAXBElement<BigInteger> createGetContentStreamOffset(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetContentStreamOffset_QNAME, BigInteger.class, GetContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "length", scope = GetContentStream.class)
    public JAXBElement<BigInteger> createGetContentStreamLength(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetContentStreamLength_QNAME, BigInteger.class, GetContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetContentStream.class)
    public JAXBElement<CmisExtensionType> createGetContentStreamExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "streamId", scope = GetContentStream.class)
    public JAXBElement<String> createGetContentStreamStreamId(String value) {
        return new JAXBElement<String>(_GetContentStreamStreamId_QNAME, String.class, GetContentStream.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "skipCount", scope = GetObjectRelationships.class)
    public JAXBElement<BigInteger> createGetObjectRelationshipsSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_QuerySkipCount_QNAME, BigInteger.class, GetObjectRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "maxItems", scope = GetObjectRelationships.class)
    public JAXBElement<BigInteger> createGetObjectRelationshipsMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_QueryMaxItems_QNAME, BigInteger.class, GetObjectRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "typeId", scope = GetObjectRelationships.class)
    public JAXBElement<String> createGetObjectRelationshipsTypeId(String value) {
        return new JAXBElement<String>(_GetTypeChildrenTypeId_QNAME, String.class, GetObjectRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetObjectRelationships.class)
    public JAXBElement<Boolean> createGetObjectRelationshipsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetObjectRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetObjectRelationships.class)
    public JAXBElement<String> createGetObjectRelationshipsFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetObjectRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumRelationshipDirection }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "relationshipDirection", scope = GetObjectRelationships.class)
    public JAXBElement<EnumRelationshipDirection> createGetObjectRelationshipsRelationshipDirection(EnumRelationshipDirection value) {
        return new JAXBElement<EnumRelationshipDirection>(_GetObjectRelationshipsRelationshipDirection_QNAME, EnumRelationshipDirection.class, GetObjectRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetObjectRelationships.class)
    public JAXBElement<CmisExtensionType> createGetObjectRelationshipsExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetObjectRelationships.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelationships", scope = GetObjectByPath.class)
    public JAXBElement<EnumIncludeRelationships> createGetObjectByPathIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetObjectIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetObjectByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetObjectByPath.class)
    public JAXBElement<Boolean> createGetObjectByPathIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetObjectByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeACL", scope = GetObjectByPath.class)
    public JAXBElement<Boolean> createGetObjectByPathIncludeACL(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeACL_QNAME, Boolean.class, GetObjectByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = GetObjectByPath.class)
    public JAXBElement<String> createGetObjectByPathRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, GetObjectByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetObjectByPath.class)
    public JAXBElement<String> createGetObjectByPathFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetObjectByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetObjectByPath.class)
    public JAXBElement<CmisExtensionType> createGetObjectByPathExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetObjectByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includePolicyIds", scope = GetObjectByPath.class)
    public JAXBElement<Boolean> createGetObjectByPathIncludePolicyIds(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludePolicyIds_QNAME, Boolean.class, GetObjectByPath.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = MoveObject.class)
    public JAXBElement<CmisExtensionType> createMoveObjectExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, MoveObject.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetAllowableActions.class)
    public JAXBElement<CmisExtensionType> createGetAllowableActionsExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetAllowableActions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "allVersions", scope = DeleteTree.class)
    public JAXBElement<Boolean> createDeleteTreeAllVersions(Boolean value) {
        return new JAXBElement<Boolean>(_DeleteObjectAllVersions_QNAME, Boolean.class, DeleteTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = DeleteTree.class)
    public JAXBElement<CmisExtensionType> createDeleteTreeExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, DeleteTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "continueOnFailure", scope = DeleteTree.class)
    public JAXBElement<Boolean> createDeleteTreeContinueOnFailure(Boolean value) {
        return new JAXBElement<Boolean>(_DeleteTreeContinueOnFailure_QNAME, Boolean.class, DeleteTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumUnfileObject }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "unfileObjects", scope = DeleteTree.class)
    public JAXBElement<EnumUnfileObject> createDeleteTreeUnfileObjects(EnumUnfileObject value) {
        return new JAXBElement<EnumUnfileObject>(_DeleteTreeUnfileObjects_QNAME, EnumUnfileObject.class, DeleteTree.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = UpdatePropertiesResponse.class)
    public JAXBElement<CmisExtensionType> createUpdatePropertiesResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, UpdatePropertiesResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "skipCount", scope = GetRenditions.class)
    public JAXBElement<BigInteger> createGetRenditionsSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_QuerySkipCount_QNAME, BigInteger.class, GetRenditions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "maxItems", scope = GetRenditions.class)
    public JAXBElement<BigInteger> createGetRenditionsMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_QueryMaxItems_QNAME, BigInteger.class, GetRenditions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = GetRenditions.class)
    public JAXBElement<String> createGetRenditionsRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, GetRenditions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetRenditions.class)
    public JAXBElement<CmisExtensionType> createGetRenditionsExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetRenditions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CancelCheckOutResponse.class)
    public JAXBElement<CmisExtensionType> createCancelCheckOutResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CancelCheckOutResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "onlyBasicPermissions", scope = GetACL.class)
    public JAXBElement<Boolean> createGetACLOnlyBasicPermissions(Boolean value) {
        return new JAXBElement<Boolean>(_GetACLOnlyBasicPermissions_QNAME, Boolean.class, GetACL.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetACL.class)
    public JAXBElement<CmisExtensionType> createGetACLExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetACL.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetAppliedPolicies.class)
    public JAXBElement<String> createGetAppliedPoliciesFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetAppliedPolicies.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetAppliedPolicies.class)
    public JAXBElement<CmisExtensionType> createGetAppliedPoliciesExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetAppliedPolicies.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "changeToken", scope = UpdateProperties.class)
    public JAXBElement<String> createUpdatePropertiesChangeToken(String value) {
        return new JAXBElement<String>(_SetContentStreamChangeToken_QNAME, String.class, UpdateProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = UpdateProperties.class)
    public JAXBElement<CmisExtensionType> createUpdatePropertiesExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, UpdateProperties.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetAllVersions.class)
    public JAXBElement<Boolean> createGetAllVersionsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetAllVersions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetAllVersions.class)
    public JAXBElement<String> createGetAllVersionsFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetAllVersions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetAllVersions.class)
    public JAXBElement<CmisExtensionType> createGetAllVersionsExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetAllVersions.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreateFolderResponse.class)
    public JAXBElement<CmisExtensionType> createCreateFolderResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreateFolderResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "skipCount", scope = GetChildren.class)
    public JAXBElement<BigInteger> createGetChildrenSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_QuerySkipCount_QNAME, BigInteger.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "maxItems", scope = GetChildren.class)
    public JAXBElement<BigInteger> createGetChildrenMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_QueryMaxItems_QNAME, BigInteger.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelationships", scope = GetChildren.class)
    public JAXBElement<EnumIncludeRelationships> createGetChildrenIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetObjectIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetChildren.class)
    public JAXBElement<Boolean> createGetChildrenIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = GetChildren.class)
    public JAXBElement<String> createGetChildrenRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetChildren.class)
    public JAXBElement<String> createGetChildrenFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "orderBy", scope = GetChildren.class)
    public JAXBElement<String> createGetChildrenOrderBy(String value) {
        return new JAXBElement<String>(_GetCheckedOutDocsOrderBy_QNAME, String.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetChildren.class)
    public JAXBElement<CmisExtensionType> createGetChildrenExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includePathSegment", scope = GetChildren.class)
    public JAXBElement<Boolean> createGetChildrenIncludePathSegment(Boolean value) {
        return new JAXBElement<Boolean>(_GetFolderTreeIncludePathSegment_QNAME, Boolean.class, GetChildren.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "removeACEs", scope = CreateDocument.class)
    public JAXBElement<CmisAccessControlListType> createCreateDocumentRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceRemoveACEs_QNAME, CmisAccessControlListType.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisContentStreamType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "contentStream", scope = CreateDocument.class)
    public JAXBElement<CmisContentStreamType> createCreateDocumentContentStream(CmisContentStreamType value) {
        return new JAXBElement<CmisContentStreamType>(_CheckInContentStream_QNAME, CmisContentStreamType.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "folderId", scope = CreateDocument.class)
    public JAXBElement<String> createCreateDocumentFolderId(String value) {
        return new JAXBElement<String>(_CreateDocumentFromSourceFolderId_QNAME, String.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreateDocument.class)
    public JAXBElement<CmisExtensionType> createCreateDocumentExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "addACEs", scope = CreateDocument.class)
    public JAXBElement<CmisAccessControlListType> createCreateDocumentAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceAddACEs_QNAME, CmisAccessControlListType.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumVersioningState }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "versioningState", scope = CreateDocument.class)
    public JAXBElement<EnumVersioningState> createCreateDocumentVersioningState(EnumVersioningState value) {
        return new JAXBElement<EnumVersioningState>(_CreateDocumentFromSourceVersioningState_QNAME, EnumVersioningState.class, CreateDocument.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreatePolicyResponse.class)
    public JAXBElement<CmisExtensionType> createCreatePolicyResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreatePolicyResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "typeId", scope = GetTypeDescendants.class)
    public JAXBElement<String> createGetTypeDescendantsTypeId(String value) {
        return new JAXBElement<String>(_GetTypeChildrenTypeId_QNAME, String.class, GetTypeDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includePropertyDefinitions", scope = GetTypeDescendants.class)
    public JAXBElement<Boolean> createGetTypeDescendantsIncludePropertyDefinitions(Boolean value) {
        return new JAXBElement<Boolean>(_GetTypeChildrenIncludePropertyDefinitions_QNAME, Boolean.class, GetTypeDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "depth", scope = GetTypeDescendants.class)
    public JAXBElement<BigInteger> createGetTypeDescendantsDepth(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetFolderTreeDepth_QNAME, BigInteger.class, GetTypeDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetTypeDescendants.class)
    public JAXBElement<CmisExtensionType> createGetTypeDescendantsExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetTypeDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "removeACEs", scope = CreateRelationship.class)
    public JAXBElement<CmisAccessControlListType> createCreateRelationshipRemoveACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceRemoveACEs_QNAME, CmisAccessControlListType.class, CreateRelationship.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreateRelationship.class)
    public JAXBElement<CmisExtensionType> createCreateRelationshipExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreateRelationship.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisAccessControlListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "addACEs", scope = CreateRelationship.class)
    public JAXBElement<CmisAccessControlListType> createCreateRelationshipAddACEs(CmisAccessControlListType value) {
        return new JAXBElement<CmisAccessControlListType>(_CreateDocumentFromSourceAddACEs_QNAME, CmisAccessControlListType.class, CreateRelationship.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetPropertiesOfLatestVersion.class)
    public JAXBElement<String> createGetPropertiesOfLatestVersionFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetPropertiesOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetPropertiesOfLatestVersion.class)
    public JAXBElement<CmisExtensionType> createGetPropertiesOfLatestVersionExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetPropertiesOfLatestVersion.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CreateDocumentResponse.class)
    public JAXBElement<CmisExtensionType> createCreateDocumentResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CreateDocumentResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetRepositories.class)
    public JAXBElement<CmisExtensionType> createGetRepositoriesExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetRepositories.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = AddObjectToFolder.class)
    public JAXBElement<CmisExtensionType> createAddObjectToFolderExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, AddObjectToFolder.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CheckInResponse.class)
    public JAXBElement<CmisExtensionType> createCheckInResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CheckInResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = CancelCheckOut.class)
    public JAXBElement<CmisExtensionType> createCancelCheckOutExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, CancelCheckOut.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = RemoveObjectFromFolderResponse.class)
    public JAXBElement<CmisExtensionType> createRemoveObjectFromFolderResponseExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, RemoveObjectFromFolderResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnumIncludeRelationships }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeRelationships", scope = GetDescendants.class)
    public JAXBElement<EnumIncludeRelationships> createGetDescendantsIncludeRelationships(EnumIncludeRelationships value) {
        return new JAXBElement<EnumIncludeRelationships>(_GetObjectIncludeRelationships_QNAME, EnumIncludeRelationships.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includeAllowableActions", scope = GetDescendants.class)
    public JAXBElement<Boolean> createGetDescendantsIncludeAllowableActions(Boolean value) {
        return new JAXBElement<Boolean>(_GetObjectIncludeAllowableActions_QNAME, Boolean.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "renditionFilter", scope = GetDescendants.class)
    public JAXBElement<String> createGetDescendantsRenditionFilter(String value) {
        return new JAXBElement<String>(_GetObjectRenditionFilter_QNAME, String.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "filter", scope = GetDescendants.class)
    public JAXBElement<String> createGetDescendantsFilter(String value) {
        return new JAXBElement<String>(_GetObjectFilter_QNAME, String.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "depth", scope = GetDescendants.class)
    public JAXBElement<BigInteger> createGetDescendantsDepth(BigInteger value) {
        return new JAXBElement<BigInteger>(_GetFolderTreeDepth_QNAME, BigInteger.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CmisExtensionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "extension", scope = GetDescendants.class)
    public JAXBElement<CmisExtensionType> createGetDescendantsExtension(CmisExtensionType value) {
        return new JAXBElement<CmisExtensionType>(_CreateDocumentFromSourceExtension_QNAME, CmisExtensionType.class, GetDescendants.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ns/cmis/messaging/200908/", name = "includePathSegment", scope = GetDescendants.class)
    public JAXBElement<Boolean> createGetDescendantsIncludePathSegment(Boolean value) {
        return new JAXBElement<Boolean>(_GetFolderTreeIncludePathSegment_QNAME, Boolean.class, GetDescendants.class, value);
    }

}
