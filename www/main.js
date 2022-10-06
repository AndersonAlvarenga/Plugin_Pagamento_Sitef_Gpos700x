var exec = require('cordova/exec');


exports.getTitulo = function (success, error) {
    exec(success, error, 'MainActivity', 'getTitulo');
};

exports.imprimir = function (params, success, error) {
    exec(success, error, 'MainActivity', 'imprimir', [params]);
};

exports.pagamento = function (params, success, error) {
    exec(success, error, 'MainActivity', 'pagamento', [params]);
};
