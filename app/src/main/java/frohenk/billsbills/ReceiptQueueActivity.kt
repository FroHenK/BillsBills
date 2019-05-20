package frohenk.billsbills

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.frohenk.receiptlibrary.engine.QueuedReceipt
import com.google.android.material.snackbar.Snackbar
import com.notbytes.barcode_reader.BarcodeReaderActivity
import frohenk.billsbills.database.MyDatabase
import frohenk.billsbills.database.QueuedReceiptDao
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_receipt_queue.*
import org.jetbrains.anko.doAsync
import java.util.*

class ReceiptQueueActivity : AppCompatActivity() {

    companion object {
        const val ACTION = "action"
        const val SCAN = "scan"
    }

    private var timer: Timer? = null
    private var firstTime: Boolean = true
    private var adapter: QueuedReceiptAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = QueuedReceiptAdapter(this@ReceiptQueueActivity, R.layout.card_queued_receipt)

        queuedReceiptDao = MyDatabase.getDatabase(this@ReceiptQueueActivity).queuedReceiptsDao()
        setContentView(R.layout.activity_receipt_queue)
        if (intent.hasExtra(ACTION))
            when (intent.getStringExtra(ACTION)) {
                SCAN -> {
                    startScanner()
                }
            }

        val queuedReceiptsList = queuedReceiptDao!!.getAllFlowable()
        queuedReceiptsList.observeOn(AndroidSchedulers.mainThread()).subscribe { t: List<QueuedReceipt>? ->
            run {
                if (firstTime) {
                    firstTime = false
                    val updated = t?.filter { it.status == QueuedReceipt.QueuedReceiptStatus.TRYING }
                    updated?.forEach { it.status = QueuedReceipt.QueuedReceiptStatus.LATER }
                    if (updated != null) {
                        doAsync {
                            queuedReceiptDao?.updateReceipts(updated)
                        }
                    }
                }
                adapter?.clear()
                adapter?.addAll(t?.filter { queuedReceipt -> queuedReceipt.visible }?.sortedByDescending { queuedReceipt -> queuedReceipt.uid })
                queueListView.adapter = adapter
            }
        }

        fab.setOnClickListener {
            run {
                startScanner()
            }
        }
    }

    private fun startScanner() {
        val intent = BarcodeReaderActivity.getLaunchIntent(
            this,
            true,
            false
        )
        startActivityForResult(intent, SCANNER_REQUEST)
    }

    private var queuedReceiptDao: QueuedReceiptDao? = null
    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            timer?.cancel()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCANNER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return
            val receipt = data.getParcelableExtra<QueuedReceipt>(BarcodeReaderActivity.KEY_CAPTURED_RECEIPT)

            doAsync {

                val queue = queuedReceiptDao!!.getAll()
                if (!queue.contains(receipt)) {
                    queuedReceiptDao!!.insert(receipt)
                } else

                    runOnUiThread {
                        Snackbar.make(
                            findViewById(R.id.context),
                            "Чек уже был отсканирован до этого",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
            }

            runOnUiThread {
                //Toast.makeText(this, code, Toast.LENGTH_LONG).show()
                Toast.makeText(this, receipt.toCode(), Toast.LENGTH_LONG).show()
                Log.i("kek", "scanned receipt: ${receipt.toCode()}, $receipt")

            }
        }
    }
}