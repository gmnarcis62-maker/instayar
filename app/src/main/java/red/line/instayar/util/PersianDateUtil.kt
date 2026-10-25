package red.line.instayar.util

import java.util.Calendar

/**
 * ابزار تبدیل و فرمت‌بندی تاریخ و ساعت هجری شمسی و ارقام فارسی
 * به صورت کاملاً مستقل، دقیق، بدون نیاز به اینترنت و بدون کتابخانه خارجی.
 */
object PersianDateUtil {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    private val PERSIAN_MONTH_NAMES = arrayOf(
        "", // 1-based index
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val PERSIAN_WEEK_DAYS = mapOf(
        Calendar.SATURDAY to "شنبه",
        Calendar.SUNDAY to "یکشنبه",
        Calendar.MONDAY to "دوشنبه",
        Calendar.TUESDAY to "سه‌شنبه",
        Calendar.WEDNESDAY to "چهارشنبه",
        Calendar.THURSDAY to "پنجشنبه",
        Calendar.FRIDAY to "جمعه"
    )

    data class PersianDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val dayOfWeekName: String,
        val monthName: String,
        val fullFormattedText: String
    )

    /**
     * تبدیل تمام اعداد انگلیسی درون یک رشته به اعداد فارسی (۱۲۳)
     */
    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(PERSIAN_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toPersianDigits(number: Number): String {
        return toPersianDigits(number.toString())
    }

    /**
     * الگوریتم استاندارد و بسیار دقیق تبدیل میلادی به هجری شمسی
     */
    fun gregorianToPersian(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val isLeapYear = (gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0)
        if (isLeapYear) {
            gDaysInMonth[2] = 29
        }

        val gy2 = if (gMonth > 2) (gYear + 1) else gYear
        var days = 355666 + (365 * gYear) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gDay
        for (i in 0 until gMonth) {
            days += gDaysInMonth[i]
        }

        var jYear = -1595 + (33 * (days / 12053))
        days %= 12053
        jYear += 4 * (days / 1461)
        days %= 1461

        if (days > 365) {
            jYear += ((days - 1) / 365)
            days = (days - 1) % 365
        }

        val jMonth = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jDay = 1 + if (days < 186) (days % 31) else ((days - 186) % 30)

        return Triple(jYear, jMonth, jDay)
    }

    /**
     * دریافت تاریخ شمسی فعلی یا بر اساس میلی‌ثانیه
     */
    fun getPersianDate(timeInMillis: Long = System.currentTimeMillis()): PersianDate {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1 // 1..12
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val (pYear, pMonth, pDay) = gregorianToPersian(gYear, gMonth, gDay)
        val dayOfWeekName = PERSIAN_WEEK_DAYS[dayOfWeek] ?: "امروز"
        val monthName = PERSIAN_MONTH_NAMES.getOrNull(pMonth) ?: ""

        val formattedText = "$dayOfWeekName ${toPersianDigits(pDay)} $monthName ${toPersianDigits(pYear)}"

        return PersianDate(
            year = pYear,
            month = pMonth,
            day = pDay,
            dayOfWeekName = dayOfWeekName,
            monthName = monthName,
            fullFormattedText = formattedText
        )
    }

    /**
     * فرمت تاریخ برای پروژه‌ها و نمایش در کارت‌ها: «۲ آبان ۱۴۰۵»
     */
    fun formatShortPersianDate(timeInMillis: Long): String {
        val pDate = getPersianDate(timeInMillis)
        return "${toPersianDigits(pDate.day)} ${pDate.monthName} ${toPersianDigits(pDate.year)}"
    }

    /**
     * ساعت زنده جاری با ارقام فارسی: «۱۴:۲۵»
     */
    fun getCurrentLiveTime(timeInMillis: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val hourStr = if (hour < 10) "0$hour" else "$hour"
        val minStr = if (minute < 10) "0$minute" else "$minute"
        return toPersianDigits("$hourStr:$minStr")
    }

    /**
     * پیام تبریک و احوالپرسی بر اساس ساعت شبانه‌روز
     */
    fun getTimeOfDayGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 4..11 -> "صبح‌بخیر ☀️"
            in 12..15 -> "ظهربخیر 🌤️"
            in 16..19 -> "عصربخیر 🌇"
            else -> "شب‌بخیر 🌙"
        }
    }

    /**
     * سال شمسی جاری با ارقام فارسی جهت درج در کپی‌رایت
     */
    fun getCurrentSolarYear(): String {
        val pDate = getPersianDate()
        return toPersianDigits(pDate.year)
    }
}

/**
 * متدهای کمکی جهت دسترسی سریع به ارقام فارسی
 */
fun String.toPersianDigits(): String = PersianDateUtil.toPersianDigits(this)
fun Number.toPersianDigits(): String = PersianDateUtil.toPersianDigits(this)
