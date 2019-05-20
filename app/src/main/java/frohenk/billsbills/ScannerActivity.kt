package frohenk.billsbills

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    override fun handleResult(p0: Result?) {
        val code = p0.toString()
        Log.d("FroHenK", "Scanned code: $code")
        val data = Intent()
        data.data = Uri.parse(code)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private var scannerView: ZXingScannerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView(this)
        scannerView!!.setFormats(mutableListOf(BarcodeFormat.QR_CODE))

        setContentView(scannerView)
    }

    override fun onResume() {
        super.onResume()
        scannerView?.setResultHandler(this)
        scannerView?.startCamera()
//        CameraUtils.getCameraInstance().parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
//        CameraUtils.getCameraInstance().parameters.flashMode = Camera.Parameters.FLASH_MODE_ON

    }

    override fun onPause() {
        super.onPause()
        scannerView?.stopCamera()
    }
}
