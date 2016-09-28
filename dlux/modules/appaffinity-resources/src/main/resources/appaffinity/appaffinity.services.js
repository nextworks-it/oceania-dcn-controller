/*global angular, define*/
define(['app/appaffinity/appaffinity.module'], function(appaff) {

    appaff.factory('ODLRest', function(Restangular, ENV) {
        return Restangular.withConfig(function(RestangularConfig) {
            RestangularConfig.setBaseUrl(ENV.getBaseURL("MD_SAL"));
        });
    });

    appaff.factory('AARest', function(Restangular) {
        return Restangular.withConfig(function(RestangularConfig) {
          var url = window.location.protocol+"//"+window.location.hostname+":"+"8089/";
          RestangularConfig.setBaseUrl(url);
        });
      });

    appaff.factory('appaffinitySvc', function(AARest, ODLRest) {
        var svc = {

            // TODO: determine them someway and cut this out!
            P : 3,
            W : 2,

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
                nodes = {};
                var i,j;
                var n = mat.length;
                for (i = 0; i < n; i++) {
                    for (j = 0; j < n; j++) {
                        if (mat[i][j] !== 0){

                            var pSrc = Math.floor(i/W),
                            wSrc = i % W,
                            srcId = 'p' + pSrc + '-w' + wSrc;

                            var pDes = Math.floor(j/W),
                            wDes = j % W,
                            desId = 'p' + pDes + '-w' + wDes;

                            if (nodes[srcId] === undefined) {
                                nodes[srcId] = {id:srcId, incoming: [], outgoing: []};
                            }
                            if (nodes[desId] === undefined) {
                                nodes[desId] = {id:desId, incoming: [], outgoing: []};
                            }
                            nodes[srcId].outgoing.push([desId, mat[i][j]]);
                            nodes[desId].incoming.push([srcId, mat[i][j]]);
                        }
                    }
                }
                cb(nodes);
            },
            function(response) {
                console.log("Error with status code ", response.status);
                cb(undefined); //sends null data to the controller since the call failed
            });
        };

        svc.submitService = function(service) {
            return svc.affinity().one('connection').post(service)
        }
        return svc;
    });
});
