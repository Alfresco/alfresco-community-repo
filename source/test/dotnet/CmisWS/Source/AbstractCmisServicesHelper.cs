using System;
using System.Text;
using NUnit.Framework;
using System.ServiceModel;
using System.Collections.Generic;
using WcfTestClient.ObjectService;
using WcfTestClient.DiscoveryService;
using WcfTestClient.NavigationService;
using WcfTestClient.RepositoryService;
using WcfTestClient.VersioningService;
using WcfTestClient.MultiFilingService;

///
/// author: Dmitry Velichkevich
///
namespace WcfCmisWSTests {
    public abstract class AbstractCmisServicesHelper {
        public const int MINIMAL_ARRAY_LIKE_STRUCTURES_LENGTH = 1;

        public const string TEXTUAL_ZERO = "0";

        public const string ANY_PROPERTY_FILTER = "*";

        public const string DOCUMENT_TYPE = "document";
        public const string FOLDER_TYPE = "folder";

        public const string NAME_PROPERTY = "Name";
        public const string OBJECT_IDENTIFIER_PROPERTY = "ObjectId";

        public const string TEXT_DOCUMENT_POSTFIX = ".txt";
        public const string TEXT_DOCUMENT_MIMETYPE = "plain/text";

        public const string DEFAULT_ENCODING = "utf-8";

        public const string TEST_DOCUMENT_CONTENT_ENTRY_TEXT = "Test document Content Entry";

        public static CmisTypesConverter<WcfTestClient.ObjectService.cmisProperty[],
                                                 WcfTestClient.NavigationService.cmisProperty[]> NAVIGATION_CONVERTER =
                                                                  new NavigationServiceToObjectServiceTypesConverter();
        public static CmisTypesConverter<WcfTestClient.ObjectService.cmisProperty[],
                                                 WcfTestClient.VersioningService.cmisProperty[]> VERSIONING_CONVERTER =
                                                                  new VersioningServiceToObjectServiceTypesConverter();

        private const int ARRAY_BASED_STRUCTURE_BEING_INDEX = 0;
        private const int SAMPLE_LENGTH_MINUS_ONE_ODDS = 2;

        private const int MAXIMUM_ODD_OBJECTS_AMOUNT = 5;
        private const int NEAR_ZERO_RANDOM_MAXIMUM = 3;

        private const string DELIMETER = "/";

        private const string DEFAULT_ADMIN_USERNAME = "admin";
        private const string DEFAULT_ADMIN_PASSWORD = "admin";

        private const string CHECKED_OUT_PROPERTY = "IsVersionSeriesCheckedOut";
        private const string LATEST_VERSION_PROPERTY = "IsLatestVersion";

        private const string FOLDER_NAME_PREFIX = "TestFolder (";
        private const string DOCUMENT_NAME_PREFIX = "TestDocument (";

        private const string CMIS_OBJECTS_DELETION_FAILED_MESSAGE_PATTERN = "{0} with Id {1} was not deleted";

        private const string CMIS_HTTP_BINDING_CONFIGURATION_NAME = "cmisUnsecureHttpBinding";

        private const string SECURE_HTTP_SCHEMA = "https";

        private static string DEFAULT_REQUEST_URL_BEGIN = "http://localhost:8080";
        private const string COMMON_SERVICE_URL_PATTERN = "{0}/alfresco/cmis/{1}?wsdl";
        private const string REPOSITORY_SERVICE_NAME = "RepositoryService";
        private const string OBJECT_SERVICE_NAME = "ObjectService";
        private const string NAVIGATION_SERVICE_NAME = "NavigationService";
        private const string MULTIFILING_SERVICE_NAME = "MultiFilingService";
        private const string VERSIONING_SERVICE_NAME = "VersioningService";
        private const string DISCOVERY_SERVICE_NAME = "DiscoveryService";

        private static byte[] testDocumentContentEntry = Encoding.GetEncoding(DEFAULT_ENCODING)
                                                                           .GetBytes(TEST_DOCUMENT_CONTENT_ENTRY_TEXT);

        private static string userName = DEFAULT_ADMIN_USERNAME;
        private static string password = DEFAULT_ADMIN_PASSWORD;

