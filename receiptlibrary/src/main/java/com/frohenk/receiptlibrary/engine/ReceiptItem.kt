package com.frohenk.receiptlibrary.engine

import android.graphics.Color
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.frohenk.receiptlibrary.R
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
    enum class Category(val localedName: String, val color: Color, val drawableId: Int) {
        FOOD("Еда", Color.valueOf(Color.parseColor("#3cb44b")), R.drawable.food_apple),
        DRINKS("Напитки", Color.valueOf(Color.parseColor("#89CFF0")), R.drawable.cup_water),
        HEALTH("Здоровье", Color.valueOf(Color.parseColor("#800000")), R.drawable.pharmacy),
        HYGIENE_PERSONAL("Гигиена", Color.valueOf(Color.parseColor("#fabebe")), R.drawable.shower_head),
        HOUSE_CHEMICALS("Бытовая химия", Color.valueOf(Color.parseColor("#000075")), R.drawable.spray_bottle),
        TRANSPORT("Транспорт", Color.valueOf(Color.parseColor("#9A6324")), R.drawable.bus),
        PC("Компьютеры и комплектующие", Color.valueOf(Color.parseColor("#483C32")), R.drawable.laptop),
        ENTERTAINMENT("Развлечения", Color.valueOf(Color.parseColor("#013220")), R.drawable.gamepad_variant),
        OTHER("Прочее", Color.valueOf(Color.parseColor("#a9a9a9")), R.drawable.cart),
        UNDEFINED("Не выбрано", Color.valueOf(Color.parseColor("#000000")), R.drawable.help_rhombus)
    }

    val formattedPrice: String
        get() = MyFormatters.SUM_FORMAT.format(sum.toBigDecimal().divide(100.toBigDecimal()))

    val normalPrice: Double
        get() = price.toBigDecimal().divide(100.toBigDecimal()).toDouble()

    val normalTotalPrice: Double
        get() = normalPrice * (quantity.toInt())
}