var mysql_query = require('../db/db_query')();
var common = require('./common')();

exports.getGenre = function(req, res) {
    var query = mysql_query.getGenre();
	var obj = { id: req.query.id };
	common.doQuery(req, res, query, obj);
};
