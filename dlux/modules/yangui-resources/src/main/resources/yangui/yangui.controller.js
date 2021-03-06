define(['app/yangui/yangui.module', 'app/yangui/yangui.services', 'app/yangui/directives/abn_tree.directive', 'app/yangui/directives/sticky.directive', 'app/yangui/directives/read_file.directive', 'app/yangui/pluginHandler.services'], function(yangui) {

  yangui.register.controller('yanguiCtrl', ['$scope', '$timeout', '$rootScope', '$http', '$filter', 'YangUtilsRestangular', 'yangUtils', 'reqBuilder', 'custFunct',
    'pluginHandler', 'pathUtils', 'constants', 'nodeWrapper', 'mountPointsConnector', 'filterConstants','displayMountPoints','yinParser', 'designUtils', 'eventDispatcher', 'syncFact',
    'customFunctUnsetter', 'HistoryServices', 'dataBackuper',
    function ($scope, $timeout, $rootScope, $http, $filter, YangUtilsRestangular, yangUtils, reqBuilder, custFunctFact, pluginHandler, pathUtils, constants, nodeWrapper, mountPointsConnector, 
      filterConstants, displayMountPoints, yinParser, designUtils, eventDispatcher, syncFact, customFunctUnsetter, HistoryServices, dataBackuper) {
      $rootScope['section_logo'] = 'assets/images/logo_yangui.gif';

      $scope.currentPath = 'src/app/yangui/views/';
      $scope.apiType = '';
      $scope.constants = constants;
      $scope.filterConstants = filterConstants;
      $scope.filterRootNode = null;
      $scope.previewValidity = true;
      $scope.previewDelay = 2000;
      $scope.selCustFunct = null;
      $scope.selCustFunctButts = [];
      $scope.mpSynchronizer = syncFact.generateObj();
      $scope.baseMpApi = '';
      $scope.defaultTreeName = $filter('translate')('YANGUI_ROOT');
      $scope.treeName = $scope.defaultTreeName;

      $scope.status = {
          type: 'noreq',
          msg: null
      };

      $scope.mainTabs = {
        api: true,
        history : false,
        collection : false,
        parameters : false
      };

      $scope.showTabs = function(tabs, tabName){
        for(var prop in tabs){
          tabs[prop] = tabName === prop;
        }
      };

      var mountPrefix = constants.MPPREFIX;

      var statusChangeEvent = function(messages) {
          // var newMessage = $scope.status.rawMsg + '\r\n' + messages.join('\r\n');
          processingModulesCallback(messages[0]);
      };

      var fillPathIdentifiersByKey = function(inputs) {
          var node = inputs[0],
              value = inputs[1] || '';

          if($scope.selSubApi && node.parent && $scope.selSubApi.node.id === node.parent.id) { //or $scope.node === node.parent?
              var identifiers = $scope.selSubApi.pathArray[$scope.selSubApi.pathArray.length - 1].identifiers;
              pathUtils.fillIdentifiers(identifiers, node.label, value);
          }
      };

      var fillPathIdentifiersByListData = function(inputs) {
          var node = inputs[0];

          if($scope.selSubApi && node && $scope.selSubApi.node.id === node.id) { //or $scope.node === node.parent?
              var identifiers = $scope.selSubApi.pathArray[$scope.selSubApi.pathArray.length - 1].identifiers,
                  keys = node.refKey;

              keys.forEach(function(key) {
                  pathUtils.fillIdentifiers(identifiers, key.label, key.value);
              });
          }
      };

      var updateParameterizeData = function(req) {
          if((req.method === 'PUT' || req.method === 'POST') && req.data && $.isEmptyObject(req.data) === false) {
              req.parametrizedData = JSON.parse(req.data);
          }
      };

      $scope.parameterizeData = function(path) {
          var parameterList = null;
          
          $scope.$broadcast('GET_PARAMETER_LIST', function(parameters) {
              parameterList = parameters;
          });

          return HistoryServices.parametrizeData(parameterList.list, path);
      };

      eventDispatcher.registerHandler(constants.EV_SRC_MAIN, statusChangeEvent);
      eventDispatcher.registerHandler(constants.EV_FILL_PATH, fillPathIdentifiersByKey);
      eventDispatcher.registerHandler(constants.EV_LIST_CHANGED, fillPathIdentifiersByListData);

      var processingModulesCallback = function(e) {
          $scope.status = {
              isWorking: true,
              type: 'warning',
              msg: 'PROCESSING_MODULES',
              rawMsg: e || ''
          };
      };

      $scope.processingModulesSuccessCallback = function(e) {
          $scope.status = {
              type: 'success',
              msg: 'PROCESSING_MODULES_SUCCESS',
              rawMsg: e || ''
          };
      };

      $scope.processingModulesErrorCallback = function(e) {
          $scope.status = {
              type: 'danger',
              msg: 'PROCESSING_MODULES_ERROR',
              rawMsg: e || ''
          };
      };

      var requestWorkingCallback = function() {
          $scope.status = {
              isWorking: true,
              type: 'warning',
              msg: 'SEND_WAIT'
          };
      };

      var requestSuccessCallback = function() {
          $scope.status = {
              type: 'success',
              msg: 'SEND_SUCCESS'
          };
      };

      $scope.setStatusMessage = function(type, msg, e){
          $scope.status = {
              type: type,
              msg: msg,
              rawMsg: e || ''
          };
      };

      var requestErrorCallback = function(e, resp) {
        var errorMessages = yangUtils.errorMessages,
            msg = errorMessages.method[resp.config.method] ? errorMessages.method[resp.config.method][resp.status] ? errorMessages.method[resp.config.method][resp.status] : 'SEND_ERROR' : 'SEND_ERROR';

          $scope.status = {
              type: 'danger',
              msg: msg,
              rawMsg: e.toString()
          };
      };

      var setCustFunct = function(apis) {
          pluginHandler.plugAll($scope.apis, $scope);
      };
      
      $scope.removeMountPointPath = function(pathArray){
          var mpPathIndex = pathArray.length;

          pathArray.some(function(pathElem, index) {
              var isMPElem = pathElem.name === mountPrefix;
              if(isMPElem) {
                  mpPathIndex = index;
              }

              return isMPElem;
          });

          var pathCopy = pathArray.slice(0, mpPathIndex);
          return pathCopy;
      };

      $scope.invalidatePreview = function() {
          $scope.previewValidity = false;
      };

      $scope.validatePreview = function() {
          $scope.previewValidity = true;
      };

      $scope.isPreviewValid = function() {
          return $scope.previewValidity;
      };

      $scope.preview = function() {
          if($scope.isPreviewValid()) {
              $scope.invalidatePreview();

              $timeout(function () {
                  $scope.buildPreview();
                  $scope.validatePreview();
              }, $scope.previewDelay);
          }
      };

      $scope.buildPreview = function() {
          if($scope.node) {
              var reqString = $scope.selSubApi.buildApiRequestString(),
                  requestData = {};

              // create request
              $scope.node.buildRequest(reqBuilder, requestData);
              // update request data (remove envelope from POST request etc.)
              requestData = yangUtils.prepareRequestData(requestData, $scope.selectedOperation, reqString, $scope.selSubApi);

              var jsonRequestData = requestData ? JSON.stringify(requestData, null, 4) : '';
              // preview data
              $scope.previewValue = $scope.selApi.basePath + reqString;
              $scope.previewValue = $scope.previewValue + '\r\n' + jsonRequestData;
          } else {
              $scope.previewValue = '';
          }
      };

      $scope.getNodeName = function(localeLabel, label) {
          var localeResult = $filter('translate')(localeLabel);
          return localeResult.indexOf(constants.LOCALE_PREFIX) === 0 ? label : localeResult;
      };

      $scope.showCustFunctButton = function() {
          return $scope.selCustFunct === null;
      };

      $scope.showCustFunctCancelButton = function() {
          return $scope.selCustFunct !== null;
      };

      $scope.unsetCustomFunctionality = function() {
          if($scope.selCustFunct) {
              customFunctUnsetter.unset($scope.selCustFunct, $scope);
          }
          $scope.selCustFunct = null;
          $scope.treeName = $scope.defaultTreeName;
          $scope.selCustFunctButts = [];
      };

      var loadApis = function loadApis() {
          $scope.apis = [];
          $scope.allNodes = [];
          $scope.treeApis = [];
          $scope.augmentations = {};

          processingModulesCallback();
          yangUtils.generateNodesToApis(function(apis, allNodes, augGroups) {
              $scope.apis = apis;
              $scope.allNodes = allNodes;
              $scope.augmentations = augGroups;
              console.info('got data', $scope.apis, $scope.allNodes, $scope.augmentations);
              $scope.treeApis = yangUtils.generateApiTreeData(apis);
              console.info('tree api', $scope.treeApis);
              $scope.processingModulesSuccessCallback();

              setCustFunct($scope.apis);
              $scope.$broadcast('LOAD_REQ_DATA');
          }, function(e) {
              $scope.processingModulesErrorCallback(e);
          });
      };

      $scope.isMountPointSelected = function() {
          return $scope.selCustFunct.label === 'YANGUI_CUST_MOUNT_POINTS';
      };

      $scope.dismissStatus = function() {
          $scope.status = {};
      };

      $scope.setNode = function() {
          $scope.node = $scope.selSubApi.node;
      };

      $scope.setApiNode = function(indexApi, indexSubApi) {
          $scope.selectedOperation = null;

          if(indexApi !== undefined && indexSubApi !== undefined ) {
              $scope.selApi = $scope.apis[indexApi];
              $scope.selSubApi = $scope.selApi.subApis[indexSubApi];

              $scope.apiType = $scope.selSubApi.pathArray[0].name === 'operational' ? 'operational/':'';
              $scope.node = $scope.selSubApi.node;
              $scope.filterRootNode = $scope.selSubApi.node;
              $scope.node.clear();

              if($scope.selSubApi && $scope.selSubApi.operations) {
                  $scope.selectedOperation = $scope.selSubApi.operations[0];
              }
              $scope.$broadcast('EV_REFRESH_LIST_INDEX');
          } else {
              $scope.selApi = null;
              $scope.selSubApi = null;
              $scope.node = null;
          }
      };

      $scope.loadController = function() {
          $scope.flows = [];
          $scope.devices = [];
          $scope.apis = [];
          $scope.previewVisible = false;
          $scope.previewValue = '';
          $scope.popupData = { show: false};
          $scope.dataToFill = '';
          $scope.apiToFill = '';

          loadApis();

          $rootScope.$on('$includeContentLoaded', function() {
              designUtils.setDraggablePopups();
              //designUtils.getHistoryPopUpWidth();
          });
      };

      $scope.executeOperation = function(operation, callback, reqPath) {
          var reqString = $scope.selSubApi.buildApiRequestString(),
              requestData = {},
              preparedRequestData = {},
              headers = null;

          reqString = reqPath ? reqPath.slice($scope.selApi.basePath.length, reqPath.length) : reqString;
          var requestPath = $scope.selApi.basePath + reqString;

          $scope.node.buildRequest(reqBuilder, requestData);
          angular.copy(requestData, preparedRequestData);

          preparedRequestData = yangUtils.prepareRequestData(preparedRequestData, operation, reqString, $scope.selSubApi);
          
          operation = yangUtils.prepareOperation(operation);
          headers = yangUtils.prepareHeaders(preparedRequestData);

          requestWorkingCallback();

          YangUtilsRestangular.one('restconf').customOperation(operation.toLowerCase(), reqString, null, headers, preparedRequestData).then(
              function(data) {
                  if(operation === 'REMOVE'){
                      $scope.node.clear();
                  }

                  if(data) {
                      $scope.node.clear();
                      var props = Object.getOwnPropertyNames(data);

                      props.forEach(function(p) { //fill each property - needed for root mountpoint node, in other cases there should be only one property anyway
                          $scope.node.fill(p, data[p]);
                      });
                      $scope.node.expanded = true;
                  }
                  
                  requestSuccessCallback();
                  //TODO after first GET we have set $scope.node with data so build from the top of this function return requestData
                  if(operation === 'GET'){
                      requestData = {};
                  }
                  $scope.$broadcast('YUI_ADD_TO_HISTORY', 'success', data, preparedRequestData, operation, requestPath);

                  if ( angular.isFunction(callback) ) {
                      callback(data);
                  }

                  if($scope.previewVisible === true){
                      $scope.preview();
                  }

              }, function(resp) {
                  var errorMsg = '';

                  if(resp.data && resp.data.errors && resp.data.errors.error && resp.data.errors.error.length) {
                      errorMsg = ': ' + resp.data.errors.error.map(function(e) {
                          return e['error-message'];
                      }).join(', ');
                  }

                  requestErrorCallback(errorMsg, resp);
                  
                  //TODO after first GET we have set $scope.node with data so build from the top of this function return requestData
                  if(operation === 'GET'){
                      requestData = {};
                  }
                  $scope.$broadcast('YUI_ADD_TO_HISTORY', 'error', resp.data, preparedRequestData, operation, requestPath);

                  console.info('error sending request to',$scope.selSubApi.buildApiRequestString(),'reqString',reqString,'got',resp.status,'data',resp.data);
              }
          );
      };

      $scope.executeCustFunctionality = function(custFunct) {
          custFunct.runCallback($scope);
          $scope.selCustFunct = custFunct;
      };

      $scope.fillNodeData = function(pathElem, identifier) {
          if($scope.selSubApi && $scope.selSubApi.storage === 'config' &&
            $scope.selSubApi.pathArray.indexOf(pathElem) === ($scope.selSubApi.pathArray.length - 1)) {
              pathUtils.fillListNode($scope.node, identifier.label, identifier.value);
          }
      };

      $scope.showPreview = function() {
          $scope.previewVisible = true;
          $scope.buildPreview();
      };

      $scope.hidePreview = function() {
          $scope.previewVisible = false;
      };

      $scope.buildRoot = function() {
          $scope.node.buildRequest(reqBuilder, {});
      };

      $scope.changePathInPreview = function() {
          $scope.preview();
      };

      $scope.fillApiAndData = function(req) {
        updateParameterizeData(req);

        var path = req.parametrizedPath ? req.parametrizedPath : req.path,
            rdata = req.receivedData,
            sdata = (req.parametrizedData && !$.isEmptyObject(req.parametrizedData)) ? req.parametrizedData : req.sentData;

        if(path) {
            $scope.fillApi(path);
            
            if($scope.node && (rdata || sdata)) {
              if(rdata) {
                 $scope.fillApiData(rdata);
              }

              if(sdata) {
                 $scope.fillApiData(sdata); 
              }
            }
        }

        $scope.selectedOperation = req.method;
      };

      $scope.fillStandardApi = function(searchPath, fillPath) {
          fillPath = fillPath || searchPath;
          var apiIndexes = pathUtils.searchNodeByPath(searchPath, $scope.treeApis, $scope.treeRows);

          if(apiIndexes) {
              $scope.setApiNode(apiIndexes.indexApi, apiIndexes.indexSubApi);
              if($scope.selSubApi) {
                  pathUtils.fillPath($scope.selSubApi.pathArray, fillPath);
              }
          }
      };

      $scope.fillApi = function(path) {
          var parameterizedPath = $scope.parameterizeData(path);
              fillPath = parameterizedPath;

          if(parameterizedPath.indexOf(mountPrefix) !== -1) {
              fillPath = parameterizedPath.replace('restconf/config','restconf/operational');
          }

          $scope.fillStandardApi(fillPath);

          if(path.indexOf(mountPrefix) !== -1 && $scope.selSubApi) {
              $scope.selSubApi.pathArray = $scope.removeMountPointPath($scope.selSubApi.pathArray);
              $scope.selectMP();

              $scope.mpSynchronizer.waitFor(function () {
                  $scope.fillMPApi(parameterizedPath);
              });
          }
      };

      $scope.selectMP = function() {
          var mpCF = custFunctFact.getMPCustFunctionality($scope.selSubApi.custFunct);
          if(mpCF) {
              $scope.executeCustFunctionality(mpCF);
          } else {
              console.warn('Mountpoint custom functionality for api', $scope.selSubApi.buildApiRequestString(), ' is not set');
          }
      };

      $scope.fillMPApi = function(path) {
          var mpPath = mountPointsConnector.alterMpPath(path),
              apiIndexes = pathUtils.searchNodeByPath(mpPath, $scope.treeApis, $scope.treeRows);
          if(apiIndexes) {
              $scope.setApiNode(apiIndexes.indexApi, apiIndexes.indexSubApi);
              if($scope.selSubApi) {
                  pathUtils.fillPath($scope.selSubApi.pathArray, path);
              }
          }
      };

      $scope.fillApiData = function(data){
          var parametrizedData = $scope.parameterizeData(data),
              obj = null;

          obj = typeof parametrizedData === "object" ? parametrizedData : JSON.parse(parametrizedData);

          if (obj !== null && typeof obj === "object") {
              var p = Object.keys(obj)[0];
              $scope.node.fill(p, obj[p]);
          }
      };

      $scope.show_add_data_popup = function(){
          $scope.popupData.show = true;
      };

      $scope.close_popup = function(popObj){
          popObj.show = false;
      };

      $scope.tabs = function(event, index){
        var tabDom = $(event.target).closest('.tabs');

        tabDom.find(' > .tab-content').children('.tab-pane')
          .removeClass('active')
          .eq(index).addClass('active');

        tabDom.find('> .nav-tabs').children('li')
          .removeClass('btn-selected')
          .eq(index).addClass('btn-selected');
      };

      $scope.initMp = function(mountPointStructure, mountPointTreeApis, mountPointApis, baseMpApi, augmentations){
          dataBackuper.storeFromScope(['treeApis', 'treeRows', 'apis', 'node', 'selApi', 'selSubApi', 'augmentations'], $scope);
          $scope.filterRootNode = null;
          $scope.node = null;
          $scope.treeApis = mountPointTreeApis;
          $scope.apis = mountPointApis;
          $scope.processingModulesSuccessCallback();
          $scope.baseMpApi = baseMpApi;
          $scope.augmentations = augmentations;
          $scope.$broadcast('REFRESH_HISTORY_REQUEST_APIS');
      };

      $scope.showModalRequestWin = function(){
        $scope.$broadcast('LOAD_REQ_DATA');
      };

      $scope.$on('SET_SCOPE_TREE_ROWS', function(e, rows){
        $scope.treeRows = rows;
      });

      $scope.__test = {
          loadApis: loadApis,
          processingModulesErrorCallback: $scope.processingModulesErrorCallback,
          requestErrorCallback: requestErrorCallback,
          requestSuccessCallback: requestSuccessCallback,
          requestWorkingCallback: requestWorkingCallback,
          processingModulesCallback: processingModulesCallback,
          processingModulesSuccessCallback: $scope.processingModulesSuccessCallback
      };

      $scope.loadController();

    }]);

  yangui.register.controller('requestHistoryCtrl', ['$scope', '$rootScope','pathUtils','HistoryServices', 'handleFile', 'yangUtils', 'constants', 'mountPointsConnector', '$filter',
    function ($scope, $rootScope, pathUtils, HistoryServices, handleFile, yangUtils, constants, mountPointsConnector, $filter) {

    $scope.collectionBoxView = false;

    var mountPrefix = constants.MPPREFIX;

    var getApiCallback = function(pathString) {
        var normalizedPath = mountPointsConnector.alterMpPath(pathString), //if the path is for mountpoint then normalize it
            apiIndexes = pathUtils.searchNodeByPath(normalizedPath, $scope.treeApis, $scope.treeRows),
            selApi = apiIndexes ? $scope.apis[apiIndexes.indexApi] : null,
            selSubApi = selApi ? selApi.subApis[apiIndexes.indexSubApi] : null,
            copiedApi = selSubApi ? {pathArray : []} : null;

        if (selSubApi) {
            copiedApi = selSubApi.cloneWithoutNode();
                copiedApi.pathArray.forEach(function(p){
                p.hover = false;
            });

            pathUtils.fillPath(copiedApi.pathArray, normalizedPath);
        }
        return copiedApi;
    };

    $scope.requestList = HistoryServices.createEmptyHistoryList('requestList', getApiCallback);
    $scope.collectionList = HistoryServices.createEmptyCollectionList('collectionList', getApiCallback);
    $scope.parameterList = HistoryServices.createEmptyParamList('parameterList');
    $scope.parameterList.loadListFromStorage();
    
    $scope.popupHistory = { show: false};

    $scope.$on('REFRESH_HISTORY_REQUEST_APIS', function(event, callback){
        $scope.requestList.refresh();
        $scope.collectionList.refresh();
    });

        
    $scope.$on('GET_PARAMETER_LIST', function(event, callback){
        callback($scope.parameterList);
    });

    $scope.$on('YUI_ADD_TO_HISTORY', function(event, status, data, requestData, operation, requestPath) {
        $scope.addRequestToList(status, data, requestData, operation, requestPath);
    });

    $scope.addRequestToList = function(status, receivedData, sentData, operation, path) {
        if(typeof(Storage) !== "undefined") {

            var rList = HistoryServices.createEmptyHistoryList(),
                reqObj = HistoryServices.createHistoryRequest(sentData, receivedData, null, path, null, operation, status, null, null, getApiCallback);

            $scope.requestList.addRequestToList(reqObj);
            $scope.requestList.saveToStorage();

        }
    };
    
    $scope.reqHistoryFunc = function(){
        $scope.popupHistory.show = !$scope.popupHistory.show;

        $scope.requestList.loadListFromStorage();
        $scope.collectionList.loadListFromStorage();
        $scope.requestList.show = false;
    };

    $scope.show_history_data = function(req, sent, noData){
        req.sentDataView = sent;
        req.show = true;//!req.show;
        if ( !noData ) {
          req.setDataForView(sent);
        }
    };

    $scope.close_history_data = function(req){
        req.show = false;
    };

    $scope.saveParametrizedData = function(req, list){
        var parametrizedPath = req.api.parent.basePath + pathUtils.translatePathArray(req.api.clonedPathArray).join('/'),
            newReq = req.copyWithParametrizationAsNatural(parametrizedPath, getApiCallback),
            d = '';
        
        ParsingException = function(msg){
            this.msg = msg;
        };
        
        try{
            try{
                d = JSON.parse(req.data);
            }catch(e){
                throw new ParsingException(e.message);
            }
            newReq.sentData = d;

            req.clearParametrizedData();

            list.addRequestToList(newReq);
            list.saveToStorage();

            req.show = false;
            return true;
        }
        catch(e){
            if(e instanceof ParsingException){
                alert($filter('translate')('YANGUI_ERROR_PARSING_JSON') + ': ' + e.msg);
            }
            return false;
        }

    };

    $scope.showCollBox = function(req, edit){
      $scope.collectionBoxView = true;
      $scope.$broadcast('COLL_CLEAR_VAL_SET_REQ', req, edit);
    };
    

    $scope.hideCollBox = function(){
      $scope.collectionBoxView = false;
    };

    $scope.saveElemToList = function(elem) {
        $scope.collectionList.addRequestToList(elem);
        $scope.collectionList.saveToStorage();
    };

    $scope.saveParamToList = function(elem, oldElem) {
        $scope.parameterList.saveRequestToList(elem, oldElem);
    };

    $scope.deleteRequestItem = function(elem, list){
        $scope[list].deleteRequestItem(elem);
        $scope[list].saveToStorage();
    };

    $scope.clearHistoryData = function(list){
        $scope[list].clear();
        $scope[list].saveToStorage();
    };

    var clearFileInputValue = function() {
        var el = document.getElementById("upload-collection");
        el.value = '';
    };

    $scope.exportHistoryData = function() {
        var cListJSON = localStorage.getItem("collectionList");

        handleFile.downloadFile('requestCollection.json', cListJSON, 'json', 'charset=utf-8', function(){
            $scope.setStatusMessage('success', 'EXPORT_COLLECTIONS_SUCCESS');
        },function(e){
            $scope.setStatusMessage('danger', 'EXPORT_COLLECTIONS_ERROR', e);
            console.error('ExportCollection error:', e);
        });
    };

    $scope.readCollectionFromFile = function($fileContent) {
        var data = $fileContent;

        if(data && HistoryServices.validateCollectionFile(data)){
            try {
              $scope.collectionList.loadListFromFile(data);
              $scope.collectionList.saveToStorage();
              $scope.setStatusMessage('success', 'LOAD_COLLECTIONS_SUCCESS');
              clearFileInputValue();
            }catch(e) {
                clearFileInputValue();
                $scope.setStatusMessage('danger', 'PARSE_JSON_FILE_ERROR', e);
                console.error('DataStorage error:', e);
            }
        }else{
            $scope.setStatusMessage('danger', 'PARSE_JSON_FILE_ERROR');
            clearFileInputValue();
        }
    };

    $scope.executeCollectionRequest = function(req) {
        updateParameterizeData(req);

        var sdata = !$.isEmptyObject(req.parametrizedData) ? req.parametrizedData : req.sentData,
            path = req.parametrizedPath && req.show ? pathUtils.translatePathArray(req.api.clonedPathArray).join('/') : req.api.buildApiRequestString(),
            pathStrParts = path.split('/');

        path = $scope.parameterizeData(path);
        $scope.fillStandardApi(path, req.path);

        if(sdata) {
            $scope.fillApiData(sdata);
        }

        if($scope.baseMpApi) {
            pathStrParts.splice(1, 0, $scope.baseMpApi);
            path = pathStrParts.join('/');
        }

        var requestPath = req.api.parent.basePath + path;

        $scope.executeOperation(req.method, function(data){

            if ( !data &&  req.receivedData ){
              $scope.node.fill($scope.node.label,req.receivedData[$scope.node.label]);
            }
        }, requestPath);
    };

    $scope.fillRequestData = function(req, pathElem, identifier) {
        if(req.api && req.api.clonedPathArray.indexOf(pathElem) === (req.api.clonedPathArray.length - 1)) {
          var data = JSON.parse(req.data);
          pathUtils.fillListRequestData(data, pathElem.name, identifier.label, identifier.value);
          var strippedData = yangUtils.stripAngularGarbage(data, req.getLastPathDataElemName());

          angular.copy(strippedData, req.parametrizedData);
          req.data = JSON.stringify(strippedData, null, 4);
        }

        req.parametrizedPath = pathUtils.translatePathArray(req.api.clonedPathArray).join('/');
    };

    $scope.groupView = {};

    $scope.setGroupView = function(key) {
      $scope.groupView[key] = false;
    };

    $scope.toggleExpanded = function(key) {
        $scope.groupView[key] = !$scope.groupView[key];
    };

    $scope.$on('LOAD_REQ_DATA', function(){
      $scope.reqHistoryFunc();
    });

  }]);

  yangui.register.controller('collBoxCtrl', ['$scope','HistoryServices',function ($scope, HistoryServices) {
    
    $scope.collection = {
      name: '',
      group: ''
    };

    $scope.selectedRequest = null;
    $scope.editBox = false;

    $scope.addHistoryItemToColl = function(){
        var elemToAdd = $scope.selectedRequest.clone();

        HistoryServices.setNameAndGroup($scope.collection.name, $scope.collection.group, elemToAdd);
        $scope.saveElemToList(elemToAdd);

        if ( $scope.editBox ) {
          $scope.deleteRequestItem($scope.selectedRequest, 'collectionList');
        }

        $scope.hideCollBox();
    };

    $scope.moveHistoryItemToGroup = function(elem, event){
        var elemToMove = elem.clone();

        HistoryServices.setNameAndGroup($scope.collection.name, $scope.collection.group, elemToMove);
        $scope.saveElemToList(elemToMove);
        $scope.deleteRequestItem(elem, 'collectionList');
        $scope.hideCollBox();
    };

    $scope.$on('COLL_CLEAR_VAL_SET_REQ', function(e, req, edit){
        $scope.collection.name = edit ? req.name : '';
        $scope.collection.group = edit ? req.groupName : '';
        $scope.selectedRequest = req;
        $scope.editBox = edit;
    });

  }]);
  
  yangui.register.controller('historyParamCtrl',['$scope','HistoryServices', function($scope, HistoryServices){
          
    $scope.showParamBox = function(param){
        $scope.$broadcast('HISTORY_INIT_PARAM', param);
    };
    
    $scope.createNewParam = function(){
        return HistoryServices.createParameter('','');
    };
    
    
  }]);

  yangui.register.controller('paramBoxCtrl', ['$scope', 'HistoryServices', 'eventDispatcher', 'constants', function ($scope, HistoryServices, eventDispatcher, constants) {
    $scope.paramObj = null;
    $scope.oldParam = null;
    $scope.boxView = false;

    $scope.hideParamBox = function(){
        $scope.boxView = false;
        $scope.paramObj = $scope.createNewParam();
    };

    var addEditSuccessfull = function() {
        $scope.hideParamBox();
        $scope.parameterList.saveToStorage();
    };

    eventDispatcher.registerHandler(constants.EV_PARAM_EDIT_SUCC, addEditSuccessfull);

    $scope.saveParam = function(){
        $scope.saveParamToList($scope.paramObj, $scope.oldParam);
    };

    $scope.$on('HISTORY_INIT_PARAM', function(e, obj){
        if ( obj ){
            $scope.paramObj = obj.clone();
            $scope.oldParam = obj;
            $scope.boxView = true;
        }
    });
  }]);

  yangui.register.controller('leafCtrl', function ($scope) {
    var types = [
                  'binary', 
                  'bits', 
                  'boolean', 
                  'decimal64', 
                  'enumeration', 
                  'empty', 
                  'identityref', 
                  'instance-identifier', 
                  'int16', 
                  'int32', 
                  'int64', 
                  'int8', 
                  'leafref', 
                  'string', 
                  'uint16', 
                  'uint32', 
                  'uint64', 
                  'uint8', 
                  'union'
                ];

      $scope.getLeafType = function(){
        var label = $scope.node.getChildren('type')[0].label;
        return types.indexOf(label) !== -1 ? label : 'default';
      };

      $scope.displayValue = function() {
          return $scope.node.typeChild.label !== 'empty';
      };
  });

  yangui.register.controller('containerCtrl', function ($scope) {
      $scope.augModalView = false;

      $scope.toggleExpandedAugModal = function(){
        $scope.augModalView = !$scope.augModalView;
      };

      $scope.toggleExpanded = function() {
          $scope.node.expanded = !$scope.node.expanded;
      };
  });

  yangui.register.controller('caseCtrl', function ($scope) {
      $scope.empty = ($scope.case.children.length === 0 || ($scope.case.children.length === 1 && $scope.case.children[0].children.length ===0));

      $scope.augModalView = false;

      $scope.toggleExpandedAugModal = function(){
        $scope.augModalView = !$scope.augModalView;
      };
  });

  yangui.register.controller('augmentationModalCtrl', ['$scope', function($scope){
    $scope.init = function(node){
      $scope.node = node;
    };
  }]);

  yangui.register.controller('choiceCtrl', function ($scope, constants) {
    $scope.constants = constants;
    $scope.augModalView = false;

    $scope.toggleExpandedAugModal = function(){
      $scope.augModalView = !$scope.augModalView;
    };

    $scope.toggleExpanded = function() {
        $scope.node.expanded = !$scope.node.expanded;
    };

    $scope.caseShowing = function (node) {
      return !node.augmentationId ? true : $scope.augmentations.getAugmentation(node.parent, node.augmentationId).expanded;
    };
  });

  yangui.register.controller('rpcCtrl', function ($scope) {
    $scope.toggleExpanded = function() {
        $scope.node.expanded = !$scope.node.expanded;
    };
  });

  yangui.register.controller('inputCtrl', function ($scope) {
    $scope.augModalView = false;

    $scope.toggleExpandedAugModal = function(){
      $scope.augModalView = !$scope.augModalView;
    };

    $scope.toggleExpanded = function() {
        $scope.node.expanded = !$scope.node.expanded;
    };
  });

  yangui.register.controller('outputCtrl', function ($scope) {
    $scope.augModalView = false;

    $scope.toggleExpandedAugModal = function(){
      $scope.augModalView = !$scope.augModalView;
    };

    $scope.toggleExpanded = function() {
        $scope.node.expanded = !$scope.node.expanded;
    };
  });

  yangui.register.controller('listCtrl', function ($scope, listFiltering, nodeWrapper) {
      $scope.actElement = null;
      $scope.showModal = false;
      $scope.showListFilter = false;
      $scope.filterListHover = 0;
      $scope.currentDisplayIndex = 1;
      $scope.displayOffsets = [-1, 0, 1];
      $scope.augModalView = false;

      $scope.toggleExpandedAugModal = function(){
        $scope.augModalView = !$scope.augModalView;
      };

      $scope.$on('EV_REFRESH_LIST_INDEX', function(event) {
          $scope.currentDisplayIndex = 1;
      });

      $scope.addListElem = function() {
          $scope.showListFilter = false;
          $scope.showModal = false;
          listFiltering.removeEmptyFilters($scope.node);
          $scope.node.addListElem();
      };

      $scope.removeListElem = function(elemIndex,fromFilter) {
          $scope.node.removeListElem(elemIndex,fromFilter);
          $scope.preview();
          $scope.currentDisplayIndex = Math.max(Math.min($scope.currentDisplayIndex, $scope.node.listData.length - 2), 1);
      };

      $scope.toggleExpanded = function() {
          $scope.node.expanded = !$scope.node.expanded;
      };

      $scope.shiftDisplayNext = function(typeListData) {
          $scope.currentDisplayIndex = Math.min($scope.currentDisplayIndex + 3, $scope.node[typeListData].length - 2);
      };

      $scope.shiftDisplayPrev = function() {
          $scope.currentDisplayIndex = Math.max($scope.currentDisplayIndex - 3, 1);
      };

      $scope.showPrevButton = function() {
         return $scope.currentDisplayIndex > 1;
      };

      $scope.showNextButton = function(typeListData) {
         return $scope.node[typeListData] && $scope.currentDisplayIndex < $scope.node[typeListData].length - 2; //node is selected after view is loaded
      };

      $scope.showModalWin = function() {
        $scope.showModal = !$scope.showModal;
        if($scope.showListFilter){
            $scope.showListFilter = !$scope.showListFilter;
        }
      };

      $scope.showListFilterWin = function() {
          $scope.showListFilter = !$scope.showListFilter;
          if($scope.showModal){
              $scope.showModal = !$scope.showModal;
          }
          listFiltering.showListFilterWin($scope.filterRootNode,$scope.node);
      };

      $scope.getFilterData = function() {
          listFiltering.getFilterData($scope.node);
      };

      $scope.switchFilter = function(showedFilter) {
         listFiltering.switchFilter($scope.node,showedFilter);
      };

      $scope.createNewFilter = function() {
         listFiltering.createNewFilter($scope.node);
      };

      $scope.applyFilter = function() {
          listFiltering.applyFilter($scope.node);
          $scope.showListFilter = !$scope.showListFilter;
          $scope.currentDisplayIndex = 1;
          if($scope.node.filteredListData.length){
            $scope.node.doubleKeyIndexes = nodeWrapper.checkKeyDuplicity($scope.node.filteredListData,$scope.node.refKey);
          }else{
            $scope.node.doubleKeyIndexes = nodeWrapper.checkKeyDuplicity($scope.node.listData,$scope.node.refKey);
          }
      };

      $scope.clearFilterData = function(changeAct, filterForClear, removeFilters) {
        listFiltering.clearFilterData($scope.node,changeAct,filterForClear,removeFilters);
        if(changeAct){
            $scope.showListFilter = !$scope.showListFilter;
        }
        $scope.node.doubleKeyIndexes = nodeWrapper.checkKeyDuplicity($scope.node.listData,$scope.node.refKey);
      };

      $scope.activeFilter = function(filter) {
        if(filter.active == 1){
            filter.active = 2;
        }else{
            filter.active = 1;
        }
      };

      $scope.getListName = function(offset, config) {
        var createdListItemName = $scope.node.createListName($scope.currentDisplayIndex + offset);

        if ( createdListItemName.length > 33 ) {
          return {
            name: createdListItemName.substring(0,30) + '...',
            tooltip: createdListItemName
          };
        } else {
          return {
            name: config ? createdListItemName || '[' + ($scope.currentDisplayIndex + offset) + ']' : createdListItemName,
            tooltip: ''
          };
        }
      };

  });

  yangui.register.controller('leafListCtrl', function ($scope) {

      $scope.addListElem = function() {
          $scope.node.addListElem();
      };

      $scope.removeListElem = function(elem){
          $scope.node.removeListElem(elem);
      };

      $scope.changed = function() {
          $scope.preview();
      };

      $scope.toggleExpanded = function() {
          $scope.node.expanded = !$scope.node.expanded;
      };

  });

  yangui.register.controller('typeCtrl', function($scope){

    $scope.valueChanged = function(){
      if($scope.previewVisible) {
        $scope.preview();
      } else {
        $scope.buildRoot();
      }

      $scope.node.checkValueType();
      $scope.node.fill($scope.node.label, $scope.node.value);
    };

  });

  yangui.register.controller('filterTypeCtrl', function($scope){

    $scope.valueChanged = function(){
      $scope.node.checkValueType();
      $scope.node.fill($scope.node.label, $scope.node.value);
    };

  });

  yangui.register.controller('typeEmptyCtrl', function($scope){
    $scope.valueChanged = function(){
      $scope.type.setLeafValue($scope.type.emptyValue);

      if($scope.previewVisible) {
          $scope.preview();
      } else {
          $scope.buildRoot();
      }
    };
    
  });

  yangui.register.controller('typeEnumCtrl', function($scope){

    $scope.valueChanged = function(){
      var value = $scope.type.selEnum && $scope.type.selEnum.label ? $scope.type.selEnum.label : '';

      $scope.type.setLeafValue(value);

      if($scope.previewVisible) {
        $scope.preview();
      } else {
        $scope.buildRoot();
      }

      $scope.node.checkValueType();
      $scope.node.fill($scope.node.label, $scope.node.value);
    };
    
  });

yangui.register.controller('typeBitCtrl', function($scope){

    $scope.valueChanged = function(){
      $scope.type.setLeafValue($scope.type.bitsValues);

      if($scope.previewVisible) {
        $scope.preview();
      } else {
        $scope.buildRoot();
      }

      $scope.node.checkValueType();
      $scope.node.fill($scope.node.label, $scope.node.value);
    };
    
  });

  yangui.register.controller('filter', function($scope, listFiltering){
    $scope.isFilter = true;

    $scope.getFilterTypeArray = function(type){
      return listFiltering.getFilterTypeArray(type);
    };
  });

  yangui.register.controller('filterTypeEnumCtrl', function($scope){

    $scope.valueChanged = function(){
      var value = $scope.type.selEnum && $scope.type.selEnum.label ? $scope.type.selEnum.label : '';

      $scope.type.setLeafValue(value);

      $scope.node.checkValueType();
      $scope.node.fill($scope.node.label, $scope.node.value);
    };
    
  });

  yangui.register.controller('filterTypeBitCtrl', function($scope){

    $scope.valueChanged = function(){
      $scope.type.setLeafValue($scope.type.bitsValues,true);

      $scope.node.checkValueType();
      $scope.node.fill($scope.node.label, $scope.node.value);
    };
    
  });

  yangui.register.filter('onlyConfigStmts', function(nodeUtils){
        return function(nodes){
            
            if(nodes.length) {
                nodes = nodes.filter(function(n){
                    return nodeUtils.isOnlyOperationalNode(n);
                });
            }

            return nodes;
        };
    });

});
