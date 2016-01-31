/**
 * Main entrypoint for script.
 *
 * @method main
 */
function main()
{
   // Log debug message
   logger.log("Record " + node.name + " has been superseded.  Sending notification");
   
   // Send notification
   rmService.sendSupersededNotification(node);
}

main();
