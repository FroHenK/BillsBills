package frohenk.billsbills

import android.util.Log
import com.frohenk.receiptlibrary.engine.QueuedReceipt
import com.frohenk.receiptlibrary.engine.Receipt
import com.frohenk.receiptlibrary.engine.ReceiptItem
import com.google.gson.GsonBuilder
import frohenk.billsbills.database.MyDatabase
import java.lang.Exception
import java.time.LocalDateTime

fun QueuedReceipt.getReceipt(): Receipt? {
    try {
        this.status = QueuedReceipt.QueuedReceiptStatus.TRYING
        MyDatabase.database?.queuedReceiptsDao()?.updateReceipts(this)

        val response = khttp.post("https://receipt-getter.herokuapp.com/", data = mapOf("data" to this.toCode()))
        val jsonObject = response.jsonObject
//    Log.i("kek", this.toCode() + " " + this.originalCode)
        if (jsonObject.getString("status") != "success")
            return null
        val gson = GsonBuilder().registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeConverter()).create()
        val receiptJson = jsonObject.getJSONObject("document").getJSONObject("receipt")
        val dateTime = LocalDateTime.parse(receiptJson.getString("dateTime"), QueuedReceipt.RECEIPT_DATE_TIME)
        receiptJson.put(
            "dateTime",
            gson.toJson(dateTime)
        )
        Log.v("kek", gson.toJson(dateTime) + " " + receiptJson.toString() + " " + this.toCode())

        this.status = QueuedReceipt.QueuedReceiptStatus.LATER
        MyDatabase.database?.queuedReceiptsDao()?.updateReceipts(this)

        return gson.fromJson<Receipt>(receiptJson.toString(), Receipt::class.java)
    } catch (e: Exception) {
        this.status = QueuedReceipt.QueuedReceiptStatus.LATER
        MyDatabase.database?.queuedReceiptsDao()?.updateReceipts(this)
        throw e
    }
}

fun QueuedReceipt.fetchReceiptToDatabase(database: MyDatabase) {
    val receipt = this.getReceipt()
    receipt?.addToDatabase(database)
}

@Synchronized
fun Receipt.addToDatabase(database: MyDatabase) {
    val receiptsDao = database.receiptsDao()
    val receiptItemsDao = database.receiptItemsDao()
    if (receiptsDao.count(this.fiscalDocumentNumber, this.totalSum) != 0)
        return//already in the database

    receiptsDao.insert(this)
    val me = receiptsDao.getByFiscalAndSum(this.fiscalDocumentNumber, this.totalSum).blockingGet()
    this.items.forEach {
        it.receiptUid = me.receipt.uid
        it.category = ReceiptItem.Category.UNDEFINED
    }
    receiptItemsDao.insertAll(this.items)

    database.queuedReceiptsDao().apply {
        getByFiscalAndSum(this@addToDatabase.fiscalDocumentNumber, this@addToDatabase.totalSum).subscribe { it ->
            it.status = QueuedReceipt.QueuedReceiptStatus.READY
            this@apply.updateReceipts(it)
        }
    }
}
