package com.sotopatrick.onjoue.detekt

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class DesignSystemRuleSetProvider : RuleSetProvider {
    override val ruleSetId: RuleSetId = RuleSetId("onjoue-design-system")

    override fun instance(): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            ::RawColorUsage,
            ::HardcodedDimension,
            ::SpacingArithmetic,
            ::DirectMaterialThemeUsage,
            ::DirectTextUsage,
            ::ForbiddenMaterialImport,
        ),
    )
}
