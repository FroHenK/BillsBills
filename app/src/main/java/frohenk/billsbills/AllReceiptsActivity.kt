package frohenk.billsbills

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.frohenk.receiptlibrary.engine.MyFormatters
import com.frohenk.receiptlibrary.engine.ReceiptItem
import frohenk.billsbills.database.MyDatabase
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_all_receipts.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.doAsync

class AllReceiptsActivity : AppCompatActivity() {
    private var database: MyDatabase? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_receipts)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = MyDatabase.getDatabase(this)

        database?.receiptsDao()?.getAllFlowableData()?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { t ->
                run {
                    val receiptItems = t.sortedByDescending { it.receipt.dateTime }.map { it.receiptItems }.flatten()
                        .filter { it.category != ReceiptItem.Category.UNDEFINED }
                    var receipts =
                        t.filter { it.receipt.totalSum != 0.toBigInteger() }.sortedByDescending { it.receipt.dateTime }
                    allReceiptsLayout.removeAllViews()
                    var totalSum = 0.0
                    for (receipt in receipts) {
                        totalSum += receipt.receipt.normalSum
                        val receiptView = layoutInflater.inflate(R.layout.linear_receipt_header, null)
                        allReceiptsLayout.addView(receiptView)
                        receiptView.findViewById<TextView>(R.id.receiptSumTextView).text =
                            MyFormatters.SUM_FORMAT.format(receipt.receipt.normalSum) + " \u20BD"
                        receiptView.findViewById<TextView>(R.id.receiptTimeTextView).text =
                            receipt.receipt.dateTime.format(MyFormatters.RECEIPT_DATE_TIME_HUMAN_YEAR)
                        for (receiptItem in receipt.receiptItems) {
                            val view = layoutInflater.inflate(R.layout.linear_receipt_category_chooser, null)
                            view.findViewById<TextView>(R.id.receiptItemNameTextView).text = receiptItem.name
                            view.findViewById<TextView>(R.id.receiptItemPriceTextView).text =
                                receiptItem.formattedPrice + " \u20BD"
                            view.findViewById<TextView>(R.id.receiptItemDateTimeTextView).text =
                                t.map { it.receipt }.first { it.uid == receiptItem.receiptUid }.dateTime.format(
                                    MyFormatters.RECEIPT_DATE_TIME_HUMAN_YEAR_REVERSE
                                )
                            allReceiptsLayout.addView(view, allReceiptsLayout.childCount)
                            val receiptItemItemLayout = view.findViewById<View>(R.id.receiptItemItemLayout)
                            view.findViewById<View>(R.id.receiptItemEditLayout).visibility = View.GONE
                            view.findViewById<View>(R.id.receiptItemDeleteLayout).setOnClickListener {
                                doAsync(ExceptionHandler.errorLogger) {
                                    receiptItems.filter { it.name == receiptItem.name && it.receiptUid == receiptItem.receiptUid }
                                        .forEach { it.delete(database!!) }
                                }
                            }
                            receiptItemItemLayout.setOnClickListener {
                                val hiddenActionsView = view.findViewById<View>(R.id.receiptItemHiddenActions)
                                if (hiddenActionsView.visibility == View.VISIBLE) {
                                    hiddenActionsView.visibility = View.GONE
                                } else {
                                    for (i in 0 until allReceiptsLayout.childCount) {
                                        val child = allReceiptsLayout.getChildAt(i)
                                        child.findViewById<View>(R.id.receiptItemHiddenActions)?.visibility = View.GONE
                                    }
                                    hiddenActionsView.visibility = View.VISIBLE
                                }
                            }

                            receiptItemItemLayout.setOnLongClickListener {
                                val hiddenActionsView = view.findViewById<View>(R.id.receiptItemHiddenActions)
                                if (hiddenActionsView.visibility == View.VISIBLE) {
                                    hiddenActionsView.visibility = View.GONE
                                } else {
                                    for (i in 0 until allReceiptsLayout.childCount) {
                                        val child = allReceiptsLayout.getChildAt(i)
                                        child.findViewById<View>(R.id.receiptItemHiddenActions)?.visibility = View.GONE
                                    }
                                    hiddenActionsView.visibility = View.VISIBLE
                                }
                                true
                            }

                            val imageView = view.findViewById<ImageView>(R.id.receiptItemImageView)
                            if (receiptItem.category != ReceiptItem.Category.UNDEFINED)
                                imageView.visibility = View.VISIBLE
                            val drawable = this@AllReceiptsActivity.getDrawable(receiptItem.category.drawableId)
                            drawable.setColorFilter(receiptItem.category.color.toArgb(), PorterDuff.Mode.SRC_ATOP)
                            imageView.setImageDrawable(drawable)
                        }
                    }
                    allReceiptsSumTextView.text = MyFormatters.SUM_FORMAT.format(totalSum) + " \u20BD"
                }
            }
    }
}
