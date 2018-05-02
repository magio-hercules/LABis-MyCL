var mysql_query = require('../db/db_query')();
var common = require('./common')();

exports.getUser = function(req, res) {
    var query = mysql_query.getUser();
	var obj = { id: req.query.id };
	common.doQuery(req, res, query, obj);
};
