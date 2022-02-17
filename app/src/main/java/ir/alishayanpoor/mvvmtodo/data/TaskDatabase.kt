package ir.alishayanpoor.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.alishayanpoor.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    class CallBack @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // db operations
            val dao = database.get().taskDao()
            applicationScope.launch {
                dao.insert(
                    Task(
                        name = "this is task #1",
                        description = "this is description for task #1"
                    )
                )
                dao.insert(
                    Task(
                        name = "write some title",
                        description = "write some description here"
                    )
                )
                dao.insert(Task(name = "TODO", description = "is it Done?", important = true))

                dao.insert(
                    Task(
                        name = "completed task",
                        description = "this is a description for a completed task!",
                        completed = true
                    )
                )
            }
        }
    }

}