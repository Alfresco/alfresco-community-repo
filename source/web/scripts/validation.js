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
 * Ensures the value of the 'control' is a number.
 *
 * @return true if the value is a number
 */
function validateIsNumber(control, message, showMessage)
{
   var result = true;
   
   if (isNaN(control.value))
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
   var result = true;
   var pattern = /([\"\*\\\>\<\?\/\:\|]+)|([ ]+$)|([\.]?[\.]+$)/;
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
      var message = (window.gecko) ? $("dialog:dialog-body:validation_invalid_character").textContent : $("dialog:dialog-body:validation_invalid_character").innerText;
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
      var message = (window.gecko) ? $("wizard:wizard-body:validation_invalid_character").textContent : $("wizard:wizard-body:validation_invalid_character").innerText;
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