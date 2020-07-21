function main()
{
   // FIXME URL-encoded post of forms data is not yet working.
   if (logger.isLoggingEnabled())
   {
	   logger.log("x-www-form-urlencoded request received");

	   logger.log("decodedparams: " + decodedparams);
   }

   model.message = "Not implemented yet";
}

main();
