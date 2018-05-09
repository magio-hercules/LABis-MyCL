var mysql_query = require('../db/db_query')();
var table = require('../db/db_table');
var common = require('./common')();

var bFirst = true;



// exports.getUser = function(req, res) {
//     var query = mysql_query.getUser();
// 	var obj = { id: req.query.id };
// 	common.doQuery(req, res, query, obj);
// };

exports.postUser = function(req, res) {
	console.log("[INFO] call postUser");

    var query = mysql_query.postUser();
	var params = [];

	query = _checkParams(query, params, req.body.id, table.User.id);

	bFirst = true;
	common.doQuery(req, res, query, params);
};


function _checkParams(query, params, val, str) {
	if (val != null && val != undefined) {
		if (bFirst) {
			query += " WHERE "; 
			bFirst = false;
		} else {
			query += " AND ";
		}		
		
		query = query + str + "=? ";
		params.push(val);
	}
	return query;
}