package com.wutsi.security.service

import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.security.dao.PasswordRepository
import com.wutsi.security.dto.CreatePasswordRequest
import com.wutsi.security.dto.UpdatePasswordRequest
import com.wutsi.security.dto.VerifyPasswordRequest
import com.wutsi.security.entity.PasswordEntity
import com.wutsi.security.error.ErrorURN
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID

@Service
public class PasswordService(
    private val dao: PasswordRepository
) {
    fun create(request: CreatePasswordRequest): PasswordEntity {
        val salt = UUID.randomUUID().toString()
        return dao.save(
            PasswordEntity(
                accountId = request.accountId,
                value = hash(request.accountId, request.value, salt),
                salt = salt
            )
        )
    }

    fun update(id: Long, request: UpdatePasswordRequest) {
        val password = findById(id)
        password.value = hash(password.accountId, request.value, password.salt)
        dao.save(password)
    }

    fun verify(id: Long, request: VerifyPasswordRequest) {
        val password = findById(id)
        val value = hash(password.id!!, request.value, password.salt)
        if (value != password.value) {
            throw NotFoundException(
                error = Error(
                    code = ErrorURN.PASSWORD_MISMATCH.urn
                )
            )
        }
    }

    fun delete(id: Long) {
        val password = dao.findById(id)
            .orElse(null)
        if (password?.isDeleted == false) {
            password.isDeleted = true
            password.deleted = Date()
            dao.save(password)
        }
    }

    private fun findById(id: Long): PasswordEntity {
        val password = dao.findById(id)
            .orElseThrow {
                notFound(id)
            }
        if (password.isDeleted) {
            throw notFound(id)
        }
        return password
    }

    private fun notFound(id: Long) = NotFoundException(
        error = Error(
            code = ErrorURN.PASSWORD_NOT_FOUND.urn,
            parameter = Parameter(
                name = "id",
                type = ParameterType.PARAMETER_TYPE_PATH,
                value = id.toString()
            )
        )
    )

    private fun hash(accountId: Long, value: String, salt: String): String =
        DigestUtils.md5Hex("$accountId-$value-$salt")
}