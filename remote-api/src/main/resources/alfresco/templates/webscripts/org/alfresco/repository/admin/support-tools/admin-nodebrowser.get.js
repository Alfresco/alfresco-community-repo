<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

/**
 * Repository Admin Console
 * 
 * Node Browser GET method
 */

function main()
{
   // available repository stores
   model.stores = utils.getStores();
   
   // action persisted values
   model.action = args.action;
   model.actionValue = args.actionValue;
   
   // special case for "nodeRef" argument
   // support URL driven NodeRef argument to the Node Browser page e.g. /admin-nodebrowser?nodeRef=workspace://SpacesStore/...
   model.query = args.query;
   if (args.nodeRef)
   {
      model.query = args.nodeRef;
   }
   
   // result info from session
   if (args.resultId)
   {
      model.result = session.getValue(args.resultId);
   }
   
   // mandatory model values for UI
   model.attributes = [];
   model.tools = Admin.getConsoleTools("admin-nodebrowser");
   model.metadata = Admin.getServerMetaData();
}

main();