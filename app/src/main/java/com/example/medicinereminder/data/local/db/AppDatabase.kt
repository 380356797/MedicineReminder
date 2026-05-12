package com.example.medicinereminder.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Medicine::class,
        MedicineSchedule::class,
        MedicineLog::class,
        HealthIndicator::class,
        HealthRecord::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun medicineScheduleDao(): MedicineScheduleDao
    abstract fun medicineLogDao(): MedicineLogDao
    abstract fun healthIndicatorDao(): HealthIndicatorDao
    abstract fun healthRecordDao(): HealthRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicine_reminder.db"
                )
                    .addCallback(PrepopulateCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    private class PrepopulateCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                prepopulateMedicines(database.medicineDao())
                prepopulateHealthIndicators(database.healthIndicatorDao())
            }
        }
    }
}

private suspend fun prepopulateMedicines(dao: MedicineDao) {
    val medicines = listOf(
        // 降压药
        Medicine(name = "氨氯地平", category = "降压药", isPreset = true),
        Medicine(name = "硝苯地平", category = "降压药", isPreset = true),
        Medicine(name = "缬沙坦", category = "降压药", isPreset = true),
        Medicine(name = "厄贝沙坦", category = "降压药", isPreset = true),
        Medicine(name = "氯沙坦", category = "降压药", isPreset = true),
        Medicine(name = "替米沙坦", category = "降压药", isPreset = true),
        Medicine(name = "美托洛尔", category = "降压药", isPreset = true),
        Medicine(name = "比索洛尔", category = "降压药", isPreset = true),
        Medicine(name = "卡托普利", category = "降压药", isPreset = true),
        Medicine(name = "依那普利", category = "降压药", isPreset = true),
        Medicine(name = "培哚普利", category = "降压药", isPreset = true),
        Medicine(name = "氢氯噻嗪", category = "降压药", isPreset = true),
        Medicine(name = "吲达帕胺", category = "降压药", isPreset = true),
        Medicine(name = "非洛地平", category = "降压药", isPreset = true),
        // 降糖药
        Medicine(name = "二甲双胍", category = "降糖药", isPreset = true),
        Medicine(name = "格列美脲", category = "降糖药", isPreset = true),
        Medicine(name = "格列齐特", category = "降糖药", isPreset = true),
        Medicine(name = "阿卡波糖", category = "降糖药", isPreset = true),
        Medicine(name = "达格列净", category = "降糖药", isPreset = true),
        Medicine(name = "恩格列净", category = "降糖药", isPreset = true),
        Medicine(name = "利格列汀", category = "降糖药", isPreset = true),
        Medicine(name = "西格列汀", category = "降糖药", isPreset = true),
        Medicine(name = "格列吡嗪", category = "降糖药", isPreset = true),
        Medicine(name = "瑞格列奈", category = "降糖药", isPreset = true),
        Medicine(name = "吡格列酮", category = "降糖药", isPreset = true),
        Medicine(name = "胰岛素", category = "降糖药", isPreset = true),
        // 降脂药
        Medicine(name = "阿托伐他汀", category = "降脂药", isPreset = true),
        Medicine(name = "瑞舒伐他汀", category = "降脂药", isPreset = true),
        Medicine(name = "辛伐他汀", category = "降脂药", isPreset = true),
        Medicine(name = "普伐他汀", category = "降脂药", isPreset = true),
        Medicine(name = "氟伐他汀", category = "降脂药", isPreset = true),
        Medicine(name = "匹伐他汀", category = "降脂药", isPreset = true),
        Medicine(name = "非诺贝特", category = "降脂药", isPreset = true),
        Medicine(name = "依折麦布", category = "降脂药", isPreset = true),
        // 抗生素
        Medicine(name = "阿莫西林", category = "抗生素", isPreset = true),
        Medicine(name = "头孢克洛", category = "抗生素", isPreset = true),
        Medicine(name = "头孢呋辛", category = "抗生素", isPreset = true),
        Medicine(name = "阿奇霉素", category = "抗生素", isPreset = true),
        Medicine(name = "左氧氟沙星", category = "抗生素", isPreset = true),
        Medicine(name = "莫西沙星", category = "抗生素", isPreset = true),
        Medicine(name = "克拉霉素", category = "抗生素", isPreset = true),
        Medicine(name = "甲硝唑", category = "抗生素", isPreset = true),
        Medicine(name = "罗红霉素", category = "抗生素", isPreset = true),
        // 止痛药
        Medicine(name = "布洛芬", category = "止痛药", isPreset = true),
        Medicine(name = "对乙酰氨基酚", category = "止痛药", isPreset = true),
        Medicine(name = "双氯芬酸", category = "止痛药", isPreset = true),
        Medicine(name = "塞来昔布", category = "止痛药", isPreset = true),
        Medicine(name = "洛索洛芬", category = "止痛药", isPreset = true),
        // 胃药
        Medicine(name = "奥美拉唑", category = "胃药", isPreset = true),
        Medicine(name = "雷贝拉唑", category = "胃药", isPreset = true),
        Medicine(name = "兰索拉唑", category = "胃药", isPreset = true),
        Medicine(name = "铝碳酸镁", category = "胃药", isPreset = true),
        Medicine(name = "多潘立酮", category = "胃药", isPreset = true),
        Medicine(name = "莫沙必利", category = "胃药", isPreset = true),
        Medicine(name = "胶体果胶铋", category = "胃药", isPreset = true),
        Medicine(name = "枸橼酸铋钾", category = "胃药", isPreset = true),
        // 抗过敏
        Medicine(name = "氯雷他定", category = "抗过敏", isPreset = true),
        Medicine(name = "西替利嗪", category = "抗过敏", isPreset = true),
        Medicine(name = "依巴斯汀", category = "抗过敏", isPreset = true),
        Medicine(name = "氯苯那敏", category = "抗过敏", isPreset = true),
        // 维生素/保健品
        Medicine(name = "维生素C", category = "维生素/保健品", isPreset = true),
        Medicine(name = "维生素D", category = "维生素/保健品", isPreset = true),
        Medicine(name = "维生素B族", category = "维生素/保健品", isPreset = true),
        Medicine(name = "钙片", category = "维生素/保健品", isPreset = true),
        Medicine(name = "鱼油", category = "维生素/保健品", isPreset = true),
        Medicine(name = "叶酸", category = "维生素/保健品", isPreset = true),
        Medicine(name = "铁剂", category = "维生素/保健品", isPreset = true),
        Medicine(name = "锌片", category = "维生素/保健品", isPreset = true),
        Medicine(name = "辅酶Q10", category = "维生素/保健品", isPreset = true),
        Medicine(name = "氨糖", category = "维生素/保健品", isPreset = true),
        // 呼吸系统
        Medicine(name = "氨溴索", category = "呼吸系统", isPreset = true),
        Medicine(name = "右美沙芬", category = "呼吸系统", isPreset = true),
        Medicine(name = "沙丁胺醇", category = "呼吸系统", isPreset = true),
        Medicine(name = "孟鲁司特", category = "呼吸系统", isPreset = true),
        Medicine(name = "布地奈德", category = "呼吸系统", isPreset = true),
        // 心血管
        Medicine(name = "阿司匹林", category = "心血管", isPreset = true),
        Medicine(name = "氯吡格雷", category = "心血管", isPreset = true),
        Medicine(name = "华法林", category = "心血管", isPreset = true),
        Medicine(name = "利伐沙班", category = "心血管", isPreset = true),
        Medicine(name = "硝酸甘油", category = "心血管", isPreset = true),
        Medicine(name = "单硝酸异山梨酯", category = "心血管", isPreset = true),
        // 神经系统
        Medicine(name = "谷维素", category = "神经系统", isPreset = true),
        Medicine(name = "甲钴胺", category = "神经系统", isPreset = true),
        Medicine(name = "氟桂利嗪", category = "神经系统", isPreset = true),
        Medicine(name = "佐匹克隆", category = "神经系统", isPreset = true),
        Medicine(name = "褪黑素", category = "神经系统", isPreset = true),
        // 泌尿系统
        Medicine(name = "非那雄胺", category = "泌尿系统", isPreset = true),
        Medicine(name = "坦索罗辛", category = "泌尿系统", isPreset = true),
        Medicine(name = "别嘌醇", category = "泌尿系统", isPreset = true),
        Medicine(name = "苯溴马隆", category = "泌尿系统", isPreset = true),
        // 内分泌
        Medicine(name = "左甲状腺素", category = "内分泌", isPreset = true),
        Medicine(name = "甲巯咪唑", category = "内分泌", isPreset = true),
        // 骨关节
        Medicine(name = "碳酸钙D3", category = "骨关节", isPreset = true),
        Medicine(name = "双醋瑞因", category = "骨关节", isPreset = true),
        Medicine(name = "氨基葡萄糖", category = "骨关节", isPreset = true),
        // 外用
        Medicine(name = "红霉素软膏", category = "外用", isPreset = true),
        Medicine(name = "莫匹罗星软膏", category = "外用", isPreset = true),
        Medicine(name = "酮康唑乳膏", category = "外用", isPreset = true),
        Medicine(name = "复方醋酸地塞米松乳膏", category = "外用", isPreset = true),
    )
    medicines.forEach { dao.insert(it) }
}

