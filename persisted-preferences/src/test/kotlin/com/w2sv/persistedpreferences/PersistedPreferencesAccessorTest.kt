package com.w2sv.persistedpreferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PersistedPreferencesAccessorTest {

    private lateinit var tempDir: Path

    private lateinit var scope: CoroutineScope
    private lateinit var accessor: PersistedPreferencesAccessor

    @Before
    fun setUp() {
        tempDir = createTempDirectory()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        accessor = PersistedPreferencesAccessor(
            PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { tempDir.resolve("test.preferences_pb").toFile() }
            )
        )
    }

    @After
    fun tearDown() {
        scope.cancel()
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `persistedPreference saves and reads primitive value`() =
        runTest {
            val preference = accessor.persistedPreference(
                key = booleanPreferencesKey("primitive"),
                default = { false }
            )

            assertEquals(false, preference.flow.first())

            preference.save(true)

            assertEquals(true, preference.flow.first())
        }

    @Test
    fun `stateIn uses preference default as initial value`() =
        runTest {
            val preference = accessor.persistedPreference(
                key = booleanPreferencesKey("state_flow"),
                default = { true }
            )

            val state = preference.stateIn(backgroundScope)

            assertEquals(true, state.value)

            preference.save(false)

            assertEquals(false, state.first { !it })
        }

    @Test
    fun `nullablePreference saves and reads nullable value`() =
        runTest {
            val preference = accessor.nullablePreference(
                key = stringPreferencesKey("nullable"),
                default = { "default" },
                nullSavable = "<NULL>"
            )

            assertEquals("default", preference.flow.first())

            preference.save("saved")

            assertEquals("saved", preference.flow.first())

            preference.save(null)

            assertEquals(null, preference.flow.first())
        }

    @Test
    fun `nullablePreference preserves default matching null representation until null is saved`() =
        runTest {
            val preference = accessor.nullablePreference(
                key = stringPreferencesKey("nullable_default"),
                default = { "<NULL>" },
                nullSavable = "<NULL>"
            )

            assertEquals("<NULL>", preference.flow.first())

            preference.save(null)

            assertEquals(null, preference.flow.first())
        }

    @Test
    fun `enumPreference with ordinal save policy`() =
        runTest {
            val preference = accessor.enumPreference(
                keyName = "ordinal_enum",
                default = { TestEnum.First },
                savePolicy = EnumSavePolicy.byOrdinal()
            )

            assertEquals(TestEnum.First, preference.flow.first())

            preference.save(TestEnum.Third)

            assertEquals(TestEnum.Third, preference.flow.first())
        }

    @Test
    fun `enumPreference with name save policy`() =
        runTest {
            val preference = accessor.enumPreference(
                keyName = "name_enum",
                default = { TestEnum.First },
                savePolicy = EnumSavePolicy.byName()
            )

            assertEquals(TestEnum.First, preference.flow.first())

            preference.save(TestEnum.Second)

            assertEquals(TestEnum.Second, preference.flow.first())
        }

    @Test
    fun `enumPreference with custom string save policy`() =
        runTest {
            val preference = accessor.enumPreference(
                keyName = "custom_string_enum",
                default = { TestEnum.First },
                savePolicy = EnumSavePolicy.byString(
                    toSavable = { it.customValue },
                    toExternal = { customValue, _ ->
                        TestEnum.entries.first { it.customValue == customValue }
                    }
                )
            )

            assertEquals(TestEnum.First, preference.flow.first())

            preference.save(TestEnum.Third)

            assertEquals(TestEnum.Third, preference.flow.first())
        }

    @Test
    fun `nullableEnumPreference with ordinal save policy`() =
        runTest {
            assertEquals(-1, EnumSavePolicy.byOrdinal<TestEnum>().nullSavable)

            val preference = accessor.nullableEnumPreference(
                keyName = "nullable_ordinal_enum",
                default = { TestEnum.First },
                savePolicy = EnumSavePolicy.byOrdinal()
            )

            assertEquals(TestEnum.First, preference.flow.first())

            preference.save(TestEnum.Third)

            assertEquals(TestEnum.Third, preference.flow.first())

            preference.save(null)

            assertEquals(null, preference.flow.first())
        }

    @Test
    fun `nullableEnumPreference with name save policy`() =
        runTest {
            val preference = accessor.nullableEnumPreference(
                keyName = "nullable_name_enum",
                default = { null },
                savePolicy = EnumSavePolicy.byName<TestEnum>()
            )

            assertEquals(null, preference.flow.first())

            preference.save(TestEnum.Second)

            assertEquals(TestEnum.Second, preference.flow.first())

            preference.save(null)

            assertEquals(null, preference.flow.first())
        }

    @Test
    fun `uriPreference saves and reads null value`() =
        runTest {
            val preference = accessor.uriPreference(
                keyName = "uri",
                default = { null }
            )

            assertEquals(null, preference.flow.first())

            preference.save(null)

            assertEquals(null, preference.flow.first())
        }

    @Test
    fun `localDateTimePreference saves and reads value`() =
        runTest {
            val saved = LocalDateTime.of(2026, 5, 28, 10, 30)
            val preference = accessor.localDateTimePreference(
                keyName = "local_date_time",
                default = { null }
            )

            assertEquals(null, preference.flow.first())

            preference.save(saved)

            assertEquals(saved, preference.flow.first())
        }

    @Test
    fun `listPreference with explicit serialization saves and reads list`() =
        runTest {
            val preference = accessor.listPreference(
                keyName = "explicit_list",
                default = { emptyList() },
                serialize = { it.joinToString(separator = "|") },
                deserialize = { if (it.isEmpty()) emptyList() else it.split("|") }
            )

            assertEquals(emptyList<String>(), preference.flow.first())

            preference.save(listOf("one", "two"))

            assertEquals(listOf("one", "two"), preference.flow.first())
        }

    @Test
    fun `listPreference with element serialization saves and reads list`() =
        runTest {
            val preference = accessor.listPreference(
                keyName = "element_list",
                default = { emptyList() },
                separator = "|",
                serializeElement = Int::toString,
                deserializeElement = String::toInt
            )

            assertEquals(emptyList<Int>(), preference.flow.first())

            preference.save(listOf(1, 2, 3))

            assertEquals(listOf(1, 2, 3), preference.flow.first())
        }

    @Test
    fun `persistedPreference with mapping saves and reads external value`() =
        runTest {
            val preference = accessor.persistedPreference(
                key = intPreferencesKey("mapped"),
                default = { "none" },
                toSavable = String::length,
                toExternal = { "length=$it" }
            )

            assertEquals("length=4", preference.flow.first())

            preference.save("abcdef")

            assertEquals("length=6", preference.flow.first())
        }
}

private enum class TestEnum {
    First,
    Second,
    Third;

    val customValue: String
        get() = name.lowercase()
}
