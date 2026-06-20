package com.example.leveluplife.ui.auth

data class CharacterClassOption(
    val id: Int,
    val labelRes: Int,
)

object CharacterClasses {
    val available = listOf(
        CharacterClassOption(id = 1, labelRes = com.example.leveluplife.R.string.register_class_warrior),
    )
}
