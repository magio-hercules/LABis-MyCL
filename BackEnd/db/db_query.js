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
        getTotalRequestType: function () {
            // .../MyCL/total_request_type
            return 'SELECT * FROM Request_type';
        },


        //////////
        // POST //
        //////////

        // user
        postUser: function () {
            // .../MyCL/user (id)
           return 'SELECT * FROM User';
        },

        // genre
        postGenre: function () {
            // .../MyCL/genre (id)
            return 'SELECT * FROM Genre';
        },

        // favorite
        postFavorite: function () {
            // .../MyCL/favorite (user_id)
            return 'SELECT * FROM Favorite';
        },

        // prefer
        postPrefer: function () {
            // .../MyCL/prefer (user_id)
            return 'SELECT * FROM Prefer';
        },

        // login
        postLogin: function () {
            // .../MyCL/login (id)
            return 'SELECT * FROM User WHERE `id`=?';
        },
        postRegister: function () {
            // .../MyCL/register (id,age,gender,nickname,phone,image,uid)
            return 'INSERT INTO User SET ?';
        },
        postUpdate: function () {
            // .../MyCL/update (id,age,gender,nickname,phone,image,uid)
            return 'UPDATE User SET ';
        },

        // request
        postRequestList: function () {
            // .../MyCL/request_list (req_type_id)
            return 'SELECT * FROM Request_list';
        },
        postInsertRequest: function () {
            // .../MyCL/insert_request (user_id,req_type_id,comment)
            return 'INSERT INTO Request_list SET ';
        },

        // contents
        postTotalContents: function () {
            // .../MyCL/total_contents (user_id)
            return 'SELECT * FROM Contents_list';
        },
        postTotalNewContents: function () {
            // .../MyCL/total_new_contents (user_id)
            return 'SELECT * FROM Contents_list';
        },
        postMyContents: function () {
            // .../MyCL/my_contents (user_id)
            return 'SELECT * FROM Contents_my JOIN Contents_list USING(id)';
        },
        postInsertMyContents: function () {
            // .../MyCL/insert_my_contents (user_id,id_list:[id])
            return 'INSERT INTO Contents_my (user_id, id, chapter) VALUES ';
        },
        postInsertMyNewContents: function () {
            // .../MyCL/insert_my_new_contents (user_id,id_list:[id])
            return 'INSERT INTO Contents_my SET ';
        },
        postUpdateMyContents: function () {
            // .../MyCL/update_my_contents (id,user_id,score,comment,chapter,favorite)
            return 'UPDATE Contents_my SET ';
        },
        postFilterMyContents: function () {
            // .../MyCL/filter_my_contents (user_id,gen_id)
            return 'SELECT * FROM Contents_my JOIN Contents_list USING(id)';
        },
        postInsertContentsList: function () {
            // .../MyCL/insert_contents_list (gen_id,season,name,name_org,chapter_end,theatrical,series_id,summary,publisher,auth,image)
            return 'INSERT INTO Contents_list SET ';
        },
        // postInsertMyContents: function () {
        //     // .../MyCL/insert_contents_list (id,user_id,score,comment,chapter)
        //     return 'INSERT INTO Contents_my SET';
        // },
        postFilterContentsList: function () {
            // .../MyCL/filter_contents_list (gen_id)
            return 'SELECT * FROM Contents_list';
        },
        postNonAuthContentsList: function () {
            // .../MyCL/non_auth_contents_list (user_id)
            return 'SELECT * FROM Contents_list';
        },
        postSetAuthContents: function () {
            // .../MyCL/set_auth_contents_list (user_id,id_list:[id])
            return 'UPDATE Contents_list SET ';
        },
        postDeleteMyContents: function () {
            // .../MyCL/delete_my_contents (user_id,id_list:[id])
            return 'DELETE FROM Contents_my';
        },
        postUpdateContentsImage: function () {
            // .../MyCL/update_contents_image (id, url)
            return 'UPDATE Contents_list SET ';
        },
        postSearchContentsList: function () {
            // .../MyCL/search_contents_list (name)
            return 'SELECT * FROM Contents_list ';
        },
        postSearchMyContents: function () {
            // .../MyCL/search_my_contents (user_id,name)
            return 'SELECT * FROM Contents_my JOIN Contents_list USING(id) ';
        }
    }
};
