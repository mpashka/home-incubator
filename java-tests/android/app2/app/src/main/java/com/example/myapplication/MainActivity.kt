package com.example.myapplication

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.RawContacts
import android.util.Log
import android.view.Menu
import android.view.View
import android.Manifest
import android.content.ContentProviderOperation
import android.provider.ContactsContract.RawContactsEntity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

import java.util.Date
import java.util.UUID
import java.util.function.Predicate
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

/*
        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
*/
        binding.appBarMain.fab.setOnClickListener { view -> onBarClick(view) }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    fun onBarClick(view: View) {
        var result: String
        Log.d(LOG_TAG, "Begin operation")
        try {
            checkAndRequestPermissions(this)


//        showProviders()

//        showContent(ContactsContract.Settings.CONTENT_URI)

            /*
    private fun showContent(uri: Uri, filter: Predicate<Cursor>) {
        processRows("contacts", uri, filter) { showFields(it) }
    }

        showContent(ContactsContract.Profile.CONTENT_URI)
        showContent(ContactsContract.Contacts.CONTENT_URI)
        showContent(ContactsContract.RawContactsEntity.CONTENT_URI)
        showContent(ContactsContract.RawContacts.CONTENT_URI)
        showContent(ContactsContract.Data.CONTENT_URI)
        showContent(ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        // Email, StructuredPostal, Callable, Contactables
        showContent(ContactsContract.Groups.CONTENT_URI)
        showContent(ContactsContract.AggregationExceptions.CONTENT_URI)
        showContent(ContactsContract.ProviderStatus.CONTENT_URI)
//        showContent(ContactsContract.DisplayPhoto.CONTENT_URI)
*/

//            processRows("settings", ContactsContract.Settings.CONTENT_URI, all()) { showFields(it) }
//            showTelegram(ContactsContract.RawContactsEntity.CONTENT_URI, test(RawContacts.ACCOUNT_NAME, TG_WORK))
//            showContent(ContactsContract.Settings.CONTENT_URI)

//            doCreateContact()

//            processRows("google", ContactsContract.RawContactsEntity.CONTENT_URI, {
//                    val contactId = getIntValue(it, RawContacts.CONTACT_ID)
//                    getStringValue(it, RawContacts.ACCOUNT_NAME) == G_WORK && (contactId == 1728 || contactId == 1729)
//                }
//            ) { showFields(it) }

//            showMimeTypes("google", test(RawContacts.ACCOUNT_NAME, G_WORK))
//            showMimeTypes("telegram", test(RawContacts.ACCOUNT_NAME, TG_WORK))
//            processRows("links", ContactsContract.AggregationExceptions.CONTENT_URI, all()) { showFields(it) }

            val links = HashMap<Int, MutableSet<Int>>()
//            ,
//            rawContactId1
//            )
//            .withValue(
//                ContactsContract.AggregationExceptions.RAW_CONTACT_ID2,

            processRows("links", ContactsContract.AggregationExceptions.CONTENT_URI, all()) {
                val contact1 = getIntValue(it, ContactsContract.AggregationExceptions.RAW_CONTACT_ID1)
                val contact2 = getIntValue(it, ContactsContract.AggregationExceptions.RAW_CONTACT_ID2)
                links.computeIfAbsent(contact1) {HashSet()}.add(contact2)
                links.computeIfAbsent(contact2) {HashSet()}.add(contact1)
            }

            val google = collectRawContact(test(RawContacts.ACCOUNT_NAME, G_WORK))
            val telegram = collectRawContact(test(RawContacts.ACCOUNT_NAME, TG_WORK))
            process(google, telegram, links)

//            processRows("links", ContactsContract.AggregationExceptions.CONTENT_URI, all()) { showFields(it) }

//            debug(google, telegram)
//            debug2(telegram, google)

//            removeTgEmails()


            Log.d(LOG_TAG, "Operation success")
            result = "success"
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Operation error", e)
            result = "Error $e"
        }

        Snackbar.make(view, result, Snackbar.LENGTH_LONG)
            .setAction("Action", null)
            .setAnchorView(R.id.fab).show()
    }

    private fun removeTgEmails() {
        val rowsDeleted = contentResolver.delete(ContactsContract.Data.CONTENT_URI,
            "${RawContacts.ACCOUNT_NAME} = ? AND " +
                    "${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(
                TG_WORK,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
        )

        Log.i(LOG_TAG, "Removed $rowsDeleted emails")

/*
        processRows("tg contacts", RawContactsEntity.CONTENT_URI, test(RawContacts.ACCOUNT_NAME, TG_WORK)) { fieldRow ->
            val mimetype = getStringValue(fieldRow, ContactsContract.Data.MIMETYPE)
            if (mimetype == ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) {
            }
        }
*/

    }

    private fun debug2(
        telegram: Map<Int, Contact>,
        google: Map<Int, Contact>
    ) {
        Log.i(LOG_TAG, "tg-1782: ${telegram[1782]}")
        Log.i(LOG_TAG, "tg-1784: ${telegram[1784]}")
        Log.i(LOG_TAG, "T:C-1770: ${telegram[1770]}")
        Log.i(LOG_TAG, "T:C-1772: ${telegram[1772]}")
        Log.i(LOG_TAG, "T:C-1814: ${telegram[1814]}")
        Log.i(LOG_TAG, "G:C-1771: ${google[1771]}")
        Log.i(LOG_TAG, "G:C-1772: ${google[1772]}")
        Log.i(LOG_TAG, "G:C-1740: ${google[1740]}")
        Log.i(LOG_TAG, "T:id-118038584: ${tgContactByIds["118038584"]}")
        Log.i(LOG_TAG, "T:name-michurinandrey: ${tgContactByNames["michurinandrey"]}")
        Log.i(LOG_TAG, "Y:amich: ${tgContactByNames["amich"]}")
    }

    private fun debug(google: Map<Int, Contact>, telegram: Map<Int, Contact>) {
        Log.i(LOG_TAG, "G0: ${google[1814]}")
        Log.i(LOG_TAG, "G1: ${google[1740]}")
        Log.i(LOG_TAG, "T0: ${telegram[1814]}")
        Log.i(LOG_TAG, "T1: ${telegram[1740]}")

        /*
                    processRows("contacts", ContactsContract.Contacts.CONTENT_URI, all()) {
                        val _id = getIntValue(it, "_id")
                        val id = getIntValue(it, "name_raw_contact_id")
                        val name = getStringValue(it, "display_name")
                        Log.i(LOG_TAG, "$_id:$id $name")
                    }
        */

        val collectedContacts = google.keys union telegram.keys
        Log.i(LOG_TAG, "All contacts: ${collectedContacts.size} (${google.size + telegram.size})")
        val missedRawContacts = collectedContacts.toMutableSet()
        processRows("contacts", RawContacts.CONTENT_URI, all()) {
            val id = getIntValue(it, "_id")
            val contactId = getIntValue(it, "contact_id")
//                (setOf(id, contactId) intersect setOf(1814, 1802)).isNotEmpty()
            missedRawContacts.remove(contactId)
            if (missedRawContacts.contains(contactId)) {
                Log.i(LOG_TAG, "Missed contact:")
                showFields(it)
            }
        }
        Log.i(LOG_TAG, "Missed raw contacts: ${missedRawContacts.size}")

        processRows("contacts", ContactsContract.Contacts.CONTENT_URI, all()) {
            val _id = getIntValue(it, "_id")
            val id = getIntValue(it, "name_raw_contact_id")
            val name = getStringValue(it, "display_name")
            if (!collectedContacts.contains(id)) {
                Log.i(LOG_TAG, "Missed contact $id $name")
            }
        }

        processRows("raw-entity-contacts", RawContactsEntity.CONTENT_URI, test("contact_id", 1740)) { showFields(it) }
        processRows("raw-contacts", RawContacts.CONTENT_URI, test("contact_id", 1740)) { showFields(it) }
        processRows("data-contacts", ContactsContract.Data.CONTENT_URI, test("contact_id", 1740)) { showFields(it) }
    }

    private fun showMimeTypes(name: String, filter: Predicate<Cursor>) {
        val mimeTypes = HashMap<String, MutableSet<String>>()
        processRows(name, ContactsContract.RawContactsEntity.CONTENT_URI, filter) { fieldRow ->
            val mimetype = getStringValue(fieldRow, ContactsContract.Data.MIMETYPE)
            for (k in arrayOf(ContactsContract.Data.DATA1, ContactsContract.Data.DATA2, ContactsContract.Data.DATA3, ContactsContract.Data.DATA4, ContactsContract.Data.DATA5)) {
                val index = fieldRow.getColumnIndex(k)
                if (index >= 0) {
                    val dataVal = fieldRow.getString(index)
                    if (dataVal != null && dataVal.isNotBlank()) {
                        val vals = mimeTypes.computeIfAbsent("$mimetype:$k") { HashSet() }
                        if (vals.size < 10) {
                            vals.add(dataVal)
                        }
                    }
                }
            }
        }
        for (mimeType in mimeTypes) {
            Log.i(LOG_TAG, "${mimeType.key}: ${mimeType.value}")
        }
    }

    private fun checkAndRequestPermissions(activity: Activity) {
        val permissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            Log.i(LOG_TAG, "Permissions needed: $permissionsToRequest")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // Покажите объяснение пользователю
                println("Этому приложению нужен доступ к контактам для работы.")
            }
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), 1)
        } else {
            Log.i(LOG_TAG, "Permissions granted")
        }
    }

    private fun doCreateContact() {
        Log.i(LOG_TAG, "Create contact...")

//        val contactId = createContact(tg())
//        val contactId = testContactId
//        setContactName(contactId, "telegram", plain())

//        val tgContactId
//        setTgContactData(contactId, plain(), TgType.profile)
//        setTgContactData(contactId, plain(), TgType.voice)
//        setTgContactData(contactId, plain(), TgType.video)

        val name = "my-test-contact ${Date()} ${UUID.randomUUID().toString().substring(0, 5)}"

        doCreateContactGoogle(name)
        doCreateContactTg(name)

        Log.i(LOG_TAG, "Create contact done")
    }

    private fun doCreateContactTg(name: String) {
        Log.i(LOG_TAG, "Create telegram contact...")

        val contactId = createContact(tg())
//        setContactName(contactId, name)

//        val tgContactId
        setTgContactData(contactId, plain(), TgType.profile)
        setTgContactData(contactId, plain(), TgType.voice)
        setTgContactData(contactId, plain(), TgType.video)
//        setContactIdentity(contactId, "tg:" + tgNick, plain())
        setContactEmail(contactId, "my_email@gmail.com", ContactsContract.CommonDataKinds.Email.TYPE_WORK)

        Log.i(LOG_TAG, "Create telegram contact done")
    }

    private fun doCreateContactGoogle(name: String) {
        Log.i(LOG_TAG, "Create google contact...")

        val contactId = createContact(google())
//        setContactName(contactId, name)
        setContactEmail(contactId, "my_email@gmail.com", ContactsContract.CommonDataKinds.Email.TYPE_WORK)
        setContactIdentity(contactId, "tg:$TG_WORK_NICK", plain())

        Log.i(LOG_TAG, "Create google contact done")
    }

    private fun createContact(data: ContentValues): Long {
        val rawContactUri = contentResolver.insert(RawContacts.CONTENT_URI, data)
        Log.i(LOG_TAG, "New contact URI: $rawContactUri")
        val contactId = ContentUris.parseId(rawContactUri!!)
        Log.i(LOG_TAG, "New contact ID: $contactId")
        return contactId
    }

    private fun updateContactName(contactId: Long, display: String, first: String, second: String) {
        // Условие для поиска имени контакта
        val where = "${ContactsContract.Data.CONTACT_ID} = ? AND " +
                "${ContactsContract.Data.MIMETYPE} = ?"
        val whereArgs = arrayOf(
            contactId.toString(),
            StructuredName.CONTENT_ITEM_TYPE
        )

        // Новые данные для обновления
        val values = ContentValues().apply {
            put(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
            put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
            put(StructuredName.DISPLAY_NAME, display)
            put(StructuredName.GIVEN_NAME, first)
            put(StructuredName.FAMILY_NAME, second)
        }

        // Выполнение обновления
        val rowsUpdated = contentResolver.update(
            ContactsContract.Data.CONTENT_URI,
            values,
            where,
            whereArgs
        )
        Log.i(LOG_TAG, "Name contact URI updated: $rowsUpdated")
    }

/*
    private fun linkRawContacts(
        rawContactId1: Long,
        rawContactId2: Long
    ) {
        val operations = ArrayList<ContentProviderOperation>()

        // Устанавливаем режим агрегации для первого контакта
        operations.add(
            ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.RawContacts._ID}=?",
                    arrayOf(rawContactId1.toString())
                )
                .withValue(
                    ContactsContract.RawContacts.AGGREGATION_MODE,
                    ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT
                )
                .build()
        )

        // Устанавливаем режим агрегации для второго контакта
        operations.add(
            ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.RawContacts._ID}=?",
                    arrayOf(rawContactId2.toString())
                )
                .withValue(
                    ContactsContract.RawContacts.AGGREGATION_MODE,
                    ContactsContract.RawContacts.AGGREGATION_MODE_IMMEDIATE
                )
                .build()
        )

        // Применяем операции
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
*/

    fun linkContacts(
        rawContactId1: Long,
        rawContactId2: Long
    ) {
        val operations = ArrayList<ContentProviderOperation>()

        operations.add(
            ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
                .withValue(
                    ContactsContract.AggregationExceptions.TYPE,
                    ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER
                )
                .withValue(
                    ContactsContract.AggregationExceptions.RAW_CONTACT_ID1,
                    rawContactId1
                )
                .withValue(
                    ContactsContract.AggregationExceptions.RAW_CONTACT_ID2,
                    rawContactId2
                )
                .build()
        )

        // Применяем операции
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Error linking contacts $rawContactId1, $rawContactId2", e)
//            e.printStackTrace()
        }
    }

    fun linkContacts2(
        rawContactId1: Long,
        rawContactId2: Long
    ) {
        val data = ContentValues()
        data.put(ContactsContract.AggregationExceptions.TYPE, ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER)
        data.put(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, rawContactId1)
        data.put(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, rawContactId2)

        val rawContactUri = contentResolver.insert(ContactsContract.AggregationExceptions.CONTENT_URI, data)
//        val rawContactUri = contentResolver.insert(RawContactsEntity.CONTENT_URI, data)
        Log.i(LOG_TAG, "Link $rawContactId1 + $rawContactId2: $rawContactUri")
    }

    private fun setContactEmail(contactId: Long, email:String, type:Int) {
        val data = ContentValues()
        data.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
        data.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
        data.put(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
        data.put(ContactsContract.CommonDataKinds.Email.TYPE, type)

        val rawContactUri = contentResolver.insert(ContactsContract.Data.CONTENT_URI, data)
//        val rawContactUri = contentResolver.insert(RawContactsEntity.CONTENT_URI, data)
        Log.i(LOG_TAG, "Email contact URI: $rawContactUri")
    }

    private fun setContactIdentity(contactId: Long, id: String, nameData: ContentValues) {
        nameData.put(ContactsContract.CommonDataKinds.Identity.MIMETYPE, ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE)
        nameData.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
        nameData.put(ContactsContract.CommonDataKinds.Identity.IDENTITY, id)

        val rawContactUri = contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameData)
        Log.i(LOG_TAG, "Identity contact URI: $rawContactUri")
    }

    private fun setTgContactData(contactId: Long, tgData: ContentValues, type: TgType) {
        val tgId = 51848647

        tgData.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.org.telegram.messenger.android.${type.mimetype}")
        tgData.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
        tgData.put(ContactsContract.Data.DATA1, tgId)
        tgData.put(ContactsContract.Data.DATA2, "Telegram ${type.pname}")
        tgData.put(ContactsContract.Data.DATA3, "${type.callTitle} $TG_WORK_NICK")
        tgData.put(ContactsContract.Data.DATA4, tgId)
//        tgData.put(RawContacts.Entity.SYNC1, tgNick)
//        tgData.put(RawContacts.Entity.SYNC2, tgId)
        val rawContactUri = contentResolver.insert(ContactsContract.Data.CONTENT_URI, tgData)
        Log.i(LOG_TAG, "Tg $type contact URI: $rawContactUri")
    }

    private fun tg(): ContentValues {
        val tgId = 51848647
        val tgNick = "@M_pashka"
        val tgProvider = "org.telegram.messenger"

        val tgData = ContentValues()
        tgData.put(RawContacts.ACCOUNT_TYPE, tgProvider)
        tgData.put(RawContacts.ACCOUNT_NAME, TG_SRB)
        return tgData
    }

    private fun google(): ContentValues {
        val gProvider = "com.google"
        val myGId = "pavelmoukhataevcontacts@gmail.com"

        val nameData = ContentValues()
        nameData.put(RawContacts.ACCOUNT_TYPE, gProvider)
        nameData.put(RawContacts.ACCOUNT_NAME, myGId)

        return nameData
    }

    private fun plain(): ContentValues {
        return ContentValues()
    }

    enum class TgType(val mimetype: String, val pname: String, val callTitle: String) {
        profile("profile", "Profile", "Message"),
        voice("call", "Voice Call", "Voice call"),
        video("call.video", "Video Call", "Video call"),
    }

    private fun showProviders() {
        Log.i(LOG_TAG, "Content providers")
        for (pack in packageManager.getInstalledPackages(PackageManager.GET_PROVIDERS)) {
            val providers = pack.providers
            if (providers != null) {
                for (provider in providers) {
                    Log.i(LOG_TAG, "provider: ${provider.authority} / $provider")
                }
            }
        }

        val providers: List<ProviderInfo> = packageManager.queryContentProviders(null, 0, 0)
        for (provider in providers) {
            Log.d(LOG_TAG, "provider: ${provider.authority} / $provider")
        }
    }

    private fun processRows(name: String, uri:Uri, filter: Predicate<Cursor>, fn: (contacts: Cursor) -> Unit) {
        Log.i(LOG_TAG, "Start $name from ${uri}...")

        try {
            val cursor = contentResolver.query(
                uri, null/*arrayOf(Contacts._ID)*/,
                null, null, null
            )

            if (cursor == null) {
                Log.i(LOG_TAG, "Contacts not found")
            } else {
                try {
                    Log.i(LOG_TAG, "Contacts found: ${cursor.count}")
                    while (cursor.moveToNext()) {
                        if (filter.test(cursor)) {
                            fn.invoke(cursor)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(LOG_TAG, "Show contact error", e)
                } finally {
                    cursor.close()
                }
            }
/*
                .use { contacts -> {
                    if (contacts == null) {
                        Log.i(LOG_TAG, "Contacts not found")
                    } else {
                        showContacts(contacts)
                    }
            }}
*/
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Internal error", e)
        }

        Log.i(LOG_TAG, "Done reading contacts")
    }

    // test(RawContacts.ACCOUNT_NAME, TG_WORK)
    // test("_id", testContactId.toInt())
    // test(RawContacts.CONTACT_ID, testContactId.toInt())
    // !findValue(contacts, "shoumkina") && !findValue(contacts, "telegram")

    private fun all(): Predicate<Cursor> = Predicate { true }
    private var firstIndex:Int = 0;
    private fun first(count: Int, predicate: Predicate<Cursor>): Predicate<Cursor> {
        firstIndex = 0
        return Predicate {
            if (predicate.test(it)) {
                firstIndex++
                firstIndex <= count
            } else {
                false
            }
        }
    }
    private fun test(column: String, value: String): Predicate<Cursor> = Predicate { getStringValue(it, column) == value }
    private fun test(column: String, value: Int): Predicate<Cursor> = Predicate { getIntValue(it, column) == value }

    data class User(val id: Int, val account_type: String, val account_name: String, val sync1: String, var name: String, var phone: String, var tg: String)
    interface UserField {
        fun get(user: User): String
        fun set(user: User, value: String)
    }
    object userFieldName: UserField {
        override fun get(user: User): String {
            return user.name
        }

        override fun set(user: User, value: String) {
            user.name = value
        }
    }
    private fun showTelegram(uri: Uri, filter: Predicate<Cursor>) {
        fun addUserField(user:User, value: String, field: UserField) {
            val modValue = value.replace(valDelimiterRegex, " ~ ")
            val oldValue = field.get(user)
            val newValue = if (oldValue.isEmpty()) modValue else {
                if (oldValue.split("/").any({it == modValue})) oldValue else "$oldValue$valDelimiter$modValue"
            }
            field.set(user, newValue)
        }

        val users: MutableMap<Int, User> = HashMap()
        processRows("contacts", uri, filter) contact@ { contacts ->
            val deleted = getIntValue(contacts, ContactsContract.Data.DELETED)
            if (deleted == 1) {
                return@contact
            }
            val id = getIntValue(contacts, ContactsContract.Data.CONTACT_ID)
            if (id == -1) {
                showFields(contacts)
            }
            val user = users.computeIfAbsent(id) {
                val accountType = getStringValue(contacts, RawContacts.ACCOUNT_TYPE)
                val accountName = getStringValue(contacts, RawContacts.ACCOUNT_NAME)
                val sync1 = getStringValue(contacts, RawContacts.SYNC1)
                User(id, accountType, accountName, sync1, "", "", "")
            }
            val mimetype = getStringValue(contacts, ContactsContract.Data.MIMETYPE)
            if (mimetype == "vnd.android.cursor.item/name") {
                addUserField(user, getStringValue(contacts, ContactsContract.Data.DATA1), userFieldName)
            } else if (mimetype == "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile") {
                user.tg = getStringValue(contacts, ContactsContract.Data.DATA1)
                setUserField(contacts, "Message ") { s -> user.phone = s}
            } else if (mimetype == "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call") {
                setUserField(contacts, "Voice call ") { s -> user.phone = s}
            } else if (mimetype == "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call.video") {
                setUserField(contacts, "Video call ") { s -> user.phone = s}
            } else {
                Log.i(LOG_TAG, "Unknown mime type: $mimetype")
                showFields(contacts)
            }
        }

        users.values.forEach { u ->
//            Log.i(LOG_TAG, "${u.id} ${u.name} phone:${u.phone} tg:${u.tg} acc:${u.account_name}")
            Log.i(LOG_TAG, "${u.tg}, ${u.name}, phone:${u.phone}")
        }

    }


    data class Contact(val id: Int, val _id: Int, val data: MutableMap<String, MutableMap<String, MutableSet<String>>>)
    private fun collectRawContact(filter: Predicate<Cursor>): Map<Int, Contact> {
        val contacts: MutableMap<Int, Contact> = HashMap()
        processRows("contacts", RawContactsEntity.CONTENT_URI, filter) contact@ { fieldRow ->
            val deleted = getIntValue(fieldRow, ContactsContract.Data.DELETED)
            if (deleted == 1) {
                return@contact
            }
            val id = getIntValue(fieldRow, ContactsContract.Data.CONTACT_ID)
            val _id = getIntValue(fieldRow, "_id")
            if (id == -1) {
                showFields(fieldRow)
            }
            val contact = contacts.computeIfAbsent(id) {
//                val accountType = getStringValue(field, RawContacts.ACCOUNT_TYPE)
//                val accountName = getStringValue(field, RawContacts.ACCOUNT_NAME)
//                val sync1 = getStringValue(field, RawContacts.SYNC1)
                Contact(id, _id, HashMap())
            }
            val mimetype = getStringValue(fieldRow, ContactsContract.Data.MIMETYPE)
//            contact.data.put(mimetype, )
            val data = contact.data.computeIfAbsent(mimetype) {HashMap()}
            for (k in arrayOf(ContactsContract.Data.DATA1, ContactsContract.Data.DATA2, ContactsContract.Data.DATA3, ContactsContract.Data.DATA4, ContactsContract.Data.DATA5)) {
                val index = fieldRow.getColumnIndex(k)
                if (index >= 0) {
                    val dataVal = fieldRow.getString(index)
                    if (dataVal != null && dataVal.isNotBlank()) {
//                        Log.i(LOG_TAG, "Contact[$id:$k]: $dataVal ($data)")
                        val dataVals = data.computeIfAbsent(k) { HashSet() }
                        dataVals.add(dataVal)
                    }
                }
            }
        }
        return contacts
    }


    data class Name(val display: String?, val first:String?, val second: String?) {
        fun full(): Boolean {
            return first != null && second != null && display != null
        }

        fun eq(other: Name): Boolean {
            fun cnts(v1: String?, v2: String?): Boolean {
                return v2?.let { v1?.contains(it) } ?: false
            }
            return full() && (
                    cnts(other.display, display) ||
                            (cnts(display, other.first) && cnts(display, other.second)) ||
                            (cnts(other.display, first) && cnts(other.display, second))
                    )
        }
    }
    data class GContact(val contactId: Int, val _id: Int, val yandexLogin: String, val telegram: String,
                        val displayName: Name, val gRawContact: Contact)
    data class TContact(val contactId: Int, val _id: Int, val telegramId: String, val telegramName: String?,
                        val displayName: Name, val yandex: Collection<String>, val tgRawContact: Contact)
    data class GlobalContact(var g: GContact?, var t: TContact?)
    private val yContacts = HashMap<String, GlobalContact>()
    private val tgContactByIds = HashMap<String, GlobalContact>()
    private val tgContactByNames = HashMap<String, GlobalContact>()
//    private val connectTgY = HashSet<GlobalContact>()

    private fun process(gRawContacts: Map<Int, Contact>, tgRawContacts: Map<Int, Contact>, links: HashMap<Int, MutableSet<Int>>) {
        fun debug(d: String) {
            Log.i(LOG_TAG, d)
        }

        Log.i(LOG_TAG, "Google contacts: ${gRawContacts.size}, Telegram contacts: ${tgRawContacts.size}")

        fun d(contact: Contact, mime: String, field: String) = contact.data.getOrDefault(mime, emptyMap())
            .getOrDefault(field, emptySet())

        fun n(contact: Contact): Name {
            // g - фамилия имя, d2-имя, d3-фамилия
            // t - имя фамилия, d2-имя, d3-фамилия
            val display = d(contact, "vnd.android.cursor.item/name", "data1").elementAtOrNull(0)
            val first = d(contact, "vnd.android.cursor.item/name", "data2").elementAtOrNull(0)
            val second = d(contact, "vnd.android.cursor.item/name", "data3").elementAtOrNull(0)
            return Name(display, first, second)
        }

        val y: (Collection<String>) -> Set<String> = { all ->
            all.filter { it.endsWith(Y_DOMAIN) }
                .map { it.substring(0, it.length - Y_DOMAIN.length) }
                .toSet()
        }


        for (gRawContact in gRawContacts.values) {
            val contactId = gRawContact.id
            val yLogins = y(d(gRawContact, "vnd.android.cursor.item/im", "data1") union d(gRawContact, "vnd.android.cursor.item/email_v2", "data1"))
            if (yLogins.size > 1) {
                Log.w(LOG_TAG, "Contact $contactId has multiple yandex $yLogins")
            }
            val yLogin = yLogins.elementAtOrElse(0, { "" })
            val tLogins = d(gRawContact,"vnd.android.cursor.item/website", "data1")
                .map { T_DOMAIN_REGEX.matcher(it) }
                .filter { it.matches() }
                .map { it.group(1)!! }
                .toSet()
            if (tLogins.size > 1) {
                Log.w(LOG_TAG, "Contact $contactId has multiple telegram $tLogins")
            }
            val tLogin = tLogins.elementAtOrElse(0, { "" })
            val gDisplayName = n(gRawContact)
            val g = GContact(contactId, gRawContact._id, yLogin, tLogin, gDisplayName, gRawContact)
            if (yLogin.isNotEmpty()) {
                val globalContact = yContacts.computeIfAbsent(yLogin) { GlobalContact(null, null) }
                globalContact.g = g
                if (tLogin.isNotEmpty()) {
                    tgContactByNames[tLogin] = globalContact
                    debug("Y:$yLogin -> tg:$tLogin")
                } else {
//                    Log.i(LOG_TAG, "Yandex $yLogin has no tg: ${d(contact,"vnd.android.cursor.item/website", "data1")}")
                }
            } else {
                val emails = d(gRawContact, "vnd.android.cursor.item/email_v2", "data1")
                Log.w(LOG_TAG, "GContact has no yandex login $emails: $gRawContact")
            }
        }

        for (tgRawContact in tgRawContacts.values) {
            val contactId = tgRawContact.id
            val tgIds = d(tgRawContact, "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile", "data1")
            val tgDisplayName = n(tgRawContact)
            val tgYandex = y(d(tgRawContact, "vnd.android.cursor.item/email_v2", "data1"))
            if (tgIds.size != 1) {
                Log.w(LOG_TAG, "Invalid Tg IDs: $tgIds")
            } else {
                val tgId = tgIds.elementAt(0)
                var tgName = tWorkMap[tgId.toLong()]
                if (tgName != null && tgName.isBlank()) {
                    tgName = null
                }
                if (tgYandex.isNotEmpty()) {
//                    debug("Tg $contactId:$tgId:$tgName:/${tgRawContact._id}. Yandex:$tgYandex. TgRaw:$tgRawContact")
                }
                val t = TContact(contactId, tgRawContact._id, tgId, tgName, tgDisplayName, tgYandex, tgRawContact)
                val globalT = if (tgName != null) {
                    var tByNameOld = tgContactByNames[tgName]
                    if (tByNameOld == null) {
                        tByNameOld = GlobalContact(null, t)
                    } else if (tByNameOld.t == null) {
                        tByNameOld.t = t
                    } else {
                        Log.w(LOG_TAG, "Same tg name [$tgName] for different contacts: ${tByNameOld.t?.contactId}:${tByNameOld.t?.telegramId} $tByNameOld")
                    }
                    tByNameOld
                } else {
                    GlobalContact(null, t)
                }
                tgContactByIds[tgId] = globalT
            }
        }

        Log.i(LOG_TAG, "Contacts. Y:${yContacts.size}, Tg:${tgContactByIds.size}, Tg named: ${tgContactByNames.size}")
        for (contact in tgContactByNames.values) {
            val t = contact.t
            val g = contact.g
//            debug("Telegram contact: ${t?.telegramName}, y:${t?.yandex}, G:${g != null}")
            if (t?.telegramName == null || g == null) {
                continue
            }
            val tgRawId = t._id
            val gRawId = g._id
            val yandexLogin = g.yandexLogin
            Log.i(LOG_TAG, "Connect telegram ${t.contactId}:$tgRawId:${t.telegramId}:${t.telegramName} to G:${g.contactId}:$gRawId:$yandexLogin")
            if (t.yandex.isEmpty()) {
                Log.i(LOG_TAG, "Connect by email telegram ${t.contactId}:${t.telegramId}:${t.telegramName} to G:${g.contactId}:$yandexLogin")
//                Log.i(LOG_TAG, "Tg ${t.tgRawContact}")
//                Log.i(LOG_TAG, "G ${g.gRawContact}")
                setContactEmail(tgRawId.toLong(), yandexLogin + Y_DOMAIN, ContactsContract.CommonDataKinds.Email.TYPE_WORK)

            } else {
//                Log.i(LOG_TAG, "Already connected by tg email")
            }

            if (links.getOrDefault(tgRawId, emptySet()).contains(gRawId)) {
//                Log.i(LOG_TAG, "Already connected by tg:$tgRawId -> g:$gRawId")
            } else if (links.getOrDefault(gRawId, emptySet()).contains(tgRawId)) {
//                Log.i(LOG_TAG, "Already connected by g:$gRawId -> tg:$tgRawId")
            } else {
                Log.i(LOG_TAG, "Connect by link telegram ${t.contactId}:$tgRawId:${t.telegramId}:${t.telegramName} to G:${g.contactId}:$gRawId:$yandexLogin")
//                linkContacts(tgRawId.toLong(), gRawId.toLong())
            }

            if (g.displayName.full() && !g.displayName.eq(t.displayName)) {
                // g - фамилия имя, d2-имя, d3-фамилия
                // t - имя фамилия, d2-имя, d3-фамилия
                Log.i(LOG_TAG, "Update telegram name: ${t.displayName.display} -> ${g.displayName}")
                updateContactName(t._id.toLong(), g.displayName.first + " " + g.displayName.second, g.displayName.first!!, g.displayName.second!!)
            }
        }
/*
        val yTelegramContacts = yContacts.values.filter { it.t?.telegramName != null }
        Log.i(LOG_TAG, "Yandex telegram contacts: ${yTelegramContacts.size}")
        for (y1 in yTelegramContacts) {
            Log.i(LOG_TAG, "yandex: ${y1.g?.yandexLogin}, telegram: ${y1.t?.telegramName}")
        }
*/
    }


    private fun setUserField(contacts: Cursor, prefix: String, fn: (String) -> Unit) {
//        ContactsContract.Data.DATA3
        val data3 = getStringValue(contacts, ContactsContract.CommonDataKinds.Contactables.LABEL)
        if (data3.startsWith(prefix)) {
            fn.invoke(data3.substring(prefix.length))
        } else {
            Log.i(LOG_TAG, "Unknown data3: $data3")
        }
    }

    private fun showFields(contacts: Cursor) {
        Log.i(LOG_TAG, "Contact: ${contacts.position}")
        for (i in contacts.columnNames.indices) {
            val columnName = contacts.columnNames[i]
            val type = contacts.getType(i)
            val typeStr = fieldTypes.getOrDefault(type, "_$type")
            if (type == Cursor.FIELD_TYPE_BLOB) {
                Log.i(LOG_TAG, "[${contacts.position}:$i:$columnName:$typeStr]: BLOB")
            } else {
                val value = contacts.getString(i)
                if (value != null) {
                    Log.i(LOG_TAG, "[${contacts.position}:$i:$columnName:$typeStr]: $value")
                }
            }
        }
    }

    private fun <T> getValue(contacts: Cursor, column: String, columnType: Int, defVal: T, fn: (Cursor, Int) -> T): T {
        val index = findColumnIndex(contacts, column, columnType)
        return if (index >=0) fn.invoke(contacts, index) else defVal
    }

    private fun findValue(cursor: Cursor, searchValue: String): Boolean {
        for (i in cursor.columnNames.indices) {
            if (!cursor.isNull(i) && cursor.getType(i) != Cursor.FIELD_TYPE_BLOB && cursor.getString(i) == searchValue) {
                return true
            }
        }
        return false
    }

    private fun findIntValue(contacts: Cursor, column: String, searchValue: Int): Boolean {
        return getValue(contacts, column, Cursor.FIELD_TYPE_INTEGER, false) { c, i ->
            val real = c.getInt(i)
//            Log.d(LOG_TAG, " search:$searchValue ${if (searchValue==real) "==" else "!="} real:$real")
            searchValue == real
        }
    }

    private fun getIntValue(contacts: Cursor, column: String): Int {
        return getValue(contacts, column, Cursor.FIELD_TYPE_INTEGER, INT_VALUE_NOT_FOUND) { c, i -> c.getInt(i) }
    }

    private fun getStringValue(contacts: Cursor, column: String): String {
        return getValue(contacts, column, Cursor.FIELD_TYPE_STRING, STRING_VALUE_NOT_FOUND) { c, i -> c.getString(i) }
    }

    private fun findColumnIndex(contacts: Cursor, column: String, columnType: Int): Int {
        val idx = contacts.getColumnIndex(column)
        if (idx >= 0 && contacts.getType(idx) != columnType) {
            return COLUMN_NOT_FOUND
        }
        return idx
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        const val valDelimiter = " / "
        val valDelimiterRegex = Regex(valDelimiter)

        const val LOG_TAG = "my-contacts"
        const val COLUMN_NOT_FOUND = -1
        const val INT_VALUE_NOT_FOUND = -1
        const val STRING_VALUE_NOT_FOUND = "not-found"
        const val TG_SRB = "6833731837"
        const val TG_WORK = "2081266979"
        const val TG_WORK_NICK = "@M_pashka"

        const val G_WORK = "pavel.moukhataev.work@gmail.com"

        const val Y_DOMAIN = "@yandex-team.ru"
//        const val T_DOMAIN = "https://t.me/"
        val T_DOMAIN_REGEX = Pattern.compile("https://t.me/([^/]+)/?")

//        const val gContactId = 315L
//        const val tgContactId = 316L
//        const val testContactId = tgContactId
//        const val testContactId = 321L

        val tWorkMap = buildMap {

            put(1474613229L, "ChatsImportBot")
            put(5514078368L, "PikZevsa")
            put(542396511L, "DanyaSpyBot")
            put(6412168736L, "Lisssichkaa")
            put(470131732L, "mus1andrey")
            put(362460547L, "SecureMasterTagAlert2Bot")
            put(748287104L, "pznamenskii")
            put(193217445L, "bembemd")
            put(2036926797L, "SvetlanaUshk")
            put(6592895198L, "CaiNiao_logistics_bot")
            put(5014819523L, "nastia_vk")
            put(141543493L, "antonstartsev")
            put(5209017943L, "Elton1108")
            put(108791374L, "cherepizzai")
            put(477838408L, "Viktorserbia")
            put(1441827L, "pticavorobey")
            put(5323419003L, "rtlabs_geps_dev_bot")
            put(264521726L, "BegetBot")
            put(507201024L, "Rino77s")
            put(506625549L, "m_tikhon")
            put(1483720740L, "Katie_Cookie")
            put(206119940L, "leonidsafronov")
            put(103421541L, "nadyaeva")
            put(1521826534L, "Tubarao_lv")
            put(5040804911L, "vignettery")
            put(161913339L, "avicorp")
            put(7916351416L, "stsvetk")
            put(118122336L, "syspulse")
            put(619838618L, "ada_cdo")
            put(5455500610L, "Nadj33")
            put(353969514L, "mityarzn")
            put(1726555795L, "iGeorgich")
            put(650856119L, "red_rose29")
            put(6499423001L, "Poiskkniga_bot")
            put(1865011028L, "ITmax_Alena")
            put(1663781717L, "DashaSavchebko")
            put(1593022065L, "cotzo87")
            put(129187643L, "Siniy")
            put(227091713L, "m1b5n")
            put(1782538935L, "arseniy_ok")
            put(1983147704L, "mffie")
            put(76104711L, "borodutch")
            put(6737119488L, "mrij_tuova306")
            put(439556772L, "avyurlova")
            put(7268975066L, "ajOlgaaj")
            put(5815951920L, "ggerditmsk")
            put(608693468L, "titovtrener")
            put(454271880L, "kamba89")
            put(127129352L, "ann_and")
            put(1003922250L, "Malinka365")
            put(386318233L, "maqorett")
            put(240455414L, "pictl42")
            put(800319253L, "danilinag")
            put(5909291158L, "Lakovleva")
            put(1125122982L, "El_la_t")
            put(841099924L, "Goldpride")
            put(5422553993L, "taempty8bot")
            put(1332041688L, "Ketti_82")
            put(6729709984L, "Hiking_avia")
            put(173556377L, "mshelukhin")
            put(433791261L, "tg_analytics_bot")
            put(481606771L, "Allaallaa")
            put(353758687L, "ofelya1212")
            put(621993437L, "Painful_insensitivity")
            put(1204659970L, "zoya_zai")
            put(1006755024L, "HR_Olga_T")
            put(813521067L, "MultiFactorBot")
            put(273234066L, "PostBot")
            put(135334092L, "EvgenyIlyin")
            put(156025261L, "d_roslyakov")
            put(868639534L, "alshimm")
            put(1258195473L, "Gulia2502")
            put(7320954030L, "Vizantiac")
            put(596120443L, "ithr22")
            put(5560755078L, "demid88888")
            put(1189221915L, "Annchous_S")
            put(172165818L, "alexwooden")
            put(1320146513L, "Cantor_Capoeira")
            put(446075805L, "railyakaM")
            put(5521254427L, "taempty3bot")
            put(584719306L, "Tanya_kukki")
            put(613166484L, "paaksyut")
            put(6920332646L, "Intellectum_academy")
            put(286001169L, "katya_noni")
            put(5448023443L, "taempty10bot")
            put(325171785L, "Xtotama")
            put(822714400L, "rpuchkin")
            put(1090404820L, "AdvanceClubBot")
            put(263999120L, "avayfr")
            put(6938283169L, "Roman_Liahovich")
            put(2081266979L, "ya_pashka")
            put(118631997L, "the_ook")
            put(224384109L, "olleran")
            put(5783654797L, "Give_me_your_cv")
            put(570624830L, "shodoff")
            put(616422909L, "Jelena777")
            put(4538627L, "topinsky")
            put(933394152L, "YuriiK1984")
            put(6086970245L, "terrazapusk27_bot")
            put(1319344111L, "RZDOfficialBot")
            put(6857327092L, "BelgradeITConnectBot")
            put(6180321586L, "pavelznam1")
            put(262194931L, "lisiymafin")
            put(1672651494L, "Natalja_atabaeva")
            put(715250681L, "Drgon79")
            put(1796833862L, "TenebrisOrAmens_bot")
            put(69680452L, "SergScherbakov")
            put(1427332202L, "mihpd")
            put(5797369871L, "work20005")
            put(1227415715L, "Deniss1601")
            put(683878741L, "marijanakk")
            put(850434834L, "username_to_id_bot")
            put(5579203887L, "mrandelovic")
            put(793362158L, "Mihalih")
            put(240044026L, "IFTTT")
            put(313092884L, "banofbot")
            put(202898858L, "DmNox")
            put(622412964L, "delimobil_support_bot")
            put(658044660L, "the_b_t_c")
            put(303041501L, "anton0xf")
            put(200942125L, "ControllerBot")
            put(1734481594L, "whitenigga667")
            put(419342628L, "Chernenko_Maria")
            put(1900140701L, "x5paket_bot")
            put(430766194L, "y_offer")
            put(567922744L, "kriskrisych")
            put(266841605L, "MasterIgriy")
            put(311341928L, "stefaniya_mind")
            put(225541248L, "andyboga")
            put(844757928L, "Vladislava_IT_Recruiter")
            put(42881919L, "AnitaBrid")
            put(47633188L, "Neighbour")
            put(269033981L, "sprut666666")
            put(5526780060L, "taempty4bot")
            put(5116352770L, "olgetmatch")
            put(5464251466L, "YuliaP_it")
            put(35048429L, "isthisfeya")
            put(8058960293L, "BelaVrana_bot")
            put(168439821L, "koroteevangelina")
            put(7138030462L, "DENhelp_BOT")
            put(215654102L, "SergeyJukov")
            put(1825818334L, "LyubochPolk")
            put(458021214L, "EnotikVkedaxxx")
            put(1043106873L, "ilyaryz")
            put(1006824980L, "RUTRA84")
            put(5583507157L, "taempty11bot")
            put(1182852252L, "Universaldbot")
            put(190042446L, "kiril_d1")
            put(266610118L, "tatoshka")
            put(396514699L, "IraKapralova")
            put(6936524637L, "Ivanovsergey35")
            put(919014650L, "chica_cdo")
            put(169556057L, "AnnRock")
            put(195717614L, "madm1ke")
            put(123712794L, "I_drozdoff")
            put(771096498L, "shieldy_bot")
            put(8065293035L, "nickoooo7")
            put(66924975L, "Kukarin_Ivan")
            put(7895508172L, "chatadminhelper158_bot")
            put(349165208L, "Gwittel")
            put(207818302L, "valerivardaya")
            put(116126858L, "kukushk")
            put(5938676634L, "redcatprint58")
            put(2033759070L, "Irinabolotova140")
            put(1665171750L, "Anchouuys")
            put(5545720039L, "official_apteka_ru_bot")
            put(5285722467L, "ThinkPadSX4")
            put(5181036222L, "vtsoukanov")
            put(450486440L, "ibriga")
            put(5415889080L, "endlesslatte")
            put(122164686L, "alenparf")
            put(906505349L, "valeriya_t01")
            put(322701743L, "etozhepirozhok")
            put(224774130L, "Ivan_sky")
            put(320295308L, "slnc_pls")
            put(207599358L, "n_i_c_k_n_a_m_e")
            put(481984027L, "konstantinna212")
            put(915637297L, "aleksey_shevchuk")
            put(1214282444L, "SofiaVishnevaya")
            put(673350284L, "Irina_Um")
            put(996157902L, "yana_couch")
            put(380922907L, "Boxberrybot")
            put(5277386815L, "di_mne")
            put(411828791L, "samoylov_vladimir")
            put(173005041L, "NoJpg")
            put(340604558L, "LennyEnglishBot")
            put(1059519970L, "Elena_Korneva122")
            put(5452525112L, "taempty6bot")
            put(1770322772L, "BegetNotificationBot")
            put(75667027L, "alexbobs")
            put(5044819555L, "mysksen")
            put(7388625536L, "massage_odintsovo_moscow")
            put(273533608L, "erosgyttyna")
            put(891844549L, "dimariktern")
            put(608313005L, "tomosaito")
            put(5861848983L, "gtimermanis")
            put(6571740483L, "MknEndz")
            put(1354553931L, "Manual_therapist")
            put(6830906844L, "zhiv_sam")
            put(460693903L, "getmyid_bot")
            put(1075245846L, "RandomGodBot")
            put(6134582947L, "frappapato")
            put(32398059L, "mazurkin")
            put(1260722211L, "evgenia_and_life")
            put(316451284L, "Cryan")
            put(5182078957L, "nomiD77")
            put(6844069094L, "karinikitina")
            put(370332941L, "Goldnbox")
            put(5683870187L, "AmetistBookBot")
            put(1296502373L, "Julia_Po_CV")
            put(894516855L, "dmiryy")
            put(247453080L, "vie2004")
            put(1994290675L, "Angel_Cveyi")
            put(84540985L, "msemenkin")
            put(465955058L, "ritfest_bot")
            put(518364635L, "gablb")
            put(724320367L, "Designer_LuisM9")
            put(149234593L, "CatharinaPirata")
            put(1618805558L, "GiveShareBot")
            put(677671728L, "JerarMukunku")
            put(303817036L, "belaaaaaaaya")
            put(5454089805L, "taempty7bot")
            put(37538151L, "Le_V1comte")
            put(191055933L, "melnikova_mashaa")
            put(910173473L, "BichevDmitry")
            put(54695987L, "Alex_077")
            put(209698574L, "chekmeneva_a")
            put(6020296100L, "akulavaaa")
            put(984703432L, "nastya_terentyeva")
            put(701635244L, "natallia_dzi")
            put(1539168316L, "Bluefilters_NSK")
            put(660260063L, "BelkaCar_bot")
            put(1611383685L, "MichalPersonaly")
            put(2020785123L, "tatyana_shumkina")
            put(553147242L, "ChatKeeperBot")
            put(7764280656L, "chatadminhelper162_bot")
            put(1744905114L, "Tatyana_Korshikova")
            put(204373937L, "zakharovvi")
            put(5835804788L, "CodeMatesBot")
            put(5822040230L, "granatapplebot")
            put(5893956841L, "SvetlanaBesedin")
            put(964035293L, "minitigrra")
            put(6567099586L, "BigTelecomHimki_bot")
            put(73979867L, "antonermak")
            put(97418872L, "realcapoeira")
            put(5019201693L, "GetBridgesBot")
            put(222768891L, "burmistrov_m")
            put(162726413L, "GroupHelpBot")
            put(7912471647L, "akiyamaraize12")
            put(783451506L, "martiianoff")
            put(572676578L, "AlexanderAbashin")
            put(1771905625L, "uagalkina")
            put(527230116L, "trophy_eyes")
            put(450165021L, "lida_nic")
            put(5715668838L, "AFeigenblatt")
            put(523752697L, "priletayskoree")
            put(429000L, "Stickers")
            put(7638101130L, "tem_money_admin")
            put(400593969L, "englexschool")
            put(181661964L, "Sledzevskaya")
            put(330633301L, "SecureMasterTagAlert6Bot")
            put(1644755433L, "vlads121")
            put(5739988418L, "Kety_Ig")
            put(901086812L, "AlekDavi")
            put(937255986L, "JUGConfSupport_bot")
            put(715497802L, "SafronKomar")
            put(7431330051L, "chatadmin_stable3bot")
            put(5517716246L, "taempty9bot")
            put(1398616228L, "sblogistica_bot")
            put(2083862581L, "Timeweb_help_ne_bot")
            put(661859191L, "zima80")
            put(6436977569L, "Eskera_228")
            put(2109747661L, "nhanipova")
            put(975576616L, "cap_pronin")
            put(5218564894L, "JohnDovvn")
            put(5158780777L, "Adtut777")
            put(645802592L, "olgaolga0201")
            put(522560269L, "Alexey_Sh_HR")
            put(787057350L, "orgrobot")
            put(1394530085L, "AntiServiceMessage_Bot")
            put(136817688L, "Channel_Bot")
            put(7207272919L, "life_Belarus_lifebot")
            put(209350712L, "vetal_rc")
            put(7487699465L, "Venkaveng")
            put(5452089523L, "taempty15bot")
            put(7932064900L, "rd_coolify_bot")
            put(84210004L, "PollBot")
            put(1371567506L, "Yuri_shumkin")
            put(565601775L, "yksnuter")
            put(974364238L, "alina_123356")
            put(2066837331L, "HR_inno")
            put(323790638L, "vasya_puzanov")
            put(7052148798L, "citydrive_ru_bot")
            put(1990664909L, "Boris41Love")
            put(5704595563L, "Sberlogistics_bot")
            put(6332579082L, "dcgarage")
            put(2059198146L, "lslobacheva")
            put(5889026947L, "tofifi30")
            put(530436887L, "belsts")
            put(5371575483L, "travelask_help_bot")
            put(5985253420L, "forum360advert1_bot")
            put(267891552L, "alxyopl")
            put(1509322094L, "Alex_Andrusenko")
            put(233152836L, "renieleon")
            put(484479048L, "Ktrnppkv")
            put(453734309L, "MaryAbros")
            put(296428826L, "AhmadZubr")
            put(5875239565L, "Rusrb_logistic")
            put(347074669L, "ks_aleshina")
            put(1100578139L, "doctorvav")
            put(250030637L, "MasterTagAlertBot")
            put(254911485L, "Inmago")
            put(5045413992L, "Ametist2021_bot")
            put(70068478L, "Tionlierite")
            put(1230480769L, "RemoveJoinGroupMsgBot")
            put(310664798L, "Sent_off")
            put(995786624L, "avvakandr")
            put(184098321L, "TinkoffServiceBot")
            put(2145897228L, "Kaschei123")
            put(84105713L, "Blaine93")
            put(5065138449L, "Serebryakova_TA")
            put(6627260409L, "provoice_bg")
            put(33490175L, "ggggrow")
            put(934544055L, "el_pomorochka")
            put(6362433076L, "Anety26")
            put(305129276L, "SkyengCareBot")
            put(1473809102L, "soffzzs")
            put(99485945L, "Softovick")
            put(93372553L, "BotFather")
            put(1343407369L, "losev78")
            put(5587417120L, "taempty16bot")
            put(1518931875L, "danka_radovanovic")
            put(2095831495L, "MelnikovaAlya")
            put(346511754L, "Nekrad")
            put(979257198L, "bavmsk")
            put(984711043L, "NotifyEventsBot")
            put(157967448L, "BashmakovaMaria")
            put(111369109L, "Dedok_s")
            put(5132025055L, "Dmitrytabot")
            put(1020490130L, "daniil68rus_868")
            put(5974544728L, "stabakman")
            put(850192363L, "anna_r_mebel")
            put(1271266957L, "replies")
            put(824276078L, "a_Irina")
            put(1635867865L, "k_delete")
            put(5458290696L, "taempty12bot")
            put(5245236673L, "travelask_moderator_bot")
            put(269380970L, "Psychodan666")
            put(7165118071L, "Restartcentr")
            put(278586876L, "StasyaLuckiest")
            put(221448277L, "Swepss")
            put(432473263L, "Shupala")
            put(1063086635L, "KatarinaONKY")
            put(953925037L, "AlfiyaSalakhetdin")
            put(5512072694L, "taempty13bot")
            put(401943177L, "TaisiiaSemina")
            put(1194771018L, "AndruHummer")
            put(760770778L, "U_L_T_I")
            put(5477980937L, "taempty2bot")
            put(1424447849L, "spiage")
            put(7240409625L, "CaptchaDijasporaBot")
            put(461696541L, "ASelectroPRO")
            put(7762674453L, "ruskadijaspora_bot")
            put(77873423L, "linktome")
            put(374385661L, "qwrzz88")
            put(1896680611L, "DSinikova")
            put(461923889L, "krakotay")
            put(1663061658L, "VTO_Prom")
            put(160662568L, "vaksenov")
            put(5900259945L, "Printbar_internet")
            put(431234173L, "fourpdarubot")
            put(348715268L, "noraltavir")
            put(5326045345L, "taempty1bot")
            put(465419882L, "PochtaruBot")
            put(5935674384L, "wowmiracle1")
            put(5421553348L, "taempty14bot")
            put(779811742L, "konovael")
            put(1989227387L, "Bankbnbbot")
            put(199042615L, "pmezentsev")
            put(449154879L, "anthemva")
            put(8155767779L, "m_pashka")
            put(860022461L, "kuraeva_e")
            put(379676838L, "edostavkabot")
            put(558489410L, "mary_m0s")
            put(175039587L, "Casetochka")
            put(292745058L, "alexey_petroff")
            put(192978331L, "e_sigitova")
            put(134201826L, "qvava")
            put(293947288L, "A_Potemkina")
            put(908619785L, "allwq0")
            put(5993112340L, "uladzimir_kash")
            put(1364060011L, "Vera_Baranova_psy")
            put(7770114315L, "dasha12345123")
            put(503141328L, "innoch_v")
            put(148211448L, "PetrIvanov")
            put(693837674L, "Kalabaaa")
            put(874822160L, "atrkhv")
            put(5144005807L, "Dregovik")
            put(1042581530L, "NanNanNanNanNanNa")
            put(782469368L, "Polina_recr")
            put(703328181L, "pmfsb")
            put(1018395349L, "inessazharikova")
            put(216552066L, "e_konturBot")
            put(1521855704L, "Oven2040")
            put(5191416576L, "phone_assistant_alice_bot")
            put(641712208L, "deep_cosmos_questions_bot")
            put(415372979L, "yrsubconscious")
            put(7194407593L, "SIS_4DM1N")
            put(1018030802L, "marginal_summer")
            put(715234341L, "tata3133")
            put(522008984L, "Darya_VW")
            put(175561983L, "Natalia_Tk")
            put(527380965L, "FabulouS01")
            put(1203841355L, "mvideo_official_bot")
            put(1980342425L, "IT_HR_Mary")
            put(176991261L, "gosuslugi_support_bot")
            put(5376802536L, "vishmivish")
            put(95486761L, "A_Atroshkina")
            put(699162423L, "nikolay_mukhataev")
            put(5304707727L, "TravelAsk_guide_adviser_bot")
            put(1667648964L, "NeuralUniversity_bot")
            put(235677859L, "viktorrazumnyy")
            put(7714518396L, "chatadminhelper161_bot")
            put(1964764076L, "BerdnikovaE")
            put(1002515832L, "coworking_port")
            put(200181038L, "it_OM_21")
            put(919590921L, "Alex19790420")
            put(1008294612L, "xabuku")
            put(5144661099L, "Makonis73")
            put(51848647L, "M_pashka_archive")
            put(8781397L, "Herbert_Wells")
            put(762319777L, "TravelAgency_By")
            put(205134562L, "alb2048")
            put(6835454388L, "juliagetmatch")
            put(133949728L, "boriskina")
            put(446544961L, "Zairameoow")
            put(440784024L, "EkaterinaMitroshkina")
            put(1172257537L, "maski_telega")
            put(343745180L, "dariashapka")
            put(99689239L, "silin_ilya")
            put(915525750L, "ilzaii1")
            put(5483589694L, "SelectelTRexBot")
            put(1986521230L, "joinhide17_bot")
            put(293828462L, "OttPlayerBot")
            put(1185216597L, "g_jobbot")
            put(876819120L, "lanaangelo444")
            put(190119342L, "rayskiy")
            put(6026600906L, "rogday_work")
            put(1190930932L, "master_mozaika")
            put(7433620508L, "dertip9")
            put(186991668L, "Pashtet64")
            put(470717212L, "KaterinaHR")
            put(184196590L, "Elizamur")
            put(5757237308L, "prokatavtonovisad")
            put(120500283L, "Aleksandra_Medova")
            put(1006345447L, "yaroslav0804")
            put(475779908L, "Nina_Shvyryaeva")
            put(1844464337L, "botsterbot")
            put(742394723L, "magkiy")
            put(553942561L, "spk27")
            put(371986699L, "nikolskaya_maria")
            put(350090228L, "danagra")
            put(73681736L, "AlexSmall")
            put(910090512L, "ImStill")
            put(7286906213L, "iwillwriteyou")
            put(567932655L, "artt652")
            put(6947383598L, "Flibuistar_bot")
            put(7437456497L, "citydrive_stickers_bot")
            put(1320896900L, "YurinaArt")
            put(7349209423L, "TatarIN2_0_0")
            put(1306634914L, "zerocodity_bot")
            put(1510235457L, "iamarmands")
            put(432617093L, "sadsadfred")
            put(22756885L, "ep2OX")
            put(459592746L, "borodylia_n")
            put(444579117L, "evgenysmirnov604")
            put(995671050L, "bugagaa10")
            put(7744977653L, "chatadminhelper160_bot")
            put(79772571L, "stroganoff")
            put(168959347L, "val_par")
            put(1382522123L, "ITAnnaa")
            put(153499208L, "ilyager")
            put(392470545L, "demeterp")
            put(266705040L, "Ninakou")
            put(358953490L, "Soulight")
            put(750990267L, "CJIuBoBoe_BaPeHbE")
            put(1873168603L, "flibusta_club_bot")
            put(1284250381L, "Roll007")
            put(88519372L, "shoumkina")
            put(1373558356L, "plavanie_khimki")
            put(547995450L, "opetrusevich")
            put(327214104L, "sashazhegal")
            put(895873733L, "AlenaSazhina")
            put(1010144144L, "rubezhanka")
            put(1014219732L, "Deliberate_Steps")
            put(5425876161L, "geps_robot")
            put(238137752L, "Miracle2005")
            put(1145887733L, "SportmasterChat_Bot")
            put(432539255L, "leo_kazantsev")
            put(305391094L, "svetlana_scher")
            put(7656855467L, "chatadminhelper159_bot")
            put(342129590L, "Ekaterina_Bredneva")
            put(857570501L, "Dmitry_Mukhin81")
            put(256376977L, "avkondrakov")
            put(6225953986L, "Renata_geras_it")
            put(291124870L, "chikaneellie")
            put(2071592101L, "arinazubun")
            put(996740278L, "kingoff_91")
            put(6188508983L, "Trusted_recommendation_bot")
            put(5487818997L, "iyliiaart")
            put(810979L, "kolyann87")
            put(432348726L, "topDA85")
            put(320212194L, "Anastasiia_sel")
            put(322561217L, "DrRinkman")
            put(5430864971L, "Random1zeBot")
            put(1494552298L, "Botka85")


put(1112279629L, "dimaputin31")
put(357518665L, "thelex0")
put(364979656L, "Antonidared")
put(122860377L, "speedstalker")
put(5514078368L, "PikZevsa")
put(558569798L, "dparhonin")
put(409775228L, "EmetsE")
put(35150346L, "boolker")
put(75192425L, "aokhotin")
            put(5667162572L, "")
put(6739648931L, "")
            put(163321255L, "")
            put(820288763L, "")
            put(248510191L, "")
            put(1119264403L, "")
put(1069364132L, "dnk2020")
put(6542964197L, "zapisnaCheckup")
put(318322403L, "egabzhamilova")
put(175258220L, "Aykeron")
put(7090273973L, "berdnikov_se_yndx")
put(122585958L, "AShergina")
put(387048252L, "")
put(7422721602L, "")
put(443169369L, "YandexHelpDeskbot")
put(477838408L, "Viktorserbia")
put(5915554989L, "ya_randomcoffeebot")
put(200576960L, "annatsepeleva")
put(265896940L, "vvp_vvp")
put(554073395L, "eborodina")
put(5323419003L, "rtlabs_geps_dev_bot")
put(511984316L, "Danara_Kh")
put(6129861987L, "")
put(506625549L, "m_tikhon")
put(145947889L, "glebskvortsov")
put(6569286601L, "")
put(301344190L, "krokodilchk")
put(195410924L, "ivanaxe")
put(257318685L, "lo_r_d")
put(5506651241L, "devmanool")
put(6617622048L, "an_afonina")
put(742066281L, "nslsrv")
put(210245855L, "albazh")
put(507983376L, "aanazaretyan")
put(171190890L, "")
put(5200355752L, "TashaChannelBot")
put(242642473L, "faustkun0")
put(323790638L, "vasya_puzanov")
put(431389141L, "UhuraToolsBot")
put(114954928L, "N0iseless")
put(6251172082L, "")
put(1133381605L, "naumbi4")
put(842431067L, "dmitry_i_denisov")
put(7227191977L, "")
put(602868608L, "DEADDEADx0")
put(5455500610L, "Nadj33")
put(353969514L, "mityarzn")
put(6391111813L, "agolikov_ya")
put(79300815L, "XCommandeRX")
put(97003966L, "yurial")
put(6332579082L, "dcgarage")
put(180385465L, "Telekme")
put(27887058L, "pgrigsch")
put(62515143L, "maxow")
put(1333034999L, "helloaolenevme")
put(223566296L, "Casepl")
put(988369528L, "KalchenkoNO")
put(6425029216L, "ya_karmometr_bot")
put(1574149451L, "releng")
put(6455967828L, "")
put(5964714778L, "zenyoga12")
put(222852565L, "dridgerve")
put(52813701L, "")
put(284836L, "")
put(68023467L, "lyalkaknopkina")
put(215399550L, "zelenskyds")
put(450902601L, "reshilkin")
put(199453522L, "")
put(58661836L, "gotocoding")
put(434668881L, "isiluyanov")
put(153814423L, "mshulaev")
put(826853878L, "Daria_Chutcheva")
put(6729709984L, "Hiking_avia")
put(173556377L, "mshelukhin")
put(163283321L, "gleb0t")
put(116103727L, "helloimindellawere")
put(108171959L, "")
put(349630483L, "evgrom")
put(2047719114L, "Kter1nka")
put(476899488L, "")
put(7209689454L, "")
put(761872829L, "slavyanskyy")
put(813521067L, "MultiFactorBot")
put(7236478859L, "")
put(5756854265L, "")
put(5456448993L, "")
put(1776603455L, "StojanovicMladen")
put(6282163880L, "ivansaikin_ya")
put(6300245850L, "ya_sum_bot")
put(5741402858L, "")
put(7154983590L, "HappyFoxhappy")
put(1719401115L, "")
put(417976333L, "smev_at")
put(897238431L, "kocks1k")
put(1495738816L, "")
put(6895956975L, "demiler_ya")
put(5126635898L, "RomanKoshedev")
put(153537999L, "verytable")
put(319085549L, "kramlih_k")
put(117143063L, "eugene_konkov")
put(415091793L, "juluasharakhomenko")
put(126944124L, "gluk47")
put(36336346L, "")
put(5560755078L, "demid88888")
put(172992181L, "EgorYuditskiy")
put(1320146513L, "Cantor_Capoeira")
put(124803700L, "")
put(188833349L, "cheb47")
put(863149689L, "oleynikov_sergey")
put(6377657646L, "sergei_butkin")
put(177671064L, "")
put(793835103L, "TashaNaturalBot")
put(484016598L, "Ch0p1k")
put(6967346777L, "")
put(7468369973L, "")
put(274043206L, "aleksandranedomolkina")
put(498924712L, "AnnaSBo")
put(215094727L, "HealingWard")
put(166398204L, "renadeen")
put(613166484L, "paaksyut")
put(443279218L, "rucyk")
put(477300074L, "")
put(93372553L, "BotFather")
put(984645545L, "Andrei_Lanin")
put(237077304L, "Kse_space")
put(286001169L, "katya_noni")
put(6125560835L, "")
put(391032401L, "Das_1gel")
put(5918221793L, "")
put(5585480139L, "ya_bruh")
put(5303501118L, "")
put(571444007L, "EL_Supyanov")
put(487729211L, "dozmorovadv")
put(6267487416L, "GodOfAchievementBot")
put(2081266979L, "ya_pashka")
put(118631997L, "the_ook")
put(422867972L, "mykr0t")
put(136689941L, "alina_moshkovich")
put(501516496L, "edorozhkina")
put(509926236L, "stasia_sav")
put(102036269L, "evakopylova")
put(5390690421L, "egorwork")
put(241659739L, "nogert")
put(570624830L, "shodoff")
put(149222874L, "ches13r")
put(388172386L, "miroslav2")
put(177258452L, "BorzenkoAnton")
put(175327028L, "Jollyekb")
put(1271266957L, "replies")
put(214982041L, "")
put(119879279L, "dmitko")
put(6649348378L, "")
put(196382436L, "gubanov_sergey")
put(270895477L, "lefantino")
put(234155173L, "Nypetrova")
put(202831172L, "kkembo")
put(199459511L, "chicherov")
put(117344171L, "arturgspb")
put(383999926L, "anton1821")
put(5959078507L, "cerg1168")
put(238067540L, "tasadovnikova")
put(131045034L, "lalekz")
put(620744090L, "IvanBeltsev")
put(82487693L, "miripiruni")
put(182488972L, "AngieSerebriakova")
put(436964198L, "kawabatat")
put(875354433L, "dogonthesun")
put(6068991185L, "")
put(753374601L, "")
put(66763413L, "rvetrov")
put(877481277L, "Rusnak_Olga_HR")
put(101865315L, "gromanev")
put(1699958826L, "")
put(1985737506L, "wallet")
put(1427332202L, "mihpd")
put(884093384L, "zdikov")
put(738336132L, "arishapav")
put(1292461185L, "penguinBurger")
put(5369213326L, "avkudelina")
put(426816737L, "rosamonaar")
put(966040L, "Efgen")
put(825176249L, "Bubnova_ya")
put(444532712L, "")
put(1891937628L, "leonmusdev")
put(6452428032L, "dmitriivarlamov")
put(1045061588L, "maxstroev")
put(474809837L, "trushkin_alexey")
put(5896884955L, "")
put(1149220313L, "")
put(119571078L, "artzlt")
put(183444297L, "verpex")
put(1938869L, "relizarov")
put(142347734L, "Tabada")
put(1295835287L, "igorman007")
put(397169159L, "YA_yulia_makarova")
put(401727069L, "shishqa")
put(393162432L, "khurtak")
put(101974951L, "oov0v")
put(826530032L, "scriptsdevtoolsbot")
put(5252188347L, "ronhill_work")
put(274350560L, "nastyaprolomova")
put(228684826L, "N_Starkov")
put(1533355L, "")
put(470353333L, "Bjorgg")
put(273160693L, "conquistador1306")
put(202898858L, "DmNox")
put(5626001131L, "uvray_w")
put(892362L, "yuliya_rubtsova")
put(172301186L, "Guzarevich")
put(1748432L, "ierogliph")
put(194924115L, "")
put(362877038L, "klimev")
put(107931582L, "beastofman")
put(717683477L, "monopaltus")
put(273689990L, "agdevera")
put(7442245184L, "")
put(5935674384L, "wowmiracle1")
put(117021476L, "andozer")
put(556480715L, "vadvolo")
put(6650153345L, "")
put(5992302792L, "L1danu")
put(370235379L, "release_machine_bot")
put(2135342756L, "staroverov_wrk")
put(6266600547L, "")
put(107701123L, "chlnts")
put(7625662319L, "yapashka_yandex_robot")
put(517934902L, "Huszarius")
put(348520745L, "JeriC4o")
put(231670846L, "lowgear")
put(5645507978L, "")
put(6657824033L, "")
put(7040552354L, "anailic84")
put(674956851L, "nu_saash")
put(6812032010L, "vtsyndx")
put(215654102L, "SergeyJukov")
put(329145742L, "ts123plus")
put(1409674773L, "weknowqp")
put(292745058L, "alexey_petroff")
put(5530374066L, "")
put(7434234120L, "")
put(283903733L, "NikonovFedor")
put(1202139580L, "vazaubaev_work")
put(379341959L, "Bayanist01")
put(192671079L, "dkochetov88")
put(210944655L, "combot")
put(351378159L, "msenin")
put(174340165L, "slonn")
put(271780368L, "uran1x")
put(475151991L, "vitocha")
put(6658943683L, "")
put(318218463L, "Aleks_Rensk")
put(659670393L, "VK_Structura")
put(298745831L, "")
put(101722169L, "Lelesia")
put(116870365L, "blackwithwhite")
put(7047168545L, "juliavelascob")
put(280768146L, "eugeon")
put(489725727L, "ilukatsk")
put(98894802L, "sereglond")
put(312749215L, "chizhonkov_ve")
put(483841770L, "haha_grusha")
put(419596094L, "evgenyvnukov")
put(85116718L, "lazuka23")
put(7005626286L, "aalyushin")
put(1267087801L, "OlegOtvetBot")
put(174281820L, "Ushakova")
put(760941337L, "l_the_dark_l")
put(6640655762L, "")
put(283172519L, "pirum1ch")
put(158494523L, "vdvision")
put(466126212L, "valyasokol")
put(125998541L, "ndtretyak")
put(227885018L, "grasscatreal")
put(483167900L, "klyushinmisha")
put(160468932L, "zebda")
put(2025764584L, "ya_arcanum_bot")
put(505739390L, "grigorev101")
put(377090085L, "postnikov_artem")
put(130379160L, "nikita01010101")
put(356011867L, "AlisaLapina")
put(290406186L, "")
put(1085978333L, "sergeysubbotin")
put(6118835932L, "")
put(5732307905L, "")
put(444319764L, "abdulla_b1")
put(450486440L, "ibriga")
put(426443549L, "gibzer")
put(115599508L, "JugglerSearchBot")
put(83036804L, "aromanovich")
put(5802827672L, "saha2231")
put(6712077930L, "")
put(412926694L, "nardzhiev")
put(129661L, "The_whiner")
put(616173255L, "saladin366")
put(230492099L, "urezan")
put(5692824879L, "")
put(316319832L, "SlayerGGXX")
put(181681234L, "vasya_toropov")
put(413567520L, "Siruufim")
put(5037749963L, "jovanastojanovic")
put(272990922L, "solidsanchez")
put(6229330396L, "")
put(6854619650L, "mk99wrk")
put(246436899L, "ya_iveus")
put(6213072070L, "anapav227")
put(249074764L, "st0ke")
put(628623330L, "totsamiydanya13")
put(699162423L, "nikolay_mukhataev")
put(320295308L, "slnc_pls")
put(288338866L, "Spentahh")
put(319061403L, "")
put(266017151L, "Vlytar")
put(214386275L, "oibeer")
put(5619516503L, "")
put(218023512L, "tboriev")
put(6976547187L, "")
put(345525808L, "rykon")
put(200779100L, "")
put(714404L, "")
put(223083493L, "kndrvt")
put(1282923225L, "akosarchuk")
put(2137397718L, "dtrue")
put(5970068842L, "")
put(929053144L, "die_pastete")
put(4512511L, "y_romanov")
put(291767205L, "YaIncBot")
put(288339949L, "dan_sleepo")
put(667566350L, "timofeytst")
put(215507472L, "")
put(292325285L, "MikailBag")
put(275478177L, "Solles_Albys")
put(140669608L, "maria_golofaeva")
put(81205789L, "Gromov1989")
put(349848087L, "YegorDB")
put(1410318017L, "altynbekPirman")
put(51848647L, "M_pashka_archive")
put(404300489L, "PVLTMK")
put(109074515L, "room599")
put(143427160L, "galqiwi")
put(440398049L, "")
put(145952364L, "unlanin")
put(92743527L, "Armijo")
put(7388625536L, "massage_odintsovo_moscow")
put(788429846L, "ghtlv")
put(326626719L, "stepik82")
put(140848780L, "Qelit")
put(180156503L, "headlessness")
put(469045528L, "YaAlekseeva")
put(483929653L, "theborrow")
put(795757955L, "")
put(6415635593L, "")
put(5802558254L, "ya_lev_kh")
put(6065806201L, "")
put(217015835L, "Tanya_Pav")
put(830013325L, "Led333")
put(6571740483L, "MknEndz")
put(5697139050L, "deltametroroyal")
put(6026600906L, "rogday_work")
put(58876287L, "warwish")
put(6339210020L, "chulkov_alex")
put(1111468225L, "aleksabaykova")
put(320472549L, "entr0py83")
put(372895033L, "shipovalovaksenia")
put(448809410L, "marv_l")
put(634464978L, "Eovchinn")
put(7733255157L, "oksana_procenkoo")
put(5788508L, "skel_nl")
put(8186769662L, "")
put(1190908412L, "thatsrightimkiryu")
put(256391232L, "jm_sub")
put(441442628L, "Maxumus")
put(2065339415L, "ilyabatkov4")
put(5966645423L, "")
put(5653108732L, "y4blokof")
put(548625340L, "banaev_alexandr")
put(60525705L, "ulyanin")
put(7614364364L, "")
put(68381447L, "O6JIOMOB")
put(718214617L, "ComputerPers")
put(102939957L, "hrustyashko")
put(505294086L, "look_valery")
put(1231978102L, "neShatokhina")
put(234273519L, "EVG_Vir")
put(116278770L, "DmiYakovlev")
put(475779908L, "Nina_Shvyryaeva")
put(1041902906L, "ithirteeng")
put(294306472L, "maximciel")
put(476477252L, "alexzellda")
put(777000L, "")
put(5699388560L, "")
put(553942561L, "spk27")
put(6903106652L, "")
put(1078285166L, "")
put(5842590479L, "")
put(118038584L, "michurinandrey")
put(169122082L, "dimitrylss")
put(5902232827L, "genesis_test_bot")
put(6488842746L, "")
put(6488936958L, "Alwarazs")
put(473372426L, "Ouliane")
put(537642825L, "")
put(7349209423L, "TatarIN2_0_0")
put(1306634914L, "zerocodity_bot")
put(5775918855L, "")
put(773542239L, "tester_meh")
put(626583058L, "Skeef79")
put(444172597L, "timothyxp")
put(432617093L, "sadsadfred")
put(312381906L, "dbashakin")
put(362063577L, "")
put(444579117L, "evgenysmirnov604")
put(254922853L, "chizhikov_alexey")
put(587703379L, "zhomanatr")
put(141098814L, "ArtDemidov")
put(765306570L, "")
put(138110816L, "")
put(100824409L, "")
put(739412814L, "vonovoselcev")
put(1331441666L, "")
put(5903312003L, "epguDevBot")
put(6220962223L, "maarulav_yandex")
put(744484669L, "Igor_Voronin93")
put(171147605L, "")
put(7130714720L, "math_helper_ai_bot")
put(5434074253L, "")
put(67826528L, "grindos")
put(6316013338L, "sfb_belgrade_skyline")
put(1200419041L, "BalandinAlex")
put(345483257L, "evgeniy_trishechkin")
put(153499208L, "ilyager")
put(1051329989L, "aleksskug")
put(6020296100L, "akulavaaa")
put(342835990L, "Glebodin")
put(8183483579L, "")
put(128170204L, "")
put(6786043898L, "")
put(257220822L, "")
put(95987258L, "NovikovaIra")
put(57795829L, "")
put(88519372L, "shoumkina")
put(118803591L, "apri_kot")
put(158360291L, "gProxyz")
put(183581028L, "oleg_dzenenko")
put(7435091132L, "isiluyanov_w")
put(134430617L, "Vladimir_Neverov")
put(413611836L, "GarnetAki")
put(159934997L, "xlebobulka")
put(446785855L, "ukuleleguy")
put(6875466321L, "")
put(6116640564L, "")
put(688040L, "thevery")
put(6821555514L, "")
put(420928179L, "s_isaev")
put(5199234638L, "")
put(442591695L, "Sandra15shu")
put(6833731837L, "")
put(1038959170L, "KonstantIMP")
put(420482720L, "nonickatall")
put(143638330L, "dmitry_golomolzin")
put(252714155L, "aculage")
put(275216344L, "")
put(6090077503L, "nekolyanich_work")
put(411139906L, "Savelievser")
put(427900484L, "k_kir")
put(5536837057L, "f16_eto_jet")
put(8105485528L, "Arte_3815")
put(7098721492L, "")
put(2112364065L, "ivantkhnv")
put(5715668838L, "AFeigenblatt")
put(5318317877L, "")
put(231642626L, "kirresponsible")
put(5913344935L, "CalendulosBot")
put(177049951L, "tsbalzhanov")
put(446429512L, "q57V59p3rCBIt1avV")
put(331494421L, "leventsov")
put(6619714886L, "")
put(6700283015L, "achizhikov_work")
put(227979852L, "sbelov1")
put(7976471728L, "aillario")
put(145826670L, "hurd54")
put(77220730L, "")
put(787329295L, "misasos")
put(5198052922L, "")
put(7758765468L, "")
put(260234346L, "on1kova")
put(1035925079L, "YandexSportsBot")
put(378129762L, "AndrewBystrov")
put(5243559782L, "ya_rudeshko")
put(6447943613L, "")
put(669213712L, "SolomonMonitoringBot")
put(6684813707L, "AndreyService")
put(5537682922L, "BayginRR")
put(120895689L, "bragovo")
put(843280828L, "")
put(445210998L, "lexxxandr")
        }

        val missedNumbers = setOf(
            1509,
            1681,
            1682,
            1683,
            1684,
            1685,
            1686,
            1687,
            1688,
            1689,
            1690,
            1691,
            1692,
            1717,
            1718,
            1721,
            1722,
            1725,
            1726,
            1741,
            1768,
            1779,
        )



        val fieldTypes: Map<Int, String> = mapOf(Cursor.FIELD_TYPE_NULL to "Null",
            Cursor.FIELD_TYPE_INTEGER to "Integer",
            Cursor.FIELD_TYPE_FLOAT to "Float",
            Cursor.FIELD_TYPE_STRING to "String",
            Cursor.FIELD_TYPE_BLOB to "Blob"
        )
    }
}
