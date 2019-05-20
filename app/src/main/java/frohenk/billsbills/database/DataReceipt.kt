package frohenk.billsbills.database

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.frohenk.receiptlibrary.engine.Receipt
import com.frohenk.receiptlibrary.engine.ReceiptItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DataReceipt(
    @Embedded private var mReceipt: Receipt? = null,
    @Relation(
        parentColumn = "uid",
        entityColumn = "receiptUid",
        entity = ReceiptItem::class
    ) var receiptItems: List<ReceiptItem> = listOf()
) : Parcelable {
    val receipt: Receipt
        get() {
            if (mReceipt!!.items.isEmpty()) {
                mReceipt!!.items += receiptItems
            }
            return mReceipt!!
        }
}