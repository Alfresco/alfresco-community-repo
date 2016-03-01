 
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.forms.RecordsManagementNodeFormFilter;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * Test for RM-1727
 *
 * @author Tatsiana Shalima
 * @since 2.3
 */
public class RM1727Test extends BaseRMTestCase
{
    private String myUser;
    private NodeRef folder;
    private NodeRef record;

    private RecordsManagementNodeFormFilter nodeFormFilter;
    private Form form;

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        nodeFormFilter = (RecordsManagementNodeFormFilter)applicationContext.getBean("rmNodeFormFilter");
        //create user
        myUser = GUID.generate();
        createPerson(myUser);
        //give user RM Manager role
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, myUser);
        //create category > folder > record
        NodeRef category = filePlanService.createRecordCategory(filePlan, GUID.generate());
        folder = recordFolderService.createRecordFolder(category, GUID.generate());
        record = recordService.createRecordFromContent(folder, GUID.generate(), TYPE_CONTENT, null, null);
    }

    public void testRM1727()
    {
        //set read and file permissions for folder
        filePlanPermissionService.setPermission(folder, myUser, RMPermissionModel.FILING);
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Item item = new Item("node",folder.toString());
                item.setType("rma:recordFolder");
                form = new Form(item);
                nodeFormFilter.afterGenerate(folder, null, null, form, null);
                return null;
            }
        }, myUser);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Item item = new Item("node",record.toString());
                item.setType("rma:record");
                form = new Form(item);
                nodeFormFilter.afterGenerate(record, null, null, form, null);
                return null;
            }
        }, myUser);
    }
}
