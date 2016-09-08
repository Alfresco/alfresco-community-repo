function main()
{
    test.assertNotNull(filePlan);
    test.assertNotNull(record);
    
    var rmNode = rmService.getRecordsManagementNode(record);
    test.assertNotNull(rmNode);
    
    var capabilities = rmNode.capabilities;
    var countCheck = capabilities.length != 0;
    test.assertTrue(countCheck);
    
    var capability = capabilities[0];
    test.assertNotNull(capability);
    test.assertNotNull(capability.name);    
}

main();