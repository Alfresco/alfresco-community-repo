
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.alfresco.repo.cmis.ws.DeleteTreeResponse.FailedToDelete;
import org.alfresco.repo.cmis.ws.GetAllowableActionsResponse.AllowableActionCollection;
import org.alfresco.repo.cmis.ws.GetTypesResponse.Types;


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

    private final static QName _ObjectNotFoundFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "ObjectNotFoundFault");
    private final static QName _OffsetFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "OffsetFault");
    private final static QName _StorageFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "StorageFault");
    private final static QName _RuntimeFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "RuntimeFault");
    private final static QName _StreamNotSupportedFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "StreamNotSupportedFault");
    private final static QName _HasMoreItems_QNAME = new QName("http://www.cmis.org/ns/1.0", "hasMoreItems");
    private final static QName _FilterNotValidFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "FilterNotValidFault");
    private final static QName _SkipCount_QNAME = new QName("http://www.cmis.org/ns/1.0", "skipCount");
    private final static QName _Parent_QNAME = new QName("http://www.cmis.org/ns/1.0", "parent");
    private final static QName _InvalidArgumentFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "InvalidArgumentFault");
    private final static QName _Name_QNAME = new QName("http://www.cmis.org/ns/1.0", "name");
    private final static QName _OperationNotSupportedFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "OperationNotSupportedFault");
    private final static QName _SourceOID_QNAME = new QName("http://www.cmis.org/ns/1.0", "sourceOID");
    private final static QName _Filter_QNAME = new QName("http://www.cmis.org/ns/1.0", "filter");
    private final static QName _TypeNotFoundFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "TypeNotFoundFault");
    private final static QName _PermissionDeniedFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "PermissionDeniedFault");
    private final static QName _ConcurrencyFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "ConcurrencyFault");
    private final static QName _NotInFolderFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "NotInFolderFault");
    private final static QName _FolderNotValidFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "FolderNotValidFault");
    private final static QName _AlreadyExistsFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "AlreadyExistsFault");
    private final static QName _ConstraintViolationFault_QNAME = new QName("http://www.cmis.org/ns/1.0", "ConstraintViolationFault");
    private final static QName _MaxItems_QNAME = new QName("http://www.cmis.org/ns/1.0", "maxItems");
    private final static QName _TargetOID_QNAME = new QName("http://www.cmis.org/ns/1.0", "targetOID");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.alfresco.repo.cmis.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeleteObject }
     * 
     */
    public DeleteObject createDeleteObject() {
        return new DeleteObject();
    }

    /**
     * Create an instance of {@link GetRelationships }
     * 
     */
    public GetRelationships createGetRelationships() {
        return new GetRelationships();
    }

    /**
     * Create an instance of {@link DocumentFolderOrRelationshipObjectType }
     * 
     */
    public DocumentFolderOrRelationshipObjectType createDocumentFolderOrRelationshipObjectType() {
        return new DocumentFolderOrRelationshipObjectType();
    }

    /**
     * Create an instance of {@link CheckInResponse }
     * 
     */
    public CheckInResponse createCheckInResponse() {
        return new CheckInResponse();
    }

    /**
     * Create an instance of {@link GetDescendants }
     * 
     */
    public GetDescendants createGetDescendants() {
        return new GetDescendants();
    }

    /**
     * Create an instance of {@link QueryResponse }
     * 
     */
    public QueryResponse createQueryResponse() {
        return new QueryResponse();
    }

    /**
     * Create an instance of {@link GetChildrenResponse }
     * 
     */
    public GetChildrenResponse createGetChildrenResponse() {
        return new GetChildrenResponse();
    }

    /**
     * Create an instance of {@link BasicFault }
     * 
     */
    public BasicFault createBasicFault() {
        return new BasicFault();
    }

    /**
     * Create an instance of {@link UpdatePropertiesResponse }
     * 
     */
    public UpdatePropertiesResponse createUpdatePropertiesResponse() {
        return new UpdatePropertiesResponse();
    }

    /**
     * Create an instance of {@link GetFolderParentResponse }
     * 
     */
    public GetFolderParentResponse createGetFolderParentResponse() {
        return new GetFolderParentResponse();
    }

    /**
     * Create an instance of {@link ObjectTypeDefinitionType }
     * 
     */
    public ObjectTypeDefinitionType createObjectTypeDefinitionType() {
        return new ObjectTypeDefinitionType();
    }

    /**
     * Create an instance of {@link DeleteAllVersions }
     * 
     */
    public DeleteAllVersions createDeleteAllVersions() {
        return new DeleteAllVersions();
    }

    /**
     * Create an instance of {@link RepositoryInfoType }
     * 
     */
    public RepositoryInfoType createRepositoryInfoType() {
        return new RepositoryInfoType();
    }

    /**
     * Create an instance of {@link GetCheckedoutDocs }
     * 
     */
    public GetCheckedoutDocs createGetCheckedoutDocs() {
        return new GetCheckedoutDocs();
    }

    /**
     * Create an instance of {@link RemoveDocumentFromFolderResponse }
     * 
     */
    public RemoveDocumentFromFolderResponse createRemoveDocumentFromFolderResponse() {
        return new RemoveDocumentFromFolderResponse();
    }

    /**
     * Create an instance of {@link DeleteTree }
     * 
     */
    public DeleteTree createDeleteTree() {
        return new DeleteTree();
    }

    /**
     * Create an instance of {@link ContentStream }
     * 
     */
    public ContentStream createContentStream() {
        return new ContentStream();
    }

    /**
     * Create an instance of {@link GetRepositoryInfoResponse }
     * 
     */
    public GetRepositoryInfoResponse createGetRepositoryInfoResponse() {
        return new GetRepositoryInfoResponse();
    }

    /**
     * Create an instance of {@link GetAllowableActionsResponse }
     * 
     */
    public GetAllowableActionsResponse createGetAllowableActionsResponse() {
        return new GetAllowableActionsResponse();
    }

    /**
     * Create an instance of {@link RemoveDocumentFromFolder }
     * 
     */
    public RemoveDocumentFromFolder createRemoveDocumentFromFolder() {
        return new RemoveDocumentFromFolder();
    }

    /**
     * Create an instance of {@link GetAllVersions }
     * 
     */
    public GetAllVersions createGetAllVersions() {
        return new GetAllVersions();
    }

    /**
     * Create an instance of {@link AddDocumentToFolderResponse }
     * 
     */
    public AddDocumentToFolderResponse createAddDocumentToFolderResponse() {
        return new AddDocumentToFolderResponse();
    }

    /**
     * Create an instance of {@link SetContentStream }
     * 
     */
    public SetContentStream createSetContentStream() {
        return new SetContentStream();
    }

    /**
     * Create an instance of {@link GetDocumentParents }
     * 
     */
    public GetDocumentParents createGetDocumentParents() {
        return new GetDocumentParents();
    }

    /**
     * Create an instance of {@link CancelCheckOutResponse }
     * 
     */
    public CancelCheckOutResponse createCancelCheckOutResponse() {
        return new CancelCheckOutResponse();
    }

    /**
     * Create an instance of {@link AddDocumentToFolder }
     * 
     */
    public AddDocumentToFolder createAddDocumentToFolder() {
        return new AddDocumentToFolder();
    }

    /**
     * Create an instance of {@link GetChildren }
     * 
     */
    public GetChildren createGetChildren() {
        return new GetChildren();
    }

    /**
     * Create an instance of {@link GetRootFolderResponse }
     * 
     */
    public GetRootFolderResponse createGetRootFolderResponse() {
        return new GetRootFolderResponse();
    }

    /**
     * Create an instance of {@link GetContentStream }
     * 
     */
    public GetContentStream createGetContentStream() {
        return new GetContentStream();
    }

    /**
     * Create an instance of {@link GetTypesResponse }
     * 
     */
    public GetTypesResponse createGetTypesResponse() {
        return new GetTypesResponse();
    }

    /**
     * Create an instance of {@link GetUnfiledDocs }
     * 
     */
    public GetUnfiledDocs createGetUnfiledDocs() {
        return new GetUnfiledDocs();
    }

    /**
     * Create an instance of {@link MoveObject }
     * 
     */
    public MoveObject createMoveObject() {
        return new MoveObject();
    }

    /**
     * Create an instance of {@link GetProperties }
     * 
     */
    public GetProperties createGetProperties() {
        return new GetProperties();
    }

    /**
     * Create an instance of {@link DeleteTreeResponse }
     * 
     */
    public DeleteTreeResponse createDeleteTreeResponse() {
        return new DeleteTreeResponse();
    }

    /**
     * Create an instance of {@link GetUnfiledDocsResponse }
     * 
     */
    public GetUnfiledDocsResponse createGetUnfiledDocsResponse() {
        return new GetUnfiledDocsResponse();
    }

    /**
     * Create an instance of {@link Types }
     * 
     */
    public Types createGetTypesResponseTypes() {
        return new Types();
    }

    /**
     * Create an instance of {@link RelationshipObjectType }
     * 
     */
    public RelationshipObjectType createRelationshipObjectType() {
        return new RelationshipObjectType();
    }

    /**
     * Create an instance of {@link GetRelationshipsResponse }
     * 
     */
    public GetRelationshipsResponse createGetRelationshipsResponse() {
        return new GetRelationshipsResponse();
    }

    /**
     * Create an instance of {@link DocumentCollection }
     * 
     */
    public DocumentCollection createDocumentCollection() {
        return new DocumentCollection();
    }

    /**
     * Create an instance of {@link DocumentObjectType }
     * 
     */
    public DocumentObjectType createDocumentObjectType() {
        return new DocumentObjectType();
    }

    /**
     * Create an instance of {@link CancelCheckOut }
     * 
     */
    public CancelCheckOut createCancelCheckOut() {
        return new CancelCheckOut();
    }

    /**
     * Create an instance of {@link GetTypeDefinitionResponse }
     * 
     */
    public GetTypeDefinitionResponse createGetTypeDefinitionResponse() {
        return new GetTypeDefinitionResponse();
    }

    /**
     * Create an instance of {@link GetAllVersionsResponse }
     * 
     */
    public GetAllVersionsResponse createGetAllVersionsResponse() {
        return new GetAllVersionsResponse();
    }

    /**
     * Create an instance of {@link Children }
     * 
     */
    public Children createChildren() {
        return new Children();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link DocumentOrFolderObjectType }
     * 
     */
    public DocumentOrFolderObjectType createDocumentOrFolderObjectType() {
        return new DocumentOrFolderObjectType();
    }

    /**
     * Create an instance of {@link GetFolderParent }
     * 
     */
    public GetFolderParent createGetFolderParent() {
        return new GetFolderParent();
    }

    /**
     * Create an instance of {@link ChoiceType }
     * 
     */
    public ChoiceType createChoiceType() {
        return new ChoiceType();
    }

    /**
     * Create an instance of {@link CapabilitiesType }
     * 
     */
    public CapabilitiesType createCapabilitiesType() {
        return new CapabilitiesType();
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link CreateFolderResponse }
     * 
     */
    public CreateFolderResponse createCreateFolderResponse() {
        return new CreateFolderResponse();
    }

    /**
     * Create an instance of {@link CreateDocument }
     * 
     */
    public CreateDocument createCreateDocument() {
        return new CreateDocument();
    }

    /**
     * Create an instance of {@link DeleteAllVersionsResponse }
     * 
     */
    public DeleteAllVersionsResponse createDeleteAllVersionsResponse() {
        return new DeleteAllVersionsResponse();
    }

    /**
     * Create an instance of {@link GetRepositoryInfo }
     * 
     */
    public GetRepositoryInfo createGetRepositoryInfo() {
        return new GetRepositoryInfo();
    }

    /**
     * Create an instance of {@link FailedToDelete }
     * 
     */
    public FailedToDelete createDeleteTreeResponseFailedToDelete() {
        return new FailedToDelete();
    }

    /**
     * Create an instance of {@link DeleteContentStreamResponse }
     * 
     */
    public DeleteContentStreamResponse createDeleteContentStreamResponse() {
        return new DeleteContentStreamResponse();
    }

    /**
     * Create an instance of {@link GetTypes }
     * 
     */
    public GetTypes createGetTypes() {
        return new GetTypes();
    }

    /**
     * Create an instance of {@link CreateDocumentResponse }
     * 
     */
    public CreateDocumentResponse createCreateDocumentResponse() {
        return new CreateDocumentResponse();
    }

    /**
     * Create an instance of {@link PropertyAttributesType }
     * 
     */
    public PropertyAttributesType createPropertyAttributesType() {
        return new PropertyAttributesType();
    }

    /**
     * Create an instance of {@link GetPropertiesResponse }
     * 
     */
    public GetPropertiesResponse createGetPropertiesResponse() {
        return new GetPropertiesResponse();
    }

    /**
     * Create an instance of {@link CreateRelationshipResponse }
     * 
     */
    public CreateRelationshipResponse createCreateRelationshipResponse() {
        return new CreateRelationshipResponse();
    }

    /**
     * Create an instance of {@link DeleteContentStream }
     * 
     */
    public DeleteContentStream createDeleteContentStream() {
        return new DeleteContentStream();
    }

    /**
     * Create an instance of {@link UpdateProperties }
     * 
     */
    public UpdateProperties createUpdateProperties() {
        return new UpdateProperties();
    }

    /**
     * Create an instance of {@link FolderCollection }
     * 
     */
    public FolderCollection createFolderCollection() {
        return new FolderCollection();
    }

    /**
     * Create an instance of {@link GetDocumentParentsResponse }
     * 
     */
    public GetDocumentParentsResponse createGetDocumentParentsResponse() {
        return new GetDocumentParentsResponse();
    }

    /**
     * Create an instance of {@link FolderObjectType }
     * 
     */
    public FolderObjectType createFolderObjectType() {
        return new FolderObjectType();
    }

    /**
     * Create an instance of {@link GetAllowableActions }
     * 
     */
    public GetAllowableActions createGetAllowableActions() {
        return new GetAllowableActions();
    }

    /**
     * Create an instance of {@link MoveObjectResponse }
     * 
     */
    public MoveObjectResponse createMoveObjectResponse() {
        return new MoveObjectResponse();
    }

    /**
     * Create an instance of {@link DeleteObjectResponse }
     * 
     */
    public DeleteObjectResponse createDeleteObjectResponse() {
        return new DeleteObjectResponse();
    }

    /**
     * Create an instance of {@link SetContentStreamResponse }
     * 
     */
    public SetContentStreamResponse createSetContentStreamResponse() {
        return new SetContentStreamResponse();
    }

    /**
     * Create an instance of {@link GetContentStreamResponse }
     * 
     */
    public GetContentStreamResponse createGetContentStreamResponse() {
        return new GetContentStreamResponse();
    }

    /**
     * Create an instance of {@link CreateFolder }
     * 
     */
    public CreateFolder createCreateFolder() {
        return new CreateFolder();
    }

    /**
     * Create an instance of {@link AllowableActionCollection }
     * 
     */
    public AllowableActionCollection createGetAllowableActionsResponseAllowableActionCollection() {
        return new AllowableActionCollection();
    }

    /**
     * Create an instance of {@link DocumentAndFolderCollection }
     * 
     */
    public DocumentAndFolderCollection createDocumentAndFolderCollection() {
        return new DocumentAndFolderCollection();
    }

    /**
     * Create an instance of {@link GetDescendantsResponse }
     * 
     */
    public GetDescendantsResponse createGetDescendantsResponse() {
        return new GetDescendantsResponse();
    }

    /**
     * Create an instance of {@link CheckOutResponse }
     * 
     */
    public CheckOutResponse createCheckOutResponse() {
        return new CheckOutResponse();
    }

    /**
     * Create an instance of {@link RelationshipCollection }
     * 
     */
    public RelationshipCollection createRelationshipCollection() {
        return new RelationshipCollection();
    }

    /**
     * Create an instance of {@link CheckIn }
     * 
     */
    public CheckIn createCheckIn() {
        return new CheckIn();
    }

    /**
     * Create an instance of {@link GetRootFolder }
     * 
     */
    public GetRootFolder createGetRootFolder() {
        return new GetRootFolder();
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
     * Create an instance of {@link CreateRelationship }
     * 
     */
    public CreateRelationship createCreateRelationship() {
        return new CreateRelationship();
    }

    /**
     * Create an instance of {@link GetCheckedoutDocsResponse }
     * 
     */
    public GetCheckedoutDocsResponse createGetCheckedoutDocsResponse() {
        return new GetCheckedoutDocsResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "ObjectNotFoundFault")
    public JAXBElement<BasicFault> createObjectNotFoundFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_ObjectNotFoundFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "OffsetFault")
    public JAXBElement<BasicFault> createOffsetFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_OffsetFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "StorageFault")
    public JAXBElement<BasicFault> createStorageFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_StorageFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "RuntimeFault")
    public JAXBElement<BasicFault> createRuntimeFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_RuntimeFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "StreamNotSupportedFault")
    public JAXBElement<BasicFault> createStreamNotSupportedFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_StreamNotSupportedFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "hasMoreItems")
    public JAXBElement<Boolean> createHasMoreItems(Boolean value) {
        return new JAXBElement<Boolean>(_HasMoreItems_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "FilterNotValidFault")
    public JAXBElement<BasicFault> createFilterNotValidFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_FilterNotValidFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "skipCount")
    public JAXBElement<BigInteger> createSkipCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_SkipCount_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "parent")
    public JAXBElement<String> createParent(String value) {
        return new JAXBElement<String>(_Parent_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "InvalidArgumentFault")
    public JAXBElement<BasicFault> createInvalidArgumentFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_InvalidArgumentFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "OperationNotSupportedFault")
    public JAXBElement<BasicFault> createOperationNotSupportedFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_OperationNotSupportedFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "sourceOID")
    public JAXBElement<String> createSourceOID(String value) {
        return new JAXBElement<String>(_SourceOID_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "filter")
    public JAXBElement<String> createFilter(String value) {
        return new JAXBElement<String>(_Filter_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "TypeNotFoundFault")
    public JAXBElement<BasicFault> createTypeNotFoundFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_TypeNotFoundFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "PermissionDeniedFault")
    public JAXBElement<BasicFault> createPermissionDeniedFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_PermissionDeniedFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "ConcurrencyFault")
    public JAXBElement<BasicFault> createConcurrencyFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_ConcurrencyFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "NotInFolderFault")
    public JAXBElement<BasicFault> createNotInFolderFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_NotInFolderFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "FolderNotValidFault")
    public JAXBElement<BasicFault> createFolderNotValidFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_FolderNotValidFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "AlreadyExistsFault")
    public JAXBElement<BasicFault> createAlreadyExistsFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_AlreadyExistsFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BasicFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "ConstraintViolationFault")
    public JAXBElement<BasicFault> createConstraintViolationFault(BasicFault value) {
        return new JAXBElement<BasicFault>(_ConstraintViolationFault_QNAME, BasicFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "maxItems")
    public JAXBElement<BigInteger> createMaxItems(BigInteger value) {
        return new JAXBElement<BigInteger>(_MaxItems_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.cmis.org/ns/1.0", name = "targetOID")
    public JAXBElement<String> createTargetOID(String value) {
        return new JAXBElement<String>(_TargetOID_QNAME, String.class, null, value);
    }

}
