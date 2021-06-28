package uk.gov.hmrc.eventhub.controllers

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import cats.Parallel
import com.google.protobuf.empty.Empty
import uk.gov.hmrc.eventhub.repository.MongoEventHubRepository
import uk.gov.hmrc.eventhub.{v1 => api}
import cats.syntax.either._
import org.mongodb.scala.bson.ObjectId
import uk.gov.hmrc.eventhub.controllers.EventHubServiceGrpc.{ErrorOps, ObjectIdOps}
import uk.gov.hmrc.eventhub.model.ConversionError.MissingRequiredField
import uk.gov.hmrc.eventhub.model.{ConversionError, Event}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.eventhub.model.Event._
import uk.gov.hmrc.eventhub.model.MongoEvent.toProto

@Singleton
class EventHubServiceGrpc @Inject()()
(implicit
 actorSystem: ActorSystem,
 executionContext: ExecutionContext,
 materializer: Materializer
) extends api.AbstractEventHubServiceRouter(actorSystem) {

  private val eventHubRepository = new MongoEventHubRepository()

  override def createEvent(in: api.Event): Future[Empty] = {
    val event: Event = fromProto(in).valueOr(error => throw error.asRuntimeException)
    eventHubRepository
      .saveEvent(event)
      .map(result =>
        if(result.wasAcknowledged) { Empty.defaultInstance } else throw new RuntimeException(s"could not persist: $in")
      )
  }

  override def streamEvents(in: api.StreamEventsRequest): Source[api.Event, NotUsed] = {
    val topic = in.topic.getOrElse(throw MissingRequiredField("topic").asRuntimeException)

    eventHubRepository
      .streamEvents(topic)
      .map(toProto)
  }

  override def streamEventsFrom(in: api.StreamEventsFromRequest): Source[api.Event, NotUsed] = {
    val (id, topic) = Parallel.parProduct(
      Either.fromOption(in.fromId, MissingRequiredField("fromId"): ConversionError).map(_.toObjectId),
      Either.fromOption(in.topic, MissingRequiredField("topic"))
    ).valueOr(error => throw error.asRuntimeException)

    eventHubRepository
      .streamEventsFrom(id, topic)
      .map(toProto)
  }
}

object EventHubServiceGrpc {
  implicit class ErrorOps(val conversionError: ConversionError) extends AnyVal {
    def asRuntimeException = new IllegalArgumentException(conversionError.message)
  }

  implicit class ObjectIdOps(val id: String) extends AnyVal {
    def toObjectId = new ObjectId(id)
  }
}
