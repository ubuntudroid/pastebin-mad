

package omg.lol.pastebin.feature.pastes.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import omg.lol.pastebin.core.data.di.fakePastes
import omg.lol.pastebin.core.ui.UiResource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [PurePastesScreen].
 */
@RunWith(AndroidJUnit4::class)
class PastesListTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent {
            PastesList(
                pastesResource = UiResource.Success(fakePastes),
                onPasteClick = {}
            )
        }
    }
    @Test
    fun firstItem_exists() {
        composeTestRule.onNodeWithText(fakePastes.first().title).assertExists().performClick()
    }
}
