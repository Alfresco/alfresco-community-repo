function testTransferService()
{

   test.assertNotNull(transfer, "RootScoped object not found :transfer: ");
            
   test.assertNotNull(testNode, "RootScoped object not found :testNode: ");
   
   var testNode = "foo://123/123";
   
   // First transfer - with a rubbish target
   try
   {
      var ret = transfer.transfer("exception", testNode);
      test.fail("should have thrown exception.")
   }
   catch (e)
   {
      // expect to go here.
   }

   // second transfer - with a good target name
   {
      var ret = transfer.transfer("good", testNode);
   }
   
   // third transfer - with an array of strings
   {
      var testNodes=new Array(); // regular array (add an optional integer
      testNodes[0]="foo://123/1";    
      testNodes[1]="foo://123/2";
      testNodes[2]="foo://123/3";
      var ret = transfer.transfer("good", testNodes);
   }
   
   // Test remove - one node
   {
      var report = transfer.remove("good", testNode);
   }   
   
   // Test remove - array of strings
   {
      var testNodes=new Array();
      testNodes[0]="foo://123/1";    
      testNodes[1]="foo://123/2";
      testNodes[2]="foo://123/3";
      var report = transfer.remove("good", testNodes);
   }
   
   // Test getTransferTarget
   {
      var ret = transfer.getTransferTarget("good");
      test.assertNotNull(ret, "return from getTransferTarget is null");
      test.assertNotNull(ret.name, "target name is null");
      test.assertNotNull(ret.noderef, "node ref is null");
   }
   
   // Test getTransferTargets
   {
      var ret = transfer.getAllTransferTargets();
      test.assertNotNull(ret, "return from getTransferTargets is null");
      //test.assertNotNull(ret.name, "target name is null");
      //test.assertNotNull(ret.noderef, "node ref is null");
      for(x in ret)
      {
         test.assertNotNull(x.name, "target name is null");
         test.assertNotNull(x.noderef, "node ref is null");
      }
   }
   
   // Test getTransferTargets
   {
      var ret = transfer.getAllTransferTargets();
      test.assertNotNull(ret, "return from getAllTransferTargets is null");
      
      for(x in ret)
      {
         test.assertNotNull(x.name, "target name is null");
         test.assertNotNull(x.noderef, "node ref is null");
      }

   }
   
   // Test getTransferTargets
   {
      var ret = transfer.getTransferTargetsByGroup("good");
      test.assertNotNull(ret, "return from getTransferTargetsByGroup is null");
      //test.assertNotNull(ret.name, "target name is null");
      //test.assertNotNull(ret.noderef, "node ref is null");
   }
}

// Execute test's
testTransferService();