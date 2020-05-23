package controllers

import exceptions.BadFileUploadException
import io.javalin.http.Context
import services.FileService

object FileController {
    fun getFile(ctx: Context) {
        val ownerId = ctx.pathParam("ownerId")
        val fileName = ctx.pathParam("filename")
        ctx.result(FileService.getFile(ownerId, fileName))
        ctx.contentType("application/octet-stream")
        ctx.header("Content-Disposition", "attachment; filename=\"$fileName\"")
    }

    fun saveFile(ctx: Context) {
        val uploadedFile = ctx.uploadedFile("file") ?: throw BadFileUploadException()
        FileService.saveFile(ctx.pathParam("ownerId"), uploadedFile.filename, uploadedFile.content)
    }
}
