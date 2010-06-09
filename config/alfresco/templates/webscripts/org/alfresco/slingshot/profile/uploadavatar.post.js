/**
 * User Profile Avatar Upload method
 * 
 * @method POST
 * @param username {string}
 *        filedata {file}
 */

function main()
{
   try
   {
      var filename = null;
      var content = null;
      var username = null;

      // locate file attributes
      for each (field in formdata.fields)
      {
         if (field.name == "filedata" && field.isFile)
         {
            filename = field.filename;
            content = field.content;
         }
         else if (field.name == "username")
         {
            username = field.value;
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
      if (username == null || username.length == 0)
      {
         status.code = 500;
         status.message = "Username parameter not supplied.";
         status.redirect = true;
         return;
      }

      var user = people.getPerson(username);
      // ensure we found a valid user and that it is the current user or we are an admin
      if (user == null ||
          (people.isAdmin(person) == false && user.properties.userName != person.properties.userName))
      {
         status.code = 500;
         status.message = "Failed to locate user to modify or permission denied.";
         status.redirect = true;
         return;
      }

      // ensure cm:person has 'cm:preferences' aspect applied - as we want to add the avatar as
      // the child node of the 'cm:preferenceImage' association
      if (!user.hasAspect("cm:preferences"))
      {
         user.addAspect("cm:preferences");
      }

      // remove old image child node if we already have one
      var assocs = user.childAssocs["cm:preferenceImage"];
      if (assocs != null && assocs.length == 1)
      {
         assocs[0].remove();
      }

      // create the new image node
      var image = user.createNode(filename, "cm:content", "cm:preferenceImage");
      image.properties.content.write(content);
      image.properties.content.guessMimetype(filename);
      image.properties.content.encoding = "UTF-8";
      image.save();

      // wire up 'cm:avatar' target association - backward compatible with JSF web-client avatar
      assocs = user.associations["cm:avatar"];
      if (assocs != null && assocs.length == 1)
      {
         user.removeAssociation(assocs[0], "cm:avatar");
      }
      user.createAssociation(image, "cm:avatar");

      // save ref to be returned
      model.image = image;
   }
   catch (e)
   {
      var x = e;
      status.code = 500;
      status.message = "Unexpected error occured during upload of new content.";
      if(x.message && x.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
      {
         status.code = 413;
         status.message = x.message;
      }
      status.redirect = true;
      return;
   }
}

main();