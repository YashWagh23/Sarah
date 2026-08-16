package com.sarah.app.media

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.darwin.NSObject

/**
 * iOS Platform Bridge for document and photo picking.
 *
 * Privacy-first compliance:
 * - Does NOT request Camera, Microphone, or Location permissions.
 * - Uses PHPickerViewController for out-of-process user-selected photo picking without full library access.
 * - Uses UIDocumentPickerViewController for user-selected PDF/document picking.
 * - Returns pure ByteArray payloads to be processed by DocumentTextExtractor.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosMediaPickerBridge {

    fun pickPdf(onPdfPicked: (ByteArray?) -> Unit) {
        val rootVC = getRootViewController() ?: run {
            onPdfPicked(null)
            return
        }

        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                if (url != null) {
                    val data = NSData.dataWithContentsOfURL(url)
                    if (data != null && data.length > 0u) {
                        val bytes = data.bytes?.readBytes(data.length.toInt())
                        onPdfPicked(bytes)
                        return
                    }
                }
                onPdfPicked(null)
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onPdfPicked(null)
            }
        }

        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypePDF, UTTypeItem),
            asCopy = true
        )
        picker.delegate = delegate
        rootVC.presentViewController(picker, animated = true, completion = null)
    }

    fun pickImage(onImagePicked: (ByteArray?) -> Unit) {
        val rootVC = getRootViewController() ?: run {
            onImagePicked(null)
            return
        }

        val config = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter()
            selectionLimit = 1
        }

        val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, completion = null)
                val result = didFinishPicking.firstOrNull() as? PHPickerResult
                if (result == null) {
                    onImagePicked(null)
                    return
                }

                val itemProvider = result.itemProvider
                val typeIdentifier = UTTypeImage.identifier
                if (itemProvider.hasItemConformingToTypeIdentifier(typeIdentifier)) {
                    itemProvider.loadDataRepresentationForTypeIdentifier(typeIdentifier) { data, error ->
                        if (data != null && error == null && data.length > 0u) {
                            val bytes = data.bytes?.readBytes(data.length.toInt())
                            onImagePicked(bytes)
                        } else {
                            onImagePicked(null)
                        }
                    }
                } else {
                    onImagePicked(null)
                }
            }
        }

        val picker = PHPickerViewController(configuration = config)
        picker.delegate = delegate
        rootVC.presentViewController(picker, animated = true, completion = null)
    }

    private fun getRootViewController(): UIViewController? {
        val windows = UIApplication.sharedApplication.windows
        val keyWindow = UIApplication.sharedApplication.keyWindow
            ?: windows.firstOrNull { (it as? platform.UIKit.UIWindow)?.isKeyWindow() == true } as? platform.UIKit.UIWindow
            ?: windows.firstOrNull() as? platform.UIKit.UIWindow
        return keyWindow?.rootViewController
    }
}
