package frohenk.billsbills

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.frohenk.receiptlibrary.engine.MyFormatters
import com.frohenk.receiptlibrary.engine.Receipt
import com.frohenk.receiptlibrary.engine.ReceiptItem
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.navigation.NavigationView
import frohenk.billsbills.database.MyDatabase
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.doAsync
import java.time.LocalDateTime

private const val receiptItemCategoryTag = 1337

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private var database: MyDatabase? = null

    var categoryChooserPopup: CategoryChooserPopup? = null
    var latestCategory: ReceiptItem.Category? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        database = MyDatabase.getDatabase(this)

        fab.setOnClickListener {
            run {
                val intent = Intent(this@MainActivity, ReceiptQueueActivity::class.java).apply {
                    putExtra(ReceiptQueueActivity.ACTION, ReceiptQueueActivity.SCAN)
                }
                startActivity(intent)
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)


        database?.receiptsDao()?.getAllFlowableData()?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { t ->
                run {
                    textView2.text = t.toString()
                    val receiptItems = t.map { it.receiptItems }.flatten()
                    val undefinedItems = receiptItems.filter { it.category == ReceiptItem.Category.UNDEFINED }
                    val toBeDeleted: ArrayList<View> = ArrayList()
                    for (i in 0 until linearLayout.childCount) {
                        val child = linearLayout.getChildAt(i)
                        if (child.getTag(receiptItemCategoryTag) == null) {
                            toBeDeleted.add(child)
                            continue
                        }
                        if (!undefinedItems.any { it.name == child.getTag(receiptItemCategoryTag) })
                            toBeDeleted.add(child)
                    }
                    for (child in toBeDeleted)
                        linearLayout.removeView(child)
                    val toBeAdded =
                        undefinedItems.map { Pair(it.name, it.receiptUid) }.toSet().take(3 - linearLayout.childCount)
                            .map { it1 ->
                                undefinedItems.first { it.name == it1.first && it.receiptUid == it1.second }
                            }
                    for (receiptItem in toBeAdded) {
                        val view = layoutInflater.inflate(R.layout.linear_receipt_category_chooser, null)
                        view.findViewById<TextView>(R.id.receiptItemNameTextView).text = receiptItem.name
                        view.findViewById<TextView>(R.id.receiptItemPriceTextView).text =
                            receiptItem.formattedPrice + " \u20BD"
                        view.findViewById<TextView>(R.id.receiptItemDateTimeTextView).text =
                            t.map { it.receipt }.first { it.uid == receiptItem.receiptUid }.dateTime.format(
                                MyFormatters.RECEIPT_DATE_TIME_HUMAN_YEAR_REVERSE
                            )
                        linearLayout.addView(view, linearLayout.childCount)
                        val receiptItemItemLayout = view.findViewById<View>(R.id.receiptItemItemLayout)
                        val onClickFunction: (View) -> Unit = {
                            for (i in 0 until linearLayout.childCount) {
                                val child = linearLayout.getChildAt(i)
                                child.findViewById<View>(R.id.receiptItemHiddenActions).visibility = View.GONE
                            }
                            doAsync(ExceptionHandler.errorLogger) {
                                runOnUiThread {
                                    categoryChooserPopup = CategoryChooserPopup(this@MainActivity, mainView)
                                    categoryChooserPopup?.onCategorySelectedListener = { category ->
                                        val updated =
                                            undefinedItems.filter { it.name == receiptItem.name && it.receiptUid == receiptItem.receiptUid }
                                        updated.forEach { it.category = category }
                                        doAsync(ExceptionHandler.errorLogger) {
                                            database?.receiptItemsDao()?.updateReceiptItems(updated)
                                        }
                                    }
                                    categoryChooserPopup?.show()
                                }
                            }
                        }
                        view.findViewById<View>(R.id.receiptItemEditLayout).visibility = View.GONE
                        view.findViewById<View>(R.id.receiptItemDeleteLayout).setOnClickListener {
                            doAsync(ExceptionHandler.errorLogger) {
                                undefinedItems.filter { it.name == receiptItem.name && it.receiptUid == receiptItem.receiptUid }
                                    .forEach { it.delete(database!!) }
                            }
                        }

                        receiptItemItemLayout.setOnClickListener(onClickFunction)
                        receiptItemItemLayout.setOnLongClickListener {
                            val hiddenActionsView = view.findViewById<View>(R.id.receiptItemHiddenActions)
                            if (hiddenActionsView.visibility == View.VISIBLE) {
                                hiddenActionsView.visibility = View.GONE
                                receiptItemItemLayout.setOnClickListener(onClickFunction)
                            } else {
                                for (i in 0 until linearLayout.childCount) {
                                    val child = linearLayout.getChildAt(i)
                                    child.findViewById<View>(R.id.receiptItemHiddenActions).visibility = View.GONE
                                }
                                hiddenActionsView.visibility = View.VISIBLE
                                receiptItemItemLayout.setOnClickListener {
                                    hiddenActionsView.visibility = View.GONE
                                    receiptItemItemLayout.setOnClickListener(onClickFunction)
                                }
                            }
                            true
                        }
                    }
                    uncategorizedCardView.visibility = if (linearLayout.childCount == 0) View.GONE else View.VISIBLE

                    //pie chart
                    val latestReceipts = t.map { it.receipt }//make it 7 day latest, or 30 days latest
                    val latestReceiptItems = latestReceipts.map { it.items }.flatten()
                    val latestSum = latestReceipts.sumByDouble {
                        it.normalSum
                    }
                    expenditureSum.text = MyFormatters.SUM_FORMAT.format(latestSum) + " \u20BD"
                    val pieDataSet = PieDataSet(ReceiptItem.Category.values().mapIndexed { index, category ->
                        PieEntry(latestReceiptItems.filter { it.category == category }.sumByDouble {
                            it.normalTotalPrice
                        }.toFloat(), category)
                    }, "Расходы по категориям")
                    pieDataSet.colors =
                        ColorTemplate.createColors(ReceiptItem.Category.values().map { it.color.toArgb() }.toIntArray())
                    pieDataSet.setDrawValues(false)

                    expenditrePieChart.data = PieData(pieDataSet)
                    expenditrePieChart.isSelected = true
                    expenditrePieChart.isSelected = false
                    expenditrePieChart.setDrawEntryLabels(false)
                    expenditrePieChart.legend.isEnabled = false
                    expenditrePieChart.holeRadius = 60F
                    expenditrePieChart.description.isEnabled = false
                    expenditrePieChart.setDrawMarkers(false)
                    expenditrePieChart.setDrawCenterText(false)
                    expenditrePieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onNothingSelected() {
                            chartSelectedCategoryTextView.text = "Все"
                            chartSelectedCategorySumTextView.text = latestSum.toInt().toString() + " \u20BD"
                            chartSelectedCategoryPercentTextView.visibility = View.GONE
                        }

                        override fun onValueSelected(e: Entry?, h: Highlight?) {
                            val category = e!!.data as ReceiptItem.Category
                            latestCategory = category
                            chartSelectedCategoryTextView.text = category.localedName
                            val categorySum = latestReceiptItems.filter { it.category == category }
                                .sumByDouble { it.normalTotalPrice }
                            chartSelectedCategorySumTextView.text = categorySum.toInt().toString() + " \u20BD"
                            chartSelectedCategoryPercentTextView.visibility = View.VISIBLE
                            chartSelectedCategoryPercentTextView.text =
                                ((categorySum / latestSum) * 100).toInt().toString() + "%"
                        }

                    })
                    if (latestCategory == null) {
                        chartSelectedCategoryTextView.text = "Все"
                        chartSelectedCategorySumTextView.text = latestSum.toInt().toString() + " \u20BD"
                        chartSelectedCategoryPercentTextView.visibility = View.GONE
                    } else {
                        chartSelectedCategoryTextView.text = latestCategory?.localedName
                        val categorySum = latestReceiptItems.filter { it.category == latestCategory }
                            .sumByDouble { it.normalTotalPrice }
                        chartSelectedCategorySumTextView.text = categorySum.toInt().toString() + " \u20BD"
                        chartSelectedCategoryPercentTextView.visibility = View.VISIBLE
                        chartSelectedCategoryPercentTextView.text =
                            ((categorySum / latestSum) * 100).toInt().toString() + "%"
                    }
                }
                run {
                    //recent items
                    val receiptItems = t.sortedByDescending { it.receipt.dateTime }.map { it.receiptItems }.flatten()
                        .filter { it.category != ReceiptItem.Category.UNDEFINED }

                    latestItemsLinearLayout.removeAllViews()
                    val toBeAdded = receiptItems.map { Pair(it.name, it.receiptUid) }.toSet().take(4)
                        .map { it1 ->
                            receiptItems.first { it.name == it1.first && it.receiptUid == it1.second }
                        }
                    for (receiptItem in toBeAdded) {
                        val view = layoutInflater.inflate(R.layout.linear_receipt_category_chooser, null)
                        view.findViewById<TextView>(R.id.receiptItemNameTextView).text = receiptItem.name
                        view.findViewById<TextView>(R.id.receiptItemPriceTextView).text =
                            receiptItem.formattedPrice + " \u20BD"
                        view.findViewById<TextView>(R.id.receiptItemDateTimeTextView).text =
                            t.map { it.receipt }.first { it.uid == receiptItem.receiptUid }.dateTime.format(
                                MyFormatters.RECEIPT_DATE_TIME_HUMAN_YEAR_REVERSE
                            )
                        latestItemsLinearLayout.addView(view, latestItemsLinearLayout.childCount)
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
                                for (i in 0 until latestItemsLinearLayout.childCount) {
                                    val child = latestItemsLinearLayout.getChildAt(i)
                                    child.findViewById<View>(R.id.receiptItemHiddenActions).visibility = View.GONE
                                }
                                hiddenActionsView.visibility = View.VISIBLE
                            }
                        }

                        receiptItemItemLayout.setOnLongClickListener {
                            val hiddenActionsView = view.findViewById<View>(R.id.receiptItemHiddenActions)
                            if (hiddenActionsView.visibility == View.VISIBLE) {
                                hiddenActionsView.visibility = View.GONE
                            } else {
                                for (i in 0 until latestItemsLinearLayout.childCount) {
                                    val child = latestItemsLinearLayout.getChildAt(i)
                                    child.findViewById<View>(R.id.receiptItemHiddenActions).visibility = View.GONE
                                }
                                hiddenActionsView.visibility = View.VISIBLE
                            }
                            true
                        }

                        val imageView = view.findViewById<ImageView>(R.id.receiptItemImageView)
                        imageView.visibility=View.VISIBLE
                        val drawable = this@MainActivity.getDrawable(receiptItem.category.drawableId)
                        drawable.setColorFilter(receiptItem.category.color.toArgb(), PorterDuff.Mode.SRC_ATOP)
                        imageView.setImageDrawable(drawable)
                    }
                }
            }
        button.setOnClickListener {
            var itemsList = ArrayList<ReceiptItem>()
            val itemsSize = (1..3).shuffled().first()
            var totalSum = 0.toBigInteger()
            for (i in 1..itemsSize) {
                val price = (1000..10000).shuffled().first().toBigInteger()
                itemsList.add(ReceiptItem("dsadsa" + LocalDateTime.now(), price, 1.toBigInteger(), price))
                totalSum += price
            }
            val receipt = Receipt(
                LocalDateTime.now()
                    .minusMinutes((0..59).shuffled().first().toLong())
                    .minusHours((0..23).shuffled().first().toLong())
                    .minusDays((0..6).shuffled().first().toLong()),
                System.currentTimeMillis().toBigInteger(),
                System.currentTimeMillis().toBigInteger(),
                System.currentTimeMillis().toBigInteger(),
                null,
                null,
                totalSum,
                "kek",
                228.toBigInteger()
            )
            receipt.items = itemsList
            doAsync(ExceptionHandler.errorLogger) {
                receipt.addToDatabase(database!!)
            }
        }

        nukeDatabaseButton.setOnClickListener {
            doAsync(ExceptionHandler.errorLogger) {
                MyDatabase.nukeDatabase(this@MainActivity)
            }
        }
    }

    override fun onBackPressed() {
        if (categoryChooserPopup?.isShowing == true) {
            categoryChooserPopup?.dismiss()
            return
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_queue -> {
                val intent = Intent(this, ReceiptQueueActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