        public static RepositoryServicePortClient createRepositoryServiceClient() {

            EndpointAddress serviceAddress = new EndpointAddress(string.Format(COMMON_SERVICE_URL_PATTERN,
                                                   new object[] {DEFAULT_REQUEST_URL_BEGIN, REPOSITORY_SERVICE_NAME}));

            RepositoryServicePortClient result = new RepositoryServicePortClient(createBindingFromConfig(
                                                                                      serviceAddress), serviceAddress);

            result.ChannelFactory.Endpoint.Behaviors.Add(new SoapRequestHeaderProcessorAttribute(UserName, Password));

            return result;
        }

        public static ObjectServicePortClient createObjectServiceClient() {

            EndpointAddress serviceAddress = new EndpointAddress(string.Format(COMMON_SERVICE_URL_PATTERN,
                                                       new object[] {DEFAULT_REQUEST_URL_BEGIN, OBJECT_SERVICE_NAME}));

            ObjectServicePortClient result = new ObjectServicePortClient(createBindingFromConfig(serviceAddress, true),
                                                                                                       serviceAddress);

            result.ChannelFactory.Endpoint.Behaviors.Add(new SoapRequestHeaderProcessorAttribute(UserName, Password));

            return result;
        }

        public static ObjectServicePortClient createObjectServiceClient(string customUserName, string customPassword) {

            userName = customUserName;
            password = customPassword;

            WcfTestClient.ObjectService.ObjectServicePortClient result = createObjectServiceClient();

            userName = DEFAULT_ADMIN_USERNAME;
            password = DEFAULT_ADMIN_PASSWORD;

            return result;
        }

        public static NavigationServicePortClient createNavigationServiceClient() {

            EndpointAddress serviceAddress = new EndpointAddress(string.Format(COMMON_SERVICE_URL_PATTERN,
                                                   new object[] {DEFAULT_REQUEST_URL_BEGIN, NAVIGATION_SERVICE_NAME}));

            NavigationServicePortClient result = new NavigationServicePortClient(createBindingFromConfig(
                                                                                      serviceAddress), serviceAddress);

            result.ChannelFactory.Endpoint.Behaviors.Add(new SoapRequestHeaderProcessorAttribute(UserName, Password));

            return result;
        }

        public static VersioningServicePortClient createVersioningServiceClient() {

            EndpointAddress serviceAddress = new EndpointAddress(string.Format(COMMON_SERVICE_URL_PATTERN,
                                                   new object[] {DEFAULT_REQUEST_URL_BEGIN, VERSIONING_SERVICE_NAME}));

            VersioningServicePortClient result = new VersioningServicePortClient(createBindingFromConfig(
                                                                                      serviceAddress), serviceAddress);

            result.ChannelFactory.Endpoint.Behaviors.Add(new SoapRequestHeaderProcessorAttribute(UserName, Password));

            return result;
        }

        public static DiscoveryServicePortClient createDiscoveryServiceClient() {

            EndpointAddress serviceAddress = new EndpointAddress(string.Format(COMMON_SERVICE_URL_PATTERN,
                                                    new object[] {DEFAULT_REQUEST_URL_BEGIN, DISCOVERY_SERVICE_NAME}));

            DiscoveryServicePortClient result = new DiscoveryServicePortClient(createBindingFromConfig(
                                                                                      serviceAddress), serviceAddress);

            result.ChannelFactory.Endpoint.Behaviors.Add(new SoapRequestHeaderProcessorAttribute(UserName, Password));

            return result;
        }

        public static MultiFilingServicePortClient createMultiFilingServiceClient() {

            EndpointAddress serviceAddress = new EndpointAddress(string.Format(COMMON_SERVICE_URL_PATTERN,
                                                  new object[] {DEFAULT_REQUEST_URL_BEGIN, MULTIFILING_SERVICE_NAME}));

            MultiFilingServicePortClient result = new MultiFilingServicePortClient(createBindingFromConfig(
                                                                                      serviceAddress), serviceAddress);

            result.ChannelFactory.Endpoint.Behaviors.Add(new SoapRequestHeaderProcessorAttribute(UserName, Password));

            return result;
        }

        public static string getAndAssertRepositoryId() {

            RepositoryServicePortClient client = createRepositoryServiceClient();

            WcfTestClient.RepositoryService.cmisRepositoryEntryType[] repositories = client.getRepositories();

            assertRepositoriesResponse(repositories);

            return repositories[0].repositoryID;
        }

