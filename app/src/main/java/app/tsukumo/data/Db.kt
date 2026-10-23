package app.tsukumo.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert

@Entity(tableName = "hobby")
data class HobbyRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String?,
    val name: String,
    val cat: String,
    val archived: Boolean,
    val created: Long,
    val ord: Int = 0,
)

@Entity(tableName = "thing", indices = [Index("hobbyId")])
data class ThingRow(
    @PrimaryKey val id: Long,
    val hobbyId: Long?,
    val key: String?,
    val name: String,
    val arch: String,
    val pal: String,
    val verb: String,
    val pts: Int,
    val pers: String,
    val created: Long,
    val kind: String,
    val rare: String?,
    val parentA: Long?,
    val parentB: Long?,
)

@Entity(tableName = "session", indices = [Index("hobbyId"), Index("at")])
data class SessionRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long,
    val hobbyId: Long,
    val minutes: Int,
    /** Verb names, comma-separated. */
    val verbs: String,
    val note: String,
)

@Entity(tableName = "ev", indices = [Index("at")])
data class EvRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long,
    val type: String,
    val a: Long,
    val b: Long,
    val c: Long,
    val n: Int,
    val s: String,
)

@Entity(tableName = "bond", primaryKeys = ["a", "b"])
data class BondRow(val a: Long, val b: Long, val days: Int, val lastDay: Long)

@Entity(tableName = "meta")
data class MetaRow(@PrimaryKey val k: String, val v: String)

@Dao
interface HouseDao {
    @Query("SELECT * FROM hobby ORDER BY ord, id") suspend fun hobbies(): List<HobbyRow>
    @Query("SELECT * FROM thing ORDER BY id") suspend fun things(): List<ThingRow>
    @Query("SELECT * FROM session ORDER BY at, id") suspend fun sessions(): List<SessionRow>
    @Query("SELECT * FROM ev ORDER BY at, id") suspend fun evs(): List<EvRow>
    @Query("SELECT * FROM bond") suspend fun bonds(): List<BondRow>
    @Query("SELECT v FROM meta WHERE k = :k") suspend fun meta(k: String): String?

    @Insert suspend fun insert(h: HobbyRow): Long
    @Upsert suspend fun upsert(h: HobbyRow)
    @Upsert suspend fun upsertThings(t: List<ThingRow>)
    @Insert suspend fun insert(s: SessionRow): Long
    @Insert suspend fun insertEvs(e: List<EvRow>)
    @Upsert suspend fun upsertBonds(b: List<BondRow>)
    @Upsert suspend fun meta(m: MetaRow)

    @Query("DELETE FROM thing WHERE id = :id") suspend fun deleteThing(id: Long)
    @Query("DELETE FROM bond WHERE a = :id OR b = :id") suspend fun deleteBonds(id: Long)
    @Query("UPDATE thing SET name = :name WHERE id = :id") suspend fun rename(id: Long, name: String)
    @Query("UPDATE hobby SET name = :name WHERE id = :id") suspend fun renameHobby(id: Long, name: String)
    @Query("UPDATE hobby SET archived = :archived WHERE id = :id") suspend fun archive(id: Long, archived: Boolean)

    @Transaction
    suspend fun session(s: SessionRow, things: List<ThingRow>, evs: List<EvRow>, bonds: List<BondRow>) {
        insert(s); if (things.isNotEmpty()) upsertThings(things); if (evs.isNotEmpty()) insertEvs(evs); if (bonds.isNotEmpty()) upsertBonds(bonds)
    }
}

@Database(entities = [HobbyRow::class, ThingRow::class, SessionRow::class, EvRow::class, BondRow::class, MetaRow::class], version = 1, exportSchema = true)
abstract class HouseDb : RoomDatabase() {
    abstract fun dao(): HouseDao

    companion object {
        fun create(c: Context): HouseDb = Room.databaseBuilder(c, HouseDb::class.java, "house.db").build()
        fun memory(c: Context): HouseDb = Room.inMemoryDatabaseBuilder(c, HouseDb::class.java).build()
    }
}
