package models.authentication

import org.joda.time.DateTime


/**
  * Created by corpus on 01/02/2016.
  */

// The implicit conversion has to be done ia long for dateTime and BSONDateTime
case class Article(

                    title: String,
                    content: String,
                    creationDate: DateTime,
                    lastUpdate: DateTime,
                    nbLikes: Int,
                    nbComments: Int
                    //in DB:
                    //author_id = field "_id" in collection "users"
                  )
