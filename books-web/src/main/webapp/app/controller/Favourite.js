'use strict';

/**
 * Service controller.
 */
App.controller('Favourite', function ($scope, $timeout, Restangular, $stateParams) {
    // Initialization
    $scope.contentType = null;
    $scope.results = [];

    $scope.resetResults = function () {
        $scope.results = [];
    };

    $scope.selectContentType = function (contentType) {
        $scope.resetResults();
        $scope.contentType = contentType;
        console.log($scope.contentType);
        $scope.fetchResults();
    }

    $scope.removeFavourite = function (audioBookId) {
        console.log("Adding book to favourite", audioBookId)

        if ($scope.contentType == 'Audio Books') {
            Restangular.one('service/favourite/audioBook', audioBookId).remove().then(() => {
                $scope.fetchResults();
            })
        } else {
            Restangular.one('service/favourite/podcast', audioBookId).remove().then(() => {
                $scope.fetchResults();
            })
        }
    }

    $scope.fetchResults = function () {

        if ($scope.contentType == 'Audio Books') {
            // Put your code here
            Restangular.one('service/favourite/audioBook').get().then(function (response) {

                $scope.results = response

            }, function (error) {
                // Handle error
                console.error("Error fetching resource:", error);
            });
        } else {
            Restangular.one('service/favourite/podcast').get().then(function (response) {

                $scope.results = response

            }, function (error) {
                // Handle error
                console.error("Error fetching resource:", error);
            });
        }
    };

    // Other existing functions and variables...
});
