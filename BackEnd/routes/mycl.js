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

/////////
// GET //
/////////
// router.get('/user',				controller_user.getUser);
// router.get('/contents', 		controller_contents.getContents);
router.get('/total_contents', 	controller_contents.getTotalContents);
router.get('/total_genre', 		controller_genre.getTotalGenre);

//////////
// POST //
//////////
router.post('/login', 					controller_login.postLogin);
router.post('/register', 				controller_login.postRegister);
router.post('/user',					controller_user.postUser);
router.post('/genre', 					controller_genre.postGenre);
router.post('/favorite', 				controller_favorite.postFavorite);
router.post('/prefer', 					controller_prefer.postPrefer);

router.post('/total_contents', 			controller_contents.postTotalContents);
router.post('/my_contents', 			controller_contents.postMyContents);

router.post('/insert_my_contents', 		controller_contents.postInsertMyContents);
router.post('/update_my_contents', 		controller_contents.postUpdateMyContents);
router.post('/filter_my_contents', 		controller_contents.postFilterMyContents);
router.post('/delete_my_contents', 		controller_contents.postDeleteMyContents);

router.post('/insert_contents_list', 	controller_contents.postInsertContentsList);
router.post('/filter_contents_jenre',   controller_contents.postFilterContentsJenre);

router.post('/non_auth_contents_list', 	controller_contents.postNonAuthContentsList);
router.post('/set_auth_contents_list', 	controller_contents.postSetAuthContentsList);

router.post('/update_contents_image',	controller_contents.postUpdateContentsImage);

router.post('/search_all_contents',		controller_contents.postSearchAllContents);
router.post('/search_my_contents', 		controller_contents.postSearchMyContents);

module.exports = router;