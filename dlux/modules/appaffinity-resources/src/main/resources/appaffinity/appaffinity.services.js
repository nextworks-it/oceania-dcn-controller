define(['app/appaffinity/appaffinity.module'], function(appaff) {

    appaff.register.factory('ODLRest', function(Restangular, ENV) {
        return Restangular.withConfig(function(RestangularConfig) {
            RestangularConfig.setBaseUrl(ENV.getBaseURL("MD_SAL"));
        });
    });

    appaff.register.factory('AARest', function(Restangular) {
        return Restangular.withConfig(function(RestangularConfig) {
          var url = window.location.protocol+"//"+window.location.hostname+":"+"8089/";
          RestangularConfig.setBaseUrl(url);
        });
      });

    appaff.register.factory('appaffinitySvc', function(AARest, ODLRest) {
        var svc = {

            // TODO: determine them someway and cut this out!
            P : 3,
            W : 4,
            Z : 3,
            T : 12,

            inventory: function() {
                return ODLRest.one('restconf/config/opendaylight-inventory:nodes');
            },
            affinity: function() {
                return AARest.one('affinity');
            },
            tMat: function() {
                return AARest.one('trafficmatrix/matrix');
            }
        };

        svc.getTraffic = function(cb) {
            return svc.tMat().get().then(function(mat) {
                var nodes = {};
                var i,j;
                var n = mat.length;
                for (i = 0; i < svc.P * svc.W * svc.Z; i++) {
                    for (j = 0; j < svc.P * svc.W; j++) {
                        if (mat[i][j] !== 0){

                            var pSrc = Math.floor(i/(svc.W * svc.Z)),
                            wSrc = Math.floor((i % (svc.W * svc.Z))/svc.Z),
                            srcId = 'p' + pSrc + '-w' + wSrc;

                            var pDes = Math.floor(j/(svc.W * svc.Z)),
                            wDes = Math.floor((j % (svc.W * svc.Z))/svc.Z),
                            desId = 'p' + pDes + '-w' + wDes;

                            if (nodes[srcId] === undefined) {
                                nodes[srcId] = {id:srcId, incoming: [], outgoing: []};
                            }
                            if (nodes[desId] === undefined) {
                                nodes[desId] = {id:desId, incoming: [], outgoing: []};
                            }
                            nodes[desId].incoming.push([srcId, mat[i][j]]);
                            nodes[srcId].outgoing.push([desId, mat[i][j]]);
                        }
                    }
                }
                for (var key in nodes){
                    var node = nodes[key];
                    var inLen = 0, outLen = 0;
                    if (node.incoming) {
                        inLen = Object.keys(node.incoming).length;
                    }
                    if (node.outgoing) {
                        outLen = Object.keys(node.outgoing).length;
                    }
                    node.rows = inLen + outLen;
                }
                cb(nodes);
            },
            function(response) {
                console.log("Error with status code ", response.status);
                cb(undefined); //sends null data to the controller since the call failed
            });
        };

        svc.parseFlow = function(flow) {
            var output = {};
            output.id = flow.id;

            var outAction = flow.instructions.instruction[0]['apply-actions'].action[0]['output-action'];
            output.outPort = outAction['output-node-connector'];
            if (outAction.timeslot !== undefined) {
                output.timeslot = outAction.timeslot.substring(0, svc.T);
            }
            if (outAction.wavelength !== undefined) {
                output.wavelength = outAction.wavelength;
            }

            output.inPort = flow.match['in-port'];

            return output;
        };

        svc.parseNode = function(data, node) {
            var flowList;
            for (var i = 0; i< data['flow-node-inventory:table'].length; i++) {
                 if (data['flow-node-inventory:table'][i].id == '0') {
                     flowList = data['flow-node-inventory:table'][i].flow;
                     i = data['flow-node-inventory:table'].length;
                 }
            }
            var outflows = [];
            for (var j = 0; j < flowList.length; j++) {
                var flow = flowList[j];
                node.flows.push(svc.parseFlow(flow));
            }
            return outflows;
        };


        svc.getFlows= function(cb) {
            return svc.inventory().get().then(function(inventory) {
                var lst = inventory['nodes']['node'];
                var nodes = [];
                for (var i = 0; i<lst.length; i++) {
                    var data = lst[i];
                    var node = {};
                    node.id = data.id;
                    node.flows = [];
                    svc.parseNode(data, node);
                    nodes.push(node);
                }
                cb(nodes);
            }, function(response) {
                console.log("error with status code " + response.status);
            });
        };

        svc.submitService = function(service) {
            return svc.affinity().all('connection').post(service);
        };
        return svc;
    });
});
