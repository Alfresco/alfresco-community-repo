using NUnit.Framework;
using System.Collections.Generic;

namespace WcfCmisWSTests {
    ///
    /// author: Dmitry Velichkevich
    ///
    [TestFixture]
    public class MultiFilingServiceClientTest {
        [Test]
        public void testParentAdding() {

            KeyValuePair<string, KeyValuePair<string, string[]>> multifilledDocument =
                                                                  AbstractCmisServicesHelper.createMultiFilledDocument(
                                                                  AbstractCmisServicesHelper.getAndAssertRootFolder());

            AbstractCmisServicesHelper.assertDocumentParents(multifilledDocument.Value.Key,
                                                                                      multifilledDocument.Value.Value);

            AbstractCmisServicesHelper.deleteAndAssertDocument(multifilledDocument.Value.Key);
            AbstractCmisServicesHelper.deleteAndAssertFolder(multifilledDocument.Key, false);
        }

        [Test]
        public void testParentAddingAndRemoving() {

            KeyValuePair<string, KeyValuePair<string, string[]>> multifilledDocument =
                                                                  AbstractCmisServicesHelper.createMultiFilledDocument(
                                                                  AbstractCmisServicesHelper.getAndAssertRootFolder());

            AbstractCmisServicesHelper.createMultiFilingServiceClient().removeObjectFromFolder(
                                                                 AbstractCmisServicesHelper.getAndAssertRepositoryId(),
                                                               multifilledDocument.Value.Key, multifilledDocument.Key);

            AbstractCmisServicesHelper.assertDocumentParents(multifilledDocument.Value.Key,
                                                   new string[] {AbstractCmisServicesHelper.getAndAssertRootFolder()});

            AbstractCmisServicesHelper.deleteAndAssertFolder(multifilledDocument.Key, false);
            AbstractCmisServicesHelper.deleteAndAssertDocument(multifilledDocument.Value.Key);
        }
    }
}
