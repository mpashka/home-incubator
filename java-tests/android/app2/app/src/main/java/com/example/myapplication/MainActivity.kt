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
        setContactName(contactId, name, plain())

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
        setContactName(contactId, name, plain())
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

    private fun setContactName(contactId: Long, name: String, nameData: ContentValues) {
        nameData.put(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
        nameData.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
        nameData.put(StructuredName.DISPLAY_NAME, name)

        val rawContactUri = contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameData)
        Log.i(LOG_TAG, "Name contact URI: $rawContactUri")
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


    data class GContact(val contactId: Int, val _id: Int, val yandexLogin: String, val telegram: String, val gRawContact: Contact)
    data class TContact(val contactId: Int, val _id: Int, val telegramId: String, val telegramName: String?, val yandex: Collection<String>, val tgRawContact: Contact)
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
            val g = GContact(contactId, gRawContact._id, yLogin, tLogin, gRawContact)
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
            val tgYandex = y(d(tgRawContact, "vnd.android.cursor.item/email_v2", "data1"))
            if (tgIds.size != 1) {
                Log.w(LOG_TAG, "Invalid Tg IDs: $tgIds")
            } else {
                val tgId = tgIds.elementAt(0)
                val tgName = tWorkMap[tgId.toLong()]
                if (tgYandex.isNotEmpty()) {
//                    debug("Tg $contactId:$tgId:$tgName:/${tgRawContact._id}. Yandex:$tgYandex. TgRaw:$tgRawContact")
                }
                val t = TContact(contactId, tgRawContact._id, tgId, tgName, tgYandex, tgRawContact)
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
//                setContactEmail(tRawId.toLong(), yandexLogin + Y_DOMAIN, ContactsContract.CommonDataKinds.Email.TYPE_WORK)

            } else {
                Log.i(LOG_TAG, "Already connected by tg email")
            }

            if (links.getOrDefault(tgRawId, emptySet()).contains(gRawId)) {
                Log.i(LOG_TAG, "Already connected by tg:$tgRawId -> g:$gRawId")
            } else if (links.getOrDefault(gRawId, emptySet()).contains(tgRawId)) {
                Log.i(LOG_TAG, "Already connected by g:$gRawId -> tg:$tgRawId")
            } else {
                Log.i(LOG_TAG, "Connect by link telegram ${t.contactId}:$tgRawId:${t.telegramId}:${t.telegramName} to G:${g.contactId}:$gRawId:$yandexLogin")
//                linkContacts(tgRawId.toLong(), gRawId.toLong())
//                linkContacts2(g._id.toLong(), t._id.toLong())
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
            put(7040552354L, "anailic84")
            put(357518665L, "thelex0")
            put(674956851L, "nu_saash")
            put(364979656L, "Antonidared")
            put(122860377L, "speedstalker")
            put(6812032010L, "vtsyndx")
            put(6896809752L, "HikaruTeamBot")
            put(558569798L, "dparhonin")
            put(409775228L, "EmetsE")
            put(329145742L, "ts123plus")
            put(1409674773L, "weknowqp")
            put(35150346L, "boolker")
            put(75192425L, "aokhotin")
            put(6542964197L, "zapisnaCheckup")
            put(5583507157L, "taempty11bot")
            put(5565996505L, "bahbka_ya")
            put(192671079L, "dkochetov88")
            put(351378159L, "msenin")
            put(175258220L, "Aykeron")
            put(174340165L, "slonn")
            put(271780368L, "uran1x")
            put(7090273973L, "berdnikov_se_yndx")
            put(125267295L, "semikovaanna")
            put(116870365L, "blackwithwhite")
            put(280768146L, "eugeon")
            put(443169369L, "YandexHelpDeskbot")
            put(7691883599L, "HikaruToolsBot")
            put(312749215L, "chizhonkov_ve")
            put(483841770L, "haha_grusha")
            put(419596094L, "evgenyvnukov")
            put(7005626286L, "aalyushin")
            put(1267087801L, "OlegOtvetBot")
            put(145947889L, "glebskvortsov")
            put(301344190L, "krokodilchk")
            put(195410924L, "ivanaxe")
            put(158494523L, "vdvision")
            put(5452089523L, "taempty15bot")
            put(257318685L, "lo_r_d")
            put(466126212L, "valyasokol")
            put(125998541L, "ndtretyak")
            put(227885018L, "grasscatreal")
            put(6617622048L, "an_afonina")
            put(250388670L, "mariekeri")
            put(160468932L, "zebda")
            put(2025764584L, "ya_arcanum_bot")
            put(507983376L, "aanazaretyan")
            put(6837164509L, "alexbuchkin_work")
            put(377090085L, "postnikov_artem")
            put(130379160L, "nikita01010101")
            put(431389141L, "UhuraToolsBot")
            put(1085978333L, "sergeysubbotin")
            put(114954928L, "N0iseless")
            put(1133381605L, "naumbi4")
            put(602868608L, "DEADDEADx0")
            put(444319764L, "abdulla_b1")
            put(426443549L, "gibzer")
            put(97003966L, "yurial")
            put(83036804L, "aromanovich")
            put(115599508L, "JugglerSearchBot")
            put(6143605847L, "aerial_alin")
            put(5802827672L, "saha2231")
            put(412926694L, "nardzhiev")
            put(129661L, "The_whiner")
            put(616173255L, "saladin366")
            put(230492099L, "urezan")
            put(316319832L, "SlayerGGXX")
            put(1333034999L, "helloaolenevme")
            put(181681234L, "vasya_toropov")
            put(413567520L, "Siruufim")
            put(223566296L, "Casepl")
            put(988369528L, "KalchenkoNO")
            put(5371575483L, "travelask_help_bot")
            put(6213072070L, "anapav227")
            put(249074764L, "st0ke")
            put(6425029216L, "ya_karmometr_bot")
            put(1574149451L, "releng")
            put(628623330L, "totsamiydanya13")
            put(5964714778L, "zenyoga12")
            put(218023512L, "tboriev")
            put(215399550L, "zelenskyds")
            put(223083493L, "kndrvt")
            put(1282923225L, "akosarchuk")
            put(2137397718L, "dtrue")
            put(58661836L, "gotocoding")
            put(929053144L, "die_pastete")
            put(434668881L, "isiluyanov")
            put(4512511L, "y_romanov")
            put(5422553993L, "taempty8bot")
            put(291767205L, "YaIncBot")
            put(288339949L, "dan_sleepo")
            put(349630483L, "evgrom")
            put(5452525112L, "taempty6bot")
            put(275478177L, "Solles_Albys")
            put(140669608L, "maria_golofaeva")
            put(1410318017L, "altynbekPirman")
            put(51848647L, "M_pashka_archive")
            put(404300489L, "PVLTMK")
            put(143427160L, "galqiwi")
            put(1776603455L, "StojanovicMladen")
            put(6282163880L, "ivansaikin_ya")
            put(145952364L, "unlanin")
            put(92743527L, "Armijo")
            put(5577954968L, "taempty17bot")
            put(6895956975L, "demiler_ya")
            put(153537999L, "verytable")
            put(415091793L, "juluasharakhomenko")
            put(483929653L, "theborrow")
            put(5802558254L, "ya_lev_kh")
            put(830013325L, "Led333")
            put(58876287L, "warwish")
            put(6026600906L, "rogday_work")
            put(634464978L, "Eovchinn")
            put(1190908412L, "thatsrightimkiryu")
            put(256391232L, "jm_sub")
            put(441442628L, "Maxumus")
            put(5521254427L, "taempty3bot")
            put(793835103L, "TashaNaturalBot")
            put(498924712L, "AnnaSBo")
            put(215094727L, "HealingWard")
            put(166398204L, "renadeen")
            put(102939957L, "hrustyashko")
            put(1231978102L, "neShatokhina")
            put(443279218L, "rucyk")
            put(93372553L, "BotFather")
            put(237077304L, "Kse_space")
            put(234273519L, "EVG_Vir")
            put(984645545L, "Andrei_Lanin")
            put(116278770L, "DmiYakovlev")
            put(5585480139L, "ya_bruh")
            put(294306472L, "maximciel")
            put(551825859L, "ycloud_duty_bot")
            put(5448023443L, "taempty10bot")
            put(5587417120L, "taempty16bot")
            put(6267487416L, "GodOfAchievementBot")
            put(2081266979L, "ya_pashka")
            put(118631997L, "the_ook")
            put(136689941L, "alina_moshkovich")
            put(118038584L, "michurinandrey")
            put(169122082L, "dimitrylss")
            put(5902232827L, "genesis_test_bot")
            put(5132025055L, "Dmitrytabot")
            put(5390690421L, "egorwork")
            put(241659739L, "nogert")
            put(177258452L, "BorzenkoAnton")
            put(1271266957L, "replies")
            put(119879279L, "dmitko")
            put(270895477L, "lefantino")
            put(234155173L, "Nypetrova")
            put(202831172L, "kkembo")
            put(254922853L, "chizhikov_alexey")
            put(117344171L, "arturgspb")
            put(5959078507L, "cerg1168")
            put(5454089805L, "taempty7bot")
            put(5458290696L, "taempty12bot")
            put(6220962223L, "maarulav_yandex")
            put(5245236673L, "travelask_moderator_bot")
            put(131045034L, "lalekz")
            put(7130714720L, "math_helper_ai_bot")
            put(82487693L, "miripiruni")
            put(67826528L, "grindos")
            put(6316013338L, "sfb_belgrade_skyline")
            put(436964198L, "kawabatat")
            put(66763413L, "rvetrov")
            put(1051329989L, "aleksskug")
            put(877481277L, "Rusnak_Olga_HR")
            put(342835990L, "Glebodin")
            put(1985737506L, "wallet")
            put(884093384L, "zdikov")
            put(5512072694L, "taempty13bot")
            put(5477980937L, "taempty2bot")
            put(426816737L, "rosamonaar")
            put(118803591L, "apri_kot")
            put(183581028L, "oleg_dzenenko")
            put(966040L, "Efgen")
            put(825176249L, "Bubnova_ya")
            put(7435091132L, "isiluyanov_w")
            put(1891937628L, "leonmusdev")
            put(134430617L, "Vladimir_Neverov")
            put(413611836L, "GarnetAki")
            put(446785855L, "ukuleleguy")
            put(1045061588L, "maxstroev")
            put(474809837L, "trushkin_alexey")
            put(183444297L, "verpex")
            put(119571078L, "artzlt")
            put(1938869L, "relizarov")
            put(1295835287L, "igorman007")
            put(401727069L, "shishqa")
            put(5497150986L, "RomanPortwine")
            put(442591695L, "Sandra15shu")
            put(1038959170L, "KonstantIMP")
            put(101974951L, "oov0v")
            put(826530032L, "scriptsdevtoolsbot")
            put(143638330L, "dmitry_golomolzin")
            put(274350560L, "nastyaprolomova")
            put(252714155L, "aculage")
            put(273160693L, "conquistador1306")
            put(5626001131L, "uvray_w")
            put(6090077503L, "nekolyanich_work")
            put(411139906L, "Savelievser")
            put(5536837057L, "f16_eto_jet")
            put(892362L, "yuliya_rubtsova")
            put(2112364065L, "ivantkhnv")
            put(362877038L, "klimev")
            put(5913344935L, "CalendulosBot")
            put(107931582L, "beastofman")
            put(446429512L, "q57V59p3rCBIt1avV")
            put(717683477L, "monopaltus")
            put(5326045345L, "taempty1bot")
            put(370235379L, "release_machine_bot")
            put(5992302792L, "L1danu")
            put(787329295L, "misasos")
            put(2135342756L, "staroverov_wrk")
            put(5421553348L, "taempty14bot")
            put(7625662319L, "yapashka_yandex_robot")
            put(107701123L, "chlnts")
            put(517934902L, "Huszarius")
            put(6188508983L, "Trusted_recommendation_bot")
            put(348520745L, "JeriC4o")
            put(609346464L, "smaugcow")
            put(231670846L, "lowgear")
            put(669213712L, "SolomonMonitoringBot")
            put(5517716246L, "taempty9bot")
            put(5526780060L, "taempty4bot")
            put(5537682922L, "BayginRR")
            put(120895689L, "bragovo")
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
