=====================================
OCEANiA DCN Controller

OPtiCal Electrical ApplicatioN Aware Data Center Network Controller
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
There might be problems if the ODL plugins are not installed
in the correct order. Best working order is:

odl-openflowpugin-all
odl-dlux-all
odl-aaa-authn-no-cluster

This will install the minimum needed for the demo.
Notice that the extensions must be compiled first. 
If some or all of these plugins are installed without
extensions, from bin folder of ODL launch

./karaf clean

This will wipe all data and let you start with a "fresh install" of ODL.
Compile the extensions, then install the features as above.

-------------------------------------
OpenFlow Plugin extensions
-------------------------------------

TO BE COMPLETED

-------------------------------------
DLUX GUI
-------------------------------------

TO BE COMPLETED
