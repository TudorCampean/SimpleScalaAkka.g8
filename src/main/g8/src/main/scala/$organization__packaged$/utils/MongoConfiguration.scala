package com.webappsproduction.utils

import com.mongodb.casbah._
import com.mongodb.{MongoException, ServerAddress}
import com.mongodb.casbah.gridfs.GridFS
import commons.MongoDBObject
import com.typesafe.config.ConfigFactory

class MongoConfiguration{
 
 lazy val configuration =  Configuration.load.getConfig("mongodb").getOrElse(Configuration.empty)
  
 case class MongoSource(
    val hosts: List[ServerAddress],
    val dbName: String,
    val writeConcern: com.mongodb.WriteConcern,
    val user: Option[String] = None,
    val password: Option[String] = None,
    private var conn: MongoConnection = null
  ){
    
    def connection: MongoConnection = {
      if (conn == null) {
        conn = MongoConnection(hosts)
        val authOpt = for {
          u <- user
          p <- password
        } yield connection(dbName).authenticate(u, p)
        if (!authOpt.getOrElse(true)) {
          throw new Exception("mongodb - Access denied to MongoDB database: [" + dbName + "] with user: [" + user.getOrElse("") + "]")
        }
        conn.setWriteConcern(writeConcern)
      }
      conn
    }
    
    def reset() {
      conn.close()
      conn = null
    }

    def db: MongoDB = connection(dbName)

    def collection(name: String): MongoCollection = db(name)

    def cappedCollection(name: String, size: Int, max: Option[Int] = None) = {
      val coll = if (db.collectionExists(name)) {
        db(name)
      } else {
        import com.mongodb.casbah.Implicits.mongoCollAsScala
        val options = MongoDBObject.newBuilder
        options += "capped" -> true
        options += "size" -> size
        if (max.isDefined)
          options += "max" -> max.get
        db.createCollection(name, options.result()).asScala
      }
      coll
    }

    def gridFS(bucketName: String = "fs"): GridFS = GridFS(db, bucketName)

    override def toString() = {
      (if (user.isDefined) user.get + "@" else "") +
      hosts.map(h => h.getHost + ":" + h.getPort).mkString(", ") +
      "/" + dbName
    }
  }
 
 lazy val sources: Map[String, MongoSource] = configuration.subKeys.map { sourceKey =>
    val source = configuration.getConfig(sourceKey).getOrElse(Configuration.empty)
    source.getString("uri").map { str =>
      // MongoURI config - http://www.mongodb.org/display/DOCS/Connections
      val uri = MongoURI(str)
      val hosts = uri.hosts.map { host =>
        if (host.contains(':')) {
          val Array(h, p) = host.split(':')
          new ServerAddress(h, p.toInt)
        } else {
          new ServerAddress(host)
        }
      }.toList
      val db = uri.database.getOrElse(throw new Exception("mongodb." + sourceKey + ".uri - db missing for source[" + sourceKey + "]"))
      val writeConcern = uri.options.getWriteConcern
      val user = uri.username
      val password = uri.password.map(_.mkString).filterNot(_.isEmpty)
      sourceKey -> MongoSource(hosts, db, writeConcern, user, password)
    }.getOrElse {
      val dbName = source.getString("db").getOrElse(throw new Exception("mongodb." + sourceKey + ".db - db missing for source[" + sourceKey + "]"))
      // Simple config
      val host = source.getString("host").getOrElse("127.0.0.1")
      val port = source.getInt("port").getOrElse(27017)
      val user:Option[String] = source.getString("user")
      val password:Option[String] = source.getString("password")

      // Replica set config
      val hosts: List[ServerAddress] = source.getConfig("replicaset").map { replicaset =>
        replicaset.subKeys.map { hostKey =>
          val c = replicaset.getConfig(hostKey).get
          val host = c.getString("host").getOrElse(throw new Exception("mongodb." + sourceKey + ".replicaset - host missing for replicaset in source[" + sourceKey + "]"))
          val port = c.getInt("port").getOrElse(27017)
          new ServerAddress(host, port)
        }.toList
      }.getOrElse(List.empty)

      val writeConcern = WriteConcern.valueOf(source.getString("writeconcern", Some(Set("fsyncsafe", "replicassafe", "safe", "normal"))).getOrElse("safe"))

      // If there are replicasets configured go with those otherwise fallback to simple config
      if (hosts.isEmpty)
        sourceKey -> MongoSource(List(new ServerAddress(host, port)), dbName, writeConcern, user, password)
      else
        sourceKey -> MongoSource(hosts, dbName, writeConcern, user, password)
    }
  }.toMap

  
  /**
   * Returns the MongoSource that has been configured in application.conf
   * @param source The source name ex. default
   * @return A MongoSource
   */
  def source(source: String): MongoSource = {
    sources.get(source).getOrElse(throw new Exception("mongodb." + source+" doesn't exist"))
  }

  /**
   * Returns MongoDB for configured source
   * @param sourceName The source name ex. default
   * @return A MongoDB
   */
  def db(sourceName:String = "default"): MongoDB = source(sourceName).db

  /**
   * Returns MongoCollection that has been configured in application.conf
   * @param collectionName The MongoDB collection name
   * @param sourceName The source name ex. default
   * @return A MongoCollection
   */
  def collection(collectionName:String, sourceName:String = "default"): MongoCollection = source(sourceName).collection(collectionName)

  /**
   * Returns Capped MongoCollection that has been configured in application.conf
   * @param collectionName The MongoDB collection name
   * @param size The capped collection size
   * @param max The capped collection max number of documents
   * @param sourceName The source name ex. default
   * @return A MongoCollection
   */
  def cappedCollection(collectionName:String, size: Int, max: Option[Int] = None, sourceName:String = "default"): MongoCollection = source(sourceName).cappedCollection(collectionName, size, max)

  /**
   * Returns GridFS for configured source
   * @param bucketName The bucketName for the GridFS instance
   * @param sourceName The source name ex. default
   * @return A GridFS
   */
  def gridFS(bucketName: String = "fs", sourceName:String = "default"): GridFS = source(sourceName).gridFS(bucketName)
}