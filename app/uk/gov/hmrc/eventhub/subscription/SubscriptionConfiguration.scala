package uk.gov.hmrc.eventhub.subscription

import akka.http.scaladsl.model.Uri

case class SubscriptionConfiguration(
  topic: String,
  name: String,
  uri: Uri
)