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
      if (control.type != "hidden") control.focus();
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
 * Ensures the value of the 'control' is a number.
 *
 * @return true if the value is a number
 */
function validateIsNumber(control, message, showMessage)
{
   var result = true,
      testValue = control.value;

   // Be tolerant of numbers that contain decimal commas and/or use a dot/apostrophe/space as a thousand separator & ignore.
   testValue = testValue.toString().replace(/[ '.,]/g, "");

   if (isNaN(testValue))
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
function validateMultivalueRegex(control, expression, requiresMatch, matchMessage, noMatchMessage, showMessage)
{
   var result = true;
   var pattern = new RegExp(decode(expression));
   
   var arrayOfStrings = control.value.substring(1, control.value.length - 1).split(", ");
   for (var i=0; i < arrayOfStrings.length; i++)
   {
       var matches = pattern.test(arrayOfStrings[i]);
       if (matches != requiresMatch)
       {
           if (requiresMatch)
           {
               informUser(control, noMatchMessage, showMessage);
               return false;
           }
           else
           {
               informUser(control, matchMessage, showMessage);
               return false;
           }
      }
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
   
   var pattern = new RegExp(decode(expression));
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
   
   return result;
}

/**
 * Ensures the value of the 'control' does not contain any illegal characters.
 * 
 * @return true if the file name is valid
 */
function validateName(control, message, showMessage)
{
   var pattern = /([\"\*\\\>\<\?\/\:\|]+)|([ ]+$)|([\.]?[\.]+$)/;
   return validateValue(control, pattern, message, showMessage);
}

/**
 * Ensures the user name value does not contain any illegal characters while user creating.
 * 
 * @return true if the user name is valid
 */
function validateUserNameForCreate(control, message, showMessage)
{
   var pattern = /([\"\*\\\>\<\?\:\|]+)|([ ]+$)|([\.]?[\.]+$)/;
   return validateValue(control, pattern, message, showMessage);
}

/**
 * Ensures the user name value does not contain any illegal characters while login.
 * 
 * @return true if the user name is valid
 */
function validateUserNameForLogin(control, message, showMessage)
{
   var pattern = /([\"\*\>\<\?\:\|]+)|([ ]+$)|([\.]?[\.]+$)/;
   return validateValue(control, pattern, message, showMessage);
}

/**
 * Ensures the value of the 'control' coresponds to required pattern.
 * 
 * @return true if the file name is valid
 */
function validateValue(control, pattern, message, showMessage)
{
   var result = true;
   var trimed = control.value.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
   var idx = trimed.search(pattern);
   if (idx != -1)
   {
      informUser(control, "'" + trimed.charAt(idx) + "' " + message, showMessage);
      result = false;
   }
   
   return result;
}

function validateDialog()
{
   if (finishButtonPressed)
   {
      finishButtonPressed = false;
      var message = $("dialog:dialog-body:validation_invalid_character").textContent ? $("dialog:dialog-body:validation_invalid_character").textContent : $("dialog:dialog-body:validation_invalid_character").innerText;
      return validateName($("dialog:dialog-body:name"), message, true);
   }
   else
   {
      return true;
   }
}

function validateWizard()
{
   if (finishButtonPressed)
   {
      finishButtonPressed = false;
      var message = $("wizard:wizard-body:validation_invalid_character").textContent ? $("wizard:wizard-body:validation_invalid_character").textContent : $("wizard:wizard-body:validation_invalid_character").innerText;
      return validateName($("wizard:wizard-body:name"), message, true);
   }
   else
   {
      return true;
   }
}

/**
 * Decodes the given string
 * 
 * @param str The string to decode
 * @return The decoded string
 */
function decode(str)
{
    var s0, i, j, s, ss, u, n, f;
    
    s0 = "";                // decoded str

    for (i = 0; i < str.length; i++)
    {   
        // scan the source str
        s = str.charAt(i);

        if (s == "+") 
        {
            // "+" should be changed to SP
            s0 += " ";
        }       
        else 
        {
            if (s != "%") 
            {
                // add an unescaped char
                s0 += s;
            }     
            else
            {               
                // escape sequence decoding
                u = 0;          // unicode of the character

                f = 1;          // escape flag, zero means end of this sequence

                while (true) 
                {
                    ss = "";        // local str to parse as int
                    for (j = 0; j < 2; j++ ) 
                    {  
                        // get two maximum hex characters for parse
                        sss = str.charAt(++i);

                        if (((sss >= "0") && (sss <= "9")) || ((sss >= "a") && (sss <= "f"))  || ((sss >= "A") && (sss <= "F"))) 
                        {
                            ss += sss;      // if hex, add the hex character
                        } 
                        else 
                        {
                            // not a hex char., exit the loop
                            --i; 
                            break;
                        }    
                    }

                    // parse the hex str as byte
                    n = parseInt(ss, 16);

                    // single byte format
                    if (n <= 0x7f) { u = n; f = 1; }

                    // double byte format
                    if ((n >= 0xc0) && (n <= 0xdf)) { u = n & 0x1f; f = 2; }

                    // triple byte format
                    if ((n >= 0xe0) && (n <= 0xef)) { u = n & 0x0f; f = 3; }

                    // quaternary byte format (extended)
                    if ((n >= 0xf0) && (n <= 0xf7)) { u = n & 0x07; f = 4; }

                    // not a first, shift and add 6 lower bits
                    if ((n >= 0x80) && (n <= 0xbf)) { u = (u << 6) + (n & 0x3f); --f; }

                    // end of the utf byte sequence
                    if (f <= 1) { break; }         

                    if (str.charAt(i + 1) == "%") 
                    { 
                        // test for the next shift byte
                        i++ ; 
                    }                   
                    else 
                    {
                        // abnormal, format error
                        break;
                    }                   
                }

                // add the escaped character
                s0 += String.fromCharCode(u);

            }
        }
    }

    return s0;
}

/**
 * This function validates Output Path Pattern parameter for Create Web Site and
 * Create Form Wizards
 * 
 * @param disablingElement -
 *            some input control for disabling if Pattern parameter is not valid
 * @param outputPathInput -
 *            input that contains Pattern parameter
 * @param additionalConditionInput -
 *            input that contains additional parameter for enabling
 *            disablingElement. If this parameter is 'null' then
 *            disablingElement will be enabled
 */
function validateOutputPathPattern(disabledElement, outputPathInput, additionalConditionInput)
{
   var path = (null != outputPathInput) ? (outputPathInput.value) : (null);
   var pattern = new RegExp("^([\\s\u0020]*)([^\\s\\u0020]+)([^\\0]*)$", "");
   if ((null == path) || ("" == path) || !pattern.test(path))
   {
      disabledElement.disabled = true;
   }
   else
   {
      value = (null != additionalConditionInput) ? (additionalConditionInput.value) : (null);
      disabledElement.disabled = (null != value) ? (("" == value.trim()) || !pattern.test(value)) : (false);
   }
}

/**
 *  Function executes after page load.
 *  It checks an existence of buttons with specified ids in all forms of the page.
 *  If a button exists the script will cache it.
 */
function getDisableButtons()
{
   // 'test-button' and 'submit-button' are examples.
   var buttonIds = ['finish-button', 'ok-button' ,'test-button', 'submit-button', 'cancel-button'];
   // create button cache
   window.buttonsToDisable = new Array();
   for (var i=0; i<document.forms.length; i++)
   {
      var form = document.forms.item(i);
      var formId = form.attributes.getNamedItem('id');
      // form without id being ignored
      if (formId != undefined)
      {
         // assign a submit handler (instead of addEvent) to handle function return value.
         // it is necessary to handle validate function result.
         form.onsubmit = formSubmit;
         for (var j=0; j<buttonIds.length; j++)
         {
            // construct a possible button id
            var buttonId = new String(formId.nodeValue + ':' + buttonIds[j]);
            // check button if exists
            if (document.getElementById(buttonId) != undefined)
            {
               // cache its name
               window.buttonsToDisable[window.buttonsToDisable.length] = buttonId;
            }
         }
       }
    }
 }
 
 /**
 *  General onsubmit handler. It disables all buttons that have been cached in pageLoaded.
 * 
 */
function formSubmit()
{
   // this function will be delayed to enable return from the function
   var disable = function()
   {
      for(var i=0; i<window.buttonsToDisable.length; i++)
      {
         var element = document.getElementById(window.buttonsToDisable[i]);
         // just paranoid
         if (element != undefined)
         {
            element.disabled = true;
         }
      }
   }

   // call a validate function if one exists.
   // if not 'validate' function provided it returns true and disables buttons

   if (typeof validate == 'function')
   {
      if (validate() == true)
      {
         disable.delay(5, this);
         return true;
      }
      else
      {
         return false; 
      }
   }
   else
   {
      disable.delay(5, this);
      return true;
   }
}

/**
 *  Helper function to attache an event to element instead of element.onload = func.
 *  It helps to resolve several window.onload attaches.
 */
function addEventToElement(element, type, func, useCapture)
{
   if (element.addEventListener)
   {
      element.addEventListener(type, func, useCapture);
      return true;
   }
   else if (element.attachEvent)
   {
      var result = element.attachEvent('on' + type, func);
      return result;
   }
   
   return false;
}

// add an onload event instead of window.onload attach...
// because a last onload attach disables all previous...
addEventToElement(window, 'load', getDisableButtons, false);