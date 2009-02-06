<html>
   <head>
      <title>Form Posted</title>
   </head>
   <body>
      <h2>${message}</h2>
      <#if data?exists && data.fields?exists>
         <ul>
            <#list data.fields as field>
               <li>${field.name} = ${field.value}</li>
            </#list>
         </ul>
      </#if>
   </body>
</html>