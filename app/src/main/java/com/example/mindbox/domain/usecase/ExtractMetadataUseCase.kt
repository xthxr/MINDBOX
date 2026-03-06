package com.example.mindbox.domain.usecase

import com.example.mindbox.nlp.ExtractionResult
import com.example.mindbox.nlp.MetadataExtractor
import javax.inject.Inject

class ExtractMetadataUseCase @Inject constructor(
    private val metadataExtractor: MetadataExtractor
) {
    operator fun invoke(text: String): ExtractionResult = metadataExtractor.extract(text)
}
