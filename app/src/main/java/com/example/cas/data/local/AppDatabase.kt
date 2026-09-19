package com.example.cas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cas.data.local.dao.CaseDAO
import com.example.cas.data.local.dao.EvidenceDAO
import com.example.cas.data.local.dao.InterviewDAO
import com.example.cas.data.local.dao.RecordDAO
import com.example.cas.data.local.dao.UserDAO
import com.example.cas.data.local.entity.CaseEntity
import com.example.cas.data.local.entity.EvidenceEntity
import com.example.cas.data.local.entity.InterviewEntity
import com.example.cas.data.local.entity.RecordEntity
import com.example.cas.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CaseEntity::class,
        InterviewEntity::class,
        EvidenceEntity::class,
        RecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDAO
    abstract fun caseDao(): CaseDAO
    abstract fun interviewDao(): InterviewDAO
    abstract fun evidenceDao(): EvidenceDAO
    abstract fun recordDao(): RecordDAO

    companion object {
        @Volatile
        private var INSTANCE : AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                AppDatabase::class.java,
                    "casdb"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}