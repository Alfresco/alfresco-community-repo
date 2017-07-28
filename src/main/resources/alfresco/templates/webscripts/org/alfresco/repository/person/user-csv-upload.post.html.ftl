<html>
<head>
   <title>Upload success</title>
</head>
<body>
<#if (args.success!"")?matches("^[\\w\\d\\._]+$")>
   <script type="text/javascript">
      ${args.success}({
          data: 
          {
              "totalUsers": ${totalUsers?c},
              "addedUsers": ${addedUsers?c},
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