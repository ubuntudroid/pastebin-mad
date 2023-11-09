

package omg.lol.pastebin.test.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import omg.lol.pastebin.core.data.di.fakePastes
import omg.lol.pastebin.ui.MainActivity
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AppTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun test1() {
        composeTestRule.onNodeWithText(fakePastes.first().content, substring = true).assertExists()
    }
}
