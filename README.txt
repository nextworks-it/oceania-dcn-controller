=====================================
OCEANiA DCN Controller

OPtiCal Electrical ApplicatioN Aware Data Center Network Controller
=====================================

-------------------------------------
What is OCEANiA?
-------------------------------------

OCEANiA DCN Controller is an SDN controller developed for the 
NEPHELE project.
NEPHELE is a research project on optical network technologies, 
supported by the Horizon2020 Framework Programme for Research 
and Innovation of the European Commission.

OCEANiA is a controller automating the configuration of a hybrid
opto-electrical data center network, leveraging SDN principles
and the OpenFlow protocol. It is developed to be used on top of
the OpenDaylight SDN controller, and makes use of its REST API.

NEPHELE project homepage:
http://www.nepheleproject.eu/

H2020 programme homepage:
https://ec.europa.eu/programmes/horizon2020/en

-------------------------------------
Installation
-------------------------------------
Launch the
./install.sh
script.
This will install both the Application affinity service and the
optical translator ODL module.

-------------------------------------
Application Affinity Service
-------------------------------------
To launch:
cd AppAffinity/
./run.sh

User guide:
ReST API documentation
can then be found at http://localhost:8089/swagger-ui.html

Info: m.capitani@nextworks.it

-------------------------------------
Optical translator
-------------------------------------
After installation, from the karaf console of ODL, launch the following commands:

feature:repo-add mvn:opticaltranslator/optical-translator-features/1.0.0-SNAPSHOT/xml/features
feature:install odl-optical-translator-nephele

This will install and start the optical translator for the processing of optical informations.

-------------------------------------
DLUX GUI
-------------------------------------

TO BE COMPLETED
