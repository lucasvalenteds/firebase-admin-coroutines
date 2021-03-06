package kotlinx.coroutines.experimental.firebase.tasks

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseCredentials
import com.google.firebase.auth.UserRecord
import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.FileInputStream
import java.nio.file.Paths

@TestInstance(Lifecycle.PER_CLASS)
class TaskApiTest {

    private val credentialsPath = Paths.get(".").resolve("..").resolve("google-services.json")

    @BeforeAll
    fun setUpFirebase() {
        FileInputStream(credentialsPath.toFile()).use {
            FirebaseApp.initializeApp(FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(it))
                    .build())
        }
    }

    @Test
    fun `A Task can return a result`() = runBlocking<Unit> {
        val auth = FirebaseAuth.getInstance()

        val (name, email) = "John" to "example@email.com"
        auth.createUser(UserRecord.CreateRequest()
                .setDisplayName(name)
                .setEmail(email)).await()

        val user = auth.getUserByEmail(email).await()

        assertEquals(user.displayName, name)
        assertEquals(user.email, email)

        auth.deleteUser(user.uid).await()
    }

    @Test
    fun `A Task can throw an exception`() {
        val invalidEmail = "not.registered@email.com"

        assertThrows(FirebaseAuthException::class.java) {
            runBlocking {
                FirebaseAuth.getInstance().getUserByEmail(invalidEmail).await()
            }
        }
    }
}
