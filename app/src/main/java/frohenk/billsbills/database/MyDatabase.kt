package frohenk.billsbills.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.frohenk.receiptlibrary.engine.Converters
import com.frohenk.receiptlibrary.engine.QueuedReceipt
import com.frohenk.receiptlibrary.engine.Receipt
import com.frohenk.receiptlibrary.engine.ReceiptItem

@Database(entities = [QueuedReceipt::class, Receipt::class, ReceiptItem::class], version = 1)
@TypeConverters(Converters::class)
abstract class MyDatabase : RoomDatabase() {
    abstract fun queuedReceiptsDao(): QueuedReceiptDao
    abstract fun receiptsDao(): ReceiptDao
    abstract fun receiptItemsDao(): ReceiptItemDao


    companion object {
        var database: MyDatabase? = null
        @Synchronized
        fun getDatabase(context: Context): MyDatabase {
            if (database == null) database =
                Room.databaseBuilder(context.applicationContext, MyDatabase::class.java, "database1").build()
            return database as MyDatabase
        }

        fun nukeDatabase(context: Context) {
            if (database == null) database =
                Room.databaseBuilder(context.applicationContext, MyDatabase::class.java, "database1").build()
            database?.clearAllTables()
        }
    }
}