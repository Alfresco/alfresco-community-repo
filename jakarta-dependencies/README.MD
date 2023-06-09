## How to build dependencies

### Binary migrated ones
```shell
cd jakarta-dependencies
mvn clean install
```

### Internal components

#### surf-webscripts
```shell
git clone --single-branch --branch jakarta-migration git@github.com:Alfresco/surf-webscripts.git
cd surf-webscripts
mvn clean install
```

#### alfresco-greenmail
```shell
git clone --single-branch --branch jakarta-migration git@github.com:Alfresco/alfresco-greenmail.git
cd alfresco-greenmail
mvn clean install
```

#### alfresco-tas-email
```shell
git clone --single-branch --branch jakarta-migration git@github.com:Alfresco/alfresco-tas-email.git
cd alfresco-tas-email
mvn clean install
```

## How to build the repo
```shell
#Use jakarta-migration branch
mvn clean package -DskipTests -Dmaven.javadoc.skip=true -Pags
```

## With TAS tests compilation
```shell
 mvn clean package -DskipTests -Dmaven.javadoc.skip=true -Pags -Pall-tas-tests
```
* requires TAS test framework migration (similar approach to the greenmail/webscripts )
* Search for `//JAKARTA_TO_DO|` in all repositories
* Try running tests