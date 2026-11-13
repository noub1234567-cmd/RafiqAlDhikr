package com.rafiqaldhikr.app

import com.rafiqaldhikr.app.core.model.ThemeMode
import com.rafiqaldhikr.app.data.backup.BackupCodec
import com.rafiqaldhikr.app.data.backup.BackupPayload
import com.rafiqaldhikr.app.data.backup.BackupValidationResult
import com.rafiqaldhikr.app.data.local.datastore.AppSettingsSnapshot
import com.rafiqaldhikr.app.data.local.db.entities.DuaEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupSerializerTest {

    @Test
    fun `valid multi-profile backup encodes and validates cleanly`() {
        val payload = BackupPayload(
            profiles = listOf(
                UserProfileEntity(id = 1, name = "أحمد", isDefault = true),
                UserProfileEntity(id = 2, name = "فاطمة", isDefault = false)
            ),
            duaEntries = listOf(
                DuaEntryEntity(id = 10, profileId = 1, title = "دعاء", body = "اللهم بارك لنا"),
                DuaEntryEntity(id = 11, profileId = 2, title = "دعاء خاص", body = "رب اشرح لي صدري")
            )
        )
        val settings = AppSettingsSnapshot(
            activeProfileId = 2,
            themeMode = ThemeMode.DARK,
            hijriOffsetDays = 1,
            tasbeehHapticEnabled = false
        )

        val encoded = BackupCodec.encodeBackup(payload, settings, exportedAtMillis = 123456789L)
        val result = BackupCodec.decodeAndValidate(encoded)

        assertTrue(result is BackupValidationResult.Valid)
        val valid = result as BackupValidationResult.Valid
        assertEquals(2, valid.backup.payload.profiles.size)
        assertEquals(2, valid.backup.payload.duaEntries.size)
        assertEquals(ThemeMode.DARK, valid.backup.settings.themeMode)
        assertEquals(1, valid.backup.settings.hijriOffsetDays)
    }

    @Test
    fun `tampered checksum or corrupted json is safely rejected`() {
        val payload = BackupPayload(
            profiles = listOf(UserProfileEntity(id = 1, name = "أحمد", isDefault = true))
        )
        val encoded = BackupCodec.encodeBackup(payload, AppSettingsSnapshot())
        val tampered = encoded.replace("أحمد", "خالد")

        val tamperedResult = BackupCodec.decodeAndValidate(tampered)
        assertTrue(tamperedResult is BackupValidationResult.Invalid)

        val malformedResult = BackupCodec.decodeAndValidate("{not valid json")
        assertTrue(malformedResult is BackupValidationResult.Invalid)

        val emptyResult = BackupCodec.decodeAndValidate("   ")
        assertTrue(emptyResult is BackupValidationResult.Invalid)
    }

    @Test
    fun `orphan records without matching profile are rejected`() {
        val payload = BackupPayload(
            profiles = listOf(UserProfileEntity(id = 1, name = "أحمد", isDefault = true)),
            duaEntries = listOf(
                DuaEntryEntity(id = 5, profileId = 999, title = "يتيم", body = "لا يرتبط بملف")
            )
        )
        val encoded = BackupCodec.encodeBackup(payload, AppSettingsSnapshot())
        val result = BackupCodec.decodeAndValidate(encoded)
        assertTrue(result is BackupValidationResult.Invalid)
    }
}
