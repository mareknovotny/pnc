/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

(function() {

  var module = angular.module('pnc.websockets', [
    'angular-websocket'
  ]);

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider.state('websockets', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      }
    });

    $stateProvider.state('websockets.list', {
      url: '/websockets',
      templateUrl: 'websockets/views/websockets.html',
      controller: 'WebSocketsController'
    });

  }]);

  module.factory('MyData', ['$websocket', 'Notifications', function ($websocket, Notifications) {
    var dataStream = $websocket('ws://localhost:8080/pnc-rest/ws/build-records/notifications');
    dataStream.onMessage(function(message) {
      Notifications.success('WS response ' + message.data);
    });

  }]);

})();
