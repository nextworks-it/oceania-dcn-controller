define(['app/appaffinity/appaffinity.module', 'vis'], function(appaff, vis) {

  appaff.register.directive('flowsTopology', function() {
    // constants
    var width = 800,
      height = 800;

    return {
      restrict: 'E',
      scope: {
          graphicsData: '='
      },
      link: function($scope, iElm, iAttrs, controller) {

          $scope.$watch('graphicsData', function (ntdata) {
              if(ntdata){
                  //   visinit(inNodes, inEdges, container, inOptions) {
                  var inNodes = $scope.graphicsData.nodes;
                  var inEdges = $scope.graphicsData.links;
                  var container = iElm[0];

                  // legend moved to topology controller

                  var data = {
                      nodes: inNodes,
                      edges: inEdges
                  };

                  var color = '#66FFFF',
                      hl = '#0066FF',
                      hover = '#33CC33',
                      BLACK = '#2B1B17';

                  var options = {
                      physics: {hierarchicalRepulsion: {nodeDistance: 70}},
                      hierarchicalLayout: {direction: 'RL'},
                      width:  '80%',
                      height: '500px',
                      nodes: {
                          widthMin: 20,
                          widthMax: 64,
                          fontColor: BLACK
                      },
                      edges: {
                          length: 150,
                          color: {
                              color: '#070707',
                              highlight: hl,
                              hover: hover
                          }
                      },
//                      edges: {
//                          smooth: {
//                              type: 'cubicBezier',
//                              forceDirection: 'vertical',
//                              roundness: 0.4,
//                              color: {
//                                  color: '#070707',
//                                  highlight: hl,
//                                  hover: hover
//                              }
//                          }
//                      },
//                      physics: {
//                          barnesHut: {
//                              gravitationalConstant: -7025
//                          }
//                      },
//                      hover: false,
                      groups: {
                          'switch': {
                              shape: 'image',
                              image: 'assets/images/Device_switch_3062_unknown_64.png'
                          },
                          'host': {
                              shape: 'image',
                              image: 'assets/images/Device_pc_3045_default_64.png'
                          }
                      },
                      keyboard:true,
                      tooltip: {
                          delay: 300,
                          fontColor: "black",
                          fontSize: 14, // px
                          fontFace: "verdana",
                          color: {
                              border: "#666",
                              background: "#FFFFC6"
                          }
                      }

                      //smoothCurves: true
                      //stabilizationIterations: (inNodes.length > 30 ? inNodes.length * 10 : 1000),
                      //freezeForStabilization: true
                  };

                  var graph = new vis.Graph(container, data, options);
                  return graph;
              }
          });

      }
    };
  });
});
