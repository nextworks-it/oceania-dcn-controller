{ type mvn >/dev/null 2>&1 && echo "Maven is installed. Starting operations."; } || { echo >&2 "Maven is needed but it's not installed. Aborting."; exit 1; }
echo "Installing the Application Affinity SDN app."
cd AppAffinity; mvn clean install -Dskiptests
echo "Installing the Optical Translator Opendaylight plugin."
