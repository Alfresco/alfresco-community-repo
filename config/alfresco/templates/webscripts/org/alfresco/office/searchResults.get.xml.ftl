<?xml version="1.0" encoding="UTF-8"?>
<!--****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.                    *
 *                                                                             *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.                  *
 *                                                                             * 
 * The contents of this file are subject to the terms of either the GNU General* 
 * Public License Version 2 only ("GPL") or the Common Development and         *  
 * Distribution License("CDDL") (collectively, the "License").                 * 
 * You may not use this file except in  compliance with the License.           *
 * You can obtain a copy of the License at AlfrescoAddon/licenses/license.txt. * 
 * See the License for the specific language governing permissions and         *
 * limitations under the License.  When distributing the software, include this* 
 * License Header Notice in each file and include the License file at          *
 * AlfrescoAddon/licenses/license.txt. If applicable, add the following below  *
 * the License Header, with the fields enclosed by brackets [] replaced by your* 
 * own identifying information:                                                * 
 * "Portions Copyrighted [year] [name of copyright owner]"                     * 
 *                                                                             * 
 * Contributor(s):                                                             * 
 * If you wish your version of this file to be governed by only the CDDL or    * 
 * only the GPL Version 2, indicate your decision by adding "[Contributor]     * 
 * elects to include this software in this distribution under the              *
 * [CDDL or GPL Version 2] license." If you don't indicate a single choice of  * 
 * license, a recipient has the option to distribute your version of this file *
 * under either the CDDL, the GPL Version 2 or  to extend the choice of        *
 * license to its licensees as provided above.  However, if you add            *
 * GPL Version 2 code and therefore, elected the GPL Version 2 license, then   * 
 * the option applies only if the new code is made subject to such option by   * 
 * the copyright holder.                                                       *
 ****************************************************************************-->

<#assign extn="doc" extn1="odt" extn2="sxw">
<#if args.search?exists>
   <#assign searchString = args.search>
   <#if searchString != "">
      <#assign queryString = "(TEXT:\"${searchString}\") OR (@cm\\:name:*${searchString}*)" >
      <#-- assign queryString = "@\\{http\\://www.alfresco.org/model/content/1.0\\}name:*${searchString}*" -->
   </#if>
<#else>
   <#assign searchString = "">
   <#assign queryString = "">
</#if>
<searchResults>
<#if searchString != "">
   <#if args.maxresults?exists>
      <#assign maxresults=args.maxresults?number>
   <#else>
      <#assign maxresults=10>
   </#if>
   <#assign resCount=0>
   <#assign totalResults=0>
   <#assign results = companyhome.childrenByLuceneSearch[queryString] >
   <#if results?size = 0>
   <noItems>
     (No results found)
   </noItems>
   <#else>
      <#assign totalResults = results?size>
      <#list results as child>	      
         <#if child.isDocument>	
<#assign resCount=resCount + 1>  
            <#if child.name?ends_with(extn) || child.name?ends_with(extn1) || child.name?ends_with(extn2)>
               <#assign relativePath = (child.displayPath?substring(companyhome.name?length+1) + '/' + child.name)?url?replace('%2F', '/')?replace('\'', '\\\'') />
               
               <#assign openURL = "${relativePath}">
            <#else>
               <#assign openURL = "${url.context}${child.url}?ticket=${session.ticket}">
              
            </#if>
	<result>
	<name>${child.name}</name>
         <url>${openURL}</url>

         <#if child.properties.description?exists>
            <#if (child.properties.description?length > 0)>
              <details> ${child.properties.description}</details>
            </#if>
         </#if>
         <#if child.isDocument>
            <modified>${child.properties.modified?datetime} (${(child.size / 1024)?int}Kb)</modified>
         </#if>
	</result>
         </#if>   
	     <#if resCount = maxresults>
            <#break>
         </#if>       
      </#list>
   </#if>
</#if>
</searchResults>
