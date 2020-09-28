<html>
<head>
  <title>Registered Mimetypes</title>
</head>
<body>
 <h1>Registered Mimetypes</h1>

 <#list mimetypes as mimetype>
   <a name="${mimetype}"></a>
   <h3 style="margin-bottom: 0em;">${mimetype} <#if extensions[mimetype]??>- ${extensions[mimetype]}</#if></h3>
   <#if details[mimetype]??>
    <div style="margin-left: 2em">
     <#if details[mimetype]["extractors"]?has_content>
        <div>
           <b>Extractors:</b>
           <#list details[mimetype]["extractors"] as ext>
              ${ext}<br />
           </#list>
        </div>
     <#else>
        <div><i>No extractors</i></div>
     </#if>

     <div>
       <b>Transformable To:</b>
       <#if details[mimetype]["transformFrom"]?has_content>
         <ul>
         <#list details[mimetype]["transformFrom"] as det>
             <li>${det}</li>
          </#list>
         </ul>
       <#else>
         <i>Cannot be transformed into anything else</i>
       </#if>
     </div>

     <div>
       <b>Transformable From:</b>
       <#if details[mimetype]["transformTo"]?has_content>
         <ul>
         <#list details[mimetype]["transformTo"] as det>
             <li>${det}</li>
          </#list>
         </ul>
       <#else>
         <i>Cannot be generated from anything else</i>
       </#if>
     </div>
    </div>
   <#else>
     <div style="font-size: 75%">
       <a href="?mimetype=${mimetype}#${mimetype}">(details for ${mimetype})</a>
     </div>
   </#if>
 </#list>

 <h4><a href="?mimetype=*">(details for all types)</a></h4>
</body>
</html>
