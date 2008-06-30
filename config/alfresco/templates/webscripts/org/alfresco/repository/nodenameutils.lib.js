/**
 * Returns a two digit string if it is a one-digit number
 */
function getTwoDigitNum(num) {
   if(num < 10)
   {
      return "0" + num;
   }
   else
   {
      return num;
   }
}

/**
 * Returns a unique name that can be used to create a
 * child of the given parentNode.
 *
 * The name will be of form prefix_yyyy-mm-dd_hh-mm["","_x"],
 * where x represents a number > 1 and is only added if a node
 * with the base name already exists
 */
function getUniqueChildName(parentNode, prefix)
{
   // we create a name looking like 
   var name = prefix + "-";
   var date = new Date();
   name += date.getFullYear() + "-";
   name += getTwoDigitNum(date.getMonth() + 1) + "-";
   name += getTwoDigitNum(date.getDate()) + "_";
   name += getTwoDigitNum(date.getHours());
   name += getTwoDigitNum(date.getMinutes());
   
   // check that no child for the given name exists
   var finalName = name;
   var count = 1;
   while (parentNode.childByNamePath(finalName) !== null)
   {
      count += 1;
      finalName = name + "_" + count;
   }
   return finalName;
}

