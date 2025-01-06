package com.example.myapplication

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
import androidx.appcompat.app.AppCompatActivity
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

//        showProviders()

//        showContent(ContactsContract.Settings.CONTENT_URI)

            /*
        showContent(ContactsContract.Profile.CONTENT_URI)
        showContent(ContactsContract.Contacts.CONTENT_URI)
        showContent(ContactsContract.RawContactsEntity.CONTENT_URI)
        showContent(ContactsContract.RawContacts.CONTENT_URI)
        showContent(ContactsContract.Data.CONTENT_URI)
        showContent(ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        // Email, StructuredPostal, Callable, Contactables
        showContent(ContactsContract.Groups.CONTENT_URI)
        showContent(ContactsContract.AggregationExceptions.CONTENT_URI)
        showContent(ContactsContract.Settings.CONTENT_URI)
        showContent(ContactsContract.ProviderStatus.CONTENT_URI)
//        showContent(ContactsContract.DisplayPhoto.CONTENT_URI)
*/

//        showField(ContactsContract.RawContactsEntity.CONTENT_URI)
            showContent(ContactsContract.Settings.CONTENT_URI)
//            doCreateContact()

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
        setContactEmail(contactId, plain())

        Log.i(LOG_TAG, "Create telegram contact done")
    }

    private fun doCreateContactGoogle(name: String) {
        Log.i(LOG_TAG, "Create google contact...")

        val contactId = createContact(google())
        setContactName(contactId, name, plain())
        setContactEmail(contactId, plain())
        setContactIdentity(contactId, "tg:" + tgNick, plain())

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

    private fun setContactEmail(contactId: Long, data: ContentValues) {
        data.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
        data.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
        data.put(ContactsContract.CommonDataKinds.Email.ADDRESS, "my_email@gmail.com")
        data.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)

        val rawContactUri = contentResolver.insert(ContactsContract.Data.CONTENT_URI, data)
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
        tgData.put(ContactsContract.Data.DATA3, "${type.callTitle} $tgNick")
        tgData.put(ContactsContract.Data.DATA4, tgId)
//        tgData.put(RawContacts.Entity.SYNC1, tgNick)
//        tgData.put(RawContacts.Entity.SYNC2, tgId)
        val rawContactUri = contentResolver.insert(ContactsContract.Data.CONTENT_URI, tgData)
        Log.i(LOG_TAG, "Tg $type contact URI: $rawContactUri")
    }

    private fun tg(): ContentValues {
        val tgId = 51848647
        val tgNick = "@M_pashka"
        val myTgId = "6833731837"
        val tgProvider = "org.telegram.messenger"

        val tgData = ContentValues()
        tgData.put(RawContacts.ACCOUNT_TYPE, tgProvider)
        tgData.put(RawContacts.ACCOUNT_NAME, myTgId)
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

    private fun processContent(name: String, uri:Uri, fn: (uri:Uri, contacts: Cursor) -> Unit) {
        Log.i(LOG_TAG, "Start $name from ${uri}...")

        try {
            val contacts = contentResolver.query(
                uri, null/*arrayOf(Contacts._ID)*/,
                null, null, null
            )

            if (contacts == null) {
                Log.i(LOG_TAG, "Contacts not found")
            } else {
                try {
                    Log.i(LOG_TAG, "Contacts found: ${contacts.count}")
                    while (contacts.moveToNext()) {
                        fn.invoke(uri, contacts)
                    }
                } catch (e: Exception) {
                    Log.w(LOG_TAG, "Show contact error", e)
                } finally {
                    contacts.close()
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

    data class User(val id: Int, val account_type: String, val account_name: String, val sync1: String, var name: String, var phone: String, var tg: String)
    val users: MutableMap<Int, User> = HashMap()
    private fun showField(contentUri: Uri) {
        processContent("contacts", contentUri) contact@ { uri, contacts ->
            val deleted = getIntValue(uri, contacts, "deleted")
            if (deleted == 1) {
                return@contact
            }
            val id = getIntValue(uri, contacts, "contact_id")
            if (id == -1) {
                showFields(contacts)
            }
            val user = users.computeIfAbsent(id, {i ->
                val accountType = getStringValue(uri, contacts, "account_type")
                val accountName = getStringValue(uri, contacts, "account_name")
                val sync1 = getStringValue(uri, contacts, "sync1")
                User(id, accountType, accountName, sync1, "", "", "")
            })
            val mimetype = getStringValue(uri, contacts, "mimetype")
            if (mimetype == "vnd.android.cursor.item/name") {
                user.name = getStringValue(uri, contacts, "data1")
            } else if (mimetype == "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile") {
                user.tg = getStringValue(uri, contacts, "data1")
                setUserField(uri, contacts, "Message ") {s -> user.phone = s}
            } else if (mimetype == "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call") {
                setUserField(uri, contacts, "Voice call ") {s -> user.phone = s}
            } else if (mimetype == "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call.video") {
                setUserField(uri, contacts, "Video call ") {s -> user.phone = s}
            } else {
                Log.i(LOG_TAG, "Unknown mime type: $mimetype")
                showFields(contacts)
            }
        }

        users.values.forEach { u ->
            Log.i(LOG_TAG, "${u.id} ${u.name} phone:${u.phone} tg:${u.tg} acc:${u.account_name}")
        }
    }

    private fun setUserField(uri: Uri, contacts: Cursor, prefix: String, fn: (String) -> Unit) {
        val data3 = getStringValue(uri, contacts, "data3")
        if (data3.startsWith(prefix)) {
            fn.invoke(data3.substring(prefix.length))
        } else {
            Log.i(LOG_TAG, "Unknown data3: $data3")
        }
    }

    private fun showContent(contentUri: Uri) {
        processContent("contacts", contentUri) ret@ { uri, contacts ->
            if (getStringValue(uri, contacts, "account_name") != myTgWork) {
                return@ret
            }
//            if (!findIntValue(uri, contacts, "_id", testContactId.toInt())) {
//                return@ret
//            }
//            if (!findIntValue(uri, contacts, "contact_id", testContactId.toInt())) {
//                return@ret
//            }

/*
            if (!findValue(contacts, "shoumkina") && !findValue(contacts, "telegram")) {
                continue
            }
*/
            showFields(contacts)
        }
    }

    private fun showFields(contacts: Cursor) {
        Log.i(LOG_TAG, "Contact: ${contacts.position}")
        for (i in contacts.columnNames.indices) {
            val columnName = contacts.columnNames[i]
            val type = contacts.getType(i)
            val typeStr = fieldTypes.getOrDefault(type, "_$type")
            val value = contacts.getString(i)
            Log.i(LOG_TAG, "[${contacts.position}:$i:$columnName:$typeStr]: $value")
        }
    }

    private fun <T> getValue(uri: Uri, contacts: Cursor, column: String, columnType: Int, defVal: T, fn: (Cursor, Int) -> T): T {
        val index = findColumnIndex(contacts, column, columnType)
        return if (index >=0) fn.invoke(contacts, index) else defVal
    }

    private fun findIntValue(uri: Uri, contacts: Cursor, column: String, searchValue: Int): Boolean {
        return getValue(uri, contacts, column, Cursor.FIELD_TYPE_INTEGER, false) { c, i ->
            val real = c.getInt(i)
//            Log.d(LOG_TAG, " search:$searchValue ${if (searchValue==real) "==" else "!="} real:$real")
            searchValue == real
        }
    }

    private fun getIntValue(uri: Uri, contacts: Cursor, column: String): Int {
        return getValue(uri, contacts, column, Cursor.FIELD_TYPE_INTEGER, -1) { c, i -> c.getInt(i) }
    }

    private fun getStringValue(uri: Uri, contacts: Cursor, column: String): String {
        return getValue(uri, contacts, column, Cursor.FIELD_TYPE_STRING, "not-found") { c, i -> c.getString(i) }
    }

    private fun findColumnIndex(contacts: Cursor, column: String, columnType: Int): Int {
        val idx = contacts.getColumnIndex(column)
        if (idx >= 0 && contacts.getType(idx) != columnType) {
            return -1
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
        const val LOG_TAG = "my-contacts"
        const val gContactId = 315L
        const val tgContactId = 316L
        const val testContactId = tgContactId
//        const val testContactId = 321L

        const val myTgWork = "2081266979"

        const val tgNick = "@M_pashka"

        val fieldTypes: Map<Int, String> = mapOf(Cursor.FIELD_TYPE_NULL to "Null",
            Cursor.FIELD_TYPE_INTEGER to "Integer",
            Cursor.FIELD_TYPE_FLOAT to "Float",
            Cursor.FIELD_TYPE_STRING to "String",
            Cursor.FIELD_TYPE_BLOB to "Blob"
        )
    }
}
