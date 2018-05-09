var mysql_dbc = require('../db/db_con')();
var connection = mysql_dbc.init();

module.exports = function () {
    return {
        // doQuery : function(req, res, query, obj, callback) {
        doQuery : function(req, res, query, params, callback) {
            console.log("[INFO][doQuery] query :" + query);
            console.log("[INFO][doQuery] params :" + JSON.stringify(params));

            connection.query(query, params, function (error, result) {
                if (error) {
                    console.log('error :' + error);
                } else {
                    if (callback != undefined) {
                        console.log('[DEBUG] callback');
                        console.log(error);
                        console.log('------');
                        console.log(result);
                        console.log('------');
                        console.log(params);

                        callback(req, res, params, error, result);
                    } else {
                        console.log('[INFO][QUERY] result : ', result);
                        // res.writeHead(200, {'Content-Type':'text/html'});
                        res.writeHead(200, {'Content-Type': 'application/json'});
                        console.log('[INFO][QUERY] result end');
                        var jsonData = JSON.stringify(result);
                        res.end(jsonData);
                    }
                    
                }
                
                
            });
        },
        doRegister : function(req, res, query, user) {
            console.log("query :" + query);
            console.log("user :" + JSON.stringify(user));
        
            connection.query(query, user, function (error, result) {
                console.log("req.body : " + JSON.stringify(req.body));
            
                if (error) {
                    console.log("error ocurred",error);
                    res.send({
                        "code":400,
                        "failed":"error ocurred"
                    })
                } else {
                    console.log('[REGISTER] result : ', result);
                    res.writeHead(200, {'Content-Type': 'application/json'});
                    
                    // var jsonData = JSON.stringify(result);
                    // res.end(jsonData);
                    var response = {"result":"OK", "reason":"Register Success", "id":req.body.id};
                    console.log('[REGISTER] TEST : ', response);
                    res.end(JSON.stringify(response));
                }
            });
        },
    }
	
}