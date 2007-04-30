<link rel="stylesheet" href="${url.context}/css/main.css" TYPE="text/css">

<script type="text/javascript" src="/alfresco/scripts/ajax/common.js"></script>
<script type="text/javascript" src="/alfresco/scripts/ajax/mootools.js"></script>
<script type="text/javascript">setContextPath('${url.context}');</script>

<#-- List the available web form objects in all web projects the user is assigned to -->
<table cellspacing=0 cellpadding=0 border=0 class="formsTable">
<tr><td>
<div id="formsPanel">
   <#assign search="TYPE:\"{http://www.alfresco.org/model/wcmappmodel/1.0}webfolder\"">
   <#list companyhome.childrenByLuceneSearch[search] as wp>
      <#list wp.childAssocs["wca:webuser"] as user>
         <#if user.properties["wca:username"] = person.properties.userName>
            <div class="webProjectRow"><a class="webProjectLink" href="${url.context}${wp.url}">${wp.name}</a></div>
               <#if wp.childAssocs["wca:webform"]?exists>
                  <#list wp.childAssocs["wca:webform"] as form>
                     <div class="formsRow">
                        ${form.properties["wca:formname"]}
                     </div>
                  </#list>
               </#if>
            </div>
         </#if>
      </#list>
   </#list>
</div>
</td></tr>
</table>

<STYLE type="text/css">
.formsTable
{
   background-color: #F8FCFD;
   border: 1px solid #CCD4DB;
}

#formsPanel
{
   width: 716px;
   overflow: auto;
}

a.webProjectLink:link, a.webProjectLink:visited, a.webProjectLink:hover
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
}

.webProjectRow
{
   padding: 8px;
   border-top: 1px solid #CCD4DB;
}

.formsRow, a.formsRow:link, a.formsRow:visited, a.formsRow:hover
{
   color: #5A5741;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   padding-left: 32px;
   padding-top: 4px;
   padding-bottom: 4px;
   border-bottom: 1px solid #F8FCFD;
}

.formsRowAlt
{
}

.formsFooter
{
   width: 700px;
   padding: 8px;
   border: 1px solid #F8FCFD;
   background-image: url();
   text-align: center;
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 13px;
   font-weight: bold;
}

.formsItem
{
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 14px;
   color: #515D6B;
   margin: 0 0 0 24;
   padding: 0px 8px 6px 8px;
}

.formsIcon
{
   width: 32px;
   float: left;
   padding-left: 16px;
   padding-top: 4px;
}

.formsDetail
{
   background-color: #CCE7F3;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-size: 12px;
   color: #000000;
   margin: 0px;
   display: none;
   overflow: hidden;
}

.formsMetadata
{
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
}

.formsMetaprop
{
   color: #515D6B;
   font-family: Trebuchet MS, Arial, Helvetica, sans-serif;
   font-weight: bold;
}
</STYLE>