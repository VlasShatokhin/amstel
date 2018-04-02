package io.vlas.amstel.core.model

case class EventEnvelope(groupId: String,
                         deviceId: String,
                         event: Event)
