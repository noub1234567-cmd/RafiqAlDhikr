package com.rafiqaldhikr.app.core.jumua

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate

/** One Friday sunna: [text] is the explanation and the hadith, shown when the person opens it. */
data class JumuaItem(val key: String, val label: String, val text: String)

/** The Friday checklist: tick what you did today. Stored on the device, one set per profile and Friday. */
object JumuaChecklist {
    val items: List<JumuaItem> = listOf(
        JumuaItem("ghusl", "الاغتسال يوم الجمعة",
            "وهو من آكد سنن الجمعة.\nقال رسول الله ﷺ:\n«غُسْلُ يَوْمِ الْجُمُعَةِ وَاجِبٌ عَلَى كُلِّ مُحْتَلِمٍ».\nرواه البخاري ومسلم."),
        JumuaItem("tayyib", "التطيب ولبس أحسن الثياب",
            "يُستحب للمسلم أن يتطيب ويلبس ثياباً نظيفة حسنة عند الذهاب إلى صلاة الجمعة.\nقال رسول الله ﷺ:\n«مَنِ اغْتَسَلَ يَوْمَ الْجُمُعَةِ، وَتَطَهَّرَ بِمَا اسْتَطَاعَ مِنْ طُهْرٍ، ثُمَّ ادَّهَنَ أَوْ مَسَّ مِنْ طِيبٍ، ثُمَّ رَاحَ فَلَمْ يُفَرِّقْ بَيْنَ اثْنَيْنِ، فَصَلَّى مَا كُتِبَ لَهُ، ثُمَّ أَنْصَتَ إِذَا تَكَلَّمَ الْإِمَامُ، غُفِرَ لَهُ مَا بَيْنَهُ وَبَيْنَ الْجُمُعَةِ الْأُخْرَى».\nرواه البخاري."),
        JumuaItem("bakkir", "التبكير إلى صلاة الجمعة",
            "من أعظم سنن الجمعة التبكير إلى المسجد.\nقال رسول الله ﷺ:\n«إِذَا كَانَ يَوْمُ الْجُمُعَةِ، كَانَ عَلَى كُلِّ بَابٍ مِنْ أَبْوَابِ الْمَسْجِدِ الْمَلَائِكَةُ، يَكْتُبُونَ الْأَوَّلَ فَالْأَوَّلَ، فَإِذَا جَلَسَ الْإِمَامُ طَوَوْا الصُّحُفَ وَجَاءُوا يَسْتَمِعُونَ الذِّكْرَ».\nرواه البخاري ومسلم."),
        JumuaItem("mashy", "الذهاب إلى المسجد ماشياً إن تيسر",
            "قال رسول الله ﷺ:\n«مَن غَسَّلَ يَوْمَ الْجُمُعَةِ وَاغْتَسَلَ، وَبَكَّرَ وَابْتَكَرَ، وَمَشَى وَلَمْ يَرْكَبْ، وَدَنَا مِنَ الْإِمَامِ فَاسْتَمَعَ وَلَمْ يَلْغُ، كَانَ لَهُ بِكُلِّ خُطْوَةٍ عَمَلُ سَنَةٍ، أَجْرُ صِيَامِهَا وَقِيَامِهَا».\nرواه أبو داود والترمذي والنسائي وابن ماجه، وصححه أهل العلم."),
        JumuaItem("kahf", "قراءة سورة الكهف",
            "تُستحب قراءة سورة الكهف يوم الجمعة.\nقال رسول الله ﷺ:\n«مَنْ قَرَأَ سُورَةَ الْكَهْفِ فِي يَوْمِ الْجُمُعَةِ أَضَاءَ لَهُ مِنَ النُّورِ مَا بَيْنَ الْجُمُعَتَيْنِ».\nرواه الحاكم والبيهقي، وصححه عدد من أهل العلم."),
        JumuaItem("salawat", "الإكثار من الصلاة على النبي ﷺ",
            "من السنن العظيمة في يوم الجمعة الإكثار من الصلاة والسلام على رسول الله ﷺ.\nقال رسول الله ﷺ:\n«إِنَّ مِنْ أَفْضَلِ أَيَّامِكُمْ يَوْمَ الْجُمُعَةِ، فَأَكْثِرُوا عَلَيَّ مِنَ الصَّلَاةِ فِيهِ، فَإِنَّ صَلَاتَكُمْ مَعْرُوضَةٌ عَلَيَّ».\nرواه أبو داود والنسائي وابن ماجه، وصححه أهل العلم."),
        JumuaItem("dua", "تحرّي ساعة الإجابة والدعاء فيها",
            "في يوم الجمعة ساعة لا يوافقها عبد مسلم يسأل الله شيئاً إلا أعطاه إياه.\nقال رسول الله ﷺ:\n«إِنَّ فِي الْجُمُعَةِ لَسَاعَةً، لَا يُوَافِقُهَا عَبْدٌ مُسْلِمٌ، وَهُوَ قَائِمٌ يُصَلِّي، يَسْأَلُ اللَّهَ شَيْئًا، إِلَّا أَعْطَاهُ إِيَّاهُ».\nرواه البخاري ومسلم.\nوقد ورد أن هذه الساعة في آخر ساعة بعد العصر، وورد أنها من حين جلوس الإمام إلى انتهاء الصلاة، وهما من أشهر الأقوال عند العلماء."),
        JumuaItem("sunna", "صلاة السنة بعد الجمعة",
            "ثبت عن النبي ﷺ أنه كان يصلي بعد الجمعة ركعتين في بيته.\nقال عبد الله بن عمر رضي الله عنهما:\n«كَانَ رَسُولُ اللَّهِ ﷺ لَا يُصَلِّي بَعْدَ الْجُمُعَةِ حَتَّى يَنْصَرِفَ، فَيُصَلِّي رَكْعَتَيْنِ».\nرواه البخاري ومسلم.\nوثبت أيضاً الأمر بأربع ركعات بعد الجمعة، قال ﷺ:\n«إِذَا صَلَّى أَحَدُكُمُ الْجُمُعَةَ فَلْيُصَلِّ بَعْدَهَا أَرْبَعًا».\nرواه مسلم.")
    )

    fun isFriday(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.FRIDAY

    /** The Friday the checklist refers to: today when it is Friday, otherwise the latest one. */
    fun fridayFor(date: LocalDate): LocalDate {
        var d = date
        while (!isFriday(d)) d = d.minusDays(1)
        return d
    }

    private const val FILE = "jumua_checklist"
    private fun key(profileId: Long, friday: LocalDate) = "p${profileId}_${friday.toEpochDay()}"

    fun checked(context: Context, profileId: Long, friday: LocalDate): Set<String> =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getStringSet(key(profileId, friday), emptySet()).orEmpty().toSet()
            .filter { k -> items.any { it.key == k } }.toSet()

    fun toggle(context: Context, profileId: Long, friday: LocalDate, itemKey: String): Set<String> {
        val now = checked(context, profileId, friday).let { if (itemKey in it) it - itemKey else it + itemKey }
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putStringSet(key(profileId, friday), now).apply()
        return now
    }
}