        public static string getAndAssertRootFolder() {

            return performManipulationsAndAssertionWrapping(new RootFolderReceiverStrategy());
        }

        public static string generateObjectName(string postfix) {

            bool document = ((postfix != null) && (postfix.Length >= MINIMAL_ARRAY_LIKE_STRUCTURES_LENGTH));

            return ((document) ? (DOCUMENT_NAME_PREFIX):(FOLDER_NAME_PREFIX)) + DateTime.Now.Ticks.ToString() + ")" +
                                                                               ((document) ? (postfix):(string.Empty));
        }

        public static WcfTestClient.ObjectService.cmisPropertiesType createCmisObjectProperties(string documentName) {

            WcfTestClient.ObjectService.cmisPropertiesType result =
                                                                  new WcfTestClient.ObjectService.cmisPropertiesType();

            WcfTestClient.ObjectService.cmisPropertyString nameProperty =
                                                                  new WcfTestClient.ObjectService.cmisPropertyString();
            nameProperty.value = documentName;
            nameProperty.name = NAME_PROPERTY;
            result.Items = new WcfTestClient.ObjectService.cmisProperty[] {nameProperty};

            return result;
        }

        public static byte[] getTestDocumentContentEntry() {

            return testDocumentContentEntry;
        }

        public static WcfTestClient.ObjectService.cmisContentStreamType createCmisDocumentContent(string documentName,
                                                                                string mimeType, byte[] contentEntry) {

            WcfTestClient.ObjectService.cmisContentStreamType result =
                                                               new WcfTestClient.ObjectService.cmisContentStreamType();
            result.filename = documentName;
            result.length = contentEntry.Length.ToString();
            result.mimeType = mimeType;
            result.stream = contentEntry;

            return result;
        }

        public static string createAndAssertDocument(string documentName, string parentFolder, string mimeType,
                                                                                                 byte[] contentEntry) {

            return performManipulationsAndAssertionWrapping(new DocumentCreatorStrategy(documentName, parentFolder,
                                                                   mimeType, enumVersioningState.major, contentEntry));
        }

        public static string createAndAssertDocument(string documentName, string parentFolder, string mimeType,
                                                            enumVersioningState versioningState, byte[] contentEntry) {

            return performManipulationsAndAssertionWrapping(new DocumentCreatorStrategy(documentName, parentFolder,
                                                                              mimeType, versioningState, contentEntry));
        }

        public static string createAndAssertFolder(string folderName, string parentFolder) {

            return performManipulationsAndAssertionWrapping(new FolderCreatorStrategy(folderName, parentFolder));
        }

        public static string[] createAndAssertFileFolderHierarchy(int depth, int minimalChildrenAmount,
                                                                    int maximumChildrenAmount, bool withoutDocuments) {

            List<string> hierarchyObjects = new List<string>();
            Queue<KeyValuePair<int, string>> folders = createAndInitializeFoldersQueue(hierarchyObjects);

            Random randomCounter = new Random();

            while((folders.Count > 0) && (folders.Peek().Key <= (depth - 1))) {
                createObjectsForCurrentLevel(minimalChildrenAmount, maximumChildrenAmount, withoutDocuments,
                                                          hierarchyObjects, folders, randomCounter, folders.Dequeue());
            }

            return hierarchyObjects.ToArray();
        }

        public static WcfTestClient.ObjectService.cmisPropertiesType getObjectProperties(string objectId,
                                                                                                  bool assertResults) {

            WcfTestClient.ObjectService.ObjectServicePortClient client = createObjectServiceClient();

            WcfTestClient.ObjectService.cmisObjectType result = client.getProperties(
                                                                                  getAndAssertRepositoryId(), objectId,
                                                                         enumReturnVersion.latest, ANY_PROPERTY_FILTER,
                                                                                                         false, false);

            if(assertResults) {
                assertrObjectProperties(result);
            }

            return (result != null) ? (result.properties):(null);
        }

        public static object searchPropertyAndGetValueByName(WcfTestClient.ObjectService.cmisProperty[] properties,
                                                                                                 string propertyName) {

            foreach(WcfTestClient.ObjectService.cmisProperty property in properties) {
                if ((property.name != null) && property.name.Equals(propertyName)) {
                    return determinePropertyValue(property);
                }
            }

            return null;
        }

