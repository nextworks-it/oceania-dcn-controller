if [[ "$#" -gt 1 ]]
then
    echo "Unexpected arguments: ${@:1}"
    exit 1
elif [[ "$#" -eq 1 ]]
then
    echo "Using $1 as JAVA_HOME"
    export JAVA_HOME="$1"
else
    echo "Using system-defined JAVA_HOME."
fi

if [ "$JAVA_HOME" == "" ]
then
    echo "JAVA_HOME is not set. Please set it to a Java 8+ JDK or specify a JAVA_HOME as argument."
    exit 1
fi

folder=$(pwd)
{ type mvn >/dev/null 2>&1 && echo "Maven is installed. Starting operations."; } || {
    echo >&2 "Maven is needed but it's not installed. Installing."
    cd $HOME
    wget http://ftp.cc.uoc.gr/mirrors/apache/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.tar.gz
    tar xvzf apache-maven-3.5.0-bin.tar.gz
    export PATH="$HOME/apache-maven-3.5.0/bin:$PATH"
    wget -q -O - https://raw.githubusercontent.com/opendaylight/odlparent/master/settings.xml > ~/.m2/settings.xml
    cd "$folder"
}
echo "Installing the Application Affinity SDN app."
{ cd AppAffinity; mvn clean install -Dskiptests; } || { echo >&2 "Maven error. Please check."; exit 1; }
echo "Downloading OpenDaylight"
cd $HOME
wget https://nexus.opendaylight.org/content/repositories/opendaylight.release/org/opendaylight/integration/distribution-karaf/0.3.4-Lithium-SR4/distribution-karaf-0.3.4-Lithium-SR4.zip
cd $folder
echo "Installing the Optical Translator Opendaylight plugin."
cd optical-translator; mvn clean install -DskipTests -Dcheckstyle.skip=true
