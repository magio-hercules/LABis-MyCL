var mysql_dbc = require('../db/db_con')();
var connection = mysql_dbc.init();

module.exports = function () {
    return {
        // doQuery : function(req, res, query, obj, callback) {
        doQuery : function(req, res, query, params, callback) {
            console.log("[INFO][doQuery] query : " + query);
            console.log("[INFO][doQuery] params : " + JSON.stringify(params));

            connection.query(query, params, function (error, result) {
                if (error) {
                    console.log('[ERROR] error :' + error);
                    
                    var response = {"result":"NOK", "reason":"Query Failed", "id":req.body.id};
                    console.log('[INFO][doQuery] response : ', response);
                    res.end(JSON.stringify(response));
                } else {
                    if (callback != undefined) {
                        console.log('[INFO][DEBUG] callback');
                        console.log(error);
                        console.log('------');
                        console.log(result);
                        console.log('------');
                        console.log(params);

                        callback(req, res, params, error, result);
                    } else {
                        // console.log('[INFO][QUERY] result : ', result);
                        // console.log('[INFO][QUERY] result end');
                        var count = result.length ? result.length : result.changedRows;
                        console.log('[INFO][QUERY] result end (count: ' + count + ')');

                        // res.writeHead(200, {'Content-Type':'text/html'});
                        res.writeHead(200, {'Content-Type': 'application/json'});
                        var jsonData = JSON.stringify(result);
                        res.end(jsonData);
                    }
                }
            });
        },
        doRequest : function(req, res, query, params) {
            console.log("[INFO][doRequest] query : " + query);
            console.log("[INFO][doRequest] params : " + JSON.stringify(params));
        
            connection.query(query, params, function (error, result) {
                if (error) {
                    console.log("[ERROR] error ocurred",error);
                    
                    var response = {"result":"NOK", "reason":"Register Failed", "id":req.body.id};
                    console.log('[INFO][doRequest] response : ', response);
                    res.end(JSON.stringify(response));
                } else {
                    res.writeHead(200, {'Content-Type': 'application/json'});
                    var response = {"result":"OK", "reason":"Register Success", "id":req.body.id};
                    console.log('[INFO][doRequest] response : ', response);
                    res.end(JSON.stringify(response));
                }
            });
        },
    }
	
}