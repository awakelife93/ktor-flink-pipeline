package com.example.flink.dto

data class Job(
	val jobList: List<Pair<String, () -> Unit>>
) {
	val size: Int get() = jobList.size

	fun forEach(action: (Pair<String, () -> Unit>) -> Unit) {
		jobList.forEach(action)
	}
}
