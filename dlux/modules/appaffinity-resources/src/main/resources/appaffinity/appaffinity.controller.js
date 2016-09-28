/*global define, console*/
define(['app/appaffinity/appaffinity.module', 'app/appaffinity/appaffinity.services'], function(appaff) {

        appaff.controller('appaffinityCtrl', function($scope, $rootScope, appaffinitySvc) {
            $rootScope['section_logo'] = 'assets/images/logo_network.gif';
            $scope.getText = function(text) {
                return text.innerText||text.textContent;
            };
            $scope.testvalue = 'success!'
            $scope.refresh = function() {
                appaffinitySvc.getTraffic(function(data) {
                    if (data){
                        $scope.nodes = data;
                    }
                });
            };
            $scope.const = {
                conntypes = [{name: 'Point to point', code: 'POINTTOPOINT'}]
            }
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
