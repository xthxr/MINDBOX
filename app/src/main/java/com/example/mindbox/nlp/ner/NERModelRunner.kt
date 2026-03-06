package com.example.mindbox.nlp.ner

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class NerResult(
    val people: List<String> = emptyList(),
    val organizations: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val dates: List<String> = emptyList()
)

/**
 * Lightweight NER fallback using a quantized BERT-NER TFLite model.
 * Loaded lazily and only invoked when rule-based confidence < 0.5.
 *
 * Uses the core TFLite Interpreter API (no task-text dependency).
 * The model file [bert_ner_quant.tflite] must exist in assets/.
 * If the model is absent, returns an empty NerResult gracefully.
 */
@Singleton
class NERModelRunner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val NER_MODEL_ASSET = "bert_ner_quant.tflite"
        private const val MAX_SEQ_LEN = 128

        // BIO tag label indices expected from the NER model output
        private val LABEL_MAP = listOf(
            "O", "B-PER", "I-PER", "B-ORG", "I-ORG",
            "B-LOC", "I-LOC", "B-DATE", "I-DATE"
        )
        private val PERSON_TAGS = setOf("B-PER", "I-PER")
        private val ORG_TAGS = setOf("B-ORG", "I-ORG")
        private val LOC_TAGS = setOf("B-LOC", "I-LOC")
        private val DATE_TAGS = setOf("B-DATE", "I-DATE")
    }

    private var interpreter: org.tensorflow.lite.Interpreter? = null
    private var modelLoadAttempted = false

    @Synchronized
    private fun getInterpreter(): org.tensorflow.lite.Interpreter? {
        if (modelLoadAttempted) return interpreter
        modelLoadAttempted = true
        return try {
            val afd = context.assets.openFd(NER_MODEL_ASSET)
            val inputStream = afd.createInputStream()
            val modelBytes = inputStream.readBytes()
            inputStream.close()
            val buf = java.nio.ByteBuffer.allocateDirect(modelBytes.size)
            buf.put(modelBytes)
            buf.rewind()
            val options = org.tensorflow.lite.Interpreter.Options().apply { setNumThreads(2) }
            org.tensorflow.lite.Interpreter(buf, options).also { interpreter = it }
        } catch (e: Exception) {
            // Model asset not present — degrade gracefully
            null
        }
    }

    /**
     * Run NER on [text] using the core TFLite Interpreter.
     * Returns an empty [NerResult] if the model is unavailable.
     */
    fun run(text: String): NerResult {
        val interp = getInterpreter() ?: return NerResult()

        return try {
            val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
            val inputIds = IntArray(MAX_SEQ_LEN)
            words.take(MAX_SEQ_LEN).forEachIndexed { idx, word ->
                inputIds[idx] = word.lowercase().hashCode().and(0x7FFF)
            }

            val inputBuffer = Array(1) { inputIds }
            val outputBuffer = Array(1) { Array(MAX_SEQ_LEN) { FloatArray(LABEL_MAP.size) } }
            interp.run(inputBuffer, outputBuffer)

            val predictions = outputBuffer[0]
            val people = mutableListOf<String>()
            val orgs = mutableListOf<String>()
            val locs = mutableListOf<String>()
            val dates = mutableListOf<String>()

            var currentSpan = StringBuilder()
            var currentTag: String? = null

            words.take(MAX_SEQ_LEN).forEachIndexed { idx, word ->
                val logits = predictions[idx]
                val labelIdx = logits.indices.maxByOrNull { logits[it] } ?: 0
                val label = LABEL_MAP.getOrElse(labelIdx) { "O" }

                if (label == "O" || label.startsWith("B-")) {
                    flushSpan(currentSpan, currentTag, people, orgs, locs, dates)
                    currentSpan = StringBuilder()
                    currentTag = null
                }
                if (label != "O") {
                    if (currentSpan.isNotEmpty()) currentSpan.append(' ')
                    currentSpan.append(word.trimEnd('.', ',', ';'))
                    currentTag = label
                }
            }
            flushSpan(currentSpan, currentTag, people, orgs, locs, dates)

            NerResult(
                people = people.distinct(),
                organizations = orgs.distinct(),
                locations = locs.distinct(),
                dates = dates.distinct()
            )
        } catch (e: Exception) {
            NerResult()
        }
    }

    private fun flushSpan(
        span: StringBuilder,
        tag: String?,
        people: MutableList<String>,
        orgs: MutableList<String>,
        locs: MutableList<String>,
        dates: MutableList<String>
    ) {
        if (span.isEmpty() || tag == null) return
        val text = span.toString().trim()
        when (tag) {
            in PERSON_TAGS -> people.add(text)
            in ORG_TAGS -> orgs.add(text)
            in LOC_TAGS -> locs.add(text)
            in DATE_TAGS -> dates.add(text)
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        modelLoadAttempted = false
    }
}
