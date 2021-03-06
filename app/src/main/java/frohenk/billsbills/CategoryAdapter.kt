package frohenk.billsbills

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.frohenk.receiptlibrary.engine.ReceiptItem


class CategoryAdapter(context: Context, val resourceLayout: Int) :
    ArrayAdapter<ReceiptItem.Category>(context, resourceLayout) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(resourceLayout, null)
        }

        val item = getItem(position)
        if (item != null) {
            view!!.findViewById<TextView>(R.id.categoryTextView).text = item.localedName
            val drawable = context.getDrawable(item.drawableId)
            drawable.setColorFilter(item.color.toArgb(),PorterDuff.Mode.SRC_ATOP)
            view.findViewById<ImageView>(R.id.categoryImageView).setImageDrawable(drawable)
        }
        return view
    }

}