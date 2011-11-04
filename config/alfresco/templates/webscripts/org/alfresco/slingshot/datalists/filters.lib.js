/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

var Filters =
{
   /**
    * Types that we want to suppress from the resultset
    */
   IGNORED_TYPES:
   [
      "cm:systemfolder",
      "fm:forums",
      "fm:forum",
      "fm:topic",
      "fm:post"
   ],

   /**
    * Create filter parameters based on input parameters
    *
    * @method getFilterParams
    * @param filter {string} Required filter
    * @param parsedArgs {object} Parsed arguments object literal
    * @return {object} Object literal containing parameters to be used in Lucene search
    */
   getFilterParams: function Filter_getFilterParams(filter, parsedArgs)
   {
      var filterParams =
      {
         query: "+PARENT:\"" + parsedArgs.nodeRef + "\" ",
         limitResults: null,
         sort: [
         {
            column: "@cm:name",
            ascending: true
         }],
         language: "lucene",
         templates: null
      };

      // Max returned results specified?
      var argMax = args.max;
      if ((argMax !== null) && !isNaN(argMax))
      {
         filterParams.limitResults = argMax;
      }

      // Create query based on passed-in arguments
      var filterData = String(filter.filterData || ""),
         filterQuery = filterParams.query;

      // Common types and aspects to filter from the UI
      var filterQueryDefaults = ' -TYPE:"' + Filters.IGNORED_TYPES.join('" -TYPE:"') + '"';

      switch (String(filter.filterId))
      {
         case "recentlyAdded":
         case "recentlyModified":
         case "recentlyCreatedByMe":
         case "recentlyModifiedByMe":
            var onlySelf = (filter.filterId.indexOf("ByMe")) > 0 ? true : false,
               dateField = (filter.filterId.indexOf("Modified") > 0) ? "modified" : "created",
               ownerField = (dateField == "created") ? "creator" : "modifier";

            // Default to 7 days - can be overridden using "days" argument
            var dayCount = 7,
               argDays = args.days;
            if ((argDays !== null) && !isNaN(argDays))
            {
               dayCount = argDays;
            }

            // Default limit to 50 documents - can be overridden using "max" argument
            if (filterParams.limitResults === null)
            {
               filterParams.limitResults = 50;
            }

            var date = new Date();
            var toQuery = date.getFullYear() + "\\-" + (date.getMonth() + 1) + "\\-" + date.getDate();
            date.setDate(date.getDate() - dayCount);
            var fromQuery = date.getFullYear() + "\\-" + (date.getMonth() + 1) + "\\-" + date.getDate();

            filterQuery = "+PARENT:\"" + parsedArgs.nodeRef;
            if (parsedArgs.nodeRef == "alfresco://sites/home")
            {
               // Special case for "Sites home" pseudo-nodeRef
               filterQuery += "/*/cm:dataLists";
            }
            filterQuery += "\"";
            filterQuery += " +@cm\\:" + dateField + ":[" + fromQuery + "T00\\:00\\:00.000 TO " + toQuery + "T23\\:59\\:59.999]";
            if (onlySelf)
            {
               filterQuery += " +@cm\\:" + ownerField + ":\"" + person.properties.userName + '"';
            }
            filterQuery += " -TYPE:\"folder\"";

            filterParams.sort = [
            {
               column: "@cm:" + dateField,
               ascending: false
            }];
            filterParams.query = filterQuery + filterQueryDefaults;
            break;

         case "createdByMe":
            // Default limit to 50 documents - can be overridden using "max" argument
            if (filterParams.limitResults === null)
            {
               filterParams.limitResults = 50;
            }

            filterQuery = "+PARENT:\"" + parsedArgs.nodeRef;
            if (parsedArgs.nodeRef == "alfresco://sites/home")
            {
               // Special case for "Sites home" pseudo-nodeRef
               filterQuery += "/*/cm:dataLists";
            }
            filterQuery += "\"";
            filterQuery += " +@cm\\:creator:\"" + person.properties.userName + '"';
            filterQuery += " -TYPE:\"folder\"";
            filterParams.query = filterQuery + filterQueryDefaults;
            break;

         case "node":
            filterParams.query = "+ID:\"" + parsedArgs.nodeRef + "\"";
            break;

         case "tag":
            // Remove any trailing "/" character
            if (filterData.charAt(filterData.length - 1) == "/")
            {
               filterData = filterData.slice(0, -1);
            }
            filterParams.query += "+PATH:\"/cm:taggable/cm:" + search.ISO9075Encode(filterData) + "/member\"";
            break;

         default:
            filterParams.query = filterQuery + filterQueryDefaults;
            break;
      }

      return filterParams;
   }
};