        public static void deleteAndAssertDocument(string documentId) {

            WcfTestClient.ObjectService.ObjectServicePortClient client = createObjectServiceClient();

            client.deleteObject(getAndAssertRepositoryId(), documentId);

            assertObjectAbsence(documentId, DOCUMENT_TYPE);
        }

        public static void deleteAndAssertFolder(string folderId, bool notEmptyBehaviour) {

            WcfTestClient.ObjectService.ObjectServicePortClient client = createObjectServiceClient();

            try {
                client.deleteObject(getAndAssertRepositoryId(), folderId);

                determineAssertionFailed(notEmptyBehaviour, "Not empty folder was deleted");
            } catch (Exception e) {
                determineAssertionFailed(!notEmptyBehaviour, e.Message);
            }

            assertObjectAbsence(folderId, FOLDER_TYPE);
        }

        public static void assertDocumentParents(string documentId, string[] expectedParentsIds) {

            performParentsReceivingWrapping(new ObjectParentsReceiverStrategy(documentId), expectedParentsIds);
        }

        public static void assertFolderParents(string folderId, string[] expectedParentsIds, bool allParents) {

            performParentsReceivingWrapping(new FolderParentsReceiverStrategy(folderId, allParents), expectedParentsIds);
        }

        public static void deleteAndAssertHierarchy(string[] hierarchyObjectsIds) {

            WcfTestClient.ObjectService.ObjectServicePortClient client = createObjectServiceClient();

            string[] undeletedObjectsIds = client.deleteTree(getAndAssertRepositoryId(), hierarchyObjectsIds[0],
                                                                              enumUnfileNonfolderObjects.delete, true);

            Assert.IsNotNull(undeletedObjectsIds);
            Assert.AreEqual(undeletedObjectsIds.Length, ARRAY_BASED_STRUCTURE_BEING_INDEX);

            foreach(string objectId in hierarchyObjectsIds) {
                assertObjectAbsence(objectId, null);
            }
        }

        public static string[] addOneMoreParent(string documentId, string parentFolderId) {

            string repositoryId = getAndAssertRepositoryId();

            string[] result = receiveOldParentsList(parentFolderId, createNavigationServiceClient().getObjectParents(
                                                         repositoryId, documentId, ANY_PROPERTY_FILTER, false, false));

            createMultiFilingServiceClient().addObjectToFolder(repositoryId, documentId, parentFolderId);

            return result;
        }

        public static KeyValuePair<string, KeyValuePair<string, string[]>> createMultiFilledDocument(
                                                                                              string primaryFolderId) {

            string documentId = createAndAssertDocument(generateObjectName(TEXT_DOCUMENT_POSTFIX), primaryFolderId,
                                                                     TEXT_DOCUMENT_MIMETYPE, testDocumentContentEntry);

            string folderId = createAndAssertFolder(generateObjectName(null), primaryFolderId);

            return new KeyValuePair<string, KeyValuePair<string, string[]>>(folderId, new KeyValuePair<string,
                      string[]>(documentId, addOneMoreParent(documentId, folderId)));
        }

        public static string checkOutAndAssert(string documentId) {

            bool copied;

            createVersioningServiceClient().checkOut(getAndAssertRepositoryId(), ref documentId, out copied);

            assertCheckedOutDocument(documentId, copied);

            return documentId;
        }

        public static void assertCheckedOutDocument(string documentId, bool copied) {

            Assert.IsNotNull(documentId);
            Assert.IsTrue(copied && (bool)searchPropertyAndGetValueByName(getObjectProperties(documentId, true).Items,
                                                                                                CHECKED_OUT_PROPERTY));
        }

        public static void cancelCheckOutAndAssert(string checkedOutDocumentId) {

            createVersioningServiceClient().cancelCheckOut(getAndAssertRepositoryId(), checkedOutDocumentId);

            assertObjectAbsence(checkedOutDocumentId, null);
        }

        public static void getAndAssertLatestVersionProperties(string versionSeriesId, string name,
                                                                       string expectedVersionSuffix, bool onlyLatest) {

            VersioningServicePortClient client = createVersioningServiceClient();

            WcfTestClient.VersioningService.cmisObjectType response = client.getPropertiesOfLatestVersion(
                                              getAndAssertRepositoryId(), versionSeriesId, false, ANY_PROPERTY_FILTER);

            asserLatestVersionProperties(versionSeriesId, name, expectedVersionSuffix, onlyLatest, response);
        }

