<!-- _lcid="1033" _version="11.0.5510" _dal="1" -->
<!-- _LocalBinding -->
<html dir="ltr">
<HEAD>
    <META Name="GENERATOR" Content="Microsoft SharePoint">
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=utf-8">
    <META HTTP-EQUIV="Expires" content="0">
    <link rel="stylesheet" href="${resourcesUrl}/css/main.css" type="text/css">
    <link rel="stylesheet" href="${resourcesUrl}/css/picker.css" type="text/css">
    <Title ID=onetidTitle>File Properties</Title>
    <script language="JavaScript">
       function checkScroll()
       {
          if (document.body.scrollHeight > document.body.offsetHeight || document.body.scrollWidth > document.body.offsetWidth)
             document.body.scroll="yes";
       }
    </script>
    <style type="text/css">
      .deselected {
         background-color: white;
      }
      .selected {
         background-color: #BBDDFF;
      }
    </style>
    <script type="text/javascript">
       var oldSelect = null;
       function selectrow(rowId) {
          var selectedRow = document.getElementById(rowId);
             if (oldSelect != null && selectedRow != oldSelect) {
                oldSelect.className = "deselected";
             }
             selectedRow.className = "selected";
             oldSelect = selectedRow;
       }
    </script>
    <script type="text/javascript">
       function changeStyle(id)
       {
          document.getElementById(id).style.cursor = "pointer";
          document.getElementById(id).style.textDecoration = "underline";
       }

       function revertStyle(id)
       {
          document.getElementById(id).style.cursor = "default";
          document.getElementById(id).style.textDecoration = "none";
       }
    </script>
</HEAD>

<BODY topmargin=5 leftmargin=5 scroll=no serverType=OWS onload="checkScroll()" onresize="checkScroll()">
   
   <table width="100%">
      <tr> <td width="100%" align="rigth"> <img src='${resourcesUrl}/images/logo/AlfrescoLogo200.png' width=200 height=58 alt="Alfresco" title="Alfresco"> </td> </tr>
   </table>
   
   <table ID="FileDialogViewTable" width="100%" class="recordSet" style="cursor: default;" cellspacing=0>

      <tr>
         <th style="padding: 2px; text-align: left; width: 40px;" class="recordSetHeader">&nbsp;</th>
         <th style="padding: 2px; text-align: left" class="recordSetHeader">${name}</th>
      </tr>

      <tr height="5">
         <td colspan="5"> </td>
      </tr>
   
   <#list items as item>
      <#if item.isFolder()>
         <tr class="recordSetRow"  fileattribute=folder ID="${item.getUrl()}" onmousedown="selectrow('${item.getUrl()}')">
            <td style="padding: 2px; text-align: left; width: 40px;"><IMG BORDER=0 ALT="Icon" SRC="${resourcesUrl}/images/icons/space-icon-website.gif"></td>
            <td style="text-align: left; font-size: 250%;">${item.getName()}</td>
         </tr>                
      </#if>             
   </#list>
   
</BODY>

</html>