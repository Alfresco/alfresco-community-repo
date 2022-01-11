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
// RM Search Lib.

// Wrap the original document item method with our own one that appends RM specific properties.
// Additional properties will also need rendering in rmsearch.get.json.ftl.
var getOriginalDocumentItem = getDocumentItem,
   getOriginalRepositoryItem = getRepositoryItem;
getDocumentItem = function(siteId, containerId, pathParts, node, populate, highlighting){
   // Get original Document item.
   var item = getOriginalDocumentItem(siteId, containerId, pathParts, node, populate, highlighting);

   item.nodeJSON = appUtils.toJSON(node, true);

   return item;
};

getRepositoryItem = function(folderPath, node, populate, highlighting){
   // Get Original Repo item
   var item = getOriginalRepositoryItem(folderPath, node, populate, highlighting);

   if (item.type === "document") {
      item.nodeJSON = appUtils.toJSON(node, true);
   }

   return item;
};
