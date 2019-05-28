package com.frohenk.receiptlibrary.engine

import android.os.Parcelable
import androidx.room.*
import com.frohenk.receiptlibrary.engine.ReceiptItem
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.math.BigInteger
import java.time.LocalDateTime

@Entity
@Parcelize
data class Receipt(
    @ColumnInfo(name = "dateTime") val dateTime: LocalDateTime,
    @ColumnInfo(name = "fiscalDocumentNumber") val fiscalDocumentNumber: BigInteger,
    @ColumnInfo(name = "fiscalDriveNumber") val fiscalDriveNumber: BigInteger,
    @ColumnInfo(name = "fiscalSign") val fiscalSign: BigInteger,
    @ColumnInfo(name = "retailPlace") val retailPlace: String?,
    @ColumnInfo(name = "retailPlaceAddress") val retailPlaceAddress: String?,
    @ColumnInfo(name = "totalSum") var totalSum: BigInteger,
    @ColumnInfo(name = "user") val user: String?,
    @ColumnInfo(name = "userInn") val userInn: BigInteger?,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0
) : Parcelable {
    @IgnoredOnParcel
    @Ignore
    var items: List<ReceiptItem> = ArrayList()


    val normalSum: Double
        get() = totalSum.toBigDecimal().divide(100.toBigDecimal()).toDouble()
}