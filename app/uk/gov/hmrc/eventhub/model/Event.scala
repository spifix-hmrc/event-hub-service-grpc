package uk.gov.hmrc.eventhub.model

import cats.syntax.parallel._
import cats.syntax.either._
import play.api.libs.json.{JsValue, Json, OFormat}
import uk.gov.hmrc.eventhub.model.ConversionError.RequiredOps
import uk.gov.hmrc.eventhub.{v1 => proto}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

case class Event(
  eventId: UUID,
  topic: String,
  subject: Option[String],
  timestamp: LocalDateTime,
  event: JsValue
)

object Event {
  implicit val eventFormat: OFormat[Event] = Json.format[Event]

  def fromProto(protoEvent: proto.Event): Either[ConversionError, Event] = {
    (
      protoEvent.eventId.required("eventId", UUID.fromString),
      protoEvent.topic.required("topic", identity),
      protoEvent.subject.asRight,
      protoEvent.timestamp.required("timestamp", LocalDateTime.parse(_, DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
      protoEvent.event.required("event", Json.parse)
    ).parMapN(Event.apply)
  }
}

