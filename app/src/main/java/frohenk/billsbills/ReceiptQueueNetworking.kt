package frohenk.billsbills

import android.util.Log
import com.frohenk.receiptlibrary.engine.QueuedReceipt
import com.frohenk.receiptlibrary.engine.Receipt
import com.google.gson.GsonBuilder
import java.time.LocalDateTime

fun QueuedReceipt.getReceipt(): Receipt? {
    val response = khttp.post("https://receipt-getter.herokuapp.com/", data = mapOf("data" to this.toCode()))
    val jsonObject = response.jsonObject
    Log.i("kek", this.toCode() + " " + this.originalCode)
    if (jsonObject.getString("status") != "success")
        return null
    val gson = GsonBuilder().registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeConverter()).create()
    val receiptJson = jsonObject.getJSONObject("document").getJSONObject("receipt")
    val dateTime = LocalDateTime.parse(receiptJson.getString("dateTime"), QueuedReceipt.RECEIPT_DATE_TIME)
    receiptJson.put(
        "dateTime",
        gson.toJson(dateTime)
    )
    Log.i("kek", gson.toJson(dateTime) + " " + receiptJson.toString() + " " + this.toCode())
    return gson.fromJson<Receipt>(receiptJson.toString(), Receipt::class.java)
}