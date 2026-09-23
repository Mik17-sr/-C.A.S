package com.example.cas.data.repository

import com.example.cas.data.local.dao.EvidenceDAO
import com.example.cas.data.local.entity.EvidenceEntity
import kotlinx.coroutines.flow.Flow

class EvidenceRepository(
    val evidenceDao: EvidenceDAO
) {

    fun getEvidencesByCase(caseId: Long) : Flow<List<EvidenceEntity>> {
        return evidenceDao.getEvidencesByCase(caseId)
    }

    suspend fun insertEvidence(evidence: EvidenceEntity): Long {
        return evidenceDao.insert(evidence)
    }

    suspend fun deleteEvidence(evidence: EvidenceEntity) {
        evidenceDao.delete(evidence)
    }
}