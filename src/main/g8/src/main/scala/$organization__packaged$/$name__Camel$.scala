package $organization$

import org.slf4j._
import com.mongodb.casbah.Imports._
import $organization$.utils._

object $name;format="Camel"$ extends App {
  val logger = LoggerFactory.getLogger(getClass.getName)
  val mongo = new MongoConfiguration
  val db = mongo.db()
  logger.info(db.command(MongoDBObject("serverStatus"-> 1 )))
}
