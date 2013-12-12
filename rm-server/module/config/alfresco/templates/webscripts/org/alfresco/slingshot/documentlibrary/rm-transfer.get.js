function main()
{
   var nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id,
      transfer = search.findNode(nodeRef);

   if (transfer === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
      return null;
   }

   if (String(transfer.typeShort) != "rma:transfer")
   {
      status.setCode(status.STATUS_BAD_REQUEST, "nodeRef: '" + nodeRef + "' is not of type 'rma:transfer'");
      return null;
   }
   
   model.transfer = transfer;
}

main();