# Fetches this scripts' path (in case it is executed from another folder)
pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null

errors=0

if [ ! -f "$SCRIPTPATH/target/application.properties" ]; then
    cp "$SCRIPTPATH/src/main/resources/application.properties" "$SCRIPTPATH/target/application.properties"
    errors="$?"
fi

if [[ "$errors" == 0 ]]; then
    /usr/bin/env editor "$SCRIPTPATH/target/application.properties"
else
    echo "ERROR! Command 'cp $SCRIPTPATH/src/main/resources/application.properties $SCRIPTPATH/target/application.properties' failed."
fi
