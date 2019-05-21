package com.frohenk.receiptlibrary.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

annotation class MyFormatters {
    companion object{
        val RECEIPT_DATE_TIME_HUMAN_YEAR_REVERSE: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseSensitive()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(' ')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('/')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('/')
            .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
            .toFormatter()
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
         val SUM_FORMAT = DecimalFormat("#.00").apply {
            decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT)
        }
    }
}