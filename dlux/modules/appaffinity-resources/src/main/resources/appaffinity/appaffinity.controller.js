define(['app/appaffinity/appaffinity.module', 'app/appaffinity/appaffinity.services'], function(appaff) {

        appaff.register.controller('appaffinityCtrl', ['$scope', '$rootScope', 'appaffinitySvc', function($scope, $rootScope, appaffinitySvc) {

            $rootScope['section_logo'] = 'assets/images/logo_network.gif';

            $scope.refreshTraffic = function() {
                appaffinitySvc.getTraffic(function(data) {
                    if (data){
                        $scope.nodes = data;
                        $scope.nodes.rows = Object.keys(data).length;
                    }
                });
            };

            $scope.refreshFlows = function() {
                appaffinitySvc.getFlows(function(data) {
                    if (data){
                        $scope.nodeFlows = data;
                    }
                });
            };

            $scope.refreshConnections = function() {
                appaffinitySvc.getConnections(function(connections) {
                    $scope.profiles = connections;
                });
            };

            $scope.deleteConnection = function(connectionID) {
                appaffinitySvc.deleteConnection(connectionID, function(message) {
                    alert(message);
                    $scope.refreshConnections();
                });
            };

            $scope.refreshConnection = function(connectionID) {
                appaffinitySvc.getConnection(connectionID, function(connection) {
                    $scope.profile = connection;
                });
            };

            $scope.refreshGraphics = function() {
                if ($scope.nodeFlows === undefined) {
                    alert("There is no data available. Please reload.");
                    return;
                }
                if ($scope.grData === undefined) {
                    alert("Please specify the path to be visualized.");
                    return;
                }
                if ($scope.grData.srcTor === undefined) {
                    alert("Please specify a source ToR switch.");
                    return;
                }
                if ($scope.grData.srcZone === undefined) {
                    alert("Please specify a source zone.");
                    return;
                }
                if ($scope.grData.destTor === undefined) {
                    alert("Please specify the destination ToR switch.");
                    return;
                }
                if ($scope.grData.srcTor == $scope.grData.destTor) {
                    alert("Source and destination ToRs must be different.");
                    return;
                }

                $scope.grFlow = appaffinitySvc.graphics($scope.nodeFlows,
                                        $scope.grData.srcTor,
                                        $scope.grData.srcZone,
                                        $scope.grData.destTor);
            };

            $scope.submitService = function() {
                var out_data = {connections: []};
                out_data.connections.push($scope.service);
                appaffinitySvc.submitService(out_data).then( function(response) {
                    alert("Request sent.");
                    $scope.refreshConnections();
                }, function(response) {
                    alert("Error: " + response.data.message);
                });
            };

            //Defaults and constants
            $scope.const = {
                connTypes : [{name: 'Point to point', code: 'POINTTOPOINT'}],
                recoveryTypes : [{name: 'Unprotected', code: 'UNPROTECTED'}]
            };
            $scope.service = {};
            $scope.service.Connection_type = 'POINTTOPOINT';
            $scope.service.Recovery = 'UNPROTECTED';

            //Utility filter
            $scope.torFilter = function(node) {
               return /^openflow:1.*$/.test(node.id);
            };

            //load data
            $scope.refreshTraffic();
            $scope.refreshFlows();
            $scope.zoneList = appaffinitySvc.zones();
            $scope.refreshConnections();
        }]);
    });
