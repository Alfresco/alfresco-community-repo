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
<#assign path=args.p!"">
<#if args.e??><#assign extn=args.e><#else><#assign extn="doc"></#if>
<#assign nav=args.n!"">
<#if docWorkflow??>
   <#assign d=docWorkflow>
<#else>
   <#-- resolve the path (from Company Home) into a node -->
   <#if companyhome.childByNamePath[path]??>
      <#assign d=companyhome.childByNamePath[path]>
   <#else>
      <#assign d=companyhome>
   </#if>
</#if>
<myTasks>
<title>My Tasks</title>
      <fileExtn>${extn}</fileExtn>
<myTasks title="My Tasks">
<task>
<#assign taskNum=0>
<#list workflow.assignedTasks as t>
   <#assign taskNum=taskNum+1>
   <#assign hasDue=t.properties["bpm:dueDate"]??>
   <#if hasDue>
      <#assign due=t.properties["bpm:dueDate"]>
   </#if>
<hasDue>
<#if hasDue>${due?date?string("yyyyMMddHHmmss")}<#else>99999999999999</#if>">
</hasDue>
   <#if hasDue>
      <#-- items due today? -->
      <#if (dateCompare(date?date, due?date, 0, "==") == 1)>
         <img src="${url.context}/images/office/task_today.gif" alt="due today" />
      <#-- items overdue? -->
      <#elseif (dateCompare(date?date, due?date) == 1)>
         <img src="${url.context}/images/office/task_overdue.gif" alt="overdue" />
      </#if>
   <#else>
         &nbsp;
   </#if>
      <taskItemDetails>
         ${t.description?html} (${t.type?html})
	</taskItemDetails>
   <#if hasDue>
            <dueDate> ${due?date}
   <#else>
            (No due date)
   </#if>
</dueDate>
</#list>
<#if taskNum = 0>
      <noItems>(No tasks)</noItems>
</#if>
</task>
<workflow>
<#if args.w?? && d.isDocument>
      <taskDetails>
               <taskAction>${url.serviceContext}/office/docActions, ${d.id}</taskAction>
<#else>
      
</#if>
</myTasks>
