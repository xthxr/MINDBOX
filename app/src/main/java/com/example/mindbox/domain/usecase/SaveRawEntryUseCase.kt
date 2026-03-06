package com.example.mindbox.domain.usecase

import com.example.mindbox.domain.repository.IEntryRepository
import javax.inject.Inject

class SaveRawEntryUseCase @Inject constructor(
    private val entryRepository: IEntryRepository
) {
    suspend operator fun invoke(text: String, source: String = "TEXT"): Long {
        require(text.isNotBlank()) { "Entry text cannot be blank" }
        return entryRepository.saveRawEntry(text.trim(), source)
    }
}
