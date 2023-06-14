## How to build dependencies
```shell
cd jakarta-dependencies
./build-dependencies.sh
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