package com.frohenk.receiptlibrary.engine

import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.frohenk.receiptlibrary.engine.MyFormatters.Companion.SUM_FORMAT
import kotlinx.android.parcel.Parcelize
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField.*
import java.util.*

//t=20190420T1857&s=352.11&fn=9288000100115747&i=99052&fp=844102519&n=1
@Parcelize
@Entity
data class QueuedReceipt(
    @ColumnInfo(name = "dateTime") val dateTime: LocalDateTime,
    @ColumnInfo(name = "sum") val sum: BigInteger,
    @ColumnInfo(name = "fiscalDriveNumber") val fiscalDriveNumber: BigInteger,
    @ColumnInfo(name = "fiscalDocumentNumber") val fiscalDocumentNumber: BigInteger,
    @ColumnInfo(name = "fiscalSign") val fiscalSign: BigInteger,
    @ColumnInfo(name = "originalCode") val originalCode: String,
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "status") var status: QueuedReceiptStatus = QueuedReceiptStatus.LATER,
    @ColumnInfo(name = "visible") var visible: Boolean = true
) : Parcelable {

    enum class QueuedReceiptStatus(val statusMessage: String) {
        LATER("Чек ещё не был получен"), TRYING("Подключение к серверу..."), READY("Чек был добавлен в ваш список")
    }

    companion object {


        val RECEIPT_DATE_TIME: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseSensitive()
            .appendValue(YEAR, 4)
            .optionalStart()
            .appendLiteral('-')
            .optionalEnd()
            .appendValue(MONTH_OF_YEAR, 2)
            .optionalStart()
            .appendLiteral('-')
            .optionalEnd()
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .optionalStart()
            .appendLiteral(':')
            .optionalEnd()
            .optionalStart()
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .optionalEnd()
            .optionalStart()
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter()

        val RECEIPT_DATE_TIME_ENCODED: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseSensitive()
            .appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendValue(MINUTE_OF_HOUR, 2)
            .toFormatter()

        fun isValid(code: String): Boolean {
            try {

                val uri = Uri.parse("www.example.com/index.php?$code")

                if (uri.getQueryParameter("t") == null ||
                    LocalDateTime.parse(uri.getQueryParameter("t"), RECEIPT_DATE_TIME) == null
                )
                    return false
                if (uri.getQueryParameter("s")?.toFloatOrNull() == null)
                    return false
                if (uri.getQueryParameter("fn")?.toBigDecimalOrNull() == null)
                    return false
                if (uri.getQueryParameter("i")?.toBigDecimalOrNull() == null)
                    return false
                if (uri.getQueryParameter("fp")?.toBigDecimalOrNull() == null)
                    return false
                if (uri.getQueryParameter("n")?.toIntOrNull() != 1)
                    return false
                return true
            } catch (e: Exception) {
                Log.v("kek", "exception during url validity check", e)
                return false
            }
        }

        fun parse(code: String): QueuedReceipt {
            val uri = Uri.parse("www.example.com/index.php?$code")
            val time = LocalDateTime.parse(uri.getQueryParameter("t"), RECEIPT_DATE_TIME)
            val sum = (uri.getQueryParameter("s").toFloat() * 100).toBigDecimal().toBigInteger()
            val fiscalDriveNumber = uri.getQueryParameter("fn").toBigInteger()
            val fiscalDocumentNumber = uri.getQueryParameter("i").toBigInteger()
            val fiscalSign = uri.getQueryParameter("fp").toBigInteger()

            return QueuedReceipt(time, sum, fiscalDriveNumber, fiscalDocumentNumber, fiscalSign, code)
        }

        fun parseOrNull(code: String): QueuedReceipt? {
            if (!isValid(code))
                return null
            return parse(code)
        }
    }

    val formattedSum: String
        get() = SUM_FORMAT.format(sum.toBigDecimal().divide(100.toBigDecimal()))

    fun toCode(): String {
        return StringBuilder().apply {
            append("t=")
            append(dateTime.format(RECEIPT_DATE_TIME_ENCODED))

            append("&s=")
            append(formattedSum)

            append("&fn=")
            append(fiscalDriveNumber)

            append("&i=")
            append(fiscalDocumentNumber)

            append("&fp=")
            append(fiscalSign)

            append("&n=1")

        }.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QueuedReceipt

        if (dateTime != other.dateTime) return false
        if (sum != other.sum) return false
        if (fiscalDriveNumber != other.fiscalDriveNumber) return false
        if (fiscalDocumentNumber != other.fiscalDocumentNumber) return false
        if (fiscalSign != other.fiscalSign) return false
        if (originalCode != other.originalCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dateTime.hashCode()
        result = 31 * result + sum.hashCode()
        result = 31 * result + fiscalDriveNumber.hashCode()
        result = 31 * result + fiscalDocumentNumber.hashCode()
        result = 31 * result + fiscalSign.hashCode()
        result = 31 * result + originalCode.hashCode()
        return result
    }

}