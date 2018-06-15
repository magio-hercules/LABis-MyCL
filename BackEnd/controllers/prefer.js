var mysql_query = require('../db/db_query')();
var table = require('../db/db_table');
var common = require('./common')();

var bFirst = true;




exports.postPrefer = function(req, res) {
	console.log("[====] call postPrefer");

	var query = mysql_query.postPrefer();
	var params = [ ];

	query = _checkParams(query, params, req.body.user_id, table.Prefer.user_id);

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