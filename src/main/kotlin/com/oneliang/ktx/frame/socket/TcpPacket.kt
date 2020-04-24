package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.common.toByteArray
import java.io.ByteArrayOutputStream

class TcpPacket constructor(var type: Int, var body: ByteArray = ByteArray(0)) : Packet {

    @Throws(Exception::class)
    override fun toByteArray(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.write(this.type.toByteArray())
        val bodyLengthByteArray = body.size.toByteArray()
        byteArrayOutputStream.write(bodyLengthByteArray)
        byteArrayOutputStream.write(body)
        return byteArrayOutputStream.toByteArray()
    }
}