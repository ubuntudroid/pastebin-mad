package omg.lol.pastebin.core.data.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import omg.lol.pastebin.core.database.user.model.DbUser
import omg.lol.pastebin.core.database.user.model.UserDao
import omg.lol.pastebin.core.database.user.model.mapToUser
import omg.lol.pastebin.core.model.user.User
import javax.inject.Inject

// TODO: return values and errors should be wrapped in DataSources like in PastebinRepository
interface UserRepository {
    val user: Flow<User?>
    suspend fun login(name: String, apiKey: String)
    val isAuthenticated: Flow<Boolean>
        get() = user.map { it != null }
}

class DbBackedUserRepository @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override val user: Flow<User?> = userDao.getUser().map { it?.mapToUser() }
        .distinctUntilChanged()

    override suspend fun login(name: String, apiKey: String) {
        require(name.isNotBlank() && apiKey.isNotBlank())
        // TODO in a real app we would obviously store sensible user information in an encrypted way
        //  such as the Android Keystore (which however doesn't work if the device is locked, but
        //  that's fine in most cases as background updates should happen via push mechanisms anyway)
        // TODO and we would check the validity of the data by making a request to fetch
        //  e.g. account information.
        userDao.insertUser(DbUser(name = name, apiKey = apiKey))
    }
}