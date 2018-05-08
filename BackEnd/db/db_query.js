module.exports = function () {
    return {
        // POST
        postLogin: function () {
            return 'SELECT * FROM User WHERE `id`=?';
        },
        postRegister: function () {
            return 'INSERT INTO User SET ?';
        },
        postContents: function () {
            return 'SELECT * FROM Contents_my';
        },
        postMyContents: function () {
            // MyCL/my_contents?user_id=khercules
           return 'SELECT * FROM Contents_my JOIN Contents_list USING(id, gen_id, season) where `user_id`=?';
       },
        

        // GET
        getUser: function () {
             // MyCL/user?id=khercules
            return 'SELECT * FROM User WHERE `id`=?';
        },
        getContents: function () {
             // MyCL/contents?id=khercules&gen_id=A01
            // return 'SELECT * FROM Contents_my WHERE `id`=? AND `gen_id`=?';
            return 'SELECT * FROM Contents_my';
        },
        getTotalContents: function () {
            // MyCL/total_contents
            return 'SELECT * FROM Contents_list';
        },
        getGenre: function () {
            // MyCL/contents?id=A01
            return 'SELECT * FROM Genre WHERE `id`=?';
        },
        getFavorite: function () {
            // MyCL/favorite?id=1
            return 'SELECT * FROM Favorite WHERE `id`=?';
        },
        getPrefer: function () {
            // MyCL/prefer?id=1
            return 'SELECT * FROM Prefer WHERE `id`=?';
        }
    }
};