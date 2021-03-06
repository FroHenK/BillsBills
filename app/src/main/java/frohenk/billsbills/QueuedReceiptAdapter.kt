package frohenk.billsbills

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.frohenk.receiptlibrary.engine.MyFormatters.Companion.RECEIPT_DATE_TIME_HUMAN_YEAR
import com.frohenk.receiptlibrary.engine.QueuedReceipt
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import frohenk.billsbills.database.MyDatabase
import org.jetbrains.anko.doAsync

class QueuedReceiptAdapter(context: Context, val resourceLayout: Int) :
    ArrayAdapter<QueuedReceipt>(context, resourceLayout) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(resourceLayout, null)
        }
        val item = getItem(position)
        if (item != null) {
            view!!.findViewById<TextView>(R.id.sumTextView).text = "${item.formattedSum} \u20BD"
            view.findViewById<TextView>(R.id.queueStatusTextView).text = item.trueStatus.statusMessage
            view.findViewById<TextView>(R.id.queueTimeTextView).text =
                item.dateTime.format(RECEIPT_DATE_TIME_HUMAN_YEAR)

            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(item.originalCode, BarcodeFormat.QR_CODE, 200, 200)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            view.findViewById<ImageView>(R.id.queuePreviewImageView).setImageBitmap(bitmap)

            view.findViewById<ImageButton>(R.id.removeFromQueueImageButton).setOnClickListener {
                run {
                    doAsync {
                        val queuedReceiptsDao = MyDatabase.getDatabase(context).queuedReceiptsDao()
                        when (item.trueStatus) {
                            QueuedReceipt.QueuedReceiptStatus.LATER -> queuedReceiptsDao.delete(item)
                            QueuedReceipt.QueuedReceiptStatus.READY -> {
                                item.visible = false
                                queuedReceiptsDao.updateReceipts(item)
                            }
                        }
                    }
                }
            }

            view.findViewById<Button>(R.id.queueRetryButton).setOnClickListener {
                run {
                    doAsync {
                        try {
                            item.fetchReceiptToDatabase(MyDatabase.getDatabase(context))
                        } catch (e: Exception) {
                            Log.e("kek", "error", e)
                        }
                    }
                }
            }


            view.findViewById<ImageButton>(R.id.removeFromQueueImageButton).isEnabled =
                item.trueStatus != QueuedReceipt.QueuedReceiptStatus.TRYING
            view.findViewById<ImageButton>(R.id.removeFromQueueImageButton).visibility =
                if (view.findViewById<ImageButton>(R.id.removeFromQueueImageButton).isEnabled) View.VISIBLE else View.GONE

            view.findViewById<Button>(R.id.queueRetryButton).isEnabled =
                item.trueStatus == QueuedReceipt.QueuedReceiptStatus.LATER
            view.findViewById<Button>(R.id.queueRetryButton).visibility =
                if (view.findViewById<Button>(R.id.queueRetryButton).isEnabled) View.VISIBLE else View.GONE
        }
        return view
    }

}