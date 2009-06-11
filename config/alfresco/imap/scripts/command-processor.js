<import resource="/Company Home/Data Dictionary/Scripts/command-utils.js">

function processCommand()
{
   var isEmailed = document.hasAspect("emailserver:emailed");

   logger.log("Command Processor: isEmailed=" + isEmailed);

   if (isEmailed)
   {
      // Delete email attachments
      var attachments = document.assocs["attachments"];
      if (attachments != null)
      {
         for (var i = 0; i < attachments.length; i++)
         {
            attachments[i].remove();
         }
      }

      var command = document.properties["cm:title"];
      logger.log("Command Processor: command=" + command);
      
      var parts = new Array();
      var str = command;
      var i = 0;
      while (true)
      {
         var index = str.indexOf("-");
         if (index == -1)
         {
            parts[i] = str;
            break;
         }
         parts[i] = str.substring(0, index);
         str = str.substr(index  + 1);
         i++;
      }

      

      // do-<commandName>-<arg1>-...-<argN>
      if (parts.length < 3 || parts[0].toLowerCase() != "do")
      {
         var message = "Unknown command: " + command;
         logger.log(message);
         createEmail(message, message, message);
         return;
      }
      
      var commandName = parts[1].toLowerCase();
      var commandFolder = space.childByNamePath(commandName);
      logger.log("Found '" + commandName + "' command folder: '" + commandFolder + "'");
      if (commandFolder == null)
      {
         var message = "Command Processor: wrong command=" + command;
         createEmail(message, message, message);
         logger.log(message);
         return;
      }
      
      document.move(commandFolder);

   }

}

processCommand();









