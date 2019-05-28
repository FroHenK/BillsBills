package frohenk.billsbills.database

import androidx.room.*
import com.frohenk.receiptlibrary.engine.QueuedReceipt
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigInteger

@Dao
interface QueuedReceiptDao {
    @Query("SELECT * FROM QueuedReceipt")
    fun getAll(): List<QueuedReceipt>

    @Query("SELECT * FROM QueuedReceipt")
    fun getAllFlowable(): Flowable<List<QueuedReceipt>>


    @Query("SELECT * FROM QueuedReceipt WHERE uid = :uid")
    fun getByUid(uid: Int): Single<QueuedReceipt>

    @Query("SELECT * FROM QueuedReceipt WHERE fiscalDocumentNumber = :fiscalDocumentNumber AND sum=:sum")
    fun getByFiscalAndSum(fiscalDocumentNumber: BigInteger, sum: BigInteger): Flowable<QueuedReceipt>

    @Query("SELECT * FROM QueuedReceipt WHERE visible = :visible")
    fun getVisible(visible: Boolean = true): List<QueuedReceipt>

    @Update
    fun updateReceipts(vararg queuedReceipt: QueuedReceipt)


    @Update
    fun updateReceipts(queuedReceipts: List<QueuedReceipt>)

    @Insert
    fun insert(queuedReceipt: QueuedReceipt)

    @Delete
    fun delete(queuedReceipt: QueuedReceipt)
}