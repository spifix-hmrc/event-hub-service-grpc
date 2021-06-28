package uk.gov.hmrc.eventhub.model

import cats.syntax.either._
import cats.Semigroup
import cats.data.NonEmptyList

sealed abstract class ConversionError(val message: String)

object ConversionError {
  implicit class RequiredOps(val required: Option[String]) extends AnyVal {
    def required[T](fieldName: String, f: String => T): Either[ConversionError, T] =
      required
        .toRight(MissingRequiredField(fieldName))
        .flatMap(parse(_, fieldName, f))

    private def parse[T](value: String, fieldName: String, f: String => T): Either[ParseError, T] =
      Either
        .catchNonFatal(f(value))
        .leftMap(ParseError(_, fieldName))
  }

  implicit object ConversionErrorSemiGroup extends Semigroup[ConversionError] {
    override def combine(x: ConversionError, y: ConversionError): ConversionError = (x, y) match {
      case (ConversionErrors(left), ConversionErrors(right)) => ConversionErrors(left ++ right.toList)
      case (ConversionErrors(left), right) => ConversionErrors(right :: left)
      case (left, ConversionErrors(right)) => ConversionErrors(left :: right)
      case (left, right) => ConversionErrors(NonEmptyList.of(left, right))
    }
  }

  final case class ConversionErrors(nonEmptyList: NonEmptyList[ConversionError]) extends ConversionError(
    nonEmptyList
      .toList
      .map(_.message)
      .mkString(" | ")
  )

  final case class MissingRequiredField(fieldName: String) extends ConversionError(
    s"missing field: $fieldName"
  )

  final case class ParseError(throwable: Throwable, fieldName: String) extends ConversionError(
    s"error parsing: $fieldName - ${throwable.getMessage}"
  )
}
