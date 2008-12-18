/**
 * Returns a two digit string if it is a one-digit number
 */
function getTwoDigitNum(num)
{
   if (num < 10)
   {
      return "0" + num;
   }
   return num;
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
   var date = new Date(), name = prefix + "-" + date.getTime();

   // check that no child for the given name exists
   var finalName = name + "_" + Math.floor(Math.random() * 1000), count = 0;
   while (parentNode.childByNamePath(finalName) !== null || count > 100)
   {
      finalName = name + "_" + Math.floor(Math.random() * 1000);
      ++count;
   }
   return finalName;
}

