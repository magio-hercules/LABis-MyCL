var express = require('express');
var router = express.Router();

// controller
var controller_user = require('../controllers/user');
var controller_contents = require('../controllers/contents');
var controller_genre = require('../controllers/genre');
var controller_favorite = require('../controllers/favorite');
var controller_prefer = require('../controllers/prefer');

// for db
// var mysql_query = require('../db/db_query')();
// var mysql_dbc = require('../db/db_con')();
// var connection = mysql_dbc.init();
// mysql_dbc.test_open(connection);





// middleware that is specific to this router
router.use(function timeLog(req, res, next) {
	// console.log('Time: ', Date.now());
	next();
});

// router.get('/', function(req, res) {
// 	res.redirect('/mycl');
// });

router.get('/', function(req, res) {
	res.send('Welcome to My Contents List');
});


router.get('/user', controller_user.getUser);

router.get('/contents', controller_contents.getContents);

router.get('/genre', controller_genre.getGenre);

router.get('/favorite', controller_favorite.getFavorite);

router.get('/prefer', controller_prefer.getPrefer);




// function doQuery(req, res, query, obj) {
// 	console.log("query :" + query);
// 	console.log("obj :" + JSON.stringify(obj));

// 	connection.query(query, [obj.id, obj.gen_id], function (err, result) {
// 		console.log(result);
	
// 		if (err) {
// 			console.log('err :' + err);
// 		} else {
// 			var jsonData = JSON.stringify(result);
	
// 			// res.writeHead(200, {'Content-Type':'text/html'});
// 			res.writeHead(200, {'Content-Type': 'application/json'});
// 			res.end(jsonData);
// 		}
// 	});
// }




module.exports = router;