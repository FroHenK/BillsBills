package frohenk.billsbills.database

import androidx.room.*
import com.frohenk.receiptlibrary.engine.Receipt
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import java.math.BigInteger
import java.util.concurrent.Future

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM Receipt")
    fun getAllData(): List<DataReceipt>

    @Query("SELECT * FROM Receipt")
    fun getAll(): List<Receipt>


    @Query("SELECT * FROM Receipt")
    fun getAllFlowableData(): Flowable<List<DataReceipt>>

    @Query("SELECT * FROM Receipt")
    fun getAllFlowable(): Flowable<List<Receipt>>

    @Query("SELECT * FROM Receipt WHERE uid = :uid")
    fun getByUid(uid: Int): Maybe<DataReceipt>


    @Query("SELECT * FROM Receipt WHERE fiscalDocumentNumber = :fiscalDocumentNumber AND totalSum=:totalSum")
    fun getByFiscalAndSum(fiscalDocumentNumber: BigInteger, totalSum: BigInteger): Single<DataReceipt>

    @Update
    fun updateReceipts(vararg receipt: Receipt)

    @Update
    fun updateReceipts(receipts: List<Receipt>)

    @Insert
    fun insert(receipt: Receipt)

    @Delete
    fun delete(receipt: Receipt)

    @Query("SELECT COUNT(fiscalDocumentNumber) FROM Receipt WHERE fiscalDocumentNumber = :fiscalDocumentNumber AND totalSum=:totalSum")
    fun count(fiscalDocumentNumber: BigInteger, totalSum: BigInteger): Int
}