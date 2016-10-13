define(['angularAMD', 'app/routingConfig', 'app/core/core.services', 'Restangular', 'common/config/env.module'], function (ng) {
        
        var appaff = angular.module('app.appaffinity', ['ui.router.state', 'app.core', 'restangular', 'config']);

        appaff.config(function($controllerProvider, $compileProvider, $provide, $stateProvider, NavHelperProvider) {
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
                "link": "#/appaffinity/create",
                "active": "appaffinity",
                "title": "Application Affinity",
                "icon": "icon-exchange",
                "page": {
                    "title": "Application Affinity",
                    "description": "Application Affinity Client"
                }
            });
            $stateProvider.state('main.appaffinity.traffic', {
                url: '/traffic',
                access: access.public,
                views: {
                    '': {
                        templateUrl: 'src/app/appaffinity/traffic.tpl.html',
                        controller: 'appaffinityCtrl'
                    }
                }
            });
            $stateProvider.state('main.appaffinity.create', {
                url: '/create',
                access: access.public,
                views: {
                    '': {
                        templateUrl: 'src/app/appaffinity/create.tpl.html',
                        controller: 'appaffinityCtrl'
                    }
                }
            });
            $stateProvider.state('main.appaffinity.manage', {
                url: '/manage',
                access: access.public,
                views: {
                    '': {
                        templateUrl: 'src/app/appaffinity/manage.tpl.html',
                        controller: 'appaffinityCtrl'
                    }
                }
            });
            $stateProvider.state('main.appaffinity.flow', {
                url: '/flows',
                access: access.public,
                views: {
                    '': {
                        templateUrl: 'src/app/appaffinity/flow.tpl.html',
                        controller: 'appaffinityCtrl'
                    }
                }
            });
            $stateProvider.state('main.appaffinity.path', {
                url: '/path',
                access: access.public,
                views: {
                    '': {
                        templateUrl: 'src/app/appaffinity/path.tpl.html',
                        controller: 'appaffinityCtrl'
                    }
                }
            });
        });
        return appaff;
    });
