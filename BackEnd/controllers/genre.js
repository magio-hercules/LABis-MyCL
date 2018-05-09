var mysql_query = require('../db/db_query')();
var table = require('../db/db_table');
var common = require('./common')();

var bFirst = true;



exports.getTotalGenre = function(req, res) {
	console.log("[INFO] call getTotalGenre");
	// console.log("req.query : " + JSON.stringify(req.query));
	var query = mysql_query.getTotalGenre();
	common.doQuery(req, res, query);
};

// exports.getGenre = function(req, res) {
//     var query = mysql_query.getGenre();
// 	var obj = { id: req.query.id };
// 	common.doQuery(req, res, query, obj);
// };

exports.postGenre = function(req, res) {
	console.log("[INFO] call postGenre");

    var query = mysql_query.postGenre();
	var params = [];

	query = _checkParams(query, params, req.body.id, table.Genre.id);
	
	bFirst = true;
	common.doQuery(req, res, query, obj);
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