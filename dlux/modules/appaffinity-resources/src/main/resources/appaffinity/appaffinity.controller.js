define(['app/appaffinity/appaffinity.module', 'app/appaffinity/appaffinity.services'], function(appaff) {

        appaff.register.controller('appaffinityCtrl', ['$scope', '$rootScope', 'appaffinitySvc', function($scope, $rootScope, appaffinitySvc) {

            $rootScope['section_logo'] = 'assets/images/logo_network.gif';

            $scope.refreshTraffic = function() {
                appaffinitySvc.getTraffic(function(data) {
                    if (data){
                        $scope.nodes = data;
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

            $scope.submitService = function() {
                var out_data = {connections: []};
                out_data.connections.push($scope.service);
                appaffinitySvc.submitService(out_data).then( function(response) {
                    alert("Request sent.");
                }, function(response) {
                    alert("Error " + response.status);
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

            //load data
            $scope.refreshTraffic();
            $scope.refreshFlows();
        }]);
    });
