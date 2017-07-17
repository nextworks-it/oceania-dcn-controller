{ type mvn >/dev/null 2>&1 && echo "Maven is installed. Starting operations."; } || {
    echo >&2 "Maven is needed but it's not installed. Installing."
    cd ..
    wget http://ftp.cc.uoc.gr/mirrors/apache/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.tar.gz
    tar xvzf apache-maven-3.5.0-bin.tar.gz
    export PATH="apache-maven-3.5.0/bin:$PATH"
}
echo "Installing the Application Affinity SDN app."
cd AppAffinity; mvn clean install -Dskiptests
echo "Downloading OpenDaylight"
wget https://nexus.opendaylight.org/content/repositories/opendaylight.release/org/opendaylight/integration/distribution-karaf/0.3.4-Lithium-SR4/distribution-karaf-0.3.4-Lithium-SR4.zip
echo "Installing the Optical Translator Opendaylight plugin."
cd optical-translator; mvn clean install -DskipTests -Dcheckstyle.skip=true
