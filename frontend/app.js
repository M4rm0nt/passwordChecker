angular.module('passwordCheckerApp', [])
    .controller('PasswordController', ['$http', '$sce', function($http, $sce) { // $sce hinzugefügt
        var vm = this;
        vm.password = '';
        vm.passwordFeedback = '';

        vm.checkPassword = function() {
            $http.post('http://localhost:8000/validatePassword', {password: vm.password})
                .then(function(response) {
                    vm.passwordFeedback = $sce.trustAsHtml(response.data.message);
                }, function(error) {
                    vm.passwordFeedback = $sce.trustAsHtml(error.data.error ? error.data.error : 'Fehler bei der Überprüfung des Passworts.');
                });
        };
    }]);
