module.exports = function () {
    return {
        getUser: function () {
            return 'select * from User where `id`=?';
        },
        getContents: function () {
            return 'select * from Contents where id=? and gen_id=?';
        },
        getGenre: function () {
            return 'select * from Genre where `id`=?';
        },
        getFavorite: function () {
            return 'select * from Favorite where `id`=?';
        },
        getPrefer: function () {
            return 'select * from Prefer where `id`=?';
        }
    }
};