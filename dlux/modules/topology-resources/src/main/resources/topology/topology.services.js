define(['app/topology/topology.module'], function(topology) {

  topology.register.factory('TopologyRestangular', function(Restangular, ENV) {
    return Restangular.withConfig(function(RestangularConfig) {
      RestangularConfig.setBaseUrl(ENV.getBaseURL("MD_SAL"));
    });
  });

  topology.register.factory('NetworkTopologySvc', function(TopologyRestangular) {
      var svc = {
          base: function() {
              return TopologyRestangular.one('restconf').one('operational').one('network-topology:network-topology');
          },
          data: null,
          TOPOLOGY_CONST: {
              HT_SERVICE_ID:"host-tracker-service:id",
              IP:"ip",
              HT_SERVICE_ATTPOINTS:"host-tracker-service:attachment-points",
              HT_SERVICE_TPID:"host-tracker-service:tp-id",
              NODE_ID:"node-id",
              SOURCE_NODE:"source-node",
              DEST_NODE:"dest-node",
              SOURCE_TP:"source-tp",
              DEST_TP:"dest-tp",
              ADDRESSES:"addresses",
              HT_SERVICE_ADDS:"host-tracker-service:addresses",
              HT_SERVICE_IP:"host-tracker-service:ip"
          }
      };
      svc.getCurrentData = function() {
          return svc.data;
      };
      svc.getAllNodes = function() {
          svc.data = svc.base().getList();
          return svc.data;
      };
      svc.getNode = function(node,cb) { // TODO Change into returning static topology
          //Determines the node id from the nodes array corresponding to the text passed
          var getNodeIdByText = function getNodeIdByText(inNodes, text) {
              var nodes = inNodes.filter(function(item, index) {
                      return item.label === text;
                  }),
                  nodeId;

              if(nodes && nodes[0]) {
                  nodeId = nodes[0].id;
              }else{
                  return null;
              }

              return nodeId;
          };
          //Checks if the edge is present in the links map or not so we show single edge link between switches
          var isEdgePresent = function(inLinks,srcId,dstId){
              if( inLinks[srcId+":"+dstId] === undefined && inLinks[dstId+":"+srcId] === undefined) {
                  return false;
              }
              else {
                  return true;
              }
          };
          return svc.base().one("topology", node).get().then(function(ntData){

              var nodes = [];
              var links = [];
              var linksMap = {};

              /*if(ntData.topology && ntData.topology[0]){
                  //Loop over the nodes
                  angular.forEach(ntData.topology[0].node, function(nodeData) {
                    var groupType = "", nodeTitle = "", nodeLabel = "";
                    var nodeId = nodeData[svc.TOPOLOGY_CONST.NODE_ID];
                    var level = 3;
                      if(nodeId!==undefined && nodeId.indexOf("host")>=0){
                        groupType = "host";
                        var ht_serviceadd = nodeData[svc.TOPOLOGY_CONST.ADDRESSES];
                          if(ht_serviceadd===undefined){
                              ht_serviceadd = nodeData[svc.TOPOLOGY_CONST.HT_SERVICE_ADDS];
                          }
                          if(ht_serviceadd!==undefined){
                              var ip;
                            //get title info
                            for(var i=0;i<ht_serviceadd.length;i++){
                                ip = ht_serviceadd[i][svc.TOPOLOGY_CONST.IP];
                                if(ip===undefined){
                                    ip = ht_serviceadd[i][svc.TOPOLOGY_CONST.HT_SERVICE_IP];
                                }
                                nodeTitle += 'IP: <b>' + ip + '</b><br>';
                            }
                          }
                        nodeTitle += 'Type: Host';
                      }
                      else{
                        groupType = "switch";
                        nodeTitle = 'Name: <b>' + nodeId + '</b><br>Type: Switch';
                        if (nodeId!==undefined && nodeId.indexOf("openflow:201") >= 0) {
                          level = 0;
                        } else if (nodeId!==undefined && nodeId.indexOf("openflow:202") >= 0) {
                          level = 1;
                        } else if (nodeId!==undefined && nodeId.indexOf("openflow:1") >= 0) {
                          level = 2;
                        }
                      }

                    nodeLabel = nodeData[svc.TOPOLOGY_CONST.NODE_ID];
                    nodes.push({
                        'id': nodes.length.toString(),
                        'label': nodeLabel,
                        group: groupType,
                        value: 20,
                        title: nodeTitle
                    });
                  });
                  //Loops over the links
                  angular.forEach(ntData.topology[0].link, function(linkData) {
                      var srcId = getNodeIdByText(nodes, linkData.source[svc.TOPOLOGY_CONST.SOURCE_NODE]),
                          dstId = getNodeIdByText(nodes, linkData.destination[svc.TOPOLOGY_CONST.DEST_NODE]),
                          srcPort = linkData.source[svc.TOPOLOGY_CONST.SOURCE_TP],
                          dstPort = linkData.destination[svc.TOPOLOGY_CONST.DEST_TP],
                          linkId = links.length.toString();
                      if(srcId!=null && dstId!=null && !isEdgePresent(linksMap,srcId,dstId)){
                          links.push({id: linkId, 'from' : srcId, 'to': dstId, title:'Source Port: <b>' + srcPort + '</b><br>Dest Port: <b>'+dstPort+'</b>'});
                          linksMap[srcId+":"+dstId]=linkId;
                      }
                  });
              }*/

              var nodeLabel;

              var srcId;
              var dstId;
              var linkId;

              for (var pod = 1; pod < 3; pod++) {
                for (var plane = 1; plane < 3; plane++) {
                  nodeLabel = "openflow:20" + plane.toString() + "0" + pod.toString();
                  if (getNodeIdByText(nodes, nodeLabel) == null) {
                      nodes.push({
                        'id': nodes.length.toString(),
                        'label': nodeLabel,
                        group: "switch",
                        value: 20,
                        title: 'Name: <b>' + nodeLabel + '</b><br>Type: Switch',
                        level: plane - 1
                      });
                  }
                  srcId = getNodeIdByText(nodes, "openflow:20" + plane.toString() + "01");
                  dstId = getNodeIdByText(nodes, "openflow:20" + plane.toString() + "02");
                  if (pod == 2 && srcId!=null && dstId!=null && !isEdgePresent(
                      linksMap,
                      srcId,
                      dstId
                  )) {
                    linkId = links.length.toString();
                    links.push({id: linkId, 'from' : srcId, 'to': dstId, title:'Placeholder link'});
                    linksMap[srcId+":"+dstId]=linkId;
                  }

                  srcId = getNodeIdByText(nodes, "openflow:2010" + pod.toString());
                  dstId = getNodeIdByText(nodes, "openflow:2020" + pod.toString());

                  if (plane == 2 && srcId!=null && dstId!=null && !isEdgePresent(
                      linksMap,
                      srcId,
                      dstId
                  )) {
                    linkId = links.length.toString();
                    links.push({id: linkId, 'from' : srcId, 'to': dstId, title:'Placeholder link'});
                    linksMap[srcId+":"+dstId]=linkId;
                  }
                }
              }

              for (var pod2 = 1; pod2 < 3; pod2++) {
                for (var tor = 1; tor < 5; tor++) {
                  nodeLabel = "openflow:10" + pod2.toString() + "0" + tor.toString();
                  if (getNodeIdByText(nodes, nodeLabel) == null) {
                      nodes.push({
                        'id': nodes.length.toString(),
                        'label': nodeLabel,
                        group: "switch",
                        value: 20,
                        title: 'Name: <b>' + nodeLabel + '</b><br>Type: Switch',
                        level: 2
                      });
                  }
                  for (var plane2 = 1; plane2 < 3; plane2++) {
                    srcId = getNodeIdByText(nodes, "openflow:10" + pod2.toString() + "0" + tor.toString());
                    dstId = getNodeIdByText(nodes, "openflow:20" + plane2.toString() + "0" + pod2.toString());
                    if (srcId!=null && dstId!=null && !isEdgePresent(
                      linksMap,
                      srcId,
                      dstId
                    )) {
                      linkId = links.length.toString();
                      links.push({id: linkId, 'from' : srcId, 'to': dstId, title:'Placeholder link'});
                      linksMap[srcId+":"+dstId]=linkId;
                    }
                  }
                }
              }

              var data = {
                  "nodes" : nodes,
                  "links" : links
              };
              cb(data);
          },function(response) {
              console.log("Error with status code", response.status);
          });
      };
      return svc;
  });

});
