/*global define, console*/
define(['app/appaffinity/appaffinity.module', 'jquery', 'footable', 'app/appaffinity/appaffinity.services',
        'common/general/common.general.filters'],
    function(prov) {
        prov.register.controller('appaffinityCtrl', function($scope, $rootScope) {
            $rootScope['section_logo'] = 'assets/images/logo_provision.gif';
            $scope.getText = function(text) {
                return text.innerText||text.textContent;
            };
            $scope.testvalue = 'success!'
        });

        prov.filter('unique', function(){
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
    });
