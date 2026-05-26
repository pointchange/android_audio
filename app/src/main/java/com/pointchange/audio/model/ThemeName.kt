package com.pointchange.audio.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.pointchange.audio.R
import com.pointchange.audio.ui.theme.ThreeColor

enum class ThemeName(@param:StringRes val text: Int) {
    DYNAMIC_COLOR(text = R.string.theme_Dynamic_Color) {
        override fun getColor() =
            ThreeColor(
                primaryLight = Color(0xFFD0BCFF),
                secondaryLight = Color(0xFFCCC2DC),
                tertiaryLight = Color(0xFFEFB8C8),
                primaryDark = Color(0xFF6650a4),
                secondaryDark = Color(0xFF625b71),
                tertiaryDark = Color(0xFF7D5260)
            )

    },
    SUBJECT_COLOR(text = R.string.theme_Subject_Color) {
        override fun getColor() =
            ThreeColor(
                primaryLight = Color(0xFF6650a4),
                secondaryLight = Color(0xFF625b71),
                tertiaryLight = Color(0xFF7D5260),

                primaryDark = Color(0xFFD0BCFF),
                secondaryDark = Color(0xFFCCC2DC),
                tertiaryDark = Color(0xFFEFB8C8)

            )

    },
    BUSINESS_BLUE(text = R.string.theme_Business_Blue) {
        override fun getColor() =
            ThreeColor(
                primaryLight = Color(0xFF005FB0),
                secondaryLight = Color(0xFF535F70),
                tertiaryLight = Color(0xFF6B5778),
                primaryDark = Color(0xFFA6C8FF),
                secondaryDark = Color(0xFFBBC7DB),
                tertiaryDark = Color(0xFFD6BAE2)
            )

    },
    ECO_GREEN(text = R.string.theme_Eco_Green) {
        override fun getColor() =
            ThreeColor(
                primaryLight = Color(0xFF386A20),
                secondaryLight = Color(0xFF55624C),
                tertiaryLight = Color(0xFF19676D),
                primaryDark = Color(0xFF9CD67D),
                secondaryDark = Color(0xFFBCCBB0),
                tertiaryDark = Color(0xFF8AD3D9)
            )

    },
    CREATIVE_VIOLET(text = R.string.theme_Creative_Violet) {
        override fun getColor() =
            ThreeColor(
                primaryLight = Color(0xFF6750A4),
                secondaryLight = Color(0xFF625B71),
                tertiaryLight = Color(0xFF7D5260),
                primaryDark = Color(0xFFD0BCFF),
                secondaryDark = Color(0xFFCCC2DC),
                tertiaryDark = Color(0xFFEFB8C8)
            )

    },
    WARM_AMBER(text = R.string.theme_Warm_Amber) {
        override fun getColor() =
            ThreeColor(
                primaryLight = Color(0xFF825500),
                secondaryLight = Color(0xFF6F5B40),
                tertiaryLight = Color(0xFF51643F),
                primaryDark = Color(0xFFFFB951),
                secondaryDark = Color(0xFFDCC48C),
                tertiaryDark = Color(0xFFB8CEA1)
            )

    },
    MODERN_TEAL(text = R.string.theme_Modern_Teal) {
        override fun getColor() =
            ThreeColor(
                primaryLight = Color(0xFF006A6A),
                secondaryLight = Color(0xFF4A6363),
                tertiaryLight = Color(0xFF4B607C),
                primaryDark = Color(0xFF84D3D2),
                secondaryDark = Color(0xFFB1CCCC),
                tertiaryDark = Color(0xFFB2C8E8)
            )

    };

    abstract fun getColor(): ThreeColor


}