using System;
using NUnit.Framework;
using WcfTestClient.DiscoveryService;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    [TestFixture]
    public class DiscoveryServiceClientTest {
        private const string TEXTUAL_TEN = "10";
        private const string SAMPLE_QUERY = "SELECT * FROM DOCUMENT";

        [Test]
        public void testCmisSqlFeature() {

            try {
                assertQuering(AbstractCmisServicesHelper.createDiscoveryServiceClient());
            } catch(Exception e) {
                Assert.Fail(e.Message);
            }
        }

        private static void assertQuering(DiscoveryServicePortClient client) {

            bool hasMoreElements;

            cmisObjectType[] response = client.query(SAMPLE_QUERY, false, TEXTUAL_TEN,
                                            AbstractCmisServicesHelper.TEXTUAL_ZERO, false, null, out hasMoreElements);

            Assert.IsTrue((response != null) && (response[0] != null));
            Assert.IsTrue(hasMoreElements);
            Assert.AreEqual(response.Length, int.Parse(TEXTUAL_TEN));
        }
    }
}
