var express = require('express');
var router = express.Router();


// controller
var controller_login	= require('../controllers/login');
var controller_user 	= require('../controllers/user');
var controller_contents = require('../controllers/contents');
var controller_genre 	= require('../controllers/genre');
var controller_favorite = require('../controllers/favorite');
var controller_prefer 	= require('../controllers/prefer');


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

// POST
router.post('/login', 		controller_login.postLogin);
router.post('/register', 	controller_login.postRegister);

// GET
router.get('/user',			controller_user.getUser);
router.get('/contents', 	controller_contents.getContents);
router.get('/genre', 		controller_genre.getGenre);
router.get('/favorite', 	controller_favorite.getFavorite);
router.get('/prefer', 		controller_prefer.getPrefer);




module.exports = router;