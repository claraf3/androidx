/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.ui.core.focus

import androidx.compose.foundation.Box
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.test.filters.SmallTest
import androidx.ui.core.focus.FocusState2.Inactive
import androidx.ui.test.createComposeRule
import androidx.ui.test.runOnIdle
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@SmallTest
@OptIn(ExperimentalFocus::class)
@RunWith(JUnit4::class)
class FindFocusableChildrenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun returnsFirstFocusNodeInModifierChain() {
        val focusModifier1 = FocusModifier2(Inactive)
        val focusModifier2 = FocusModifier2(Inactive)
        val focusModifier3 = FocusModifier2(Inactive)
        // Arrange.
        // layoutNode--focusNode1--focusNode2--focusNode3
        composeTestRule.setContent {
            Box(modifier = focusModifier1.then(focusModifier2).then(focusModifier3))
        }

        // Act.
        val focusableChildren = runOnIdle {
            focusModifier1.focusNode.focusableChildren()
        }

        // Assert.
        runOnIdle {
            assertThat(focusableChildren).containsExactly(focusModifier2.focusNode)
        }
    }

    @Test
    fun skipsNonFocusNodesAndReturnsFirstFocusNodeInModifierChain() {
        val focusModifier1 = FocusModifier2(Inactive)
        val focusModifier2 = FocusModifier2(Inactive)
        // Arrange.
        // layoutNode--focusNode1--nonFocusNode--focusNode2
        composeTestRule.setContent {
            Box(focusModifier1.background(color = Red).then(focusModifier2))
        }

        // Act.
        val focusableChildren = runOnIdle {
            focusModifier1.focusNode.focusableChildren()
        }

        // Assert.
        runOnIdle {
            assertThat(focusableChildren).containsExactly(focusModifier2.focusNode)
        }
    }

    @Test
    fun returnsFirstFocusChildOfEachChildLayoutNode() {
        // Arrange.
        // parentLayoutNode--parentFocusNode
        //       |___________________________________
        //       |                                   |
        // childLayoutNode1--focusNode1          childLayoutNode2--focusNode2--focusNode3
        val parentFocusModifier = FocusModifier2(Inactive)
        val focusModifier1 = FocusModifier2(Inactive)
        val focusModifier2 = FocusModifier2(Inactive)
        val focusModifier3 = FocusModifier2(Inactive)
        composeTestRule.setContent {
            Box(modifier = parentFocusModifier) {
                Box(modifier = focusModifier1)
                Box(modifier = focusModifier2.then(focusModifier3))
            }
        }

        // Act.
        val focusableChildren = runOnIdle {
            parentFocusModifier.focusNode.focusableChildren()
        }

        // Assert.
        runOnIdle {
            assertThat(focusableChildren).containsExactly(
                focusModifier1.focusNode, focusModifier2.focusNode
            )
        }
    }
}
