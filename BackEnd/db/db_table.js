module.exports = (function () {
    return {
      User: {
        id: "`id`",
        pw: "`pw`",
        age: "`age`",
        gender: "`gender`",
        nickname: "`nickname`",
        phone: "`phone`",
        pre_id: "`pre_id`",
        fav_id: "`fav_id`"
      },
      Contents_my: { 
        id: "`id`",
        gen_id: "`gen_id`",
        season: "`season`",
        user_id: "`user_id`",
        score: "`score`",
        comment: "`comment`",
        chapter: "`chapter`"
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