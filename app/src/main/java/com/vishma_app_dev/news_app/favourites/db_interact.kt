package com.vishma_app_dev.news_app.favourites

import android.content.Context
import android.media.Image
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.vishma_app_dev.news_app.network.Source
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favourites")
data class FavNewsEntity(
    @ColumnInfo("source")
    var source:String?,
    @ColumnInfo("urlToImage")
    var urlToImage: String?,
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "url")
    var url: String,
    @ColumnInfo(name = "title")
    var title: String?,
    @ColumnInfo(name = "author")
    var author: List<String>?,
    @ColumnInfo(name = "description")
    var description: String?
)
//val source: Source,
//val author: String?,
//val title: String?,
//val description: String?,
//val url: String?,
//val urlToImage: String?

class TypeConvertors {
    @TypeConverter
    fun listToString(list: List<String>): String {
        return list.joinToString(separator = ";")
    }

    @TypeConverter
    fun stringToList(str: String?): List<String> {
        return str?.split(";")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    }
}

@Dao // Added the @Dao annotation
interface NewsDao { // Renamed to NewsDao for clarity and consistency
    @Insert
    suspend fun insert(item: FavNewsEntity)

    @Delete
    suspend fun delete(item: FavNewsEntity)

    @Update
    suspend fun update(item: FavNewsEntity)

    @Query("SELECT * FROM `favourites`")
    suspend fun getFavs(): List<FavNewsEntity>

    @Query("SELECT * FROM favourites WHERE url = :url LIMIT 1")
    fun getByUrl(url: String): Flow<FavNewsEntity?>

    @Query("SELECT * FROM favourites")
    fun getAllFavNews(): Flow<List<FavNewsEntity>>



}

@Database(entities = [FavNewsEntity::class], version = 1, exportSchema = false)
@TypeConverters(TypeConvertors::class) // Added the @TypeConverters annotation
abstract class Newsdb : RoomDatabase() {
    abstract fun newsDao(): NewsDao // Renamed to newsDao() to match the interface name

    companion object {
        @Volatile
        private var instance: Newsdb? = null // Made instance private

        fun getInstance(context: Context): Newsdb { // Changed return type to non-nullable Newsdb
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    Newsdb::class.java,
                    "NewsDb"
                ).build().also { instance = it }
                instance!! // Use !! because it's initialized within the synchronized block
            }
        }
    }
}