package models.contents

import org.joda.time.DateTime

/**
  * Created by corpus on 01/02/2016.
  */
case class Comment (

                     content: String,
                     creationDate: DateTime,
                     nbLikes: Int
  //DB:
                   //"author_id" = "_id" in "users"
                   //"article_id" = "_id" in "comments"
                   )
