module.exports = (function () {
    return {
      Config: {
        public_publisher: "labis",
        auth: "1",
        non_auth: "0",
        favorite: "1",
        non_favorite: "0"
      },
      User: {
        id: "`id`",
        pw: "`pw`",
        age: "`age`",
        gender: "`gender`",
        nickname: "`nickname`",
        phone: "`phone`",
        image: "`image`"
      },
      Contents_my: { 
        id: "`id`",
        user_id: "`user_id`",
        score: "`score`",
        comment: "`comment`",
        chapter: "`chapter`",
        favorite: "`favorite`",
        time: "`time`"
      },
      Contents_list: { 
        id: "`id`",
        gen_id: "`gen_id`",
        season: "`season`",
        name: "`name`",
        name_org: "`name_org`",
        chapter_end: "`chapter_end`",
        theatrical: "`theatrical`",
        series_id: "`series_id`",
        summary: "`summary`",
        publisher: "`publisher`",
        auth: "`auth`",
        image: "`image`"
      },
      Favorite: { 
        id: "`id`",
        user_id: "`user_id`",
        con_list_id: "`con_list_id`",
        con_list_gen_id: "`con_list_gen_id`",
        con_list_season: "`con_list_season`",
      },
      Prefer: { 
        id: "`id`",
        user_id: "`user_id`",
        gen_id: "`gen_id`"
      },
      Genre: { 
        id: "`id`",
        name: "`name`"
      },
      Series: { 
        id: "`id`",
        name: "`name`"
      }
    }
  })();