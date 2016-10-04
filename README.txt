=====================================
Repository for H2020-NEPHELE project 
=====================================


-------------------------------------
Application Affinity Service
-------------------------------------
To compile:
cd AppAffinity
mvn clean install

To launch:
cd AppAffinityService/
./run.sh

User guide:
Launch by following instructions above. ReST API documentation
can then be found at http://localhost:8089/swagger-ui.html

Info: m.capitani@nextworks.it

-------------------------------------
DLUX GUI
-------------------------------------
To compile:
cd dlux
mvn clean install [-DskipTests -DchckStyle.skip=true]

To install:
from ODL karaf console
feature-install odl-dlux-all
bundle:install mvn:org.opendaylight.dlux/dlux.appaffinity/0.2.4-Lithium-SR4

User Guide:
GUI can be found at
http://localhost:8181/index.html

Application affinity GUI is in the Application Affinity Tab.
