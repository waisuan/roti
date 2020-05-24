package controllers

import exceptions.BadFileUploadException
import io.javalin.http.Context
import services.FileService

object FileController {
    fun getFileNames(ctx: Context) {
        ctx.json(FileService.getFileNames(ctx.pathParam("ownerId")))
    }

    fun getFile(ctx: Context) {
        val fileName = ctx.pathParam("fileName")
        ctx.result(FileService.getFile(ctx.pathParam("ownerId"), fileName))
        ctx.contentType("application/octet-stream")
        ctx.header("Content-Disposition", "attachment; filename=\"$fileName\"")
    }

    fun saveFile(ctx: Context) {
        val uploadedFile = ctx.uploadedFile("file") ?: throw BadFileUploadException()
        FileService.saveFile(ctx.pathParam("ownerId"), uploadedFile.filename, uploadedFile.content)
    }

    fun deleteFile(ctx: Context) {
        FileService.deleteFile(ctx.pathParam("ownerId"), ctx.pathParam("fileName"))
    }
}
