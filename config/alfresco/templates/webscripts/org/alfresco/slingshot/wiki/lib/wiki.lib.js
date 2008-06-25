/**
 * A collection of general functions used across most wiki scripts.
 */

/* Format and return error object */
function jsonError(errorString)
{
   var obj =
   {
      "error": errorString
   };
   
   return obj;
}
