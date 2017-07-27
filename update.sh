source "$HOME/.profile"

if [[ "$#" -gt 1 ]]
then
    echo "Unexpected arguments: ${@:1}"
    exit 1
elif [[ "$#" -eq 1 ]]
then
    echo "Updating $1."
else
    echo "Please specify what should be updated."; exit 1
fi

if [[ "$1" == "affinity" ]]
then
    echo "Updating the Application Affinity SDN app."
    { cd AppAffinity && mvn clean install -Dskiptests; } || { echo >&2 "Maven error. Please check."; exit 1; }
elif [[ "$1" == "translator" ]]
then
    echo "Updating the Optical Translator Opendaylight plugin."
    { cd optical-translator && mvn clean install -DskipTests -Dcheckstyle.skip=true; } || { echo >&2 "Maven error. Please check."; exit 1; }
elif [[ "$1" == "dlux" ]]
then
    echo "Updating the DLUX GUI."
    { 
        cd dlux/modules && mvn clean install &&
        cd ../bundles && mvn clean install;
    } || { echo >&2 "Maven error. Please check."; exit 1; }
else
    echo "No component named $1."
fi

