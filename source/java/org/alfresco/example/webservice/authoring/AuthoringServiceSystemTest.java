/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.example.webservice.authoring;


import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.alfresco.example.webservice.BaseWebServiceSystemTest;
import org.alfresco.example.webservice.content.Content;
import org.alfresco.example.webservice.types.ContentFormat;
import org.alfresco.example.webservice.types.NamedValue;
import org.alfresco.example.webservice.types.ParentReference;
import org.alfresco.example.webservice.types.Predicate;
import org.alfresco.example.webservice.types.Reference;
import org.alfresco.example.webservice.types.Version;
import org.alfresco.example.webservice.types.VersionHistory;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.namespace.QName;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthoringServiceSystemTest extends BaseWebServiceSystemTest
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(AuthoringServiceSystemTest.class);

    private static final String INITIAL_VERSION_CONTENT = "Content of the initial version";
    private static final String SECOND_VERSION_CONTENT = "The content for the second version is completely different";
    
    private static final String VALUE_DESCRIPTION = "description";

    private AuthoringServiceSoapBindingStub authoringService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        try
        {
            EngineConfiguration config = new FileProvider(getResourcesDir(),
                    "client-deploy.wsdd");
            this.authoringService = (AuthoringServiceSoapBindingStub) new AuthoringServiceLocator(
                    config).getAuthoringService();
        } 
        catch (ServiceException jre)
        {
            if (jre.getLinkedCause() != null)
            {
                jre.getLinkedCause().printStackTrace();
            }

            throw new AssertionFailedError("JAX-RPC ServiceException caught: "
                    + jre);
        }

        assertNotNull(this.authoringService);

        // Time out after a minute
        this.authoringService.setTimeout(60000);
    }

    /**
     * Tests the checkout service method
     * 
     * @throws Exception
     */
    public void testCheckout() throws Exception
    {
        doCheckOut();
        
        // TODO test multiple checkout
    }
    
    /**
     * Reusable method to do a standard checkout
     * 
     * @return
     * @throws Exception
     */
    private Reference doCheckOut() throws Exception
    {
        // Use the helper to create the verionable node
        Reference reference = createContentAtRoot("version_test.txt", INITIAL_VERSION_CONTENT);        
        Predicate predicate = convertToPredicate(reference);

        // Check the content out (to the same detination)
        CheckoutResult result = this.authoringService.checkout(predicate, null);
        assertNotNull(result);
        assertEquals(1, result.getOriginals().length);
        assertEquals(1, result.getWorkingCopies().length);
        
        // TODO need to check that the working copy and the origional are in the correct states ...
        
        return result.getWorkingCopies()[0];
    }

    /**
     * Tests the checkout service method passing a destination for the working
     * copy
     * 
     * @throws Exception
     */
    public void testCheckoutWithDestination() throws Exception
    {
        Reference reference = createContentAtRoot("version_test.txt", INITIAL_VERSION_CONTENT);
        Predicate predicate = convertToPredicate(reference);        
        ParentReference parentReference = getFolderParentReference(QName.createQName("{test}workingCopy"));
        
        // Checkout the content to the folder
        CheckoutResult result = this.authoringService.checkout(predicate, parentReference);
        assertNotNull(result);
        assertEquals(1, result.getOriginals().length);
        assertEquals(1, result.getWorkingCopies().length);
        
        // TODO need to check that the working copy and the origional are in the correct states
    }

    /**
     * Tests the checkin service method
     * 
     * @throws Exception
     */
    public void testCheckin() throws Exception
    {
        // First we need to check a document out
        Reference workingCopy = doCheckOut();
        
        // Check in but keep checked out
        Predicate predicate = convertToPredicate(workingCopy);
        NamedValue[] comments = getVersionComments();
        CheckinResult checkinResult = this.authoringService.checkin(predicate, comments, true);
        
        // Check the result
        assertNotNull(checkinResult);
        assertEquals(1, checkinResult.getCheckedIn().length);
        assertEquals(1, checkinResult.getWorkingCopies().length);
        // TODO check that state of the orig and working copies
        
        // Checkin but don't keep checked out
        Predicate predicate2 = convertToPredicate(checkinResult.getWorkingCopies()[0]);
        CheckinResult checkinResult2 = this.authoringService.checkin(predicate2, comments, false);
        
        // Check the result
        assertNotNull(checkinResult2);
        assertEquals(1, checkinResult2.getCheckedIn().length);
        assertNull(checkinResult2.getWorkingCopies());
        // TODO check the above behaviour ...
        // TODO check that the state of the org and working copies
        
        // TODO check multiple checkin
    }

    /**
     * Helper method to get a list of version comments
     * 
     * @return
     */
    private NamedValue[] getVersionComments()
    {
        NamedValue[] comments = new NamedValue[1];
        comments[0] = new NamedValue(org.alfresco.service.cmr.version.Version.PROP_DESCRIPTION, VALUE_DESCRIPTION);
        return comments;
    }

    /**
     * Tests the checkinExternal service method
     * 
     * @throws Exception
     */
    public void testCheckinExternal() throws Exception
    {
        // First we need to check a document out
        Reference workingCopy = doCheckOut();
        
        // Check in with external content
        NamedValue[] comments = getVersionComments();
        ContentFormat contentFormat = new ContentFormat(MimetypeMap.MIMETYPE_TEXT_PLAIN, "UTF-8");
        Reference origionalNode = this.authoringService.checkinExternal(workingCopy, comments, false, contentFormat, SECOND_VERSION_CONTENT.getBytes());
        
        // Check the origianl Node
        assertNotNull(origionalNode);
        Content[] contents = this.contentService.read(new Predicate(new Reference[]{origionalNode}, getStore(), null), ContentModel.PROP_CONTENT.toString());
        Content readResult = contents[0];
        assertNotNull(readResult);
        String checkedInContent = getContentAsString(readResult.getUrl());
        assertNotNull(checkedInContent);
        assertEquals(SECOND_VERSION_CONTENT, checkedInContent);
    }

    /**
     * Tests the cancelCheckout service method
     * 
     * @throws Exception
     */
    public void testCancelCheckout() throws Exception
    {
        // Check out a node
        Reference workingCopy = doCheckOut();
        
        // Cancel the check out
        Predicate predicate = convertToPredicate(workingCopy);
        CancelCheckoutResult result = this.authoringService.cancelCheckout(predicate);
        
        // Check the result
        assertNotNull(result);
        assertEquals(1, result.getOriginals().length);
        // TODO check that state of the orig and that the working copy has been deleted
        
        // TODO I don't think that the working copies should be returned in the result since they have been deleted !!
    }

    /**
     * Tests the lock service methods, lock, unlock and getStaus
     * 
     * @throws Exception
     */
    public void testLockUnLockGetStatus() throws Exception
    {
        Reference reference = createContentAtRoot("lock_test1.txt", INITIAL_VERSION_CONTENT);        
        Predicate predicate = convertToPredicate(reference);
        
        // Get the status 
        checkLockStatus(predicate, null, null);
        
        // Lock with a write lock
        Reference[] lockedRefs = this.authoringService.lock(predicate, false, LockTypeEnum.write);
        assertNotNull(lockedRefs);
        assertEquals(1, lockedRefs.length);        
        // TODO check in more detail
        
        // Get the status
        checkLockStatus(predicate, USERNAME, LockTypeEnum.write);
        
        // Unlock (bad)
//        try
//        {
//            this.authoringService.unlock(predicate, "bad", false);
//            fail("This should have thrown an exception.");
//        }
//        catch (Throwable exception)
//        {
//            // Good .. we where expceting this
//        }
        
        // Unlock (good)
        Reference[] unlocked = this.authoringService.unlock(predicate, false);
        assertNotNull(unlocked);
        assertEquals(1, unlocked.length);
        
        // Get the status
        checkLockStatus(predicate, null, null);
        
        // Read lock
        Reference[] lockedRefs2 = this.authoringService.lock(predicate, false, LockTypeEnum.read);
        
        assertNotNull(lockedRefs2);
        assertEquals(1, lockedRefs2.length);
        // TODO check in more detail
        
        // Get the status
        checkLockStatus(predicate, USERNAME, LockTypeEnum.read);
    }
    
    private void checkLockStatus(Predicate predicate, String lockOwner, LockTypeEnum lockType)
        throws Exception
    {
        LockStatus[] lockStatus1 = this.authoringService.getLockStatus(predicate);
        assertNotNull(lockStatus1);
        assertEquals(1, lockStatus1.length);
        LockStatus ls1 = lockStatus1[0];
        assertNotNull(ls1);
        assertEquals(lockOwner, ls1.getLockOwner());
        assertEquals(lockType, ls1.getLockType());
    }

    /**
     * Tests the createVersion service method
     * 
     * @throws Exception
     */
    public void testVersionMethods() throws Exception
    {
        Reference reference = createContentAtRoot("create_version_test.txt", INITIAL_VERSION_CONTENT);        
        Predicate predicate = convertToPredicate(reference);
        
        // Get the version history (before its been versioned)
        VersionHistory emptyVersionHistory = this.authoringService.getVersionHistory(reference);
        assertNotNull(emptyVersionHistory);
        assertNull(emptyVersionHistory.getVersions());
        
        // Create the version
        VersionResult result = this.authoringService.createVersion(predicate, getVersionComments(), false);        
        assertNotNull(result);
        assertEquals(1, result.getNodes().length);
        assertEquals(1, result.getVersions().length);
        Version version = result.getVersions()[0];
        assertEquals("1.0", version.getLabel());
        // TODO check commentaries
        // TODO check creator
        
        // Get the version history
        VersionHistory versionHistory = this.authoringService.getVersionHistory(reference);
        assertNotNull(versionHistory);
        assertEquals(2, versionHistory.getVersions().length);
        // TODO some more tests ...
        
        // Update the content
        this.contentService.write(reference, ContentModel.PROP_CONTENT.toString(), SECOND_VERSION_CONTENT.getBytes(), null);
        
        // Create another version
        VersionResult versionResult2 = this.authoringService.createVersion(predicate, getVersionComments(), false);
        assertNotNull(versionResult2);
        assertEquals(1, versionResult2.getNodes().length);
        assertEquals(1, versionResult2.getVersions().length);
        Version version2 = versionResult2.getVersions()[0];
        assertEquals("1.3", version2.getLabel());
        // TODO check commentaries
        // TODO check creator
        
        // Check the version history
        VersionHistory versionHistory2 = this.authoringService.getVersionHistory(reference);
        assertNotNull(versionHistory2);
        assertEquals(4, versionHistory2.getVersions().length);
        // TODO some more tests ...
        
        // Confirm the current content of the node
        Content[] contents = this.contentService.read(new Predicate(new Reference[]{reference}, getStore(), null), ContentModel.PROP_CONTENT.toString());
        Content readResult1 = contents[0];
        String content1 = getContentAsString(readResult1.getUrl());
        assertEquals(SECOND_VERSION_CONTENT, content1);
        
        // Revert the node to the first version
        this.authoringService.revertVersion(reference, "1.0");
        
        // Confirm that the state of the node has been reverted
        Content[] contents2 = this.contentService.read(new Predicate(new Reference[]{reference}, getStore(), null), ContentModel.PROP_CONTENT.toString());
        Content readResult2 = contents2[0];
        String content2 = getContentAsString(readResult2.getUrl());
        assertEquals(INITIAL_VERSION_CONTENT, content2);
        
        // Now delete the version history
        VersionHistory deletedVersionHistory = this.authoringService.deleteAllVersions(reference);
        assertNotNull(deletedVersionHistory);
        assertNull(deletedVersionHistory.getVersions());
        
        // Check the version history
        VersionHistory versionHistory3 = this.authoringService.getVersionHistory(reference);
        assertNotNull(versionHistory3);
        assertNull(versionHistory3.getVersions());        
    }
}
