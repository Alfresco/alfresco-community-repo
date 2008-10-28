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
<#if args.e?exists><#assign extn=args.e>
<#else>
</#if>
<#assign extn="doc" extn1="odt" extn2="sxw">
<#assign extnx=extn+"x">
<#if args.n?exists><#assign nav=args.n><#else><#assign nav=""></#if>
<#assign chLen=companyhome.name?length>
<#if node.isDocument>
   <#assign thisSpace = node.parent>
<#else>
   <#assign thisSpace = node>
</#if>
<navigation>
<webdavSpace>${thisSpace.displayPath}/${thisSpace.name}/</webdavSpace>
<space>${thisSpace.name}</space>
<id>${thisSpace.id}</id>
<descr><#if thisSpace.properties.description?exists>${thisSpace.properties.description}</#if></descr>
<#if thisSpace=companyhome>
<#else>
   <up>${url.serviceContext}/office/navigation.xml?p=${path?url}&amp;n=${thisSpace.parent.id}</up>
</#if>

<lblSpaces>Spaces in ${thisSpace.name}</lblSpaces>
<lblCreate>Create New <#if args.cc?exists>Collaboration </#if>Space...</lblCreate>
<#assign xpath="app:dictionary/app:space_templates/*">
<#assign templates = companyhome.childrenByXPath[xpath]>
<#if (templates?size > 0)>
   <#list templates as template>
<template>
<name>${template.name}</name>
<value>${template.id}</value>
</template>                 
   </#list>
</#if>
<#assign spacesFound = 0>
<childSpaces>
<#list thisSpace.children?sort_by('name') as child>
   <#if child.isContainer>
<childSpace>
      <#assign spacesFound = spacesFound + 1>
<name>${child.name}</name>      
       <url>${url.serviceContext}/office/navigation.xml?p=${path?url}&amp;n=${child.id}</url>

      <#if child.properties.description?exists>
      		<descr>${child.properties.description}</descr>
      </#if>
</childSpace>
   </#if>
</#list>
</childSpaces>
<#if spacesFound = 0>
      <noItems>(No subspaces)</noItems>
</#if>

<docsLabel>Documents in ${thisSpace.name}</docsLabel>
<documents>
<#assign documentsFound = 0>
<#list thisSpace.children?sort_by('name') as child>
<document>
   <#if child.isDocument>
      <#assign documentsFound = documentsFound + 1>
	<title>${child.name}</title>
      <#assign relativePath = (child.displayPath?substring(chLen+1) + '/' + child.name)?url?replace('%2F', '/')?replace('\'', '\\\'') />
	<url>
       <#if child.name?ends_with(extn) || child.name?ends_with(extnx) || child.name?ends_with(extn1) || child.name?ends_with(extn2)>
	${relativePath}
      <#else>
            ${url.context}${child.url}?ticket=${session.ticket}
      </#if></url>
      <#if child.properties.description?exists>
         <#if (child.properties.description?length > 0)>
	 <descr>${child.properties.description}</descr>
		   </#if>
	<#else>
	 <descr>None.</descr>
      </#if>	
	<lastModified>${child.properties.modified?datetime}</lastModified>
	<fileSize>${(child.size / 1024)?int}Kb</fileSize>
      <#if child.isLocked >
        <locked>YES</locked>
      <#elseif hasAspect(child, "cm:workingcopy") == 1>
	<checkin>${doc_actions}?a=checkin&amp;n=${child.id}</checkin>
      <#else>
	<checkout>${doc_actions}?a=checkout&amp;n=${child.id}</checkout>      
      </#if>
	<workflow>${url.serviceContext}/office/docActions?a=workflow&amp;n=${child.id}</workflow>
            
        <insertDoc>${url.context}${child.url}?ticket=${session.ticket}</insertDoc>
      <#if !child.name?ends_with(".pdf")>
        <makePDF>${doc_actions}?a=makepdf&amp;n=${child.id}</makePDF>
      </#if>
      <#if !child.isLocked>
           <delete>${doc_actions}?a=delete&amp;n=${child.id}</delete>
      </#if>
   </#if>
</document>
</#list>
</documents>
<#assign currentPath = thisSpace.displayPath  + '/' + thisSpace.name />
<#assign currentPath = currentPath?substring(chLen+1)?url?replace('%2F', '/')?replace('\'', '\\\'') />
<currentPath>${currentPath}</currentPath>
<#if documentsFound = 0>
      <noDocs>YES</noDocs>
</#if>
</navigation>
