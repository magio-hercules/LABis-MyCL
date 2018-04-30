var mysql_dbc = require('../db/db_con')();
var connection = mysql_dbc.init();

module.exports = function () {
    return {
        doQuery : function(req, res, query, obj) {
            console.log("query :" + query);
            console.log("obj :" + JSON.stringify(obj));
        
            connection.query(query, [obj.id, obj.gen_id], function (err, result) {
                console.log(result);
            
                if (err) {
                    console.log('err :' + err);
                } else {
                    var jsonData = JSON.stringify(result);
            
                    // res.writeHead(200, {'Content-Type':'text/html'});
                    res.writeHead(200, {'Content-Type': 'application/json'});
                    res.end(jsonData);
                }
            });
        }
    }
	
}