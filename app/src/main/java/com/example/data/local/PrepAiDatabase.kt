package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.model.Converters
import com.example.data.model.ExamAttempt
import com.example.data.model.JobCircular
import com.example.data.model.MockExam
import com.example.data.model.Question
import com.example.data.model.StudyTask
import com.example.data.model.UserProfile
import com.example.data.model.WeaknessItem
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserProfile(userId: String = "user_default_1"): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserProfile)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE examCategory = :category")
    fun getQuestionsByCategory(category: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE subject = :subject")
    fun getQuestionsBySubject(subject: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1")
    fun getBookmarkedQuestions(): Flow<List<Question>>

    @Query("UPDATE questions SET isBookmarked = :isBookmarked WHERE id = :questionId")
    suspend fun updateBookmark(questionId: String, isBookmarked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Update
    suspend fun updateQuestion(question: Question)

    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestion(questionId: String)
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_tasks")
    fun getAllTasks(): Flow<List<StudyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTask>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTask)

    @Query("DELETE FROM study_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("UPDATE study_tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean)

    @Query("SELECT * FROM weakness_items")
    fun getAllWeaknesses(): Flow<List<WeaknessItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeaknesses(items: List<WeaknessItem>)

    @Query("SELECT * FROM exam_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<ExamAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExamAttempt)
}

@Dao
interface CircularDao {
    @Query("SELECT * FROM job_circulars")
    fun getAllCirculars(): Flow<List<JobCircular>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCirculars(circulars: List<JobCircular>)

    @Query("DELETE FROM job_circulars WHERE id = :circularId")
    suspend fun deleteCircular(circularId: String)
}

@Dao
interface MockExamDao {
    @Query("SELECT * FROM mock_exams")
    fun getAllMockExams(): Flow<List<MockExam>>

    @Query("SELECT * FROM mock_exams WHERE id = :examId LIMIT 1")
    suspend fun getMockExamById(examId: String): MockExam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockExams(exams: List<MockExam>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockExam(exam: MockExam)
}

@Database(
    entities = [
        UserProfile::class,
        Question::class,
        StudyTask::class,
        WeaknessItem::class,
        JobCircular::class,
        MockExam::class,
        ExamAttempt::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PrepAiDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questionDao(): QuestionDao
    abstract fun studyDao(): StudyDao
    abstract fun circularDao(): CircularDao
    abstract fun mockExamDao(): MockExamDao

    companion object {
        @Volatile
        private var INSTANCE: PrepAiDatabase? = null

        fun getInstance(context: Context): PrepAiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrepAiDatabase::class.java,
                    "prepai_bd_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