        public static void assertActualIdsFromObjectsWithSpecified(WcfTestClient.NavigationService.cmisObjectType[]
                                                                                 actualObjects, string[] expectedIds) {

            bool value;

            Dictionary<string, bool> expectedIdsMap = createExpectedParentsIdsMap(expectedIds);

            foreach (WcfTestClient.NavigationService.cmisObjectType actualParent in actualObjects) {
                Assert.IsTrue(expectedIdsMap.TryGetValue((string)searchPropertyAndGetValueByName(
                                                 NAVIGATION_CONVERTER.convertProperties(actualParent.properties.Items),
                                                                              OBJECT_IDENTIFIER_PROPERTY), out value));
            }
        }

        public static string UserName {
            get {

                return userName;
            }
        }

        public static string Password {
            get {

                return password;
            }
        }

        private AbstractCmisServicesHelper() {
        }

        private static BasicHttpBinding createBindingFromConfig(EndpointAddress serviceAddress, bool mtomEncoding) {

            BasicHttpBinding result = createBindingFromConfig(serviceAddress);

            result.MessageEncoding = (mtomEncoding) ? (WSMessageEncoding.Mtom):(WSMessageEncoding.Text);

            return result;
        }

        private static BasicHttpBinding createBindingFromConfig(EndpointAddress serviceAddress) {

            BasicHttpBinding result = new BasicHttpBinding(CMIS_HTTP_BINDING_CONFIGURATION_NAME);

            if(serviceAddress.Uri.Scheme.ToLower().Equals(SECURE_HTTP_SCHEMA)) {
                result.Security.Mode = BasicHttpSecurityMode.Transport;
            }

            return result;
        }

        private static string performManipulationsAndAssertionWrapping(CmisManipulationsStrategy<
                                                                                               string> createVisitor) {

            string result = null;

            try {
                result = createVisitor.performManipulations();

                Assert.IsNotNull(result);
                Assert.IsTrue(result.Length > MINIMAL_ARRAY_LIKE_STRUCTURES_LENGTH);
            } catch(Exception e) {
                Assert.Fail(e.Message);
            }

            return result;
        }

        private static void performParentsReceivingWrapping(CmisManipulationsStrategy<
                             WcfTestClient.NavigationService.cmisObjectType[]> receiver, string[] expectedParentsIds) {

            WcfTestClient.NavigationService.cmisObjectType[] actualParents = receiver.performManipulations();

            Assert.IsTrue(actualParents != null);
            Assert.AreEqual(expectedParentsIds.Length, actualParents.Length);

            assertActualIdsFromObjectsWithSpecified(actualParents, expectedParentsIds);
        }

        private static void assertRepositoriesResponse(WcfTestClient.RepositoryService.cmisRepositoryEntryType[]
                                                                                                        repositories) {

            Assert.IsNotNull(repositories);
            Assert.IsNotNull(repositories[0]);
            Assert.IsTrue(repositories[0].repositoryID.Length > MINIMAL_ARRAY_LIKE_STRUCTURES_LENGTH);
        }

        private static void assertrObjectProperties(WcfTestClient.ObjectService.cmisObjectType result) {

            Assert.IsNotNull(result);
            Assert.IsNotNull(result.properties);
            Assert.IsNotNull(result.properties.Items);
            Assert.IsTrue(result.properties.Items.Length >= MINIMAL_ARRAY_LIKE_STRUCTURES_LENGTH);
            Assert.IsNotNull(searchPropertyAndGetValueByName(result.properties.Items, NAME_PROPERTY));
        }

        private static void assertObjectAbsence(string objectId, string objectName) {

            try {
                getObjectProperties(objectId, false);

                Assert.Fail(string.Format(CMIS_OBJECTS_DELETION_FAILED_MESSAGE_PATTERN, new object[] {objectName,
                                                                                                           objectId}));
            } catch (Exception) {
            }
        }

        private static object determinePropertyValue(WcfTestClient.ObjectService.cmisProperty property) {

