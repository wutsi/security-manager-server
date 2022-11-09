package com.wutsi.security.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import kotlin.Long
import kotlin.String

public data class CreatePasswordRequest(
    @get:NotBlank
    public val `value`: String = "",
    public val accountId: Long = 0,
    @get:NotBlank
    @get:Size(max = 30)
    public val username: String = ""
)
