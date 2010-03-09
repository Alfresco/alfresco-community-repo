# ! /bin/sh

python validator/appclienttest.py --html --credentials=credentials.txt --verbose --output=results.html "http://localhost:8080/alfresco/service/api/repository" 
open results.html
