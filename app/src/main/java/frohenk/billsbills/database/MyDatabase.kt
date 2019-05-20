package frohenk.billsbills.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.frohenk.receiptlibrary.engine.Converters
import com.frohenk.receiptlibrary.engine.QueuedReceipt

@Database(entities = [QueuedReceipt::class], version = 1)
@TypeConverters(Converters::class)
abstract class MyDatabase : RoomDatabase() {
    abstract fun queuedReceiptsDao(): QueuedReceiptDao

    companion object {
        var database: MyDatabase? = null
        @Synchronized
        fun getDatabase(context: Context): MyDatabase {
            if (database == null) database =
                Room.databaseBuilder(context.applicationContext, MyDatabase::class.java, "database").build()
            return database as MyDatabase
        }
    }
}