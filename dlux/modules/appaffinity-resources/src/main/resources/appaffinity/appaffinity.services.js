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
                for (i = 0; i < svc.P * svc.W * svc.Z; i++) {
                    for (j = 0; j < svc.P * svc.W; j++) {
                        if (mat[i][j] !== 0){

                            var pSrc = Math.floor(i/(svc.W * svc.Z)),
                            wSrc = Math.floor((i % (svc.W * svc.Z))/svc.Z),
                            srcId = 'pod: ' + pSrc + '; lambda: ' + wSrc;

                            var pDes = Math.floor(j/(svc.W)),
                            wDes = j % (svc.W),
                            desId = 'pod: ' + pDes + '; lambda: ' + wDes;

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
                console.log("Error: " + response.data.message + " with status code ", response.status);
                cb(undefined); //sends null data to the controller since the call failed
            });
        };

        svc.parseFlow = function(flow) {
            var output = {};
            output.id = flow.id;

            var outAction = flow.instructions.instruction[0]['apply-actions'].action[0]['output-action'];
            output.outPort = outAction['output-node-connector'];
            if (flow.match['ipv4-destination'] !== undefined) {
                output.destination = flow.match['ipv4-destination'];
            }
            output.inPort = flow.match['in-port'];

            return output;
        };

        svc.parseOpticalFlow = function(flow) {
            var output = {};
            if (flow['opt-opt-case']) {
                output.outPort = flow['opt-opt-case']['opt-output-type']['wport'];
                if (flow['opt-opt-case']['opt-match-type']['wport']) {
                    output.inPort = flow['opt-opt-case']['opt-match-type']['wport'];
                } else {
                    output.inPort = "ANY";
                }

                output.timeslot = flow['opt-opt-case']['opt-output-type']['timeslots'].substring(0, svc.T);
                output.wavelength = flow['opt-opt-case']['opt-output-type']['wavelength'];

            }

            if (flow['eth-opt-case']) {
                output.outPort = flow['eth-opt-case']['opt-output-type']['wport'];
                output.timeslot = flow['eth-opt-case']['opt-output-type']['timeslots'].substring(0, svc.T);
                output.wavelength = flow['eth-opt-case']['opt-output-type']['wavelength'];
                if (flow['eth-opt-case']['eth-match-type']['in-port']) {
                    output.inPort = flow['eth-opt-case']['eth-match-type']['in-port'];
                } else {
                    output.inPort = "ANY";
                }
            }
            return output;
        };

        svc.parseNode = function(data, node) {
            var flowList;
            for (var i = 0; i< data['flow-node-inventory:table'].length; i++) {
                 if (data['flow-node-inventory:table'][i].id == '1') {
                     flowList = data['flow-node-inventory:table'][i].flow;
                     i = data['flow-node-inventory:table'].length;
                 }
            }
            for (var j = 0; j < flowList.length; j++) {
                var flow = flowList[j];
                node.flows.push(svc.parseFlow(flow));
            }
            var optFlowList = data['optical-translator:optical-flow-table']['optical-flow'];
            for (var k = 0; k < optFlowList.length; k++) {
                var optFlow = optFlowList[k];
                node.flows.push(svc.parseOpticalFlow(optFlow));
            }
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
                console.log("Error: " + response.data.message +" with status code ", response.status);
                cb(undefined);
            });
        };

        svc.graphics = function(nodes, sourceTor, sourceZone, destTor) {
            if (nodes === undefined) {return undefined;}
            var lambda = parseInt(destTor.match(/^openflow:10[1-9]00([1-9])$/)[1]);
            var destPod = parseInt(destTor.match(/^openflow:10([1-9])00[1-9]$/)[1]);
            var destIP = "10." + String(destPod) + "." + String(lambda) + ".";

            var data = {};
            data.destPod = destPod;
            data.srcPod = parseInt(sourceTor.match(/^openflow:10([1-9])00[1-9]$/)[1]);
            data.lambda = lambda;
            data.srcLambda = parseInt(sourceTor.match(/^openflow:10[1-9]00([1-9])$/)[1]);
            var node;
            var flow;
            data.source = [];
            for (var i = 0; i<nodes.length; i++) {
                node = nodes[i];
                if (node.id.match(/^openflow:2.*/)) {
                    data[node.id] = {};
                    for (var j = 0; j<node.flows.length; j++) {
                        flow = node.flows[j];
                        // TODO This is configuration dependant.
                        if (flow.wavelength == lambda && flow.outPort.match(/^[24]$/)) {
                            data[node.id]['forward'] = flow.timeslot;
                        }
                        // TODO This is configuration dependant.
                        if (flow.wavelength == lambda && flow.outPort.match(/^[5678]$/)) {
                            data[node.id]['drop'] = flow.timeslot;
                        }
                    }
                }
                else if (node.id == sourceTor) {
                    for (var k = 0; k<node.flows.length; k++) {
                        flow = node.flows[k];
                        if (flow.inPort == sourceZone && flow.destination.includes(destIP)) {
                            data.source.push({node: node.id, time: flow.timeslot, outPort: flow.outPort});
                            if (data[node.id] === undefined) {
                                data[node.id] = {};
                            }
                            data[node.id][flow.outPort] = flow.timeslot;
                        }
                    }
                }
            }
            svc.emphasize(data, svc.getDetails(data, destPod));
            return data;
        };

        svc.getDetails = function(data, destPod) {
            var details = [];
            var time;
            var path;
            var flow;
            var plane;
            var next;
            for (var i = 0; i<data.source.length; i++) {
                path = [];
                flow = data.source[i];
                path.push(flow.node);
                plane = flow.outPort;
                time = flow.time;
                next = "openflow:20" + plane + "00" + flow.node.match(/^openflow:10([1-9])00[1-9]$/)[1];
                while (next !== null) {
                    flow = data[next];
                    path.push(next);
                    if (destPod == next.match(/^openflow:20[1-9]00([1-9])$/)[1]) {
                        next = null;
                        time = svc.intersect(time, flow.drop);
                    }
                    else {
                        time = svc.intersect(time, flow.forward);
                        next = svc.incrementNext(next);
                    }
                }
                details.push({time: time, path: path});
            }
            return details;
        };

        svc.incrementNext = function(next) {
            var plane = next.match(/^openflow:10([1-9])00[1-9]$/)[1];
            var current = parseInt(next.match(/^openflow:10[1-9]00([1-9])$/)[1]);
            var incremented = ((current) % svc.P) + 1;
            return "openflow:20" + plane + "00" + incremented.toString();
        };

        svc.intersect = function(str1, str2) {
            var result = "";
            for (var t = 0; t<svc.T; t++) {
                result += (str1[t] == "1" && str2[t] == "1") ? "1" : "0";
            }
            return result;
        };

        svc.emphasize = function(data, details) {
            var det;
            for (var j = 0; j<details.length; j++) {
                det = details[j];
                var node;
                for (var k = 0; k<det.path.length; k++){
                    node = det.path[k];
                    var direction;
                    if (k === (det.path.length -1)) {direction = 'drop';}
                    else if (k > 0) {direction = 'forward';}
                    else if (k === 0) {direction = det.path[1].match(/^openflow:20([1-9])00[1-9]$/)[1];}
                    data[node][direction] = svc.replaceChars(data[node][direction], det.time);
                }
            }
        };

        svc.replaceChars = function(str, boolStr) {
            var result = "";
            for (var i = 0; i<str.length; i++) {
                result += (boolStr[i] == 1) ? 2 : str[i]; // replaces with 2 all chars at indexes given by boolStr
            }
            return result;
        };

        svc.submitService = function(service) {
            return svc.affinity().all('connection').post(service);
        };

        svc.getConnections = function(cb) {
            return svc.affinity().all('connections').getList().then(function(connections) {
                cb(connections);
            }, function(response) {
            console.log("Error: " + response.data.message +" with status code " + response.status);
            cb(undefined);
            });
        };

        svc.getConnection = function(connectionID, cb) {
            return svc.affinity().all('connection').one(connectionID).get().then(function(connection) {
                cb(connection);
            }, function(response) {
            console.log("Error: " + response.data.message +" with status code " + response.status);
            cb(undefined);
            });
        };

        svc.deleteConnection = function(connectionID, cb) {
            return svc.affinity().all('connection').one(connectionID).remove().then(function(response) {
                cb("Connection deleted.");
            }, function(response) {
                cb("Error: " + response.data.message +" with status code " + response.status);
            });
        };


        svc.zones = function(){
            var result = [];
            for (var i = 0; i < svc.Z; i++) {
                result.push(i);
            }
            return result;
        };
        return svc;
    });
});
