/**
 * Application Log Upload method
 * 
 * @method POST
 * @param filedata {file}
 */

function main()
{
   try
   {
      var filename = null;
      var content = null;
      
      // locate file attributes
      for each (field in formdata.fields)
      {
         if (field.name == "filedata" && field.isFile)
         {
            filename = field.filename;
            content = field.content;
            break;
         }
      }
      
      // ensure all mandatory attributes have been located
      if (filename == undefined || content == undefined)
      {
         status.code = 400;
         status.message = "Uploaded file cannot be located in request";
         status.redirect = true;
         return;
      }
      
      var sitesNode = companyhome.childrenByXPath("st:sites")[0];
      if (sitesNode == null)
      {
         status.code = 500;
         status.message = "Failed to locate Sites folder.";
         status.redirect = true;
         return;
      }
      
      // create the new image node
      logoNode = sitesNode.createNode(new Date().getTime() + "_" + filename, "cm:content");
      logoNode.properties.content.write(content);
      logoNode.properties.content.guessMimetype(filename);
      logoNode.save();
      
      // save ref to be returned
      model.logo = logoNode;
      model.name = filename;
   }
   catch (e)
   {
      var x = e;
      status.code = 500;
      status.message = "Unexpected error occured during upload of new content.";
      if (x.message && x.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
      {
         status.code = 413;
         status.message = x.message;
      }
      status.redirect = true;
      return;
   }
}

main();