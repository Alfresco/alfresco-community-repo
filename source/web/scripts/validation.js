//
// Validation functions
// Gavin Cornwell 30-11-2005
//

/**
 * Informs the user of the given 'message', if 'showMessage' is true.
 */
function informUser(message, showMessage)
{
	if (showMessage)
   {
      alert(message);
   }
}

/**
 * Ensures the 'value' is not null or 0.
 *
 * @return true if the mandatory validation passed
 */
function validateMandatory(value, message, showMessage)
{
   var result = true;
   
   if (value == null || value.length == 0)
   {
      informUser(message, showMessage);
      result = false;
   }
   
   return result;
}

/**
 * Ensures the 'value' is more than 'min' and less than 'max'.
 *
 * @return true if the number range validation passed
 */
function validateNumberRange(value, min, max, message, showMessage)
{
   var result = true;
   
   if (value < min || value > max)
   {
      informUser(message, showMessage);
      result = false;
   }
   
   return result;
}

/**
 * Ensures the 'value' has a string length more than 'min' and less than 'max'.
 *
 * @return true if the string length validation passed
 */
function validateStringLength(value, min, max, message, showMessage)
{
   var result = true;
   
   if (value.length < min || value.length > max)
   {
      informUser(message, showMessage);
      result = false;
   }
   
   return result;
}

/**
 * Ensures the 'value' matches the 'expression' if 'requiresMatch' is true. 
 * Ensures the 'value' does not matche the 'expression' if 'requiresMatch' is false.
 * 
 * @return true if the regex validation passed
 */
function validateRegex(value, expression, requiresMatch, message, showMessage)
{
   var result = true;
   
   // TODO: implement the regular expression matching
   
   return result;
}

