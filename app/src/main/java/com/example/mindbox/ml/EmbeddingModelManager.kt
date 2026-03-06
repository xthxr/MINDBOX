package com.example.mindbox.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the MobileBERT TFLite model for sentence embedding generation.
 *
 * The model [mobileBERT_embedding.tflite] must be placed in app/src/main/assets/.
 * The model is loaded lazily the first time an embedding is requested.
 *
 * Output: 768-dimensional float vector (normalized).
 */
@Singleton
class EmbeddingModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val EMBEDDING_MODEL_ASSET = "mobileBERT_embedding.tflite"
        const val MODEL_VERSION = "mobilebert_v1"
        const val VECTOR_DIM = 768
    }

    // We use the NLClassifier API from TFLite Task Library as a lightweight bridge.
    // For actual MobileBERT embedding, a custom TFLite interpreter wrapper is used below.
    private var interpreter: org.tensorflow.lite.Interpreter? = null
    private var modelLoadAttempted = false

    @Synchronized
    private fun getInterpreter(): org.tensorflow.lite.Interpreter? {
        if (modelLoadAttempted) return interpreter
        modelLoadAttempted = true
        return try {
            val assetFileDescriptor = context.assets.openFd(EMBEDDING_MODEL_ASSET)
            val inputStream = assetFileDescriptor.createInputStream()
            val modelBytes = inputStream.readBytes()
            inputStream.close()
            val buf = java.nio.ByteBuffer.allocateDirect(modelBytes.size)
            buf.put(modelBytes)
            buf.rewind()
            val options = org.tensorflow.lite.Interpreter.Options().apply {
                setNumThreads(2)
            }
            org.tensorflow.lite.Interpreter(buf, options).also { interpreter = it }
        } catch (e: Exception) {
            null // Model not yet bundled — degrade gracefully
        }
    }

    /**
     * Generate a normalized embedding vector for [text].
     * Returns a zero vector of [VECTOR_DIM] dimensions if the model is unavailable.
     */
    fun embed(text: String): FloatArray {
        val interp = getInterpreter() ?: return FloatArray(VECTOR_DIM)

        return try {
            // Input: tokenized text as int32 array of shape [1, maxSeqLen=128]
            val maxSeqLen = 128
            val inputIds = tokenize(text, maxSeqLen)
            val inputBuffer = Array(1) { inputIds }
            val outputBuffer = Array(1) { FloatArray(VECTOR_DIM) }
            interp.run(inputBuffer, outputBuffer)
            val vec = outputBuffer[0]
            normalize(vec)
        } catch (e: Exception) {
            FloatArray(VECTOR_DIM)
        }
    }

    /** Naive whitespace tokenizer → word IDs (for integration test; replace with real vocab). */
    private fun tokenize(text: String, maxLen: Int): IntArray {
        val tokens = text.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }
        val ids = IntArray(maxLen)
        tokens.take(maxLen).forEachIndexed { idx, token ->
            ids[idx] = token.hashCode().and(0x7FFF) // Simple hash; replace with real vocab lookup
        }
        return ids
    }

    /** L2-normalize the vector in-place. */
    private fun normalize(vec: FloatArray): FloatArray {
        val norm = Math.sqrt(vec.sumOf { (it * it).toDouble() }).toFloat().coerceAtLeast(1e-8f)
        return FloatArray(vec.size) { vec[it] / norm }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        modelLoadAttempted = false
    }
}
