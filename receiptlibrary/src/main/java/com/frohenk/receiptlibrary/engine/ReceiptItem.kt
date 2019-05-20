package com.frohenk.receiptlibrary.engine

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.math.BigInteger

@Parcelize
data class ReceiptItem(val name: String, val price: BigInteger, val quantity: BigInteger, val sum: BigInteger) : Parcelable