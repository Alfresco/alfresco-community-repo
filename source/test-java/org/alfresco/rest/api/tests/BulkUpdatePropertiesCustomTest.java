package org.alfresco.rest.api.tests;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.INFO;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Class to test if bulk properties update works for the
 * description(cmis:description) property
 * 
 * This test checks the fix from MNT-16376 is valid but only if the change in
 * MNT-13670 is not present in the code base. The fix from MNT-16376 ensures
 * that the correct call context is available to the working threads that
 * process bulk update. The fix from MNT-13670 added the description to the CMIS
 * version 1.0 mapping in RuntimePropertyAccessorMapping and that masks the
 * problem of the missing call context of the bulk update working threads for
 * the description property.
 * 
 * Currently all of the properties in the RuntimePropertyAccessorMapping are
 * added to the CMIS version 1.0, except for isSecondaryTypesProperty and
 * IsPrivateWorkingCopy. I am not sure if/how and what would be the consequences
 * if we update in the bulk update operation the two properties that are
 * missing for version CMIS 1.0.
 *
 */
public class BulkUpdatePropertiesCustomTest extends AbstractSessionTest
{
    private static final String CONTENT = "Custom Bluk update test content.";
    private static final String NEW_DESCRIPTION_VALUE = "new description value";

    @Override
    public void init(Map<String, String> parameters)
    {
        super.init(parameters);
        setName("Custom Bulk Update Properties Test");
        setDescription("Creates a few folders and documents,bulk update the description and check that it has been updated, and deletes all created objects.");
    }

    @Override
    public void run(Session session)
    {
        if (session.getRepositoryInfo().getCmisVersion() == CmisVersion.CMIS_1_0)
        {
            addResult(createResult(SKIPPED, "Bulk Update Properties is not supported by CMIS 1.0. Test skipped!"));
            return;
        }

        CmisTestResult failure = null;
        int numOfObjects = 25;

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try
        {
            Map<String, Folder> folders = new HashMap<String, Folder>();
            Map<String, Document> documents = new HashMap<String, Document>();

            // create folders and documents
            for (int i = 0; i < numOfObjects; i++)
            {
                Folder newFolder = createFolder(session, testFolder, "bufolder" + i);
                folders.put(newFolder.getId(), newFolder);
                Document newDocument = createDocument(session, newFolder, "budoc" + i + ".txt", CONTENT);
                documents.put(newDocument.getId(), newDocument);
            }

            // update cmis:description of all the documents
            List<CmisObject> objects = new ArrayList<CmisObject>(documents.values());
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(PropertyIds.DESCRIPTION, NEW_DESCRIPTION_VALUE);

            List<BulkUpdateObjectIdAndChangeToken> updatedIds = session.bulkUpdateProperties(objects, properties, null, null);

            // check the result
            if (getBinding() == BindingType.WEBSERVICES)
            {
                addResult(createResult(INFO, "The Web Services binding does not return the updated ids."
                        + " This issue has to be clarified by the CMIS TC and the test to adopted later."));
            }
            else
            {
                if (updatedIds == null || updatedIds.isEmpty())
                {
                    addResult(createResult(FAILURE, "Bulk Update Properties did not update any documents!"));
                }
                else
                {
                    failure = createResult(FAILURE, "Bulk Update Properties did not update all test documents!");
                    addResult(assertEquals(documents.size(), updatedIds.size(), null, failure));
                }
            }

            // check all documents
            for (Folder folder : folders.values())
            {
                List<CmisObject> children = new ArrayList<CmisObject>();
                for (CmisObject child : folder.getChildren(SELECT_ALL_NO_CACHE_OC))
                {
                    children.add(child);
                }

                if (children.size() != 1)
                {
                    String errorMessage = "Test folder should have exactly one child, but it has " + children.size() + "!";
                    addResult(createResult(FAILURE, errorMessage));
                }
                else
                {
                    failure = createResult(FAILURE, "Document does not have the new description! Id: " + children.get(0).getId());
                    addResult(assertEquals(NEW_DESCRIPTION_VALUE, children.get(0).getDescription(), null, failure));
                }
            }

            // delete folders and documents
            for (Folder folder : folders.values())
            {
                folder.deleteTree(true, null, true);
            }
        }
        finally
        {
            // delete the test folder
            deleteTestFolder();
        }

    }
}