var mysql_query = require('../db/db_query')();
var table = require('../db/db_table');
var common = require('./common')();

var bFirst = true;




exports.postFavorite = function(req, res) {
	console.log("[INFO] call postFavorite");

	var query = mysql_query.postFavorite();
	var params = [ ];

	query = _checkParams(query, params, req.body.user_id, table.Favorite.user_id);

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