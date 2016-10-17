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
ODL
-------------------------------------
There might be problems if ODL plugins are not installed in the correct order.
Best working order is:

odl-openflowpugin-all
odl-dlux-all
odl-aaa-authn-no-cluster

This will install the minimum needed for the demo.

-------------------------------------
DLUX GUI
-------------------------------------
To compile:
cd dlux
mvn clean install [-DskipTests -DchckStyle.skip=true]

To install:
from ODL karaf console
feature-install odl-dlux-all

User Guide:
GUI can be found at
http://localhost:8181/index.html

Application affinity GUI is in the Application Affinity Tab.
