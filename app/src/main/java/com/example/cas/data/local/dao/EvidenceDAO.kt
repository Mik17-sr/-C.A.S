package com.example.cas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.cas.data.local.entity.EvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDAO : InterfaceDAO<EvidenceEntity> {

    @Query("SELECT * FROM evidences WHERE case_id = :caseId ORDER BY date DESC")
    fun getEvidencesByCase(caseId: Long) : Flow<List<EvidenceEntity>>

    @Query("SELECT COUNT(*) FROM evidences")
    suspend fun countEvidences(): Int
}