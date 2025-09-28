package com.example.flink.module

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

val logStreamExecutionEnvironment: StreamExecutionEnvironment by lazy {
  StreamExecutionEnvironment.getExecutionEnvironment()
}

fun configureFlinkModule(topic: String, bootstrapServers: String): DataStream<String> {
  val source = KafkaSource.builder<String>()
    .apply {
      setBootstrapServers(bootstrapServers)
      setTopics(topic)
      setGroupId("flink-log-consumer")
      setStartingOffsets(OffsetsInitializer.latest())
      setValueOnlyDeserializer(SimpleStringSchema())
    }
    .build()

  return logStreamExecutionEnvironment.fromSource(
    source,
    WatermarkStrategy.noWatermarks(),
    "Kafka Source"
  )
}