            if(property is WcfTestClient.ObjectService.cmisPropertyString) {
                return ((WcfTestClient.ObjectService.cmisPropertyString)property).value;
            }

            if(property is WcfTestClient.ObjectService.cmisPropertyBoolean) {
                return ((WcfTestClient.ObjectService.cmisPropertyBoolean)property).value;
            }

            if(property is WcfTestClient.ObjectService.cmisPropertyDateTime) {
                return ((WcfTestClient.ObjectService.cmisPropertyDateTime)property).value;
            }

            if(property is WcfTestClient.ObjectService.cmisPropertyDecimal) {
                return ((WcfTestClient.ObjectService.cmisPropertyDecimal)property).value;
            }

            if(property is WcfTestClient.ObjectService.cmisPropertyId) {
                return ((WcfTestClient.ObjectService.cmisPropertyId)property).value;
            }

            if(property is WcfTestClient.ObjectService.cmisPropertyInteger) {
                return ((WcfTestClient.ObjectService.cmisPropertyInteger)property).value;
            }

            if(property is WcfTestClient.ObjectService.cmisPropertyUri) {
                return ((WcfTestClient.ObjectService.cmisPropertyUri)property).value;
            }

            return null;
        }

        private static void determineAssertionFailed(bool assertionCondition, string message) {

            if(assertionCondition) {
                Assert.Fail(message);
            }
        }

        private static System.Collections.Generic.Dictionary<string, bool> createExpectedParentsIdsMap(
                                                                                                string[] expectedIds) {

            System.Collections.Generic.Dictionary<string, bool> result =
                                                             new System.Collections.Generic.Dictionary<string, bool>();

            foreach (string key in expectedIds) {
                result.Add(key, false);
            }

            return result;
        }

        private static Queue<KeyValuePair<int, string>> createAndInitializeFoldersQueue(
                                                                                       List<string> hierarchyObjects) {

            Queue<KeyValuePair<int, string>> result = new Queue<KeyValuePair<int, string>>();

            result.Enqueue(new KeyValuePair<int, string>(MINIMAL_ARRAY_LIKE_STRUCTURES_LENGTH, createAndAssertFolder(
                                                                 generateObjectName(null), getAndAssertRootFolder())));
            hierarchyObjects.Add(result.Peek().Value);

            return result;
        }

        private static void createObjectsForCurrentLevel(int minimalChildrenAmount, int maximumChildrenAmount,
                                                                  bool withoutDocuments, List<string> hierarchyObjects,
                                                        Queue<KeyValuePair<int, string>> folders, Random randomCounter,
                                                                             KeyValuePair<int, string> currentParent) {

            for(int i = 0; i < generateBoundedChildrenAmount(randomCounter, minimalChildrenAmount,
                                                                                         maximumChildrenAmount); i++) {
                hierarchyObjects.Add(createDeterminedObject(withoutDocuments, folders, randomCounter, currentParent));
            }
        }

        private static int generateBoundedChildrenAmount(Random randomCounter, int minimalChildrenAmount,
                                                                                           int maximumChildrenAmount) {

            int result = randomCounter.Next(MAXIMUM_ODD_OBJECTS_AMOUNT);

            result = ((minimalChildrenAmount < 0) && ((minimalChildrenAmount + result) < 0)) ? (0):
                                                                                      (result + minimalChildrenAmount);

            return ((maximumChildrenAmount > 0) && (result > maximumChildrenAmount)) ? (maximumChildrenAmount):
                                                                                                              (result);
        }

        private static string createDeterminedObject(bool withoutDocuments, Queue<KeyValuePair<int, string>> folders,
                                                       Random randomCounter, KeyValuePair<int, string> currentParent) {

            bool folder = withoutDocuments || (randomCounter.Next(NEAR_ZERO_RANDOM_MAXIMUM) == 0);

            CmisManipulationsStrategy<string> objectCreator = (folder) ? ((CmisManipulationsStrategy<string>)
                                               new FolderCreatorStrategy(generateObjectName(null), currentParent.Value)):
                                 ((CmisManipulationsStrategy<string>)new DocumentCreatorStrategy(generateObjectName(
                                                   TEXT_DOCUMENT_POSTFIX), currentParent.Value, TEXT_DOCUMENT_MIMETYPE,
                                                                 enumVersioningState.major, testDocumentContentEntry));

            string objectId = objectCreator.performManipulations();

            if(folder) {
                folders.Enqueue(new KeyValuePair<int, string>((currentParent.Key + 1), objectId));
            }

            return objectId;
        }

