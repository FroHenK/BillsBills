package frohenk.billsbills

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.frohenk.receiptlibrary.engine.QueuedReceipt
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import frohenk.billsbills.database.MyDatabase
import org.jetbrains.anko.doAsync
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

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
//            view.findViewById<TextView>(R.id.queueStatusTextView).text = "${item.formattedSum} \u20BD"
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
                        when (item.status) {
                            QueuedReceipt.QueuedReceiptStatus.LATER -> queuedReceiptsDao.delete(item)
                            QueuedReceipt.QueuedReceiptStatus.READY -> {
                                item.visible = false
                                queuedReceiptsDao.updateReceipts(item)
                            }
                        }
                    }
                }
            }
        }
        return view
    }

    companion object {

        val RECEIPT_DATE_TIME_HUMAN_NO_YEAR: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseSensitive()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(' ')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('/')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .toFormatter()

        val RECEIPT_DATE_TIME_HUMAN_YEAR: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseSensitive()

            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('/')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('/')
            .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .toFormatter()

    }
}