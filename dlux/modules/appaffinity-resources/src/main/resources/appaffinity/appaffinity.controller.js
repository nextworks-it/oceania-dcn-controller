/*global define, console*/
define(['app/appaffinity/appaffinity.module', 'app/appaffinity/appaffinity.services'], function(appaff) {

        appaff.controller('appaffinityCtrl', function($scope, $rootScope, appaffinitySvc) {

            $rootScope['section_logo'] = 'assets/images/logo_network.gif';

            $scope.refresh = function() {
                appaffinitySvc.getTraffic(function(data) {
                    if (data){
                        $scope.nodes = data;
                    }
                });
            };

            $scope.submitService = function() {
                appaffinitySvc.submitService($scope.service).then( function(response) {
                    alert("Request sent.");
                }, function(response) {
                    alert("Error " + response.status);
                });
            }

            //Defaults and constants
            $scope.const = {
                connTypes : [{name: 'Point to point', code: 'POINTTOPOINT'}],
                recoveryTypes : [{name: 'Unprotected', code: 'UNPROTECTED'}]
            };
            $scope.service = {};
            $scope.service.Connection_Type = 'POINTTOPOINT';
            $scope.service.Recovery = 'UNPROTECTED';

            //load data
            $scope.refresh();
        });
        /*
        appaff.filter('unique', function(){
            return function(collection, keyname) {
                var output = [];
                var keys = [];
                angular.forEach(collection, function(item) {
                    if(item.hasOwnProperty(keyname)) {
                        var value = item[keyname];
                        if (keys.valueOf().indexOf(value) < 0) {
                            keys.push(value);
                            output.push(item);
                        }
                    }
                });
                return output;
            }
        });
        */
    });
