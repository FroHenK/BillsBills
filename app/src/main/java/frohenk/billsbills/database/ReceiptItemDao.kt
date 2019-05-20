package frohenk.billsbills.database

import androidx.room.*
import com.frohenk.receiptlibrary.engine.ReceiptItem
import io.reactivex.Flowable

@Dao
interface ReceiptItemDao {
    @Query("SELECT * FROM ReceiptItem")
    fun getAll(): List<ReceiptItem>

    @Query("SELECT * FROM ReceiptItem")
    fun getAllFlowable(): Flowable<List<ReceiptItem>>

    @Update
    fun updateReceiptItems(vararg receiptItem: ReceiptItem)

    @Update
    fun updateReceiptItems(receiptItems: List<ReceiptItem>)

    @Insert
    fun insert(receiptItem: ReceiptItem)

    @Insert
    fun insertAll(receiptItems: List<ReceiptItem>)

    @Delete
    fun delete(receiptItem: ReceiptItem)

    @Delete
    fun deleteAll(receiptItems: List<ReceiptItem>)

}