private suspend fun prepopulateHealthIndicators(dao: HealthIndicatorDao) {
    val indicators = listOf(
        // 基础生命体征
        HealthIndicator(name = "血压（收缩压）", unit = "mmHg", category = "基础生命体征", normalRange = "90-140", isPreset = true, sortOrder = 1),
        HealthIndicator(name = "血压（舒张压）", unit = "mmHg", category = "基础生命体征", normalRange = "60-90", isPreset = true, sortOrder = 2),
        HealthIndicator(name = "心率", unit = "次/分", category = "基础生命体征", normalRange = "60-100", isPreset = true, sortOrder = 3),
        HealthIndicator(name = "体温", unit = "°C", category = "基础生命体征", normalRange = "36.1-37.2", isPreset = true, sortOrder = 4),
        HealthIndicator(name = "血氧饱和度", unit = "%", category = "基础生命体征", normalRange = "95-100", isPreset = true, sortOrder = 5),
        HealthIndicator(name = "呼吸频率", unit = "次/分", category = "基础生命体征", normalRange = "12-20", isPreset = true, sortOrder = 6),
        // 血糖相关
        HealthIndicator(name = "空腹血糖", unit = "mmol/L", category = "血糖", normalRange = "3.9-6.1", isPreset = true, sortOrder = 10),
        HealthIndicator(name = "餐后2小时血糖", unit = "mmol/L", category = "血糖", normalRange = "<7.8", isPreset = true, sortOrder = 11),
        HealthIndicator(name = "随机血糖", unit = "mmol/L", category = "血糖", normalRange = "<11.1", isPreset = true, sortOrder = 12),
        HealthIndicator(name = "糖化血红蛋白", unit = "%", category = "血糖", normalRange = "4.0-6.0", isPreset = true, sortOrder = 13),
        // 血脂
        HealthIndicator(name = "总胆固醇", unit = "mmol/L", category = "血脂", normalRange = "<5.2", isPreset = true, sortOrder = 20),
        HealthIndicator(name = "甘油三酯", unit = "mmol/L", category = "血脂", normalRange = "<1.7", isPreset = true, sortOrder = 21),
        HealthIndicator(name = "高密度脂蛋白", unit = "mmol/L", category = "血脂", normalRange = ">1.0", isPreset = true, sortOrder = 22),
        HealthIndicator(name = "低密度脂蛋白", unit = "mmol/L", category = "血脂", normalRange = "<3.4", isPreset = true, sortOrder = 23),
        // 肝功能
        HealthIndicator(name = "谷丙转氨酶(ALT)", unit = "U/L", category = "肝功能", normalRange = "0-40", isPreset = true, sortOrder = 30),
        HealthIndicator(name = "谷草转氨酶(AST)", unit = "U/L", category = "肝功能", normalRange = "0-40", isPreset = true, sortOrder = 31),
        HealthIndicator(name = "总胆红素", unit = "μmol/L", category = "肝功能", normalRange = "3.4-17.1", isPreset = true, sortOrder = 32),
        HealthIndicator(name = "直接胆红素", unit = "μmol/L", category = "肝功能", normalRange = "0-6.8", isPreset = true, sortOrder = 33),
        HealthIndicator(name = "白蛋白", unit = "g/L", category = "肝功能", normalRange = "40-55", isPreset = true, sortOrder = 34),
        // 肾功能
        HealthIndicator(name = "肌酐", unit = "μmol/L", category = "肾功能", normalRange = "44-133", isPreset = true, sortOrder = 40),
        HealthIndicator(name = "尿素氮", unit = "mmol/L", category = "肾功能", normalRange = "2.9-8.2", isPreset = true, sortOrder = 41),
        HealthIndicator(name = "尿酸", unit = "μmol/L", category = "肾功能", normalRange = "男208-428/女155-357", isPreset = true, sortOrder = 42),
        HealthIndicator(name = "估算肾小球滤过率(eGFR)", unit = "mL/min/1.73m²", category = "肾功能", normalRange = ">90", isPreset = true, sortOrder = 43),
        // 血常规
        HealthIndicator(name = "白细胞计数", unit = "×10⁹/L", category = "血常规", normalRange = "3.5-9.5", isPreset = true, sortOrder = 50),
        HealthIndicator(name = "红细胞计数", unit = "×10¹²/L", category = "血常规", normalRange = "男4.3-5.8/女3.8-5.1", isPreset = true, sortOrder = 51),
        HealthIndicator(name = "血红蛋白", unit = "g/L", category = "血常规", normalRange = "男130-175/女115-150", isPreset = true, sortOrder = 52),
        HealthIndicator(name = "血小板计数", unit = "×10⁹/L", category = "血常规", normalRange = "125-350", isPreset = true, sortOrder = 53),
        // 甲状腺
        HealthIndicator(name = "促甲状腺激素(TSH)", unit = "mIU/L", category = "甲状腺", normalRange = "0.27-4.2", isPreset = true, sortOrder = 60),
        HealthIndicator(name = "游离T3(FT3)", unit = "pmol/L", category = "甲状腺", normalRange = "3.1-6.8", isPreset = true, sortOrder = 61),
        HealthIndicator(name = "游离T4(FT4)", unit = "pmol/L", category = "甲状腺", normalRange = "12-22", isPreset = true, sortOrder = 62),
        // 身体指标
        HealthIndicator(name = "体重", unit = "kg", category = "身体指标", normalRange = "", isPreset = true, sortOrder = 70),
        HealthIndicator(name = "身高", unit = "cm", category = "身体指标", normalRange = "", isPreset = true, sortOrder = 71),
        HealthIndicator(name = "BMI", unit = "kg/m²", category = "身体指标", normalRange = "18.5-24.0", isPreset = true, sortOrder = 72),
        HealthIndicator(name = "腰围", unit = "cm", category = "身体指标", normalRange = "男<90/女<85", isPreset = true, sortOrder = 73),
        HealthIndicator(name = "臀围", unit = "cm", category = "身体指标", normalRange = "", isPreset = true, sortOrder = 74),
        // 生活习惯
        HealthIndicator(name = "睡眠时长", unit = "小时", category = "生活习惯", normalRange = "7-9", isPreset = true, sortOrder = 80),
        HealthIndicator(name = "步数", unit = "步", category = "生活习惯", normalRange = ">6000", isPreset = true, sortOrder = 81),
        HealthIndicator(name = "饮水量", unit = "mL", category = "生活习惯", normalRange = "1500-2000", isPreset = true, sortOrder = 82),
        // 其他
        HealthIndicator(name = "尿常规pH", unit = "", category = "其他", normalRange = "4.5-8.0", isPreset = true, sortOrder = 90),
        HealthIndicator(name = "尿蛋白", unit = "", category = "其他", normalRange = "阴性", isPreset = true, sortOrder = 91),
        HealthIndicator(name = "尿糖", unit = "", category = "其他", normalRange = "阴性", isPreset = true, sortOrder = 92),
        HealthIndicator(name = "幽门螺杆菌", unit = "", category = "其他", normalRange = "阴性", isPreset = true, sortOrder = 93),
    )
    indicators.forEach { dao.insert(it) }
}
