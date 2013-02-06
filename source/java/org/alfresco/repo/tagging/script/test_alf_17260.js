for (var j = 0; j < 10 ; j ++)
{
   var test1 = folder.createFolder("Test1");
   var test2 = folder.createFolder("Test2");
   
   for(var i=0;i<102;i++)
   {
      var node = test1.createNode(j + "_test_"+i+".text","cm:content");
      node.addTag(j + "testTag" + i);
   }
	  
   for each (child in test1.children){
      child.clearTags();
   }
	  
   test1.move(test2);
   if (j < 9)
   {
      test2.remove();
   }
}