        private static string[] receiveOldParentsList(string parentFolderId,
                                                            WcfTestClient.NavigationService.cmisObjectType[] parents) {

            string[] result = new string[parents.Length + 1];

            int index = 0;

            foreach(WcfTestClient.NavigationService.cmisObjectType parent in parents) {
                result[index++] = searchPropertyAndGetValueByName(NAVIGATION_CONVERTER.convertProperties(
                                                      parent.properties.Items), OBJECT_IDENTIFIER_PROPERTY).ToString();
            }

            result[index] = parentFolderId;

            return result;
        }

        private static void asserLatestVersionProperties(string versionSeriesId, string name,
              string expectedVersionSuffix, bool onlyLatest, WcfTestClient.VersioningService.cmisObjectType response) {

            Assert.IsNotNull(response);
            Assert.IsTrue((bool)searchPropertyAndGetValueByName(VERSIONING_CONVERTER.convertProperties(
                                                                 response.properties.Items), LATEST_VERSION_PROPERTY));
            Assert.AreEqual(name, searchPropertyAndGetValueByName(
                                    VERSIONING_CONVERTER.convertProperties(response.properties.Items), NAME_PROPERTY));

            assertVersionId(versionSeriesId, expectedVersionSuffix, response, onlyLatest);
        }

        private static void assertVersionId(string sourceId, string expectedVersionNumber,
                                            WcfTestClient.VersioningService.cmisObjectType response, bool onlyLatest) {

            sourceId = (onlyLatest) ? (sourceId.Substring(0, sourceId.LastIndexOf(DELIMETER))):(sourceId);

            string latestVersionId = (string)searchPropertyAndGetValueByName(
                        VERSIONING_CONVERTER.convertProperties(response.properties.Items), OBJECT_IDENTIFIER_PROPERTY);

            Assert.AreEqual(sourceId, latestVersionId);

            if(expectedVersionNumber != null) {
                Assert.IsTrue(latestVersionId.EndsWith(expectedVersionNumber)); 
            }
        }

        private class ObjectParentsReceiverStrategy: CmisManipulationsStrategy<
                                                                    WcfTestClient.NavigationService.cmisObjectType[]> {
            private string objectId;

            string CmisManipulationsStrategy<WcfTestClient.NavigationService.cmisObjectType[]>.getName() {

                return null;
            }

            public ObjectParentsReceiverStrategy(string objectId) {

                this.objectId = objectId;
            }

            WcfTestClient.NavigationService.cmisObjectType[] CmisManipulationsStrategy<
                                             WcfTestClient.NavigationService.cmisObjectType[]>.performManipulations() {

                return createNavigationServiceClient().getObjectParents(getAndAssertRepositoryId(), objectId,
                                                                                    ANY_PROPERTY_FILTER, false, false);
            }
        }

        private class FolderParentsReceiverStrategy: CmisManipulationsStrategy<
                                                                    WcfTestClient.NavigationService.cmisObjectType[]> {
            private bool allParents;
            private string folderId;

            string CmisManipulationsStrategy<WcfTestClient.NavigationService.cmisObjectType[]>.getName() {

                return null;
            }

            public FolderParentsReceiverStrategy(string folderId, bool allParents) {

                this.folderId = folderId;
                this.allParents = allParents;
            }

            WcfTestClient.NavigationService.cmisObjectType[] CmisManipulationsStrategy<
                                             WcfTestClient.NavigationService.cmisObjectType[]>.performManipulations() {

                return createNavigationServiceClient().getFolderParent(getAndAssertRepositoryId(), folderId,
                                                                        ANY_PROPERTY_FILTER, false, false, allParents);
            }
        }

        private class RootFolderReceiverStrategy: CmisManipulationsStrategy<string> {
            string CmisManipulationsStrategy<string>.getName() {

                return string.Empty;
            }

            string CmisManipulationsStrategy<string>.performManipulations() {

                return createRepositoryServiceClient().getRepositoryInfoWrapper(getAndAssertRepositoryId())
                                                                                                         .rootFolderId;
            }
        }
    }
}
