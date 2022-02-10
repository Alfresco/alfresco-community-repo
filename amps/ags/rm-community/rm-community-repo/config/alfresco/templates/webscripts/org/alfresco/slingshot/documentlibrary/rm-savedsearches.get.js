/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
function main()
{
   var savedSearches = [],
       siteId = url.templateArgs.site,
       siteNode = siteService.getSite(siteId),
       bPublic = args.p;

   if (siteNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Site not found: '" + siteId + "'");
      return null;
   }
   
   var searchNode = siteNode.getContainer("Saved Searches");
   if (searchNode != null)
   {
      var kids, ssNode;
      
      if (bPublic == null || bPublic == "true")
      {
         // public searches are in the root of the folder
         kids = searchNode.children;
      }
      else
      {
         // user specific searches are in a sub-folder of username
         var userNode = searchNode.childByNamePath(person.properties.userName);
         if (userNode != null)
         {
            kids = userNode.children;
         }
      }
      
      if (kids)
      {
         for (var i = 0, ii = kids.length; i < ii; i++)
         {
            ssNode = kids[i];
            if (ssNode.isDocument)
            {
               savedSearches.push(
               {
                  name: ssNode.name,
                  description: ssNode.properties.description
               });
            }
         }
      }
   }
   
   model.savedSearches = savedSearches;
}

main();
