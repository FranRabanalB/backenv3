import com.example.config.AuthConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {  // Eliminado el parámetro 'name:' ya que no es necesario
            realm = AuthConfig.REALM
            verifier(AuthConfig.makeJwtVerifier())
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val roles = credential.payload.getClaim("roles").asList(String::class.java)

                if (userId != null && roles.isNotEmpty()) {
                    JWTPrincipal(credential.payload) // Retorna el principal si es válido
                } else {
                    null // Retorna null si la validación falla
                }
            }
        }
    }
}