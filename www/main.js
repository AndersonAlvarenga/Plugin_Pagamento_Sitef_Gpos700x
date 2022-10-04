var exec = require('cordova/exec');

exports.checarPagamento = function (success, error) {
    exec(success, error, 'MainActivity', 'checarPagamento');
};

exports.imprimir = function (params, success, error) {
    exec(success, error, 'MainActivity', 'imprimir', [params]);
};

exports.pagamento = function (success, error) {
    exec(success, error, 'MainActivity', 'pagamento');
};
