module.exports = function () {
    return {
        /////////
        // GET //
        /////////
        // getUser: function () {
        //      // .../MyCL/user?id=khercules
        //     return 'SELECT * FROM User WHERE `id`=?';
        // },
        // getContents: function () {
        //      // .../MyCL/contents?id=khercules&gen_id=A01
        //     return 'SELECT * FROM Contents_my';
        // },
        getTotalContents: function () {
            // .../MyCL/total_contents
            return 'SELECT * FROM Contents_list';
        },
        getTotalGenre: function () {
            // .../MyCL/total_genre
            return 'SELECT * FROM Genre';
        },


        //////////
        // POST //
        //////////
        // search
        postLogin: function () {
            // .../MyCL/login (id=khercules)
            return 'SELECT * FROM User WHERE `id`=?';
        },
        postRegister: function () {
            // .../MyCL/register (id=khercules,pw:1234,age:36,gender:male,nickname:hercules,phone:01023250866,image:null)
            return 'INSERT INTO User SET ?';
        },
        postUser: function () {
            // .../MyCL/user (id=khercules)
           return 'SELECT * FROM User';
        },
        postGenre: function () {
            // .../MyCL/genre (id=A01)
            return 'SELECT * FROM Genre';
        },
        postFavorite: function () {
            // .../MyCL/favorite (user_id:khercules)
            return 'SELECT * FROM Favorite';
        },
        postPrefer: function () {
            // .../MyCL/prefer (user_id:khercules)
            return 'SELECT * FROM Prefer';
        },
        postContents: function () {
            // .../MyCL/contents (id=khercules,user_id=khercules)
            return 'SELECT * FROM Contents_my';
        },
        postMyContents: function () {
            // .../MyCL/my_contents (user_id=khercules)
            return 'SELECT * FROM Contents_my JOIN Contents_list USING(id)';
        },
        postInsertContents: function () {
            // .../MyCL/
            return 'INSERT INTO Contents_my SET ?';
        },
        
    }
};
