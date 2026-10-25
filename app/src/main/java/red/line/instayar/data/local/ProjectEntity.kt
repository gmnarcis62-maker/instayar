package red.line.instayar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val templateId: String,
    val formatName: String,
    val templateJson: String,
    val lastModified: Long = System.currentTimeMillis()
)
