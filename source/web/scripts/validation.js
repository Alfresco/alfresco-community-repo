//
// Validation functions
// Gavin Cornwell 30-11-2005
//

/**
 * Informs the user of the given 'message', if 'showMessage' is true.
 * If 'showMessage' is true focus is given to the 'control'.
 */
function informUser(control, message, showMessage)
{
	if (showMessage)
   {
      alert(message);
      control.focus();
   }
}

/**
 * Ensures the value of the 'control' is not null or 0.
 *
 * @return true if the mandatory validation passed
 */
function validateMandatory(control, message, showMessage)
{
   var result = true;
   
   if (control.value == null || control.value.length == 0)
   {
      informUser(control, message, showMessage);
      result = false;
   }
   
   return result;
}

/**
 * Ensures the value of the 'control' is more than 'min' and less than 'max'.
 *
 * @return true if the number range validation passed
 */
function validateNumberRange(control, min, max, message, showMessage)
{
   var result = true;
   
   if (isNaN(control.value) || control.value < min || control.value > max)
   {
      informUser(control, message, showMessage);
      result = false;
   }
   
   return result;
}

/**
 * Ensures the value of the 'control' has a string length more than 'min' and less than 'max'.
 *
 * @return true if the string length validation passed
 */
function validateStringLength(control, min, max, message, showMessage)
{
   var result = true;
   
   if (control.value.length < min || control.value.length > max)
   {
      informUser(control, message, showMessage);
      result = false;
   }
   
   return result;
}

/**
 * Ensures the value of the 'control' matches the 'expression' if 'requiresMatch' is true. 
 * Ensures the value of the 'control' does not match the 'expression' if 'requiresMatch' is false.
 * 
 * @return true if the regex validation passed
 */
function validateRegex(control, expression, requiresMatch, matchMessage, noMatchMessage, showMessage)
{
   var result = true;
   
   /*
   var pattern = new RegExp(unescape(expression));
   var matches = pattern.test(control.value);
   
   if (matches != requiresMatch)
   {
      if (requiresMatch)
      {
         informUser(control, noMatchMessage, showMessage);
      }
      else
      {
         informUser(control, matchMessage, showMessage);
      }
      
      result = false;
   }
   */
   
   return result;
}

/**
 * Ensures the value of the 'control' does not contain any illegal characters.
 * 
 * @return true if the file name is valid
 */
function validateName(control, message, showMessage)
{
   var result = true;
   var pattern = /[\"\*\\\>\<\?\/\:\|\%\&\+\;\xA3\xAC]+/;
   
   var idx = control.value.search(pattern);
   if (idx != -1)
   {
      informUser(control, control.value.charAt(idx) + " " + message, showMessage);
      result = false;
   }
   
   return result;
}

