/*global angular, define, routingConfig*/
define(['angularAMD', 'Restangular', 'app/routingConfig', 'common/general/finishRender.module', 'app/core/core.services',
        'common/general/common.general.services', 'common/config/env.module'],
    function () {
        var appaff = angular.module('app.appaffinity', ['ui.router.state', 'app.core', 'restangular', 'app.common.finishRender','app.common.general', 'config']);

        appaff.config(function($controllerProvider, $compileProvider, $provide, $stateProvider, $translateProvider, NavHelperProvider) {
            appaff.register = {
                controller : $controllerProvider.register,
                directive : $compileProvider.directive,
                service : $provide.service,
                factory : $provide.factory
            };

            var access = routingConfig.accessLevels;
            $stateProvider.state('main.appaffinity', {
                url: 'appaffinity',
                views : {
                    'content' : {
                        templateUrl: 'src/app/appaffinity/root.tpl.html',
                        controller: 'appaffinityCtrl'
                    }
                },
                abstract: true
            });

            NavHelperProvider.addControllerUrl('app/appaffinity/appaffinity.controller');
            NavHelperProvider.addToMenu('appaffinity', {
                "link": "#/appaffinity/index",
                "active": "appaffinity",
                "title": "Application Affinity",
                "icon": "icon-exchange",
                "page": {
                    "title": "Application Affinity",
                    "description": "Application Affinity Client"
                }
            });

            // Show cross-connections
            $stateProvider.state('main.appaffinity.index', {
                url: '/index',
                access: access.public,
                views: {
                    '': {
                        templateUrl: 'src/app/appaffinity/index.tpl.html',
                        controller: 'appaffinityCtrl'
                    }
                }
            });
            // Create cross-connection
            $stateProvider.state('main.appaffinity.xconncreate', {
                url: '/appaffinity-create',
                access: access.public,
                views: {
                    '': {
                        templateUrl: 'src/app/appaffinity/create.tpl.html',
                        controller: 'appaffinityCtrl'
                    }
                }
            });
            // Delete cross-connection
            $stateProvider.state('main.appaffinity.xconndelete', {
                url: '/appaffinity-delete',
                access: access.public,
                views: {
                    '': {
                        templateUrl: 'src/app/appaffinity/delete.tpl.html',
                        controller: 'appaffinityCtrl'
                    }
                }
            });
        });
        return appaff;
    });
