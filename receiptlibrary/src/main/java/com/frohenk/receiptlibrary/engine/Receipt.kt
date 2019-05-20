package com.frohenk.receiptlibrary.engine

import android.os.Parcelable
import com.frohenk.receiptlibrary.engine.ReceiptItem
import kotlinx.android.parcel.Parcelize
import java.math.BigInteger
import java.time.LocalDateTime

@Parcelize
data class Receipt(
    val dateTime: LocalDateTime,
    val fiscalDocumentNumber: BigInteger,
    val fiscalDriveNumber: BigInteger,
    val fiscalSign: BigInteger,
    val items: List<ReceiptItem>,
    val retailPlace: String,
    val retailPlaceAddress: String,
    val totalSum: BigInteger,
    val user: String,
    val userInn: BigInteger
) : Parcelable