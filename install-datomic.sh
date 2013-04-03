#!/bin/sh
set -eux

VERSION="0.8.3862"

wget --continue http://downloads.datomic.com/$VERSION/datomic-free-$VERSION.zip
unzip -o datomic-free-$VERSION.zip

cd datomic-free-$VERSION
mvn install:install-file -DgroupId=com.datomic -DartifactId=datomic-free -Dfile=datomic-free-$VERSION.jar -DpomFile=pom.xml
cp config/samples/free-transactor-template.properties transactor.properties

echo "bin\transactor.cmd transactor.properties" > start.cmd
chmod a+x start.cmd
echo $'#!/bin/sh\n./bin/transactor transactor.properties' > start.sh
chmod a+x start.sh
