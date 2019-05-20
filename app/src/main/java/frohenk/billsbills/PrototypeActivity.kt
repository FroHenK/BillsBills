package frohenk.billsbills

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.notbytes.barcode_reader.BarcodeReaderActivity
import kotlinx.android.synthetic.main.activity_prototype.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import android.R.attr.data
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.barcode.Barcode


const val SCANNER_REQUEST = 228

class PrototypeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prototype)
        button2.setOnClickListener(fun(_: View) {
            val intent = BarcodeReaderActivity.getLaunchIntent(this, true, false)//Intent(this, ScannerActivity::class.java)
            startActivityForResult(intent, SCANNER_REQUEST)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCANNER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return
            val barcode = data.getParcelableExtra<Barcode>(BarcodeReaderActivity.KEY_CAPTURED_BARCODE)

            val code = barcode.rawValue
            doAsync {

                val response = khttp.post("https://receipt-getter.herokuapp.com/", data = mapOf("data" to code))
                val responseJson =
                    response.jsonObject
                Log.d("FroHenK", "HTTP Request status: " + responseJson.getString("status"))

                uiThread {
                    val receipt = responseJson.getJSONObject("document").getJSONObject("receipt")
                    val builder = StringBuilder()
                    val items = receipt.getJSONArray("items")
                    for (i in 0 until items.length()) {
                        builder.append(items.getJSONObject(i).getString("name"))
                        builder.append(" * ")
                        builder.append(items.getJSONObject(i).getInt("quantity"))
                        builder.append(": ")
                        builder.append(items.getJSONObject(i).getInt("sum") / 100.0)
                        builder.append("\n")
                    }
                    builder.append("Sum: ")
                    builder.append(receipt.getInt("totalSum") / 100.0)
                    textView.text = builder.toString()
                    Toast.makeText(this@PrototypeActivity, code, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
