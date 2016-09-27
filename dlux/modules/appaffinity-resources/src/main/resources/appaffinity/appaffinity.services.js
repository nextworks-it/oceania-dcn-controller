/*global angular, define*/
define(['app/appaffinity/appaffinity.module'], function(prov) {
    prov.register.factory('appaffinityRestangular', function(Restangular, ENV) {
        return Restangular.withConfig(function(RestangularConfig) {
            RestangularConfig.setBaseUrl(ENV.getBaseURL("MD_SAL"));
        });
    });

    prov.register.factory('appaffinitySvc', function(appaffinityRestangular) {
        var svc = {
            config: function() {
                return appaffinityRestangular.one('restconf/config/opendaylight-inventory:nodes');
            }
        };
        return svc;
    });
});
