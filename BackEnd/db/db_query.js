module.exports = function () {
    return {
        // POST
        postLogin: function () {
            return 'SELECT * FROM User WHERE `id`=?';
        },
        postRegister: function () {
            return 'INSERT INTO User SET ?';
        },

        // GET
        getUser: function () {
            return 'SELECT * FROM User WHERE `id`=?';
        },
        getContents: function () {
            return 'SELECT * FROM Contents WHERE `id`=? AND `gen_id`=?';
        },
        getGenre: function () {
            return 'SELECT * FROM Genre WHERE `id`=?';
        },
        getFavorite: function () {
            return 'SELECT * FROM Favorite WHERE `id`=?';
        },
        getPrefer: function () {
            return 'SELECT * FROM Prefer WHERE `id`=?';
        }
    }
};