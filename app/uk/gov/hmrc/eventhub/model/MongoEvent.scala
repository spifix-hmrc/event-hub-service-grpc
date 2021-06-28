package uk.gov.hmrc.eventhub.model

import org.bson.types.ObjectId
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{Format, JsPath, OFormat}
import play.api.libs.functional.syntax._
import cats.syntax.option._
import uk.gov.hmrc.eventhub.{v1 => proto}

import java.time.Instant
import java.time.format.DateTimeFormatter

case class MongoEvent(_id: ObjectId, createdAt: Instant, topic: String, event: Event)

object MongoEvent {
  def apply(instant: Instant, topic: String, e: Event): MongoEvent = MongoEvent(ObjectId.get(), instant, topic, e)

  private implicit val objectIdFormat: Format[ObjectId] = MongoFormats.objectIdFormat
  private implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  implicit val mongoEventFormat: OFormat[MongoEvent] = ((JsPath \ "_id").format[ObjectId]
    ~ (JsPath \ "createdAt").format[Instant]
    ~ (JsPath \ "topic").format[String]
    ~ (JsPath \ "event").format[Event]
    )(MongoEvent.apply, unlift(MongoEvent.unapply))

  def toProto(mongoEvent: MongoEvent): proto.Event = {
    val event = mongoEvent.event
    proto.Event(
      id = mongoEvent._id.toString.some,
      eventId = event.eventId.toString.some,
      topic = event.topic.some,
      subject = event.subject,
      timestamp = event.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).some,
      event = event.event.toString().some
    )
  }
}
