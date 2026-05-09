package com.example.leveluplife.data.disciplines

sealed class DisciplineError(open val message: String? = null) {
    object NotFound : DisciplineError()
    data class Network(override val message: String? = null) : DisciplineError(message)
    data class Server(override val message: String? = null) : DisciplineError(message)
    data class Unknown(override val message: String? = null) : DisciplineError(message)
}

class DisciplineErrorException(val disciplineError: DisciplineError) :
    RuntimeException(disciplineError.message ?: disciplineError::class.simpleName)
