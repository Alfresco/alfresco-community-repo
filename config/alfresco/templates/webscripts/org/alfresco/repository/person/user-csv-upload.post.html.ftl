<html>
<head>
   <title>Upload success</title>
</head>
<body>
<#if (args.successCallback?exists)>
   <script type="text/javascript">
      ${args.successCallback}.call(${args.successScope},
      {
          data: 
          {
              "totalUsers": ${totalUsers},
              "addedUsers": ${addedUsers},
              "users": 
              [
                   <#list users?keys as username>
                       {
                           "username": "${username}",
                           "uploadStatus": "${users[username]}"
                       }
                       <#if username_has_next>,</#if>
                   </#list>
              ]
          }
      });
   </script>
</#if>
</body>
</html>