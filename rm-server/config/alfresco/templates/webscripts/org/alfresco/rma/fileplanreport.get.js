/**
 * Main entry point for this webscript.
 * Builds a nodeRef from the url and creates a records series, category and/or folder
 * template model depending on what nodeRef that has been given.
 *
 * @method main
 */
function main()
{
   // Get the node from the URL
   var pathSegments = url.match.split("/");
   var reference = [ url.templateArgs.store_type, url.templateArgs.store_id ].concat(url.templateArgs.id.split("/"));
   var node = search.findNode(pathSegments[2], reference);

   // 404 if the node is not found
   if (node == null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "The node could not be found");
      return;
   }

   // Get the record series, categories and/or folders
   if(node.type == "{http://www.alfresco.org/model/dod5015/1.0}filePlan")
   {
      var recordSeries = [],
         seriesNodes = node.children,
         seriesNode;
      for (var rsi = 0, rsl = seriesNodes.length; rsi < rsl; rsi++)
      {
         var seriesNode = seriesNodes[rsi];
         if(seriesNode.type == "{http://www.alfresco.org/model/dod5015/1.0}recordSeries")
         {
            recordSeries.push(getRecordSeries(seriesNode));
         }
      }
      recordSeries.sort(sortByName);
      model.recordSeries = recordSeries;
   }
   else if(node.type == "{http://www.alfresco.org/model/dod5015/1.0}recordSeries")
   {
      var recordSeries = [];
      recordSeries.push(getRecordSeries(node));
      model.recordSeries = recordSeries;
   }
   else if(node.type == "{http://www.alfresco.org/model/dod5015/1.0}recordCategory")
   {
      var recordCategories = [];
      recordCategories.push(getRecordCategory(node, "/" + node.parent.name + "/"));
      model.recordCategories = recordCategories;
   }
   else if(node.type == "{http://www.alfresco.org/model/recordsmanagement/1.0}recordFolder")
   {
      var recordFolders = [];
      var recordCategory = node.parent;
      recordFolders.push(getRecordFolder(node, "" + recordCategory.parent.name + "/" + recordCategory.name + "/"));
      model.recordFolders = recordFolders;
   }
}

/**
 * Sort helper function for objects with names
 *
 * @method sortByName
 * @param obj1
 * @param obj2
 */
function sortByName(obj1, obj2)
{
   return (obj1.name > obj2.name) ? 1 : (obj1.name < obj2.name) ? -1 : 0;
}

/**
 * Takes a ScriptNode and builds a Record Series template model from it
 *
 * @method getRecordSeries
 * @param seriesNode {ScriptNode} A ScriptNode of type "rma:recordSeries"
 */
function getRecordSeries(seriesNode)
{
	// Create Record Series object
	var recordSerie = {
      parentPath: "/",
      name: seriesNode.name,      
      identifier: seriesNode.properties["rma:identifier"],
      description: seriesNode.properties["description"]
   };

   // Find all Record Categories
	var recordCategories = [],
		categoryNodes = seriesNode.children,
		categoryNode;
	for (var rci = 0, rcl = categoryNodes.length; rci < rcl; rci++)
	{
      categoryNode = categoryNodes[rci];
      if(categoryNode.type == "{http://www.alfresco.org/model/dod5015/1.0}recordCategory")
      {
         // Create and add Record Category object
         recordCategories.push(getRecordCategory(categoryNode, "/" + seriesNode.name + "/"));
      }
   }
   recordCategories.sort(sortByName);
   recordSerie.recordCategories = recordCategories;

   // Return Record Series
   return recordSerie;
}

/**
 * Takes a ScriptNode and builds a Record Category template model from it
 *
 * @method getRecordCategory
 * @param categoryNode {ScriptNode} A ScriptNode of type "rma:recordCategory"
 * @param parentPath {string} The file path starting from the top of the fileplan
 */
function getRecordCategory(categoryNode, parentPath)
{
   // Create Record Category object
   var recordCategory = {
      parentPath: parentPath,
      name: categoryNode.name,
      identifier: categoryNode.properties["rma:identifier"],
      vitalRecordIndicator: categoryNode.properties["vitalRecordIndicator"],
      dispositionAuthority: categoryNode.properties["dispositionAuthority"], 
      recordFolders: [],
      dispositionActions: []
   };

   // Find all Record Folders & Disposition information
   var recordFolders = [],
		dispositionActions = [],
		categoryChildren = categoryNode.children,
		categoryChild,
		dispScheduleChildren,
      dispScheduleChild;
   for (var cci = 0, ccil = categoryChildren.length; cci < ccil; cci++)
   {
      categoryChild = categoryChildren[cci]
      if (categoryChild.type == "{http://www.alfresco.org/model/recordsmanagement/1.0}recordFolder")
      {
         // Create and add Record Folder object
         recordFolders.push(getRecordFolder(categoryChild, parentPath + categoryNode.name + "/"));
      }
      else if (categoryChild.type == "{http://www.alfresco.org/model/recordsmanagement/1.0}dispositionSchedule")
      {
         // Get Disposition authority
         recordCategory.dispositionAuthority = categoryChild.properties["rma:dispositionAuthority"];
         dispScheduleChildren = categoryChild.children;
         for (var dsi = 0, dsil = dispScheduleChildren.length; dsi < dsil; dsi++)
         {
            dispScheduleChild = dispScheduleChildren[dsi];
            if (dispScheduleChild.type == "{http://www.alfresco.org/model/recordsmanagement/1.0}dispositionActionDefinition")
            {
               // Add Disposition Action description
               dispositionActions.push({
                  dispositionDescription: dispScheduleChild.properties["rma:dispositionDescription"]
               });
            }
         }
      }
   }

   // Add Record Category to the list
   recordFolders.sort(sortByName);
   recordCategory.recordFolders = recordFolders;
   recordCategory.dispositionActions = dispositionActions;
   return recordCategory;
}

/**
 * Takes a ScriptNode and builds a Record Category template model from it
 *
 * @method getRecordFolder
 * @param recordFolder {ScriptNode} A ScriptNode of type "rma:recordrecordFolder"
 * @param parentPath {string} The file path starting from the top of the fileplan
 */
function getRecordFolder(recordFolder, parentPath)
{
	return {
		parentPath: parentPath,
      name: recordFolder.name,
		identifier: recordFolder.properties["rma:identifier"],
		vitalRecordIndicator: recordFolder.properties["vitalRecordIndicator"]
	};
}

// Start webscript
main();

