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

<#assign doc_actions="${url.serviceContext}/office/docActions">
<#if args.p?exists><#assign path=args.p><#else><#assign path=""></#if>
<#if args.e?exists><#assign extn=args.e><#assign extn="doc" extn1="odt" extn2="sxw"></#if>
<#if args.n?exists><#assign nav=args.n><#else><#assign nav=""></#if>
<#-- resolve the path (from Company Home) into a node -->
<#if companyhome.childByNamePath[path]?exists>
   <#assign d=companyhome.childByNamePath[path]>
<#else>
   <#assign d=companyhome>
</#if>
<myAlfresco>
   <title>My Alfresco</title>
   <links>
      <link title="Browse Spaces and Documents">${url.serviceContext}/office/navigation?p=${path?url}&amp;e=${extn}&amp;n=${nav}</link>
      <link title="Search Alfresco">${url.serviceContext}/office/search?p=${path?url}&amp;e=${extn}&amp;n=${nav}</link>
      <documentDetails>${url.serviceContext}/office/documentDetails?p=${path?url}&amp;e=${extn}&amp;n=${nav}</documentDetails>
      <link title="My Tasks">${url.serviceContext}/office/myTasks?p=${path?url}&amp;e=${extn}&amp;n=${nav}</link>
   </links>
   <myDocs title="My Checked Out Documents">
<#assign query="@cm\\:workingCopyOwner:${person.properties.userName}">
   <#list companyhome.childrenByLuceneSearch[query] as child>
      <#if child.isDocument>
         <#assign relativePath = (child.displayPath?substring(companyhome.name?length+1) + '/' + child.name)?url?replace('%2F', '/')?replace('\'', '\\\'') />
<saveDir>${child.displayPath?substring(companyhome.name?length+1) + '/'}</saveDir>
      <document type="<#if child.name?ends_with(extn) || child.name?ends_with(extn1) || child.name?ends_with(extn2)>office<#else>general</#if>">
         <icon>${url.context}${child.icon32}</icon>
         <title>${child.name}</title>
         <url>
         <#if child.name?ends_with(extn) || child.name?ends_with(extn1) || child.name?ends_with(extn2)>
            ${relativePath}
         <#else>
            ${url.context}${child.url}?ticket=${session.ticket}
         </#if>
         <#if child.properties.description?exists>
            <#if (child.properties.description?length > 0)>
               <description>${child.properties.description}</description>
            </#if>
         </#if>
</url>
         <lastModified>${child.properties.modified?datetime}</lastModified>
         <fileSize>${(child.size / 1024)?int}Kb</fileSize>
	 <checkin>${doc_actions}?a=checkin&amp;n=${child.id}</checkin>
            <workflow>${url.serviceContext}/office/docActions?a=workflow&amp;n=${child.id}</workflow>
            <insertDoc>${url.context}${child.url}?ticket=${session.ticket}</insertDoc>
         <#if !child.name?ends_with(".pdf")>
            <makePDF>${doc_actions}?a=makepdf&amp;n=${child.id}</makePDF>
         </#if>
      </document>
      </#if>
   </#list>
   </myDocs>
   <myTasks title="My Tasks">
<#list workflow.assignedTasks as t>
      <task>
   <#assign hasDue=t.properties["bpm:dueDate"]?exists>
   <#if hasDue>
      <#assign due=t.properties["bpm:dueDate"]>
   </#if>
   <#if hasDue>
      <#-- items due today? -->
      <#if (dateCompare(date?date, due?date, 0, "==") == 1)>
         <dueToday/>
      <#-- items overdue? -->
      <#elseif (dateCompare(date?date, due?date) == 1)>
         <overdue/>
      </#if>
   </#if>
	 <id>${t.id}</id>
	<url>${url.context}/service/office/myTasksDetail.xml?t=${t.id}&amp;e=doc</url>
         <description>${t.description!""?html}</description>
         <type>${t.type?html}</type>
   <#if hasDue>
         <dueDate>${due?date}</dueDate>
   </#if>
      </task>
</#list>
   </myTasks>
<launchAlfresco>${url.context}/navigate/browse?ticket=${session.ticket}</launchAlfresco>
</myAlfresco>
