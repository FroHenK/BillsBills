package frohenk.billsbills.database

import androidx.room.*
import com.frohenk.receiptlibrary.engine.QueuedReceipt
import com.frohenk.receiptlibrary.engine.Receipt
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface QueuedReceiptDao {
    @Query("SELECT * FROM QueuedReceipt")
    fun getAll(): List<QueuedReceipt>

    @Query("SELECT * FROM QueuedReceipt")
    fun getAllFlowable(): Observable<List<QueuedReceipt>>


    @Query("SELECT * FROM QueuedReceipt WHERE uid = :uid")
    fun getByUid(uid: Int): Single<QueuedReceipt>


    @Query("SELECT * FROM QueuedReceipt WHERE visible = :visible")
    fun getVisible(visible: Boolean = true): List<QueuedReceipt>

    @Update
    fun updateReceipts(vararg queuedReceipt: QueuedReceipt)

    @Insert
    fun insert(queuedReceipt: QueuedReceipt)

    @Delete
    fun delete(queuedReceipt: QueuedReceipt)
}