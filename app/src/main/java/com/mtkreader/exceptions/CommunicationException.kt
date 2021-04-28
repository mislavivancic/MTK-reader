package com.mtkreader.exceptions

class CommunicationException(override val message: String = "Timed out!") : Exception(message)
