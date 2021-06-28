package uk.gov.hmrc.eventhub.repository

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.{Concat, Sink, Source}
import com.mongodb.reactivestreams.client.{ChangeStreamPublisher, MongoClients, MongoCollection}
import org.bson.codecs.configuration.{CodecRegistries, CodecRegistry}
import org.bson.types.ObjectId
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import org.mongodb.scala.result.InsertOneResult
import org.reactivestreams.Publisher
import uk.gov.hmrc.eventhub.model.{Event, MongoEvent}
import uk.gov.hmrc.eventhub.repository.MongoEventHubRepository._

import java.time.Instant
import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.jdk.CollectionConverters.SeqHasAsJava

class MongoEventHubRepository()(implicit materializer: Materializer) extends EventHubRepository {
  private val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromCodecs(Codecs.playFormatCodec(MongoEvent.mongoEventFormat)),
    DEFAULT_CODEC_REGISTRY
  )

  private val client = MongoClients.create("mongodb://localhost:27017")
  private val db = client.getDatabase("event-hub")
  private val eventsCollection: MongoCollection[MongoEvent] = db
    .getCollection("event-hub", classOf[MongoEvent])
    .withCodecRegistry(codecRegistry)

  eventsCollection.createIndexes(
    Seq(
      IndexModel(
        ascending("createdAt"),
        IndexOptions().expireAfter(300, TimeUnit.SECONDS)
      )
    ).asJava
  )

  def saveEvent(event: Event): Future[InsertOneResult] =
    eventsCollection
      .insertOne(MongoEvent.apply(Instant.now, event.topic, event))
      .asSource
      .runWith(Sink.head)

  override def streamEvents(topic: String): Source[MongoEvent, NotUsed] =
    eventsCollection
      .find(topicFilter(topic), classOf[MongoEvent])
      .asEventSource
      //.watchAfter(eventsCollection, topic) so sad this only works with replicated data so did not test locally ;(

  override def streamEventsFrom(id: ObjectId, topic: String): Source[MongoEvent, NotUsed] =
    eventsCollection
      .find(and(gt(Id, id), topicFilter(topic)), classOf[MongoEvent])
      .asEventSource
      //.watchAfter(eventsCollection, topic)
}

object MongoEventHubRepository {
  val Id: String = "_id"
  val Topic: String = "topic"

  def topicFilter(topic: String): Bson = equal(Topic, topic)

  implicit class ChangeStreamPublisherOps(val publisher: ChangeStreamPublisher[MongoEvent]) extends AnyVal {
    def asEventSource: Source[MongoEvent, NotUsed] = MongoSource(publisher).map(_.getFullDocument)
  }

  implicit class MongoPublisherOps(val publisher: Publisher[MongoEvent]) extends AnyVal {
    def asEventSource: Source[MongoEvent, NotUsed] = MongoSource(publisher)
  }

  implicit class PublisherOps[T](val publisher: Publisher[T]) extends AnyVal {
    def asSource: Source[T, NotUsed] = MongoSource(publisher)
  }

  implicit class EventStreamOps(val source: Source[MongoEvent, NotUsed]) extends AnyVal {
    def watchAfter(eventsCollection: MongoCollection[MongoEvent], topic: String): Source[MongoEvent, NotUsed] = {
      val watch = eventsCollection
        .watch(classOf[MongoEvent])
        .asEventSource
        .filter(_.topic == topic)

      Source.combine(source, watch)(Concat(_))
    }
  }
}

