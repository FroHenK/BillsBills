package frohenk.billsbills

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ListView
import android.widget.PopupWindow
import com.frohenk.receiptlibrary.engine.ReceiptItem


class CategoryChooserPopup(val context: Context, val parent: View) {

    var onCategorySelectedListener: ((ReceiptItem.Category) -> Unit)? = null
    val popupWindow: PopupWindow
    val view: View
    val isShowing: Boolean
        get() = popupWindow.isShowing
    var categoriesListView: ListView
    var categoryAdapter: CategoryAdapter

    private val categoriesList: List<ReceiptItem.Category> =
        ReceiptItem.Category.values().filter { it != ReceiptItem.Category.UNDEFINED }


    init {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = layoutInflater.inflate(R.layout.popup_receipt_item_category_chooser, null)
        popupWindow = PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        view.findViewById<View>(R.id.popupMainView).setOnClickListener {
            dismiss()
        }
        view.findViewById<View>(R.id.dismissPopupImageButton).setOnClickListener {
            dismiss()
        }

        categoriesListView = view.findViewById(R.id.categoriesListView)
        categoryAdapter = CategoryAdapter(context, R.layout.list_item_category)
        categoryAdapter.addAll(categoriesList)
        categoriesListView.setOnItemClickListener { _, _, position, _ ->
            onCategorySelectedListener?.invoke(
                categoriesList[position]
            )
            dismiss()
        }
        categoriesListView.adapter = categoryAdapter
    }

    fun show() {
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0)
    }

    fun dismiss() {
        val animateSlideUp = TranslateAnimation(0F, 0F, 0F, view.height.toFloat())
        animateSlideUp.duration = 200
        animateSlideUp.fillAfter = true
        view.startAnimation(animateSlideUp)
        animateSlideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                // actually, I don't need this method but I have to implement this.
            }

            override fun onAnimationEnd(animation: Animation?) {
                popupWindow.dismiss()
            }

            override fun onAnimationStart(animation: Animation?) {
                // actually, I don't need this method but I have to implement this.
            }
        })
    }

}