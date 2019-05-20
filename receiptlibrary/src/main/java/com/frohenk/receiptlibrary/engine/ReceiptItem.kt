package com.frohenk.receiptlibrary.engine

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.math.BigInteger

@Entity(
    foreignKeys = [ForeignKey(
        entity = Receipt::class,
        parentColumns = ["uid"],
        childColumns = ["receiptUid"],
        onDelete = CASCADE
    )]
)
@Parcelize
data class ReceiptItem(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "price") val price: BigInteger,
    @ColumnInfo(name = "quantity") var quantity: BigInteger,
    @ColumnInfo(name = "sum") val sum: BigInteger,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "receiptUid") var receiptUid: Int = 0,
    @ColumnInfo(name = "category") var category: Category = Category.OTHER
) : Parcelable {
    enum class Category {
        FOOD,
        DRINKS,
        HEALTH,
        HYGIENE_PERSONAL,
        HOUSE_CHEMICALS,
        TRANSPORT,
        PC,
        ENTERTAINMENT,
        OTHER
    }
}