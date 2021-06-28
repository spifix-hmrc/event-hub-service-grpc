package uk.gov.hmrc.eventhub.repository

import akka.NotUsed
import akka.stream.scaladsl.Source
import org.bson.types.ObjectId
import org.mongodb.scala.result.InsertOneResult
import uk.gov.hmrc.eventhub.model.{Event, MongoEvent}

import scala.concurrent.Future

trait EventHubRepository {
  def saveEvent(event: Event): Future[InsertOneResult]
  def streamEvents(topic: String): Source[MongoEvent, NotUsed]
  def streamEventsFrom(id: ObjectId, topic: String): Source[MongoEvent, NotUsed]
}