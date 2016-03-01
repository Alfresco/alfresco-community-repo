/*
 * #%L
 * This file is part of Alfresco.
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
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
 * #L%
 */
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/rmsearch/faceted/rmsearch.lib.js">
function main() {
   var params =
   {
      siteId: args.site,
      containerId: args.container,
      repo: (args.repo !== null) ? (args.repo == "true") : false,
      term: args.term,
      tag: args.tag,
      query: args.query,
      rootNode: args.rootNode,
      sort: args.sort,
      maxResults: (args.maxResults !== null) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS,
      pageSize: (args.pageSize !== null) ? parseInt(args.pageSize, 10) : DEFAULT_PAGE_SIZE,
      startIndex: (args.startIndex !== null) ? parseInt(args.startIndex, 10) : 0,
      facetFields: args.facetFields,
      filters: args.filters,
      spell: (args.spellcheck !== null) ? (args.spellcheck == "true") : false
   };

   model.data = getSearchResults(params);
};

main();