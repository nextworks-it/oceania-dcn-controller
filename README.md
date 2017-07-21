# OCEANiA DCN Controller

## (OPtiCal Electrical ApplicatioN Aware Data Center Network Controller)


1. What is OCEANiA?
-------------------------------------

OCEANiA DCN Controller is an SDN controller developed for the
NEPHELE project.

[NEPHELE](
  http://www.nepheleproject.eu/ "NEPHELE project"
)
is a research project on optical network technologies,
supported by the [Horizon2020](
  https://ec.europa.eu/programmes/horizon2020/en "H2020 programme"
)
Framework Programme for Research
and Innovation of the European Commission.

OCEANiA is a controller automating the configuration of a hybrid
opto-electrical data center network, leveraging SDN principles
and the OpenFlow protocol. It is developed to be used on top of
the OpenDaylight SDN controller, and makes use of its REST API.


2. Installation
-------------------------------------
Launch the `./install.sh` script.
This will install
+ [Apache Maven](https://maven.apache.org "Maven"),
  if not already present on the system
+ The Application affinity service
+ A pre-built distribution of the [OpenDaylight SDN controller](
  https://www.opendaylight.org/ "OpenDaylight"
)
+ The optical translator ODL module.
+ The modified NEPHELE DLUX GUI


3. Application Affinity Service
-------------------------------------
To launch:
```
cd AppAffinity/
./run.sh
```

The ReST API documentation
can then be found at http://&lt;controller-IP&gt;:8089/swagger-ui.html


4. Optical translator
-------------------------------------
After installation, to run the OpenDaylight controller, use `./../distribution-karaf-0.3.4-Lithium-SR4/bin/karaf`. This will start ODL and
log into the karaf console.
From the karaf console of ODL then use the following commands:
```
feature:repo-add mvn:opticaltranslator/optical-translator-features/1.0.0-SNAPSHOT/xml/features
feature:install odl-optical-translator-nephele
```

This will install and start the optical translator, which will then process
the requests for optical flows.

5. DLUX GUI
-------------------------------------
After Installation, from the karaf console of ODL, use the following commands:
```
feature:install odl-dlux-all
```

This will install the modified DLUX GUI. It will then be accessible by
directing any web browser to http://&lt;controller-IP&gt;:8181/index.html.
