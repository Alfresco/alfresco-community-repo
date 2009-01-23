using System;
using NUnit.Framework;
using WcfTestClient.RepositoryService;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    [TestFixture]
    public class RepositoryServiceClientTest {
        [Test]
        public void testGetRepositories() {

            try {
                AbstractCmisServicesHelper.getAndAssertRepositoryId();
            } catch (Exception e) {
                Assert.Fail(e.Message);
            }
        }

        [Test]
        public void testGetRepositoryInfo() {

            try {
                RepositoryServicePortClient client = AbstractCmisServicesHelper.createRepositoryServiceClient();

                assertRepositoryInfoReponse(client.getRepositoryInfoWrapper(AbstractCmisServicesHelper.
                                                                                          getAndAssertRepositoryId()));
            } catch (Exception e) {
                Assert.Fail(e.Message);
            }
        }

        [Test]
        public void testGetTypes() {

            try {
                assertGetTypesResponse(AbstractCmisServicesHelper.createRepositoryServiceClient());
            } catch (Exception e) {
                Assert.Fail(e.Message);
            }
        }

        [Test]
        public void testGetTypeDefinition() {

            try {
                RepositoryServicePortClient client = AbstractCmisServicesHelper.createRepositoryServiceClient();

                string typeId = assertGetTypesResponse(client);

                Assert.IsNotNull(typeId);
                Assert.IsTrue(typeId.Length > AbstractCmisServicesHelper.MINIMAL_ARRAY_LIKE_STRUCTURES_LENGTH);

                getTypeDefinitionResponse response = client.getTypeDefinitionWrapper(AbstractCmisServicesHelper.
                                                                                   getAndAssertRepositoryId(), typeId);

                Assert.IsNotNull(response, typeId);
            } catch (Exception e) {
                Assert.Fail(e.Message);
            }
        }

        private static void assertRepositoryInfoReponse(getRepositoryInfoResponse repositoryInfo) {

            Assert.IsNotNull(repositoryInfo);
            assertCapabilities(repositoryInfo);
        }

        private string assertGetTypesResponse(RepositoryServicePortClient client) {

            bool hasMoreElements;

            cmisTypeDefinitionType[] types = client.getTypes(AbstractCmisServicesHelper.getAndAssertRepositoryId(),
                              AbstractCmisServicesHelper.DOCUMENT_TYPE, false, AbstractCmisServicesHelper.TEXTUAL_ZERO,
                                                         AbstractCmisServicesHelper.TEXTUAL_ZERO, out hasMoreElements);
            Assert.IsNotNull(types);
            Assert.IsTrue((types.Length >= AbstractCmisServicesHelper.MINIMAL_ARRAY_LIKE_STRUCTURES_LENGTH)
                                                                              && types[0].fileable && hasMoreElements);

            return types[new Random().Next(types.Length)].typeId;
        }

        private static void assertCapabilities(getRepositoryInfoResponse repositoryInfo) {

            Assert.IsFalse(repositoryInfo.capabilities.capabilityUnfiling);
            Assert.IsFalse(repositoryInfo.capabilities.capabilityPWCSearchable);
            Assert.IsFalse(repositoryInfo.capabilities.capabilityVersionSpecificFiling);
            Assert.IsFalse(repositoryInfo.capabilities.capabilityAllVersionsSearchable);

            Assert.IsTrue(repositoryInfo.capabilities.capabilityMultifiling);
            Assert.IsTrue(repositoryInfo.capabilities.capabilityPWCUpdateable);

            Assert.AreEqual(enumCapabilityJoin.nojoin, repositoryInfo.capabilities.capabilityJoin);
            Assert.AreEqual(enumCapabilityQuery.none, repositoryInfo.capabilities.capabilityQuery);
            Assert.AreEqual(enumCapabilityFullText.fulltextandstructured,
                                                                       repositoryInfo.capabilities.capabilityFullText);
        }
    }
}
