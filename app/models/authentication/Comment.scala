

package models.authentication

import org.joda.time.DateTime
/**
  * Created by corpus on 01/02/2016.
  */
case class Comment (

                     author: User,
                     content: String,
                     creationDate: DateTime,
                     nbLikes: Int,
                     listUsersLikes: List[User]

                   )

