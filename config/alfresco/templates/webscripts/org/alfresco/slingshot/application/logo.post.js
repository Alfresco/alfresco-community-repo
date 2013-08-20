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
      
      var logoConfig = new XML(config.script);
      var widthxheight = logoConfig.width + "x" + logoConfig.height;
      
      var transformationOptions = "-resize " + widthxheight + " -background none -gravity center -extent " + widthxheight;
      
      // create the new image node
      var nodeName = new Date().getTime() + "_" + filename;
      var tmpFolder = sitesNode.createFolder(nodeName + "_tmp");
      logoNode = sitesNode.createNode(nodeName, "cm:content");
      logoNode.properties.content.write(content);
      logoNode.properties.content.guessMimetype(filename);
      var resizedImage = logoNode.transformImage(logoNode.properties.content.mimetype, transformationOptions, tmpFolder);
      logoNode.properties.content.write(resizedImage.properties.content);
      // CLOUD-951, no need to delete the resizedImage, as removing the tmpFolder will remove resizedImage too.
      tmpFolder.remove();
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
