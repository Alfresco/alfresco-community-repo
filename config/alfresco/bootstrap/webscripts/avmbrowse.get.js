script:
{
    // extract avm store id and path
    var fullpath = url.extension.split("/");
    if (fullpath.length == 0)
    {
      status.code = 400;
      status.message = "Store id has not been provided.";
      status.redirect = true;
      break script;
    }
    var storeid = fullpath[0];
    var path = (fullpath.length == 1 ? "/" : "/" + fullpath.slice(1).join("/"));
    
    // locate avm node from path
    var store = avm.lookupStore(storeid);
    if (store == undefined)
    {
      status.code = 404;
      status.message = "Store " + storeid + " not found.";
      status.redirect = true;
      break script;
    }
    var node = avm.lookupNode(storeid + ":" + path);
    if (node == undefined)
    {
      status.code = 404;
      status.message = "Path " + path + " within store " + storeid + " not found.";
      status.redirect = true;
      break script;
    }
    
    // setup model for templates
    model.store = store;
    model.folder = node;    
}