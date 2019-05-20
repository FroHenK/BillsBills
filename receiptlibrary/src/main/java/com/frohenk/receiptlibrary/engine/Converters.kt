package com.frohenk.receiptlibrary.engine

import androidx.room.TypeConverter
import java.math.BigInteger
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun toLocalDateTimeFromString(value: String?): LocalDateTime? {
        return if (value == null) null else LocalDateTime.parse(value, QueuedReceipt.RECEIPT_DATE_TIME)
    }

    @TypeConverter
    fun toStringFromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(QueuedReceipt.RECEIPT_DATE_TIME)
    }

    @TypeConverter
    fun toBigIntegerFromString(value: String?): BigInteger? {
        return value?.toBigInteger()
    }

    @TypeConverter
    fun toStringFromBigInteger(value: BigInteger?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toQueuedReceiptStatusFromString(value: String?): QueuedReceipt.QueuedReceiptStatus? {
        return if (value == null) null else QueuedReceipt.QueuedReceiptStatus.valueOf(value)
    }

    @TypeConverter
    fun toStringFromQueuedReceiptStatus(value: QueuedReceipt.QueuedReceiptStatus?): String? {
        return value?.toString()
    }
}