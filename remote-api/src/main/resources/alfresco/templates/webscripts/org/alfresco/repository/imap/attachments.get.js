function main()
{
   var attachmentsAssocs = [];

   if (args["nodeRef"] != null)
   {
      var nodeRef = args["nodeRef"],
         node = search.findNode(nodeRef);	

      if (node != null)
      {
         var assocs = node.associations;
         for (var assocName in assocs)
         {
            if(assocName.equalsIgnoreCase("{http://www.alfresco.org/model/imap/1.0}attachment"))
            {
               var associations = assocs[assocName];
               for (var j = 0, jj = associations.length; j < jj; j++)
               {
             
                  attachmentsAssocs.push(
                  {
                     nodeRef: associations[j].nodeRef.toString(),
                     name: associations[j].name,
                     assocname: assocName
                  }
                  );
               }
            }
         } 
       }
    }
    
    model.attachmentsAssocs = attachmentsAssocs;
}

main();