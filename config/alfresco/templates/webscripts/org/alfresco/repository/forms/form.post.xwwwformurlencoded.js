function main()
{
   //FIXME URL-encoded post of forms data is not yet working.
   if (logger.isLoggingEnabled())
   {
	   logger.log("x-www-form-urlencoded request received for nodeRef");

	   logger.log("decodedparams: " + decodedparams);
   }

   model.message = "Successfully updated node " + "nodeRef";
}

main();
