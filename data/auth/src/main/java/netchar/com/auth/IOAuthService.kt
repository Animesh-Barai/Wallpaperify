package netchar.com.auth


/**
 * Created by Netchar on 31.03.2019.
 * e.glushankov@gmail.com
 */
interface IOAuthService {
    fun isAuthorized(): Boolean
    fun getUserApiAccessTokenKey(): String
}