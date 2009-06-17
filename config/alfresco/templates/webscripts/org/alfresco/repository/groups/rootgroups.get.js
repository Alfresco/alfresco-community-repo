/**
 * List/Search root groups
 */ 

function main ()
{
   // Get the args
   var shortNameFilter = args["shortNameFilter"];
   var includeInternalStr = args["includeInternal"];
   
   if (shortNameFilter == null)
   {
      shortNameFilter = "";
   }
   
   var includeInternal = includeInternalStr == "true" ? true : false;
      
   // Do the search
   model.groups = groups.searchRootGroups(shortNameFilter, includeInternal);
}

main();