var mysql_dbc = require('../db/db_con')();
var connection = mysql_dbc.init();

module.exports = function () {
    return {
        // doQuery : function(req, res, query, obj, callback) {
        doOnlyQuery : function(req, res, query, params, callback, bRet) {
            return new Promise(function(resolve, reject){
                console.log("[INFO][doOnlyQuery] query : " + query);
                console.log("[INFO][doOnlyQuery] params : " + JSON.stringify(params));
    
                connection.query(query, params, function (error, result, fields) {
                    if (error) {
                        console.log('[ERROR] error :' + error);
                        
                        var response = {"result":"NOK", "reason":"Query Failed", "id":req.body.id};
                        console.log('[INFO][doOnlyQuery] response : ', response);
                        res.end(JSON.stringify(response));

                        // for test
                        //throw err;
                        console.log(error);
                        // logger.info(error);
                        console.log('[doRequest] reject');
                        reject(error);
                    } else {
                        if (callback != undefined) {
                            console.log('[INFO][DEBUG] callback');
                            // console.log(error);
                            // console.log('------');
                            // console.log(result);
                            // console.log('------');
                            // console.log(params);
    
                            callback(req, res, params, error, result);
                        } else {
                            // console.log('[INFO][QUERY] result : ', result);
                            // console.log('[INFO][QUERY] result end');
                            var count = result.length ? result.length : result.changedRows;
                            console.log('[INFO][QUERY] result end (count: ' + count + ')');
    
                            console.log('[INFO][doRequest] !!! bRet : ' + bRet);
                            if (bRet == undefined || bRet != false) {
                                    // res.writeHead(200, {'Content-Type':'text/html'});
                                // res.writeHead(200, {'Content-Type': 'application/json'});
                                // var jsonData = JSON.stringify(result);
                                // res.end(jsonData);
                                console.log('jsonData : ' + JSON.stringify(result));
                            }
                            // console.log('[INFO][TEST] fields: ' + JSON.stringify(fields));
                            resolve(result, fields, response);
                        }
                    }
                });
            });
        },
        doQuery : function(req, res, query, params, callback, bRet) {
            return new Promise(function(resolve, reject){
                console.log("[INFO][doQuery] query : " + query);
                console.log("[INFO][doQuery] params : " + JSON.stringify(params));
    
                connection.query(query, params, function (error, result, fields) {
                    if (error) {
                        console.log('[ERROR] error :' + error);
                        
                        var response = {"result":"NOK", "reason":"Query Failed", "id":req.body.id};
                        console.log('[INFO][doQuery] response : ', response);
                        res.end(JSON.stringify(response));

                        // for test
                        //throw err;
                        console.log(error);
                        // logger.info(error);
                        console.log('[doRequest] reject');
                        reject(error);
                    } else {
                        if (callback != undefined) {
                            console.log('[INFO][DEBUG] callback');
                            // console.log(error);
                            // console.log('------');
                            // console.log(result);
                            // console.log('------');
                            // console.log(params);
    
                            callback(req, res, params, error, result);
                        } else {
                            // console.log('[INFO][QUERY] result : ', result);
                            // console.log('[INFO][QUERY] result end');
                            var count = result.length ? result.length : result.changedRows;
                            console.log('[INFO][QUERY] result end (count: ' + count + ')');
    
                            console.log('[INFO][doRequest] !!! bRet : ' + bRet);
                            if (bRet == undefined || bRet != false) {
                                 // res.writeHead(200, {'Content-Type':'text/html'});
                                res.writeHead(200, {'Content-Type': 'application/json'});
                                var jsonData = JSON.stringify(result);
                                res.end(jsonData);
                            }
                            // console.log('[INFO][TEST] fields: ' + JSON.stringify(fields));
                            resolve(result, fields, response);
                        }
                    }
                });
            });
        },
        doRequest : function(req, res, query, params, bRet) {
            return new Promise(function(resolve, reject){
                console.log("[INFO][doRequest] query : " + query);
                console.log("[INFO][doRequest] params : " + JSON.stringify(params));
            
                connection.query(query, params, function (error, result, fields) {
                    if (error) {
                        console.log("[ERROR] error ocurred",error);
                        
                        var response = {"result":"NOK", "reason":"Register Failed", "id":req.body.id};
                        console.log('[INFO][doRequest] response : ', response);
                        res.end(JSON.stringify(response));
    
                        // for test
                        //throw err;
                        console.log(error);
                        // logger.info(error);
                        console.log('[doRequest] reject');
                        reject(error);
                    } else {
                        console.log('[INFO][doRequest] !!! bRet : ' + bRet);
                        if (bRet == undefined || bRet != false) {
                            res.writeHead(200, {'Content-Type': 'application/json'});
                            var response = {"result":"OK", "reason":"Register Success", "id":req.body.id};
                            console.log('[INFO][doRequest] response : ', response);
                            res.end(JSON.stringify(response));
                        }
			            // console.log('[INFO][TEST] fields: ' + JSON.stringify(fields));
                        resolve(result, fields, response);
                    }
                });
            });
        },
    }
	
}