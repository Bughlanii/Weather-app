package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [WeatherCached::class, LocalEvent::class], version = 1, exportSchema = false)
abstract class HubDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: HubDatabase? = null

        fun getDatabase(context: Context): HubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HubDatabase::class.java,
                    "hub_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed prepopulated cultural events upon first launch
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    val eventDao = getDatabase(context).eventDao()
                    eventDao.insertEvents(getPresetEvents())
                }
            }
        }

        fun getPresetEvents(): List<LocalEvent> {
            return listOf(
                LocalEvent(
                    name = "Mela Sakhi Sarwar (Sang Mela)",
                    location = "Shrine of Sakhi Sarwar, Near Dera Ghazi Khan",
                    description = "A grand annual spring mela honoring Sufi saint Sakhi Sarwar. Devotees perform Sufi Dhamal and traditional jhummar dances with rhythmic beating of Saraiki drums dhol. Colorful local handicrafts, traditional sweets, and sports (wrestling) are featured.",
                    dateString = "Mid-March (Desi Month Chet)",
                    season = "Spring",
                    cityAssociation = "Dera Ghazi Khan",
                    latitude = 30.0400,
                    longitude = 70.4000
                ),
                LocalEvent(
                    name = "Urs Hazrat Bahauddin Zakariya",
                    location = "Fort Kohna, Multan",
                    description = "An annual spiritual assembly of hundreds of thousands of pilgrims, remembering the legendary Sufi Saint who established the Suhrawardyya order block in South Asia. Soul-stirring Multani Sufi kafis are recited.",
                    dateString = "27 Safar (Hijri Calendar)",
                    season = "Autumn",
                    cityAssociation = "Multan",
                    latitude = 30.1989,
                    longitude = 71.4786
                ),
                LocalEvent(
                    name = "Mela Chiraghan (Festival of Lights)",
                    location = "Shalimar Gardens / Tomb of Madho Lal Hussain, Lahore",
                    description = "A historically significant Punjabi three-day Sufi festival marking the Urs of 16th-century saint Shah Hussain. Celebrated by lighting giant clay lamps, dancing under ecstatic dhol beats, and reciting Punjabi Sufi poetry.",
                    dateString = "Late March",
                    season = "Spring",
                    cityAssociation = "Lahore",
                    latitude = 31.5833,
                    longitude = 74.3822
                ),
                LocalEvent(
                    name = "Lok Mela Cultural Festival",
                    location = "Lok Virsa Heritage Center, Islamabad - Rawalpindi",
                    description = "A massive showcase of national heritage bringing artisans, folk musicians, and traditional culinary masterclasses from Punjab, Saraiki Waseeb, Sindh, Balochistan, and KPK together in beautiful folk pavilions.",
                    dateString = "Mid October",
                    season = "Autumn",
                    cityAssociation = "Rawalpindi",
                    latitude = 33.6938,
                    longitude = 73.0652
                ),
                LocalEvent(
                    name = "Multan Mango festival",
                    location = "Sufi Bagh, Multan",
                    description = "A colorful summer exhibition featuring dozens of world-class sweet mango varieties (Anwar Ratol, Chaunsa, Sindhri) cultivated in the rich soils of Multan. Includes family fun activities and Punjabi folks.",
                    dateString = "July (Desi Month Sawan)",
                    season = "Summer",
                    cityAssociation = "Multan",
                    latitude = 30.1575,
                    longitude = 71.5249
                ),
                LocalEvent(
                    name = "Urs Lal Shahbaz Qalandar",
                    location = "Sehwan Sharif, Sindh (Karachi Hub)",
                    description = "One of the most intense and ecstatic Sufi festivals in the country, celebrating Lal Shahbaz Qalandar. Iconic daily dhamal (spiritual dance), Sindhi music, and communal feeding (langar).",
                    dateString = "18 Sha'ban (Hijri Calendar)",
                    season = "Spring",
                    cityAssociation = "Karachi",
                    latitude = 26.4253,
                    longitude = 67.9625
                ),
                LocalEvent(
                    name = "Urs Bari Imam",
                    location = "Nurpur Shahan, Rawalpindi",
                    description = "Commemoration of the beloved spiritual protector of Rawalpindi and Margalla hills, Bari Imam. Features extensive Qawwali programs and spiritual lectures with green-flag processions.",
                    dateString = "May (Desi Month Jeth)",
                    season = "Summer",
                    cityAssociation = "Rawalpindi",
                    latitude = 33.7460,
                    longitude = 73.1023
                ),
                LocalEvent(
                    name = "Urs Hazrat Khawaja Ghulam Farid",
                    location = "Mithankot, South Punjab (DG Khan / Rajanpur region)",
                    description = "Commemorating the pre-eminent Saraiki Sufi poet Khawaja Ghulam Farid. Musicians perform classical Rahim-Yar-Khan and Mithankot Kafi compositions using local instruments like Alghoza.",
                    dateString = "5-7 Rabi-us-Sani (Hijri Calendar)",
                    season = "Winter",
                    cityAssociation = "Dera Ghazi Khan",
                    latitude = 29.1833,
                    longitude = 70.3667
                ),
                LocalEvent(
                    name = "Jashn-e-Baharan Horse & Cattle Show",
                    location = "Jinnah Stadium, Lahore",
                    description = "A highly popular seasonal spring festival highlighting Punjab's agrarian heritage. Features tentative tent pegging, decorative horse dancing, local dog race championships, and lively local food courts.",
                    dateString = "Early March",
                    season = "Spring",
                    cityAssociation = "Lahore",
                    latitude = 31.5497,
                    longitude = 74.3436
                )
            )
        }
    }
}
